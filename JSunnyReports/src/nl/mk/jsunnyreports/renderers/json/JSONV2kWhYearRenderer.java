package nl.mk.jsunnyreports.renderers.json;


import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.templates.JSONTemplate;
import org.apache.log4j.Logger;


/**
 * Date         Version     Who     What
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2kWhYearRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2kWhYearRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2kWhYearRenderer.class);


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
        json.append("{");
        json.append( "\"series\":");
        json.append( "[");
        
        int yIdx = inverterData.getYears().size() - 1;
        int yC   = 0;
        for ( Year year: inverterData.getYears()) {
            // we are going to "dump" all the years in the same year. thus creating one single graph with a timespan of one year.

            Month maxMonth = year.getLatestMonthWithData();
            Day   maxDay   = maxMonth.getLatestDayWithData();
            
            float totalValue = 0f;
            
            json.append( "{");
            json.append( "\"name\":" + year.getYear() );
            json.append( ",");
            
            json.append( "\"data\":" );
            json.append( "[");
            
                       
            for ( Month month: year.getMonths() ) {
                for ( Day day: month.getDays() ) {
                    
                    
                    if ( day.hasInverters() ) {
                        totalValue = totalValue + day.getkWh();
                        
                        json.append("{");

                        json.append("\"y\":" + year.getYear() );
                        json.append(",");

                        json.append("\"mo\":" + month.getMonth() );
                        json.append(",");

                        json.append("\"d\":" + day.getDay() );
                        json.append(",");

                        json.append("\"h\":" + 0f );
                        json.append(",");

                        json.append("\"mi\":" + 0f );
                        json.append(",");

                        json.append("\"s\":" + 0f );
                        json.append(",");

                        json.append("\"kwhc\":" + totalValue );

                        json.append("}");
                        
                        if ( month.getMonth() == maxMonth.getMonth() && day.getDay() == maxDay.getDay() ) {
                            // do nothing.. hmmm. bad coding.
                        
                        } else {
                            json.append(", ");
                            
                        }

                    }
                }
            }
            json.append( "]");

                        
            json.append( "}");
            if ( yC < yIdx ) {
                json.append( ",");
            }
            yC++;
            
            
        }
        json.append( "]");
        
        json.append( "}");

        json.writeFile( "kwh_year.json", settings.getOutputLocation() + "/json/" );

        numGraphs++;        




        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "kWhYear", numGraphs, duration);
    }
}
