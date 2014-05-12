#!/bin/bash
mkdir class
#javac -cp ./class:./jna-3.5.2.jar:./platform-3.5.2.jar ./src/MyProcessReaderExample.java -d ./class
javac -cp ./class;./jna-3.5.2.jar;./platform-3.5.2.jar ./src/com/hybris95/ProcessAccess.java -d ./class
