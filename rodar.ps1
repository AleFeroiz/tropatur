param([switch]$Testar, [switch]$Empacotar, [switch]$Postgres)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $tools = Join-Path (Split-Path $PSScriptRoot -Parent) '.tools'
    $jdk = Get-ChildItem $tools -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jdk) { $env:JAVA_HOME = $jdk.FullName }
    if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        throw 'Configure JAVA_HOME com a pasta de um JDK 21 (não o Java 8). Consulte README.md.'
    }
    $maven = Join-Path $tools 'apache-maven-3.9.11\bin\mvn.cmd'
    if (-not (Test-Path $maven)) { $maven = Join-Path $PSScriptRoot 'mvnw.cmd' }
    $arguments = @()
    if (Test-Path $tools) { $arguments += "-Dmaven.repo.local=$tools\m2" }
    if ($Testar) { $arguments += 'test' }
    elseif ($Empacotar) { $arguments += 'package' }
    else {
        $arguments += 'spring-boot:run'
        if ($Postgres) { $arguments += '-Dspring-boot.run.profiles=postgres' }
    }
    & $maven @arguments
    if ($LASTEXITCODE -ne 0) { throw "Maven terminou com código $LASTEXITCODE" }
} finally { Pop-Location }
