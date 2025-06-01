package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import au.com.bytecode.opencsv.CSVReader;

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
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.RegularExpressionFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;

import nl.mk.jsunnyreports.inverters.SunnyBeamBluetoothInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.EndsWithFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SunnybeamBluetoothFileProcessor;

import org.apache.log4j.Logger;

/**
 *
 * Date         Version     Who     What
 * 04-10-2010   1.1.0.0     MKL     Fixed SB Bluetooth read error ( bzh ). File was mistaken for newFormat while it was the old firmware version
 * 19-10-2010   1.1.2.0     MKL     Code improvements.
 * 02-11-2010   1.1.2.0     MKL     Code fix for new firmware upgrade where "DST" is removed from the header.
 *                                  jSunnyreports now checks for the last column.
 * 11-11-2010   1.1.2.1     MKL     Fixed bug ( Maclu ) bugid 177. some files could not be read.
 * 11-01-2011   1.1.2.3     MKL     Fixed issue 173 for new firmware.
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 15-12-2011   1.3.2.0     MKL     Finally fixed the sunnybeam bluetooth bug!!!!!!
 * 16-12-2011   1.3.2.0     MKL     Fixed the Stephane van Hal bug ( ignore yyyy-MM.csv files.
 * 17-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 *                          MKL     Removed check for optionsfield, this is moved to the loaderprogram.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 * 04-06-2012   1.3.2.0B4   MKL     Added fix for 304, and 309
 * 20-09-2012   1.3.3.0B1   MKL     Fixed issue 312, updated regular expression., also 317 is fixed.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.3.0
 * @since     1.0.0.0
 */
public class SunnyBeamBTDataLoader extends BaseLoader implements LoaderInterface {

    private SunnyBeamBluetoothInverter inverter;

    private static final Logger log = Logger.getLogger(SunnyBeamBTDataLoader.class);

    public SunnyBeamBTDataLoader(SunnyBeamBluetoothInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);

        this.inverter = inverter;
    }

    private boolean isNewFirmware(File csvFile) {
        boolean isNewFirmware = false;
        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile), ';');
            List<String[]> workSheet = new ArrayList<String[]>();
            workSheet = reader.readAll();

            int startRow = 7;
            String ColARow0 = workSheet.get(0)[0].toUpperCase();
            if (ColARow0.equals("SEP=")) {
                startRow++;
            }

            //the whole file is now in the workSheet object. now we have to test if it is the old
            //format or the new and dispatch the right loader accordingly.
            String ColARow8 = workSheet.get(startRow)[0].toUpperCase();
            String ColBRow8 = workSheet.get(startRow)[1].toUpperCase();

            if (ColARow8.contains("YYYY") && ColBRow8.contains("KWH")) {
                // old file format.
                isNewFirmware = false;

            } else {
                // new file format.
                isNewFirmware = true;
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + csvFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured attempting to determine the right firmware format in file: " + csvFile.getName());
        }

        return isNewFirmware;
    }


    private void removeOldFirmwareFile(File csvFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile), ';');
            List<String[]> workSheet = new ArrayList<String[]>();
            workSheet = reader.readAll();

            String workDateTime = "";
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            formatter.setTimeZone(Constants.getLocalTimeZone());

            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

            // now we have the following problem people are using various dateformats
            // dd.MM.yyyy
            // dd/MM/yyyy
            // etc. so lets first replace all the possible seperators
            workDateTime = workSheet.get(workSheet.size() - 1)[0];

            workDateTime = workDateTime.replace(".", "-"); // point replaced
            workDateTime = workDateTime.replace("/", "-"); // forward slash replaced

            lineNumber = workSheet.size() - 1;
            try {
                cal.setTime(formatter.parse(workDateTime));
                inverterData.removeDayFromSet(inverter, cal);

            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + csvFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + csvFile.getName());
        }

    }


    private void removeNewFirmwareFile(File csvFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        try {

            CSVReader reader = new CSVReader(new FileReader(csvFile), ';');
            List<String[]> workSheet = new ArrayList<String[]>();
            workSheet = reader.readAll();

            String configOptionsString = workSheet.get(1)[0].replace("|", ",");
            String[] configOptionsArray = configOptionsString.split(",");

            String fileDate = configOptionsArray[configOptionsArray.length - 1];
            fileDate = fileDate.replace(".", "-"); // point replaced
            fileDate = fileDate.replace("/", "-"); // forward slash replaced

            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            formatter.setTimeZone(Constants.getLocalTimeZone());

            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

            try {

                cal.setTime(formatter.parse(fileDate));

                inverterData.removeDayFromSet(inverter, cal);

            } catch (ParseException pe) {
                log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + fileDate + "\" as a correct date. ");
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + csvFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + csvFile.getName());
        }
    }


    private void SunnyBeamCSVFileRemove(File CSVFile) {

        if (isNewFirmware(CSVFile)) {
            this.removeNewFirmwareFile(CSVFile);
        } else {
            this.removeOldFirmwareFile(CSVFile);
        }

    }

    private void processFiles(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^([0-9][0-9][-][0-9][0-9][-][0-9][0-9])+\\.csv$"), true );

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.SunnyBeamCSVFileRemove(newFile);
            }
            if ( f.isToLoad() ) {
                Runnable fileData = new SunnybeamBluetoothFileProcessor(newFile, inverterData, inverter, settings, init, year,f.getFc() );
                executor.execute(fileData);
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
    @Override
    public void dataLoader(boolean init, Integer year) {
        this.processFiles(init, year );
        super.dataLoader(init, year );
    }
}
