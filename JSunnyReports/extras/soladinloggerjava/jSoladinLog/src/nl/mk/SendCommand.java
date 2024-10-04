package nl.mk;

import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Calendar;
import java.util.TimerTask;

import javax.xml.bind.DatatypeConverter;

public class SendCommand extends TimerTask {
    
    private SerialPort serialport;
    private OutputStream outputStream;
    
    // B6SoladinTX As Byte() = {&H11, &H0, &H0, &H0, &HB6, &H0, &H0, &H0, &HC7}
    private String b6CommandTx = "11000000B6000000C7"; 
    
    /** Init van SendCommand, wat wil ik doorgeven **/
    public SendCommand(SerialPort s, OutputStream o) {
      this.serialport = s;
      this.outputStream = o;
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

    @Override
    public void run() {
       Calendar c = Calendar.getInstance();
       NumberFormat tf = new DecimalFormat("00");
       
       //;14-2-2014 8:16:04;54,1;0;49,97;224;0;193,26;14;724752
       
       
       String fileName = c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR);
       String dateTime = fileName + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + tf.format( c.get(Calendar.MINUTE) ) + ":" + tf.format( c.get(Calendar.SECOND) );
       
       
        System.out.println( dateTime + " : versturen B6 commando naar Soladin");
        
        try { 
           
           outputStream.write( hexStringToByteArray(b6CommandTx) );
        } catch ( IOException e ) {
           e.printStackTrace();
        }
        
        
        
    }
}
