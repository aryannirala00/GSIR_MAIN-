$ErrorActionPreference = "Stop"
Write-Host "Cleaning target..."
Remove-Item -Recurse -Force "c:\AI START UP\gsir-backend\target" -ErrorAction SilentlyContinue

Write-Host "Running 5 full suites..."
for ($i=1; $i -le 5; $i++) {
    Write-Host "Run $i"
    .\mvnw.cmd clean test -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FAILED AT FULL RUN $i"
        exit 1
    }
}

Write-Host "Running 20 Part 19 tests..."
for ($i=1; $i -le 20; $i++) {
    Write-Host "Part 19 Run $i"
    .\mvnw.cmd test -Dtest=NoteServiceTest -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FAILED AT PART 19 RUN $i"
        exit 1
    }
}

Write-Host "Running clean package..."
.\mvnw.cmd clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAILED AT PACKAGE"
    exit 1
}

Write-Host "ALL VERIFICATIONS PASSED"

