$ErrorActionPreference = "Stop"

# Creates the first official Track Car Android signing key on this Windows PC.
# Keep the .jks file and both passwords in secure offline storage. Never commit them.

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$signingDir = Join-Path $repoRoot "signing"
$keystore = Join-Path $signingDir "trackcar-release.jks"
$base64File = Join-Path $signingDir "trackcar-release-keystore-base64.txt"
$alias = "trackcar-release"

$keytool = Get-Command keytool -ErrorAction SilentlyContinue
if (-not $keytool) {
    throw "Java keytool was not found. Install JDK 17 and ensure its bin folder is in PATH."
}
if (Test-Path $keystore) {
    throw "A keystore already exists at $keystore. Do not overwrite an existing signing identity."
}

New-Item -ItemType Directory -Force -Path $signingDir | Out-Null
Write-Host ""
Write-Host "Creating the Track Car production signing key."
Write-Host "Use a strong password and save it in a password manager."
Write-Host "When keytool asks for the key password, press ENTER to use the same password as the keystore."
Write-Host ""

& $keytool.Source -genkeypair -v -keystore $keystore -storetype JKS -keyalg RSA -keysize 4096 -validity 10000 -alias $alias -dname "CN=Track Car, OU=Fleet, O=Track Car, L=Cairo, ST=Cairo, C=EG"
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $keystore)) {
    throw "Keystore generation failed."
}

Write-Host ""
Write-Host "Verifying keystore alias..."
& $keytool.Source -list -keystore $keystore -alias $alias
if ($LASTEXITCODE -ne 0) { throw "Keystore verification failed." }

$bytes = [System.IO.File]::ReadAllBytes($keystore)
$encoded = [Convert]::ToBase64String($bytes)
[System.IO.File]::WriteAllText($base64File, $encoded, [System.Text.Encoding]::ASCII)

$hash = (Get-FileHash -Algorithm SHA256 $keystore).Hash
Write-Host ""
Write-Host "SUCCESS"
Write-Host "Keystore: $keystore"
Write-Host "Base64 secret file: $base64File"
Write-Host "Keystore SHA-256: $hash"
Write-Host ""
Write-Host "Next: add the four GitHub Actions secrets listed in docs/DEPLOYMENT_SETUP.md."
Write-Host "TRACKCAR_RELEASE_KEYSTORE_BASE64 = full contents of the Base64 text file."
Write-Host "TRACKCAR_RELEASE_KEYSTORE_PASSWORD = the keystore password you entered."
Write-Host "TRACKCAR_RELEASE_KEY_ALIAS = $alias"
Write-Host "TRACKCAR_RELEASE_KEY_PASSWORD = the same password (you pressed ENTER at the key password prompt)."
Write-Host ""
Write-Host "Back up the .jks file and passwords in at least two secure places. Losing them prevents future app updates."
