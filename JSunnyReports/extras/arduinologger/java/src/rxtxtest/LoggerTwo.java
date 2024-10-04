package rxtxtest;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Calendar;

public class LoggerTwo {

 
    public LoggerTwo(Settings settings) {
       this.settings = settings;
       this.max_watt = settings.getPropertyFloat("logger2.maxwatt");
       this.dirLocation = settings.getProperty("logger2.outputlocation");
       this.PULSE_KWH = settings.getPropertyInt("logger2.pulses");
       if ( PULSE_KWH == 0 ) {
          PULSE_KWH = 2000;
       }

    }
    
  private Calendar lastPulseReceived = null;
  private Settings settings;
  private float max_watt; // maximum yield that can come out of two combined steca's.
  private String dirLocation;
  private static String seperatorChar = ";";
   private static int PULSE_KWH;
   
   public float getWH_PULSE() {
      float r = 0.5f;
      r = (1f / (float)PULSE_KWH);
      return r;                 
   }

    public void doLog(Calendar currentPulse) {
        // first determine what the day should be we will log this pulse in.
        // this is done by checking the date stamp from currentPulse
        NumberFormat tf = new DecimalFormat("00");
        String fileName = currentPulse.get(Calendar.YEAR) + "-" + tf.format( (currentPulse.get(Calendar.MONTH) + 1) ) + "-" + tf.format( currentPulse.get(Calendar.DATE) );
        String dateTime = fileName + " " + currentPulse.get(Calendar.HOUR_OF_DAY) + ":" + tf.format( currentPulse.get(Calendar.MINUTE) ) + ":" + tf.format( currentPulse.get(Calendar.SECOND) );
        float watt = 0.0f;
        boolean insertlog = true;
        
        // now determine if this is the first pulse received for this day. If this is the case we will default the Watt field with 0. ( init ).
        // also when lastPulseReceived = null we will default to 0.
        if ( lastPulseReceived == null || ( currentPulse.get(Calendar.DAY_OF_YEAR) != lastPulseReceived.get(Calendar.DAY_OF_YEAR ) ) ) {
           watt = 0.0f;
        } else {
            // here we have two pulses within the same day. Thus lets do some magic on them :)
            long millisecondsDifference = currentPulse.getTimeInMillis() - lastPulseReceived.getTimeInMillis(); // difference in milliseconds between two pulses.
            
            if ( millisecondsDifference == 0 ) {
                insertlog = false;
            } else {
                float occurenceInAnHour = 3600000f / new Float( millisecondsDifference ).floatValue();  // how much in an hour. ( to get rid of the H in Wh... ).
                watt = getWH_PULSE() * occurenceInAnHour;
                if ( watt > max_watt ) {
                    // more then xW out of the Steca is impossible. 
                    insertlog = false;
                }
            }
            
        }
        
        
        // only write logfile when insertlog = true. ( meaning the pulse ment something.)
        // this hopefully prevents rogue pulses with extremely high yields.
        if ( insertlog ) {
          NumberFormat wf = new DecimalFormat("#0");        
          String toWrite = dateTime + seperatorChar + wf.format( watt );
          System.out.println( "Logger2 : " + toWrite );

          
          // finalize
          lastPulseReceived = currentPulse;

          
          // and write to the destination.
          try {
              // Create file
              FileWriter fstream = new FileWriter(dirLocation + fileName + ".csv", true);
              BufferedWriter out = new BufferedWriter(fstream);
              out.write(toWrite);
              out.newLine();
              out.flush();
              out.close();
          } catch (Exception e) {
              System.err.println("Error: " + e.getMessage());
          }
        }


    }


}
