package com.brunocapezzali;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DeviceTest {
   
   private static MainServer mMainServer;
   private TestClientSimulator mSimulatedDevice;
   private Device mDeviceInstance;
   
   @BeforeClass
   public static void setUpClass() {
      TestsConfig.initInstance();
      mMainServer = new MainServer();
      mMainServer.start();
   }
   
   @AfterClass
   public static void cleanEnvironment() {
      mMainServer.stopServer();
   }
   
   @Before
   public void connectSimulatedDevice() {
      try {
         mMainServer.setAllRemoteConnection(true); // only for debug purpose
         mSimulatedDevice = new TestClientSimulator();
         TestsConfig.delay(100);
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
   }
   
   @After
   public void tearDown() {
      mDeviceInstance = null;
   }
   
   private void authenticateSimulatedDevice() {
      // send the welcome json
      mSimulatedDevice.writeln(TestsConfig.deviceWelcome.toString());
      TestsConfig.delay(300);
      
      // check if everything is ok, and get the device instance
      mDeviceInstance = mMainServer.getDevice(
              TestsConfig.deviceWelcome.getString("identifier"));
      assertNotNull(mDeviceInstance);
   }
   
   /**
    * Test of getTimestampLastKeepAlive method, of class Device.
    */
   @Test
   public void keepAliveMechanismCheck() {
      System.out.println("* Device JUnit4Test: keepAliveMechanismCheck()");
      long waitInterval = 1000;

      // authenticate the device and get the initial timestamp
      authenticateSimulatedDevice();
      long preKeepAlive = mDeviceInstance.getTimestampLastKeepAlive();
      
      // sleep a delay and send the timestamp keepalive
      TestsConfig.delay(waitInterval);
      mSimulatedDevice.writeln(Config.kDeviceKeepAlive);
      TestsConfig.delay(100);
      long afterKeepAlive = mDeviceInstance.getTimestampLastKeepAlive();
      assertTrue(afterKeepAlive > preKeepAlive + waitInterval);
   }

   /**
    * Test of stopDevice method, of class Device.
    * NOTE: No one can call stopDevice except for the MainServer.
    * Don't call stopDevice directly but instead call MainServer.removeDevice(device)
    */
   @Test
   public void stopDeviceCheck() {
      System.out.println("* Device JUnit4Test: stopDeviceCheck()");

      // authenticate the device and then stop it
      authenticateSimulatedDevice();
      mMainServer.removeDevice(mDeviceInstance);
      TestsConfig.delay(150);
      
      // check if the device is corretly removed from the server
      assertFalse(mDeviceInstance.isDeviceActive());
      assertNull(mMainServer.getDevice(mDeviceInstance.getUniqueIdentifier()));
   }

   @Test
   public void parseRightWelcomeJSONCheck() {
      System.out.println("* Device JUnit4Test: parseRightWelcomeJSONCheck()");

      // Create a fake Device NOT authenticated
      try {
         mDeviceInstance = new Device(mMainServer, mSimulatedDevice.getSocket());
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
      
      JSONObject json = TestsConfig.deviceWelcome;
      mDeviceInstance.parseWelcomeJSON(json.toString());
      assertEquals(json.get("identifier"), mDeviceInstance.getUniqueIdentifier());
      assertEquals(json.get("networkType"), mDeviceInstance.getNetworkType());
      assertEquals(json.get("model"), mDeviceInstance.getModel());
      assertEquals(json.getLong("keepAliveInterval"), mDeviceInstance.getKeepAliveInterval());
   }

   @Test (expected = org.json.JSONException.class)
   public void parseWrongWelcomeJSONCheck() throws JSONException {
      System.out.println("* Device JUnit4Test: parseWrongWelcomeJSONCheck()");

      // Create a fake Device NOT authenticated
      try {
         mDeviceInstance = new Device(mMainServer, mSimulatedDevice.getSocket());
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
      
      // wrong welcome json will raise a JSONException
      mDeviceInstance.parseWelcomeJSON(TestsConfig.wrongJSON.toString());
      fail("We need to raise a JSONException.");
   }
}
