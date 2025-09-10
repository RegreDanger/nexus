@echo off
REM ==========================
REM Local CI/CD Script for Windows with Logging (Sequential)
REM ==========================

set LOGFILE=build.log
echo [%DATE% %TIME%] Starting build process... > %LOGFILE%
echo ========================== >> %LOGFILE%

REM --- Clean ---
echo [%DATE% %TIME%] Cleaning project...
mvnw clean >> %LOGFILE% 2>&1
IF ERRORLEVEL 1 (
    echo [ERROR] Clean failed! Check %LOGFILE% for details.
    exit /b 1
)

REM --- Package ---
echo [%DATE% %TIME%] Packaging project (includes compile)...
mvnw package >> %LOGFILE% 2>&1
IF ERRORLEVEL 1 (
    echo [ERROR] Package failed! Check %LOGFILE% for details.
    exit /b 1
)

REM --- Install ---
echo [%DATE% %TIME%] Installing JAR to local repository...
mvnw install >> %LOGFILE% 2>&1
IF ERRORLEVEL 1 (
    echo [ERROR] Install failed! Check %LOGFILE% for details.
    exit /b 1
)

echo ==========================
echo [%DATE% %TIME%] Build finished successfully!
echo Full log available at %LOGFILE%
echo ==========================
exit /b 0
