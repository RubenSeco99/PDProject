@echo off

rem Navegar para o diretório base do projeto
cd ../..

rem Abrir uma nova janela para iniciar o Servidor
start cmd /c "java -cp out/production/PDProject;lib/sqlite-jdbc-3.46.1.3.jar Servidor.Servidor 5000 src/BaseDeDados/BaseDados.db"

pause
