#!/usr/bin/env  bash

mvn clean  package -Pclient

cp src/main/resources/config.properties target/config.properties

cd "target"

pwd

java -jar lightsocks-client.jar -c=config.propeties
