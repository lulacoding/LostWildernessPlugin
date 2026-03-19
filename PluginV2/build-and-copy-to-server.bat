@echo off
rem Build PluginV2 and copy the JARs to Server\backends\survival-1, amplified-1, and lobby-1\plugins
rem Run from PluginV2 folder, or from repo root (script uses relative paths from PluginV2).

setlocal
cd /d "%~dp0"

call gradlew.bat build
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)

rem Build wrappers
pushd survival-plugin
call ..\gradlew.bat build
if errorlevel 1 (
    echo Survival wrapper build failed.
    popd
    exit /b 1
)
popd

pushd amplified-plugin
call ..\gradlew.bat build
if errorlevel 1 (
    echo Amplified wrapper build failed.
    popd
    exit /b 1
)
popd

pushd lobby-plugin
call ..\gradlew.bat build
if errorlevel 1 (
    echo Lobby wrapper build failed.
    popd
    exit /b 1
)
popd

set CORE_JAR=build\libs\RPG_Core_V2-2.0.0-SNAPSHOT-all.jar
set SURVIVAL_JAR=survival-plugin\build\libs\LW_Survival_V2-1.0.0-SNAPSHOT.jar
set AMPLIFIED_JAR=amplified-plugin\build\libs\LW_Amplified_V2-1.0.0-SNAPSHOT.jar
set LOBBY_JAR=lobby-plugin\build\libs\LW_Lobby_V2-1.0.0-SNAPSHOT.jar

set ROOT=..
set SERVER=%ROOT%\Server\backends
if not exist "%SERVER%\survival-1\plugins" mkdir "%SERVER%\survival-1\plugins"
if not exist "%SERVER%\amplified-1\plugins" mkdir "%SERVER%\amplified-1\plugins"
if not exist "%SERVER%\lobby-1\plugins" mkdir "%SERVER%\lobby-1\plugins"

copy /Y "%CORE_JAR%" "%SERVER%\survival-1\plugins\"
copy /Y "%CORE_JAR%" "%SERVER%\amplified-1\plugins\"
copy /Y "%CORE_JAR%" "%SERVER%\lobby-1\plugins\"

copy /Y "%SURVIVAL_JAR%" "%SERVER%\survival-1\plugins\"
copy /Y "%AMPLIFIED_JAR%" "%SERVER%\amplified-1\plugins\"
copy /Y "%LOBBY_JAR%" "%SERVER%\lobby-1\plugins\"

echo.
echo Copied core + wrappers to survival-1, amplified-1, and lobby-1.
echo Done.
endlocal
