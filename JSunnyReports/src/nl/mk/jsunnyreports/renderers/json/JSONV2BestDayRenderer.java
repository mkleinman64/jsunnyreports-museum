package nl.mk.jsunnyreports.renderers.json;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.TimeEntry;
import nl.mk.jsunnyreports.dataobjects.inverterdata.TopDay;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.dataobjects.invertersumdata.DataItem;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * JSONV2BestDayRenderer.java
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2BestDayRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2BestDayRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2BestDayRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    public void createJSON() {
        long programStart = System.currentTimeMillis();

        StringBuilder dateString;
        
        for ( TopDay td: inverterData.getTopDayInverters().getTopDayInverters()) {
            
            
            JSONTemplate json = new JSONTemplate();
            
            BaseInverter b = inverters.getInverter( td.getName() );
            
            json.append("{");
            json.append("\"name\":\"" + td.getName() + "\",");
            json.append("\"date\":\"" + td.getBestDayString() + "\",");
            
            //json.append("\"peakpower\":" + td.getPeakPower() + ",");
            json.append("\"kwh\":" + td.getkWh() + ",");
            
            json.append( "\"linecolor\" : [" + b.getM_LineColor().getRed() + "," + b.getM_LineColor().getGreen() + "," + b.getM_LineColor().getBlue() + "]"  );
            json.append( ",");

            if ( b.getO_CompareLineColor() != null ) {
                json.append( "\"comparelinecolor\" : [" + b.getO_CompareLineColor().getRed() + "," + b.getO_CompareLineColor().getGreen() + "," + b.getO_CompareLineColor().getBlue() + "]"  );
                
            } else {
                json.append( "\"comparelinecolor\" : []"  );
                
            }  
            json.append( ",");
            
            json.append("\"data\":");
            json.append("[");

            if ( td.getTimeEntries() != null ) {
                int count = 1;
                for (TimeEntry t : td.getTimeEntries()) {
                    
                    float wwp = t.getWatt() / b.getM_WattPeak();

                    json.append("{");

                    json.append("\"h\":" + t.getHour());
                    json.append(",");

                    json.append("\"m\":" + t.getMinute());
                    json.append(",");

                    json.append("\"s\":" + t.getSecond());
                    json.append(",");

                    json.append("\"watt\":" + t.getWatt() );
                    json.append(",");

                    json.append("\"kwhc\":" + t.getkWhCummulative() );
                    json.append(",");
                    
                    json.append("\"wwp\":" + wwp );

                    json.append("}");

                    if (count < td.getTimeEntries().size()) {
                        json.append(", ");
                    }
                    count++;
                }
                
            }
            
            json.append("]");
            json.append("}");
            json.writeFile( "best-" + td.getName() + ".json", settings.getOutputLocation() + "/json/" );

            numGraphs++;
            
        }
        

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "Day", numGraphs, duration);
    }
}
