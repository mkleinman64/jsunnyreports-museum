package nl.mk.jsunnyreports.renderers.javascript;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;

import java.util.List;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.SimpleInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;

import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

/**
 * SonnenertragJSRenderer.java, creates all the necessary .js files needed for the website sonnenertrag.eu
 * It generates them on a generic basis and an baseInverter specific baseInverter.
 *
 * @author Martin Kleinman
 * @since 1.3.0.1alpha
 * @version 1.3.0.1alpha
 */

public class SonnenertragJSRenderer {

   public SonnenertragJSRenderer(InverterData inverterData, InverterList inverters, Settings settings) {
      this.inverterData = inverterData;
      this.settings = settings;
      this.inverters = inverters;

      outputLocation = settings.getOutputLocation();
   }

   private static final Logger log = Logger.getLogger(SonnenertragJSRenderer.class);

   private InverterData inverterData;
   private Settings settings;
   private InverterList inverters;
   private String outputLocation;

   public void doMagic() {
      if ( inverterData.isUpdated() ) {
        this.createBaseVarJSFile();
        this.createDaysHistJSFile();
        this.createMonthJSFile();

        // and the inverter specific files
        this.createInverterBaseVarJSFiles();
        this.createInverterDaysHistJSFiles();
        this.createInverterMonthJSFiles();
        
      }
   }

   /* First the js files on a total basis, inverter independent */

