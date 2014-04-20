package com.brunocapezzali;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class MainServer {
   private static final String TAG = "MainServer";

   private final long mStartTimestamp = System.currentTimeMillis();
   private InetAddress mLANAddress = null;
   private final HashMap<String, Device> mDevices = new HashMap<String, Device>();
   
   public long getStartTimestamp() {
      return mStartTimestamp;
   }
   
   public void removeDevice(Device d) {
      Utils.log(TAG, "Removing device with uniqueIdentifier = "
              + d.getUniqueIdentifier()  +", connected since "
              + Utils.timestampDifferenceNow(d.getTimestampConnected()));
      synchronized (mDevices) {
         mDevices.remove(d.getUniqueIdentifier());
      }
      d.stopDevice();
      d.sockClose();
   }
      
   public Device getDevice(String uniqueIdenfier) {
      synchronized (mDevices) {
         if ( mDevices.containsKey(uniqueIdenfier) ) {
            Device toReturn = mDevices.get(uniqueIdenfier);
            if ( toReturn.isDeviceActive() ) {
               return toReturn;
            } else {
               Utils.log(TAG, "Device with uniqueIdentifier = "
                       + uniqueIdenfier +" found, but NOT isDeviceActive()");
               removeDevice(toReturn);
            }
         }
         return null;
      }
   }
   
   public void addDevice(Device device) {
      synchronized (mDevices) {
         if ( mDevices.containsKey(device.getUniqueIdentifier()) ) {
            Device toRemove = mDevices.get(device.getUniqueIdentifier());
            Utils.log(TAG, "Device with uniqueIdentifier = "
                    + toRemove.getUniqueIdentifier() +" already in list.");
            removeDevice(toRemove);
         }
         mDevices.put(device.getUniqueIdentifier(), device);
         device.start();
      }
   }
   
   private boolean isLocalConnection(InetAddress addr) {
      if ( addr.isLoopbackAddress() || addr.equals(mLANAddress) ) {
         return true;
      }
      return false;
   }
   
   private void determinateLocalAddress() {
      try {
         mLANAddress = InetAddress.getLocalHost();
      } catch (UnknownHostException ex) {
         Utils.log(TAG, "Unable to determine local IP");
         System.exit(2);
      }      
   }

   public void start() {      
      determinateLocalAddress();
      
      ServerSocket serverSocket = null;
      try {
         serverSocket = new ServerSocket(Config.kDaemonPort);
      } catch (IOException e) {
         Utils.log(TAG, "Could not listen on port: "
                 + Config.kDaemonPort);
         System.exit(2);
      }
      
      Utils.log(TAG, "Listening on "+ mLANAddress.toString() +":"
              + Config.kDaemonPort);
      
      Socket client;
      InetAddress clientAddress;
      while (true) {
         try {
            client = serverSocket.accept();
         } catch (IOException e) {
            Utils.log(TAG, "Accept failed: "+ e.getMessage());
            continue;
         }
         
         // se IP localIP allora comando da server PHP per client
         // altrimenti è un normale client di cui dobbiamo gestire
         // l'autenticazione
         clientAddress = client.getInetAddress();
         if ( isLocalConnection(clientAddress) ) {
            Utils.log(TAG, "New local script connection");
            ( new ConnectedScriptManager(this, client) ).start();
         } else {
            Utils.log(TAG, "New device connection ("+ clientAddress.toString() +")");
            ( new ConnectedDeviceManager(this, client) ).start();
         }
      }
   }
}
