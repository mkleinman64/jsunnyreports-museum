package nl.mk.jsunnyreports.dataobjects.invertersumdata;

import java.io.Serializable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import nl.mk.jsunnyreports.dataobjects.inverterdata.TimeEntry;

public class MergedInverterData implements Serializable {

    @SuppressWarnings("compatibility")
    private static final long serialVersionUID = 99129129129291L;

    private List<DataItem> dayData;

    public MergedInverterData() {
        dayData = new ArrayList<DataItem>(150);
    }

    public void addInverterTimeEntries(List<TimeEntry> timeEntries) {
        // twee opties, lijst is helemaal leeg, dan kunnen we hem bijna 1:1 overnemen.
        if (dayData.size() == 0) {

            for (TimeEntry t : timeEntries) {
                // -1 betekent dat we de lijst nog moeten interpoleren.
                DataItem d = new DataItem( t.getHour(), t.getMinute(), t.getSecond(), t.getWatt(), -1f);
                dayData.add(d);
            }

        } else {
            // nu moeten we een soort "MERGE" uitvoeren, hier zit het echte werk. Er is
            // dus al een lijst beschikbaar is waar we informatie aan moeten toevoegen.
            for (TimeEntry t : timeEntries) {
                DataItem item = getDateItemInList(t.getTotalSeconds());

                // exacte datumtijd bestaat dus al. nu makkelijk doen.
                if (item != null) {
                    item.setWattFromToAdd(t.getWatt());
                } else {
                    // hij bestaat nog niet, we gaan dus een nieuwe toevoegen.
                    DataItem d = new DataItem(  t.getHour(), t.getMinute(), t.getSecond(), -1f, t.getWatt());
                    dayData.add(d);
                }
            }
            Collections.sort(dayData); // en ff sorteren

            // nu moeten we beide lijsten gaan interpoleren en daarna kunnen we ze optellen.
            this.interpolateMainList();
            this.interpolateToAddList();
            this.mergeLists();
            this.finalizeList();
        }
    }

    // 3 mogelijkheden om te interpoleren
    // 1. niets X iets ==> X = 0
    // 2. ietx X iets ==> pak tussen liggende waarde en interpoleer die
    // 3. iets X niets ==> X = 0

    private void interpolateMainList() {
        DataItem previousDataItem;
        DataItem nextDataItem;
        for (DataItem d : dayData) {
            if (d.getWattFromMainList() == -1) {
                // we need todo some work here.

                // vorige waarde pakken.
                previousDataItem = getPreviousDataEntryFromMainList(d.getTotalSeconds());
                if (previousDataItem == null) {
                    // geen waarde ervoor gevonden. Dus er valt niets te interpoleren.
                    d.setWattFromMainList(0);
                } else {
                    // hier moeten we dus gaan interpoleren.
                    nextDataItem = getNextDataEntryFromMainList(d.getTotalSeconds());

                    if (nextDataItem == null) {
                        // we take the previous value as setting this to 0 makes no sense.
                        // we don't have this value yet for the main list. assume now its the previous.
                        d.setWattFromMainList(d.getWattFromMainList());
                    } else {

                        // 2. Het is de tussenliggende.
                        d.setWattFromMainList(getDifferenceMainWatt(previousDataItem, nextDataItem, d));
                        // interpoleren voltooid.
                    }
                }
            }
        }
    }

    // 3 mogelijkheden om te interpoleren
    // 1. niets X iets ==> X = 0
    // 2. ietx X iets ==> pak tussen liggende waarde en interpoleer die
    // 3. iets X niets ==> X = 0

    private void interpolateToAddList() {
        DataItem previousDataItem;
        DataItem nextDataItem;
        for (DataItem d : dayData) {
            if (d.getWattFromToAdd() == -1f) {
                // we need todo some work here.

                // vorige waarde pakken.
                previousDataItem = getPreviousDataEntryFromToAddList(d.getTotalSeconds());
                if (previousDataItem == null) {
                    // geen waarde ervoor gevonden. Dus er valt niets te interpoleren.
                    d.setWattFromToAdd(0f);
                } else {
                    // hier moeten we dus gaan interpoleren.
                    nextDataItem = getNextDataEntryFromToAddList(d.getTotalSeconds());

                    if (nextDataItem == null) {
                        d.setWattFromToAdd(0f);
                    } else {
                        // 2. Het is de tussenliggende.
                        d.setWattFromToAdd(getDifferenceToAddWatt(previousDataItem, nextDataItem, d));
                        // interpoleren voltooid.
                    }
                }
            }
        }
    }

