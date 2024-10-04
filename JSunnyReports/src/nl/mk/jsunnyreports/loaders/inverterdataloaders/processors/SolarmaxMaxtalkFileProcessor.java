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

public class SolarmaxMaxtalkFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(SolarmaxMaxtalkFileProcessor.class);    
    
    public SolarmaxMaxtalkFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.baseInverter = baseInverter;
        this.settings = settings;
        this.year = year;
        this.init = init;
        this.fc = fc;
    }

    public void run() {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());


        /* used for averaging one minute of data */
        int averageMinute = 0;
        float averagePac = 0;
        int entries = 0;

        /* used for calcing and averaging */
        int workMinute = 0;

        String date = "";
        String time = "";
        String Pac = "";

        //;;wr_pac;wr_uac_l1;wr_iac_l1;wr_e_day;wr_e_month;temp_lt;
        //01.11.2010;15:48:21;205.00;233.30;0.86;7.40;7.00;30.00;

        try {

            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 5);
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                Pac = nextLine[3];

                date = nextLine[0];
                time = nextLine[1];

                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                try {
                    cal.setTime(formatter.parse(date + " " + time));
                    if ( init ) {
                        inverterData.addInitYearSet( cal );    
                    } else {
                        // determining the minute in the day.
                        workMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

                        averagePac = averagePac + Float.valueOf(Pac);
                        entries++;

                        // interval threshold is reached. save the data and start with next set.
                        if (workMinute >= (averageMinute + settings.getTimeInterval())) {
                            float avgPac = (averagePac / entries);
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, avgPac, year);
                            if ( added ) {
                                fc.setProcessed(true);
                            }
                            // finalize for the next interval
                            averageMinute = workMinute;
                            averagePac = 0f;
                            entries = 0;
                        }
                        
                    }

                } catch (ParseException pe) {
                    log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + date + " " + time + "\" as a correct date. ");
                }
            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }
    }

}
