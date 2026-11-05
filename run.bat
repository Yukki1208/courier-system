@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "lib\mysql-connector-j-8.0.33.jar" (
    echo [ERROR] MySQL driver not found in lib folder.
    pause
    exit /b 1
)
if not exist "lib\flatlaf-3.8.jar" (
    echo [ERROR] FlatLaf not found in lib folder.
    pause
    exit /b 1
)

if not exist out mkdir out

echo ===== Compiling =====
javac -encoding UTF-8 -cp "lib\*" -d out src\main\java\com\example\courier\*.java
if errorlevel 1 (
    echo.
    echo [COMPILE FAILED] Please send the errors above.
    pause
    exit /b 1
)

echo ===== Starting courier-system =====
start "" javaw -cp "out;lib\*" com.example.courier.MainFrame
