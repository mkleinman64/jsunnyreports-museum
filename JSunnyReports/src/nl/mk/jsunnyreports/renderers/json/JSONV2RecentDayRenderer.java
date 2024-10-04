package nl.mk.jsunnyreports.renderers.json;

import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.TimeEntry;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.dataobjects.invertersumdata.DataItem;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * JSONV2RecendDayRenderer.java
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2RecentDayRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2RecentDayRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2RecentDayRenderer.class);

    private static final int MAX_DAYS = 15;

    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    public void createJSON() {
         long programStart = System.currentTimeMillis();
         JSONTemplate json = new JSONTemplate();        
         
         Year currentyear = inverterData.getLatestYear();
         Month currentmonth = currentyear.getLatestMonthWithData();
         Day currentday = currentmonth.getLatestDayWithData();
         
         Calendar cal = Calendar.getInstance( Constants.getLocalTimeZone() );
         cal.set( Calendar.HOUR_OF_DAY, 0 );
         cal.set( Calendar.MINUTE, 0 );
         cal.set( Calendar.SECOND, 0 );
         cal.set( Calendar.YEAR, currentyear.getYear());
         cal.set( Calendar.MONTH, currentmonth.getMonth() - 1 );
         cal.set( Calendar.DAY_OF_MONTH, currentday.getDay());

         json.append("{");
         json.append("\"recentdays\":");
         json.append("[");
         
         for ( int i=0; i < MAX_DAYS; i++ ) {
             Year workYear;
             Month workMonth;
             Day workDay = null;
             try {
                 workYear = inverterData.getYear( cal.get( Calendar.YEAR ));
                 workMonth = workYear.getMonth( cal.get( Calendar.MONTH  ) + 1 );
                 workDay = workMonth.getDay( cal.get( Calendar.DAY_OF_MONTH ) );
                 
             } catch ( Exception e ) {
             
             }             
             
             json.append( "{" );
             
             String date = "00-00-0000";
             float kWh = 0f;
             float peakPower = 0f;
             
             if ( workDay != null ) {
                 date = workDay.getDay() + "-" + workDay.getParentMonth().getMonth() + "-" + workDay.getParentMonth().getParentYear().getYear();
                 kWh = workDay.getkWh();
                 peakPower = workDay.getPeakpower();
             }
             
             json.append( "\"date\":\"" + date + "\"" );
             json.append( ",");
             
             json.append( "\"kwh\":" + kWh );
             json.append( ",");
         
             json.append( "\"peakpower\":" + peakPower );
         
             json.append( "}" );

             cal.add( Calendar.DAY_OF_MONTH, -1 );   

             if ( i < MAX_DAYS - 1 ) {
                 json.append( "," );
             }
             
         }    
         
         
         

         json.append("]"); // end of recent days array.
         json.append("}"); // end of file.
         
         
         
         
         numGraphs++;
         json.writeFile("recentdays.json", settings.getOutputLocation() + "/json/");

         long programEnd = System.currentTimeMillis();
         long duration = programEnd - programStart;
         printOutputText(log, "RecentDays", numGraphs, duration);
     }     
}
