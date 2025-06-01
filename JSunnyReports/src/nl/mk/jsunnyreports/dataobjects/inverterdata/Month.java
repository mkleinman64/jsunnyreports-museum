package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.inverters.BaseInverter;

/**
 * Month.java
 *
 * @version 2.5.0
 * @since   0.0.0.1
 *
 */
public class Month implements Serializable {
    public Month(byte monthNum, Year parentYear) {
        this.month = monthNum;
        this.wh = 0;
        this.peakpower = 0f;
        this.parentYear = parentYear;
        this.setCacheModified(true);
        
        this.inverters = new ArrayList<SimpleInverter>();

        // get the last day.
        this.lastCalendarDay = Calendar.getInstance(Constants.getLocalTimeZone());
        this.lastCalendarDay.set(Calendar.DAY_OF_MONTH, 1);
        this.lastCalendarDay.set(Calendar.MONTH, month);
        this.lastCalendarDay.set(Calendar.YEAR, parentYear.getYear());
        this.lastCalendarDay.add(Calendar.DAY_OF_MONTH, -1); // this changes the day of month from the first day, set earlier with month to month - 1 day. = last day.
        
        this.lastCalendarDay.set(Calendar.HOUR_OF_DAY, 0);
        this.lastCalendarDay.set(Calendar.MINUTE, 0);
        this.lastCalendarDay.set(Calendar.SECOND, 0);
        this.lastCalendarDay.set(Calendar.MILLISECOND, 0);

        this.days = new Day[ lastCalendarDay.get(Calendar.DAY_OF_MONTH) ];
        
        for (byte d = 1; d <= lastCalendarDay.get(Calendar.DAY_OF_MONTH); d++) {
            Day newDay = new Day(d, this);
            days[d-1] = newDay;
        }
    }

    private byte month;
    private int wh;
    private float peakpower;
    private Day[] days;
    private List<SimpleInverter> inverters;
    private Year parentYear;

    private Calendar lastCalendarDay;

    private transient boolean cacheModified = false;


    @SuppressWarnings("compatibility:500639245289292884")
    static final long serialVersionUID = -513207252999666448L;

    public float getkWh() {
        return this.getWh() / 1000f;
    }

    /**
     *
     * @param dayNum get Day with Daynum within this month
     * @return Day
     */
    public Day getDay(int dayNum) {
            
        if ( dayNum <= days.length ) {
            return days[dayNum - 1];
        } else {
            return null;
        }
        
    }

    public boolean hasDaysWithData() {
        Day d = getLatestDayWithData();
        if (d == null) {
            return false;
        }
        return true;
    }

    public boolean hasInverterData() {
        if (inverters.size() > 0) {
            return true;
        } else {
            return false;
        }

    }


    /**
     *
     * @return latest day for this month that contains data
     */
    public Day getLatestDayWithData() {
        Day returnValue = null;
        for (byte i = (byte)days.length; i >= 1; i--) {
            Day d = getDay(i);
            if (d.hasInverters()) {
                returnValue = d;
                break;
            }
        }

        return returnValue;
    }

    public int getMaxDay() {
        return days.length;
    }

    public SimpleInverter getAndAddInverter(BaseInverter inverter) {
        for (SimpleInverter si : inverters ) {
            if (inverter.getM_InverterName().equals(si.getName())) {
                // this change will make the dataset more consistent with the cached values.
                si.setWp(inverter.getM_WattPeak());
                si.setKwhkwp(inverter.getM_kWhkWp());

                return si;
            }
        }
        SimpleInverter i = new SimpleInverter(inverter.getM_InverterName(), inverter.getM_WattPeak(), inverter.getM_kWhkWp());
        inverters.add(i);
        setCacheModified(true);
        return i;
    }

    public SimpleInverter getInverter(String inverterName) {
        // this method returns the month specified by the intValue.
        SimpleInverter returnValue = null;
        for (SimpleInverter si : this.getInverters()) {
            if (inverterName.equals(si.getName())) {
                return si;
            }
        }
        return returnValue;
    }

    /**
     * add a specific amount to this month for a certain baseInverter.
     *
     *
     * @param inverter name of the baseInverter we have to add to
     * @param addWh wh to add
     */
    public void addValues(BaseInverter inverter, int addWh) {
        wh = wh + addWh;
        setCacheModified(true);
        this.getParentYear().addValues(inverter, addWh);
        this.getAndAddInverter(inverter).addValues(addWh);
    }

    /**
     * substract a specific amount to this month for a certain baseInverter.
     *
     * @param inverter name of the baseInverter we have to substract from
     * @param subWh wh amount to substract from the set
     */
    public void subValues(BaseInverter inverter, int subWh) {
        if (wh < subWh) {
            wh = 0;
        } else {
            wh = wh - subWh;
        }

        setCacheModified(true);
        this.getParentYear().subValues(inverter, subWh);
        this.getAndAddInverter(inverter).subValues(subWh);
    }

    public int getInstalledWp() {
        int returnValue = 0;
        for (SimpleInverter si : inverters) {
            if (si.getWh() > 0) {
                returnValue = returnValue + si.getWp();
            }
        }
        return returnValue;
    }

    public Day getBestDay() {
        Day bestDay = null;
        for (Day d :  days ) {
            if (  bestDay == null || d.getWh() > bestDay.getWh() ) {
                bestDay = d;
            }
        }
        return bestDay;
    }
    
    public float getSavingsInverter( String inverterName ) {
        float returnValue = 0f;
        for (Day d : days) {
            returnValue = returnValue + d.getSavingsInverter( inverterName );
        }
        return returnValue;
        
    }    

    public float getSavings() {
        float returnValue = 0;
        for (Day d : days) {
            returnValue = returnValue + d.getSavings();
        }
        return returnValue;
    }

    /* Setters and getters below here */

    public byte getMonth() {
        return month;
    }

    public int getWh() {
        return wh;
    }

    public void setPeakpower(float peakpower) {
       if ( peakpower < Float.MAX_VALUE ) {
          this.peakpower = peakpower;
       }
    }

    public float getPeakpower() {
        return peakpower;
    }

    public Day[] getDays() {
        return days;
    }

    public List<SimpleInverter> getInverters() {
        return inverters;
    }

    public Year getParentYear() {
        return parentYear;
    }

    public void setCacheModified(boolean cacheModified) {
        this.cacheModified = cacheModified;
        this.getParentYear().setCacheModified(cacheModified);
    }

    public boolean isCacheModified() {
        return cacheModified;
    }

    public Calendar getLastCalendarDay() {
        return lastCalendarDay;
    }


}

