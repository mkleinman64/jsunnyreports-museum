package nl.mk.jsunnyreports.renderers.json;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.SimpleInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.dataobjects.invertersumdata.DataItem;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * JSONV2MonthRenderer.java
 * 
 * note: Known issue: peakpower is derived from merged table, which is removed from the cache, peakpower needs to be added to cache.
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.8
 */
public class JSONV2DaysYearRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2DaysYearRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language);
    }

    private static final Logger log = Logger.getLogger(JSONV2DaysYearRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    public void createJSON() {
        long programStart = System.currentTimeMillis();


        for (Year year : inverterData.getYears()) {
            if ( year.isCacheModified() ) {
                JSONTemplate json = new JSONTemplate();
                json.append("{");
                json.append("\"year\": " + year.getYear() + ",");
                json.append("\"daydata\":" );
                json.append( "[");

                for (Month month : year.getMonths()) {
                    for ( int daynum=1;daynum<=month.getMaxDay();daynum++) {
                        if ( month.getDay(daynum) != null  ) {
                            json.append( month.getDay(daynum).getkWh() );
                        } else {
                            json.append( "0.000");
                        }
                        
                        if ( month.getMonth() < 12 ) {
                            json.append( "," );
                        } else {
                            if ( daynum < 31 ) {
                                json.append( "," );
                            }
                        }
                    }
                     
                    
                }    
                json.append( "]" );
                json.append( "}" );

                json.writeFile( year.getYear() + "-daydata.json", outputLocation + "/" + year.getYear() );
                numGraphs++;                

            }
            
        }




        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;

        printOutputText(log, "Year Daydata", numGraphs, duration);
    }
}
