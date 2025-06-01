package nl.mk.jsunnyreports.dataobjects.inverters;

import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.inverters.BaseInverter;

/**
 * @author      Martin Kleinman
 * @version     2.5.0
 * @since       1.3.2.0
 * 
 */
public class InverterList {

    private List<BaseInverter> baseInverters = new ArrayList<BaseInverter>();

    public List<BaseInverter> getInverters() {
        return baseInverters;
    }

    public int getInverterCount() {
        return baseInverters.size();
    }

    public BaseInverter getInverter(String inverterName) {
        for (BaseInverter bi : baseInverters) {
            if (bi.getM_InverterName().equals(inverterName)) {
                return bi;
            }
        }
        return null; // this should not happen.
    }

    public float getExpectedDayYield(int d, int m, int y) {
        float returnValue = 0f;
        for (BaseInverter b : baseInverters) {
            returnValue = returnValue + b.getExpectedDayYield(d, m, y);
        }
        return returnValue;
    }

    public float getExpectedMonthYield(int year, int month) {
        float returnValue = 0f;
        for (BaseInverter b : baseInverters) {
            returnValue = returnValue + b.getExpectedMonthYield(year, month);
        }
        return returnValue;
    }

    public float getExpectedYearYield(int year) {
        float returnValue = 0f;
        for (BaseInverter b : baseInverters) {
            returnValue = returnValue + b.getExpectedYearYield(year);
        }
        return returnValue;
    }
}
