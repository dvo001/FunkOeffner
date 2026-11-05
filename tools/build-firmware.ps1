[CmdletBinding()]
param(
    [string]$InstallationFile = 'C:\Espressif\tools\eim_idf.json'
)
$ErrorActionPreference = 'Stop'
# WSL/Editor-Prozesse koennen eine unvollstaendige PATHEXT-Liste vererben.
# Nur diese Sitzung korrigieren; keine globale Windows-Einstellung veraendern.
$env:PATHEXT = '.COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC;.CPL'
if (-not (Test-Path -LiteralPath $InstallationFile)) {
    throw "ESP-IDF-Installationsdatei fehlt: $InstallationFile"
}
$config = Get-Content -LiteralPath $InstallationFile -Raw | ConvertFrom-Json
$setups = @($config.idfInstalled | Where-Object {
    $_.name -eq 'v5.4.2' -and $_.status -eq 'finished'
})
if ($setups.Count -ne 1) {
    throw 'Es wird genau eine fertig installierte ESP-IDF-Version v5.4.2 erwartet.'
}
$setup = $setups[0]
if (-not $setup.activationScript -or -not (Test-Path -LiteralPath $setup.activationScript)) {
    throw 'ESP-IDF-Aktivierungsskript fehlt.'
}
. $setup.activationScript
if ($env:IDF_PATH -ne $setup.path -or -not (Test-Path -LiteralPath $setup.python)) {
    throw 'ESP-IDF-Aktivierung fehlgeschlagen.'
}
$firmwarePath = Join-Path (Split-Path $PSScriptRoot -Parent) 'firmware'
$env:IDF_TARGET = 'esp32s2'
& $setup.python (Join-Path $setup.path 'tools\idf.py') -C $firmwarePath build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
