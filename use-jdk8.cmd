@echo off
setlocal

set "JDK8_HOME=C:\Program Files\Java\jdk8u482-b08"
if not exist "%JDK8_HOME%\bin\java.exe" (
  echo JDK8 not found: %JDK8_HOME%
  exit /b 1
)

set "JAVA_HOME=%JDK8_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using JAVA_HOME=%JAVA_HOME%
java -version
mvn -v

if "%~1"=="clean" goto CLEAN
if "%~1"=="build" goto BUILD
if "%~1"=="test" goto TEST
if "%~1"=="run" goto RUN

:RUN
mvn spring-boot:run
goto END

:CLEAN
mvn clean
goto END

:BUILD
mvn clean package -DskipTests
goto END

:TEST
mvn test
goto END

:END
endlocal
