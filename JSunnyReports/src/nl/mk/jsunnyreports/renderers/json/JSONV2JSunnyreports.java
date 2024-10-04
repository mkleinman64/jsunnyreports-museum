package nl.mk.jsunnyreports.renderers.json;

import java.io.File;

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
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;

public class JSONV2JSunnyreports extends JSONBaseRenderer implements JSONRendererInterface, Runnable {

    public JSONV2JSunnyreports(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2JSunnyreports.class);    

    @Override
    public void run() {
        doMagic();
    }
   
    public void doMagic() {
        generateJSON();
    }
    
    private void generateJSON() {
        long programStart = System.currentTimeMillis();
        JSONTemplate version = new JSONTemplate();
        
        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
        String minute = "" + cal.get(Calendar.MINUTE);
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String lastUpload = cal.get(Calendar.DAY_OF_MONTH) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + minute;

        Year year = inverterData.getLatestYear();
        Month month = year.getLatestMonthWithData();
        Day day = month.getLatestDayWithData();
       
        version.append( "{");
        version.append( "\"jsunnyreports_core_version\": \"" + Constants.getVersionFull() + "\"" );
        version.append( ",");
        version.append( "\"jsunnyreports_last_update\": \"" + lastUpload + "\"" );
        version.append( ",");
        version.append( "\"last_active_year\": \"" + year.getYear() + "\"" );
        version.append( ",");
        version.append( "\"last_active_month\": \"" + month.getMonth() + "\"" );
        version.append( ",");
        version.append( "\"last_active_day\": \"" + day.getDay() + "\"" );        
        version.append( "}");
        
        version.writeFile( "jsunnyreports.json", outputLocation );
        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        numGraphs++;
        printOutputText(log, "jSunnyreports", numGraphs, duration);        
        
    }

}
