package com.brunocapezzali;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author capezzbr
 */
public class ConnectedDeviceManagerTest {
   private static MainServer mMainServer;
   private TestClientSimulator mSimulatedDevice;
      
   @BeforeClass
   public static void setupEnvironment() {
      TestsConfig.getInstance();
      mMainServer = new MainServer();
      mMainServer.start();
   }
   
   @AfterClass
   public static void cleanupEnvironment() {
      mMainServer.stopServer();
   }
   
   @Before
   public void setUp() {
      try {
         mMainServer.setAllRemoteConnection(true); // only for debug purpose
         mSimulatedDevice = new TestClientSimulator();
        } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
   }
   
   @Test
   public void wrongWelcomeCheck() {
      System.out.println("* ConnectedDeviceManager JUnit4Test: wrongWelcomeCheck()");
      int devicesBefore = mMainServer.getDevicesCount();
      
      TestsConfig.delay(100);
      mSimulatedDevice.writeln(TestsConfig.wrongJSON.toString() +"\n");
      TestsConfig.delay(100);

      assertTrue(mMainServer.getDevicesCount() == devicesBefore);      
   }
   
   @Test
   public void rightWelcomeCheck() {
      System.out.println("* ConnectedDeviceManager JUnit4Test: rightDeviceCheck()");
      int devicesBefore = mMainServer.getDevicesCount();

      TestsConfig.delay(100);
      mSimulatedDevice.writeln(TestsConfig.deviceWelcome.toString() +"\n");
      TestsConfig.delay(100);

      assertTrue(mMainServer.getDevicesCount() == devicesBefore + 1);
      assertNotNull(mMainServer.getDevice("C1P8"));
   }
}