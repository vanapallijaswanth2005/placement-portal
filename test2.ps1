$loginPayload = @{
    username = "admin"
    password = "password"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -Body $loginPayload -ContentType "application/json"
$token = $response.Trim()

$headers = @{
    Authorization = "Bearer $token"
}

Write-Host "Getting apps..."
try {
    $apps = Invoke-RestMethod -Uri "http://localhost:8080/apply/all" -Method Get -Headers $headers
    $apps | ConvertTo-Json -Depth 5
} catch {
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $reader.ReadToEnd()
    }
}
