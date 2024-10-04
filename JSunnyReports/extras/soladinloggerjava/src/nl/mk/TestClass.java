package nl.mk;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Calendar;

import org.apache.commons.lang3.Conversion;

public class TestClass {
   public TestClass() {
      super();
   }
   
   public static byte[] hexStringToByteArray(String s) {
       int len = s.length();
       byte[] data = new byte[len / 2];
       for (int i = 0; i < len; i += 2) {
           data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i+1), 16));
       }
       return data;
   }  

   public static void main(String[] args) {
/*
      'Data ontvangen is:
      '00 00 11 00 B6 F3 : iets van een header met daarin het commando B6 en de 11 00
      '00 00 : ?
      'X C5 03 : 0x03C5 hex = 965 decimaal = 96.5 Volt spanning van panelen
      'X 59 00 : 0x0059 hex = 89 decimall = 0.89 Amp stroom uit panelen
      'X 8A 13 : 0x138A hex = 5002 decimaal = 50.02 Hz 
      'X DF 00 : 0x00DF hex = 223 decimaal = 223 Volt
      '00 00 
      'X 4C 00 : 0x004C hex = 76 decimaal = 76 Watt vermogen
      '3A 10 : 0x103A hex = 4154 decimaal = 41.54 Kwh totaal al geleverd
      '00 2E : 0x002E hex = 46 decimaal = 46 graden (temeratuur)
      '9F 5D 00 00 00 00 : 0x5D9F hex = 23967 decimaal = aantal minuten werkzaam = 399:27 Uur (nu dus 16dagen)
      '17 (checksum)
      ' 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30
      '00 00 11 00 B6 F3 00 00 C5 03 59 00 8A 13 DF 00 00 00 4C 00 3A 10 00 2E 9F 5D 00 00 00 00 17
*/
      //                                         Vpv    Ipv    HzNet   VNet          Pac   
      //String responseHex = "00001100B6F3 00 00 2E 03  13 00  8A 13   E0 00  00 00  0C 00 EF 4B 01 2B 0F 15 0B 00 00 00 1C";
      String responseHex = "00001100B6F300002E0313008A13E00000000C00EF4B012B0F150B0000001C";

      //String responseHex = "00001100B6F300002E0313008A13E00000000C00EF4B012B0F150B0000001C";
      //                      01234567890123456789012345678901234567890123456789012345678901
      //                      0         1         2         3         4         5         6 

      String pAC = responseHex.substring( 38 , 40) + responseHex.substring( 36 , 38);
      String vNet = responseHex.substring( 30 , 32) + responseHex.substring( 28 , 30);

      Calendar c = Calendar.getInstance();
      NumberFormat tf = new DecimalFormat("00");
      
      //;14-2-2014 8:16:04;54,1;0;49,97;224;0;193,26;14;724752
     
      
      String fileName = tf.format( c.get(Calendar.DATE) ) + "-" + tf.format( (c.get(Calendar.MONTH) + 1) ) + "-" + c.get(Calendar.YEAR);
      String dateTime = fileName + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + tf.format( c.get(Calendar.MINUTE) ) + ":" + tf.format( c.get(Calendar.SECOND) );
      
      
      short pACvalue = Short.parseShort( pAC, 16);
      short VNetvalue = Short.parseShort( vNet, 16);

      //System.out.println( "dateTime : " + dateTime );

      //System.out.println( "pACHex : " + pAC + " value: " + pACvalue );
      //System.out.println( "VNet : " + vNet + " value: " + VNetvalue );

      String finalOutput = ";" + dateTime + ";0;0;0;" + VNetvalue + ";" + pACvalue + ";0;0;0"; 
      
      System.out.println( "Writing String: " + finalOutput);

      String dirLocation = "c:\\temp\\";

       // and write to the destination.
       try {
           // Create file
           FileWriter fstream = new FileWriter(dirLocation + fileName + ".csv", true);
           BufferedWriter out = new BufferedWriter(fstream);
           out.write(finalOutput);
           out.newLine();
           out.flush();
           out.close();
       } catch (Exception e) {
           System.err.println("Error: " + e.getMessage());
       }
      

   }
}
