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

import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ConsospyFileProcessor;

import org.apache.log4j.Logger;

/**
 * Consospy Dataloader.
 *
 * Date         Version     Who     What
 * 03-01-2011   1.1.2.2     MKL     First version
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 14-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 14-01-2012   1.3.2.0     MKL     Removed unneccessary while loop in removing a logfile.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.2.0
 * @since     1.1.2.2
 */
public class ConsospyDataLoader extends BaseLoader implements LoaderInterface {

    public ConsospyDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
    }

    private static final Logger log = Logger.getLogger(ConsospyDataLoader.class);

    /**
     *
     * @param logFile logFile for ConsoSpy to process.
     */
    private void logFileRemove(File logFile) {
        int lineNumber = 0;
        inverterData.setUpdated(true);
        DateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String workDateTime = "";
        //horloge ; base
        //01/11/2010 00:00;9288771

        try {

            CSVReader reader = new CSVReader(new FileReader(logFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 2);
            String[] nextLine;

            try {
                while ((nextLine = reader.readNext()) != null) {
                    // now we have the following problem people are using various dateformats
                    // dd.MM.yyyy
                    // dd/MM/yyyy
                    // etc. so lets first replace all the possible seperators
                    workDateTime = nextLine[0];
                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                    cal.setTime(formatter.parse(workDateTime));
                    inverterData.removeDayFromSet(baseInverter, cal);
                }


                nextLine = reader.readNext();

            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
            } catch (IOException IOe) {
                log.error("An error has occured reading line: " + lineNumber + " in file: " + logFile.getName());
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + logFile.getName() + " could not be found anymore, ignoring it.");
        }

    }

    private void processFiles( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".csv"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.logFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable consospyData = new ConsospyFileProcessor(newFile, inverterData, baseInverter, settings, init, year,f.getFc() );
                executor.execute(consospyData);
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
    public void dataLoader( boolean init, Integer year ) {
        this.processFiles( init, year );
        super.dataLoader( init, year );
    }
}
