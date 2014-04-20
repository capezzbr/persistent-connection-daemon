
package com.brunocapezzali;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils
{
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat ("dd/MM 'at' HH:mm:ss");
   
   synchronized public static void log(String name, String message) {
      String date = dateFormat.format(new Date());
      System.out.println("["+ date + "]\t"+ name + ".java\t - "+ message);  
   }
   
	synchronized public static String MD5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuilder hexString = new StringBuilder();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2) {
	                h = "0" + h;
               }
	            hexString.append(h);
	        }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
          return "";
	    }
	}
   
	public static boolean inArray(int[] array, int value){
		for ( int i=0; i<array.length; i++ ) {
			if ( array[i] == value  ) {
				return true;
         }
		}
		return false;
	}
   
   public static String timestampDifferenceNow(long ts) {
      Date d = new Date();
      return Utils.timestampDifference(d.getTime(), ts);
   }
   
   public static String timestampDifference(long ts1, long ts2) {
      long difference = (ts1 - ts2) / 1000; // I timestamp sono in ms e non in sec
      if ( difference <= 0 ) {
         return "0 second";
      }

      String strIntervals[][] = new String[][] {
         new String[] { "year",      "years" },
         new String[] { "month",     "months" },
         new String[] { "week",      "weeks" },
         new String[] { "day",       "days" },
         new String[] { "hour",      "hours" },
         new String[] { "minute",    "minutes" },
         new String[] { "second",    "seconds" },
      };
      
      Long lngIntervals[] = new Long[] {
         (long) 31556926,  // year
         (long) 2628000,   // month
         (long) 604800,    // week
         (long) 86400,
         (long) 3600,
         (long) 60,
         (long) 1
      };
      
      int granularity = 2;
      String retval = "";
      double time;
      for ( int i =0; i<strIntervals.length; i++ ) {
         if ( difference >= lngIntervals[i] ) {
            time = Math.floor( (double)(difference / lngIntervals[i]) );
            difference %= lngIntervals[i];
            retval += (retval.length() > 0 ? " " : "") + (int)time +" ";
            retval += ((time > 1) ? strIntervals[i][1] : strIntervals[i][0]);
            granularity--;
            
            if ( difference <= 0 ) {
               break;
            }
            
            if (granularity == 1) {
               retval += " and";
            } else if (granularity == 0) {
               break;
            }
         }
      }
      return retval;
   }
}