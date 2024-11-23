@echo off
cd
rem Navegar para o diretório base do projeto
cd ../..
cd

rem Abrir uma nova janela para iniciar o Servidor
start cmd /k "java -cp bin;lib/sqlite-jdbc-3.46.1.3.jar ServidorBackup.ServidorBackup src/BaseDeDadosBackUp"

pause
