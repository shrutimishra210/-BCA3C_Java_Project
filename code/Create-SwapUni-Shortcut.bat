@echo off
setlocal
rem ================================================================
rem  Builds SwapUni.exe and puts a "SwapUni" shortcut on your Desktop.
rem  Put this file NEXT TO build.gradle (the inner SwapUni folder), then double-click it.
rem  Needs: JDK 17+ (includes jpackage), Gradle, internet for the first build.
rem ================================================================
cd /d "%~dp0"

if not exist build.gradle (
  echo [ERROR] build.gradle not found. Put this file in the same folder as build.gradle.
  pause & exit /b 1
)
where jpackage >nul 2>nul
if errorlevel 1 (
  echo [ERROR] jpackage not found. Install JDK 17 or newer and reopen this window.
  pause & exit /b 1
)

echo.
echo Enter your MySQL login. These are saved inside the exe's config on THIS computer only.
echo Avoid the characters  "  and  %%  in the password.
set /p DBU=MySQL username [root]: 
if "%DBU%"=="" set DBU=root
set /p DBP=MySQL password: 

echo.
echo [1/4] Building the jar...
call gradle bootJar
if errorlevel 1 (
  echo [ERROR] Gradle build failed. Run: gradle --stop  then delete the build folder and try again.
  pause & exit /b 1
)

set "JAR="
for %%F in (build\libs\SwapUni-*.jar) do (
  echo %%~nF| findstr /C:"-plain" >nul
  if errorlevel 1 set "JAR=%%~nxF"
)
if not defined JAR (
  echo [ERROR] No SwapUni jar found in build\libs.
  pause & exit /b 1
)

echo [2/4] Preparing files...
if exist stage rmdir /s /q stage
if exist dist rmdir /s /q dist
mkdir stage
copy /y "build\libs\%JAR%" stage\ >nul

echo [3/4] Creating SwapUni.exe (this takes about a minute)...
call jpackage --type app-image --name SwapUni --input stage --main-jar "%JAR%" --dest dist --java-options "-DDB_USERNAME=%DBU%" --java-options "-DDB_PASSWORD=%DBP%"
if errorlevel 1 (
  echo [ERROR] jpackage failed. Make sure you are using JDK 17 or newer ^(java -version^).
  pause & exit /b 1
)

echo [4/4] Creating Desktop shortcut...
powershell -NoProfile -Command "$s=(New-Object -ComObject WScript.Shell).CreateShortcut([Environment]::GetFolderPath('Desktop')+'\SwapUni.lnk'); $s.TargetPath='%CD%\dist\SwapUni\SwapUni.exe'; $s.WorkingDirectory='%CD%\dist\SwapUni'; $s.Save()"

rmdir /s /q stage
echo.
echo DONE.  Double-click "SwapUni" on your Desktop to start the app.
echo Remember: MySQL must be running first. Do not move or delete the "dist" folder.
pause
