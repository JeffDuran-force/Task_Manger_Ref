@echo off
setlocal
cd /d "%~dp0"

if exist out rmdir /s /q out
mkdir out

javac -encoding UTF-8 -sourcepath src -d out src\studenttaskmanager\Main.java
if errorlevel 1 (
    echo.
    echo Build failed.
    exit /b 1
)

echo Build successful.
