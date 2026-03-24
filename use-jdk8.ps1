# 编译：
# powershell -ExecutionPolicy Bypass -File .\use-jdk8.ps1 -Task build
# 运行：
# powershell -ExecutionPolicy Bypass -File .\use-jdk8.ps1 -Task run
param(
  [ValidateSet('build','run','test','clean')]
  [string]$Task = 'run'
)

$Jdk8Home = 'C:\Program Files\Java\jdk8u482-b08'

if (-not (Test-Path (Join-Path $Jdk8Home 'bin\java.exe'))) {
  Write-Error "JDK8 not found: $Jdk8Home"
  exit 1
}

$env:JAVA_HOME = $Jdk8Home
$env:Path = "$($env:JAVA_HOME)\bin;$env:Path"

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
& java -version
& mvn -v

switch ($Task) {
  'clean' { & mvn clean }
  'build' { & mvn clean package -DskipTests }
  'test'  { & mvn test }
  'run'   { & mvn spring-boot:run }
}
