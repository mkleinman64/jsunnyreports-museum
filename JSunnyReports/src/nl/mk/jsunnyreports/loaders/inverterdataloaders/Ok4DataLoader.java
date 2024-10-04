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

import nl.mk.jsunnyreports.inverters.OK4EInverter;


import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.OK4EFileProcessor;

import org.apache.log4j.Logger;

/**
 * OK4E logfile parser, (***.log )
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
 *
 * @author    Martin Kleinman
 * @version   1.3.2.0
 * @since     1.1.0.0beta5
 */
public class Ok4DataLoader extends BaseLoader implements LoaderInterface {

    private OK4EInverter inverter;

    public Ok4DataLoader(OK4EInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
        this.inverter = inverter;
    }

    private static final Logger log = Logger.getLogger(Ok4DataLoader.class);

    private void logFileRemove(File logFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String date = "";

        //D [d-mm-yyyy];t [hh:mm];sn;E [Wh];Pac [W];Vac [V];Iac [A];Vdc [V];T [C]
        //24-05-2010;12:54;046091;730353;54;224;0.213;31.5;28.5

        try {
            CSVReader reader = new CSVReader(new FileReader(logFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            String[] nextLine = reader.readNext();
            date = nextLine[0];

            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
            cal.setTime(formatter.parse(date));

            inverterData.removeDayFromSet(inverter, cal);

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + logFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + logFile.getName());
        } catch (ParseException pe) {
            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + date + "\" as a correct date. ");
        }

    }

    private void processFiles(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter(".log"), true);

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.logFileRemove(newFile);
            }
            if (f.isToLoad()) {
                Runnable ok4eData = new OK4EFileProcessor(newFile, inverterData, inverter, settings, init, year, f.getFc());
                executor.execute(ok4eData);
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
        this.processFiles(init, year);
        super.dataLoader(init, year);
    }
}
