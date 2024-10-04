package nl.mk.jsunnyreports.renderers.json;

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
 * JSONV2DayRenderer.java
 *
 * @author Martin Kleinman
 * @since 2.0.0
 * @version 2.5.0
 */
public class JSONV2DayRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2DayRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2DayRenderer.class);


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

        for (Year year : inverterData.getYears()) {
            if (year.isCacheModified()) {

                for (Month month : year.getMonths()) {
                    // added cache Is Modified.
                    if (month.hasDaysWithData() && month.isCacheModified()) {

                        for (Day day : month.getDays()) {
                            if (day.isCacheModified() && day.hasInverters()) {
                                dateString = new StringBuilder(day.getDay() + "-" + month.getMonth() + "-" + year.getYear());

                                JSONTemplate json = new JSONTemplate();
                                json.append("{");
                                
                                json.append( "\"year\":\"" + year.getYear() + "\"" );
                                json.append( ",");

                                json.append( "\"month\":\"" + month.getMonth() + "\"" );
                                json.append( ",");

                                json.append( "\"day\":\"" + day.getDay() + "\"" );
                                json.append( ",");

                                
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

                                json.append( "\"peakpower\":\"" + peakPower + "\"" );
                                json.append( ",");
                               
                                json.append( "\"peakpowertime\":\"" + time + "\"" );
                                json.append( ",");

                                json.append("\"inverterdata\":");
                                json.append("[");

                                int icCount = 1;

                                for (ComplexInverter ci : day.getInverters()) {
                                    
                                    BaseInverter b = inverters.getInverter( ci.getName() );
                                    
                                    json.append("{");
                                    json.append("\"name\":\"" + ci.getName() + "\",");
                                    json.append("\"wp\":" + ci.getWp() + ",");
                                    json.append("\"show\": 1,");

                                    json.append("\"peakpower\":" + ci.getPeakPower() + ",");
                                    json.append("\"kwh\":" + ci.getkWh() + ",");
                                 
                                    float now = 0f; 
                                    if ( ci.getTimeEntries().size() > 0 ) {
                                        now = ci.getTimeEntries().get( ci.getTimeEntries().size() - 1 ).getWatt();
                                    }
                                    json.append("\"now\":" + now + ",");
                                    
                                    json.append( "\"linecolor\" : [" + b.getM_LineColor().getRed() + "," + b.getM_LineColor().getGreen() + "," + b.getM_LineColor().getBlue() + "]"  );
                                    json.append( ",");
                                    
                                    json.append( "\"piecolor\" : [" + b.getM_PieColor().getRed() + "," + b.getM_PieColor().getGreen() + "," + b.getM_PieColor().getBlue() + "]"  );
                                    json.append( ",");
                                    

                                    if ( b.getO_CompareLineColor() != null ) {
                                        json.append( "\"comparelinecolor\" : [" + b.getO_CompareLineColor().getRed() + "," + b.getO_CompareLineColor().getGreen() + "," + b.getO_CompareLineColor().getBlue() + "]"  );
                                        
                                    } else {
                                        json.append( "\"comparelinecolor\" : []"  );
                                        
                                    }  
                                    json.append( ",");
                                    
                                    json.append("\"data\":");
                                    json.append("[");


                                    int count = 1;
                                    for (TimeEntry t : ci.getTimeEntries()) {
                                        
                                        float wwp = t.getWatt() / ci.getWp();

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

                                        if (count < ci.getTimeEntries().size()) {
                                            json.append(", ");
                                        }
                                        count++;
                                    }
                                    json.append("]");

                                    json.append("}");
                                    if (icCount < day.getInverters().size()) {
                                        json.append(", ");
                                    }
                                    icCount++;
                                }
                                
                                // add the merged inverter here.
                                if ( day.getInverters().size() > 1 ) {
                                    json.append( ",");
                                    
                                    json.append("{");

                                    json.append("\"name\":\"" + language.getText( "caption.merged" ) + "\",");
                                    json.append("\"show\": 0,");
                                    json.append( "\"linecolor\" : [" + settings.getMergeColor().getRed() + "," + settings.getMergeColor().getGreen() + "," +settings.getMergeColor().getBlue() + "]"  );
                                    json.append( ",");

                                    json.append( "\"comparelinecolor\" : [" + settings.getMergeColor().getRed() + "," + settings.getMergeColor().getGreen() + "," +settings.getMergeColor().getBlue() + "]"  );
                                    json.append( ",");                                

                                    json.append("\"data\":");
                                    json.append("[");


                                    int count = 1;
                                    for (DataItem d : day.getMergedData().getDayData()) {

                                        json.append("{");

                                        json.append("\"h\":" + d.getHour());
                                        json.append(",");

                                        json.append("\"m\":" + d.getMinute());
                                        json.append(",");

                                        json.append("\"s\":" + d.getSecond());
                                        json.append(",");

                                        json.append("\"watt\":" + d.getWattFromMainList() );

                                        json.append("}");

                                        if (count < day.getMergedData().getDayData().size()) {
                                            json.append(",");
                                        }
                                        count++;
                                    }
                                    json.append("]");

                                    json.append("}");
                                    
                                }

                                json.append("]");
                                json.append("}");
                                json.writeFile(dateString + ".json", settings.getOutputLocation() + "/json/" + year.getYear());

                                numGraphs++;
                            }
                        }
                    }
                }
            }
        }
        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "Day", numGraphs, duration);
    }
}
