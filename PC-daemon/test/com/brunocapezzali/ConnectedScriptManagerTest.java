package com.brunocapezzali;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author capezzbr
 */
public class ConnectedScriptManagerTest {
   private static MainServer mMainServer;
   private TestClientSimulator mSimulatedDevice;
   private TestClientSimulator mSimulatedScript;

   @BeforeClass
   public static void setUpClass() {
      TestsConfig.getInstance();
      mMainServer = new MainServer();
      mMainServer.start();
   }
   
   @AfterClass
   public static void cleanEnvironment() {
      mMainServer.stopServer();
   }

   @Before
   public void connectSimulatedScript() {
      try {
         mMainServer.setAllRemoteConnection(false); // default value
         mSimulatedScript = new TestClientSimulator();
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
   }
   
   private void connectAndAuthenticateSimulatedDevice() {
      try {
         mMainServer.setAllRemoteConnection(true); // only for debug purpose
         mSimulatedDevice = new TestClientSimulator();
         TestsConfig.delay(100);
         mSimulatedDevice.writeln(TestsConfig.deviceWelcome.toString());
         TestsConfig.delay(100);
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
   }
   
   private void simulatedDeviceReadAndReplyCommand() {
      try {
         // read command
         JSONObject recvCommand = new JSONObject(mSimulatedDevice.readln());
         assertEquals(recvCommand.getString("cmd"), 
                 TestsConfig.scriptCommandJSON.getString("cmd"));

         // generate command reply
         JSONObject cmdReply = new JSONObject();
         cmdReply.put("id", recvCommand.getString("id"));
         cmdReply.put("reply", TestsConfig.deviceCmdReply);
         mSimulatedDevice.writeln(cmdReply.toString());
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      } catch (JSONException jex) {
         fail(jex.getMessage());
      }
   }
   
   @Test
   public void wrongCommandJSONCheck() {
      System.out.println("* ConnectedScriptManager JUnit4Test: wrongCommandJSONCheck()");

      // simulated script send an invalid command
      mSimulatedScript.writeln(TestsConfig.wrongJSON.toString() +"\n");
      TestsConfig.delay(100);

      try {
         assertEquals(Config.kScriptError, mSimulatedScript.readln());
      } catch (IOException ioex) {
         fail(ioex.getMessage());         
      }
   }
   
   @Test
   public void notConnectedDeviceCheck() {
      System.out.println("* ConnectedScriptManager JUnit4Test: notConnectedDeviceCheck()");

      // simulated script send a valid command but there isn't a connected device
      mSimulatedScript.writeln(TestsConfig.scriptCommandJSON.toString());
      TestsConfig.delay(100);

      try {
         assertEquals(Config.kScriptNoDevice, mSimulatedScript.readln());
      } catch (IOException ioex) {
         fail(ioex.getMessage());         
      }
   }
   
   @Test
   public void timeoutCommandCheck() {
      System.out.println("* ConnectedScriptManager JUnit4Test: timeoutCommandCheck()");

      connectAndAuthenticateSimulatedDevice();
      TestsConfig.delay(100);
      
      // simulated script send a valid command
      mSimulatedScript.writeln(TestsConfig.scriptCommandJSON.toString());
      TestsConfig.delay(100);

      try {
         // the simulated device never reply, so we go in timeout
         assertEquals(Config.kScriptTimeout, mSimulatedScript.readln());
      } catch (IOException ioex) {
         fail(ioex.getMessage());         
      }
   }
   
   @Test
   public void everythingFineCheck() {
      System.out.println("* ConnectedScriptManager JUnit4Test: everythingsFineCheck()");

      // simulate a device connection
      TestsConfig.delay(100);
      connectAndAuthenticateSimulatedDevice();
      
      // simulated script send a valid command
      mSimulatedScript.writeln(TestsConfig.scriptCommandJSON.toString());
      TestsConfig.delay(1000);
      
      // the simulated device read and reply to the script command
      simulatedDeviceReadAndReplyCommand();
      TestsConfig.delay(1000);
      
      try {
         // the simulated script have to read the same reply sended by the simulateed device
         assertEquals(TestsConfig.deviceCmdReply, mSimulatedScript.readln());
      } catch (IOException ioex) {
         fail(ioex.getMessage());         
      }
   }
}
