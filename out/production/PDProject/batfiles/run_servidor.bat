@echo off

rem Navegar até a pasta src onde estão os arquivos Java
cd..

rem Compilar todos os arquivos .java e colocar os binários na pasta bin
javac -d ../bin Cliente/*.java Servidor/*.java

rem Voltar para o diretório principal do projeto
cd ..

rem Abrir uma nova janela para iniciar apenas o Servidor
start cmd /c "java -cp bin;lib/sqlite-jdbc-3.46.1.3.jar Servidor.Servidor 5000 src/BaseDeDados/BaseDados.db"

rem Pausar o terminal original para ver mensagens de erro ou saída
pause
