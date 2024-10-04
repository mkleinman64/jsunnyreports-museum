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
import java.util.Locale;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

public class OmnikFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(OmnikFileProcessor.class);

    public OmnikFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc) {
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
        formatter = new SimpleDateFormat("MMM.dd,yyyy HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(Constants.getLocalTimeZone());

        int lineNumber = 0;

        try {

            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(theFile)), ',');
            String[] nextLine;

            String pAc1 = "";
            String pAc2 = "";
            String pAc3 = "";

            String date = "";
            String time = "";
            String dateTime = "";

            while ((nextLine = reader.readNext()) != null) {

                lineNumber++;

                if (lineNumber == 5) {

                    date = nextLine[1];
                }

                else {

                    if (lineNumber >= 10) {
                        // main work here
                        time = nextLine[18];
                        pAc1 = nextLine[13];
                        pAc2 = nextLine[14];
                        pAc3 = nextLine[15];

                        // we need to modify the Time string.
                        // Oct.26 10:15,2012
                        // should be
                        // 10:15
                        time = time.substring(7, 12);

                        try {
                            float pAcfloat;
                            pAcfloat = Float.valueOf(pAc1).floatValue();
                            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                            dateTime = date + " " + time;
                            cal.setTime(formatter.parse(dateTime));
                            
                            if ( init ) {
                                inverterData.addInitYearSet( cal );    
                            } else {
                                //System.out.println( "dt " + dateTime + " " + pAcfloat + " " + pAc1 );
                                boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, pAcfloat, year);
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
