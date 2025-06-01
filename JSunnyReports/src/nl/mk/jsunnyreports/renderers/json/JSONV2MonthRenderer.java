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
public class JSONV2MonthRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2MonthRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language);
    }

    private static final Logger log = Logger.getLogger(JSONV2MonthRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    public void createJSON() {
        long programStart = System.currentTimeMillis();



        int yc = 1;
        float co2Value = settings.getCo2kWh();

        for (Year year : inverterData.getYears()) {
            
            if ( year.isCacheModified() ) {
                for (Month month : year.getMonths()) {
                    
                    if ( month.isCacheModified() ) {
                        JSONTemplate json = new JSONTemplate();
                        
                        float kwhkwpm = 0f;
                        if ( month.getInstalledWp() > 0 ) {
                           kwhkwpm = ( month.getkWh() * 1000 ) / month.getInstalledWp();     
                        }
                        
                        json.append("{");
                        json.append("\"year\": " + year.getYear() + ",");
                        json.append("\"month\": " + month.getMonth() + ",");
                        json.append("\"kwh\": " + month.getkWh() + ",");
                        json.append("\"wp\": " + month.getInstalledWp() + ",");
                        json.append("\"peakpower\": " +  month.getPeakpower() + ",");
                        json.append("\"co2\": " +  ( month.getkWh() * co2Value ) + ",");
                        json.append("\"kwhkwp\": " +  kwhkwpm + ",");

                        float monthExpected =  inverters.getExpectedMonthYield(year.getYear(), month.getMonth() );

                        json.append("\"expected\": " + monthExpected + ",");
                        
                        float kWhMonthPercentage = ( month.getkWh() / monthExpected ) * 100;
                        json.append("\"kwhpercentage\": " + kWhMonthPercentage + ",");
                        
                        
                        
                        float lastYear = 0f;
                        Year previousYear = inverterData.getPreviousYear(year.getYear());
                        if (previousYear != null) {
                            if (previousYear.getMonth(month.getMonth()) != null) {
                                lastYear = previousYear.getMonth(month.getMonth()).getkWh();
                            } else {
                                lastYear =  0f;
                            }
                        }
                        
                        json.append("\"lastyear\": " + lastYear + ",");
                        
                        float averageValueDay = 0f;
                        if ( month != null && month.getLatestDayWithData() != null ){
                            averageValueDay = month.getkWh() / (float)month.getLatestDayWithData().getDay();    
                        }
                                                                         
                        
                        json.append("\"dayaverage\": " + averageValueDay + ",");
                        
                        float dayExpected = inverters.getExpectedDayYield(1, month.getMonth(), year.getYear());
                        json.append("\"dayexpected\": " + dayExpected + ",");

                        json.append("\"savings\":" + month.getSavings() + ",");
                        json.append("\"maxday\":" + month.getMaxDay() + ",");
                        
                        json.append("\"inverters\":" );
                        json.append("[");

                        int siCount = 1;
                        int siTotal = month.getInverters().size();                
                        for ( SimpleInverter bv: month.getInverters() ) {
                            
                            BaseInverter b = inverters.getInverter( bv.getName() );
                            
                            json.append("{");
                            json.append("\"inverter\": \"" + bv.getName() + "\",");
                            
                            json.append( "\"barcolor\" : [" + b.getM_LineColor().getRed() + "," + b.getM_LineColor().getGreen() + "," + b.getM_LineColor().getBlue() + "]"  );
                            json.append( ",");
                            
                            json.append( "\"piecolor\" : [" + b.getM_PieColor().getRed() + "," + b.getM_PieColor().getGreen() + "," + b.getM_PieColor().getBlue() + "]"  );
                            json.append( ",");                        
                            
                            json.append("\"kwh\": " + bv.getkWh() + "," );
                            json.append("\"daydata\":" );
                            
                            json.append( "[");
                            
                            for ( int daynum=1;daynum<=month.getMaxDay();daynum++) {
                                
                                if ( month.getDay(daynum) != null && month.getDay(daynum).getInverter( bv.getName()) != null ) {
                                    json.append( month.getDay(daynum).getInverter( bv.getName()).getkWh() );
                                } else {
                                    json.append( "0.000");
                                }
                                if ( daynum < month.getMaxDay()) {
                                    json.append(", ");
                                                               
                                }
                            }
                            json.append( "]" );
                            json.append("}");
                            if (siCount < siTotal) {
                                json.append(", ");
                            }
                            siCount++;
                        }
                        
                        json.append("],");

                        if (month.hasDaysWithData()) {
                            json.append("\"hasdata\": 1,");
                        } else {
                            json.append("\"hasdata\": 0,");
                        }

                        json.append("\"days\":");
                        json.append("[");

                        for (Day day : month.getDays()) {
                            
                            float kwhkwpd = 0f;
                            if ( day.hasInverters() ) {
                                if ( day.getInstalledWp() > 0 ) {
                                    kwhkwpd = ( day.getkWh() * 1000 ) / day.getInstalledWp();     
                                }
                                
                            } 
                            
                            json.append("{");
                            json.append("\"day\": " + day.getDay() + ",");
                            json.append("\"dayexpected\": " + dayExpected + ",");
                            json.append("\"kwh\": " + day.getkWh() + ",");
                            
                            float kWhPercentage = ( day.getkWh() / dayExpected ) * 100;

                            json.append("\"kwhpercentage\": " + kWhPercentage + ",");
                            
                            json.append("\"historic\": " + inverterData.getAverageHistoricYieldForADay( month.getMonth(), day.getDay()) + ",");
                            json.append("\"wp\": " + day.getInstalledWp() + ",");

                            String time = "N/A";
                            float peakPower = 0f;
                            
                            for (DataItem d : day.getMergedData().getDayData()) {
                                if ( d.getWattFromMainList() > peakPower ) {
                                    
                                    if ( d.getMinute() < 10 ) {
                                        time = d.getHour() + ":0" + d.getMinute();
                                        
                                    } else {
                                        time = d.getHour() + ":" + d.getMinute();
                                        
                                    }
                                    
                                }
                            }                                

                            json.append( "\"peakpower\": " + day.getPeakpower() );
                            json.append( ",");
                            
                            json.append( "\"peakpowertime\": \"" + time + "\"" );
                            json.append( ",");
                            json.append("\"co2\": " +  day.getkWh() * co2Value + ",");
                            json.append("\"kwhkwp\": " +  kwhkwpd + ",");

                            json.append("\"savings\": " + day.getSavings() + ",");
                            json.append("\"inverters\":");
                            json.append("[");

                            int icCount = 1;
                            int total = day.getInverters().size();
                            for (ComplexInverter ci : day.getInverters()) {
                                
                                
                                BaseInverter b = inverters.getInverter(ci.getName());
                                
                                json.append("{");
                                json.append("\"inverter\": \"" + ci.getName() + "\",");
                                
                                json.append( "\"barcolor\" : [" + b.getM_LineColor().getRed() + "," + b.getM_LineColor().getGreen() + "," + b.getM_LineColor().getBlue() + "]"  );
                                json.append( ",");
                                
                                json.append("\"kwh\": " + ci.getkWh() + ",");
                                json.append("\"peakpower\": " + ci.getPeakPower() + ",");
                                json.append("\"savings\": " + ci.getSavings() );

                                json.append("}");
                                if (icCount < total) {
                                    json.append(", ");
                                }
                                icCount++;

                            }
                            json.append("]");
                            json.append("}");
                            if (day.getDay() < month.getMaxDay()) {
                                json.append(", ");
                            }

                        }
                        json.append("]");

                        json.append("}");

                        json.writeFile( month.getMonth() + "-" + year.getYear() + ".json", outputLocation + "/" + year.getYear() );
                        numGraphs++;                
                        
                        
                    }
                   
               
                }

            }
            
        }




        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;

        printOutputText(log, "MonthData", numGraphs, duration);
    }
}
