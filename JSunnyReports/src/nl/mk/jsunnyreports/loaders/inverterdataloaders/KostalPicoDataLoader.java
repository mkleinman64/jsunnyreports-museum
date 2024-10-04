package nl.mk.jsunnyreports.loaders.inverterdataloaders;

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
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.KostalPicoFileProcessor;

import org.apache.log4j.Logger;

/**
 * Omnik dataloader
 *
 * Date         Version     Who     What
 * 28-02-2014   1.4.1.0     MKL     First Version

 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.4.1.0
 * @since     1.4.1.0
 */
public class KostalPicoDataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(KostalPicoDataLoader.class);

    public KostalPicoDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);

    }

    private void csvFileRemove(File csvFile) {
        inverterData.setUpdated(true);

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yyyy");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        int lineNumber = 1;

        try {

            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(csvFile)), ';');
            String[] nextLine = reader.readNext();
            String date = "";

            date = nextLine[0];
            date = date.substring(8);

            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

            try {
                cal.setTime(formatter.parse(date));
                inverterData.removeDayFromSet(baseInverter, cal);
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + date + "\" as a correct date. ");
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
                Runnable fileData = new KostalPicoFileProcessor(newFile, inverterData, baseInverter, settings, init, year,f.getFc() );
                executor.execute(fileData);
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
     * @throws DataLoadException
     * @since 1.0.0.0b
     */
    @Override
    public void dataLoader(boolean init, Integer year) {
        this.processFiles(init, year );
        super.dataLoader(init, year );
    }

}
