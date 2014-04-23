package com.brunocapezzali;

import org.json.JSONObject;

/**
 *
 * @author capezzbr
 */
public class TestsConfig {
   private static TestsConfig mInstance = null;
   
   public static JSONObject deviceWelcome;
   public static JSONObject scriptCommandJSON;
   public static JSONObject wrongJSON;
   public static final String deviceCmdReply = "12 days and 3 hours";

   private TestsConfig() {
      
      deviceWelcome = new JSONObject();
      deviceWelcome.put("identifier", "C1P8");
      deviceWelcome.put("networkType", "mobile");
      deviceWelcome.put("model", "Droid");
      deviceWelcome.put("keepAliveInterval", 5000);
      
      scriptCommandJSON = new JSONObject();
      scriptCommandJSON.put("deviceIdentifier", "C1P8");
      scriptCommandJSON.put("cmd", "uptime");
      
      wrongJSON = new JSONObject();
      wrongJSON.put("WrongField1", "none");
      wrongJSON.put("WrongField2", "none");
   }
      
   synchronized public static void initInstance() {
      if ( mInstance == null ) {
         mInstance = new TestsConfig();
      }
   }
   
   public static void delay(long ms) {
      try {
         Thread.sleep(ms);
      } catch (InterruptedException ex) { }
   }
   
}
