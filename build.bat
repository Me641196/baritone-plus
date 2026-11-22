@echo off
setlocal enabledelayedexpansion
rem Builds baritoneplus-1.0.0.jar with plain javac + jar (no Gradle).
rem Usage: build.bat [minecraft_dir] [baritone_api_jar]
cd /d "%~dp0"

set "MC_DIR=%~1"
if "%MC_DIR%"=="" set "MC_DIR=%APPDATA%\.minecraft"
set "API=%~2"
if "%API%"=="" set "API=libs\baritone-api-neoforge-1.11.2.jar"

if not exist "%MC_DIR%\libraries" (echo No libraries folder in %MC_DIR%. Install NeoForge 1.21.1 there first, or pass the right folder. & exit /b 1)
if not exist "%API%" (echo Missing %API% - see libs\README.txt & exit /b 1)
where javac >nul 2>nul || (echo javac not found. Install a JDK 21. & exit /b 1)

if exist build rmdir /s /q build
mkdir build\classes

set "CP=%API%"
for /r "%MC_DIR%\libraries" %%f in (*.jar) do set "CP=!CP!;%%f"
set "CP=!CP:\=/!"
> build\args.txt echo -cp "!CP!"
>> build\args.txt echo --release 21
>> build\args.txt echo -encoding UTF-8
>> build\args.txt echo -d build/classes
for /r src\main\java %%f in (*.java) do (
  set "S=%%f"
  >> build\args.txt echo "!S:\=/!"
)

javac @build\args.txt || exit /b 1
xcopy /e /i /y src\main\resources build\classes >nul
jar --create --file baritoneplus-1.0.0.jar -C build/classes .
echo Built baritoneplus-1.0.0.jar
