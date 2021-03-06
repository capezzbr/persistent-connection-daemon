
package com.brunocapezzali;

import java.io.IOException;
import java.net.Socket;
import org.json.JSONException;

/**
 * Rapresents a new remote connection to the Daemon that has to be
 * managed. If this new connection send a valid authentication JSON 
 * and the validation phase done by {@link authenticateDevice()} is ok,
 * than a new {@link Device} will be created.
 * 
 * @author Bruno Capezzali
 * @see Device
 * @since 1.0.0
 */
public class ConnectedDeviceManager extends Thread {
   private static final String TAG = "ConnectedDeviceManager";
   
   private final MainServer mServer;
   private final Device mNewDevice;
   
   public ConnectedDeviceManager(MainServer server, Socket deviceSock) throws IOException {
      mServer = server;
      mNewDevice = new Device(mServer, deviceSock);
   }
      
   private void abortDeviceConnection(String error) {
      Utils.log(TAG, "Aborting device connection. ERROR: "+ error);
      mNewDevice.sockClose();
   }
   
   private boolean authenticateDevice() {
      /* The authentication can be based on:
       * - SSL
       * - WelcomeJSON parameters and custom authentication algorithm
       * - Database check of the device
       */
      return true;
   }
   
   @Override
   public void run() {
      try {
         Utils.log(TAG, "Waiting for the welcome json ...");
         try {
            String strJson = mNewDevice.sockReadln();
            Utils.log(TAG, "Received auth json: "+ strJson);
            mNewDevice.parseWelcomeJSON(strJson);
         } catch (JSONException ex) {
            abortDeviceConnection(ex.getMessage());
            return;
         } catch (IOException ex) {
            abortDeviceConnection(ex.getMessage());
            return;
         }
         
         if ( !authenticateDevice() ) {
            abortDeviceConnection("Unable to authenticate the device");
            return;
         }
         
         Utils.log(TAG, "Authentication success!\n"
                 +"Adding the device to daemon's clients list.");
         mServer.addDevice(mNewDevice);
         
      } catch (Exception ex) {
         abortDeviceConnection("Unhandled error: "+ ex.getMessage());
      }
   }
   
}
