$baseUrl = "http://localhost:8080"

# Register Recruiter
$rPayload = @{ username="rec1"; password="p"; email="r@c.com"; role="RECRUITER" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $rPayload -ContentType "application/json"

# Approve Recruiter
$adminLogin = @{ username="admin"; password="password" } | ConvertTo-Json
$adminToken = (Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminLogin -ContentType "application/json").Trim()
Invoke-RestMethod -Uri "$baseUrl/admin/recruiters/1/approve" -Method Put -Headers @{Authorization="Bearer $adminToken"}

# Login Recruiter
$rToken = (Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body (@{ username="rec1"; password="p" } | ConvertTo-Json) -ContentType "application/json").Trim()
$rHeaders = @{Authorization="Bearer $rToken"}

# Create Job
$jobPayload = @{ title="Dev"; company="C"; salary=100 } | ConvertTo-Json
$job = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $jobPayload -ContentType "application/json" -Headers $rHeaders
$jobId = $job.id
Write-Host "Created job $jobId"

# Register Student
$sPayload = @{ username="stu1"; password="p"; email="s@c.com"; role="STUDENT" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $sPayload -ContentType "application/json"

# Login Student
$sToken = (Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body (@{ username="stu1"; password="p" } | ConvertTo-Json) -ContentType "application/json").Trim()
$sHeaders = @{Authorization="Bearer $sToken"}

# Apply Job
Invoke-RestMethod -Uri "$baseUrl/apply?jobId=$jobId" -Method Post -Headers $sHeaders

# Get Recruiter Applications
$apps = Invoke-RestMethod -Uri "$baseUrl/apply/recruiter/my" -Method Get -Headers $rHeaders
Write-Host "Recruiter Apps:"
$apps | ConvertTo-Json -Depth 5
