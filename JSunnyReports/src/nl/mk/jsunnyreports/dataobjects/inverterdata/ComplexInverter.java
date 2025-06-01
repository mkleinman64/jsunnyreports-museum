package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * ComplexInverter.java
 *
 * @version 2.5.0
 * @since   0.0.0.1
 *
 */
public class ComplexInverter implements Comparable, Serializable {
    public ComplexInverter() {
    }

    public ComplexInverter(String newInverterName, int wp, int kwhkwp) {
        
        // default allocation of 150 timeEntries. This usually is enough to accomodate a whole day. So no relocations are necessary for the arraylist.
        List<TimeEntry> timeEntries = new ArrayList<TimeEntry>(150);
        this.timeEntries = timeEntries;

        this.name = newInverterName;
        this.wp = wp;
        this.kwhkwp = kwhkwp;
        this.peakPower = 0f;
        this.hasHadTimeEntries = false;

        setCacheModified(true);

    }

    private String name; 
    private int wp; 
    private int wh; 
    private int energyCost; 
    private int energyIncentive; 
    private int kwhkwp; 

    private List<TimeEntry> timeEntries;    // Watt entries for this inverter              ( if available )
    private boolean hasHadTimeEntries;      // Has Had TimeEntries, for correct generation of month html tables.
    private float peakPower;                // Peakpower for a given day for this inverter.

    // when object is loaded from the cache the default value is false.
    private transient boolean cacheModified = false;

    @SuppressWarnings("compatibility:-520370614128637514")
    static final long serialVersionUID = -7468194881494774444L;


    // used for sorting the inverters by installationsize..
    @Override
    public int compareTo(Object otherInverter) {
        if (!(otherInverter instanceof ComplexInverter)) {
            throw new ClassCastException("ComplexInverter is expected");
        } else {
            int inverterCompareWp = ((ComplexInverter)otherInverter).getWp();
            return inverterCompareWp - this.wp;
        }
    }

    public float getkWh() {
        return wh / 1000f;
    }

    public float getSavings() {
        return ( energyCost + energyIncentive ) / (10000f * 1000f);
    }  
    
    public boolean hasDetailData() {
        if ( timeEntries.size() > 0 ) {
            return true;
        } else {
            return false; 
        }
        
    }

    /* Setters and getters below here */
    public String getName() {
        return name;
    }

    public void setWp(int wp) {
        this.wp = wp;
    }

    public int getWp() {
        return wp;
    }

    public void setWh(int wh) {
        this.wh = wh;
    }

    public int getWh() {
        return wh;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyIncentive(int energyIncentive) {
        this.energyIncentive = energyIncentive;
    }

    public int getEnergyIncentive() {
        return energyIncentive;
    }

    public void setKwhkwp(int kwhkwp) {
        this.kwhkwp = kwhkwp;
    }

    public int getKwhkwp() {
        return kwhkwp;
    }

    public void setTimeEntries(List<TimeEntry> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public List<TimeEntry> getTimeEntries() {
        return timeEntries;
    }

    public void setCacheModified(boolean cacheModified) {
        this.cacheModified = cacheModified;
    }


    public void setPeakPower(float peakPower) {
       if ( peakPower < Float.MAX_VALUE ) {
          this.peakPower = peakPower;
       }
       
    }

    public float getPeakPower() {
        return peakPower;
    }


    public boolean isCacheModified() {
        return cacheModified;
    }

    public void setHasHadTimeEntries(boolean hasHadTimeEntries) {
        this.hasHadTimeEntries = hasHadTimeEntries;
    }

    public boolean getHasHadTimeEntries() {
        return hasHadTimeEntries;
    }
    
    

}
