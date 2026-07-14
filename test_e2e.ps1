$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080"
$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()

Write-Host "=== CLEAN E2E TEST (timestamp: $ts) ==="

# 1. Register a recruiter
$recUser = "rec_$ts"
$recBody = @{
    username = $recUser
    password = "Test1234"
    email    = "rec_${ts}@test.com"
    role     = "RECRUITER"
    recruiterName = "Test Recruiter"
    companyName   = "Test Corp"
    designation   = "HR"
    phone         = "1234567890"
} | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $recBody -ContentType "application/json"
    Write-Host "[OK] Recruiter registered: $recUser"
} catch { Write-Host "[FAIL] Register recruiter: $_"; exit 1 }

# 2. Login as admin to approve
#    First, we need a working admin. Let's register one if needed.
$adminBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
try {
    $adminToken = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminBody -ContentType "application/json"
    Write-Host "[OK] Admin login, token: $($adminToken.Substring(0,20))..."
} catch {
    Write-Host "[WARN] Admin login failed, trying to register..."
    $adminRegBody = @{
        username = "admin"
        password = "admin123"
        email    = "admin@test.com"
        role     = "ADMIN"
    } | ConvertTo-Json
    try {
        Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $adminRegBody -ContentType "application/json"
        $adminToken = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminBody -ContentType "application/json"
        Write-Host "[OK] Admin registered and logged in"
    } catch { Write-Host "[FAIL] Admin setup: $_"; exit 1 }
}
$adminHeaders = @{ Authorization = "Bearer $adminToken" }

# 3. Get recruiters list to find our recruiter's ID
$recruiters = Invoke-RestMethod -Uri "$baseUrl/admin/recruiters?page=0&size=100" -Method Get -Headers $adminHeaders
Write-Host "[INFO] Total recruiters: $($recruiters.content.Count)"
$myRec = $recruiters.content | Where-Object { $_.username -eq $recUser } | Select-Object -First 1
if (-not $myRec) {
    # Try non-paged endpoint
    $recruiters2 = Invoke-RestMethod -Uri "$baseUrl/admin/recruiters" -Method Get -Headers $adminHeaders
    if ($recruiters2 -is [Array]) {
        $myRec = $recruiters2 | Where-Object { $_.username -eq $recUser } | Select-Object -First 1
    }
}
if (-not $myRec) { Write-Host "[FAIL] Could not find recruiter $recUser in admin list"; exit 1 }
$recId = $myRec.id
Write-Host "[OK] Found recruiter ID: $recId"

# 4. Approve recruiter
try {
    Invoke-RestMethod -Uri "$baseUrl/admin/recruiters/$recId/approve" -Method Put -Headers $adminHeaders
    Write-Host "[OK] Recruiter approved"
} catch { Write-Host "[FAIL] Approve recruiter: $_"; exit 1 }

# 5. Login as recruiter
$recLoginBody = @{ username = $recUser; password = "Test1234" } | ConvertTo-Json
try {
    $recToken = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $recLoginBody -ContentType "application/json"
    Write-Host "[OK] Recruiter login, token: $($recToken.Substring(0,20))..."
} catch { Write-Host "[FAIL] Recruiter login: $_"; exit 1 }
$recHeaders = @{ Authorization = "Bearer $recToken" }

# 6. Post a job
$jobBody = @{
    title       = "Test Job $ts"
    company     = "Test Corp"
    salary      = 100000
    description = "A test job"
    location    = "Remote"
    jobType     = "FULL_TIME"
} | ConvertTo-Json
try {
    $job = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $jobBody -ContentType "application/json" -Headers $recHeaders
    $jobId = $job.id
    Write-Host "[OK] Job posted, ID: $jobId"
} catch { Write-Host "[FAIL] Post job: $_"; exit 1 }

# 7. Verify job has recruiter linked (via test debug endpoint)
$debug = Invoke-RestMethod -Uri "$baseUrl/test/debug" -Method Get
$debugJob = $debug | Where-Object { $_ } # just check it works
Write-Host "[INFO] Debug endpoint returned data"

# 8. Register a student
$stuUser = "stu_$ts"
$stuBody = @{
    username = $stuUser
    password = "Test1234"
    email    = "stu_${ts}@test.com"
    role     = "STUDENT"
    name     = "Test Student"
    branch   = "CSE"
    year     = 4
    college  = "Test University"
    skills   = "Java,Python"
    cgpa     = 8.5
    phone    = "9876543210"
} | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $stuBody -ContentType "application/json"
    Write-Host "[OK] Student registered: $stuUser"
} catch { Write-Host "[FAIL] Register student: $_"; exit 1 }

# 9. Login as student
$stuLoginBody = @{ username = $stuUser; password = "Test1234" } | ConvertTo-Json
try {
    $stuToken = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $stuLoginBody -ContentType "application/json"
    Write-Host "[OK] Student login"
} catch { Write-Host "[FAIL] Student login: $_"; exit 1 }
$stuHeaders = @{ Authorization = "Bearer $stuToken" }

# 10. Apply for the job
try {
    $app = Invoke-RestMethod -Uri "$baseUrl/apply?jobId=$jobId" -Method Post -Headers $stuHeaders
    Write-Host "[OK] Applied for job $jobId, application ID: $($app.id)"
} catch { Write-Host "[FAIL] Apply for job: $_"; exit 1 }

# 11. Check recruiter applications
try {
    $recApps = Invoke-RestMethod -Uri "$baseUrl/apply/recruiter/my?page=0&size=10" -Method Get -Headers $recHeaders
    Write-Host ""
    Write-Host "========================================="
    Write-Host "RECRUITER APPLICATIONS RESULT:"
    Write-Host "Total elements: $($recApps.totalElements)"
    Write-Host "Content count:  $($recApps.content.Count)"
    if ($recApps.content.Count -gt 0) {
        Write-Host "[SUCCESS] Applications ARE visible to recruiter!"
        $recApps.content | ForEach-Object {
            Write-Host "  - App ID: $($_.id), Status: $($_.status), Student: $($_.student.name), Job: $($_.job.title)"
        }
    } else {
        Write-Host "[BUG] Applications NOT visible to recruiter!"
    }
    Write-Host "========================================="
} catch { Write-Host "[FAIL] Get recruiter apps: $_"; exit 1 }

# 12. Check admin applications for comparison
try {
    $adminApps = Invoke-RestMethod -Uri "$baseUrl/apply/all?page=0&size=100" -Method Get -Headers $adminHeaders
    Write-Host ""
    Write-Host "ADMIN APPLICATIONS (last 3):"
    $adminApps.content | Select-Object -Last 3 | ForEach-Object {
        Write-Host "  - App ID: $($_.id), Status: $($_.status), Student: $($_.student.name), Job: $($_.job.title)"
    }
} catch { Write-Host "[FAIL] Get admin apps: $_"; exit 1 }
