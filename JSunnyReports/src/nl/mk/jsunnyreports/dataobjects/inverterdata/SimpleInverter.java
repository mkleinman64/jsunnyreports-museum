package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

/**
 * SimpleInverter.java
 *
 * @version 1.3.2.0
 * @since   0.0.0.1
 *
 */
public class SimpleInverter implements Comparable, Serializable {
    public SimpleInverter() {
    }

    public SimpleInverter(String newInverterName, int wp, int kwhkwp) {
        this.name = newInverterName;
        this.kwhkwp = kwhkwp;
        this.wp = wp;
        this.setCacheModified(true);
    }

    private String name;
    private int wh;
    private int wp;
    private int kwhkwp;

    private transient boolean cacheModified = false;

    @SuppressWarnings("compatibility:5622353767523809455")
    static final long serialVersionUID = -960333601105147431L;

    @Override
    public int compareTo(Object otherInverter) {
        if (!(otherInverter instanceof SimpleInverter)) {
            throw new ClassCastException("SimpleInverter is expected");
        } else {
            int inverterCompare = ((SimpleInverter)otherInverter).getWp();
            return inverterCompare - this.wp;
        }
    }

    public float getkWh() {
        return wh / 1000f;
    }


    /**
     * amounts to add to this baseInverter ( summary for a month/year )
     *
     * @param addWh amount of kwh to add
     */
    public void addValues(int addWh) {
        wh = wh + addWh;
        setCacheModified(true);
    }

    /**
     * amounts to substract from this baseInverter ( summary for a month/year )
     *
     * @param subWh amount of kwh to add
     */
    public void subValues(int subWh) {
        if (wh < subWh) {
            wh = 0;
        } else {
            wh = wh - subWh;
        }
        setCacheModified(true);

    }

    /* Setters and getters below here */
    public String getName() {
        return name;
    }

    public int getWh() {
        return wh;
    }

    public void setWp(int wp) {
        this.wp = wp;
    }

    public int getWp() {
        return wp;
    }

    public void setKwhkwp(int kwhkwp) {
        this.kwhkwp = kwhkwp;
    }

    public int getKwhkwp() {
        return kwhkwp;
    }

    public void setCacheModified(boolean cacheModified) {
        this.cacheModified = cacheModified;
    }

    public boolean isCacheModified() {
        return cacheModified;
    }
}
