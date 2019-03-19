## lightsocks-java
A fast proxy that helps you bypass firewalls.


### System requirements
 *  Java 7+
 *  maven

### Quickstart
Run the script server_start.sh or client_start.sh on terminal,change setttings through [src/main/resources/config.properties](https://github.com/lightsocks/lightsocks-java/blob/master/src/main/resources/config.properties),see the detail below.

### How to build
```
On Server

mvn package -Pserver

On Client

mvn package -Pclient
```
this tool is implemented by netty , you can get more information from [netty.io](http://netty.io),there is also a maven plugin [maven-shade-plugin](http://maven.apache.org/plugins/maven-shade-plugin/) for packaging the dependency  into one jar .

### How to use

Both the server and client tool will look for  config.properties in the current directory or classpath. You can use -c option to specify another configuration file. Download the sample [config.properties](https://github.com/lightsocks/lightsocks-java/blob/master/src/main/resources/config.properties), change the following values as you wish:

server.ip &nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;server ip or hostname<br>
server.port &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;server port<br>
password &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; password used to encrypt data<br>
method &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;default aes-cfb-128<br>
local.ip &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;local address<br>
local.port &nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;local socks5 proxy port<br>

```
On Server
java -jar lightsocks-server.jar -c=config.properties

On Client
java -jar lightsocks-client.jar -c=config.properties

Change proxy settings of your browser to
SOCKS5 127.0.0.1:8888
```
### Proxy settings
Use SwitchyOmega Plugin on chrome is quite helpful.

### Reference
[rfc1928](http://www.ietf.org/rfc/rfc1928.txt)
