## lightsocks-java
A fast proxy that helps you bypass firewalls.

At first, i plan implementing the shadowsocks protocol  which is used to many popular tools  [shadowsocks](https://github.com/shadowsocks/) .  I find the AES encrpt/decrpt stream  will not end if the src bytes length  is less than 16  when invoke update method of JCE cipher (aes-cfb-128）, this will cause some problems , the connection will wait for the rest data but it is cached in the buff e.g.  By reading the [shadowsocks-go](https://github.com/shadowsocks/shadowsocks-go) code  I also find  shadowsocks handshaking process designed  not good engough when the server can't not reach the destination address  but the data has been transfered  from broswer to proxy client.

After some thinking , i make a decision that develp the tool use a new protocal  below:<br>
 *  the handshake will be ended not only the client and server have exchanged  iv but also the server side has connect the            destination server.
 *  each packet has two field plus .One is the validate length of encrpted data ,the other is the total length. This design has       much benefit when writing network program.

### System requirements
 *  Java 8 (if your use jdk5/jdk6/jdk7, make surce it can compile success, other wise,you should  modify the code. )
 *  maven

### How to build
```
On Server

mvn package -Pserver

On Client 

mvn package -Pclient
```
this tool is implemented by netty , you can get it more from [netty.io](http://netty.io),there is alao a [maven-shade-plugin](maven.apache.org/plugins/maven-shade-plugin/) for packaging the dependency class into jar together.

### How to use

Both the server and client tool will search config.properties in the current directory or classpath. You can use -c option to specify another configuration file. Download the sample [config.properties](https://github.com/lightsocks/lightsocks-java/blob/master/src/main/resources/config.properties), change the following values:

server.ip &nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;server ip or hostname<br>
server.port &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;server port<br>
password &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; password used to encrypt data<br>
method &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;default aes-cfb-128<br>
local.ip &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;local address<br>
local.port &nbsp;&nbsp;&nbsp;&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;local socks5 proxy port<br>

```
On Server
java -jar lightsocks-server.jar -c=config.propeties

On Client
java -jar lightsocks-client.jar -c=config.propeties

Change proxy settings of your browser to
SOCKS5 127.0.0.1:local.port
```

 
### Reference
[rfc1928](http://www.ietf.org/rfc/rfc1928.txt)
