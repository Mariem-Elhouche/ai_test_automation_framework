param(
    [string]$FrameworkRoot = "",
    [string]$ColabMetricsUrl = "",
    [int]$TimeoutSeconds = 6
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($FrameworkRoot)) {
    $FrameworkRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
} else {
    $FrameworkRoot = (Resolve-Path $FrameworkRoot).Path
}

$targetDir = Join-Path $FrameworkRoot "target"
$dashboardDir = Join-Path $targetDir "dashboard"
$outputFile = Join-Path $dashboardDir "index.html"
$configPath = Join-Path $FrameworkRoot "src\main\resources\config.properties"

function Read-Properties {
    param([string]$Path)
    $props = @{}
    if (-not (Test-Path $Path)) { return $props }
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#") -or -not $line.Contains("=")) { return }
        $parts = $line.Split("=", 2)
        $props[$parts[0].Trim()] = $parts[1].Trim()
    }
    return $props
}

function Get-ColabBaseUrl {
    param([hashtable]$Properties)
    $apiUrl = ""
    if ($Properties.ContainsKey("self.healing.api.url")) {
        $apiUrl = [string]$Properties["self.healing.api.url"]
    }
    if (-not [string]::IsNullOrWhiteSpace($apiUrl)) {
        return ($apiUrl -replace "/heal/?$", "")
    }
    if ($Properties.ContainsKey("colab.url")) {
        $colabUrl = [string]$Properties["colab.url"]
        if (-not [string]::IsNullOrWhiteSpace($colabUrl)) {
            return $colabUrl.TrimEnd("/")
        }
    }
    return $null
}

function Safe-Count {
    param($InputObject)
    if ($null -eq $InputObject) { return 0 }
    if ($InputObject -is [System.Array]) { return $InputObject.Count }
    return 1
}

function Get-PropertyValue {
    param(
        $Object,
        [string]$Name,
        $Default = $null
    )
    if ($null -eq $Object) { return $Default }
    $prop = $Object.PSObject.Properties[$Name]
    if ($null -eq $prop) { return $Default }
    return $prop.Value
}

$scenarioTotal = 0
$scenarioPassed = 0
$scenarioFailed = 0
$scenarioSkipped = 0

$stepTotal = 0
$stepPassed = 0
$stepFailed = 0
$stepSkipped = 0
$totalStepDurationNs = 0.0

$tagMap = @{}
$failedStepStatuses = @("failed", "ambiguous", "undefined", "pending")

$cucumberFiles = @()
if (Test-Path $targetDir) {
    $cucumberFiles = @(Get-ChildItem -Path $targetDir -Filter "cucumber*.json" -File -ErrorAction SilentlyContinue)
}

foreach ($file in $cucumberFiles) {
    try {
        $content = Get-Content -Raw $file.FullName
        if ([string]::IsNullOrWhiteSpace($content)) { continue }
        $features = $content | ConvertFrom-Json
    } catch {
        continue
    }

    foreach ($feature in @($features)) {
        foreach ($scenario in @($feature.elements)) {
            if ($scenario.type -ne "scenario") { continue }

            $scenarioTotal++
            $scenarioState = "passed"

            foreach ($step in @($scenario.steps)) {
                $result = Get-PropertyValue -Object $step -Name "result" -Default $null
                $status = [string](Get-PropertyValue -Object $result -Name "status" -Default "unknown")
                $status = $status.ToLowerInvariant()
                $duration = 0.0
                $durationValue = Get-PropertyValue -Object $result -Name "duration" -Default $null
                if ($null -ne $durationValue) {
                    $duration = [double]$durationValue
                }

                $stepTotal++
                $totalStepDurationNs += $duration

                if ($status -eq "passed") {
                    $stepPassed++
                } elseif ($status -eq "skipped") {
                    $stepSkipped++
                } else {
                    $stepFailed++
                }

                if ($failedStepStatuses -contains $status) {
                    $scenarioState = "failed"
                } elseif ($status -eq "skipped" -and $scenarioState -eq "passed") {
                    $scenarioState = "skipped"
                }
            }

            if ($scenarioState -eq "passed") {
                $scenarioPassed++
            } elseif ($scenarioState -eq "failed") {
                $scenarioFailed++
            } else {
                $scenarioSkipped++
            }

            foreach ($tag in @((Get-PropertyValue -Object $scenario -Name "tags" -Default @()))) {
                $tagName = [string](Get-PropertyValue -Object $tag -Name "name" -Default "")
                if ([string]::IsNullOrWhiteSpace($tagName)) { continue }
                if (-not $tagMap.ContainsKey($tagName)) { $tagMap[$tagName] = 0 }
                $tagMap[$tagName]++
            }
        }
    }
}

