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

public class EnphaseFileProcessor extends BaseProcessor  implements Runnable {
    

    private static final Logger log = Logger.getLogger(EnphaseFileProcessor.class);       

    public EnphaseFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
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

        int lineNumber = 1;

        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());


        String datetime = "";
        String pac = "";


        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {


                lineNumber++;

                pac = nextLine[1];
                pac = pac.replace("\"", "");

                if (!pac.equals("0")) {
                    datetime = nextLine[0];
                    datetime = datetime.substring(1, 20);

                    //System.out.println(datetime);


                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(datetime));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );       
                        } else {
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, Float.valueOf(pac), year );
                            if ( added ) {
                                fc.setProcessed(true);
                            }
                        }



                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + datetime + "\" as a correct date. ");
                    }

                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }

    }

    /**
     *
     * @param logFile logFile for Enphase to process.
     * @throws DataLoadException
     */
    private void csvFileLoad(File csvFile) {


    }


}
