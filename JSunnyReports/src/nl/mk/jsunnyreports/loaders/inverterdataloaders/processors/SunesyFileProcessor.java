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

public class SunesyFileProcessor extends BaseProcessor  implements Runnable {
    
    private static final Logger log = Logger.getLogger(SunesyFileProcessor.class);    
    
    public SunesyFileProcessor(File theFile, InverterData inverterData, BaseInverter baseInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
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
        formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());


        /* used for averaging one minute of data */
        int averageMinute = 0;
        float averagePac = 0;
        int entries = 0;

        /* used for calcing and averaging */
        int workMinute = 0;

        String date = "";
        String Pac = "";

        //Time         ;Temp(C);Vpv(V);Iac(A);Vac(V);Fac(Hz);Pac(W);Zac(mOhm);E-Total(kWh);h-Total(h);Vpv1(V);Vpv2(V);Vpv3(V);Ipv1(A);Ipv2(A);Ipv3(A);Ppv1(W);Ppv2(W);Ppv3(W);Temp1(C);Temp2(C);RAD1(W/m^2);RAD2(W/m^2)
        //2010/12/17 10:33:16; 15.4;197.0;  1.1;233.8; 50.02;   264;NA;    25.3;    26;197.0;  0.0;  0.0;  0.0;  0.0;  0.0;     0;     0;     0;NA;NA;NA;NA

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 2);
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                Pac = nextLine[6];

                date = nextLine[0];

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

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }

    }

 
    
    
}