    public DataItem getPreviousDataEntryFromMainList(int totalSeconds) {
        DataItem returnValue = null;

        int iterator = 0;
        boolean exit = false;
        while (iterator <= dayData.size() - 1 && exit == false) {
            DataItem d = dayData.get(iterator);

            // vinden we iets?
            if (d.getTotalSeconds() < totalSeconds && d.getWattFromMainList() != -1) {
                returnValue = d;
            }

            if ( d.getTotalSeconds() >= totalSeconds ) {
                exit = true;
            }

            iterator++;

        } // end while
        return returnValue;
    }

    public DataItem getPreviousDataEntryFromToAddList( int totalSeconds ) {
        DataItem returnValue = null;

        int iterator = 0;
        boolean exit = false;
        DataItem d;
        while (iterator <= dayData.size() - 1 && exit == false) {
            d = dayData.get(iterator);

            // vinden we iets?
            if (d.getTotalSeconds() < totalSeconds && d.getWattFromToAdd() != -1) {
                returnValue = d;
            }

            if ( d.getTotalSeconds() >= totalSeconds ) {
                exit = true;
            }

            iterator++;

        } // end while
        return returnValue;
    }

    private long getDifferenceInSeconds(DataItem d1, DataItem d2) {
        long seconds = (d2.getTotalSeconds() - d1.getTotalSeconds());
        return seconds;
    }

    private long getDifferenceMainWatt(DataItem d1, DataItem d2, DataItem toAddTo) {
        float difference = d2.getWattFromMainList() - d1.getWattFromMainList();
        float seconds = getDifferenceInSeconds(d1, d2);
        float secondsbetweend1andcurrent = getDifferenceInSeconds(d1, toAddTo);
        float step = difference / seconds;
        float toAddOrSubstract = step * secondsbetweend1andcurrent;

        // damn that -1
        float value = d1.getWattFromMainList() + toAddOrSubstract;
        long returnValue = (long)value;

        return returnValue;
    }

    private float getDifferenceToAddWatt(DataItem d1, DataItem d2, DataItem toAddTo) {
        float difference = d2.getWattFromToAdd() - d1.getWattFromToAdd();
        float seconds = getDifferenceInSeconds(d1, d2);
        float secondsbetweend1andcurrent = getDifferenceInSeconds(d1, toAddTo);
        float step = difference / seconds;
        float toAddOrSubstract = step * secondsbetweend1andcurrent;
        // damn that -1
        float value = d1.getWattFromToAdd() + toAddOrSubstract;
        float returnValue = value;
        return returnValue;
    }

    public DataItem getNextDataEntryFromMainList(int totalSeconds) {
        DataItem returnValue = null;
        int iterator = 0;
        boolean exit = false;
        DataItem d;
        while (iterator <= dayData.size() - 1 && exit == false) {
            d = dayData.get(iterator);

            // vinden we iets?
            if (d.getTotalSeconds() > totalSeconds && d.getWattFromMainList() != -1) {
                returnValue = d;
                exit = true;
            }
            iterator++;

        } // end while
        return returnValue;
    }

    public DataItem getNextDataEntryFromToAddList(int totalSeconds) {
        DataItem returnValue = null;
        DataItem d;
        int iterator = 0;
        boolean exit = false;
        while (iterator <= dayData.size() - 1 && exit == false) {
            d = dayData.get(iterator);

            // vinden we iets?
            if ( d.getTotalSeconds() > totalSeconds && d.getWattFromToAdd() != -1) {
                returnValue = d;
                exit = true;
            }

            iterator++;

        } // end while
        return returnValue;
    }


    public DataItem getDateItemInList( int totalSeconds ) {
        DataItem returnValue = null;
        for (DataItem d : dayData) {
            if ( d.getTotalSeconds() == totalSeconds ) {
                return d;
            }
        }
        return returnValue;
    }

    public void doPrint() {
        for (DataItem d : dayData) {
            System.out.println(d.getWattFromMainList() + " " + d.getWattFromToAdd());
        }
    }

    private void mergeLists() {
        for (DataItem d : dayData) {
            d.setWattFromMainList(d.getWattFromMainList() + d.getWattFromToAdd());
            d.setWattFromToAdd(-1f);
        }
    }

    public float getMergedPeakPower() {
        float returnValue = 0f;
        for (DataItem d : dayData) {
            if (d.getWattFromMainList() >= returnValue) {
                returnValue = d.getWattFromMainList();
            }
        }
        return returnValue;
    }
    
