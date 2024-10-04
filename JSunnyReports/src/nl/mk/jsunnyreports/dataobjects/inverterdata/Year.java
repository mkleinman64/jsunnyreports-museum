package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.inverters.BaseInverter;


/**
 * Year.java
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @author    Martijn van der Pauw
 * @version   2.6.0
 * @since     0.0.1.0
 *
 */
public class Year implements Comparable, Serializable {
    public Year(int year) {
        this.year = year;
        this.peakpower = 0f;
        this.wh = 0;
        this.setCacheModified(true);
        this.inverters = new ArrayList<SimpleInverter>();
        
        this.months = new Month[12];
        for (byte i = 1; i <= 12; i++) {
            Month m = new Month(i, this);
            months[i-1] = m;
        }
    }

    private int year;
    private int wh;
    private float peakpower;

    //private List<Month> months;
    private Month[] months;
    private List<SimpleInverter> inverters;

    private transient boolean cacheModified;
    private transient boolean tosort = false;

    static final long serialVersionUID = 5497712844734826833L;

    public float getkWh() {
        return wh / 1000f;
    }

    /**
     *
     * @param monthNum The month to return
     * @return Month
     */
    public Month getMonth(int monthNum) {
        
        if ( ( months.length - 1 ) >= monthNum - 1  ) {
            return months[monthNum - 1];
        } else {
            return null;
        }
    }


    public Month getLatestMonthWithData() {
        Month returnValue = null;
        for (byte i = 12; i >= 1; i--) {
            Month m = getMonth(i);
            
            if (m.hasDaysWithData()) {
                returnValue = m;
                break;
            }
        }
        return returnValue;
    }

    public int compareTo(Object otherYear) {
        if (!(otherYear instanceof Year)) {
            throw new ClassCastException("Year is expected");
        } else {
            int yearCompare = ((Year)otherYear).getYear();
            return this.year - yearCompare;
        }
    }

    public float getPeakpower() {
        return peakpower;
    }

    private SimpleInverter getAndAddInverter(BaseInverter inverter) {
        for (SimpleInverter i : this.getInverters()) {
            if (inverter.getM_InverterName().equals(i.getName())) {

                // this change will make the dataset more consistent with the cached values.
                i.setWp(inverter.getM_WattPeak());
                i.setKwhkwp(inverter.getM_kWhkWp());
                return i;
            }
        }
        SimpleInverter i = new SimpleInverter(inverter.getM_InverterName(), inverter.getM_WattPeak(), inverter.getM_kWhkWp());
        this.getInverters().add(i);
        this.tosort = true;
        setCacheModified(true);
        return i;
    }

    public SimpleInverter getInverter(String inverterName) {
        // this method returns the month specified by the intValue.
        for (SimpleInverter i : this.getInverters()) {
            if (inverterName.equals(i.getName())) {
                return i;
            }
        }
        return null;
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
     * @param inverter
     * @param addWh
     */
    public void addValues(BaseInverter inverter, int addWh) {
        wh = wh + addWh;
        this.getAndAddInverter(inverter).addValues(addWh);
        this.setCacheModified(true);
    }

    /**
     *
     * @param inverter
     * @param subWh
     */
    public void subValues(BaseInverter inverter, int subWh) {
        if ( wh < subWh) {
            wh = 0;
        } else {
            wh = wh - subWh;
        }
        this.setCacheModified(true);
        this.getAndAddInverter(inverter).subValues(subWh);
    }

    public int getInstalledWp() {
        int returnValue = 0;
        for (SimpleInverter i : this.getInverters()) {
            if (i.getWh() > 0) {
                returnValue = returnValue + i.getWp();
            }
        }
        return returnValue;
    }
    
    public float getSavingsInverter( String inverterName ) {
        float returnValue = 0f;
        for (Month m : this.getMonths()) {
            returnValue = returnValue + m.getSavingsInverter( inverterName );
        }
        return returnValue;
        
    }     

    public float getSavings() {
        float returnValue = 0;
        for (Month m : this.getMonths()) {
            returnValue = returnValue + m.getSavings();
        }
        return returnValue;
    }

    public void setCacheModified(boolean cacheModified) {
        this.cacheModified = cacheModified;
    }

    public boolean isCacheModified() {
        return cacheModified;
    }

    public int getYear() {
        return year;
    }

    public int getWh() {
        return wh;
    }

    public void setPeakpower(float peakpower) {
       if ( peakpower < Float.MAX_VALUE ) {
          this.peakpower = peakpower;
       }
    }

    public Month[] getMonths() {
        return months;
    }

    public List<SimpleInverter> getInverters() {
        return inverters;
    }

    public boolean isTosort() {
        return tosort;
    }
}
