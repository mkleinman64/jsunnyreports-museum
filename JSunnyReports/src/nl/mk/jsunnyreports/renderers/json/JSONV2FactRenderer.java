package nl.mk.jsunnyreports.renderers.json;


import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.geo.sun.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.renderers.diary.DiaryRenderer;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * Date         Version     Who     What
 *
 * @author Martin Kleinman
 * @since 2.5.0
 * @version 2.0.0.0
 */
public class JSONV2FactRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2FactRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2FactRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    /**
     */
    public void createJSON() {
        long programStart = System.currentTimeMillis();
        JSONTemplate json = new JSONTemplate();        
        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
       
        String sunrise = "";
        String sunset  = "";
        if (settings.getGpsLocation().isValidValue()) {
            SunriseSunsetCalculator sunCalc = new SunriseSunsetCalculator(settings.getGpsLocation().getLocation(), Constants.getLocalTimeZone());
            sunrise = sunCalc.getOfficialSunriseForDate(cal);
            sunset  = sunCalc.getOfficialSunsetForDate(cal);
        }           
        
        float totalYield = 0f;
        float totalSavings = 0f;
        for (Year y : inverterData.getYears()) {
            totalYield = totalYield + (y.getkWh());
            totalSavings = totalSavings + ( y.getSavings() );
        }        
        
        float gUraniumSaved = 0f;
        gUraniumSaved = totalYield * (1000f / settings.getKWhOutOfU235()); // 1000 grams / 50.000kWh.

        float co2Value = settings.getCo2kWh();
        float kgCO2Saved = 0f;
        kgCO2Saved = totalYield * co2Value;
        
        Year year = inverterData.getLatestYear();
        Month month = year.getLatestMonthWithData();
        Day day = month.getLatestDayWithData();
        
        Year lastYear = inverterData.getYear( year.getYear() - 1 );
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set( Calendar.HOUR_OF_DAY, 0 );
        cal2.set( Calendar.MINUTE, 0 );
        cal2.set( Calendar.SECOND, 0 );
        cal2.set( Calendar.YEAR, year.getYear());
        cal2.set( Calendar.MONTH, month.getMonth() - 1 );
        cal2.set( Calendar.DAY_OF_MONTH, day.getDay());



        cal2.add( Calendar.MONTH,  - 1);              
        
        Year lastYearMonth;
        Month lastMonth = null;
        try {
            lastYearMonth = inverterData.getYear( cal2.get( Calendar.YEAR ));
            lastMonth = lastYearMonth.getMonth( cal2.get( Calendar.MONTH  ) + 1 );
            
        } catch ( Exception e ) {
        
        }        

        float dayYield = 0f;
        for (ComplexInverter i : day.getInverters()) {
            dayYield = dayYield + (i.getkWh());
        }
        float historicDayYield = inverterData.getAverageHistoricYieldForADay(cal);        
        
        json.append("{");
        json.append("\"facts\":");
        json.append("{");
        
     
        
        json.append("\"sunrise\":" + "\"" + sunrise + "\"" );
        json.append( "," );
        
        json.append("\"sunset\":" + "\"" + sunset + "\"" );
        json.append(",");
            
        json.append("\"today\":" + dayYield );
        json.append(",");
                  
        json.append("\"historic\":" + historicDayYield );
        json.append(",");

        json.append("\"thismonth\":" + month.getkWh() );
        json.append(",");
        
        json.append("\"totalsavings\":" + totalSavings );
        json.append(",");


        float lastMonthValue = 0f;
        if ( lastMonth != null ) {
            lastMonthValue = lastMonth.getkWh();
        }

        json.append("\"lastmonth\":" + lastMonthValue );
        json.append(",");
        
        json.append("\"thisyear\":" + year.getkWh() );
        json.append(",");
        
        float lastYearValue = 0f;
        if ( lastYear != null ) {
            lastYearValue = lastYear.getkWh();
        }
        
        json.append("\"lastyear\":" + lastYearValue );
        json.append(",");

        
        json.append("\"total\":" + totalYield );
        json.append(",");

        json.append("\"uranium\":" + gUraniumSaved );
        json.append(",");
        
        json.append("\"co2\":" + kgCO2Saved );
        json.append(",");

        json.append("\"inverters\":" );
        json.append("[");
        
        boolean first = true;
        for (BaseInverter bi : inverters.getInverters()) {
            
            if ( first == false ) {
                json.append(",");
              
            } else {
                first = false;
                
            }
            
            json.append( "{");
            
            json.append("\"name\":" + "\"" +  bi.getM_InverterName() + "\"" );
            json.append(",");

            json.append("\"wp\":" +  bi.getM_WattPeak() );
            json.append(",");
            
            float inverterYield = 0f;
            for (Year y : inverterData.getYears()) {
                
                if ( y.getInverter( bi.getM_InverterName() ) != null ) {
                    inverterYield = inverterYield + y.getInverter(bi.getM_InverterName()).getkWh();
                }
            }
            json.append("\"kwh\":" + inverterYield );
            json.append(",");

            float savings = 0f;
            for (Year y : inverterData.getYears()) {
                
                if ( y.getInverter( bi.getM_InverterName() ) != null ) {
                    savings = savings + y.getSavingsInverter(bi.getM_InverterName());
                }
            }
            json.append("\"savings\":" + savings );
            
            
            

           
            json.append( "}");
        }        
        
        json.append("]");
        
        json.append("}");
        json.append("}");        
        
        numGraphs++;
        json.writeFile("facts.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "Facts", numGraphs, duration);
    }
}
