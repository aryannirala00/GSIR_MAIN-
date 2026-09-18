$ErrorActionPreference = "Stop"

Write-Host "Running clean test..."
& .\mvnw.cmd clean test -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed!"
    exit 1
}

Write-Host "Running clean package..."
& .\mvnw.cmd clean package -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Package failed!"
    exit 1
}

Write-Host "All good!"
exit 0

