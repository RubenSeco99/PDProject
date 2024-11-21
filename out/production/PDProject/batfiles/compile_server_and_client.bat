@echo off

rem Navegar até a pasta src onde estão os arquivos Java
cd..

rem Compilar todos os arquivos .java e colocar os binários na pasta bin
javac -d ../bin Cliente/*.java Servidor/*.java ServidorBackup/*.java

rem Voltar para o diretório principal do projeto
cd ..

rem Abrir uma nova janela para iniciar o Servidor
start cmd /c "java -cp bin;lib/sqlite-jdbc-3.46.1.3.jar Servidor.Servidor 5000 src/BaseDeDados/BaseDados.db"

rem Esperar alguns segundos para o servidor iniciar (ajuste conforme necessário)
ping -n 2 127.0.0.1 >null

rem Abrir uma nova janela para iniciar o Cliente
start cmd /k "java -cp bin Cliente.ClienteMain localhost 5000"

ping -n 2 127.0.0.1 >null

rem Abrir uma nova janela para iniciar o Sevidor backup
start cmd /k "java -cp bin;lib/sqlite-jdbc-3.46.1.3.jar ServidorBackup.ServidorBackup src/BaseDeDadosBackUp"

rem Pausar o terminal original para ver mensagens de erro ou saída
pause
