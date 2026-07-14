$loginPayload = @{
    username = "admin"
    password = "password"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -Body $loginPayload -ContentType "application/json"
$token = $response.Trim()

$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/test/debug" -Method Get -Headers $headers | ConvertTo-Json -Depth 5
