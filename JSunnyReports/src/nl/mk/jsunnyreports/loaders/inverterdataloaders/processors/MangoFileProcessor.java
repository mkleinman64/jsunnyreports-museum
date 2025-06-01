package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.geo.sun.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;


public class MangoFileProcessor extends BaseProcessor implements Runnable {

    private static final Logger log = Logger.getLogger(MangoFileProcessor.class);

    public MangoFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.baseInverter = baseInverter;
        this.settings = settings;
        this.init = init;
        this.year = year;
        this.fc = fc;
    }


    public void run() {
        inverterData.setUpdated(true);

        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        /* used for averaging "interval" minutes of data */
        int averageMinute = 0;
        float averagePac = 0;
        int workMinute = 0;
        int lineNumber = 0;

        float avgPac;
        try {

            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(theFile)), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, 1);
            String[] nextLine;

            String date = "";
            String pAc = "";

            int entries = 0;

            // if true then data is loaded, false data is not loaded into the internal structure
            // depends on if a certain time is before ( or after ) sunrise/sunset.
            boolean doProcess = true;
            SunriseSunsetCalculator sunCalc = null;

            while ((nextLine = reader.readNext()) != null) {

                doProcess = true;
                
                if ( nextLine.length >= 6 ) {
                    date = nextLine[3];
                    pAc = nextLine[4];


                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(date));

                        if (init) {
                            inverterData.addInitYearSet(cal);
                        } else {
                            // only going to determine this once. ( when it is valid and null ).
                            if (settings.getGpsLocation().isValidValue() && sunCalc == null) {
                                sunCalc = new SunriseSunsetCalculator(settings.getGpsLocation().getLocation(), Constants.getLocalTimeZone());
                                // These are used to calculate todays sunset!
                                Calendar sunset = sunCalc.getOfficialSunsetCalendarForDate(cal);
                                Calendar sunrise = sunCalc.getOfficialSunriseCalendarForDate(cal);

                                // going to ignore the pulse if we get if before sunrise or after sunset. As those don't mean a thing.
                                if (cal.before(sunrise) || cal.after(sunset)) {
                                    doProcess = false;
                                }
                            }


                            if (doProcess) {
                                // determining the minute in the day.
                                workMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                                averagePac = averagePac + Float.valueOf(pAc).floatValue();
                                entries++;

                                // interval threshold is reached. save the data and start with next set.
                                if (workMinute >= (averageMinute + settings.getTimeInterval())) {
                                    avgPac = (averagePac / entries);

                                    boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, avgPac, year);

                                    if (added) {
                                        fc.setProcessed(true);
                                    }

                                    // finalize for the next interval
                                    averageMinute = workMinute;
                                    averagePac = 0f;
                                    entries = 0;
                                }

                            }

                        }
                        lineNumber++;
                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + date + "\" as a correct date. ");
                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }
    }

}
