package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.helpers.UnicodeBOMInputStream;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.RegularExpressionFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;

import nl.mk.jsunnyreports.inverters.SunnyExplorerInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.EndsWithFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.SunnyExplorerFileProcessor;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.utilities.SunnyExplorerUtilities;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.log4j.Logger;

/**
 * SunnyExplorerDataLoader.java
 *
 * Responsible for processing and loading Sunny Explorer files.
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 2.0.6
 * @since 1.1.0.0beta
 */
public class SunnyExplorerDataLoader extends BaseLoader implements LoaderInterface {

    private SunnyExplorerInverter inverter;

    private static final Logger log = Logger.getLogger(SunnyExplorerDataLoader.class);

    public SunnyExplorerDataLoader(SunnyExplorerInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
        this.inverter = inverter;
    }

    /**
     * Removes a specific day from the set for this inverter.
     * This is done by finding the date of this specific file and then remove it from the set.
     *
     * @param csvFile CSVFile to be removed from memory
     */
    private void csvDetailFileRemove(File csvFile) {
        int lineNumber = 0;

        List<String[]> wholeFile = new ArrayList<String[]>();
        try {
            FileInputStream fis = new FileInputStream(csvFile);
            UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);

            // default
            String encoding = "UTF-8";

            if (ubis.getBOM().toString().contains("UTF-16")) {
                encoding = "UTF-16";
            }
            if (ubis.getBOM().toString().contains("UTF-32")) {
                encoding = "UTF-32";
            }

            CSVReader reader = new CSVReader(new InputStreamReader(ubis, encoding), ';');
            wholeFile = reader.readAll();

            if (wholeFile != null) {
                inverterData.setUpdated(true);
                String workDateTime = "";

                DateFormat formatter; // formatter has to be read dynamically from inside the file.
                formatter = SunnyExplorerUtilities.GetDateFormat(wholeFile);

                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                lineNumber = 10;
                workDateTime = wholeFile.get(10)[0];

                // extra check if dateTime contains two ":"
                String time = workDateTime.substring(11);
                if (time.length() == 5) {
                    workDateTime = workDateTime + ":00";
                }

                try {
                    cal.setTime(formatter.parse(workDateTime));
                    inverterData.removeDayFromSet(inverter, cal);
                } catch (ParseException pe) {
                    log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + csvFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + csvFile.getName());
        }
    }

    public void csvMonthProcess(File csvFile, boolean init, int year) {
        int lineNumber = 0;

        String workDate = "";
        String workkWh = "";
        DateFormat formatter; // formatter has to be read dynamically from inside the file.

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {
            FileInputStream fis = new FileInputStream(csvFile);
            UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);

            // default
            String encoding = "UTF-8";

            if (ubis.getBOM().toString().contains("UTF-16")) {
                encoding = "UTF-16";
            }
            if (ubis.getBOM().toString().contains("UTF-32")) {
                encoding = "UTF-32";
            }

            CSVReader reader = new CSVReader(new InputStreamReader(ubis, encoding), ';');
            wholeFile = reader.readAll();

            formatter = SunnyExplorerUtilities.GetDateFormat(wholeFile);

            for (int i = 9; i <= wholeFile.size() - 1; i++) {
                lineNumber = i;
                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                if ( wholeFile.get(i).length >= 3) {
                    workDate = wholeFile.get(i)[0];
                    workkWh = wholeFile.get(i)[2];

                    try {
                        cal.setTime(formatter.parse(workDate));

                        if (init) {
                            inverterData.addInitYearSet(cal);
                        } else {
                            float kWh = 0f;
                            try {
                                kWh = Float.valueOf(workkWh.replace(",", ".")).floatValue();
                                int wh = (int)(kWh * 1000);
                                inverterData.addDayYieldForInverter(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), baseInverter, wh, year);

                            } catch (NumberFormatException nfe) {
                                log.warn("Warning: " + workkWh + " is not a valid value, 0 is assumed. Date: " + workDate);
                                kWh = 0f;
                            }
                        }
                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
                    }
                    
                }

            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + csvFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + csvFile.getName());
        }
    }

    /**
     * Process the daily files for this inverter. Locates the files to be processed using a regular expression.
     * FileEntries then are returned to see which files need to be loaded, updated or deleted from memory.
     *
     * @param init true, cache is invalid and its the first run, false, regular run
     * @param year the year to be processed ( only used whtn init = true )
     */
    private void processDaily(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^(?!.*[U|u]ser)(?!.*-[S|s]pot-).*[-0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].csv"), true);

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.csvDetailFileRemove(newFile);
            }
            if (f.isToLoad()) {
                Runnable fileData = new SunnyExplorerFileProcessor(newFile, inverterData, inverter, settings, init, year, f.getFc());
                executor.execute(fileData);
            }

        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    /**
     * Process the monhly files create by Sunny Explorer. These files have a different filename compared to the daily files.
     * FileEntries then are returned to see which files need to be loaded, updated or deleted from memory.
     *
     * @param init true, cache is invalid and its the first run, false, regular run
     * @param year the year to be processed ( only used whtn init = true )
     */
    private void processMonthly(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^.*-[0-9][0-9][0-9][0-9][0-9][0-9].csv"), true);

        for (FileEntry f : fe.getFileList()) {
            // this file has changed.
            if (f.isToLoad()) {
                File newFile = new File(f.getFileLocation());
                csvMonthProcess( newFile, init, year );
            }
        }
    }


    /**
     *
     * @param init true, cache is invalid and its the first run, false, regular run
     * @param year the year to be processed ( only used whtn init = true )
     */
    public void dataLoader(boolean init, Integer year) {
        this.processDaily(init, year);
        
        // monthly files are only processsed when this specific config item is available in inverters.conf
        // when not available -1 is used.
        if ( inverter.getO_kWhMonthLocation() != -1 ) {
            this.processMonthly(init, year);
        }
        
        super.dataLoader(init, year);
    }


}
