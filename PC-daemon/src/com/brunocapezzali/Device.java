package com.brunocapezzali;

import com.brunocapezzali.Command.CommandReceiver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Rapresents the life-cycle of a mobile device connected to this daemon.
 * Every time a device try to connect to the daemon and send a valid 
 * <i>welcome json</i> the daemon create an instance of {@code Device}.
 * So we have a {@code Device} instance for every connected (and still alive) 
 * device. This class have a running thread that continuously check the 
 * following:
 * <li>Verifiy a <i>KeepAlive</i> mechanism for trust that the persistent connection
 * is still alive. If a device doesn't send a {@code KeepAlive} every definied
 * interval we will set it as a zombie connection.</li>
 * <li>Verify for new commands to send to the device.</li>
 * <li>Verify for new command's reply.</li>
 * 
 * @author Bruno Capezzali
 * @see NewDeviceThread
 * @see ScriptThread
 * @since 1.0.0
 */
public class Device extends Thread {
   private static final String TAG = "Device";

   private final MainServer mServer;

   private String mIdentifier;
   private final Socket mSock;
   private final BufferedReader mReader;
   private final PrintWriter mWriter;
   
   /* Statistics & informations (useful for futures plug-ins) */
   private String mNetworkType;
   private String mModel;
   private final long mTimestampConnected;
   private long mTimestampLastKeepAlive;
   private String mLastCommand;
   
   private final HashMap<String, Command> mCommands;
   private boolean mStopThread;
   private long mKeepAliveInterval;
   private int mCommandTimeoutCount;

