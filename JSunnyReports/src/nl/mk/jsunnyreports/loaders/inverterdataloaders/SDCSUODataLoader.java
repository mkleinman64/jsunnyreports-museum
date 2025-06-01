package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import au.com.bytecode.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
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
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.RegularExpressionFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;


import nl.mk.jsunnyreports.inverters.SDCSUOInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SunnyDataControlSUOFileProcessor;

import org.apache.log4j.Logger;


/**
 *
 * Date         Version     Who     What
 *
 * 22-09-2012   1.3.3.0B3   MKL     First version.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.2.0
 * @since     1.1.0.0beta
 */
public class SDCSUODataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(SDCSUODataLoader.class);
    private SDCSUOInverter inverter;

    public SDCSUODataLoader(SDCSUOInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
        this.inverter = inverter;
    }

    private void suoFileRemove(File suoFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String workDateTime = "";

        DateFormat formatter; // formatter has to be read dynamically from inside the file.
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {

            CSVReader reader = new CSVReader(new FileReader(suoFile), ';');
            wholeFile = reader.readAll();

            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
            lineNumber = 10;
            workDateTime = wholeFile.get(10)[0];
            workDateTime = workDateTime.replace("/", "-"); // simple fix to get rid of the / in a date.

            // extra check if dateTime contains two ":"
            String time = workDateTime.substring(11);
            if (time.length() == 5) {
                workDateTime = workDateTime + ":00";
            }

            try {
                cal.setTime(formatter.parse(workDateTime));
                inverterData.removeDayFromSet(baseInverter, cal);
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + suoFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + suoFile.getName());
        }
    }

    private void processFiles( boolean init,Integer year ) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^*[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].suo"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.suoFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable dataFile = new SunnyDataControlSUOFileProcessor(newFile, inverterData, inverter, settings, init, year,f.getFc()  );
                executor.execute(dataFile);
            }

        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }                
    }

    /**
     * This method is the start of the loading process. It will read all the information
     * and store it for this baseInverter in the dataset.
     *
     * In this case *.csv.
     *
     *
     */
    public void dataLoader( boolean init,Integer year ) {
        this.processFiles( init,year );
        super.dataLoader( init,year );
    }
}
