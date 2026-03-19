@echo off
setlocal
cd /d "%~dp0"

echo Building plugins...
cd PluginV2
call gradlew.bat build
if errorlevel 1 (
    echo Build failed.
    cd ..
    exit /b 1
)
cd ..

set "SRC=Plugin"
set "ROOT=Server\backends"

set "SURVIVAL=%ROOT%\survival-1\plugins"
set "AMPLIFIED=%ROOT%\amplified-1\plugins"
set "LOBBY=%ROOT%\lobby-1\plugins"

:: Ensure plugin folders exist
if not exist "%SURVIVAL%" mkdir "%SURVIVAL%"
if not exist "%AMPLIFIED%" mkdir "%AMPLIFIED%"
if not exist "%LOBBY%" mkdir "%LOBBY%"

echo.
echo Copying JARs to ServerUPDATE backends...

:: common.jar -> all three backends
if exist "%SRC%\common\build\libs\common.jar" (
    copy /Y "%SRC%\common\build\libs\common.jar" "%SURVIVAL%\common.jar" >nul && echo   common.jar -^> survival-1, amplified-1, lobby-1
    copy /Y "%SRC%\common\build\libs\common.jar" "%AMPLIFIED%\common.jar" >nul
    copy /Y "%SRC%\common\build\libs\common.jar" "%LOBBY%\common.jar" >nul
) else (
    echo   [skip] common.jar not found
)

:: Shared modules -> survival-1 and amplified-1 only
:: Calendar
if exist "%SRC%\calendar\build\libs\LostWilderness-Calendar-all.jar" (
    copy /Y "%SRC%\calendar\build\libs\LostWilderness-Calendar-all.jar" "%SURVIVAL%\LostWilderness-Calendar-all.jar" >nul
    copy /Y "%SRC%\calendar\build\libs\LostWilderness-Calendar-all.jar" "%AMPLIFIED%\LostWilderness-Calendar-all.jar" >nul
    echo   LostWilderness-Calendar-all.jar -^> survival-1, amplified-1
)
:: Events
if exist "%SRC%\events\build\libs\LostWilderness-Events-all.jar" (
    copy /Y "%SRC%\events\build\libs\LostWilderness-Events-all.jar" "%SURVIVAL%\LostWilderness-Events-all.jar" >nul
    copy /Y "%SRC%\events\build\libs\LostWilderness-Events-all.jar" "%AMPLIFIED%\LostWilderness-Events-all.jar" >nul
    echo   LostWilderness-Events-all.jar -^> survival-1, amplified-1
)
:: Clans
if exist "%SRC%\clans\build\libs\LostWilderness-Clans-all.jar" (
    copy /Y "%SRC%\clans\build\libs\LostWilderness-Clans-all.jar" "%SURVIVAL%\LostWilderness-Clans-all.jar" >nul
    copy /Y "%SRC%\clans\build\libs\LostWilderness-Clans-all.jar" "%AMPLIFIED%\LostWilderness-Clans-all.jar" >nul
    echo   LostWilderness-Clans-all.jar -^> survival-1, amplified-1
)
:: Portals
if exist "%SRC%\portals\build\libs\LostWilderness-Portals-all.jar" (
    copy /Y "%SRC%\portals\build\libs\LostWilderness-Portals-all.jar" "%SURVIVAL%\LostWilderness-Portals-all.jar" >nul
    copy /Y "%SRC%\portals\build\libs\LostWilderness-Portals-all.jar" "%AMPLIFIED%\LostWilderness-Portals-all.jar" >nul
    echo   LostWilderness-Portals-all.jar -^> survival-1, amplified-1
)

:: Server-specific JARs
if exist "%SRC%\servers\survival\build\libs\LostWilderness-Survival-all.jar" (
    copy /Y "%SRC%\servers\survival\build\libs\LostWilderness-Survival-all.jar" "%SURVIVAL%\LostWilderness-Survival-all.jar" >nul
    echo   LostWilderness-Survival-all.jar -^> survival-1
)
if exist "%SRC%\servers\amplified\build\libs\LostWilderness-Amplified-all.jar" (
    copy /Y "%SRC%\servers\amplified\build\libs\LostWilderness-Amplified-all.jar" "%AMPLIFIED%\LostWilderness-Amplified-all.jar" >nul
    echo   LostWilderness-Amplified-all.jar -^> amplified-1
)
if exist "%SRC%\lobby\build\libs\LostWilderness-Lobby-all.jar" (
    copy /Y "%SRC%\lobby\build\libs\LostWilderness-Lobby-all.jar" "%LOBBY%\LostWilderness-Lobby-all.jar" >nul
    echo   LostWilderness-Lobby-all.jar -^> lobby-1
)

echo.
echo Done. Plugins copied to %ROOT%
endlocal
exit /b 0
