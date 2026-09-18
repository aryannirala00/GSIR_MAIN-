$ErrorActionPreference = "Stop"
Write-Host "Running 5x Full Test Suite"
for ($i = 1; $i -le 5; $i++) {
    Write-Host "Full Test Run $i/5"
    .\mvnw.cmd clean test -B
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Full Test Run $i failed!"
        exit 1
    }
}
Write-Host "Running 10x Auth/Concurrency Tests"
for ($i = 1; $i -le 10; $i++) {
    Write-Host "Auth/Concurrency Test Run $i/10"
    .\mvnw.cmd test -Dtest="AuthIntegrationTest,AuthServiceConcurrencyTest,JwtServiceTest" -B
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Auth/Concurrency Test Run $i failed!"
        exit 1
    }
}
Write-Host "ALL TESTS PASSED SUCCESSFULLY."
