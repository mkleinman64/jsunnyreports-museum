package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import au.com.bytecode.opencsv.CSVReader;

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
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SolarmaxPDLFileProcessor;

import org.apache.log4j.Logger;

/**
 * Solarmax dataloader. Reads .pdl files. ( primary data toLoad )
 *
 * Date         Version     Who     What
 * 19-10-2010   1.1.2.0     MKL     Huge code improvements.
 * MKL     Removed unnessary parsing from loader.
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 16-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 1.3.2.0
 * @since 1.1.0.0beta
 */
public class SolarmaxPDLDataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(SolarmaxPDLDataLoader.class);

    public SolarmaxPDLDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
    }

    private void PDLFileRemove(File PDLFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String date;
        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        try {

            CSVReader reader = new CSVReader(new FileReader(PDLFile), '\t');
            String[] nextLine = reader.readNext();
            date = nextLine[0].substring(1);

            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

            try {
                cal.setTime(formatter.parse(date));
                inverterData.removeDayFromSet(baseInverter, cal);
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + date + "\" as a correct date. ");
            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + PDLFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + PDLFile.getName());
        }
    }

    private void processFiles(boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".pdl"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.PDLFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable solarMaxData = new SolarmaxPDLFileProcessor(newFile, inverterData, baseInverter, settings, init, year,f.getFc()  );
                executor.execute(solarMaxData);
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
     * In this case *.pdl and PDL_ma.xls
     *
     *
     */
    public void dataLoader(boolean init,Integer year) {
        this.processFiles(init, year );
        super.dataLoader(init, year);
    }
}
