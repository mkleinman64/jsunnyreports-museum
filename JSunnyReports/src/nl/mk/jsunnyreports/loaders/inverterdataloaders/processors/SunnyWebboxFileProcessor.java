package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

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
import nl.mk.jsunnyreports.inverters.SunnyWebboxInverter;

import org.apache.log4j.Logger;

public class SunnyWebboxFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(SunnyWebboxFileProcessor.class);    
    
    public SunnyWebboxFileProcessor(File theFile, InverterData inverterData, SunnyWebboxInverter sunnyWebboxInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.sunnyWebboxInverter = sunnyWebboxInverter;
        this.settings = settings;
        this.init = init;
        this.year = year;
        this.fc = fc;
    }

    private SunnyWebboxInverter sunnyWebboxInverter;

    

    public void run() {
        inverterData.setUpdated(true);

        int lineNumber = 0;

        DateFormat formatter;
        formatter = null;

        // get fileName.
        String fileDate = theFile.getName().substring(0, theFile.getName().indexOf("."));
        int pacColumn = sunnyWebboxInverter.getM_PACColumnLocation();

        String dateTime = "";
        String pac = "";

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            // do the main loop for inverter which equals "options".
            for (int iterator = 10; iterator < wholeFile.size() - 1; iterator++) {
                lineNumber = iterator;

                dateTime = wholeFile.get(iterator)[0];
                pac = wholeFile.get(iterator)[pacColumn - 1];
                pac = pac.replace(",", ".");

                if (wholeFile.get(iterator).length - 1 >= pacColumn) {
                    // System.out.println(time + " " + pac);

                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                        // extra functionality for getting date time correct.
                        if (dateTime.length() > 5) {
                            // contains Date!
                            dateTime = dateTime.replace(".", "-");
                            dateTime = dateTime.replace("/", "-");
                            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            formatter.setTimeZone(Constants.getLocalTimeZone());

                        } else {
                            // it does not contain a date!
                            dateTime = fileDate + " " + dateTime;
                            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            formatter.setTimeZone(Constants.getLocalTimeZone());

                        }

                        // hmmm. OK in de pAC column?
                        if (!pac.equalsIgnoreCase("ok")) {

                            cal.setTime(formatter.parse(dateTime));
                            
                            if ( init ) {
                                inverterData.addInitYearSet( cal );    
                            } else {
                                boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), sunnyWebboxInverter, Float.valueOf(pac).floatValue(), year);
                                if ( added ) {
                                    fc.setProcessed(true);
                                }
                            }
                            
                        }

                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + fileDate + " " + dateTime + "\" as a correct date. ");
                    } catch (Exception ue) {
                        log.error("General Exception on line: " + lineNumber + ".Time:  " + dateTime + " pac:" + pac);
                        ue.printStackTrace();
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
