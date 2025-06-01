package nl.mk.jsunnyreports.loaders.inverterdataloaders;

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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.EndsWithFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;

import nl.mk.jsunnyreports.inverters.XSMastervoltInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.XSMasterVoltFileProcessor;

import org.apache.log4j.Logger;

/**
 * Mastervolt XS baseInverter parser, ( .csv )
 *
 *
 * Date         Version     Who     What
 * 23-09-2012   1.3.3.0b3   MKL     Initial version
 *
 * @author Martin Kleinman
 * @version 1.3.3.0b3
 * @since 1.1.0.0beta5
 */
public class XSMastervoltDataLoader extends BaseLoader implements LoaderInterface {

    private XSMastervoltInverter inverter;

    public XSMastervoltDataLoader(XSMastervoltInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);

        this.inverter = inverter; // overwrite inverter with XSMastervoltType.
    }

    private static final Logger log = Logger.getLogger(XSMastervoltDataLoader.class);

    private void csvFileRemove(File csvFile) {
        inverterData.setUpdated(true);

        String serialNumber = inverter.getM_SerialNumber();

        int lineNumber = 0;

        DateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String dateTime = "";

        String serialNo = "";
        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            String[] nextLine;

            boolean isRemoved = false;

            while (((nextLine = reader.readNext()) != null) && isRemoved == false) {
                serialNo = nextLine[5];

                if (serialNo.equals(serialNumber)) {

                    dateTime = nextLine[1];

                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(dateTime));

                        inverterData.removeDayFromSet(inverter, cal);

                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + dateTime + "\" as a correct date. ");
                    }

                    isRemoved = true;

                }
                lineNumber++;
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + csvFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + csvFile.getName());
        }

    }

    private void processFiles(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".csv"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.csvFileRemove( newFile );
            }
            if ( f.isToLoad() ) {
                Runnable fileData = new XSMasterVoltFileProcessor(newFile, inverterData, inverter, settings, init, year,f.getFc() );
                executor.execute(fileData);
            }

        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }        
    }

    /**
     *
     * @throws DataLoadException
     * @since
     */
    @Override
    public void dataLoader(boolean init, Integer year) {
        this.processFiles(init, year );
        super.dataLoader(init, year);
    }
}
