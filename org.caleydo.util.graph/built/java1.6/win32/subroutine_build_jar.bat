@echo off

SETLOCAL

rem test if JAVAHOME environment variable ist set...
IF NOT "%JAVAHOME%"=="" GOTO JAVAHOME_IS_SET

:NO_JAVAHOMEE
echo.
echo.  Enviroment variable JAVAHOME need to be set to java.exe   (java 1.6 or higher)
echo.
GOTO END

:JAVAHOME_IS_SET

rem you can switch between "src" and "bin"
SET destination=%1

rem path and file name for jar.exe
SET file_jar=%JAVAHOME%\jar.exe

rem path to bin folder were *.class files are stored
SET class_files=..\..\..\%destination%



rem path and filename of resulting jar file
SET jar_file_name=..\..\..\lib\org.caleydo.graph_%destination%.jar

echo info= %class_files%   %jar_file_name%

echo.
echo create jar in folder %jar_file_name%
echo.

IF NOT EXIST %file_jar% GOTO NO_JAR_EXE

%file_jar% cvfM %jar_file_name% -C  %class_files% .


echo.
echo jar in folder %jar_file_name% .. [done]
echo.
GOTO END

:NO_JAR_EXE

IF EXIST %%1 GOTO REPLACE_JAR_PATH
echo jar.exe could not be found
echo.
echo curretn path to jar.exe: %file_jar%
echo.
echo. usage: %0 [path/jar.exe]
echo.

:END

ENDLOCAL

pause
