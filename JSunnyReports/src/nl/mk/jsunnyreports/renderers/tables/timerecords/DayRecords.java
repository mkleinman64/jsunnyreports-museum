package nl.mk.jsunnyreports.renderers.tables.timerecords;

import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.common.settings.Settings;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.renderers.tables.sorters.DaySortByKwh;

import org.apache.log4j.Logger;

/**
 * DayRecords.java
 *
 * Date         Version     Who     What
 * 24-01-2011   1.2.0.0     MKL     Added dfWatt to format Watt columns
 *
 * @author  Martijn van der Pauw
 * @author  Martin Kleinman
 * @version 1.2.0.0
 * @since   1.0.0.0
 */
public class DayRecords {
    public DayRecords(Settings settings) {
        this.settings = settings;
    }
    private static final Logger log = Logger.getLogger(DayRecords.class);

    private float minrecord = 0;
    private final int LIST_LENGTH = 15;

    private List<Day> recorddays = new ArrayList<Day>();
    private Settings settings;

    int[] PEAKPOWER_BORDERS = { 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900, 3000, 3100, 3200, 3300, 3400, 3500, 3600, 3700, 3800, 3900, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 300000, 400000, 500000 };
    Day[] peakpower_reached = new Day[PEAKPOWER_BORDERS.length];

    private void checkBorders(Day newday) {
        for (int i = 0; i < peakpower_reached.length; i++) {
            if (newday.getPeakpower() >= PEAKPOWER_BORDERS[i]) {
                if (peakpower_reached[i] == null) {
                    peakpower_reached[i] = newday;
                    // log.debug("reached "+PEAKPOWER_BORDERS[i]+" = "+newday.getParentMonth().getMonth()+" "+newday.getDay());
                }
            } else
                break;
        }
    }
    
    public int getYear() {
        Day d = recorddays.get(0);
        return d.getParentMonth().getParentYear().getYear();
    }

    public Day getBestDay() {
        return recorddays.get(0);
    }

    public boolean checkRecord(Day newday) {
        //
        //first check the limits
        this.checkBorders(newday);

        if (recorddays.size() < LIST_LENGTH || newday.getkWh() > minrecord) {
            if (recorddays.size() < LIST_LENGTH)
                recorddays.add(newday);
            else {
                recorddays.remove(LIST_LENGTH - 1);
                recorddays.add(LIST_LENGTH - 1, newday);
            }
            Collections.sort(recorddays, new DaySortByKwh());
            if (recorddays.size() >= LIST_LENGTH)
                minrecord = recorddays.get(LIST_LENGTH - 1).getkWh();
            return true;
        }

        return false;
    }

    public String getfirstReachedModelJSON() {
        StringBuilder sb = new StringBuilder();
        for (int frm = 0; frm < peakpower_reached.length; frm++) {

            Day startday = peakpower_reached[frm];
            if (startday != null) {
                if ( frm < peakpower_reached.length && frm != 0 ) {
                    sb.append(",");
                }
                sb.append("{");
                sb.append("\"peak\": \"" + PEAKPOWER_BORDERS[frm] + "\"");
                sb.append(",");
                String date = startday.getDay() + "/" + startday.getParentMonth().getMonth() + "/" + startday.getParentMonth().getParentYear().getYear();
                sb.append("\"date\": \"" + date + "\"");
                sb.append(",");
                sb.append( "\"peakpower\": " + startday.getPeakpower() );
                sb.append("}");
            }
        }
        return sb.toString();
    }

    public List<Day> getRecordList() {
        for (int i = 0; i < recorddays.size(); i++) {
            Day tempday = recorddays.get(i);
            //log.debug("Op plek "+i+" "+tempday.getKWh()+" on "+tempday.getDay()+"-"+tempday.getParentMonth().getMonth());
        }
        return recorddays;
    }

}
