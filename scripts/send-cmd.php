<?php

   /* Connection confiuration */
   define(kDaemonAddress,     "127.0.0.1");
   define(kDaemonPort,        5555);
   
   /* Get command parameter from URI */
   $sDeviceIdentifier = filter_input(INPUT_GET, 'identifier', FILTER_SANITIZE_STRING);
   if ( strlen($sDeviceIdentifier) < 4) {
      die("ERROR: Wrong 'identifier' parameter<br/>");
   }
   
   $sCmd = filter_input(INPUT_GET, 'cmd', FILTER_SANITIZE_STRING);
   if ( strlen($sCmd) < 2 ) {
      die("ERROR: Wrong 'cmd' parameter<br/>");
   }

   /* Connecting to daemon */
   echo("Connecting to ". kDaemonAddress .":". kDaemonPort ."... ");
   $sockDaemon = @fsockopen(kDaemonAddress, kDaemonPort, $errno, $errstr);
   if ( !$sockDaemon ) {
      die("ERROR: $errstr ($errno)<br/>");
   }
   echo("OK.<br/>");

   /* Generate command JSON and sendo to daemon */
   $aCommandJson = array( 
      "di"  => $sDeviceIdentifier,
      "cmd" => $sCmd
   );
   echo("Sending command to daemon... ");
   if ( fwrite($sockDaemon, json_encode($aCommandJson) ."\r\n") === FALSE ) {
      die("Error while sending command to device.<br/>");
   }
   echo("OK.<br/>");

   /* Read the daemon reply (tunnel of the device's reply) */
   echo("Waiting for daemon reply... ");
   $sCmdReply = "";
   while ( (!feof($sockDaemon)) ) { 
      $sCmdReply .= fread($sockDaemon, 4096); 
   }
   echo("OK.<br/>-> $sCmdReply");
      
   fclose($sockDaemon);
   
?>