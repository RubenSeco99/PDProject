@echo off
echo Executar GUI...

rem Caminho para a biblioteca do JavaFX
set JAVA_FX_LIB=..\..\lib\javafx-sdk-23.0.1\lib

rem Executar a aplicação
java --module-path "%JAVA_FX_LIB%" --add-modules javafx.controls,javafx.fxml -cp ..\..\out\production\PDProject Main

pause
