
package com.brunocapezzali;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Rapresents a command which is sent by a script, readed 
 * using a {@link ScriptThread} and managed by a {@link Device}. 
 * It will be encoded into a JSON and sent to a device via the 
 * <i>persistent socket</i>. When the device receive and execute 
 * the command return the output of its execution via the same 
 * <i>persistent socket</i> and we are able to notify this output 
 * to a {@link CommandReceiver}.
 * 
 * @author Bruno Capezzali
 * @see Device
 * @see ScriptThread
 * @since 1.0.0
 */
public class Command {  
   
   /**
    * Allows a {@link Device} to notify the receiver when
    * the command output was received via the <i>persistent socket</i>.
    */
   public interface CommandReceiver {
      public void notifyCommandReply(String reply);
   }
   
   /**
    * An object that implements the interface {@code CommandReceiver} and will
    * be notified when the device reply to this command.
    * @see CommandReceiver
    */
   private final CommandReceiver mCommandReceiver;
   
   /**
    * The unique identifier of this command, useful to link a reply to his
    * command.
    */
   private final String mCommandId;
   
   /**
    * The command {@code String} to send to a device
    */
   private final String mCommand;
   
   /**
    * A flag for check if this command is sent to a device. If no reply occurs
    * a timeout will be raised and we are able to dispose this command.
    */
   private boolean mSent;
   
   /**
    * Creates a <code>Command</code>
    * @param receiver the receiver of the command's output
    * @param id a String that identify this command
    * @param cmd a String which contain a command for a device
    */
   public Command(CommandReceiver receiver, String id, String cmd) {
      mCommandReceiver  = receiver;
      mCommandId        = id;
      mCommand          = cmd;
   }
   
   /**
    * Sets the <code>Command</code> as sent
    */
   public void setSent() {
      mSent = true;
   }

   /**
    * @return a {@code boolean} which indicating if the {@code Command} is sent
    */
   public boolean isSent() {
      return mSent;
   }
   
   /**
    * Notifies the command receiver when the command output 
    * was received via the <i>persistent socket</i>.
    * @param reply
    */
   public void notifyCommandReceiver(String reply) {
      mCommandReceiver.notifyCommandReply(reply);
   }
   
   /**
    * @return a {@code String} which rapresent the unique identifier
    */
   public String getId() {
      return mCommandId;
   }
   
   /**
    * @return a {@code String} which rapresent the command
    */
   public String getCommand() {
      return mCommand;
   }

   /**
    * @return a {@code String} which rapresent the command
    */
   public JSONObject getJSONCommand() throws JSONException {
      JSONObject jsonCmd = new JSONObject();
      jsonCmd.put("id", mCommandId);
      jsonCmd.put("cmd", mCommand);
      
      return jsonCmd;
   }
}