$requestFiles = @()
$reportFiles = @()
if (Test-Path (Join-Path $targetDir "healing-debug")) {
    $requestFiles = @(Get-ChildItem -Path (Join-Path $targetDir "healing-debug") -Filter "*-request.json" -File -ErrorAction SilentlyContinue)
}
if (Test-Path (Join-Path $targetDir "reports")) {
    $reportFiles = @(Get-ChildItem -Path (Join-Path $targetDir "reports") -Filter "*.html" -File -Recurse -ErrorAction SilentlyContinue)
}

$scenarioPassRate = if ($scenarioTotal -gt 0) { [Math]::Round(($scenarioPassed * 100.0) / $scenarioTotal, 1) } else { 0.0 }
$avgStepDurationMs = if ($stepTotal -gt 0) { [Math]::Round(($totalStepDurationNs / $stepTotal) / 1000000.0, 1) } else { 0.0 }

$allSourceFiles = @($cucumberFiles + $requestFiles + $reportFiles)
$lastRunUtc = $null
if ((Safe-Count $allSourceFiles) -gt 0) {
    $lastWrite = ($allSourceFiles | Sort-Object LastWriteTimeUtc -Descending | Select-Object -First 1).LastWriteTimeUtc
    $lastRunUtc = $lastWrite.ToString("o")
}

$topTags = $tagMap.GetEnumerator() |
    Sort-Object -Property Value -Descending |
    Select-Object -First 10 |
    ForEach-Object { [PSCustomObject]@{ tag = $_.Key; count = $_.Value } }

$frameworkMetrics = [ordered]@{
    cucumber_files = @($cucumberFiles | ForEach-Object { $_.Name })
    scenarios = [ordered]@{
        total = $scenarioTotal
        passed = $scenarioPassed
        failed = $scenarioFailed
        skipped = $scenarioSkipped
        pass_rate_percent = $scenarioPassRate
    }
    steps = [ordered]@{
        total = $stepTotal
        passed = $stepPassed
        failed = $stepFailed
        skipped = $stepSkipped
        avg_duration_ms = $avgStepDurationMs
    }
    healing_debug = [ordered]@{
        request_payload_count = (Safe-Count $requestFiles)
    }
    reports = [ordered]@{
        html_report_count = (Safe-Count $reportFiles)
    }
    top_tags = @($topTags)
    last_run_utc = $lastRunUtc
}

$props = Read-Properties -Path $configPath
$colabBaseUrl = Get-ColabBaseUrl -Properties $props

$colabCandidates = @()
if (-not [string]::IsNullOrWhiteSpace($ColabMetricsUrl)) {
    $colabCandidates += $ColabMetricsUrl.TrimEnd("/")
} elseif (-not [string]::IsNullOrWhiteSpace($colabBaseUrl)) {
    $colabCandidates += "$($colabBaseUrl.TrimEnd('/'))/metrics"
    $colabCandidates += "$($colabBaseUrl.TrimEnd('/'))/healing/metrics"
    $colabCandidates += "$($colabBaseUrl.TrimEnd('/'))/self-healing/metrics"
}

$colabMetricsResult = [ordered]@{ status = "unavailable"; error = "No Colab URL configured." }
if ((Safe-Count $colabCandidates) -gt 0) {
    $errors = New-Object System.Collections.Generic.List[string]
    foreach ($url in $colabCandidates) {
        try {
            $resp = Invoke-RestMethod -Uri $url -Method Get -Headers @{
                "Accept" = "application/json"
                "ngrok-skip-browser-warning" = "true"
                "User-Agent" = "ai-test-dashboard/1.0"
            } -TimeoutSec $TimeoutSeconds

            $metrics = $resp
            if ($resp -is [hashtable] -and $resp.ContainsKey("metrics")) {
                $metrics = $resp["metrics"]
            } elseif ($resp.PSObject.Properties.Name -contains "metrics") {
                $metrics = $resp.metrics
            }

            $colabMetricsResult = [ordered]@{
                status = "ok"
                url = $url
                metrics = $metrics
            }
            break
        } catch {
            $errors.Add("$url -> $($_.Exception.Message)")
        }
    }
    if ($colabMetricsResult.status -ne "ok") {
        $colabMetricsResult = [ordered]@{
            status = "unavailable"
            error = ($errors -join " ; ")
        }
    }
}

function Card([string]$title, [string]$value) {
    return "<div class='card'><div class='label'>$title</div><div class='value'>$value</div></div>"
}

