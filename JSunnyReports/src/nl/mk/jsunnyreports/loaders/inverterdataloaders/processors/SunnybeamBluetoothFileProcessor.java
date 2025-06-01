package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

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

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.inverters.SunnyBeamBluetoothInverter;

import org.apache.log4j.Logger;

public class SunnybeamBluetoothFileProcessor extends BaseProcessor  implements Runnable {

    private static final Logger log = Logger.getLogger(SunnybeamBluetoothFileProcessor.class);    
    
    public SunnybeamBluetoothFileProcessor(File theFile, InverterData inverterData, SunnyBeamBluetoothInverter sbInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.sbInverter = sbInverter;
        this.settings = settings;
        this.year = year;
        this.init = init;
        this.fc = fc;
    }

    private SunnyBeamBluetoothInverter sbInverter;

    
    public void run() {
        if (isNewFirmware()) {
            this.processNewFirmware();
        } else {
            this.processOldFirmware();
        }
    }

    private boolean isNewFirmware() {
        boolean isNewFirmware = false;
        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';');
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
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured attempting to determine the right firmware format in file: " + theFile.getName());
        }

        return isNewFirmware;
    }    

    /**
     *
     * @param CSVFile
     * @throws DataLoadException
     * @since
     */
    private void processOldFirmware() {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';');
            List<String[]> workSheet = new ArrayList<String[]>();
            workSheet = reader.readAll();

            int kwhColumnLocation = sbInverter.getM_kWhColumnLocation();

            String workDateTime = "";
            String previousworkDateTime = "";

            String previousKwh = "";
            String workkWh = "";

            DateFormat formatter;
            // default formatter. maybe going to overwrite in case of US (AM/PM) issue 309
            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            formatter.setTimeZone(Constants.getLocalTimeZone());

            for (int i = 9; i <= workSheet.size() - 1; i++) {
                lineNumber = i;
                // with multiple inverters there is a chance not all columns are filled. e.g. a readpoint was missed
                // this leaves empty places in the CSV which might damage / corrupt the reading
                // thus we have to first check whether the line we are reading is long enough for the field to exist.
                // if its long enough we can process the line

                if (workSheet.get(i).length > kwhColumnLocation - 1) {
                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    Calendar previousCalendar = Calendar.getInstance(Constants.getLocalTimeZone());

                    previousworkDateTime = workDateTime;

                    // now we have the following problem people are using various dateformats
                    // dd.MM.yyyy
                    // dd/MM/yyyy
                    // etc. so lets first replace all the possible seperators
                    workDateTime = workSheet.get(i)[0];
                    workDateTime = workDateTime.replace(".", "-"); // point replaced
                    workDateTime = workDateTime.replace("/", "-"); // forward slash replaced

                    //workaround for issue 309 Art Schenk.
                    if (workDateTime.toLowerCase().contains("am") || workDateTime.toLowerCase().contains("pm")) {
                        formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm aaa");
                        formatter.setTimeZone(Constants.getLocalTimeZone());

                    }

                    previousKwh = workkWh; // this one is needed to calculate the amount of watt during this period.
                    // was (i)[1] going to be (i)[readerColumn-1]
                    workkWh = workSheet.get(i)[kwhColumnLocation - 1];
                    workkWh = workkWh.replace("-,---", "0"); // fixed issue 304

                    if (("".equals(previousKwh) == false) && ("".equals(workkWh) == false)) {

                        try {
                            previousCalendar.setTime(formatter.parse(previousworkDateTime));
                            cal.setTime(formatter.parse(workDateTime));
                            
                            if ( init ) {
                                inverterData.addInitYearSet( cal );    
                            } else {
                                float timeDifferenceInMins = (cal.getTimeInMillis() - previousCalendar.getTimeInMillis()) / 60000f;

                                float kWh = Float.valueOf(workkWh.replace(",", ".")).floatValue();
                                float previouskWh = Float.valueOf(previousKwh.replace(",", ".")).floatValue();
                                float yieldkWh = kWh - previouskWh;
                                float watt = (yieldkWh * 1000) / ((float)timeDifferenceInMins / 60f);

                                boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), sbInverter, watt, year );

                            }
                            


                        } catch (ParseException pe) {
                            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
                        }

                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }
    }

    /**
     *
     * @param  csvFile
     * @throws DataLoadException
     * @since
     */
    private void processNewFirmware() {
        inverterData.setUpdated(true);
        int lineNumber = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader(theFile), ';');
            List<String[]> workSheet = new ArrayList<String[]>();
            workSheet = reader.readAll();

            int kwhColumnLocation = sbInverter.getM_kWhColumnLocation();

            String configOptionsString = workSheet.get(1)[0].replace("|", ",");
            String[] configOptionsArray = configOptionsString.split(",");

            String fileDate = configOptionsArray[configOptionsArray.length - 1];
            fileDate = fileDate.replace(".", "-"); // point replaced
            fileDate = fileDate.replace("/", "-"); // forward slash replaced

            String workWatt = "";
            DateFormat formatter;

            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            formatter.setTimeZone(Constants.getLocalTimeZone());

            // determine start and end positions to read
            // one inverter might be missing from the file. We cannot process the column directly
            // we use it indirectly instead through two booleans..
            int startPos = 8;
            float startEntry = -1;
            boolean startLineFound = false;

            while (startLineFound == false && startPos <= workSheet.size() - 4) {
                if (workSheet.get(startPos).length > kwhColumnLocation - 1) {

                    String evaluateString = workSheet.get(startPos)[kwhColumnLocation - 1];

                    if (evaluateString.contains("-")) {
                        evaluateString = "0";
                    }

                    float watt = new Float(evaluateString.replace(",", ".")).floatValue() * 1000;

                    if (startEntry == -1) {
                        startEntry = watt;
                    } else {
                        if (watt != startEntry) {
                            startLineFound = true;
                        }
                    }
                }

                if (!startLineFound) {
                    startPos++;
                }
            }

            int endPos = workSheet.size() - 5;
            boolean endLineFound = false;

            while (endLineFound == false && endPos > 8) {
                if (workSheet.get(endPos).length > kwhColumnLocation - 1) {

                    String evaluateString = workSheet.get(endPos)[kwhColumnLocation - 1];

                    if (evaluateString.contains("-")) {
                        evaluateString = "0";
                    }

                    float watt = new Float(evaluateString.replace(",", ".")).floatValue() * 1000;

                    if (startEntry == -1) {
                        startEntry = watt;
                    } else {

                        if (watt != startEntry) {
                            endLineFound = true;
                        }
                    }

                }

                if (!endLineFound) {
                    endPos--;
                }

            }

            // we found the "raw" start and endpos. now fetch one record more so it will
            // start at 0 if necessary.
            if (startPos > 8) {
                startPos--;
            }
            if (endPos < workSheet.size() - 4) {
                endPos++;
            }

            //log.info( "startPos: " + startPos + " endPos: " + endPos );

            // new format starts at line 8
            // and we ignore the bottom lines with the summary.
            for (int i = startPos; i <= endPos; i++) {
                lineNumber = i;
                // with multiple inverters there is a chance not all columns are filled. e.g. a readpoint was missed
                // this leaves empty places in the CSV which might damage / corrupt the reading
                // thus we have to first check whether the line we are reading is long enough for the field to exist.
                // if its long enough we can process the line

                String workDateTime = null;
                if (workSheet.get(i).length > kwhColumnLocation - 1) {

                    try {
                        workDateTime = fileDate + " " + workSheet.get(i)[0];
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(workDateTime));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );    
                        } else {
                            workWatt = workSheet.get(i)[kwhColumnLocation - 1];
                            if (workWatt.contains("-")) {
                                workWatt = "0";
                            }

                            float watt = new Float(workWatt.replace(",", ".")).floatValue() * 1000;
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), sbInverter, watt, year );
                            
                        }


                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
                    }

                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }

    }
}
