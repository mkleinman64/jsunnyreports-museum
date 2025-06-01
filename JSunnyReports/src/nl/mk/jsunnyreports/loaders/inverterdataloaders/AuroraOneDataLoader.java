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

import nl.mk.jsunnyreports.inverters.AuroraOneInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.AuroraOneFileProcessor;

import org.apache.log4j.Logger;

/**
 * Aurora Power One dataloader
 *
 * Loads Aurora One .log files into jSunnyreports.
 * Can deal with multiple inverters on different addresses in the same logfile. ( uses option field! ).
 *
 *
 * Date         Version     Who     What
 * 19-01-2012   1.3.2.0     MKL     First version
 * 23-09-2012   1.3.3.0b3   MKL     Fixes issue 323. different date format.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.2.0
 * @since     1.3.2.0
 */
public class AuroraOneDataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(AuroraOneDataLoader.class);

    public AuroraOneDataLoader(AuroraOneInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
        this.inverter = inverter;
    }

    private AuroraOneInverter inverter;

    private void logFileRemove(File file) {
        inverterData.setUpdated(true);

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(Constants.getLocalTimeZone());
        int lineNumber = 1;

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {

            CSVReader reader = new CSVReader(new FileReader(file), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            // determine date in file.
            // Its in format Date: dd/MM/yyyy
            // and between the : there can be spaces.
            String workDate = wholeFile.get(1)[0];
            workDate = workDate.substring(workDate.indexOf(":") + 1);
            workDate = workDate.trim();
            // fixes different date structure.
            workDate = workDate.replace("-", "/");

            try {
                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                cal.setTime(formatter.parse(workDate));
                inverterData.removeDayFromSet(inverter, cal);

            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
            }


        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + file.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + file.getName());
        }

    }

    private void processFiles( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".log"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.logFileRemove( newFile );
            }
            if ( f.isToLoad() ) {
                Runnable auroraData = new AuroraOneFileProcessor(newFile, inverterData, inverter, settings, init, year, f.getFc() );
                executor.execute(auroraData);
            }
        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }           
    }


    /**
     * Main method
     */
    @Override
    public void dataLoader( boolean init, Integer year ) {
        this.processFiles( init, year );
        super.dataLoader( init, year );
    }
}
