package nl.mk.jsunnyreports.dataobjects.costdata;

import java.io.Serializable;

import java.util.Calendar;

/**
 * CostEntry.java
 *
 * @author  Martin Kleinman
 * @version 2.5.0
 * @since   0.0.0.1
 *
 */
public class CostEntry implements Serializable {
    @SuppressWarnings("compatibility:9186036125490511496")
    static final long serialVersionUID = -8445719452915693241L;

    public CostEntry(Calendar fromDate, Calendar tillDate, int cost, int incentive) {
        this.from = fromDate;
        this.till = tillDate;
        fromMillis = fromDate.getTimeInMillis();
        tillMillis = tillDate.getTimeInMillis();
        this.energyCost = cost;
        this.incentive = incentive;
    }
    
    // both calendars are only used for displaying debug data. removing them will break the cache structure. 
    private Calendar from;
    private Calendar till;
    private long fromMillis;
    private long tillMillis;
    private int energyCost;     // 0,2231 cent/kWh is stored as 0,2331*10000 ( to remove the . ).
    private int incentive;      // 0,2231 cent/kWh is stored as 0,2331*10000 ( to remove the . ).


    public Calendar getFrom() {
        return from;
    }

    public Calendar getTill() {
        return till;
    }


    public int getEnergyCost() {
        return energyCost;
    }

    public int getIncentive() {
        return incentive;
    }

    public long getFromMillis() {
        return fromMillis;
    }

    public long getTillMillis() {
        return tillMillis;
    }
}
