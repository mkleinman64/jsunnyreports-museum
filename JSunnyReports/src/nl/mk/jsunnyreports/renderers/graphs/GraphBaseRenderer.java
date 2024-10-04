package nl.mk.jsunnyreports.renderers.graphs;

import java.io.File;

import java.io.IOException;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class GraphBaseRenderer {

   public GraphBaseRenderer(InverterData i, Settings s, InverterList l, Language lang) {
      inverterData = i;
      this.settings = s;
      inverters = l;
      language = lang;

      outputLocation = settings.getOutputLocation();
  
      // text, language
      graphs_kwh = language.getText("caption.kwh");

   }

   protected int numGraphs = 0;

   protected final InverterData inverterData;
   protected final Settings settings;
   protected final InverterList inverters;
   protected final Language language;

   protected static String outputLocation;

   // language
   protected static String graphs_kwh;


   protected int thumb_width;
   protected int thumb_height;
   
   protected int full_width = 900;
   protected int full_height = 500;
   
   protected StringBuilder ol;   
   


   
      
    public void saveSignatureGraph(String ol, JFreeChart chart  ) {
       try {
          ChartUtilities.saveChartAsPNG(new File(ol), chart, 400, 150, null, true, 9);
       } catch (IOException e) {
          System.out.println( "Error creating :" + ol );
          e.printStackTrace();
       } 
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
