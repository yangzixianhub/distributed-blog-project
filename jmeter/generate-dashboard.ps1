param(
    [string]$JMeterBin = "jmeter",
    [string]$ResultsFile = "E:/Blog/jmeter/reports/article-search-results.jtl",
    [string]$DashboardDir = "E:/Blog/jmeter/reports/dashboard"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

if (!(Test-Path $ResultsFile)) {
    throw "Results file not found: $ResultsFile"
}

if (Test-Path $DashboardDir) {
    Remove-Item -Recurse -Force $DashboardDir
}

& $JMeterBin -g $ResultsFile -o $DashboardDir

Write-Host "Dashboard generated at: $DashboardDir"
