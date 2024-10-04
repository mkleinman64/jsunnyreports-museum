package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.dataobjects.invertersumdata.MergedInverterData;
import nl.mk.jsunnyreports.dataobjects.costdata.CostEntries;

import nl.mk.jsunnyreports.geo.sun.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

/**
 * InverterData.java
 *
 * Main object to hold all the data gathered by all inverters.
 *
 * @author Martin Kleinman
 * @author Martijn van der Pauw
 * @version 2.5.0
 * @since 0.0.1.0
 *
 */
public class InverterData implements Serializable {
    private static final Logger log = Logger.getLogger(InverterData.class);

    public InverterData() {
        this.years = new ArrayList<Year>();
        this.costEntries = new CostEntries();
        this.costLastUpdate = this.costEntries.getLastModified();
        this.topDayInverters = new TopDayInverters();
        this.yearsWithData = new YearsWithData();
    }
    private final List<Year> years;

    private TopDayInverters topDayInverters;
    private CostEntries costEntries;

    private transient boolean updated = false;
    private transient boolean tosort = false;
    private transient List<String> updatedInverters;

    private transient YearsWithData yearsWithData;

    private long costLastUpdate;


    @SuppressWarnings("compatibility:6737091922749411256")
    static final long serialVersionUID = -8445719354913693270L;