    private void finalizeList() {
        for ( DataItem di: dayData ) {
            if ( di.getWattFromMainList() == -1f ) {
                di.setWattFromMainList(0f);
            }
        }

    }
    
    public void doShrink() {
        // now lets do some cleaning in the list, the same as we do with normal inverters.
        List <DataItem> newList = new ArrayList<DataItem>(150);

        int averageMinute = 0;
        float averagePac = 0;
        int entries = 0;
        int workMinute = 0;

                
        for ( DataItem di: dayData ) {
            workMinute = di.getHour() * 60 + di.getMinute();
            averagePac = averagePac + di.getWattFromMainList();
            entries++;

            // interval threshold is reached. save the data and start with next set.
            if (workMinute >= (averageMinute + 3)) {

                float avgPac = (averagePac / entries);

                DataItem d = new DataItem( di.getHour(), di.getMinute(), di.getSecond(), avgPac, 0 );
                newList.add( d );

                // finalize for the next interval
                averageMinute = workMinute;
                averagePac = 0f;
                entries = 0;
            }
        }
        
        // finalize
        if ( entries > 0 ) {
            DataItem lastItem = dayData.get( dayData.size() - 1);
            float avgPac = (averagePac / entries);
            DataItem d = new DataItem( lastItem.getHour(), lastItem.getMinute(), lastItem.getSecond(), avgPac, 0 );
            newList.add( d );
        }        
        
        //System.out.println( "Before: " + dayData.size() + " after: " + newList.size() );
        
        this.dayData = newList;        
    }

    //test

/*    public static void main(String[] args) {
        System.out.println("hier");
        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        MergedInverterData isd = new MergedInverterData();

        List<TimeEntry> timeEntries = new ArrayList<TimeEntry>();
        List<TimeEntry> timeEntriesToAdd = new ArrayList<TimeEntry>();

        TimeEntry t1, t2, t3, t4;
        Calendar c1, c2, c3, c4;

        c1 = Calendar.getInstance();
        c2 = Calendar.getInstance();
        c3 = Calendar.getInstance();
        c4 = Calendar.getInstance();

        String date1, date2, date3, date4;

        date1 = "2012-10-26 10:00:00";
        date2 = "2012-10-26 11:00:00";
        date3 = "2012-10-26 12:00:00";
        date4 = "2012-10-26 13:00:00";

        try {

            c1.setTime(formatter.parse(date1));
            c2.setTime(formatter.parse(date2));
            c3.setTime(formatter.parse(date3));
            c4.setTime(formatter.parse(date4));

            t1 = new TimeEntry(c1, 1000f, 0f);
            t2 = new TimeEntry(c2, 2000f, 0f);
            t3 = new TimeEntry(c3, 100f, 0f);
            t4 = new TimeEntry(c4, 1000f, 0f);

            timeEntries.add(t1);
            timeEntries.add(t2);
            timeEntries.add(t3);
            timeEntries.add(t4);

            isd.addInverterTimeEntries(timeEntries);

            // de te interpoleren lijst.
            Calendar ct1, ct2, ct3, ct4;
            ct1 = Calendar.getInstance();
            ct2 = Calendar.getInstance();
            ct3 = Calendar.getInstance();
            ct4 = Calendar.getInstance();

            String datet1, datet2, datet3, datet4;

            datet1 = "2012-10-26 09:00:00";
            datet2 = "2012-10-26 11:00:00";
            datet3 = "2012-10-26 12:30:00";
            datet4 = "2012-10-26 13:00:00";

            ct1.setTime(formatter.parse(datet1));
            ct2.setTime(formatter.parse(datet2));
            ct3.setTime(formatter.parse(datet3));
            ct4.setTime(formatter.parse(datet4));

            TimeEntry tt1, tt2, tt3, tt4;

            tt1 = new TimeEntry(ct1, 1000f, 0f);
            tt2 = new TimeEntry(ct2, 2000f, 0f);
            tt3 = new TimeEntry(ct3, 100f, 0f);
            tt4 = new TimeEntry(ct4, 1000f, 0f);

            timeEntriesToAdd.add(tt1);
            timeEntriesToAdd.add(tt2);
            timeEntriesToAdd.add(tt3);
            timeEntriesToAdd.add(tt4);

            isd.addInverterTimeEntries(timeEntriesToAdd);


            isd.doPrint();

        } catch (ParseException pe) {

            pe.printStackTrace();

        }

    } */

    public List<DataItem> getDayData() {
        return dayData;
    }
}
