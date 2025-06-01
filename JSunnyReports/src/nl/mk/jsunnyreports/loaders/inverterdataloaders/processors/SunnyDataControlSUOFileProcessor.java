package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import nl.mk.jsunnyreports.inverters.SDCSUOInverter;

import org.apache.log4j.Logger;

public class SunnyDataControlSUOFileProcessor extends BaseProcessor  implements Runnable {

     
    private static final Logger log = Logger.getLogger(SunnyDataControlSUOFileProcessor.class);  

    public SunnyDataControlSUOFileProcessor(File theFile, InverterData inverterData, SDCSUOInverter sdcSUOInverter, Settings settings, boolean init, Integer year , FileCache fc) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.sdcSUOInverter = sdcSUOInverter;
        this.settings = settings;
        this.init = init;
        this.year = year;
        this.fc = fc;
    }
    
    private SDCSUOInverter sdcSUOInverter;

    public void run() {
        inverterData.setUpdated(true);
        int pacColumn = sdcSUOInverter.getM_PACColumnLocation();
        int lineNumber = 0;

        String workDateTime = "";
        String pAC = "";
        DateFormat formatter; // formatter has to be read dynamically from inside the file.
        List<String[]> wholeFile = new ArrayList<String[]>();

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';');
            wholeFile = reader.readAll();

            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formatter.setTimeZone(Constants.getLocalTimeZone());

            // determine start and end positions to read
            // one inverter might be missing from the file. We cannot process the column directly
            // we use it indirectly instead through two booleans..
            int startPos = 5;
            String startEntry = null;
            boolean startLineFound = false;

            while (startLineFound == false && startPos <= wholeFile.size() - 1) {
                // we deliberatly evaluate another column! containing the kW for that period.
                String evaluateString = wholeFile.get(startPos)[1];

                if (startEntry == null) {
                    startEntry = evaluateString;
                }

                if (!evaluateString.equals(startEntry)) {
                    startLineFound = true;
                }

                if (!startLineFound) {
                    startPos++;
                }

            }

            int endPos = wholeFile.size() - 1;
            String endEntry = null;
            boolean endLineFound = false;

            while (endLineFound == false && endPos > 5) {

                // same here.
                String evaluateString = wholeFile.get(endPos)[1];

                if (endEntry == null) {
                    endEntry = evaluateString;
                }

                if (!evaluateString.equals(endEntry)) {
                    endLineFound = true;
                }

                if (!endLineFound) {
                    endPos--;
                }

            }

            // we found the "raw" start and endpos. now fetch one record more so it will
            // start at 0.
            if (startPos > 5) {
                startPos--;
            }
            if (endPos < wholeFile.size() - 1) {
                endPos++;
            }

            //log.info( "startPos: " + startPos + " endPos: " + endPos );

            for (int i = startPos; i <= endPos; i++) {
                lineNumber = i;
                // with multiple inverters there is a chance not all columns are filled. e.g. a readpoint was missed
                // this leaves empty places in the SUO which might damage / corrupt the reading
                // thus we have to first check whether the line we are reading is long enough for the field to exist.
                // if its long enough we can process the line

                if (wholeFile.get(i).length > 1) {

                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());


                    workDateTime = wholeFile.get(i)[0];
                    workDateTime = workDateTime.replace("/", "-"); // simple fix to get rid of the / in a date.

                    pAC = wholeFile.get(i)[ pacColumn - 1];
                    pAC = pAC.replace(",", ".");

                    try {
                        cal.setTime(formatter.parse(workDateTime));

                        if ( init ) {
                            inverterData.addInitYearSet( cal );    
                        } else {
                            // pac contains a .000 at the end. Going to remove those.
                            int dotLocation = pAC.indexOf(".");
                            if (dotLocation != -1) {
                                pAC = pAC.substring(0, dotLocation);
                            }

                            float watt = Float.valueOf(pAC).floatValue();
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), sdcSUOInverter, watt, year);
                            if ( added ) {
                                fc.setProcessed(true);
                            }
                        }


                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
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
