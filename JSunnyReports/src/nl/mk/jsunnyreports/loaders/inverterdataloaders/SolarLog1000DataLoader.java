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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.RegularExpressionFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.StartsWithFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;

import nl.mk.jsunnyreports.inverters.SolarLog1000Inverter;
import nl.mk.jsunnyreports.inverters.SolarLogInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.EndsWithFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;

import org.apache.log4j.Logger;

/**
 * Solarlog1000DataLoader.java
 *
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 2.0.7
 * @since 2.0.7
 */
public class SolarLog1000DataLoader extends BaseLoader implements LoaderInterface {
    private static final Logger log = Logger.getLogger(SolarLog1000DataLoader.class);

    private SolarLog1000Inverter inverter;

    public SolarLog1000DataLoader(SolarLog1000Inverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
        this.inverter = inverter;
    }

    /**
     *
     * @param minFile
     */
    private void min_load(File minFile, boolean init, Integer year) {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        int wattColumnLocation = 0;
        try {
            wattColumnLocation = inverter.getM_WattColumnLocation();
        } catch (Exception e) {
            wattColumnLocation = 4;
        }

        String workDate = "";
        String workTime = "";
        String workWatt = "";

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {

            CSVReader reader = new CSVReader(new FileReader(minFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            for (int i = wholeFile.size() - 1; i >= 1; i--) {
                lineNumber = i;
                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                workDate = wholeFile.get(i)[0];
                workDate = workDate.replace("/", ".");

                workTime = wholeFile.get(i)[1];
                
                if ( wholeFile.get(i).length >= wattColumnLocation ) {
                    try {
                        workWatt = wholeFile.get(i)[wattColumnLocation - 1];
                    } catch ( Exception e ) {
                        log.error( wattColumnLocation );
                        log.error( inverter.getM_InverterName() );
                        e.printStackTrace();
                        System.exit(100);
                    }

                    try {
                        cal.setTime(formatter.parse(workDate + " " + workTime));
                        if (init) {
                            inverterData.addInitYearSet(cal);
                        } else {


                            float watt = Float.valueOf(workWatt).floatValue();
                            boolean added =
                                inverterData.addWattEntryForInverter(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), inverter, watt,
                                                                     year);

                        }


                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + " " + workTime + "\" as a correct date. ");
                    }
                    
                } else {
                    log.warn( "line " + lineNumber + " does not have enough columns for inverter " + inverter.getM_InverterName() );
                }
                
            }


        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + minFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + minFile.getName());
        }

    }


    /**
     *
     * @param minFile min****.csv to be removed from the set.
     */
    private void min_remove(File minFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String previousWorkDate = "";
        String workDate = "";


        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yy");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {
            CSVReader reader = new CSVReader(new FileReader(minFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            for (int i = wholeFile.size() - 1; i >= 1; i--) {

                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                workDate = wholeFile.get(i)[0];
                workDate = workDate.replace("/", ".");


                if (!workDate.equals(previousWorkDate)) {
                    try {
                        cal.setTime(formatter.parse(workDate));
                        inverterData.removeDayFromSet(inverter, cal);
                        previousWorkDate = workDate;
                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + minFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + minFile.getName());
        }

    }


    /**
     * @param daysAllFile daysfile to process
     */
    private void day_load(File daysAllFile, boolean init, Integer year) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String workDate = "";
        String workEnergyWh = "";
        int inverterID = -1;

        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());


        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        try {
            CSVReader reader = new CSVReader(new FileReader(daysAllFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                workDate = nextLine[0] + " " + "23:59:59";
                workDate = workDate.replace("/", ".");
                inverterID = Integer.parseInt(nextLine[1]);
                workEnergyWh = nextLine[2];

                try {
                    cal.setTime(formatter.parse(workDate));
                    if (inverterID == inverter.getM_InverterID()) {
                        if (init) {
                            inverterData.addInitYearSet(cal);
                        } else {
                            int wh = Integer.valueOf(workEnergyWh).intValue();
                            inverterData.addDayYieldForInverter(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), inverter, wh, year);
                        }
                    }
                } catch (ParseException pe) {
                    log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + daysAllFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + daysAllFile.getName());
        }

    }


    /**
     *
     * @param daysAllFile daysfile to process
     */
    private void day_remove(File daysAllFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        String workDate = "";
        int inverterID = -1;

        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        try {
            CSVReader reader = new CSVReader(new FileReader(daysAllFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {

                workDate = nextLine[0] + " " + "23:59:59";
                workDate = workDate.replace("/", ".");
                inverterID = Integer.parseInt(nextLine[1]);

                if (inverterID == inverter.getM_InverterID()) {
                    try {
                        cal.setTime(formatter.parse(workDate));

                        inverterData.removeDayFromSet(inverter, cal);

                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
                    }

                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + daysAllFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + daysAllFile.getName());
        }

    }

    /**
     *
     * @param init
     * @param year
     */
    private void process_dayexport(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^export_day.csv$"), true);

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.day_remove(newFile);
            }
            if (f.isToLoad()) {
                this.day_load(newFile, init, year);
            }

        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    /**
     *
     * @param init
     * @param year
     */
    private void process_minexport(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^export_min.csv$"), true);

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.min_remove(newFile);
            }
            if (f.isToLoad()) {
                this.min_load(newFile, init, year);
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
     *
     */
    public void dataLoader(boolean init, Integer year) {
        this.process_dayexport(init, year);
        this.process_minexport(init, year);
        super.dataLoader(init, year);
    }
}
