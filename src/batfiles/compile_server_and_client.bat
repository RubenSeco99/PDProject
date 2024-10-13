@echo off

rem Navegar até a pasta src onde estão os arquivos Java
cd..

rem Compilar todos os arquivos .java e colocar os binários na pasta bin
javac -d ../bin Cliente/*.java Servidor/*.java

rem Voltar para o diretório principal do projeto
cd ..

rem Abrir uma nova janela para iniciar o Servidor
start cmd /k "java -cp bin Servidor.Servidor 5000 src/BaseDeDados/BaseDados.db"

rem Esperar alguns segundos para o servidor iniciar (ajuste conforme necessário)
timeout /t 2 /nobreak >nul

rem Abrir uma nova janela para iniciar o Cliente
start cmd /k "java -cp bin Cliente.Cliente localhost 5000"

rem Pausar o terminal original para ver mensagens de erro ou saída
pause
