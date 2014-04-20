
package com.brunocapezzali;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {
   
   private static final long hour = 1000 * 60 * 60;
   private static final long day = hour * 24;
   private static final long month = day * 32;
   private static final long year = month * 12;
   
   public UtilsTest() {
   }
   
   /**
    * Test of MD5 method, of class Utils.
    */
   @Test
   public void MD5Check() {
      System.out.println("* Utils JUnit4Test: MD5Check()");
      assertEquals("d41d8cd98f00b204e9800998ecf8427e", Utils.MD5(""));
      assertEquals("0cc175b9c0f1b6a831c399e269772661", Utils.MD5("a"));
      assertEquals("098f6bcd4621d373cade4e832627b4f6", Utils.MD5("test"));
      assertEquals("20988daf62bb1edb2694ee997ec349d9", Utils.MD5("1@#[d"));
   }

   /**
    * Test of inArray method, of class Utils.
    */
   @Test
   public void inArrayCheck() {
      System.out.println("* Utils JUnit4Test: inArrayCheck()");
      assertEquals(false, Utils.inArray(new int[] {}, 0));
      assertEquals(false, Utils.inArray(new int[] {}, -1));
      assertEquals(false, Utils.inArray(new int[] {0}, -1));
      assertEquals(true, Utils.inArray(new int[] {0, 1}, 1));
      assertEquals(true, Utils.inArray(new int[] {0, 1, -2, 3}, -2));
   }

   /**
    * Test of timestampDifferenceNow method, of class Utils.
    */
   @Test
   public void timestampDifferenceNowCheck() {
      System.out.println("* Utils JUnit4Test: timestampDifferenceNowCheck()");
      
      assertEquals("1 hour", Utils.timestampDifferenceNow(
              System.currentTimeMillis() - hour));
      
      assertEquals("3 hours", Utils.timestampDifferenceNow(
              System.currentTimeMillis() - hour * 3));
      
      assertEquals("1 day", Utils.timestampDifferenceNow(
              System.currentTimeMillis() - day));
      
      assertEquals("1 week", Utils.timestampDifferenceNow(
              System.currentTimeMillis() - day * 7));
      
      assertTrue(Utils.timestampDifferenceNow(
              System.currentTimeMillis() - month).startsWith("1 month"));
      
      assertTrue(Utils.timestampDifferenceNow(
              System.currentTimeMillis() - year).startsWith("1 year"));
   }

   /**
    * Test of timestampDifference method, of class Utils.
    */
   @Test
   public void timestampDifferenceCheck() {
      System.out.println("* Utils JUnit4Test: timestampDifferenceCheck()");
      
      long now = System.currentTimeMillis();
      assertEquals("0 second", Utils.timestampDifference(-1, -1));
      assertEquals("0 second", Utils.timestampDifference(0, 0));
      assertEquals("3 hours", Utils.timestampDifference(now, now - hour * 3));
      assertEquals("5 days", Utils.timestampDifference(now, now - day * 5));
      assertTrue(Utils.timestampDifference(now, now - month*3).startsWith("3 months"));
      assertTrue(Utils.timestampDifference(now, now - year*2).startsWith("2 years"));
   }
   
}
