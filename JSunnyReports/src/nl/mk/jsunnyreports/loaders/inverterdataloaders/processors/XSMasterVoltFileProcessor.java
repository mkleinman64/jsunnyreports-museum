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
import nl.mk.jsunnyreports.inverters.XSMastervoltInverter;
import org.apache.log4j.Logger;

public class XSMasterVoltFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(XSMasterVoltFileProcessor.class);    
    
    public XSMasterVoltFileProcessor(File theFile, InverterData inverterData, XSMastervoltInverter mvInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.mvInverter = mvInverter;
        this.settings = settings;
        this.year = year;
        this.init = init;
        this.fc = fc;
    }

    private XSMastervoltInverter mvInverter;
    

    public void run() {
        inverterData.setUpdated(true);

        String serialNumber = mvInverter.getM_SerialNumber();

        int lineNumber = 0;

        DateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String dateTime = "";
        String Pac = "";
        String serialNo = "";

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            for (int i = wholeFile.size() - 1; i >= 1; i--) {
                lineNumber = i;
                serialNo = wholeFile.get(i)[5];
                if (serialNo.equals(serialNumber)) {
                    Pac = wholeFile.get(i)[12];

                    dateTime = wholeFile.get(i)[1];

                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(dateTime));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );                                
                        } else {
                            float watt = Float.valueOf(Pac).floatValue();
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), mvInverter , watt, year );
                            if ( added ) {
                                fc.setProcessed(true);
                            }
                        }


                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + dateTime + "\" as a correct date. ");
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
