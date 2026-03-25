@echo off
cd /d "%~dp0"
java -Xms6G -Xmx8G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:+EnableDynamicAgentLoading -XX:MaxGCPauseMillis=200 -jar paper-1.21.11-126.jar nogui
pause
