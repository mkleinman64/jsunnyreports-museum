/*
 * MonthRecords.java
 *
 * Created on 14 oktober 2009, 11:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.mk.jsunnyreports.renderers.tables.timerecords;

import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.DataLoader;

import nl.mk.jsunnyreports.renderers.tables.sorters.MonthSortByKwh;

import org.apache.log4j.Logger;

/**
 *
 * @author Martijn van der Pauw and Martin Kleinman 2009 - 2010
 */
public class MonthRecords {
    public MonthRecords(Settings settings) {
        this.settings = settings;
    }


    private static final Logger log = Logger.getLogger(MonthRecords.class);

    private float minrecord = 0;
    private final int LIST_LENGTH = 15;
    private Settings settings;
    private boolean sort = true;

    private List<Month> recordMonths = new ArrayList<Month>();

    public boolean checkRecord(Month newMonth) {
        if (recordMonths.size() < LIST_LENGTH || newMonth.getkWh() > minrecord) {
            if (recordMonths.size() < LIST_LENGTH)
                recordMonths.add(newMonth);
            else {
                recordMonths.remove(LIST_LENGTH - 1);
                recordMonths.add(LIST_LENGTH - 1, newMonth);
            }
            if (sort)
                Collections.sort(recordMonths, new MonthSortByKwh());
            if (recordMonths.size() >= LIST_LENGTH)
                minrecord = recordMonths.get(LIST_LENGTH - 1).getkWh();
            return true;
        }

        return false;
    }

    public List<Month> getRecordList() {
        List<Month> recordMonthsReturn = new ArrayList<Month>();
        for (int i = 0; i < recordMonths.size(); i++) {
            Month tempMonth = recordMonths.get(i);
            if (tempMonth != null && tempMonth.getkWh() > 0.1)
                recordMonthsReturn.add(tempMonth);
            //log.debug("Op plek "+i+" "+tempMonth.getKWh()+" on "+tempMonth.getMonth()+"-"+tempMonth.getParentMonth().getMonth());
        }
        return recordMonthsReturn;
    }


}
