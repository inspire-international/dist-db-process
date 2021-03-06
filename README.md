# dist-db-process
The naming server of Nextra called Broker can keep track the applications' location info and provide the location info to the consumers whenever demanded.

# About this program

1. This Java program dynamically establishes a JDBC connection with one of DB engines in which the location info is fetched from Broker; the naming server of Nextra.

2. Pick one location info out of multiples and establish the connection.

3. If failed to establish the connection, then 1) remove the failed location info out of Broker and 2) reroute to another DB engine running at a different location.

4. Fetch all data entries in the specified table and generate them in a CSV file.

![Locate DB dynamically](http://www.inspire-intl.com/images/brokerAsNamingServer_en.jpg)


# Prerequisite
OS: Linux. We tested on AWS: amzn1.x86_64

Middleware: Nextra(broker & broklist). *) Provided in bin directory.

Jar files: Download the following 2 jar files and placed in ./jar directory.
* Apache log4j(We used log4j-1.2.17.jar).
* MySQL Connector/J(We used mysql-connector-java-5.1.34-bin.jar)

glibc >= 2.14 on which you run Broker from broker.sh

> How to upgrade glibc to 2.14 is well described at http://kakakikikeke.blogspot.jp/2014/10/centosdrone.html

# Environment setup
Either placing broker & broklist in $PATH directory or set this bin directory to $PATH.

# Modify suit in your enviroment
$vi broker.sh

$vi client.sh

$vi src/Processor.java  ... Modify SQL Engine listener port. Currently set as 5080.

# Build this program
$./buildClient.sh

# Create broker.env
$vi broker.env


> DCE_BROKER=localhost,4445



> DCE_DEBUGLEVEL=DEBUG,DEBUG



> DCE_LOG=broker.log



# Run this program
$./broker.sh

$./client.sh

[Naming Serer Solution in the Cloud](http://www.inspire-intl.com/product/nextra/doc/namingServerSolution_en.pdf)

#### Copyright (C) 1998 - 2015  Inspire International Inc.