   public Device(MainServer server, Socket s) throws IOException {
      mServer = server;
      
      mSock = s;
      mSock.setKeepAlive(true);
      mSock.setSoTimeout(Config.kDaemonSockTimeout); /* Non blocking socket*/
      mReader = new BufferedReader(
              new InputStreamReader(s.getInputStream(), "UTF-8"));
      mWriter = new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(s.getOutputStream(), "UTF-8")), true);
      
      mTimestampConnected = System.currentTimeMillis();
      mTimestampLastKeepAlive = mTimestampConnected;
      
      mStopThread = false;
      mCommands = new HashMap<String, Command>();
      mCommandTimeoutCount = 0;
   }
   
   public String getUniqueIdentifier() {
      return mIdentifier;
   }
   
   public long getTimestampConnected() {
      return mTimestampConnected;
   }
   
   public String getModel() {
      return mModel;
   }
      
   public long getKeepAliveInterval() {
      return mKeepAliveInterval;
   }
   
   public String getNetworkType() {
      return mNetworkType;
   }
   
   public long getTimestampLastKeepAlive() {
      return mTimestampLastKeepAlive;
   }
   
   synchronized public String getLastCommand() {
      return mLastCommand;
   }
   
   /**
    * Do not use directly. Instead use MainServer.removeDevice(this).
    */
   synchronized public void stopDevice() {
      mStopThread = true;
   } 
   
   public boolean isDeviceActive() {
      return !mStopThread;
   }
   
   private void incrementCommandTimeoutCount() {
      mCommandTimeoutCount++;
      if ( mCommandTimeoutCount >= Config.kDeviceMaxTimeout ) {
         Utils.log(TAG, "Too many command timeout ("+ 
                 Config.kDeviceMaxTimeout +"), removing zombie device.");
         mServer.removeDevice(this);
      } else {
         Utils.log(TAG, "Number of commands timeout incremented: "+ 
                 mCommandTimeoutCount);
      }
   }
   
   private void resetCommandTimeoutCount() {
      mCommandTimeoutCount = 0;
      Utils.log(TAG, "Number of commands timeout resetted to 0");
   }
   
   /* 
    * Gestione coda dei comandi che arrivano da un NewCommandThread 
    */
   
   public void sendCommand(CommandReceiver t, String id, String cmd) {
      synchronized(mCommands) {
         mCommands.put(id, new Command(t, id, cmd));
      }
   }
   
   private Command getCommandById(String id) {
      synchronized(mCommands) {
         return mCommands.get(id);
      }
   }
   
   private void removeCommandById(String id) {
      synchronized(mCommands) {
         if ( mCommands.containsKey(id) ) {
            mCommands.remove(id);
         }
      }
   }
   
   private void removeCommand(String id) {
      synchronized(mCommands) {
         mCommands.remove(id);
      }
   }
   
   @Override
   public void run() {
      try {
         String line;
         while ( !mStopThread ) {
            
            /* Check if there is data into the socket and check the type 
             * of data: KeepAlive or a command reply */
            if ( sockSafeHaveData() && (line = sockSafeReadln()) != null 
                    && line.length() > 0 ) {
               
               if ( line.equals( Config.kDeviceKeepAlive ) ) {
                  replyToKeepAlive();
               } else {
                  manageCommandReply(line);
               }
               resetCommandTimeoutCount(); // reset the timeout count, the socket is alive
            }
            
            // Check if this socket is broken (no data read before the timeout)
            if ( isKeepAliveTimeout() ) {
               Utils.log(TAG, "run() - Too time from the last received Keep-Alive. "
                       + "Aborting device connection.");
               mStopThread = true;
               break;
            }
            
            // Send (eventually) new commands
            sendUnsentCommands();
            
            try {
               Thread.sleep(100);
            } catch (InterruptedException t) {}
            
         }
         Utils.log(TAG, "run() - Thread stopped!");
      } catch (Exception eex) {
         Utils.log(TAG, "run() - Unhandled error: "+ eex.getMessage());
      }
   }
   
   private boolean isKeepAliveTimeout() {
      return ( System.currentTimeMillis() > mTimestampLastKeepAlive + 
              mKeepAliveInterval + Config.kDeviceKeepAliveIntervalMargin );
   }
      
   private void sendUnsentCommands() {
      synchronized(mCommands) {
         for ( String key : mCommands.keySet() ) {
            Command cmd = mCommands.get(key);
            if ( !cmd.isSent() ) {
               try {
                  Utils.log(TAG, "Sending to deviceIdentifier = "+ cmd.getId() +" a command = '"+ cmd.getCommand() +"'");
                  sockWriteln(cmd.getJSONCommand().toString());
                  cmd.setSent();
                  mLastCommand = cmd.getCommand();
               } catch (IOException ex) {
                  Utils.log(TAG, "Error sockWriteln(command): "+ ex.getMessage());
               } catch (JSONException jex) {
                  Utils.log(TAG, "Error JSON getJSONCommand(): "+ jex.getMessage());
               }  
            }  
         }
      }
   }
   
   private void replyToKeepAlive() {
      try {
         mTimestampLastKeepAlive = System.currentTimeMillis();
         Utils.log(TAG, "Received KeepAlive. Sending KeepAlive reply");
         sockWriteln(Config.kDeviceKeepAlive);
      } catch (IOException ex) {
         Utils.log(TAG, "Error sockWriteln(KeepAlive): "+ ex.getMessage());
      }
   }
   
   private void manageCommandReply(String reply) {
      String cmdReplyID, cmdReplyStr;
      try {
         JSONObject cmdReplyJson = new JSONObject(reply);
         cmdReplyID = cmdReplyJson.getString("id");
         cmdReplyStr = cmdReplyJson.getString("reply");
      } catch (JSONException jex) {
         Utils.log(TAG, "Error while parsing JSON: "+ jex.getMessage());
         return;
      }
      
      Command cmdWithReply = getCommandById(cmdReplyID);
      if ( cmdWithReply == null ) {
         Utils.log(TAG, "Received command reply with Id = "+ cmdReplyID +". "
                 + "Unable to find parent request (closed for timeout)");
      } else {
         Utils.log(TAG, "Received command reply with Id = "+ cmdReplyID +". "
                 + "Value: '"+ cmdReplyStr +"'");
         
         /* Notify the reply string to the script thread which is waiting for a
          * reply. After this we can remove the ended command */
         cmdWithReply.notifyCommandReceiver(cmdReplyStr);
         removeCommand(cmdReplyID);
      }
   }

   public void commandExecutionTimedout(String commandId) {
      removeCommandById(commandId);
      incrementCommandTimeoutCount();
   }
   
   private boolean sockSafeHaveData() {
      synchronized(mReader) {
         try {
            return mReader.ready();
         } catch (IOException ex) {
            Utils.log(TAG, "Error mReader.ready(): "+ ex.getMessage());
            return false;
         }
      }
   }
   
   public void parseWelcomeJSON(String strJson) throws JSONException {     
      JSONObject json = new JSONObject(strJson);
      mIdentifier = json.getString("identifier");
      mNetworkType = json.getString("networkType");
      mModel = json.getString("model");
      mKeepAliveInterval = json.getLong("keepAliveInterval");
   }
   
   private String sockSafeReadln() {
      try {
         return sockReadln();
      } catch (IOException ex) {
         Utils.log(TAG, "Error mReader.readLine(): "+ ex.getMessage());
         return null;
      }
   }

   public String sockReadln() throws IOException {
      synchronized(mReader) {
         return mReader.readLine();
      }
   }
   
   private void sockWriteln(String str) throws IOException {
      synchronized(mWriter) {
         mWriter.write(str + "\n");
         mWriter.flush();
      }
   }
   
   public synchronized void sockClose() {
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

   @Override
   public String toString() {
      String connectedSince = Utils.timestampDifferenceNow(getTimestampConnected());
      String lastKeepAlive = Utils.timestampDifferenceNow(getTimestampLastKeepAlive());
      
      StringBuilder sb = new StringBuilder();
      sb.append("Identifier: ").append(getUniqueIdentifier()).append(", ");
      sb.append("Model: ").append(getModel()).append(", ");
      sb.append("Network type: ").append(getNetworkType()).append(", ");
      sb.append("Connected since: ").append(connectedSince).append(", ");
      sb.append("Is active? ").append(isDeviceActive()).append(", ");
      sb.append("Keep-Alive interval: ").append(getKeepAliveInterval() / 1000 / 60).append(", ");
      sb.append("Last Keep-Alive received: ").append(lastKeepAlive).append(", ");
      sb.append("Last command: ").append(getLastCommand());
      
      return sb.toString();
   }

}
