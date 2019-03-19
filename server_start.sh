#!/usr/bin/env  bash

mvn clean  package -Pserver

cp src/main/resources/config.properties target/config.properties

cd "target"

pwd

java -jar lightsocks-server.jar -c=config.propeties
