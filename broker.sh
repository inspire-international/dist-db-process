#!/bin/bash

# Run broker
broker -e broker.env -bg

# broklist -add HOST_NAME PORT server_name+host+port
#   -add option adds server/DB info to the Broker running at HOST_NAME and PORT.
#   broklist -help for further info
broklist -add localhost 4445 mydb+ip-11-120-5-132.ap-use-1.compute.internal+5080
broklist -add localhost 4445 mydb+ip-11-150-101-194.ap-use-1.compute.internal+5080

# Get registered server info
broklist localhost 4445

# Remove registered server info
#broklist localhost 4445 mydb+ip-11-120-5-132.ap-use-1.compute.internal+5080