$frameworkCards = @(
    (Card "Scenarios" "$($frameworkMetrics.scenarios.total)")
    (Card "Pass rate" "$($frameworkMetrics.scenarios.pass_rate_percent)%")
    (Card "Scenarios failed" "$($frameworkMetrics.scenarios.failed)")
    (Card "Steps total" "$($frameworkMetrics.steps.total)")
    (Card "Avg step duration" "$($frameworkMetrics.steps.avg_duration_ms) ms")
    (Card "Healing payloads" "$($frameworkMetrics.healing_debug.request_payload_count)")
    (Card "HTML reports" "$($frameworkMetrics.reports.html_report_count)")
) -join ""

$colabCards = ""
$colabRawJson = "{}"
if ($colabMetricsResult.status -eq "ok") {
    $m = $colabMetricsResult.metrics
    $colabCards = @(
        (Card "Endpoint" "$($colabMetricsResult.url)")
        (Card "Healing requests" "$($m.total_healing_requests)")
        (Card "Healing success" "$($m.successful_healings)")
        (Card "Healing failed" "$($m.failed_healings)")
        (Card "Healing rate" "$($m.healing_rate)")
        (Card "Baseline hit rate" "$($m.baseline_hit_rate)")
        (Card "Avg healing time" "$($m.avg_healing_time_ms) ms")
        (Card "Avg final score" "$($m.avg_final_score)")
        (Card "Avg structural score" "$($m.avg_structural_score)")
        (Card "Avg semantic score" "$($m.avg_semantic_score)")
        (Card "NLP filter efficiency" "$($m.nlp_filter_efficiency)")
    ) -join ""
    $colabRawJson = ($m | ConvertTo-Json -Depth 20)
} else {
    $colabCards = "<div class='warning'>Métriques Colab indisponibles: $($colabMetricsResult.error)</div>"
}

$tagsRows = @($frameworkMetrics.top_tags | ForEach-Object {
    "<tr><td>$($_.tag)</td><td>$($_.count)</td></tr>"
}) -join ""
if ([string]::IsNullOrWhiteSpace($tagsRows)) {
    $tagsRows = "<tr><td colspan='2'>Aucun tag détecté.</td></tr>"
}

$frameworkRawJson = $frameworkMetrics | ConvertTo-Json -Depth 20
$generatedAt = (Get-Date).ToString("o")

$html = @"
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Automation Dashboard</title>
  <style>
    body { margin: 0; font-family: Arial, sans-serif; background: #f5f7fb; color: #1a1f36; }
    .container { max-width: 1200px; margin: 24px auto; padding: 0 16px 24px; }
    h1, h2 { margin: 0 0 12px; font-weight: 600; }
    .meta { color: #4a5674; margin-bottom: 20px; font-size: 14px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 12px; margin-bottom: 16px; }
    .card { background: #fff; border: 1px solid #dce1ef; border-radius: 8px; padding: 12px; }
    .label { color: #59627a; font-size: 12px; margin-bottom: 6px; }
    .value { font-size: 18px; font-weight: 600; word-break: break-word; }
    .section { margin-top: 24px; }
    .warning { background: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; border-radius: 8px; padding: 10px 12px; }
    table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #dce1ef; border-radius: 8px; overflow: hidden; }
    th, td { text-align: left; padding: 10px; border-bottom: 1px solid #edf0f7; font-size: 14px; }
    th { background: #f9fbff; font-weight: 600; }
    pre { background: #0f172a; color: #e2e8f0; border-radius: 8px; padding: 12px; overflow-x: auto; font-size: 12px; margin: 0; }
  </style>
</head>
<body>
  <div class="container">
    <h1>AI Test Automation Dashboard</h1>
    <div class="meta">Généré le: $generatedAt | Dernier run (UTC): $($frameworkMetrics.last_run_utc)</div>

    <div class="section">
      <h2>Métriques Framework</h2>
      <div class="grid">$frameworkCards</div>
    </div>

    <div class="section">
      <h2>Métriques Self-Healing (Colab)</h2>
      <div class="grid">$colabCards</div>
    </div>

    <div class="section">
      <h2>Top Tags Cucumber</h2>
      <table>
        <thead><tr><th>Tag</th><th>Occurrences</th></tr></thead>
        <tbody>$tagsRows</tbody>
      </table>
    </div>

    <div class="section">
      <h2>Raw JSON - Framework</h2>
      <pre>$frameworkRawJson</pre>
    </div>

    <div class="section">
      <h2>Raw JSON - Colab</h2>
      <pre>$colabRawJson</pre>
    </div>
  </div>
</body>
</html>
"@

New-Item -ItemType Directory -Path $dashboardDir -Force | Out-Null
Set-Content -Path $outputFile -Value $html -Encoding UTF8

Write-Output "Dashboard generated: $outputFile"
