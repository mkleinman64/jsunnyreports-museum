package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.mk.jsunnyreports.dataobjects.invertersumdata.MergedInverterData;
import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.inverters.BaseInverter;

/**
 * Day.java
 *
 * @author  Martin Kleinman
 * @version 2.5.0
 * @since   0.0.0.1
 *
 */
public class Day implements Comparable, Serializable {

    /**
     *
     * @param dayNum
     * @param parentMonth
     */
    public Day(byte dayNum, Month parentMonth) {
        this.day = dayNum;
        this.parentMonth = parentMonth;
        this.setCacheModified(true);

        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
        cal.set(parentMonth.getParentYear().getYear(), (parentMonth.getMonth() - 1), dayNum);

        this.calendarDate = cal;
        this.inverters = new ArrayList<ComplexInverter>(); 
        this.mergedData = new MergedInverterData();

        this.tosort = true;
    }

    private byte day;
    private int wh; 
    private float peakpower;
    private Month parentMonth; 
    private Calendar calendarDate; 
    private List<ComplexInverter> inverters; 
    private MergedInverterData mergedData;
    
    private transient boolean cacheModified = false;
    private transient boolean tosort = false;

    @SuppressWarnings("compatibility:8139954155739196743")
    static final long serialVersionUID = 2932679482693129848L;

    @Override
    public int compareTo(Object otherDay) {
        if (!(otherDay instanceof Day)) {
            throw new ClassCastException("Day is expected");
        } else {
            int dayCompare = ((Day)otherDay).day;
            return this.day - dayCompare;
        }
    }

    public float getkWh() {
        return wh / 1000f;
    }

    public ComplexInverter getAndAddInverter(BaseInverter inverter) {
        for (ComplexInverter inverterDetail : this.inverters) {
            if (inverter.getM_InverterName().equals(inverterDetail.getName())) {
                // this change will make the dataset more consistent with the cached values.
                inverterDetail.setWp(inverter.getM_WattPeak());
                inverterDetail.setKwhkwp(inverter.getM_kWhkWp());
                return inverterDetail;
            }
        }
        // we didn't find the inverter. create a new one and add it to the set.
        ComplexInverter inverterDetail = new ComplexInverter(inverter.getM_InverterName(), inverter.getM_WattPeak(), inverter.getM_kWhkWp());
        inverters.add(inverterDetail);
        setCacheModified(true);
        tosort = true;
        return inverterDetail;

    }

    public ComplexInverter getInverter(String inverterName) {
        for (ComplexInverter inverter : this.inverters) {
            if (inverter.getName().equals(inverterName)) {
                return inverter;
            }
        }
        return null;
    }

    /**
     * This method substracts wh,cost,incentive from this day.
     *
     * @param subWh             Wh amount to add to the day
     */
    public void subValues(BaseInverter inverter, int subWh) {
        if (wh < subWh) {
            wh = 0;
        } else {
            wh = wh - subWh;
        }
        this.getParentMonth().subValues(inverter, subWh);
        this.setCacheModified(true);
    }

    /**
     * This method adds kwh,cost,incentive to this day.
     *
     * @param addWh             Wh amount to add to the day
     */
    public void addValues(BaseInverter inverter, int addWh) {
        wh = wh + addWh;
        this.getParentMonth().addValues(inverter, addWh);
        this.setCacheModified(true);
    }

    public int getInstalledWp() {
        int returnValue = 0;
        
        // the if i.getWh() should be removed I think. For a given day, when an inverter is present the Wp of this day should be added to the installed Wp.
        for (ComplexInverter i : this.getInverters()) {
            if (i.getWh() > 0) {
                returnValue = returnValue + i.getWp();
            }
        }
        return returnValue;
    }

    public boolean hasTimeEntryData() {
        for (ComplexInverter i : this.getInverters()) {
            if ( i.hasDetailData() || i.getHasHadTimeEntries() ) {
                return true;
            }
        }
        return false;
    }

    public boolean hasInverters() {
        if (inverters.size() > 0) {
            return true;
        }
        return false;

    }

    public Month getParentMonth() {
        return parentMonth;
    }

    public Calendar getCalendarDate() {
        return calendarDate;
    }

    /**
     * This method will remove one inverterDetail from the dataset.
     * New ComplexInverter is created, old one is removed ( thus removing timeentries ) and then adding the new one.
     * */
    public void removeInverter(ComplexInverter inverter) {
        ComplexInverter newCI = new ComplexInverter(inverter.getName(), inverter.getWp(), inverter.getKwhkwp());

        // remove it from the set ( the old one with data )
        inverters.remove(inverter);
        // insert the new one with correct hasHadTimeEntries.
        inverters.add(newCI);
        setCacheModified(true);
        tosort = true;
    }

    public float getSavings() {
        float returnValue = 0f;
        for (ComplexInverter ci : inverters) {
            returnValue = returnValue + ci.getSavings();
        }
        return returnValue;
    }
    
    public float getSavingsInverter( String inverterName ) {
        float returnValue = 0f;
        for (ComplexInverter ci : inverters) {
            if ( ci.getName().equals( inverterName ))
            returnValue = ci.getSavings();
            break;
        }
        return returnValue;
        
    }

    public void setCacheModified(boolean cacheModified) {
        this.cacheModified = cacheModified;
        this.getParentMonth().setCacheModified(cacheModified);
    }

    public boolean isCacheModified() {
        return cacheModified;
    }

    public byte getDay() {
        return day;
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

    public List<ComplexInverter> getInverters() {
        return inverters;
    }

    public MergedInverterData getMergedData() {
        return mergedData;
    }

    public void setMergedData(MergedInverterData mergedData) {
        this.mergedData = mergedData;
    }

    public boolean isTosort() {
        return tosort;
    }


}
