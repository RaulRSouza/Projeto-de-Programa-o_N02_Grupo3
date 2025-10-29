Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "SISTEMA DE GERENCIAMENTO DE AULAS - VERIDIA" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

$PROJECT_DIR = $PSScriptRoot
Write-Host "Diretorio do projeto: $PROJECT_DIR" -ForegroundColor Yellow

Write-Host "`nVerificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "   OK - Java instalado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   ERRO - Java nao encontrado!" -ForegroundColor Red
    exit 1
}

Write-Host "`nVerificando Maven Wrapper..." -ForegroundColor Yellow
$mvnwCmd = Join-Path $PROJECT_DIR "mvnw.cmd"
$mvnw = Join-Path $PROJECT_DIR "mvnw"

if (Test-Path $mvnwCmd) {
    Write-Host "   OK - Maven Wrapper encontrado" -ForegroundColor Green
    $MAVEN_CMD = $mvnwCmd
} elseif (Test-Path $mvnw) {
    Write-Host "   OK - Maven Wrapper encontrado" -ForegroundColor Green
    $MAVEN_CMD = $mvnw
} else {
    Write-Host "   AVISO - Maven Wrapper nao encontrado" -ForegroundColor Yellow
    Write-Host "   Tentando usar Maven do sistema..." -ForegroundColor Yellow
    try {
        mvn -version | Out-Null
        $MAVEN_CMD = "mvn"
        Write-Host "   OK - Maven do sistema encontrado" -ForegroundColor Green
    } catch {
        Write-Host "   ERRO - Maven nao encontrado!" -ForegroundColor Red
        Write-Host "`n   Instale o Maven ou adicione o Maven Wrapper ao projeto:" -ForegroundColor Yellow
        Write-Host "   mvn wrapper:wrapper" -ForegroundColor Cyan
        exit 1
    }
}

Write-Host "`n================================================================" -ForegroundColor Cyan
Write-Host "COMPILANDO BACKEND (SPRING BOOT + JAVAFX)" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

Set-Location $PROJECT_DIR

Write-Host "`nCompilando o projeto..." -ForegroundColor Yellow
& $MAVEN_CMD clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nERRO ao compilar o projeto!" -ForegroundColor Red
    exit 1
}

Write-Host "`n================================================================" -ForegroundColor Cyan
Write-Host "INICIANDO APLICACAO JAVAFX" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

Write-Host "`nIniciando aplicacao JavaFX..." -ForegroundColor Yellow
& $MAVEN_CMD javafx:run

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nERRO ao iniciar a aplicacao!" -ForegroundColor Red
    exit 1
}

Write-Host "`n================================================================" -ForegroundColor Cyan
Write-Host "APLICACAO ENCERRADA" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan