@echo off
mkdir class >NUL 2>&1
mkdir doc >NUL 2>&1
set classpath=./class;./jna-3.5.2.jar;./platform-3.5.2.jar
::javac -cp %classpath% ./src/MyProcessReaderExample.java -d ./class
javac -cp %classpath% ./src/com/hybris95/ProcessAccess.java -d ./class
javadoc -linkoffline http://java.sun.com/javase/6/docs/api/ http://java.sun.com/javase/6/docs/api/ -classpath %classpath% -sourcepath ./src -d ./doc ./src/com/hybris95/ProcessAccess.java
javac -cp %classpath% ./src/com/hybris95/test/TestProcessAccess.java -d ./class
pause
goto :eof
