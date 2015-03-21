#!/bin/bash

#./processor.sh HOST_NAME PORT user password db_name tbl_name output_file
# HOST_NAME = Broker's host name
# PORT  =  Broker's port number
# user = user name for the DB
# password = password for the DB
# db_name = DB name
# tbl_name = Table name
# output_file = CSV file to be generated
./processor.sh localhost 4445 user password mydb activity ./tableout.csv
