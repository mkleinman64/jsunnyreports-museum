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
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SolarmaxMaxtalkFileProcessor;

import org.apache.log4j.Logger;

/**
 * Solarmax Maxtalk logfile parser, (***.txt )
 *
 *
 * Date         Version     Who     What
 * 03-01-2011   1.1.2.2     MKL     First release
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 16-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 *
 * @author    Martin Kleinman
 * @version   1.3.2.0
 * @since     1.1.2.2
 */
public class SolarmaxMaxtalkDataLoader extends BaseLoader implements LoaderInterface {


    public SolarmaxMaxtalkDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
    }

    private static final Logger log = Logger.getLogger(SolarmaxMaxtalkDataLoader.class);

    private void logFileRemove(File logFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yyyy");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String date = "";
        //;;wr_pac;wr_uac_l1;wr_iac_l1;wr_e_day;wr_e_month;temp_lt;
        //01.11.2010;15:48:21;205.00;233.30;0.86;7.40;7.00;30.00;

        try {
            CSVReader reader = new CSVReader(new FileReader(logFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 5);
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
            log.error("Somehow the file " + logFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + logFile.getName());
        }

    }

    private void processFiles( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".txt"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.logFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable fileData = new SolarmaxMaxtalkFileProcessor(newFile, inverterData, baseInverter, settings, init, year,f.getFc()  );
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
    public void dataLoader( boolean init, Integer year ) {
        this.processFiles( init, year );
        super.dataLoader( init, year );
    }
}
