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

public class JSONV2HistoryTableRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {

    public JSONV2HistoryTableRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2HistoryTableRenderer.class);

  
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

        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
        int thisYear = cal.get(Calendar.YEAR);
        int thisMonth = (cal.get(Calendar.MONTH) + 1);

        int firstYear = inverterData.getYears().get(0).getYear();
        int lastYear = inverterData.getLatestYear().getYear();

        float avgTotal = 0f;
        float avgYield = 0f;

        for (int month = 1; month <= 12; month++) {

            float monthSum = 0f;
            float numYears = 0f;
            for (int year = firstYear; year <= lastYear; year++) {
                if (inverterData.getYear(year) != null && inverterData.getYear(year).getMonth(month) != null) {
                    Month m = inverterData.getYear(year).getMonth(month);
                    float yield = 0f;
                    if (m != null && m.getkWh() > 0) {
                        yield = m.getkWh();
                        monthSum = monthSum + yield;
                        numYears++;
                    }
                }
            }
            avgYield = 0f;
            if (numYears > 0) {
                avgYield = monthSum / numYears;
                avgTotal = avgTotal + avgYield;
            }
        }

        json.append("{");
        json.append("\"years\":");
        json.append("[");
        
        for ( int i = firstYear; i <= lastYear; i++ ) {
            json.append(i);
            if ( i < lastYear ) {
                json.append(",");
            }
        }
        json.append("]");
        json.append(",");
        
        json.append("\"yields\":");
        json.append("[");
        
        for (int year = firstYear; year <= lastYear; year++) {
            if (inverterData.getYear(year) != null) {
                json.append(inverterData.getYear(year).getkWh());
            } else {
                json.append("0.0");
            }
            
            if ( year < lastYear ) {
                json.append(",");
            }
        }
        
        json.append("]");
        json.append(",");
        json.append("\"average\":");
        json.append( avgTotal );
        json.append(",");
                             
        
        
        json.append("\"data\":");
        json.append("[");
        
        for ( int month = 1; month <= 12; month++) {
            json.append("{");

            json.append("\"month\":" + month );
            json.append(",");
            
            json.append("\"yeardata\":");
            json.append("[");

            float monthSum = 0f;
            float numYears = 0f;

            for (int year = firstYear; year <= lastYear; year++) {

                if (inverterData.getYear(year) != null && inverterData.getYear(year).getMonth(month) != null) {
                    Month m = inverterData.getYear(year).getMonth(month);
                    float yield = 0f;
                    if (m != null && m.getkWh() > 0) {
                        if (m.getMonth() == thisMonth && year == thisYear) {

                            json.append("-1"); // display -- in the real table ( running month ).
                        } else {
                            yield = m.getkWh();
                            monthSum = monthSum + yield;
                            numYears++;
                            json.append( yield );
                        }

                    } else {
                        json.append("-2"); // display &nbsp; in the real table.

                    }
                } else {
                    json.append("-2");// display &nbsp; in the real table.
                }
                
                if ( year < lastYear ) {
                    json.append(",");
                }
            }
            json.append("]");
            json.append(",");
            
            avgYield = 0f;
            if (numYears > 0) {
                avgYield = monthSum / numYears;
            }

            float percentage = (avgYield / avgTotal) * 100;
            json.append("\"average\":" + avgYield );
            json.append(",");
            json.append("\"percentage\":" + percentage );
            json.append(",");
            json.append("\"inverters\":"  );
            json.append("[");
            json.append("]"); // to be added with the subset data of every inverter for this month. 
    
            
            
            json.append("}");
            if ( month < 12 ) {
                json.append(",");
            }
        }
        
        json.append("]");


        json.append("}"); 

   
        numGraphs++;
        json.writeFile("historytable.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "HistoryTable", numGraphs, duration);
        
    }

}
