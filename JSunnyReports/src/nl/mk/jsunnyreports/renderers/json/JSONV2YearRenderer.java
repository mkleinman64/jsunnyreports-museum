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
 * JSONV2YearRenderer.java
 * 
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2YearRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2YearRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language);
    }

    private static final Logger log = Logger.getLogger(JSONV2YearRenderer.class);


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

            if (year.isCacheModified()) {
                JSONTemplate json = new JSONTemplate();

                json.append("{");

                float kwhkwpy = (year.getkWh() * 1000) / year.getInstalledWp();
                
                float expected = inverters.getExpectedYearYield( year.getYear() );

                json.append("\"year\": " + year.getYear() + ",");
                json.append("\"kwh\": " + year.getkWh() + ",");
                json.append("\"expected\": " + expected + ",");
                
                float yearkWhPercentage = ( year.getkWh() / expected ) * 100;
                json.append("\"kwhpercentage\": " + yearkWhPercentage + ",");                
                json.append("\"wp\": " + year.getInstalledWp() + ",");
                json.append("\"peakpower\": " + year.getPeakpower() + ",");
                json.append("\"co2\": " + ( year.getkWh() * co2Value ) + ",");
                json.append("\"kwhkwp\": " + kwhkwpy + ",");
                json.append("\"savings\": " + year.getSavings() + ",");

                json.append("\"inverters\":");
                json.append("[");

                int siCounty = 1;
                int siTotaly = year.getInverters().size();
                for (SimpleInverter bv : year.getInverters()) {

                    BaseInverter b = inverters.getInverter( bv.getName());
                    
                    json.append("{");
                    json.append("\"inverter\": \"" + bv.getName() + "\",");
                    
                    json.append( "\"barcolor\" : [" + b.getM_BarColor().getRed() + "," + b.getM_BarColor().getGreen() + "," + b.getM_BarColor().getBlue() + "]"  );
                    json.append( ",");

                    json.append( "\"piecolor\" : [" + b.getM_PieColor().getRed() + "," + b.getM_PieColor().getGreen() + "," + b.getM_PieColor().getBlue() + "]"  );
                    json.append( ",");
                    
                    json.append("\"kwh\": " + bv.getkWh() + ",");
                    json.append("\"monthdata\":");

                    json.append("[");

                    for (int monthnum = 1; monthnum <= 12; monthnum++) {

                        if (year.getMonth(monthnum) != null && year.getMonth(monthnum).getInverter(bv.getName()) != null) {
                            json.append(year.getMonth(monthnum).getInverter(bv.getName()).getkWh());
                        } else {
                            json.append("0.00");
                        }
                        if (monthnum < 12) {
                            json.append(", ");

                        }
                    }
                    json.append("]");
                    json.append("}");
                    if (siCounty < siTotaly) {
                        json.append(", ");
                    }
                    siCounty++;
                }

                json.append("],");


                json.append("\"months\":");
                json.append("[");

                for (Month month : year.getMonths()) {

                    float kwhkwpm = 0f;
                    if ( month.hasDaysWithData() ) {
                        kwhkwpm = (month.getkWh() * 1000) / month.getInstalledWp();
                        
                    } 

                    json.append("{");
                    json.append("\"month\": " + month.getMonth() + ",");
                    json.append("\"kwh\": " + month.getkWh() + ",");
                    json.append("\"wp\": " + month.getInstalledWp() + ",");
                    json.append("\"peakpower\": " + month.getPeakpower() + ",");
                    json.append("\"co2\": " + (month.getkWh() * co2Value) + ",");
                    json.append("\"kwhkwp\": " + kwhkwpm + ",");

                    float monthExpected = inverters.getExpectedMonthYield(year.getYear(), month.getMonth());
                    
                    float kwhPercentage = 0f;
                    if ( monthExpected > 0f ) {
                        kwhPercentage = ( month.getkWh() / monthExpected ) * 100;
                    }

                    json.append("\"expected\": " + monthExpected + ",");
                    json.append("\"kwhpercentage\": " + kwhPercentage + ",");

                    float lastYear = 0f;
                    Year previousYear = inverterData.getPreviousYear(year.getYear());
                    if (previousYear != null) {
                        if (previousYear.getMonth(month.getMonth()) != null) {
                            lastYear = previousYear.getMonth(month.getMonth()).getkWh();
                        } else {
                            lastYear = 0f;
                        }
                    }

                    json.append("\"lastyear\": " + lastYear + ",");

                    float averageValueDay = 0f;
                    if (month != null && month.getLatestDayWithData() != null) {
                        averageValueDay = month.getkWh() / (float)month.getLatestDayWithData().getDay();
                    }


                    json.append("\"dayaverage\": " + averageValueDay + ",");

                    float dayExpected = inverters.getExpectedDayYield(1, month.getMonth(), year.getYear());
                    json.append("\"dayexpected\": " + dayExpected + ",");

                    json.append("\"savings\":" + month.getSavings() + ",");
                    json.append("\"maxday\":" + month.getMaxDay() + ",");

                    json.append("\"inverters\":");
                    json.append("[");

                    int siCount = 1;
                    int siTotal = month.getInverters().size();
                    for (SimpleInverter bv : month.getInverters()) {
                        
                        BaseInverter b = inverters.getInverter( bv.getName() );

                        json.append("{");
                        json.append("\"inverter\": \"" + bv.getName() + "\",");
                        
                        json.append( "\"barcolor\" : [" + b.getM_LineColor().getRed() + "," + b.getM_LineColor().getGreen() + "," + b.getM_LineColor().getBlue() + "]"  );
                        json.append( ",");
                        json.append("\"kwh\": " + bv.getkWh() + ",");
                        json.append("\"daydata\":");

                        json.append("[");

                        for (int daynum = 1; daynum <= month.getMaxDay(); daynum++) {

                            if (month.getDay(daynum) != null && month.getDay(daynum).getInverter(bv.getName()) != null) {
                                json.append(month.getDay(daynum).getInverter(bv.getName()).getkWh());
                            } else {
                                json.append("0.000");
                            }
                            if (daynum < month.getMaxDay()) {
                                json.append(", ");

                            }
                        }
                        json.append("]");
                        json.append("}");
                        if (siCount < siTotal) {
                            json.append(", ");
                        }
                        siCount++;
                    }

                    json.append("],");

                    if (month.hasDaysWithData()) {
                        json.append("\"hasdata\": 1");
                    } else {
                        json.append("\"hasdata\": 0");
                    }

                    json.append("}");
                    if (month.getMonth() < 12) {
                        json.append(", ");
                    }
                }
                json.append("]");


                json.append("}");

                yc++;
                json.writeFile(year.getYear() + ".json", outputLocation + "/" + year.getYear());
                numGraphs++;
            }

        }


        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;

        printOutputText(log, "YearData", numGraphs, duration);
    }
}
