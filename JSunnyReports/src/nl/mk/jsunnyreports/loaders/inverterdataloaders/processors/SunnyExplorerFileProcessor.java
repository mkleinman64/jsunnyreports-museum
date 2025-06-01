package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.helpers.UnicodeBOMInputStream;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.inverters.SunnyExplorerInverter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.utilities.SunnyExplorerUtilities;

import org.apache.log4j.Logger;

/**
 * SunnyExplorerFileProcessor.java
 * 
 * Loads and processes ( using multiple threads ) the daily files created by Sunny Explorer.
 *  * 
 * @author Martin Kleinman
 * @version 2.0.6
 * @since unknown
 */
public class SunnyExplorerFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(SunnyExplorerFileProcessor.class);

    public SunnyExplorerFileProcessor(File theFile, InverterData inverterData, SunnyExplorerInverter sunnyExplorerInverter, Settings settings, boolean init, Integer year, FileCache fc) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.sunnyExplorerInverter = sunnyExplorerInverter;
        this.settings = settings;
        this.init = init;
        this.year = year;
        this.fc = fc;
    }

    private SunnyExplorerInverter sunnyExplorerInverter;


    public void run() {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        int readerColumn = sunnyExplorerInverter.getM_kWhColumnLocation();

        String workDateTime = "";
        String previousworkDateTime = "";

        String previousKwhString = "";
        String workkWhString = "";

        DateFormat formatter; // formatter has to be read dynamically from inside the file.

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {
            FileInputStream fis = new FileInputStream(theFile);
            UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);

            // default
            String encoding = "UTF-8";

            if (ubis.getBOM().toString().contains("UTF-16")) {
                encoding = "UTF-16";
            }
            if (ubis.getBOM().toString().contains("UTF-32")) {
                encoding = "UTF-32";
            }


            CSVReader reader = new CSVReader(new InputStreamReader(ubis, encoding), ';');

            wholeFile = reader.readAll();

            formatter = SunnyExplorerUtilities.GetDateFormat(wholeFile);

            // determine start and end positions to read
            // one inverter might be missing from the file. We cannot process the column directly
            // we use it indirectly instead through two booleans..
            int startPos = 9;
            String startEntry = null;
            boolean startLineFound = false;

            while (startLineFound == false && startPos <= wholeFile.size() - 1) {
                if (wholeFile.get(startPos).length > readerColumn - 1) {
                    // we deliberatly evaluate another column! containing the kW for that period.
                    String evaluateString = wholeFile.get(startPos)[readerColumn - 1];

                    if (startEntry == null) {
                        startEntry = evaluateString;
                    }

                    if (!evaluateString.equals(startEntry)) {
                        startLineFound = true;
                    }
                }

                if (!startLineFound) {
                    startPos++;
                }

            }
            // we found the position in which the change is detected. ( first actual value ).
            // get the first position with 0.
            if (startPos > 9) {
                // only substract one if we are actually doing some reading! :)
                startPos = startPos - 1;

            }

            int endPos = wholeFile.size() - 1;
            String endEntry = null;
            boolean endLineFound = false;

            while (endLineFound == false && endPos > 9) {
                if (wholeFile.get(endPos).length > readerColumn - 1) {

                    // same here.
                    String evaluateString = wholeFile.get(endPos)[readerColumn - 1];

                    if (endEntry == null) {
                        endEntry = evaluateString;
                    }

                    if (!evaluateString.equals(endEntry)) {
                        endLineFound = true;
                    }
                }

                if (!endLineFound) {
                    endPos--;
                }
            }
            // add one position to get the last value ( if it exists! )
            if (endPos < wholeFile.size() - 1) {
                endPos++;
            }

            // we found the "raw" start and endpos. now fetch one record more so it will
            // start at 0.
            if (startPos > 9) {
                startPos--;
            }
            if (endPos < wholeFile.size() - 1) {
                endPos++;
            }

            //log.info( "startPos: " + startPos + " endPos: " + endPos );

            for (int i = startPos; i <= endPos; i++) {
                lineNumber = i;
                // with multiple inverters there is a chance not all columns are filled. e.g. a readpoint was missed
                // this leaves empty places in the CSV which might damage / corrupt the reading
                // thus we have to first check whether the line we are reading is long enough for the field to exist.
                // if its long enough we can process the line

                if (wholeFile.get(i).length > readerColumn - 1) {

                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    Calendar previousCalendar = Calendar.getInstance(Constants.getLocalTimeZone());

                    previousworkDateTime = workDateTime;
                    workDateTime = wholeFile.get(i)[0];

                    // extra check if dateTime contains two ":"
                    String time = workDateTime.substring(11);
                    if (time.length() == 5) {
                        workDateTime = workDateTime + ":00";
                    }

                    previousKwhString = workkWhString; // this one is needed to calculate the amount of watt during this period.
                    workkWhString = wholeFile.get(i)[readerColumn - 1];

                    if (("".equals(previousKwhString) == false) && ("".equals(workkWhString) == false)) {

                        try {
                            previousCalendar.setTime(formatter.parse(previousworkDateTime));

                            cal.setTime(formatter.parse(workDateTime));
                            
                            if ( init ) {
                                inverterData.addInitYearSet( cal );                                    
                            } else {
                                float timeDifferenceInMins = (cal.getTimeInMillis() - previousCalendar.getTimeInMillis()) / 60000f;
                                
                                float kWh = 0f;
                                float previouskWh = 0f;
                                try {
                                    kWh = Float.valueOf(workkWhString.replace(",", ".")).floatValue();
                                    previouskWh = Float.valueOf(previousKwhString.replace(",", ".")).floatValue();                                    
                                    
                                } catch ( NumberFormatException nfe ) {
                                    log.warn("Warning: " + workkWhString + " and/or " + previousKwhString + " is not a valid value, 0 is assumed. Time: " + workDateTime );
                                    kWh = 0f;
                                    previouskWh = 0f;
                                }

                                
                                float yieldkWh = kWh - previouskWh;
                                float watt = (yieldkWh * 1000) / (timeDifferenceInMins / 60);

                                boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), sunnyExplorerInverter, watt, year );
                                if ( added ) {
                                    fc.setProcessed(true);
                                }
                            }


                        } catch (ParseException pe) {
                            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
                        }

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
