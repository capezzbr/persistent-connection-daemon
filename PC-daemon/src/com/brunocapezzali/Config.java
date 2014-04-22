
package com.brunocapezzali;

/**
 * Contains all the constants used inside this project. All constants are
 * grouped by three main category:
 * <li>Daemon constants</li>
 * <li>Script constants</li>
 * <li>Device constants</li>
 * 
 * @author Bruno Capezzali
 * @since 1.0.0
 */
public class Config {

   /* ----------------------------------------------------------------------- */
   
   /**
    * Defines the daemon version and can be used if you want to check custom
    * server functionality based on the version.
    */
   public static final String kDaemonVersion = "1.0.0";
   
   /**
    * Defines the daemon listening port both valid for a script or a device.
    */
   public static final int kDaemonPort = 1234;
   
   /**
    * Defines the socket timeout for every connection. Ex waiting for a welcome 
    * JSON, reading a command and so on. This timeout is defined in milliseconds.
    */
   public static final int kDaemonSockTimeout = 30000;

   /* ----------------------------------------------------------------------- */
   
   /**
    * Defines the value returned by the daemon to a script which wants to send a
    * command to a device currently not connected to the daemon.
    */
   public static final String kScriptNoDevice = "NODEVICE";
   
   /**
    * Defines the value returned by the daemon to a script which wants to send a
    * command to a device, currently listed as connected to the daemon,
    * but unable to reply in time.
    */
   public static final String kScriptTimeout = "TIMEOUT";
   
   /**
    * Defines the value returned by the daemon to a script which wants to 
    * send a command to a device returning an error (ex: wrong command, 
    * wrong command parameter).
    */
   public static final String kScriptError = "ERROR";
   
   /**
    * Defines the max number of milliseconds which a script will wait for
    * a device command's reply. This is extremely useful for grant a nonblocking 
    * request system between the script and the device. 
    * <p>The value of this constant need to be not to lower due to the network
    * problems but nor too high for prevent a too long wait.
    * </p>
    */
   public static final int kScriptRequestTimeout = 15000;

   /* ----------------------------------------------------------------------- */
   
   /**
    * Defines the constant used for keeping alive (persistent) the socket 
    * between the daemon and a device.
    */
   public static final String kDeviceKeepAlive = "KEEPALIVE";
      
   /**
    * Defines a margin in milliseconds that is added to the {@code KeepAliveInterval} 
    * value defined in the authentication between the daemon and the device.
    * This margin is useful due the network overhead.
    */
   public static final long kDeviceKeepAliveIntervalMargin = 1000 * 15;

   /**
    * Defines the number of device's timeouts tolerated by the daemon.
    * When a device doesen't execute a command and go in timeout a number
    * of times equals of {@code kDeviceMaxTimeout} then the daemon will
    * declare the device as zombie and remove it from the list.
    */
   public static final int kDeviceMaxTimeout = 2;
}

