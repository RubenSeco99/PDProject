@echo off

rem Navegar até a pasta src onde estão os arquivos Java
cd..

rem Compilar todos os arquivos .java e colocar os binários na pasta bin
javac -d ../bin Cliente/*.java Servidor/*.java

rem Voltar para o diretório principal do projeto
cd ..

rem Abrir uma nova janela para iniciar apenas o Cliente
start cmd /k "java -cp bin Cliente.Cliente localhost 5000"

rem Pausar o terminal original para ver mensagens de erro ou saída
pause

