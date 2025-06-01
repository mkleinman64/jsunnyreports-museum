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

import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

public class ConsospyFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(ConsospyFileProcessor.class);

    public ConsospyFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
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
        int lineNumber = 0;
        DateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String workDateTime = "";
        String previousworkDateTime = "";

        String previousWh = "";
        String workWh = "";

        //horloge ; base
        //01/11/2010 00:00;9288771
        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 2);
            String[] nextLine;

            try {
                while ((nextLine = reader.readNext()) != null) {
                    lineNumber++;

                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    Calendar previousCalendar = Calendar.getInstance(Constants.getLocalTimeZone());

                    previousworkDateTime = workDateTime;

                    // now we have the following problem people are using various dateformats
                    // dd.MM.yyyy
                    // dd/MM/yyyy
                    // etc. so lets first replace all the possible seperators
                    workDateTime = nextLine[0];

                    previousWh = workWh; // this one is needed to calculate the amount of watt during this period.
                    // was (i)[1] going to be (i)[readerColumn-1]
                    workWh = nextLine[1];

                    if (("".equals(previousWh) == false) && ("".equals(workWh) == false)) {

                        previousCalendar.setTime(formatter.parse(previousworkDateTime));
                        cal.setTime(formatter.parse(workDateTime));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );    
                        } else {
                            float timeDifferenceInMins = (cal.getTimeInMillis() - previousCalendar.getTimeInMillis()) / 60000f;

                            float Wh = Float.valueOf(workWh.replace(",", ".")).floatValue();
                            float prevWh = Float.valueOf(previousWh.replace(",", ".")).floatValue();
                            float yieldWh = Wh - prevWh;
                            float watt = (yieldWh) / (timeDifferenceInMins / 60f);

                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, watt, year );
                            if ( added ) {
                                fc.setProcessed(true);
                            }
                        }


                    }

                }

            } catch (IOException IOe) {
                log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        }

    }


}
