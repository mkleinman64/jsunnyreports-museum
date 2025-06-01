/*
 * MonthSortByKwh.java
 *
 * Created on 21 oktober 2009, 19:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.mk.jsunnyreports.renderers.tables.sorters;

import java.util.*;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
/**
 *
 * @author Martijn
 */
public class MonthSortByKwh implements Comparator<Month> {
    
    /** Creates a new instance of MonthSortByKwh */
    public MonthSortByKwh() {
    }
    public int compare(Month o1, Month o2) {
        if( o1.getWh()-o2.getWh()<0) return 1;
        else return -1;
    }
    
}
