/*
 * DaySortByKwh.java
 *
 * Created on 21 oktober 2009, 19:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.mk.jsunnyreports.renderers.tables.sorters;

import java.util.*;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
/**
 *
 * @author Martijn
 */
public class DaySortByKwh implements Comparator<Day> {
    
    /** Creates a new instance of DaySortByKwh */
    public DaySortByKwh() {
    }
    public int compare(Day o1, Day o2) {
        if( o1.getkWh()-o2.getkWh()<0) return 1;
        else return -1;
    }
    
}
