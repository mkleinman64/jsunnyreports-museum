package nl.mk.jsunnyreports.renderers.json;

import java.util.List;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.renderers.tables.timerecords.DayRecords;
import nl.mk.jsunnyreports.renderers.tables.timerecords.MonthRecords;
import nl.mk.jsunnyreports.renderers.tables.timerecords.YearRecords;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * JSONV2RecordsRenderer.java
 *
 * @author Martijn van der Pauw 
 * @author Martin Kleinman
 * @since 1.1.0.0
 * @version 1.3.2.0
 */
public class JSONV2RecordsRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable{

   

    public JSONV2RecordsRenderer( InverterData inverterData, InverterList inverters, Settings settings, Language language ) {
        super(inverterData, settings, inverters, language );

        this.yr_total = new YearRecords(settings);
        this.mr_total = new MonthRecords(settings);
        this.dr_total = new DayRecords(settings);
    }

    private static final Logger log = Logger.getLogger(JSONV2RecordsRenderer.class);

    private YearRecords yr_total;
    private MonthRecords mr_total;
    private DayRecords dr_total;
    private DayRecords dr_months[] = new DayRecords[13];
    private DayRecords[] dr_years;

    @Override
    public void run() {
        doMagic();
    }

    /**
     * Creates the various structures that hold the records.
     */
    public void determineRecords() {
        dr_years = new DayRecords[inverterData.getYears().size()];
        int yearcounter = 0;
        for (Year year : inverterData.getYears()) {
            dr_years[yearcounter] = new DayRecords(settings);
            yr_total.checkRecord(year);
            for (int monthNum = 1; monthNum <= 12; monthNum++) {
                Month month = year.getMonth(monthNum);

                if (month.hasDaysWithData()) {
                    mr_total.checkRecord(month);
                    for (Day day : month.getDays()) {
                        dr_total.checkRecord(day);
                        if (dr_months[month.getMonth()] == null) {
                            dr_months[month.getMonth()] = new DayRecords(settings);
                        }
                        dr_months[month.getMonth()].checkRecord(day);
                        dr_years[yearcounter].checkRecord(day);
                    }

                }
            }
            yearcounter++;
        }
    }

    public void doMagic() {
        this.determineRecords();
        this.createYearRecordJSON();
        this.createYearFirstReachedWattData();
        this.createBestDaysRecords();
        this.createBestMonthsRecords();
        this.createBestYearsRecords();
    }
    
