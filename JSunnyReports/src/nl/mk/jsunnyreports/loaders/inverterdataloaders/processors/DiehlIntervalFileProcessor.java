package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;

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
import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

public class DiehlIntervalFileProcessor extends BaseProcessor  implements Runnable {
    
    private static final Logger log = Logger.getLogger(DiehlIntervalFileProcessor.class);    
    
    public DiehlIntervalFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.baseInverter = baseInverter;
        this.settings = settings;
        this.init = init;
        this.year = year;
        this.fc = fc;
    }

    private static String determineDateFormat(String date) {
        String finalFormat = null;
        DateFormat formatter = null;
        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

        try {
            String dateFormat = "dd-MMM-yyyy";
            formatter = new SimpleDateFormat(dateFormat);
            cal.setTime(formatter.parse(date));

            finalFormat = dateFormat;

        } catch (Exception e) {
            // lets do nothing

        }

        try {
            String dateFormat = "dd.MM.yyyy";
            formatter = new SimpleDateFormat(dateFormat);
            cal.setTime(formatter.parse(date));

            finalFormat = dateFormat;

        } catch (Exception e) {
            // we do nothing

        }

        return finalFormat;

    }

    public void run() {
        inverterData.setUpdated(true);
        String workDate = "";
        String workTime = "";
        String workWatt = "";

        int lineNumber = 0;

        boolean headerFound = false;

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';');
            String[] nextLine;

            try {

                while ((nextLine = reader.readNext()) != null) {

                    lineNumber++;
                    if (headerFound == true && nextLine.length >= 9) {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                        workDate = nextLine[0];
                        workTime = nextLine[1];
                        workWatt = nextLine[9].replace(",", ".");

                        String dateFormat = determineDateFormat(workDate) + " HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(dateFormat);
                        formatter.setTimeZone(Constants.getLocalTimeZone());

                        // log.info( workDate + " " + workTime + " : " + workWatt );

                        cal.setTime(formatter.parse(workDate + " " + workTime));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );    
                        } else {
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, Float.valueOf(workWatt).floatValue(), year );
                        }
                        

                    }

                    // its unknown where the actual header is, so first order of business is to find it.
                    if (!headerFound && nextLine[0].equals("Date")) {
                        headerFound = true;
                    }
                }

            } catch (IOException IOe) {
                log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + " " + workTime + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        }

    }



}
