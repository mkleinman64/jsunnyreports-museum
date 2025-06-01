package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * TopDayInverters.java
 *
 * @version 1.3.2.0
 * @since   1.3.1.1
 *
 */
public class TopDayInverters implements Serializable {

    public TopDayInverters() {
        /*
      * This list is filled just after all data has been loaded and before the cache is optimized.
      * This list is then used in the GraphActual Renderer to get the best day and fill it with the current day.
      * Another optimalization and improvement of the code.
      *
      * Topdays are retained in the cache, the rest of the timeGraphs aren't.
      *
      * */
        List<TopDay> td = new ArrayList<TopDay>();
        this.topDayInverters = td;

    }

    @SuppressWarnings("compatibility:-6783296086781817342")
    static final long serialVersionUID = -8445712354913693271L;
    private List<TopDay> topDayInverters;

    public void setTopDayInverters(List<TopDay> topDayInverters) {
        this.topDayInverters = topDayInverters;
    }

    public List<TopDay> getTopDayInverters() {
        return topDayInverters;
    }

    /**
     *
     * @param inverterName to look for..
     * @return TopDay of this baseInverter ( or a new almost empty object )
     */
    public TopDay getInverterTopDay(String inverterName) {
        TopDay tdReturn = null;
        for (TopDay td : this.getTopDayInverters()) {
            if (td.getName().equals(inverterName)) {
                return td;
            }
        }
        // not added yet
        if (tdReturn == null) {
            tdReturn = new TopDay(inverterName);
            this.getTopDayInverters().add(tdReturn);
        }

        return tdReturn;
    }
}

