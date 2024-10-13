@echo off
REM Compila o cliente e servidor

REM Diretório onde está a pasta src
cd src

REM Compilar o cliente
javac Cliente/Cliente.java -d ../out/production/G10Meta1_PD

REM Compilar o servidor
javac Servidor/Servidor.java -d ../out/production/G10Meta1_PD

echo Compilação concluída com sucesso!
pause