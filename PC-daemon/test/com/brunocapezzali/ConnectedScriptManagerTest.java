package com.brunocapezzali;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
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
   public void setUp() {
      connectScript();
   }
   
   private void connectScript() {
      try {
         mMainServer.setAllRemoteConnection(false); // default value
         mSimulatedScript = new TestClientSimulator();
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
   }
   
   private void connectAndAuthenticateNewDevice() {
      try {
         mMainServer.setAllRemoteConnection(true); // only for debug purpose
         mSimulatedDevice = new TestClientSimulator();
         TestsConfig.delay(100);
         mSimulatedDevice.writeln(TestsConfig.deviceWelcome.toString() +"\n");
         TestsConfig.delay(100);
        } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
   }
   
   @Test
   public void wrongCommandJSONCheck() {
      System.out.println("* ConnectedScriptManager JUnit4Test: wrongCommandJSONCheck()");

      TestsConfig.delay(100);
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

      TestsConfig.delay(100);
      mSimulatedScript.writeln(TestsConfig.scriptCommandJSON.toString() +"\n");
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

      connectAndAuthenticateNewDevice();
      TestsConfig.delay(100);
      mSimulatedScript.writeln(TestsConfig.scriptCommandJSON.toString() +"\n");
      TestsConfig.delay(100);

      try {
         assertEquals(Config.kScriptTimeout, mSimulatedScript.readln());
      } catch (IOException ioex) {
         fail(ioex.getMessage());         
      }
   }
   
   @Test
   public void everythingsFineCheck() {
      System.out.println("* ConnectedScriptManager JUnit4Test: everythingsFineCheck()");
      String deviceReply = "perfect!";
      
      connectAndAuthenticateNewDevice();
      TestsConfig.delay(100);
      mSimulatedScript.writeln(TestsConfig.scriptCommandJSON.toString() +"\n");
      TestsConfig.delay(100);
//      mSimulatedDevice.writeln(deviceReply);
//      TestsConfig.delay(100);
      try {
         assertEquals(deviceReply, mSimulatedScript.readln());
      } catch (IOException ioex) {
         fail(ioex.getMessage());         
      }
   }



   
}
