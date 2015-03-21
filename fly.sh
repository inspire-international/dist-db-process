#!/bin/bash
java -classpath ./jar/processor.jar:./jar/* com.inspire.gabatto.client.Processor -host $1 -port $2 -user $3 -password $4 -DB $5 -table $6 -filename $7 1> processor.log 2>&1 &

