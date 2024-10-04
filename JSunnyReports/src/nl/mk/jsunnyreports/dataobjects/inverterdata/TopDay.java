package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.List;

/**
 * TopDay.java
 * 
 * Keeps track of the TopDay ever for an baseInverter. And keeps track of the TimeEntries
 *
 * @version 1.3.2.0
 * @since 1.3.0.2A
 */
public class TopDay implements Serializable {

    @SuppressWarnings("compatibility:1808018082636738147")
    static final long serialVersionUID = -7468194881494778839L;

    private String bestDayString; // Date in "dd-mm-yyyy" format.
    private String name; // name of the inverter
    private int wh; //  wh generated this day by this inverter
    private List<TimeEntry> timeEntries; // Watt entries for this inverter

    // INIT

    public TopDay(String inverterName) {
        this.name = inverterName;
        this.wh = 0;
        this.timeEntries = null;
    }

    public float getkWh() {
        return wh  / 1000f;
    }

    public void setBestDayString(String bestDayString) {
        this.bestDayString = bestDayString;
    }

    public String getBestDayString() {
        return bestDayString;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setWh(int wh) {
        this.wh = wh;
    }

    public int getWh() {
        return wh;
    }

    public void setTimeEntries(List<TimeEntry> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public List<TimeEntry> getTimeEntries() {
        return timeEntries;
    }
}
