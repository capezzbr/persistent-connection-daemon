package com.brunocapezzali;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author capezzbr
 */
public class MainServerTest {
   private static MainServer mMainServer;
   
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
   
   public TestClientSimulator connectAndAuthenticateSimulatedDevice(String identifier) {
      try {
         mMainServer.setAllRemoteConnection(true); // only for debug purpose
         TestClientSimulator client = new TestClientSimulator();
         TestsConfig.delay(100);
         
         // customizing the welcome json
         JSONObject welcome = TestsConfig.deviceWelcome;
         welcome.put("identifier", identifier);
         client.writeln(welcome.toString());
         TestsConfig.delay(300);
      
         Device toCheck = mMainServer.getDevice(identifier);
         assertNotNull(toCheck);
         return client;
      } catch (IOException ioex) {
         fail(ioex.getMessage());
      }
      return null;
   }

   /**
    * Test of removeDevice method, of class MainServer.
    */
   @Test
   public void removeDeviceCheck() {
      System.out.println("* MainServer JUnit4Test: removeDeviceCheck()");
      
      // connect two clients
      connectAndAuthenticateSimulatedDevice("one");
      connectAndAuthenticateSimulatedDevice("two");      
      
      // remove the first client
      mMainServer.removeDevice(mMainServer.getDevice("one"));
      assertEquals(1, mMainServer.getDevicesCount());
      
      // remove the last client
      mMainServer.removeDevice(mMainServer.getDevice("two"));
      assertEquals(0, mMainServer.getDevicesCount());
   }
   
   /**
    * Test of addDevice method, of class MainServer.
    */
   @Test
   public void addDeviceCheck() {
      System.out.println("* MainServer JUnit4Test: addDeviceCheck()");

      // try to connect a client
      connectAndAuthenticateSimulatedDevice("one");
      assertEquals(1, mMainServer.getDevicesCount());

      // try to connect an already connected client
      connectAndAuthenticateSimulatedDevice("one");
      assertEquals(1, mMainServer.getDevicesCount());
   }
}
