package nl.mk.jsunnyreports.renderers.json;

import java.io.File;

import java.io.IOException;

import java.math.BigDecimal;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class JSONBaseRenderer {

   public JSONBaseRenderer(InverterData i, Settings s, InverterList l, Language lang ) {
      inverterData = i;
      this.settings = s;
      inverters = l;
      language = lang;
      
      outputLocation = settings.getOutputLocation() + "/json";

   }

   protected final InverterData inverterData;
   protected final Settings settings;
   protected final InverterList inverters;
   protected final Language language;
   
   protected int numGraphs = 0;

   protected static String outputLocation;

   protected StringBuilder ol;   
   
   
   
    public static float precision( float d, int decimalPlace ) {
        float returnValue;

        try {
            BigDecimal bd = new BigDecimal(Float.toString(d));
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
            returnValue = bd.floatValue();
            
        } catch ( Exception e ) {
            // most probably NaN.
            BigDecimal bd = new BigDecimal(Float.toString(0f));
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
            returnValue = bd.floatValue();
        }
        return returnValue;
      }   


    public static String fixedLengthString(String string, int length) {
        return String.format("%1$"+length+ "s", string);
    }    
    
    public static void printOutputText( Logger log, String renderer, int numGraphs, long duration ) {
        long durationPerGraph = 0;
        if (numGraphs > 0) {
            durationPerGraph = (duration / numGraphs);
        }
        log.info("Created:" + fixedLengthString( renderer, 18 ) + "; amount: " + fixedLengthString( numGraphs + "", 5 ) + " in : " + fixedLengthString( duration + "", 8 ) + "ms. Average: " + fixedLengthString( durationPerGraph + "", 8 ) + "ms." );
    }
  

}
