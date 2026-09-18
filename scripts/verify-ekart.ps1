$ErrorActionPreference = 'Stop'
# Uses only the JDK and dependencies in an already built local service archive.
# No Maven, npm, installation, publication, deployment or network dependency resolution.
$repoRoot = Split-Path $PSScriptRoot -Parent
$verifyRoot = Join-Path $repoRoot 'target/ekart-verification'
$serviceArchive = Join-Path $repoRoot 'target/delivery-service-1.0.0.jar'
$lombokPath = Join-Path $env:USERPROFILE '.m2/repository/org/projectlombok/lombok/1.18.42/lombok-1.18.42.jar'
if (!(Test-Path $serviceArchive) -or !(Test-Path $lombokPath)) {
    throw 'Existing service archive and cached Lombok are required. This script will not download them.'
}
New-Item -ItemType Directory -Force -Path "$verifyRoot/lib", "$verifyRoot/classes" | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($serviceArchive)
try {
    foreach ($entry in $archive.Entries) {
        if ($entry.FullName.StartsWith('BOOT-INF/lib/') -and $entry.Name.EndsWith('.jar')) {
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path "$verifyRoot/lib" $entry.Name), $true)
        }
    }
} finally { $archive.Dispose() }
$classpath = ((Get-ChildItem "$verifyRoot/lib" -Filter *.jar | ForEach-Object { $_.FullName.Replace('\', '/') }) -join ';') + ';' + $lombokPath.Replace('\', '/')
$sourceFiles = Get-ChildItem (Join-Path $repoRoot 'src/main/java') -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }
$compilerArgs = @('-encoding', 'UTF-8', '-parameters', '-cp', ('"' + $classpath + '"'), '-processorpath', ('"' + $lombokPath.Replace('\', '/') + '"'), '-d', ('"' + "$verifyRoot/classes".Replace('\', '/') + '"')) + $sourceFiles
$compilerArgs += '"' + (Join-Path $repoRoot 'src/test/java/com/zuufa/delivery/provider/ekart/EkartContractChecks.java').Replace('\', '/') + '"'
[System.IO.File]::WriteAllLines("$verifyRoot/javac.args", $compilerArgs)
& javac "@$verifyRoot/javac.args"
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
& java -cp "$verifyRoot/classes;$verifyRoot/lib/*" com.zuufa.delivery.provider.ekart.EkartContractChecks
if ($LASTEXITCODE -ne 0) { throw 'Ekart contract checks failed.' }
