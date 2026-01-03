# YouLog APK Pack Script (PowerShell)

# 1. Cleanup
Write-Host "Cleaning up build folders..." -ForegroundColor Cyan
./gradlew clean

# 2. Build APK (Debug)
Write-Host "Compiling APK..." -ForegroundColor Cyan
./gradlew assembleDebug

# 3. Locate APK
$sourceApk = "app/build/outputs/apk/debug/app-debug.apk"
$targetDir = "html"
$targetApk = "$targetDir/youlog.apk"

# 4. Move and Rename
if (Test-Path $sourceApk) {
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir
    }
    Copy-Item $sourceApk $targetApk -Force
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "APK moved to: $targetApk" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host "ERROR: APK file not found. Check build logs." -ForegroundColor Red
}
