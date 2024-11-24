@echo off
echo Executar GUI...
cd ..
cd ..

REM Definir o caminho para a biblioteca JavaFX
set JAVA_FX_LIB=lib\javafx\lib

REM Executar a aplicação com JavaFX
java --module-path %JAVA_FX_LIB% --add-modules javafx.controls,javafx.fxml -cp out\production\PDProject Main

pause
