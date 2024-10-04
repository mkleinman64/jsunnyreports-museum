package nl.mk.jsunnyreports.renderers.json;

import java.awt.Color;

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
import nl.mk.jsunnyreports.dataobjects.invertersumdata.DataItem;
import nl.mk.jsunnyreports.geo.sun.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.renderers.diary.DiaryRenderer;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * JSONV2CommonRenderer.java
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.6.0
 */
public class JSONV2CommonRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2CommonRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2CommonRenderer.class);

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
        
        Year year = inverterData.getLatestYear();
        Month month = year.getLatestMonthWithData();
        Day day = month.getLatestDayWithData();
        
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.YEAR, year.getYear());
        cal.set( Calendar.MONTH, month.getMonth() - 1 );
        cal.set( Calendar.DAY_OF_MONTH, day.getDay());
        
        int year_daynum = cal.get( Calendar.DAY_OF_YEAR );
        
        Calendar yesterday = (Calendar)cal.clone();

        yesterday.add( Calendar.DAY_OF_MONTH,  - 1);        

        Year yesterdayYear;
        Month yesterdayMonth;
        Day yesterdayDay = null;
        try {
            yesterdayYear = inverterData.getYear( yesterday.get( Calendar.YEAR ));
            yesterdayMonth = yesterdayYear.getMonth( yesterday.get( Calendar.MONTH  ) + 1 );
            yesterdayDay = yesterdayMonth.getDay( yesterday.get( Calendar.DAY_OF_MONTH ) );
            
        } catch ( Exception e ) {
            // do nothing. Weird. why is this in here.
        }
        
        String time = "N/A";
        float peakPower = 0f;
        for (DataItem d : day.getMergedData().getDayData()) {
            if ( d.getWattFromMainList() > peakPower ) {
                String minute = "";
                if ( d.getMinute() < 10 ) {
                    minute = "0" + d.getMinute();
                } else {
                    minute = "" + d.getMinute();
                }
                time = d.getHour() + ":" + minute;
                peakPower = d.getWattFromMainList();
            }
        }               
              
        json.append("{");
        json.append("\"common\":");
        json.append("{");
     
        if ( day.getMergedData() != null && day.getMergedData().getDayData().size() > 0 ) {
            json.append("\"now\":" + day.getMergedData().getDayData().get( day.getMergedData().getDayData().size() - 1 ).getWattFromMainList() );
            json.append( "," );
            
        } else {
            json.append("\"now\":" + "0" );
            json.append( "," );
        }
        
        float now2 = 0f;
        for ( ComplexInverter ci: day.getInverters() ) {
            
            if ( ci.getTimeEntries().size() > 0 ) {
                now2 = now2 + ci.getTimeEntries().get( ci.getTimeEntries().size() - 1 ).getWatt();
            }
        }
        
        if ( now2 > peakPower ) {
            peakPower = now2;
        }
        
        json.append("\"now2\":" + now2 );
        json.append( "," );

        json.append("\"peak\":" + peakPower );
        json.append( "," );
        
        json.append("\"peaktime\":" + "\"" + time + "\"" );
        json.append( "," );
        
        float dayYield = 0f;
        for (ComplexInverter i : day.getInverters()) {
            dayYield = dayYield + (i.getkWh());
        }        

        json.append("\"today\":" + dayYield );
        json.append( "," );
        
        if ( yesterdayDay != null ) {
            json.append("\"yesterday\":" + yesterdayDay.getkWh()  );
        } else {
            json.append("\"yesterday\":" + 0.0f );
        }
        json.append( "," );

        // added 2.6.0, average historic yield added.        
        json.append("\"historic_day\": " + inverterData.getAverageHistoricYieldForADay( cal.get( Calendar.MONTH) + 1, cal.get( Calendar.DAY_OF_MONTH ) ) );
        json.append( "," );
        
        json.append("\"month\":" + month.getkWh() );
        json.append( "," );

        float month_expected = inverters.getExpectedMonthYield( cal.get( Calendar.YEAR), cal.get( Calendar.MONTH) + 1 );

        json.append("\"month_expected\":" + month_expected );
        json.append( "," );
        
        // added 2.6.0
        float month_prognosis = 0f;
        month_prognosis = ( month.getkWh() / day.getDay() ) * month.getMaxDay();
        
        json.append("\"month_prognosis\":" + month_prognosis );
        json.append( "," );
        
        // added 2.6.0.
        // prognosis percentage 
        float month_prognosis_percentage = 0f;
        
        if ( month_expected > 0 ) {
            month_prognosis_percentage = month_prognosis / month_expected * 100;
        }
        
        json.append("\"month_prognosis_percentage\":" + month_prognosis_percentage );
        json.append( "," );

        json.append("\"year\":" +  year.getkWh());
        json.append( "," );
        
        float year_expected = inverters.getExpectedYearYield( year.getYear());
        json.append("\"year_expected\":" + year_expected );
        json.append( ",");
        
        // added 2.6.0
        float year_prognosis = 0f;
        
        // three pieces.
        // 1. actual yield from past months
        // 2. the prognosis ( calculated above already )
        // 3. expectation for the coming months.
        
        byte monthNum = month.getMonth(); // this is the current month.
        
        // part one.
        for ( int mo = 1; mo < monthNum; mo++ ) {
            year_prognosis = year_prognosis + year.getMonth( mo ).getkWh();
        }
        
        // part two.
        year_prognosis = year_prognosis + month_prognosis;
        
        // part three.
        for ( int mo = monthNum + 1; mo<=12;mo++ ) {
            year_prognosis = year_prognosis + inverters.getExpectedMonthYield( year.getYear(), mo );
        }
                                            
        json.append("\"year_prognosis\":" + year_prognosis );
        json.append( "," );
        
        // added 2.6.0.
        // prognosis percentage 
        float year_prognosis_percentage = 0f;
        
        if ( year_expected > 0 ) {
            year_prognosis_percentage = year_prognosis / year_expected * 100;
        }
        
        json.append("\"year_prognosis_percentage\":" + year_prognosis_percentage );
        json.append( "," );
        
        json.append("\"inverteryield\":" ) ;
        json.append( "[" );
        
        JSONTemplate dayData = new JSONTemplate();
        for ( ComplexInverter ci: day.getInverters()) {
            if ( ci.getTimeEntries() != null ) {
                
                dayData.append("{");
                dayData.append("\"invertername\":\"" +  ci.getName() + "\"" );
                dayData.append( ",");

                dayData.append("\"today\":" + ci.getkWh() );
                dayData.append( ",");
                
                float watt = 0f;
                if ( ci.getTimeEntries().size() > 0 ) {
                    watt = ci.getTimeEntries().get( ci.getTimeEntries().size() - 1  ).getWatt();
                }
                
                dayData.append("\"now\":" + watt );
                
                dayData.append("}");
                dayData.append( ",");
            }
        }
        
        dayData.removeLastChar();
        json.append( dayData );
        
        json.append( "]" );

        json.append("}"); // end of config object.
        json.append("}"); // end of file.
        
        numGraphs++;
        json.writeFile("common.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "Common", numGraphs, duration);
    }
}
