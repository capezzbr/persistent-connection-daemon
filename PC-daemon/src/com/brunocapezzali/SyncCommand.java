
package com.brunocapezzali;

import java.util.concurrent.TimeoutException;

public class SyncCommand implements Command.CommandReceiver {
   private static final String TAG = "SyncCommand";
   
   private String mCmdId;
   private String mCmdReply;
   
   @Override
   public synchronized void notifyCommandReply(String reply) {
      mCmdReply = reply;
      Utils.log(TAG, "Arrived reply for commandId = '"+ mCmdId +"' from device");
   }

   public String executeCommand(Device device, long threadId, String command, 
           long timeout) throws TimeoutException {
      
      mCmdId = Utils.MD5(String.valueOf(threadId * System.currentTimeMillis())).substring(8);
      device.sendCommand(this, mCmdId, command);

      // waiting della risposta entro un certo timeout
      long end = System.currentTimeMillis() + timeout;
      boolean replyArrived = false;
      while ( System.currentTimeMillis() < end ) {
         if ( mCmdReply != null ) {
            replyArrived = true; // verifico se la risposta mi è arrivata
            break;
         }
         try {
            Thread.sleep(200); // sleep di 200 ms
         } catch (InterruptedException t) {}
      }

      if ( !replyArrived ) {
         device.commandExecutionTimedout(mCmdId);
         throw new TimeoutException("No reply after "+ (timeout / 1000) +"sec");
      }
      return mCmdReply;
   }
}
