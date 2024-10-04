package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.Calendar;

import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;

/**
 * TimeEntry.java
 *
 * @version 1.3.2.0
 * @since   0.0.0.1
 *
 */
public class TimeEntry implements Serializable {
    public TimeEntry() {
    }

    public TimeEntry( int hour, int minute, int second , float newWatt, float newWhCummulative) {
        this.hour = (byte)hour;
        this.minute = (byte)minute;
        this.second = (byte)second;
        this.watt = newWatt;
        this.whCummulative = newWhCummulative;
    }    

 //   private Calendar timeEntry; 
    
    private byte hour;
    private byte minute;
    private byte second;
    
    private float watt; // watt at that moment
    private float whCummulative; // cummulative wh on this timentry. last timeentry = wh for that day.

    @SuppressWarnings("compatibility:862999156841106750")
    static final long serialVersionUID = -8755073383871303510L;

    public void setWatt(float watt) {
        this.watt = watt;
    }

    public float getWatt() {
        return watt;
    }

    // only used for setting 0 value when > 0 after sun has set.
    public void setWhCummulative(float whCummulative) {
        this.whCummulative = whCummulative;
    }

    public float getWhCummulative() {
        return whCummulative;
    }

    public float getkWhCummulative() {
        return whCummulative / 1000f;
        
    }

    public byte getHour() {
        return hour;
    }

    public byte getMinute() {
        return minute;
    }

    public byte getSecond() {
        return second;
    }
    
    public int getTotalSeconds() {
        return ( hour * 3600 ) + (  minute * 60 ) + second;
    }

    public void setHour(int hour) {
        this.hour = (byte)hour;
    }

    public void setMinute(int minute) {
        this.minute = (byte)minute;
    }

    public void setSecond(int second) {
        this.second = (byte)second;
    }
}
