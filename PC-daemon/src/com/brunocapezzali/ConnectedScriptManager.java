
package com.brunocapezzali;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectedScriptManager extends Thread {
   private static final String TAG = "ConnectedScriptManager";

   private final MainServer mServer;
   private final Socket mSock;
   private final BufferedReader mSockReader;
   private final PrintWriter mSockWriter;
   
   private String mDeviceIdentifier;
   private String mDeviceCmd;

   public ConnectedScriptManager(MainServer server, Socket s) throws IOException {
      mServer = server;
      
      mSock = s;
      mSock.setSoTimeout(Config.kDaemonSockTimeout);
      mSockReader = new BufferedReader(
              new InputStreamReader(mSock.getInputStream(), "UTF-8"));
      mSockWriter = new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(mSock.getOutputStream(), "UTF-8")), true);
   }
   
   private void sockAbort() {
      sockAbort(false, null, null);
   }
   
   private void sockAbort(String error) {
      sockAbort(true, error, Config.kScriptError);
   }
   
   private void sockAbort(String error, String phpRetCode) {
      sockAbort(true, error, phpRetCode);
   }
   
   private void sockAbort(boolean error, String message, String phpRetCode) {
      Utils.log(TAG, "Aborting command connection."
              + (error ? "\nERROR: "+ message : " Everything OK") );

      // se è un errore inviamo l'error code a PHP
      if ( error ) {
         try {
            sockWriteln(phpRetCode);
         } catch (IOException ex) {
            Utils.log(TAG, "Error while aborting command socket: "+ ex.getMessage());
         }
      }

      // ripuliamo lo stato del socket
      try {
         mSock.shutdownOutput();
      } catch (IOException e) {}

      try {
         mSock.shutdownInput();
      } catch (IOException e) {}

      try {
         mSock.close();
      } catch (IOException e) {}
   }
   
   private String sockReadln() throws IOException {
      synchronized(mSockWriter) {
         return mSockReader.readLine();
      }
   }
   
   private void sockWriteln(String s) throws IOException {
      synchronized(mSockWriter) {
         mSockWriter.write(s +"\n");
         mSockWriter.flush();
      }
   }
   
   private void readCommandJSON() throws IOException, JSONException {
      /* Come messaggio di benvenuto viene inviato un JSON contenente
       * il deviceIdentifier al quale parlare e il comando da inviare.
       * N.B. Qui, al contrario di un device non c'è bisogno di autenticazione
       *      in quanto essendo la connessione dalla stessa macchina del server
       *      PHP siamo sicuri che sia lecita.
       */
      Utils.log(TAG, "Waiting for command json ...");
      String line = sockReadln();
      Utils.log(TAG, "... command json = "+ line);
      JSONObject json = new JSONObject(line);
      mDeviceIdentifier = json.getString("deviceIdentifier");
      mDeviceCmd = json.getString("cmd");
   }
            
   @Override
   public void run() {
      try {
         Utils.log(TAG, "Waiting for the command json ...");
         try {
            readCommandJSON();
         } catch (JSONException jex) {
            sockAbort("Error while parsing command json: "+ jex.getMessage());
            return;
         } catch (IOException ioex) {
            sockAbort("Error while receiving command json: "+ ioex.getMessage());
            return;
         }
         
         /* Ritorniamo immediatamente errore nei casi:
          * - Device non collegato al server
          * - Device con socket non alive il quale è stato rimosso
          */
         Device device = mServer.getDevice(mDeviceIdentifier);
         if ( device == null ) {
            sockAbort("No device found", Config.kScriptNoDevice);
            return;
         }
         Utils.log(TAG, "Device found and ready for receive command");
                  
         // Inviamo il comando al device
         String cmdReply;
         SyncCommand cmdSync = new SyncCommand();
         try {
            cmdReply = cmdSync.executeCommand(device, this.getId(), mDeviceCmd, 
                    Config.kScriptRequestTimeout);
         } catch (TimeoutException tex) {
            sockAbort("TIMEOUT: "+ tex.getMessage(), Config.kScriptTimeout);
            return;
         }
         Utils.log(TAG, "Tunneling command reply to script socket");
      
         try {
            sockWriteln(cmdReply);
         } catch (IOException ioex) {
            sockAbort("Error while tunnelling command reply: "+ ioex.getMessage());
            return;
         }
         sockAbort();
         
      } catch (Exception ex) {
         sockAbort("Unhandled error occurred: "+ ex.getMessage());
      }
   }
   
}
