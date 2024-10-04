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

import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.StartsWithFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;

import org.apache.log4j.Logger;

/**
 * Arduino dataloader, reads custom csv files from Java Arduino project written by Martin Kleinman
 *
 * Date         Version     Who     What
 * 08-03-2011   1.2.4.0     MKL     First Version
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Loading manual.xls failed ( code never executed ).
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 26-12-2011   1.3.2.0     MKL     Removed exception.
 * 14-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 * 02-11-2012   1.3.4.0     MKL     Building preliminary support for not loading data before sunrise and after sunset.
 * 02-10-2014   1.5.0.0     MKL     modified filecache to filename only
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.5.0.0
 * @since     1.1.2.4
 */
public class ArduinoDataLoader extends BaseLoader implements LoaderInterface {
    private static final Logger log = Logger.getLogger(ArduinoDataLoader.class);

    public ArduinoDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);

    }

    private void csvFileRemove(File csvFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String date = "";

        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        try {
            CSVReader reader = new CSVReader(new BufferedReader(new FileReader(csvFile)), ';');
            String[] nextLine = reader.readNext();
            date = nextLine[0];
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

    private void processFiles(boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".csv"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.csvFileRemove( newFile );
            }
            if ( f.isToLoad() ) {
                Runnable fileData = new ArduinoFileProcessor(newFile, inverterData, baseInverter, settings, init, year, f.getFc() );
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
     * @since 1.0.0.0b
     */
    @Override
    public void dataLoader(boolean init, Integer year) {
        this.processFiles( init, year );
        super.dataLoader( init, year );
    }

}
