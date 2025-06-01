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
import java.util.TimeZone;

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
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.DiehlIntervalFileProcessor;

import org.apache.log4j.Logger;

/**
 * Dataloader for reading Diehl baseInverter style files. Ending with _I.txt and _D.txt
 * _I are the Interval based files.
 * _D are the Day based files ( summary for each day ).
 *
 * Date         Version     Who     What
 * 13-10-2010   1.1.1.0     MKL     Moved datareading to dataset instead of here.
 * 19-10-2010   1.1.2.0     MKL     Improved code.
 * 13-11-2010   1.1.2.2     MKL     Bugfix for crashing dataloader probably due to code improvements ( didn't test. )
 *                                  MKL is slapping himself. Mental note.. regression tests!
 * 19-01-2011   1.1.2.4     MKL     Bugfix for other dateformats as dd.mm.yyyy
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 14-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 *
 * @author Martin Kleinman
 * @version 1.3.2.0
 * @since 0.8.0.0
 */
public class DiehlDataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(DiehlDataLoader.class);

    public DiehlDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
    }

    private String determineDateFormat(String date) {
        String finalFormat = null;
        DateFormat formatter = null;
        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

        try {
            String dateFormat = "dd-MMM-yyyy";
            formatter = new SimpleDateFormat(dateFormat);
            cal.setTime(formatter.parse(date));

            finalFormat = dateFormat;

        } catch (Exception e) {
            // lets do nothing

        }

        try {
            String dateFormat = "dd.MM.yyyy";
            formatter = new SimpleDateFormat(dateFormat);
            cal.setTime(formatter.parse(date));

            finalFormat = dateFormat;

        } catch (Exception e) {
            // we do nothing

        }

        return finalFormat;

    }

    private void diehlIntervalFileRemove(File IFile) {
        inverterData.setUpdated(true);
        String previousWorkDate = "";
        String workDate = "";

        boolean headerFound = false;

        int lineNumber = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader(IFile), ';');
            String[] nextLine;

            try {
                while ((nextLine = reader.readNext()) != null) {

                    lineNumber++;
                    if (headerFound == true && nextLine.length >= 9) {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                        workDate = nextLine[0];
                        if (!workDate.equals(previousWorkDate)) {

                            String dateFormat = determineDateFormat(workDate);
                            DateFormat formatter = new SimpleDateFormat(dateFormat);
                            formatter.setTimeZone(Constants.getLocalTimeZone());

                            cal.setTime(formatter.parse(workDate));

                            inverterData.removeDayFromSet(baseInverter, cal);
                            previousWorkDate = workDate;
                        }
                    }

                    // its unknown where the actual header is, so first order of business is to find it.
                    if (!headerFound && nextLine[0].equals("Date")) {
                        headerFound = true;
                    }
                }
            } catch (IOException IOe) {
                log.error("An error has occured reading line: " + lineNumber + " in file: " + IFile.getName());
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + IFile.getName() + " could not be found anymore, ignoring it.");
        }

    }

    /**
     * This file reads the file DFile which is the day Diehl file.
     *
     *
     * @param DFile Diehl Day file ( ending with _D.txt )
     * @throws DataLoadException
     *
     */
    private void diehlDayFileLoad(File DFile, boolean init, Integer year ) {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        String workDate = "";
        String workEnergyWh = "";

        boolean headerFound = false;

        try {

            CSVReader reader = new CSVReader(new FileReader(DFile), ';');
            String[] nextLine;

            try {
                while ((nextLine = reader.readNext()) != null) {
                    lineNumber++;
                    if (headerFound == true) {
                        workDate = nextLine[0] + " " + "23:59:00";
                        workEnergyWh = nextLine[13];

                        String dateFormat = determineDateFormat(workDate) + " HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(dateFormat);
                        formatter.setTimeZone(Constants.getLocalTimeZone());

                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(workDate));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );    
                        } else {
                            int wh = Integer.parseInt(workEnergyWh);

                            inverterData.addDayYieldForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), baseInverter, wh, year );
                            
                        }
                        

                    }

                    // Unknown where header starts, trying to find it.
                    if (nextLine[0].equals("Date")) {
                        headerFound = true;
                    }
                }
            } catch (IOException IOe) {
                log.error("An error has occured reading line: " + lineNumber + " in file: " + DFile.getName());
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + DFile.getName() + " could not be found anymore, ignoring it.");
        }
    }

    /**
     * This file reads the file DFile which is the day Diehl file.
     *
     *
     * @param DFile Diehl Day file ( ending with _D.txt )
     * @throws DataLoadException
     *
     */
    private void diehlDayFileRemove(File DFile) {
        inverterData.setUpdated(true);
        String workDate = "";
        String workEnergyWh = "";
        int lineNumber = 0;

        boolean headerFound = false;

        try {
            CSVReader reader = new CSVReader(new FileReader(DFile), ';');
            String[] nextLine;
            try {
                while ((nextLine = reader.readNext()) != null) {
                    lineNumber++;

                    if (headerFound == true) {
                        workDate = nextLine[0] + " " + "23:59:00";
                        workEnergyWh = nextLine[13];

                        String dateFormat = determineDateFormat(workDate) + " HH:mm:ss";
                        DateFormat formatter = new SimpleDateFormat(dateFormat);
                        formatter.setTimeZone(Constants.getLocalTimeZone());

                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(workDate));

                        inverterData.removeDayFromSet(baseInverter, cal);
                    }

                    // Unknown where header starts, trying to find it.
                    if (nextLine[0].equals("Date")) {
                        headerFound = true;
                    }
                }

            } catch (IOException IOe) {
                log.error("An error has occured reading line: " + lineNumber + " in file: " + DFile.getName());
            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + DFile.getName() + " could not be found anymore, ignoring it.");
        }

    }

    private void process_I_Files( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter("_I.txt"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.diehlIntervalFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable diehlData = new DiehlIntervalFileProcessor(newFile, inverterData, baseInverter, settings, init, year ,f.getFc());
                executor.execute(diehlData);
            }

        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }           
    }

    private void process_D_Files( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new EndsWithFilenameFilter("_D.txt"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.diehlDayFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                this.diehlDayFileLoad(newFile, init, year );
            }

        }    
        executor.shutdown();
        while (!executor.isTerminated()) {
        }           
    }

    /**
     * dataLoader() reads all data.
     *
     * @throws DataLoadException
     */
    @Override
    public void dataLoader(boolean init, Integer year ) {

        this.process_I_Files(init, year );
        this.process_D_Files(init, year );
        super.dataLoader(init, year );
    }
}