   public void createBaseVarJSFile() {
      try {

         FileWriter basevarsJS = new FileWriter(outputLocation + "/base_vars.js");
         BufferedWriter basevarsJsWriter = new BufferedWriter(basevarsJS);

         String base_vars;
         base_vars = "inverterInfo[0]=new Array(\"All\",\"???\"," + inverterData.getLatestYear().getInstalledWp() + ",0,\"All\",1,null,null,0,null,1,0,0,1000,null)";
         basevarsJsWriter.write(base_vars);
         basevarsJsWriter.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }


   /**
    * Creates days_hist.js, sort is newest day first, oldest day last. Yield is in Wh instead of kWh ( to avoid the . or , in the outputcontent
    * day/month and year must be in a two digit format. e.g. 01.01.09 for 01-01-2009. Also the seperator is not a minus, -, but a dot, ..
    * Also the current day is NOT represented in the file.
    * In the current method this is done by omitting the last entry for the day, assumming this is the current
    *
    */
   public void createDaysHistJSFile() {
      try {
         DecimalFormat dfDayMonth = new DecimalFormat("00");

         FileWriter daysJs = new FileWriter(outputLocation + "/days_hist.js");
         BufferedWriter daysJsWriter = new BufferedWriter(daysJs);

         List<Year> years = inverterData.getYears();
            for (int y = years.size() - 1; y >= 0; y--) {
               Year year = years.get(y);

               Month[] months = year.getMonths();

               for (int m = months.length - 1; m >= 0; m--) {
                  Month month = months[m];

                  Day[] days = month.getDays();

                  // ignore the last entry
                  for (int d = days.length - 1; d >= 0; d--) {
                     Day day = days[d];

                     Float kwhJSYield = day.getkWh() * 1000;
                     int kwhJSYieldFinal = kwhJSYield.intValue();
                     String yearJS = Integer.toString(year.getYear()).substring(2, 4);
                     String dayString = dfDayMonth.format(day.getDay());
                     String monthString = dfDayMonth.format(month.getMonth());

                     daysJsWriter.write("da[dx++]=\"" + dayString + "." + monthString + "." + yearJS + "|" + kwhJSYieldFinal + ";1000\"\n");
                  }
               }
            }
            daysJsWriter.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }


   /**
    * This method generates months.js, sort is descending, the newest month first.
    */
   public void createMonthJSFile() {
      try {

         FileWriter monthJs = new FileWriter(outputLocation + "/months.js");
         BufferedWriter monthJsWriter = new BufferedWriter(monthJs);

         List<Year> years = inverterData.getYears();
            for (int y = years.size() - 1; y >= 0; y--) {
               Year year = years.get(y);

               Month[] months = year.getMonths();

               for (int m = months.length - 1; m >= 0; m--) {

                  Month month = months[m];

                  long kwhJSYieldFinal = month.getWh();
                  String yearJS = Integer.toString(year.getYear()).substring(2, 4);
                  if ( month.hasDaysWithData() ) {
                     monthJsWriter.write("mo[mx++]=\"" + month.getLatestDayWithData().getDay() + "." + month.getMonth() + "." + yearJS + "|" + kwhJSYieldFinal + "\"\n");
                  }
               }
            }
            monthJsWriter.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void createInverterBaseVarJSFiles() {
      try {
         for (BaseInverter i : inverters.getInverters()) {
            /*
             * inverterInfo[inverter Index]=new Array("inverter nickname","???",connected AC capacity,0,"inverter complete name",number of Strings,null,null,0,null,1,0,0,1000,null)
             * example
             * inverterInfo[0]=new Array("SMA SB 2100","???",4711,0,"SMA Sunny Boy 2100TL",1,null,null,0,null,1,0,0,1000,null)
             */

            FileWriter basevarsJS = new FileWriter(outputLocation + "/" + i.getM_InverterName() + "/base_vars.js");
            BufferedWriter basevarsJsWriter = new BufferedWriter(basevarsJS);

            String base_vars;
            base_vars = "inverterInfo[0]=new Array(\"" + i.getM_InverterName() + "\",\"???\"," + i.getM_WattPeak() + ",0,\"" + i.getM_InverterName() + "\",1,null,null,0,null,1,0,0,1000,null)";
            basevarsJsWriter.write(base_vars);
            basevarsJsWriter.close();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
     * Creates days_hist.js per baseInverter, sort is newest day first, oldest day last. Yield is in Wh instead of kWh ( to avoid the . or , in the outputcontent
     * day/month and year must be in a two digit format. e.g. 01.01.09 for 01-01-2009. Also the seperator is not a minus, -, but a dot, ..
     * Also the current day is NOT represented in the file.
     * In the current method this is done by omitting the last entry for the day, asumming this is the current
     *
     */
   public void createInverterDaysHistJSFiles() {
      try {
         DecimalFormat dfDayMonth = new DecimalFormat("00");

         for (BaseInverter i : inverters.getInverters()) {
            FileWriter daysJs = new FileWriter(outputLocation + "/" + i.getM_InverterName() + "/days_hist.js");
            BufferedWriter daysJsWriter = new BufferedWriter(daysJs);

            List<Year> years = inverterData.getYears();
               for (int y = years.size() - 1; y >= 0; y--) {
                  Year year = years.get(y);

                  Month[] months = year.getMonths();

                  for (int m = months.length - 1; m >= 0; m--) {
                     Month month = months[m];

                     Day[] days = month.getDays();

                     // ignore the last entry
                     for (int d = days.length - 1; d >= 0; d--) {
                        Day day = days[d];
                         
                        if ( day.hasInverters() ) {
                            
                            ComplexInverter invD = day.getInverter(i.getM_InverterName());
                            if (invD != null) {
                               Float kwhJSYield = invD.getkWh() * 1000;
                               int kwhJSYieldFinal = kwhJSYield.intValue();
                               String yearJS = Integer.toString(year.getYear()).substring(2, 4);
                               String dayString = dfDayMonth.format(day.getDay());
                               String monthString = dfDayMonth.format(month.getMonth());

                               daysJsWriter.write("da[dx++]=\"" + dayString + "." + monthString + "." + yearJS + "|" + kwhJSYieldFinal + ";1000\"\n");
                            }
                        }

                     }
                  }
               }
            daysJsWriter.close();
         }

      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * This method generates months.js, sort is descending, the newest month first.
    */
   public void createInverterMonthJSFiles() {
      try {

         for (BaseInverter i : inverters.getInverters()) {
            FileWriter monthJs = new FileWriter(outputLocation + "/" + i.getM_InverterName() + "/months.js");
            BufferedWriter monthJsWriter = new BufferedWriter(monthJs);
            List<Year> years = inverterData.getYears();

               for (int y = years.size() - 1; y >= 0; y--) {
                  Year year = years.get(y);
                  Month[] months = year.getMonths();

                  for (int m = months.length - 1; m >= 0; m--) {

                     Month month = months[m];

                     SimpleInverter invB = month.getInverter(i.getM_InverterName());
                     if (invB != null) {
                        long kwhJSYieldFinal = invB.getWh();
                        String yearJS = Integer.toString(year.getYear()).substring(2, 4);
                        if (month.hasDaysWithData() ) {
                           monthJsWriter.write("mo[mx++]=\"" + month.getLatestDayWithData().getDay() + "." + month.getMonth() + "." + yearJS + "|" + kwhJSYieldFinal + "\"\n");
                        }

                     }

                  }
               }
            monthJsWriter.close();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

}
