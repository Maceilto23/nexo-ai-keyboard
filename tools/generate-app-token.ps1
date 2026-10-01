$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$token = ($bytes | ForEach-Object { $_.ToString("x2") }) -join ""
Write-Host "APP_TOKEN gerado:" -ForegroundColor Cyan
Write-Host $token
Write-Host "\nGuarde este valor. Use o MESMO token no Render e no app Nexo AI." -ForegroundColor Yellow
