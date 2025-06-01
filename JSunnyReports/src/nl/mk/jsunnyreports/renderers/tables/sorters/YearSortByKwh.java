/*
 * YearSortByKwh.java
 *
 * Created on 16 december 2009, 19:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.mk.jsunnyreports.renderers.tables.sorters;

import java.util.*;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
/**
 *
 * @author Martin Kleinman and Martijn vam der Pauw
 */
public class YearSortByKwh implements Comparator<Year> {
    
    /** Creates a new instance of MonthSortByKwh */
    public YearSortByKwh() {
    }
    public int compare(Year o1, Year o2) {
        if( o1.getWh()-o2.getWh()<0) return 1;
        else return -1;
    }
    
}