    /**
     * Add an entry to the UpdatedInverter list. This list is used for creating the kwp and kwpyear graphs. ( optimalization )
     *
     * @param inverterName invertername to add to the modified list.
     */
    private void addUpdatedInverter(String inverterName) {
        boolean found = false;

        if (updatedInverters == null) {
            updatedInverters = new ArrayList<String>();
        }

        for (String s : updatedInverters) {
            if (s.equals(inverterName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            updatedInverters.add(inverterName);
        }
    }

    /**
     * Purges future data from the dataset, e.g. data that should not have existed in the first place.
     */
    public void purgeFuture() {
        Calendar today = Calendar.getInstance(Constants.getLocalTimeZone());

        int yearNum = today.get(Calendar.YEAR);
        int monthNum = today.get(Calendar.MONTH) + 1;
        int dayNum = today.get(Calendar.DAY_OF_MONTH);

        Year year = getYear(yearNum);
        if (year != null) {

            for (Month m : year.getMonths()) {
                if (m.getMonth() == monthNum) {
                    for (int i = (dayNum + 1); i <= m.getMaxDay(); i++) {
                        System.out.println("these days should be removed if available. this feature is not implemented yet ");
                    }
                }
                if (m.getMonth() > monthNum) {
                    System.out.println("Step 1: Going to remove the months in the future.");
                    Month newMonth = new Month(m.getMonth(), year);
                    year.getMonths()[m.getMonth() - 1] = newMonth;
                    year.setCacheModified(true);
                    this.setUpdated(true);
                }
            }
        }
    }

    /**
     * Returns the average historic value for a day.
     * E.g. When 01-01-2016 is queried and there is data present from 01-01-2010 onwards then the kWhValues for days
     * 01-01-2010,
     * 01-01-2011,
     * 01-01-2012,
     * 01-01-2013,
     * 01-01-2014,
     * 01-01-2015
     * will be averaged.
     *
     * Will run {@link #getAverageHistoricYieldForADay(int, int) getAverageHistoricYieldForADay} for the actual
     * calculation.
     *
     * @param theDay Calendar
     * @return The history average value for a day ( excluding the current day ).
     */
    public float getAverageHistoricYieldForADay(Calendar theDay) {

        int month = theDay.get(Calendar.MONTH) + 1;
        int day = theDay.get(Calendar.DAY_OF_MONTH);

        return getAverageHistoricYieldForADay(month, day);
    }

    /**
     * Returns the average historic value for a day.
     * E.g. When 01-01-2016 is queried and there is data present from 01-01-2010 onwards then the kWhValues for days
     * 01-01-2010,
     * 01-01-2011,
     * 01-01-2012,
     * 01-01-2013,
     * 01-01-2014,
     * 01-01-2015
     * will be averaged.
     *
     * @param month
     * @param day
     * @return The history average value for a day ( excluding the current day ).
     */
    public float getAverageHistoricYieldForADay(int month, int day) {
        float returnValue = 0;

        int amount = 0;

        for (Year y : years) {
            if (y.getYear() != years.get(years.size() - 1).getYear()) {
                Month m = y.getMonth(month);
                if (m != null) {
                    Day d = m.getDay(day);
                    if (d != null) {
                        returnValue = returnValue + d.getkWh();
                        amount++;
                    }
                }
            }
        }
        if (amount > 0) {
            returnValue = returnValue / amount;
        }
        return returnValue;
    }


    /**
     * this method returns the year specified by the intValue
     * if the year is not found null is returned.
     *
     * @param yearNum The year to get
     * @return year
     */
    public Year getYear(int yearNum) {
        Year returnValue = null;
        for (Year y : years) {
            if (y.getYear() == yearNum) {
                return y;
            }
        }
        return returnValue;
    }

    /**
     * this method returns the year specified by the intValue
     * if the year is not found null is returned.
     *
     * @param     yearNum The year to get ( and/or add )
     * @return    year
     */
    public Year getAndAddYear(int yearNum) {
        Year returnValue = null;
        for (Year y : years) {
            if (y.getYear() == yearNum) {
                return y;
            }
        }

        // does not exist apparently
        returnValue = new Year(yearNum);
        years.add(returnValue);
        tosort = true;
        return returnValue;
    }

    /**
     * Get the previous year before the current yearNum.
     *
     * @param yearNum current year
     * @return yearNum - 1
     * @since 1.1.0.0beta
     */
    public Year getPreviousYear(int yearNum) {
        Year returnValue = null;

        for (Year y : years) {
            if (y.getYear() == (yearNum - 1)) {
                return y;
            }
        }
        return returnValue;
    }

    /**
     * Cannot give a NPE because before this method is called the set has been checked if there are years.
     *
     * @return Newest year in the set.
     *
     * @since 1.1.2.0
     */
    public Year getLatestYear() {
        return years.get(years.size() - 1);
    }

    /**
     *
     * @return all the years.
     */
    public List<Year> getYears() {
        return years;
    }

    /**
     *
     * @return amount of Wp installed currently ( last known entry for the latest day. )
     *
     * 1.5.0.0 proof. will get the Wp of the last day loaded and will work with that.
     */
    public float getCurrentInstalledWp() {
        Year y = getLatestYear();
        Month m = y.getLatestMonthWithData();
        Day d = m.getLatestDayWithData();
        return d.getInstalledWp();
    }

    /**
     * This method is used to test whether our resultset contains information. of no information is in the list then no graphs
     * can be rendered. This is usually the case when the path in baseInverter.conf is wrong.
     *
     * @return true = has information, false = has no information.
     */
    public boolean hasData() {
        boolean returnValue = false;
        if (years.size() > 0) {
            returnValue = true;
        }
        return returnValue;
    }

    /**
     *
     * this method determines the total yield for a given period in time for one specific baseInverter.
     *
     * @param fromDate fromdate from which the yield is added ( including this date )
     * @param tillDate tilldate to which the yield is added ( including this date )
     * @param inverterName Inverter 
     * @return yield    totalyield for this period, for all tested inverters.
     */
    public int getYieldInPeriod(Calendar fromDate, Calendar tillDate, String inverterName) {
        int returnValue = 0;
        // passed by reference, need to have a seperate copy.
        Calendar from = Calendar.getInstance();
        from.set(fromDate.get(Calendar.YEAR), fromDate.get(Calendar.MONTH), fromDate.get(Calendar.DAY_OF_MONTH));

        Calendar till = Calendar.getInstance();
        till.set(tillDate.get(Calendar.YEAR), tillDate.get(Calendar.MONTH), tillDate.get(Calendar.DAY_OF_MONTH));

        outerloop:
        for (Year y : years) {
            // optimalisation, we only need the years available fromDate --> tillDate
            if (y.getYear() >= from.get(Calendar.YEAR) && y.getYear() <= till.get(Calendar.YEAR)) {

                for (Month m : y.getMonths()) {
                    // if the whole month is before the startdate then ignore this month!
                    if (!m.getLastCalendarDay().before(from)) {

                        for (Day d : m.getDays()) {
                            if (d.getCalendarDate().after(from) && d.getCalendarDate().before(till)) {
                                if (d.getInverter(inverterName) != null) {
                                    returnValue = returnValue + d.getInverter(inverterName).getWh();
                                }
                            }
                            // rest of the set can be ignored, hopefully saving some time.
                            if (d.getCalendarDate().after(till)) {
                                break outerloop;
                            }
                        }
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     *
     * get the first date for an baseInverter when it was activated ( first yield ).
     *
     * @param inverterName
     * @return The date on which the inverter was first found in the dataset.
     */
    public Calendar getFirstCalendarEntryForInverter(String inverterName) {
        Calendar returnValue = null;
        loop:
        for (Year y : years) {
            for (Month m : y.getMonths()) {
                for (Day d : m.getDays()) {
                    if (d.getInverter(inverterName) != null && d.getInverter(inverterName).getWh() > 0) {
                        returnValue = d.getCalendarDate();
                        break loop;
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     * Adds an entry ( Watt ) to the set for a specfic inverter
     *
     * This is done by first selecting the right year, month, day and inverter
     * Then the inverter is located or added to the set.
     * Final step is to create a specific timeEntry and add various values that are calculated in this method.
     *
     *
     * @param yearNum
     * @param monthNum
     * @param dayNum
     * @param hour
     * @param minute
     * @param second
     * @param inverter
     * @param watt
     * @param yearToAdd The year now in scope to add ( first run with an invalid cache only )
     * @return boolean if the value was added to the set or not.
     */
    public synchronized boolean addWattEntryForInverter(int yearNum, int monthNum, int dayNum, int hour, int minute, int second, BaseInverter inverter, float watt, int yearToAdd) {
        boolean added = false;

        if (yearNum == yearToAdd || yearToAdd == 0) {

            // not the best code, this is a significant performance hit
            // every watt entry that is loaded a cal instance is set ( 400+ bytes )
            // and then not used anymore.
            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
            cal.set(Calendar.YEAR, yearNum);
            cal.set(Calendar.MONTH, monthNum - 1);
            cal.set(Calendar.DAY_OF_MONTH, dayNum);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);

            if (cal.before(inverter.getM_FromDate())) {
                SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                log.warn("entry   : " + format1.format(cal.getTime()));
                log.warn("inverter: " + format1.format(inverter.getM_FromDate().getTime()));
                log.warn("Warning! I am loading data before the startdate set in inverters.conf ( " + inverter.getM_InverterName() + " ), check your startdate for your inverters!");
            }

            added = true;
            Year year = this.getAndAddYear(yearNum);
            Month month = year.getMonth(monthNum);
            Day day = month.getDay(dayNum);

            ComplexInverter ci = day.getAndAddInverter(inverter);

            // test if this fixes the kwhyear and kwh charts.
            addUpdatedInverter(inverter.getM_InverterName());

            float WhCummulative = 0f;

            if (ci.hasDetailData()) {
                int totalSeconds = (hour * 3600) + (minute * 60) + second;
                TimeEntry previousEntry = ci.getTimeEntries().get(ci.getTimeEntries().size() - 1);
                int differenceInSeconds = totalSeconds - previousEntry.getTotalSeconds();

                // because of the correctionfactor we have to use a temporary float.
                float tempWh = watt * (differenceInSeconds / 3600f) * inverter.getO_CorrectionFactor();
                WhCummulative = previousEntry.getWhCummulative() + tempWh; // assuming interval is always < 1 hour
            }
            TimeEntry t = new TimeEntry(hour, minute, second, watt, WhCummulative);

            if (watt > ci.getPeakPower()) {
                ci.setPeakPower(watt);
            }

            ci.getTimeEntries().add(t);

            day.setCacheModified(true);
            ci.setCacheModified(true);

            updated = true;

        }
        return added;

    }

    /**
     * Adds an entry ( Wh ) to the dataset for a specfic inverter.
     * Overwrites the value taken from the TimeEntries page ( as they might differ )
     * The actual value the inverter delivers is always more accurate.
     *
     * @param yearNum
     * @param monthNum
     * @param dayNum
     * @param inverter
     * @param whEntry
     * @param yearToAdd yearToAdd The year now in scope to add ( first run with an invalid cache only )
     */
    public synchronized void addDayYieldForInverter(int yearNum, int monthNum, int dayNum, BaseInverter inverter, int whEntry, int yearToAdd) {
        if (yearNum == yearToAdd || yearToAdd == 0) {

            // not the best code, this is a significant performance hit
            // every watt entry that is loaded a cal instance is set ( 400+ bytes )
            // and then not used anymore.
            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
            cal.set(Calendar.YEAR, yearNum);
            cal.set(Calendar.MONTH, monthNum - 1);
            cal.set(Calendar.DAY_OF_MONTH, dayNum);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            if (cal.before(inverter.getM_FromDate())) {
                SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                log.warn("entry   : " + format1.format(cal.getTime()));
                log.warn("inverter: " + format1.format(inverter.getM_FromDate().getTime()));
                log.warn("Warning! I am loading data before the startdate set in inverters.conf, check your startdate for your inverters!");
            }
            Year year = this.getAndAddYear(yearNum);
            Month month = year.getMonth(monthNum);
            Day day = month.getDay(dayNum);
            ComplexInverter inverterDetail = day.getAndAddInverter(inverter);
            addUpdatedInverter(inverter.getM_InverterName());

            int yield = (int) (whEntry * inverter.getO_CorrectionFactor());

            int currentWh = inverterDetail.getWh();

            inverterDetail.setWh(yield);
            inverterDetail.setEnergyCost(costEntries.getEnergyCosts(yearNum, monthNum, dayNum) * yield);
            inverterDetail.setEnergyIncentive(costEntries.getEnergyIncentive(yearNum, monthNum, dayNum) * yield);

            if (currentWh > 0) {
                // substract the appropriate amount from the day / month / year . through day everything is propagated to month and year.
                // only necessary when != 0f.
                day.subValues(inverter, currentWh);
            }

            // add the appropriate amount to the day / month / year .
            day.addValues(inverter, inverterDetail.getWh());

            updated = true;

        }


    }

    /**
     *
     * @param date Will add a specific year to the dataset.
     */
    public synchronized void addInitYearSet(Calendar date) {
        int yearNum = date.get(Calendar.YEAR);
        yearsWithData.addYear(yearNum);
    }

    /**
     * Gets a month using a yearNum and monthNum; Used for the previous and next month links ( if they exist ).
     *
     * @param yearNum
     * @param monthNum
     * @return Month object, null when not found.
     */
    public Month getMonthByYearAndMonth(int yearNum, int monthNum) {
        Month month = null;

        if (this.getYear(yearNum) != null) {
            month = this.getYear(yearNum).getMonth(monthNum);
        }
        return month;
    }

    /**
     * Finalizes the dataset. checks all the information in de dataset and checks whether an update has to be made on
     * a day which has timeEntries. ( usually this is the current day! ). This will correct the day value with the value in ComplexInverters
     *
     * This information is added to all appropriate objects
     *
     *
     * @param inverters The inverterlist to finalize the data for.
     * @since 1.0.0.0
     */
    @SuppressWarnings("unchecked")
    private void finalizeSet(InverterList inverters) {
        if (tosort) {
            Collections.sort(this.getYears());
        }
        for (Year y : years) {
            for (Month m : y.getMonths()) {
                for (Day d : m.getDays()) {
                    if (d.isTosort()) {
                        Collections.sort(d.getInverters());
                    }
                    // from this point on we now know that all days in this month up to the current day are in the set.
                    // now check if this inverter is present in every day
                    // getting it like below will automatically add it. .

                    // below code will test if there is a difference between time-entries and the Wh of the day.
                    for (ComplexInverter ci : d.getInverters()) {
                        if (ci.hasDetailData()) {
                            BaseInverter inv = inverters.getInverter(ci.getName());

                            TimeEntry lastTimeEntry = ci.getTimeEntries().get(ci.getTimeEntries().size() - 1);

                            // minor modification, always check complexInverters with data and make sure the day structure is in sync.
                            // remove the amount of Wh from the whole structure.
                            // but only if the WhCum from the detailData is larger than the dayValue .
                            if (lastTimeEntry.getWhCummulative() > ci.getWh()) {

                                d.subValues(inv, ci.getWh());

                                // determine the right value from the timeEntry structure.
                                float WhCummulative = lastTimeEntry.getWhCummulative();
                                int costs = (int) (costEntries.getEnergyCosts(y.getYear(), m.getMonth(), d.getDay()) * WhCummulative);
                                int incentive = (int) (costEntries.getEnergyIncentive(y.getYear(), m.getMonth(), d.getDay()) * WhCummulative);

                                //System.out.println("Wh: " + WhCummulative + " costs: " + costs + " incentive:" + incentive);

                                // update the complexInverter structure.
                                ci.setWh((int) WhCummulative);
                                ci.setEnergyCost(costs);
                                ci.setEnergyIncentive(incentive);

                                // add the appropriate amount to the day / month / year .
                                d.addValues(inv, ci.getWh());

                            }
                            // all days with data should be in sync now.
                        }
                    }
                }
                Collections.sort(m.getInverters());
            }
            Collections.sort(y.getInverters());
        }
    }

    /**
     * MergeTimeEntryData for one cummulated graph, only for people with multiple inverters.
     */
    public void mergeTimeEntryData() {
        for (Year y : years) {
            if (y.isCacheModified()) {
                for (Month m : y.getMonths()) {
                    if (m.isCacheModified()) {
                        for (Day d : m.getDays()) {


                            if (d.isCacheModified() && d.hasTimeEntryData()) {
                                d.setMergedData(new MergedInverterData());
                                for (ComplexInverter ci : d.getInverters()) {
                                    if (ci.hasDetailData()) {
                                        d.getMergedData().addInverterTimeEntries(ci.getTimeEntries());
                                    }
                                }
                                if (d.getInverters().size() > 1) {
                                    d.getMergedData().doShrink();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets lat and lon and determines if it is after sunset.
     *
     */
    private void updateSunSet(Settings settings) {
        // its the current day!
        if (settings.getGpsLocation().isValidValue()) {
            SunriseSunsetCalculator sunCalc = new SunriseSunsetCalculator(settings.getGpsLocation().getLocation(), Constants.getLocalTimeZone());
            // These are used to calculate todays sunset!
            Calendar now = Calendar.getInstance(Constants.getLocalTimeZone());
            Calendar todaysSunset = sunCalc.getOfficialSunsetCalendarForDate(Calendar.getInstance(Constants.getLocalTimeZone()));

            // determine what is yesterday and set it to 23:59:59
            Calendar yesterday = (Calendar) now.clone();
            yesterday.add(Calendar.DATE, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 23);
            yesterday.set(Calendar.MINUTE, 59);
            yesterday.set(Calendar.SECOND, 59);

            for (Year y : years) {
                for (Month m : y.getMonths()) {
                    for (Day d : m.getDays()) {
                        for (ComplexInverter ci : d.getInverters()) {
                            if (ci.hasDetailData()) {
                                TimeEntry lastTimeEntry = ci.getTimeEntries().get(ci.getTimeEntries().size() - 1);

                                Calendar lastCal = Calendar.getInstance();
                                lastCal.set(Calendar.YEAR, y.getYear());
                                lastCal.set(Calendar.MONTH, m.getMonth() - 1);
                                lastCal.set(Calendar.DAY_OF_MONTH, d.getDay());
                                lastCal.set(Calendar.HOUR_OF_DAY, lastTimeEntry.getHour());
                                lastCal.set(Calendar.MINUTE, lastTimeEntry.getMinute());
                                lastCal.set(Calendar.SECOND, lastTimeEntry.getSecond());

                                if (lastTimeEntry.getWatt() > 0f) {
                                    Calendar teClone = Calendar.getInstance();
                                    teClone.set(Calendar.YEAR, y.getYear());
                                    teClone.set(Calendar.MONTH, m.getMonth() - 1);
                                    teClone.set(Calendar.DAY_OF_MONTH, d.getDay());
                                    teClone.set(Calendar.HOUR_OF_DAY, lastTimeEntry.getHour());
                                    teClone.set(Calendar.MINUTE, lastTimeEntry.getMinute());
                                    teClone.set(Calendar.SECOND, lastTimeEntry.getSecond());

                                    Calendar sunset = sunCalc.getOfficialSunsetCalendarForDate(teClone);

                                    if ((lastCal.before(yesterday)) || (lastCal.before(sunset) && now.after(todaysSunset))) {
                                        //log.info("Current power for the last entry we've got for this day is: " + lastTimeEntry.getWatt());
                                        //log.info("Sunset for this day is : " + stringSet);
                                        //log.info("NOT ZERO: DOING ZERO_ING! - Now it is time to do the sunset thingie and let this day end at 0.");

                                        float calcWatt = lastTimeEntry.getWatt();
                                        int i = 1;
                                        while (calcWatt > 0) {
                                            calcWatt = calcWatt - 40f;
                                            if (calcWatt < 0f) {
                                                calcWatt = 0f;
                                            }
                                            Calendar newCal2 = (Calendar) lastCal.clone();
                                            newCal2.add(Calendar.SECOND, (120 * i));

                                            TimeEntry nulEntry = new TimeEntry();
                                            nulEntry.setHour(newCal2.get(Calendar.HOUR_OF_DAY));
                                            nulEntry.setMinute(newCal2.get(Calendar.MINUTE));
                                            nulEntry.setSecond(newCal2.get(Calendar.SECOND));
                                            nulEntry.setWatt(calcWatt);
                                            nulEntry.setWhCummulative(lastTimeEntry.getWhCummulative());
                                            ci.getTimeEntries().add(nulEntry);

                                            i++;
                                        }
                                        d.setCacheModified(true);
                                        m.setCacheModified(true);
                                        y.setCacheModified(true);
                                        ci.setCacheModified(true);
                                        this.setUpdated(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @since 1.3.2.0
     */
    public void updateCosts() {
        int costs;
        int incentive;
        for (Year y : years) {
            for (Month m : y.getMonths()) {
                for (Day d : m.getDays()) {
                    for (ComplexInverter ci : d.getInverters()) {
                        costs = costEntries.getEnergyCosts(d.getCalendarDate()) * ci.getWh();
                        incentive = costEntries.getEnergyIncentive(d.getCalendarDate()) * ci.getWh();
                        ci.setEnergyCost(costs);
                        ci.setEnergyIncentive(incentive);
                    }
                }
            }
        }
    }

    /**
     *
     * Removes a day from the cache structure.
     *
     * @param inverter Inverter to remove day from
     * @param date     Date to remove.
     */
    public synchronized void removeDayFromSet(BaseInverter inverter, Calendar date) {
        int yearNum = date.get(Calendar.YEAR);
        byte monthNum = (byte) (date.get(Calendar.MONTH) + 1);
        byte dayNum = (byte) date.get(Calendar.DAY_OF_MONTH);

        Year year = this.getYear(yearNum);

        addUpdatedInverter(inverter.getM_InverterName());

        if (year != null) {
            Month month = year.getMonth(monthNum);

            if (month != null) {
                Day day = month.getDay(dayNum);

                if (day != null) {
                    // only going to delete it if it exists.
                    if (day.getInverter(inverter.getM_InverterName()) != null) {
                        ComplexInverter inverterDetail = day.getAndAddInverter(inverter);

                        // substract the appropriate amount from the day / month / year .
                        day.subValues(inverter, inverterDetail.getWh());

                        // reset the inverter and remove timeEntries.
                        day.removeInverter(inverterDetail);
                    }
                }
            }
        }
    }

    /**
     *
     * @param inverter
     * @param yearNum
     * @param monthNum
     */
    public void removeMonthFromSet(BaseInverter inverter, int yearNum, int monthNum) {
        Year year = getYear(yearNum);

        if (year != null) {
            addUpdatedInverter(inverter.getM_InverterName());
            Month month = year.getMonth(monthNum);
            if (month != null) {
                // substract the correct values from the day specific for this inverter.
                for (Day d : month.getDays()) {
                    this.removeDayFromSet(inverter, d.getCalendarDate());
                }
            }
        }
    }
    
    /**
     *
     * @param inverter
     * @param date
     */
    public void removeMonthFromSet(BaseInverter inverter, Calendar date) {
        int yearNum = date.get(Calendar.YEAR);
        byte monthNum = (byte) (date.get(Calendar.MONTH) + 1);

        removeMonthFromSet(inverter, yearNum, monthNum);

    }

    /**
     * Debug only, print the actual dataset of all inverters to test if the information is correct.
     *
     * @since 0.9.0.0beta
     */
    public void printDataSet() {
        /* Debug code to test if the dataset is correctly created */

        //int timeEntries = 0;

        int mergedEntries = 0;

        System.out.println("Dataset: ");


        for (Year y : years) {
            System.out.println("Year : " + y.getYear() + " pp: " + y.getPeakpower());
            for (SimpleInverter i : y.getInverters()) {
                System.out.println("Inverter for Year : " + i.getName() + " wh: " + i.getWh() + " Wp: " + i.getWp());
            }

            for (Month m : y.getMonths()) {
                System.out.println("Month: " + m.getMonth() + " wh: " + m.getWh() + " pp: " + m.getPeakpower());
                for (SimpleInverter i : m.getInverters()) {
                    System.out.println("Inverter for Month : " + i.getName() + " wh: " + i.getWh() + " Wp: " + i.getWp());
                }


                for (Day d : m.getDays()) {
                    System.out.println("   Date : " + d.getDay() + "-" + m.getMonth() + "-" + y.getYear() + " -> kWh(day) : " + d.getWh() + " pp: " + d.getPeakpower());
                    //  System.out.println("   cDate : " + d.getCalendarDate().get(Calendar.DAY_OF_MONTH) + "-" + (d.getCalendarDate().get(Calendar.MONTH) + 1) + "-" + d.getCalendarDate().get(Calendar.YEAR));

                    System.out.println(" Day mergedSize: " + d.getMergedData()
                                                              .getDayData()
                                                              .size());

                    mergedEntries = mergedEntries + d.getMergedData()
                                                     .getDayData()
                                                     .size();

                    /*for (ComplexInverter i : d.getInverters()) {
                        System.out.println("    Inverter found : " + i.getName() + " kwh(inv) : " + i.getWh() + " money: " + i.getEnergyCost() + " size: " + i.getTimeEntries().size() );

                        timeEntries = timeEntries + i.getTimeEntries().size();

                        //if (i.getTimeEntries() != null && i.getTimeEntries().size() > 0) {
                        //System.out.println("Day : " + d.getWh() + " inverter : " + i.getTimeEntries().get(i.getTimeEntries().size() - 1).getWhCummulative());

                        //for (TimeEntry t : i.getTimeEntries()) {
                        //     System.out.println("      " + t.getTimeEntry().get(Calendar.HOUR_OF_DAY) + ":" + t.getTimeEntry().get(Calendar.MINUTE) + ":" + t.getTimeEntry().get(Calendar.SECOND) + " : " + t.getWatt() + " kWhCumm : " + t.getKWhCummulative());
                        //}
                        // }
                    } */
                }
            }
        }

        // System.out.println( "Total timeEntries in memory: " + timeEntries );
        System.out.println("Total mergedENtries in memory: " + mergedEntries);


    }

    /**
     * Go through the dataset once again and set all peak powers.
     */
    private void determinePeakPower() {

        // Step 1. Going to update the peakPowers for modified data.
        for (Year y : years) {
            if (y.isCacheModified()) {
                for (Month m : y.getMonths()) {
                    if (m.isCacheModified()) {
                        for (Day d : m.getDays()) {
                            if (d.isCacheModified()) {
                                // update: check if one of the inverters had its timeentries removed.
                                // if so we are not going to update the peakpower. Not all data is available in this case.
                                int purgedInverters = 0;
                                for (ComplexInverter c : d.getInverters()) {
                                    if (c.getHasHadTimeEntries()) {
                                        purgedInverters++;
                                    }
                                }

                                if (purgedInverters == 0) {
                                    // for every inverter the timeseries are available, in this case we can use the mergedPeakPower.
                                    d.setPeakpower(d.getMergedData().getMergedPeakPower());
                                }
                            }
                        }
                    }
                }
            }
        }

        // going to do a "big" task.
        // regardless of years and months and the cachesettings
        // we are going to update ALL peakpowers. For 10 years this is a set of 10x12 records. so should be blazing fast.
        //
        // STEP 2. Now data is updated, update the rest of the structure as well.
        for (Year y : years) {
            float maxMonthPower = 0f;
            for (Month m : y.getMonths()) {
                float maxDayPower = 0f;
                for (Day d : m.getDays()) {
                    if (d.getPeakpower() > maxDayPower) {
                        maxDayPower = d.getPeakpower();
                    }
                }
                // month is now known.
                if (m.getPeakpower() != maxDayPower) {
                    m.setPeakpower(maxDayPower);

                }
                if (m.getPeakpower() > maxMonthPower) {
                    maxMonthPower = m.getPeakpower();
                }

            }
            if (y.getPeakpower() != maxMonthPower) {
                y.setPeakpower(maxMonthPower);
            }
        }
    }


    /**
     * This method determines the top day for every baseInverter
     *
     */
    private void determineTopDays() {
        String dayString;
        for (Year y : years) {
            for (Month m : y.getMonths()) {
                for (Day d : m.getDays()) {
                    dayString = d.getDay() + "-" + m.getMonth() + "-" + y.getYear();
                    for (ComplexInverter i : d.getInverters()) {
                        TopDay tdInverter = this.topDayInverters.getInverterTopDay(i.getName());

                        // Test if we found a better day.
                        if (i.hasDetailData() && i.getWh() > tdInverter.getWh()) {
                            // we found a better one.
                            tdInverter.setBestDayString(dayString);
                            tdInverter.setWh(i.getWh());
                            tdInverter.setTimeEntries(i.getTimeEntries());
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     *
     * @param il
     * @param s
     */
    public void postProcessing(InverterList il, Settings s) {
        finalizeSet(il);
        updateSunSet(s);
        determineTopDays();
        mergeTimeEntryData();
        determinePeakPower();
    }

    /* Get and set below. */

    public void setUpdated(boolean isUpdated) {
        this.updated = isUpdated;
    }

    public boolean isUpdated() {
        return updated;
    }


    public TopDayInverters getTopDayInverters() {
        return topDayInverters;
    }

    public void setCostEntries(CostEntries costEntries) {
        this.costEntries = costEntries;
    }

    public long getCostLastUpdate() {
        return costLastUpdate;
    }

    public void setCostLastUpdate(long costLastUpdate) {
        this.costLastUpdate = costLastUpdate;
    }

    public List<String> getUpdatedInverters() {
        return updatedInverters;
    }

    public YearsWithData getYearsWithData() {
        return yearsWithData;
    }
}
