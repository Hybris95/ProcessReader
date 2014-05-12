#!/bin/bash
mkdir class
mkdir doc
#javac -cp ./class:./jna-3.5.2.jar:./platform-3.5.2.jar ./src/MyProcessReaderExample.java -d ./class
javac -cp ./class;./jna-3.5.2.jar;./platform-3.5.2.jar ./src/com/hybris95/ProcessAccess.java -d ./class
javadoc -linkoffline http://java.sun.com/javase/6/docs/api/ http://java.sun.com/javase/6/docs/api/ -classpath ./class;./jna-3.5.2.jar;./platform-3.5.2.jar -sourcepath ./src -d ./doc ./src/com/hybris95/ProcessAccess.java
