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

public class SolarmaxPDLFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(SolarmaxPDLFileProcessor.class); 
    
    public SolarmaxPDLFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
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
        formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());


        /* used for averaging one minute of data */
        int averageMinute = 0;
        float averagePac = 0;
        int entries = 0;

        /* used for calcing and averaging */
        int workMinute = 0;

        Calendar cal;

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), '\t');
            String[] nextLine;
            int lineNum = 1;

            String date = "";
            String hhmm = "";
            String ss = "00"; // if first value is empty it might generate an error.
            String Udc = "";
            String Pac = "";
            String workDate;
            float avgPac;

            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                if (lineNum == 1) {
                    date = nextLine[0].substring(1);
                }
                // we ignore line 2 and 3 as they have nothing interesting to tell.
                if (lineNum >= 4) {

                    // values are only in the file when they change! so we have to check for every single item whether we need to
                    // update it. ( its complex but saves a lot of info in the file. ).
                    int arrayLength = nextLine.length;

                    //hhmm column [0], check if array is big enough and if the value is updated.
                    if (arrayLength >= 1 && nextLine[0].length() > 0) {
                        hhmm = nextLine[0];
                    }

                    //ss column [1]
                    if (arrayLength >= 2 && nextLine[1].length() > 0) {
                        ss = nextLine[1];
                    }

                    if (arrayLength >= 3 && nextLine[2].length() > 0) {
                        Udc = nextLine[2];
                    }

                    if (arrayLength >= 4 && nextLine[3].length() > 0) {
                        Pac = nextLine[3];
                    }

                    cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    workDate = date + " " + hhmm + ":" + ss;

                    try {
                        cal.setTime(formatter.parse(workDate));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );      
                        } else {
                            // determining the minute in the day.
                            workMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

                            averagePac = averagePac + Float.valueOf(Pac);
                            entries++;

                            // interval threshold is reached. save the data and start with next set.
                            if (workMinute >= (averageMinute + settings.getTimeInterval())) {
                                avgPac = (averagePac / entries);
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
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
                    }
                }
                lineNum++;
            }
            reader = null;

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }
        
    }

    

}
