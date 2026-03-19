@echo off
setlocal

echo Building Feeshman Deelux jar...

if not exist "%~dp0gradlew.bat" (
  echo ERROR: gradlew.bat not found in project root.
  exit /b 1
)

call "%~dp0gradlew.bat" build
if errorlevel 1 (
  echo ERROR: Gradle build failed.
  exit /b 1
)

echo.
echo Build complete. Jar files are in build\libs\.
exit /b 0
