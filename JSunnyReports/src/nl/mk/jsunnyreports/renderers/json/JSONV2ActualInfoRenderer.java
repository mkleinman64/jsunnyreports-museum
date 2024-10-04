package nl.mk.jsunnyreports.renderers.json;

import java.io.File;

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

public class JSONV2ActualInfoRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {

    public JSONV2ActualInfoRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2ActualInfoRenderer.class);

  
    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }
    
    private void createJSON() {
        long programStart = System.currentTimeMillis();
        JSONTemplate json = new JSONTemplate();

        Year year = inverterData.getLatestYear();
        Month month = year.getLatestMonthWithData();
        Day day = month.getLatestDayWithData();

        float actualW = 0f;
        float dayYield = 0f;
        int wp = 0;
        
        for (ComplexInverter ci : day.getInverters()) {
            dayYield = dayYield + (ci.getkWh());
            
            
            if (ci.hasDetailData()) {
                actualW = actualW + ci.getTimeEntries().get(ci.getTimeEntries().size() - 1).getWatt();
                wp = wp + ci.getWp();
            }            
            
        }
        
        json.append( "{");
        json.append( "\"Actual\":" + actualW );
        json.append( ",");
        json.append( "\"Peak\":" + day.getMergedData().getMergedPeakPower() );
        json.append( ",");
        json.append( "\"Wp\":" + wp );
        json.append( ",");
        json.append( "\"Today\":" + dayYield );
        
        json.append( ",");
        
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
        
        
        json.append( "}");
        
        numGraphs++;
        json.writeFile("actualinfo.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "ActualInfo", numGraphs, duration);
        
    }

}
