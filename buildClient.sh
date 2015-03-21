#!/bin/bash
javac -cp .:"jar/*" src/Processor.java -d ./jar
cd jar
jar cf processor.jar com 
cd ..
