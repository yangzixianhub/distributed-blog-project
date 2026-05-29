# JMeter Search Test

## Files

- `article-search-test.jmx`: search pressure test plan
- `search-data.csv`: search request data source
- `E:/Blog/jmeter/reports/article-search-results.jtl`: raw result file
- `E:/Blog/jmeter/reports/dashboard/`: HTML dashboard output
- `generate-dashboard.ps1`: generate HTML report from `.jtl`

## Run Test In GUI

1. Open `article-search-test.jmx`
2. Run the test
3. Raw results will be written to `E:/Blog/jmeter/reports/article-search-results.jtl`

## Generate HTML Dashboard

If `jmeter` is already in your `PATH`:

```powershell
.\generate-dashboard.ps1
```

If you want to specify the JMeter executable explicitly:

```powershell
.\generate-dashboard.ps1 -JMeterBin "D:\apache-jmeter-5.6.3\bin\jmeter.bat"
```

After generation, open:

```text
E:/Blog/jmeter/reports/dashboard/index.html
```
