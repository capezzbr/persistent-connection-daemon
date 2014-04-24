
package com.brunocapezzali;

import java.util.concurrent.TimeoutException;

/**
 * This class allow to synchronize the execution of a command.
 * So we are able to create a simple paradigm to verify if a request between
 * Script <-> Daemon <-> Device will go on Timeout and with safety aborting it.
 * Whenever a command reply is read by a {@link Device} we can notify it to the
 * {@link ConnectedScriptManager} and correctly send a response to the script.
 * 
 * @author Bruno Capezzali
 * @see ConnectedScriptManager
 * @see Device
 * @since 1.0.0
 */
public class SyncCommand implements Command.CommandReceiver {
   private static final String TAG = "SyncCommand";
   
   private String mCmdId;
   private String mCmdReply;
   
   @Override
   synchronized public void notifyCommandReply(String reply) {
      mCmdReply = reply;
      Utils.log(TAG, "Arrived reply for commandId = '"+ mCmdId +"' from device");
   }

   public String executeCommand(Device device, long threadId, String command, 
           long timeout) throws TimeoutException {
      
      mCmdId = Utils.MD5(String.valueOf(threadId * System.currentTimeMillis())).substring(8);
      device.sendCommand(this, mCmdId, command);

      // Wait loop
      long end = System.currentTimeMillis() + timeout;
      boolean replyArrived = false;
      while ( System.currentTimeMillis() < end ) {
         if ( mCmdReply != null ) {
            replyArrived = true; // Verify if the reply is available
            break;
         }
         try {
            Thread.sleep(200);
         } catch (InterruptedException t) {}
      }

      if ( !replyArrived ) {
         device.commandExecutionTimedout(mCmdId);
         throw new TimeoutException("No reply after "+ (timeout / 1000) +"sec");
      }
      return mCmdReply;
   }
}
