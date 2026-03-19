@echo off
cd /d "%~dp0"
java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar paper-1.21.11-126.jar nogui
pause
