package com.brunocapezzali;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author capezzbr
 */
public class CommandTest {
   
   /**
    * Test of getJSONCommand method, of class Command.
    */
   @Test
   public void getJSONCommandCheck() {
      System.out.println("* Command JUnit4Test: getJSONCommandCheck()");

      Command instance = new Command(null, "paramId", "paramCmd");
      JSONObject result = instance.getJSONCommand();
      assertTrue(result.has("id"));
      assertTrue(result.has("cmd"));
      assertTrue(result.getString("id").equals("paramId"));
      assertTrue(result.getString("cmd").equals("paramCmd"));
   }
   
}
