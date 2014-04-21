package com.brunocapezzali;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author capezzbr
 */
public class TestClientSimulator {
   private final Socket mSock;
   private final BufferedReader mReader;
   private final PrintWriter mWriter;
   
   public TestClientSimulator() throws IOException {
      mSock = new Socket("127.0.0.1", Config.kDaemonPort);
      mSock.setSoTimeout(Config.kDaemonSockTimeout); /* Non blocking socket*/
      mReader = new BufferedReader(
              new InputStreamReader(mSock.getInputStream(), "UTF-8"));
      mWriter = new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(mSock.getOutputStream(), "UTF-8")), true);
   }
   
   public Socket getSocket() {
      return mSock;
   }
   
   public String readln() throws IOException {
      synchronized(mReader) {
         return mReader.readLine();
      }
   }
   
   public void writeln(String str) {
      synchronized(mWriter) {
         mWriter.write(str + "\n");
         mWriter.flush();
      }
   }
}