    public void createYearRecordJSON() {
        long programStart = System.currentTimeMillis();
        numGraphs = 0;
        for (int i = 0; i < dr_years.length; i++) {
            JSONTemplate json = new JSONTemplate();
            json.append("{");
            
            json.append( "\"data\":" );
            json.append("[");

            List<Day> list = dr_years[i].getRecordList();

            if (list != null) {

                for (int j = 0; j < list.size(); j++) {
                    Day day = list.get(j);
                                        
                    json.append("{");
                    
                    json.append( "\"position\": " + (j+1) );
                    json.append( ",");   
                    
                    String date = day.getDay() + "-" + day.getParentMonth().getMonth() + "-" + day.getParentMonth().getParentYear().getYear();

                    json.append( "\"date\": \"" + date + "\"" );
                    json.append( ",");

                    json.append( "\"kwh\": " + day.getkWh() );
                    json.append( ",");   
                    
                    json.append( "\"peakpower\": " + day.getPeakpower() );
                    
                    json.append("}");
                    
                    if ( j < list.size() - 1 ) {
                        json.append( ",");
                    }
                }
                json.append("]");
                json.append("}");
            }
            json.writeFile( "dayrecords.json", settings.getOutputLocation() + "/json/" + dr_years[i].getYear() );
            numGraphs++;
        }

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "TopDaysYearRecords", numGraphs, duration);        
    }
    
    public void createYearFirstReachedWattData() {
        long programStart = System.currentTimeMillis();
        numGraphs = 0;
        for (int i = 0; i < dr_years.length; i++) {
            JSONTemplate json = new JSONTemplate();
            json.append("{");
            
            json.append( "\"data\":" );
            json.append("[");

            json.append( dr_years[i].getfirstReachedModelJSON() ); // IEUW, UGLY. 

            json.append("]");
            json.append("}");
            
            json.writeFile( "reachedwatt.json", settings.getOutputLocation() + "/json/" + dr_years[i].getYear() );
            numGraphs++;
        }

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "WattYearRecords", numGraphs, duration);           
    }
    
    public void createBestDaysRecords() {
        long programStart = System.currentTimeMillis();
        numGraphs = 0;
        
        JSONTemplate json = new JSONTemplate();
        json.append("{");
        
        json.append( "\"data\":" );
        json.append("[");
        
        List<Day> list = dr_total.getRecordList();

        for (int i = 0; i < list.size(); i++) {
            Day day = list.get(i);
            json.append("{");
            
            json.append( "\"position\": " + ( i + 1) );
            json.append( ","); 
        
            String date = day.getDay() + "-" + day.getParentMonth().getMonth() + "-" + day.getParentMonth().getParentYear().getYear();  

            json.append( "\"date\": \"" + date + "\"" );
            json.append( ",");

            json.append( "\"kwh\": " + day.getkWh() );
            json.append( ",");   
            
            json.append( "\"peakpower\": " + day.getPeakpower() );
            
            json.append("}");
            if ( i < list.size() - 1 ) {
                json.append( ",");
            }
            
        }
        
        json.append("]");
        json.append("}");
      
        json.writeFile( "dayrecords.json", settings.getOutputLocation() + "/json/" );
        numGraphs++;
        

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "BestDaysRecords", numGraphs, duration);             
    }

    public void createBestMonthsRecords() {
        
        long programStart = System.currentTimeMillis();
        numGraphs = 0;
        
        JSONTemplate json = new JSONTemplate();
        json.append("{");
        
        json.append( "\"data\":" );
        json.append("[");        

        List<Month> list = mr_total.getRecordList();

        for (int i = 0; i < list.size(); i++) {
            Month month = list.get(i);
            
            json.append("{");
            json.append( "\"position\": " + ( i + 1) );
            json.append( ","); 
            
            String date = month.getMonth() + "-" + month.getParentYear().getYear();  

            json.append( "\"date\": \"" + date + "\"" );
            json.append( ",");
            json.append( "\"kwh\": " + month.getkWh() );
            json.append( ",");   
            json.append( "\"peakpower\": " + month.getPeakpower() );
            
            json.append("}");
            if ( i < list.size() - 1 ) {
                json.append( ",");
            }
            
        }
        json.append("]");
        json.append("}");
        
        json.writeFile( "monthrecords.json", settings.getOutputLocation() + "/json/" );
        numGraphs++;

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "BestMonthRecords", numGraphs, duration);               
    }
    
    public void createBestYearsRecords() {
        
        long programStart = System.currentTimeMillis();
        numGraphs = 0;
        
        JSONTemplate json = new JSONTemplate();
        json.append("{");
        
        json.append( "\"data\":" );
        json.append("[");        

        List<Year> list = yr_total.getRecordList();

        for (int i = 0; i < list.size(); i++) {
            Year year = list.get(i);
            
            json.append("{");
            json.append( "\"position\": " + ( i + 1) );
            json.append( ","); 
            
            String date = "" + year.getYear();  

            json.append( "\"date\": \"" + date + "\"" );
            json.append( ",");
            json.append( "\"kwh\": " + year.getkWh() );
            json.append( ",");   
            json.append( "\"peakpower\": " + year.getPeakpower() );
            
            json.append("}");
            if ( i < list.size() - 1 ) {
                json.append( ",");
            }
            
        }
        json.append("]");
        json.append("}");
        
        json.writeFile( "yearrecords.json", settings.getOutputLocation() + "/json/" );
        numGraphs++;

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "BestYearsRecords", numGraphs, duration);               
    }    
}
