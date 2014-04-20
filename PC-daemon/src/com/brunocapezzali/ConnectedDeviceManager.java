
package com.brunocapezzali;

import java.io.IOException;
import java.net.Socket;
import org.json.JSONException;

public class ConnectedDeviceManager extends Thread {
   private static final String TAG = "ConnectedDeviceManager";
   
   private final MainServer mServer;
   private Socket mDeviceSock;
   private Device mNewDevice;
   
   public ConnectedDeviceManager(MainServer server, Socket s) {
      mServer = server;
      mDeviceSock = s;
      mNewDevice = null; // viene inizializzato in start()
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
         /* Creiamo il nuovo device e successivamente andiamo a verificare l'autenticazione
          * e se rispetta alcune parti del protocollo. Se tutto è OK allora questo Device
          * verrà aggiunto all'elenco dei client validi del demone */
         try {
            mNewDevice = new Device(mServer, mDeviceSock);
            mDeviceSock = null; // non lo rendiamo più accessibile dall'esterno della classe Device
         } catch (IOException ex) {
            Utils.log(TAG, "Error while creating new Device: "+ ex.getMessage());
         }
         
         Utils.log(TAG, "Waiting for the auth json ...");
         try {
            mNewDevice.readWelcomeJSON();
         } catch (JSONException ex) {
            abortDeviceConnection(ex.getMessage());
         } catch (IOException ex) {
            abortDeviceConnection(ex.getMessage());
         }
         
         if ( !authenticateDevice() ) {
            abortDeviceConnection("Unable to authenticate the device");
         }
         
         Utils.log(TAG, "Authentication success!\n"
                 +"Adding the device to daemon's clients list.");
         mServer.addDevice(mNewDevice);
         
      } catch (Exception ex) {
         abortDeviceConnection("Unhandled error: "+ ex.getMessage());
      }
   }
   
}
