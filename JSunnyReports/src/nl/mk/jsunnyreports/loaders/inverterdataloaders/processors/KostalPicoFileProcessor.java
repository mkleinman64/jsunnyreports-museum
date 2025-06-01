package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;

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
import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;


public class KostalPicoFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(KostalPicoFileProcessor.class);

    public KostalPicoFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
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

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        int lineNumber = 0;

        try {

            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(theFile)), ';');
            String[] nextLine;

            // three phase in this file.
            String pAc1 = ""; // 14
            String pAc2 = ""; // 17
            String pAc3 = ""; // 19

            String date = "";
            String time = "";
            String dateTime = "";

            while ((nextLine = reader.readNext()) != null) {

                lineNumber++;

                if (lineNumber == 1) {

                    date = nextLine[0];
                    date = date.substring(8);
                }

                else {

                    if (lineNumber >= 8) {
                        // main work here
                        time = nextLine[0];

                        pAc1 = nextLine[13];
                        pAc2 = nextLine[16];
                        pAc3 = nextLine[18];

                        try {
                            float pAcfloat;
                            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                            dateTime = date + " " + time;
                            cal.setTime(formatter.parse(dateTime));
                            
                            if ( init ) {
                                inverterData.addInitYearSet( cal );    
                            } else {
                                pAcfloat = Float.valueOf(pAc1).floatValue() + Float.valueOf(pAc2).floatValue() + Float.valueOf(pAc3).floatValue();
                                //System.out.println( "dt " + dateTime + " " + pAcfloat + " " + pAc1 );
                                boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, pAcfloat, year );
                                if ( added ) {
                                    fc.setProcessed(true);
                                }
                            }
                            


                        } catch (ParseException pe) {
                            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + dateTime + "\" as a correct date. ");
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
