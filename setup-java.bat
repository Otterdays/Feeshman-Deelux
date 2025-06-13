@echo off
echo Setting JAVA_HOME for Feeshman Deelux development...

REM Set JAVA_HOME for current session
set JAVA_HOME=C:\Program Files\Java\jdk-21

REM Set JAVA_HOME permanently for current user
setx JAVA_HOME "C:\Program Files\Java\jdk-21"

REM Add Java to PATH if not already there
set PATH=%JAVA_HOME%\bin;%PATH%
setx PATH "%JAVA_HOME%\bin;%PATH%"

echo.
echo JAVA_HOME has been set to: %JAVA_HOME%
echo.
echo Please restart your IDE (Cursor/VS Code) for changes to take effect.
echo.
pause 