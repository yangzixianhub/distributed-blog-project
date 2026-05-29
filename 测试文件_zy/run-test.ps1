param(
    [string]$JMeterBin = "jmeter",
    [string]$TestPlan = "E:/gether/new/测试文件_zy/article-search-test.jmx",
    [string]$ResultsFile = "E:/gether/new/测试文件_zy/reports/article-search-results.jtl",
    [string]$DashboardDir = "E:/gether/new/测试文件_zy/reports/dashboard"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$resultsParent = Split-Path -Parent $ResultsFile
if (!(Test-Path $resultsParent)) {
    New-Item -ItemType Directory -Path $resultsParent -Force | Out-Null
}

if (Test-Path $ResultsFile) {
    Remove-Item -LiteralPath $ResultsFile -Force
}

if (Test-Path $DashboardDir) {
    Remove-Item -LiteralPath $DashboardDir -Recurse -Force
}

Write-Host "Running JMeter test plan: $TestPlan"
& $JMeterBin -n -t $TestPlan -l $ResultsFile
if ($LASTEXITCODE -ne 0) {
    throw "JMeter test execution failed with exit code: $LASTEXITCODE"
}

Write-Host "Generating dashboard: $DashboardDir"
& $JMeterBin -g $ResultsFile -o $DashboardDir
if ($LASTEXITCODE -ne 0) {
    throw "JMeter dashboard generation failed with exit code: $LASTEXITCODE"
}

Write-Host "Test completed."
Write-Host "Results file: $ResultsFile"
Write-Host "Dashboard: $DashboardDir"
