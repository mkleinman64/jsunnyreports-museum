package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedReader;
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

import nl.mk.jsunnyreports.inverters.BaseInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SchucoFileProcessor;

import org.apache.log4j.Logger;

/**
 * Arduino dataloader, reads custom csv files from Java Arduino project written by Martin Kleinman
 *
 * Date         Version     Who     What
 * 19-01-2012   1.3.2.0     MKL     New
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.2.0
 * @since     1.3.2.0
 */

public class SchucoDataLoader extends BaseLoader implements LoaderInterface {


    private static final Logger log = Logger.getLogger(SchucoDataLoader.class);

    public SchucoDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
    }


    private void csvFileRemove(File file) {
        inverterData.setUpdated(true);

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String workDateTime = "";

        int lineNumber = 0;

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {

            CSVReader reader = new CSVReader(new FileReader(file), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            // do the main loop for inverter which equals "options".
            for (int iterator = 1; iterator < wholeFile.size() - 1; iterator++) {
                lineNumber = iterator;

                // check inverter address
                workDateTime = wholeFile.get(iterator)[0];

                if (workDateTime.length() >= 19) {

                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(workDateTime));
                        inverterData.removeDayFromSet(baseInverter, cal);

                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + file.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + file.getName());
        }

    }

    private void processFiles( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".csv"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.csvFileRemove( newFile );
            }
            if ( f.isToLoad() ) {
                Runnable schucoData = new SchucoFileProcessor(newFile, inverterData, baseInverter, settings, init, year,f.getFc() );
                executor.execute(schucoData);
            }

        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }        
    }

    /**
     * Dataloader implementation from interface specification. Checks the input directory, gets the files
     * that match this dataloader type and process them accordingly.
     *
     * @since 1.0.0.0b
     */
    public void dataLoader( boolean init, Integer year ) {
        this.processFiles( init, year );
        super.dataLoader( init, year );
    }
}
