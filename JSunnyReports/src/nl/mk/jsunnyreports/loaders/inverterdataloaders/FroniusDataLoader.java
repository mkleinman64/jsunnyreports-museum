package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.FroniusWFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.FroniusWhFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;

import nl.mk.jsunnyreports.inverters.BaseInverter;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.EndsWithFilenameFilter;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.processors.ArduinoFileProcessor;

import org.apache.log4j.Logger;

/**
 * Fronius logstyle dataloader.
 *
 * Date         Version     Who     What
 *              0.9.0.0     MvdP    First version.
 * 13-09-2010   1.1.1.0     MKL     Moved a lot of code away to other objects.
 * 19-10-2010   1.1.2.0     MKL     Code enhancements.
 * 11-01-2011   1.1.2.0     MvdP    Put some code back
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-12-2011   1.3.2.0     MKL     Removed all inline FilenameFilter classes and modified to them external filefilters.
 * 15-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 18-01-2012   1.3.2.0     MKL     Split dataloader() into seperate method handlers.
 *
 * TODO lets see if we can make this dataloader multithreaded.
 *
 * @author    Martijn van der Pauw ()
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.2.0
 * @since     0.9.0.0
 */
public class FroniusDataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(FroniusDataLoader.class);

    public FroniusDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
    }

    /**
     *
     * @param froniusFile Excelsheet containing Fronius log.
     * @throws DataLoadException
     */
    public void minFileLoad(File froniusFile, boolean init, Integer year ) {
        inverterData.setUpdated(true);
        String dateFormatInFile = "dd/MM/yyyy HH:mm:ss";

        SimpleDateFormat format = new SimpleDateFormat(dateFormatInFile);
        format.setTimeZone(Constants.getLocalTimeZone());

        int lineNumber = 0;
        try {
            Workbook workbook = Workbook.getWorkbook(froniusFile);
            int powerColNr = 0;
            int startrow = 1;
            Sheet sheet = null;
            boolean breakit = false;
            for (int teller = workbook.getNumberOfSheets() - 1; teller >= 0; teller--) {
                sheet = workbook.getSheet(teller);

                for (int teller2 = 1; teller2 < 10; teller2++) {
                    Cell[] testEntry = sheet.getRow(teller2);
                    for (int i = 0; i < testEntry.length; i++) {

                        if (testEntry[i].getContents().toLowerCase().indexOf("[w]") > 0 || testEntry[i].getContents().toLowerCase().indexOf("leistung") > -1) {

                            powerColNr = i;
                            startrow = teller2 + 1;
                            if (sheet.getCell(0, startrow).getType() != jxl.CellType.DATE) {
                                startrow = startrow + 1;
                            }
                            breakit = true;
                            break;
                        }
                    }
                    if (breakit)
                        break;
                }
                if (breakit)
                    break;
            }

            for (int iterator = startrow; iterator < sheet.getRows(); iterator++) {
                lineNumber = iterator;
                Cell timeEntry = sheet.getCell(0, iterator);
                Cell wattEntry = sheet.getCell(powerColNr, iterator);

                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                cal.setTime(((DateCell)timeEntry).getDate());
                
                if ( init ) {
                    inverterData.addInitYearSet( cal );                        
                } else {
                    if (!"".equals(wattEntry.getContents())) {
                        float watt = Float.valueOf(wattEntry.getContents().replace(",", ".")).floatValue();
                        inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), baseInverter, watt, year );
                    }
                    
                }
                

            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + froniusFile.getName() + " could not be found anymore, ignoring it.");
        } catch (BiffException be) {
            log.error("Error processing " + froniusFile.getName() + ". Message: " + be.getMessage());
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + froniusFile.getName());
        }
    }

    public void minFileRemove(File froniusFile) {
        inverterData.setUpdated(true);
        String dateFormatInFile = "dd/MM/yyyy HH:mm:ss";
        int lineNumber = 0;

        SimpleDateFormat format = new SimpleDateFormat(dateFormatInFile);
        format.setTimeZone(Constants.getLocalTimeZone());
        try {
            Workbook workbook = Workbook.getWorkbook(froniusFile);
            int powerColNr = 0;
            int startrow = 1;
            Sheet sheet = null;
            boolean breakit = false;
            for (int teller = workbook.getNumberOfSheets() - 1; teller >= 0; teller--) {
                sheet = workbook.getSheet(teller);

                for (int teller2 = 1; teller2 < 10; teller2++) {
                    Cell[] testEntry = sheet.getRow(teller2);
                    for (int i = 0; i < testEntry.length; i++) {

                        if (testEntry[i].getContents().toLowerCase().indexOf("[w]") > 0 || testEntry[i].getContents().toLowerCase().indexOf("leistung") > -1) {

                            powerColNr = i;
                            startrow = teller2 + 1;
                            if (sheet.getCell(0, startrow).getType() != jxl.CellType.DATE) {
                                startrow = startrow + 1;

                            }
                            breakit = true;
                            break;
                        }
                    }
                    if (breakit)
                        break;
                }
                if (breakit)
                    break;
            }


            for (int iterator = startrow; iterator < sheet.getRows(); iterator++) {
                lineNumber = iterator;
                Cell timeEntry = sheet.getCell(0, iterator);
                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                cal.setTime(((DateCell)timeEntry).getDate());

                // removing the same day over and over again here, small improvement possible here.
                inverterData.removeDayFromSet(baseInverter, cal);
            }

        } catch (IOException e) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + froniusFile.getName());
        } catch (BiffException be) {
            log.error("Error processing " + froniusFile.getName() + ". Message: " + be.getMessage());
        }
    }


    /**
     *
     * @param daysAllFile daysfile to process. Right now, I assume the user has .xls files available. T
     */
    private void daysAllFileLoad(File daysAllFile, boolean init, Integer year ) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String workDate = null;
        try {
            Workbook workbook = Workbook.getWorkbook(daysAllFile);
            Sheet sheet = workbook.getSheet(0);
            int startingrow = 0;
            for (int teller = 0; teller < 10; teller++) {
                if ((sheet.getCell(1, teller).getContents().toLowerCase().indexOf("[wh]") > 0)) {
                    startingrow = teller + 1;
                    break;
                }
            }
            String dateFormat = "";
            for (int iterator = startingrow; iterator < sheet.getRows(); iterator++) {
                lineNumber = iterator;
                Cell timeEntry = sheet.getCell(0, iterator);
                Cell kwhEntry = sheet.getCell(1, iterator);

                workDate = timeEntry.getContents();

                if (!"".equals(kwhEntry.getContents())) {
                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                    // the watt cell is not empty and thus we can read further.
                    DateFormat formatter;
                    if (timeEntry.getContents().indexOf("-") > 0) {
                        if (timeEntry.getContents().indexOf("-") > 2)
                            dateFormat = "yyyy-MM-dd HH:mm:ss";
                        else
                            dateFormat = "dd-MM-yyyy HH:mm:ss";
                    } else {
                        dateFormat = "dd/MM/yyyy HH:mm:ss";
                    }
                    formatter = new SimpleDateFormat(dateFormat);
                    formatter.setTimeZone(Constants.getLocalTimeZone());
                    cal.setTime(formatter.parse(timeEntry.getContents() + " 00:00:00"));
                    
                    if ( init ) {
                        inverterData.addInitYearSet( cal );    
                    } else {
                        int yield = Integer.valueOf(kwhEntry.getContents().replace(",", ".")).intValue();
                        inverterData.addDayYieldForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), baseInverter, yield, year );
                        
                    }


                }
            }
        } catch (ParseException e) {
            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
        } catch (IOException e) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + daysAllFile.getName());
        } catch (BiffException be) {
            log.error("Error processing " + daysAllFile.getName() + ". Message: " + be.getMessage());
        }
    }

    /**
     *
     * @param daysAllFile daysfile to process. Right now, I assume the user has .xls files available. T
     */
    private void daysAllFileRemove(File daysAllFile) {
        inverterData.setUpdated(true);
        int lineNumber = 0;
        String workDate = null;

        try {
            Workbook workbook = Workbook.getWorkbook(daysAllFile);
            Sheet sheet = workbook.getSheet(0);
            int startingrow = 0;
            for (int teller = 0; teller < 10; teller++) {
                if ((sheet.getCell(1, teller).getContents().toLowerCase().indexOf("[wh]") > 0)) {
                    startingrow = teller + 1;
                    break;
                }
            }
            String dateFormat = "";
            for (int iterator = startingrow; iterator < sheet.getRows(); iterator++) {
                lineNumber = iterator;
                Cell timeEntry = sheet.getCell(0, iterator);
                Cell kwhEntry = sheet.getCell(1, iterator);

                workDate = timeEntry.getContents();

                if (!"".equals(kwhEntry.getContents())) {
                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());

                    // the watt cell is not empty and thus we can read further.
                    DateFormat formatter;
                    if (timeEntry.getContents().indexOf("-") > 0) {
                        if (timeEntry.getContents().indexOf("-") > 2)
                            dateFormat = "yyyy-MM-dd HH:mm:ss";
                        else
                            dateFormat = "dd-MM-yyyy HH:mm:ss";
                    } else {
                        dateFormat = "dd/MM/yyyy HH:mm:ss";
                    }
                    formatter = new SimpleDateFormat(dateFormat);
                    formatter.setTimeZone(Constants.getLocalTimeZone());
                    cal.setTime(formatter.parse(timeEntry.getContents() + " 00:00:00"));

                    inverterData.removeDayFromSet(baseInverter, cal);

                }
            }
        } catch (ParseException e) {
            log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDate + "\" as a correct date. ");
        } catch (IOException e) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + daysAllFile.getName());
        } catch (BiffException be) {
            log.error("Error processing " + daysAllFile.getName() + ". Message: " + be.getMessage());
        }
    }

    private void process_Wh_Files( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new FroniusWhFilenameFilter(), true);

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.daysAllFileRemove(newFile);
            }
            if (f.isToLoad()) {
                this.daysAllFileLoad(newFile, init, year );
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void process_W_Files( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new FroniusWFilenameFilter(), true);
        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.minFileRemove(newFile);
            }
            if (f.isToLoad()) {
                this.minFileLoad(newFile, init, year );
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
    @Override
    public void dataLoader( boolean init, Integer year ) {
        this.process_Wh_Files( init, year );
        this.process_W_Files( init, year );
        super.dataLoader( init, year );
    }
}
