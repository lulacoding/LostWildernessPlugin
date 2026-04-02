@echo off
cd /d "%~dp0"
echo [DEV MODE] Starting with reduced memory (1G max)...
java -Xms3G -Xmx6G -XX:+UseG1GC -jar paper-1.21.11-126.jar nogui
pause
