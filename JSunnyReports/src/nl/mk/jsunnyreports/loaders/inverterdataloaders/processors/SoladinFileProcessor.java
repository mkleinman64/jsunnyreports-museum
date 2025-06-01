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

public class SoladinFileProcessor extends BaseProcessor  implements Runnable {
    
    private static final Logger log = Logger.getLogger(SoladinFileProcessor.class);        

    public SoladinFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc  ) {
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
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        /* used for averaging "interval" minutes of data */
        int averageMinute = 0;
        float averagePac = 0;
        int workMinute = 0;

        boolean bomFound = false;

        try {
            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(theFile)), ';');
            String[] nextLine;

            String date = "";
            String Pac = "";
            String Vac = "";

            int entries = 0;
            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                // fix for odd behaviour in .NET Project writing files. should be 1 / 5 / 6 can be 0 / 4 / 5
                // with older dataLoader.

                // possible solution for this mess is:
                // determine linelength. and get vac
                // if vac is within boundaries do magic and get other columns.
                // might save some time and make code more readable.
                if (nextLine.length == 9) {
                    date = nextLine[0];
                    Vac = nextLine[4];
                    Pac = nextLine[5];

                    if (!bomFound) {
                        // nasty way of removing BOM. Should be encorporated in CSVReader itself
                        // http://mindprod.com/jgloss/bom.html
                        // chosen for option 6.
                        date = date.substring(3);
                        bomFound = true;
                    }
                } else {
                    // Length of the CSV = 10 columns.
                    date = nextLine[1];
                    Vac = nextLine[5];
                    Pac = nextLine[6];

                }

                if (!Vac.equals("0") && !Vac.equals("")) {

                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                    try {

                        cal.setTime(formatter.parse(date));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );                                
                        } else {
                            // determining the minute in the day.
                            workMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

                            averagePac = averagePac + Float.valueOf(Pac).floatValue();
                            entries++;

                            // interval threshold is reached. save the data and start with next set.
                            if (workMinute >= (averageMinute + settings.getTimeInterval())) {
                                float avgPac = (averagePac / entries);
                                boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, avgPac, year );
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
