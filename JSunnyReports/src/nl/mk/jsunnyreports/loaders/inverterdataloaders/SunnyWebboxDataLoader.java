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

import nl.mk.jsunnyreports.inverters.SunnyWebboxInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SunnyWebboxFileProcessor;

import org.apache.log4j.Logger;

/**
 * Sunny Webbox logfile parser, (yyyy-MM-dd.csv )
 *
 *
 * Date         Version     Who     What
 * 13-10-2010   1.1.1.0     MKL     Huge update in how data is processed.
 * 19-10-2010   1.1.2.0     MKL     Code improvements.
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 16-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 * 24-01-2012   1.3.2.0     MKL     Added invertercode as extra check ( multiple inverters in one file ).
 * 30-01-2012   1.3.2.0     MKL     More logic in pac column A = 1, B = 2
 * 22-09-2012   1.3.3.0b3   MKL     Fixed issue 297
 *
 * @author    Martin Kleinman
 * @version   1.3.2.0
 * @since     1.3.2.0
 */
public class SunnyWebboxDataLoader extends BaseLoader implements LoaderInterface {

    private SunnyWebboxInverter inverter;

    public SunnyWebboxDataLoader(SunnyWebboxInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);

        // overwrite inverter with specific SunnyWebboxtype.
        this.inverter = inverter;
    }

    private static final Logger log = Logger.getLogger(SunnyWebboxDataLoader.class);

    private void csvFileRemove(File logFile) {
        inverterData.setUpdated(true);

        int lineNumber = 0;

        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        // get fileName.
        String date = logFile.getName().substring(0, logFile.getName().indexOf("."));

        try {
            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
            cal.setTime(formatter.parse(date));
            inverterData.removeDayFromSet(inverter, cal);
            ;

        } catch (ParseException pe) {
            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + date + "\" as a correct date. ");
        }

    }

    private void processFiles(boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".csv"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.csvFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable fileData = new SunnyWebboxFileProcessor(newFile, inverterData, inverter, settings, init, year,f.getFc()  );
                executor.execute(fileData);
            }

        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }        
    }

    /**
     *
     * @since
     */
    public void dataLoader(boolean init, Integer year) {
        this.processFiles(init, year  );
        super.dataLoader(init, year );
    }

}
