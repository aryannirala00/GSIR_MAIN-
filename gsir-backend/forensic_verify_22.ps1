$ErrorActionPreference = "Stop"

for ($i = 1; $i -le 5; $i++) {
    Write-Host "Run #$i Full Suite"
    & .\mvnw.cmd clean test -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Full Suite Run #$i Failed!"
        exit 1
    }
}

for ($i = 1; $i -le 10; $i++) {
    Write-Host "Run #$i FileServiceTest"
    & .\mvnw.cmd clean test -Dtest=FileServiceTest -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FileServiceTest Run #$i Failed!"
        exit 1
    }
}

Write-Host "Running clean package..."
& .\mvnw.cmd clean package -DskipTests=false -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Package failed!"
    exit 1
}

Write-Host "All good!"
exit 0

