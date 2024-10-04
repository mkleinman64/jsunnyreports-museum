------------------------------------------------------------------------------------------------------------------------------------
software: Arduinologger version 1.0.0.0
date: 21-01-2012
------------------------------------------------------------------------------------------------------------------------------------

Contents:

- What
- Intended audience
- Directory contents
- Usage
- Java code
- Credits

::WHAT::
Arduinologger is a simple java program which is used in conjuction with a small Arduino sketch and an Arduino board to monitor both interupt lines from the Arduino. The Arduino ( UNO ) is equiped with 
two hardware pins which are attached to a hardware interrupt on the microcontroller. A small software sketch detects these interrupts and relays these to the computer attached to the Arduino.

This small software program listens to the Arduino and when a signal is detected it will calculate the amount of power ( Watt ) and will write it to a CSV file which can be read by jSunnyreports.

In short the whole setup with the Arduino, the S0 based digital kWhmeter and this program:

Solar panels --> Solar inverter --> Digital kWh meter with S0 output --> Arduino board --> Computer running this java program --> jSunnyreports

This sounds complicated but actually it isn't. Put in layman terms this is what happens:
Your PV plant generates power which is put into your local powergrid. A digital kWh meter registers the output of your powerplant. This kWhmeter has an S0 output which will give a signal for every 0.5Wh
or 1Wh depending on the kWh meter used. 

This signal is registered by your Arduino which yells that signal to your computer. Your computer will detect this signal, will calculate the time between this signal and the previous one and will then calculate
the amount of Watt generated, and it will save this to a CSV file. :)

In this case it will yell an "S" when the Steca has registered an S0 sinal. And a "G" when my Gridfit has registered an S0 signal

::INTENDED AUDIENCE::
You will need some programming skills to use this program. The current code is specific for my own situation so you will need to tailor it a bit for your own needs.

::DIRECTORY CONTENTS::
/classes                 Compiled classes ( java 1.5 )
/conf                    Config file. 
/lib                     Libraries
/src                     Sources
readme.txt
runlogger.cmd

/src:
GridfitLogger.java       Logger for my Gridfit 
SerialTest.java          Main program 
Settings.java            For reading the settings
StecaLogger.java         Logger for my Steca

Note: GridfitLogger and StecaLogger are almost identical.

::USAGE::
Contents of runlogger.cmd

java.exe -client -classpath c:\loggers\arduino\classes;c:\loggers\arduino\lib\RXTXcomm.jar rxtxtest.SerialTest COM5
                            1                                                              2                   3
							
1. Classpath where all classes are found. It needs the /classes dir and the /lib dir.
2. rxtxtest.SerialTest is the main program that is started
3. COM5 is the comport on which the Arduino is connected.

::JAVA CODE::
SerialTest.java

The main code ( which you should/could/can modify ) is:

--------------
	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
			    // first get the time the event was received.  
			    Calendar dateReceivedData = Calendar.getInstance();  

			  	int available = input.available();
				  byte chunk[] = new byte[available];
				  input.read(chunk, 0, available);

          String receivedData = new String(chunk);

          // in theory both inverters *COULD* yell at exactly the same time.
          for ( int i = 0; i < receivedData.length(); i++) {
             String inverter = receivedData.substring( i,i+1 );
             
              if ( inverter.equals("S") ) {
                // it is the Steca!
                sl.doLog(dateReceivedData);
              }
              
              if ( inverter.equals("G")) {
                // it is the Gridfit!
                gl.doLog(dateReceivedData);
              }
          }

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
---------------

              if ( inverter.equals("S") ) {
                // it is the Steca!
                sl.doLog(dateReceivedData);
              }

              if ( inverter.equals("G")) {
                // it is the Gridfit!
                gl.doLog(dateReceivedData);
              }
			  
			  
			  This is the main portion that will start the StecaLogger or the GridfitLogger.
			  
StecaLogger.java

The main code here is:

    public void doLog(Calendar currentPulse) {

this method keeps track of the previous signal and the current one, it will determine the time difference between the both of them. Ahd with the amount of pulses for one kWh ( taken from the S0 kWhmeter specs ) 
the code can calculate the exact amount of Watt generated in that period.

::CREDITS::
rxtxtest.java was taken from a open source example I found on the internet, I modified it so it would work in my situation.
	
			  
			  
			  
 