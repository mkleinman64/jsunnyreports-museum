package nl.mk;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;

import java.io.OutputStream;

import java.util.Enumeration;
import java.util.Timer;

public class JSoladinLog {
   
   
   private Settings settings = new Settings();

   private InputStream input;
   private OutputStream output;

   private final int TIME_OUT = settings.getPropertyInt( "comport.timeout" );
   private final int DATA_RATE = settings.getPropertyInt( "comport.datarate" );
   private final String portName = settings.getProperty( "comport.name" );
   private final long firstDelay = settings.getPropertyLong( "logger.initdelay" );
   private final long normalInterval = settings.getPropertyLong( "logger.delay" );
   

   private SerialPort serialPort;
   
   public boolean init() {
      boolean initSuccess = true;
      CommPortIdentifier portId = null;
      Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

      // iterate through, looking for the port
      while (portEnum.hasMoreElements()) {
         CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(portName.toUpperCase())) {
               portId = currPortId;
               break;
            }
      }

      if (portId == null) {
         System.out.println("Could not find COM port with name : " + portName);
         initSuccess = false; 
      } else {
         try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                  TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                  SerialPort.DATABITS_8,
                  SerialPort.STOPBITS_1,
                  SerialPort.PARITY_NONE);

            // set RTS HIGH, necessary for Soladin operation.
            serialPort.setRTS(true);

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            ReceiveData r = new ReceiveData( input, settings );

            // add event listeners
            serialPort.addEventListener(r);
            serialPort.notifyOnDataAvailable(true);
         } catch (Exception e) {
            System.err.println(e.toString());
            initSuccess = false;
         }
         
      }
      return initSuccess;      
   }
   
   
   public void processor() {
      Timer timer = new Timer( "SoladinTimer", false);        
      System.out.println("jSoladinLog wordt gestart op poort: " + portName );
      
      if ( this.init() ) {
         // Schedule een nieuwe taak in de timer
         timer.scheduleAtFixedRate(new SendCommand(serialPort, output ), firstDelay, normalInterval);

      }
      
   }

   public static void main(String[] args) {
      JSoladinLog jSoladinLog = new JSoladinLog();
      jSoladinLog.processor();
   
   }
}
