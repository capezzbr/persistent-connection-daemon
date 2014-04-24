Persistent Connection Daemon (PC-daemon)
============================

A persistent connection daemon for mobile devices written in java. Sometimes we need to keep track of mobile devices connected to our services for sending or retrieve data. It's difficult to keep a persistent connection due to the mobility of smartphone (the device often change network). For solve this problem I've developed this Daemon that keep opened and active sockets.
The image below explain the infrastructure of the system:

![Scheme](environment_scheme.PNG)

How To Get Started 
==================

Daemon
---------
1. Open the NetBeans project and setup the right configuration in Config.java
2. Compile the daemon (Run > Clean and Build Project)
3. Start it using ```java -jar PCDaemon.jar 2>&1 /var/log/PCDaemon.log```
4. Check logs using ```tail -f /var/log/PCDaemon.log```

Script
---------
Check the basic PHP script available [here](/scripts/send-cmd.php) and create your own implementation. This is really a basic script, for example he don't manage the possibile errors returned by the daemon (no connected device, errore while sending command, ...).

Mobile (Android implementation)


JUnit Tests
---------
**JUnit tests** are available [here](PC-daemon/test/com/brunocapezzali)

License 
---------
Persistent Connection Daemon (PC-daemon) is available under the MIT license. See the LICENSE file for more info.