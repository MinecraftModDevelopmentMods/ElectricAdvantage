param(
    [string]$ProjectRoot = (Join-Path $PSScriptRoot '../..'),
    [Parameter(Mandatory = $true)][string]$Evidence,
    [string]$Compiler = 'C:/Users/johnb/.p2/pool/plugins/org.eclipse.jdt.core.compiler.batch_3.46.0.v20260528-0407.jar',
    [string]$Java17Home = 'C:/Users/johnb/.jdks/temurin-17.0.1+12/jdk-17.0.1+12',
    [string]$Java8Home = 'C:/Users/johnb/.jdks/temurin-8.0.502+7/jdk8u502-b07'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
$Evidence = [IO.Path]::GetFullPath($Evidence)
if (!$Evidence.StartsWith($ProjectRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'ECJ evidence must remain inside the Electric checkout'
}
if (Test-Path -LiteralPath $Evidence) { throw 'Refusing to overwrite ECJ evidence' }
$null = New-Item -ItemType Directory -Path $Evidence

function Get-Pin([string]$Path) {
    $file = Get-Item -LiteralPath $Path
    [ordered]@{ path = $file.FullName; sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash }
}
function Save-Report {
    $json = $report | ConvertTo-Json -Depth 15
    [IO.File]::WriteAllText((Join-Path $Evidence 'ecj-results.json'), $json + "`n", [Text.UTF8Encoding]::new($false))
}
function Test-ClassFiles([string]$Directory) {
    $files = @(Get-ChildItem -LiteralPath $Directory -Recurse -File -Filter '*.class')
    if (!$files.Count) { throw "ECJ emitted no classes in $Directory" }
    foreach ($file in $files) {
        $bytes = [IO.File]::ReadAllBytes($file.FullName)
        if ($bytes.Length -lt 8 -or $bytes[0] -ne 0xCA -or $bytes[1] -ne 0xFE -or $bytes[2] -ne 0xBA -or $bytes[3] -ne 0xBE) {
            throw "Invalid class file: $($file.FullName)"
        }
        if (($bytes[6] * 256 + $bytes[7]) -ne 52) { throw "Non-Java-8 ECJ output: $($file.FullName)" }
    }
    return $files.Count
}
function Invoke-Ecj([string]$Name, [string[]]$Sources, [string[]]$Libraries) {
    $output = Join-Path $Evidence $Name
    $null = New-Item -ItemType Directory -Path $output
    $xmlLog = Join-Path $Evidence "$Name.xml"
    $arguments = @('-1.8', '-proc:none', '-encoding', 'UTF-8', '-bootclasspath',
        ('"' + (($bootLibraries.path -replace '\\', '/') -join ';') + '"'), '-classpath',
        ('"' + (($Libraries -replace '\\', '/') -join ';') + '"'), '-d',
        ('"' + $output.Replace('\', '/') + '"'), '-log', ('"' + $xmlLog.Replace('\', '/') + '"'))
    $arguments += @($Sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' })
    $argFile = Join-Path $Evidence "$Name.args"
    [IO.File]::WriteAllLines($argFile, $arguments, [Text.UTF8Encoding]::new($false))
    & $java17 -Xmx2G -jar $Compiler "@$argFile" *> (Join-Path $Evidence "$Name-compile.log")
    $exitCode = $LASTEXITCODE
    $entry = [ordered]@{ exitCode = $exitCode; sourceCount = $Sources.Count; output = $output; log = "$Name-compile.log" }
    $report.compilations[$Name] = $entry
    if ($exitCode -ne 0) { throw "$Name ECJ compilation failed; retain its log and XML diagnostics" }
    [xml]$diagnostics = Get-Content -LiteralPath $xmlLog -Raw
    $problems = @($diagnostics.SelectNodes('//*[@severity]'))
    $entry.errors = @($problems | Where-Object { $_.GetAttribute('severity') -ieq 'ERROR' }).Count
    $entry.warnings = @($problems | Where-Object { $_.GetAttribute('severity') -ieq 'WARNING' }).Count
    if ($entry.errors) { throw "$Name ECJ XML contains errors" }
    $entry.classCount = Test-ClassFiles $output
    return $output
}

$report = [ordered]@{ status = 'RUNNING'; project = $ProjectRoot; evidence = $Evidence
    scope = 'Pinned Electric 1.16.5 candidate; fresh ECJ main/test/helper outputs and worldless Java 8 JUnit; no Gradle or Minecraft launch'
    compiler = $null; toolchains = $null; libraries = @(); sources = @(); gradleInventory = @()
    compilations = [ordered]@{}; junit = $null }
try {
    $properties = ConvertFrom-StringData -StringData (Get-Content -LiteralPath (Join-Path $ProjectRoot 'gradle.properties') -Raw)
    if ($properties.mod_id -ne 'electricadvantage' -or $properties.mod_version -ne '2.2.8.116051' -or
        $properties.poweradvantage_source_ref -ne 'bcb75bfa91c14751ebf1e31865e1ec0ffa82c0b7' -or
        $properties.poweradvantage_deobf_sha256 -ne '2FB923944A456B4F26AB2F9212C646469CE3F49BFD4DAE894C15559B48908386') {
        throw 'This verifier requires the pinned Electric 1.16.5 candidate'
    }
    $report.compiler = Get-Pin $Compiler
    if ($report.compiler.sha256 -ne 'D0D43F8E2D7003E5EFED612E2CBB5F01870043397D8F1BBE536FD9128F4FCBF7') { throw 'ECJ compiler pin mismatch' }
    $java17 = Join-Path $Java17Home 'bin/java.exe'
    $java8 = Join-Path $Java8Home 'bin/java.exe'
    $bootLibraries = @(Get-ChildItem -LiteralPath (Join-Path $Java8Home 'jre/lib') -File -Filter '*.jar' | Sort-Object Name | ForEach-Object { Get-Pin $_.FullName })
    if (!(($bootLibraries.path | Split-Path -Leaf) -contains 'rt.jar')) { throw 'Missing Java 8 boot runtime' }
    $report.toolchains = [ordered]@{ compilerJava = (Get-Pin $java17); runtimeJava = (Get-Pin $java8)
        java17Release = (Get-Pin (Join-Path $Java17Home 'release')); java8Release = (Get-Pin (Join-Path $Java8Home 'release')); bootLibraries = $bootLibraries }
    [xml]$classpath = Get-Content -LiteralPath (Join-Path $ProjectRoot '.classpath') -Raw
    $report.classpath = Get-Pin (Join-Path $ProjectRoot '.classpath')
    $mainLibraries = @()
    $testLibraries = @()
    foreach ($entry in @($classpath.classpath.classpathentry | Where-Object { $_.kind -eq 'lib' })) {
        $file = if ([IO.Path]::IsPathRooted([string]$entry.path)) { [IO.Path]::GetFullPath([string]$entry.path) }
            else { [IO.Path]::GetFullPath((Join-Path $ProjectRoot ([string]$entry.path))) }
        if ([IO.Path]::GetFileName($file) -like 'ElectricAdvantage*.jar') { throw 'Electric output jar cannot satisfy its own ECJ compilation' }
        $scopeNode = $entry.SelectSingleNode('attributes/attribute[@name="gradle_used_by_scope"]/@value')
        $scope = if ($scopeNode) { $scopeNode.Value } else { '' }
        $pin = Get-Pin $file
        $pin.scope = $scope
        $report.libraries += $pin
        if (!$scope -or $scope.Split(',') -contains 'main') { $mainLibraries += $file }
        if (!$scope -or $scope.Split(',') -contains 'test') { $testLibraries += $file }
    }
    foreach ($dependency in @(@{ prefix = 'PowerAdvantage-'; hash = $properties.poweradvantage_deobf_sha256 },
        @{ prefix = 'BaseMetals-'; hash = $properties.basemetals_deobf_sha256 }, @{ prefix = 'OreSpawn-'; hash = $properties.orespawn_deobf_sha256 })) {
        $matching = @($report.libraries | Where-Object { [IO.Path]::GetFileName($_.path).StartsWith($dependency.prefix) })
        if ($matching.Count -ne 1 -or $matching[0].sha256 -ne $dependency.hash) { throw "Typed dependency pin mismatch: $($dependency.prefix)" }
    }
    $sources = @{}
    foreach ($set in @('main', 'test')) {
        $sources[$set] = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot "src/$set/java") -Recurse -File -Filter '*.java' | Sort-Object FullName | ForEach-Object { $_.FullName })
        $report.sources += @($sources[$set] | ForEach-Object { Get-Pin $_ })
    }
    $classes = @()
    $expected = 0
    foreach ($xml in @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'build/test-results/test') -File -Filter 'TEST-*.xml' | Sort-Object Name)) {
        [xml]$inventory = Get-Content -LiteralPath $xml.FullName -Raw
        if ([int]$inventory.testsuite.failures -or [int]$inventory.testsuite.errors -or [int]$inventory.testsuite.skipped) { throw 'Gradle suite inventory contains failed/skipped tests' }
        $classes += [string]$inventory.testsuite.name
        $expected += [int]$inventory.testsuite.tests
        $report.gradleInventory += Get-Pin $xml.FullName
    }
    if ($classes.Count -ne 17 -or $expected -ne 47 -or $sources.test.Count -ne 17 -or $sources.main.Count -ne 85) { throw 'Unexpected Electric 1.16.5 source/test inventory' }
    $main = Invoke-Ecj 'main' $sources.main $mainLibraries
    $test = Invoke-Ecj 'test' $sources.test (@($main) + $testLibraries)
    $helperSource = Join-Path $PSScriptRoot 'AuditedJUnit.java'
    $report.helper = Get-Pin $helperSource
    $helper = Invoke-Ecj 'helper' @($helperSource) $testLibraries
    $junitXml = Join-Path $Evidence 'junit.xml'
    $runtime = @($main, $test, $helper, (Join-Path $ProjectRoot 'build/resources/main'), (Join-Path $ProjectRoot 'build/resources/test')) + $testLibraries
    $report.runtime = $runtime
    Push-Location $ProjectRoot
    try {
        & $java8 -Xmx2G -cp ($runtime -join ';') zone.moddev.mc.electricadvantage.verification.AuditedJUnit $junitXml $expected @classes *> (Join-Path $Evidence 'junit.log')
        $junitExit = $LASTEXITCODE
    } finally { Pop-Location }
    $report.junit = [ordered]@{ exitCode = $junitExit; expected = $expected; suites = $classes; log = 'junit.log'; xml = 'junit.xml' }
    if ($junitExit -ne 0) { throw 'Fresh ECJ JUnit execution failed' }
    [xml]$junit = Get-Content -LiteralPath $junitXml -Raw
    if ($junit.testsuite.passed -ne 'true' -or [int]$junit.testsuite.tests -ne $expected -or
        [int]$junit.testsuite.failures -or [int]$junit.testsuite.ignored -or [int]$junit.testsuite.assumptions) { throw 'Strict ECJ test counts failed' }
    foreach ($pin in @($report.sources) + @($report.libraries) + @($report.helper)) {
        if ((Get-FileHash -LiteralPath $pin.path -Algorithm SHA256).Hash -ne $pin.sha256) { throw "Input changed during ECJ qualification: $($pin.path)" }
    }
    $report.status = 'PASS'
} catch {
    $report.status = 'FAIL'
    $report.error = $_.ToString()
    throw
} finally { Save-Report }
Get-Content -LiteralPath (Join-Path $Evidence 'junit.log') -Tail 8
Write-Output "ECJ PASS: $($sources.main.Count) main, $($sources.test.Count) test sources; $expected tests; Java 8 class files"
