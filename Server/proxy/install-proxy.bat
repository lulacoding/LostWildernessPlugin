@echo off
TITLE Install BungeeCord Proxy
cd /d "%~dp0"

if exist bungeecord.jar (
    echo bungeecord.jar already exists. Delete it first if you want to re-download.
    pause
    exit /b 0
)

echo Downloading BungeeCord from SpigotMC Jenkins...
echo.

:: Download using PowerShell (works on Windows 10+)
powershell -NoProfile -Command ^
    "try { " ^
    "  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; " ^
    "  Invoke-WebRequest -Uri 'https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar' " ^
    "    -OutFile 'bungeecord.jar' -UseBasicParsing; " ^
    "  Write-Host 'Download complete: bungeecord.jar'; " ^
    "} catch { Write-Host 'Download failed:' $_.Exception.Message; exit 1 }"

if not exist bungeecord.jar (
    echo.
    echo Automatic download failed. Please download manually:
    echo 1. Open https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/
    echo 2. Download BungeeCord.jar
    echo 3. Save it to this folder as bungeecord.jar
    echo.
    pause
    exit /b 1
)

echo.
echo Done. Run start.bat to start the proxy.
pause
