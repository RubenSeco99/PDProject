@echo off

rem Caminho relativo para a pasta lib do projeto
set "LIB_PATH=..\..\lib"

rem Caminho relativo para a saída compilada (out)
set "OUT_PATH=..\..\out\production\PDProject"

rem Caminho relativo para o código fonte (src)
set "SRC_PATH=..\src"

rem Inclui todos os arquivos .jar na pasta lib no classpath
setlocal enabledelayedexpansion
set "CLASSPATH="
for %%f in ("%LIB_PATH%\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%~f"
)
endlocal & set "CLASSPATH=%CLASSPATH:~1%"

rem Verificar se a pasta out existe, senão criá-la
if not exist "%OUT_PATH%" (
    mkdir "%OUT_PATH%"
)

rem Iniciar comando de compilação
set "COMPILE_CMD=javac --module-path "%LIB_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%CLASSPATH%" -d "%OUT_PATH%" -sourcepath "%SRC_PATH%"

echo CLASSPATH=%CLASSPATH%

rem Compilar os arquivos .java de cada pasta
for %%f in ("%SRC_PATH%\Cliente\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\Servidor\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\ServidorBackup\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\BaseDeDados\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\BaseDeDadosBackUp\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\Comunicacao\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\Entidades\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\Uteis\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\UI\*.java") do (
    %COMPILE_CMD% "%%f"
)

for %%f in ("%SRC_PATH%\UI\Controllers\*.java") do (
    %COMPILE_CMD% "%%f"
)

rem Verificar se houve erros na compilação
if errorlevel 1 (
    echo ERRO: Houve falhas na compilação!
    pause
    exit /b
) else (
    echo COMPILAÇÃO CONCLUÍDA COM SUCESSO!
)
