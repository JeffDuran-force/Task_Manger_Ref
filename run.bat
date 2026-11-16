@echo off
setlocal
cd /d "%~dp0"

call build.bat
if errorlevel 1 (
    pause
    exit /b 1
)

java -cp out studenttaskmanager.Main
if errorlevel 1 pause
