package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import java.io.File;


import java.io.IOException;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.RegularExpressionFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;


import nl.mk.jsunnyreports.inverters.GrowattInverter;


import org.apache.log4j.Logger;

/**
 * GrowattXLSDataLoader.java
 *
 *
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   2.5.0
 * @since     2.5.0
 */

public class GrowattXLSDataLoader extends BaseLoader implements LoaderInterface {

    private static final Logger log = Logger.getLogger(GrowattXLSDataLoader.class);

    private GrowattInverter growattInverter;

    public GrowattXLSDataLoader(GrowattInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);
        this.growattInverter = inverter;
    }

    /**
     * Removes a month file from the set in memory by parsing the file, getting the date
     * and remove it using the date.
     *
     * @param xls the excelsheet containing month data to remove.
     */
    private void monthRemove(File xls) {
        inverterData.setUpdated(true);

        String filename = xls.getName().substring(0, xls.getName().length() - 4);

        String split[] = filename.split("-");

        int yearNum = Integer.parseInt(split[1].trim());
        int monthNum = Integer.parseInt(split[2].trim());

        // remove the whole month from the set for this inverter.
        inverterData.removeMonthFromSet(baseInverter, yearNum, monthNum);

    }

    /**
     *
     * This method loads a month containing a date + yield field and process
     * those into the datastructure.
     *
     * @param xls SDTFile ( Excel ) to process
     * @param init [true/false] depending if the cache is invalid
     * @param year the year to process
     */
    private void monthAdd(File xls, boolean init, Integer year) {
        inverterData.setUpdated(true);

        String filename = xls.getName().substring(0, xls.getName().length() - 4);
        String split[] = filename.split("-");

        String yearString = split[1].trim();
        String monthString = split[2].trim();

        int yearNum = Integer.parseInt(yearString);
        int monthNum = Integer.parseInt(monthString);

        // get max day
        Calendar maxDay = Calendar.getInstance(Constants.getLocalTimeZone());
        maxDay.set(Calendar.DAY_OF_MONTH, 1);
        maxDay.set(Calendar.MONTH, monthNum - 1);
        maxDay.set(Calendar.YEAR, yearNum);

        // ugly. but want to avoid Java 8 because of backwards compatibility.
        String maxDayNum = "" + maxDay.getActualMaximum(Calendar.DAY_OF_MONTH);


        try {
            Workbook workbook = Workbook.getWorkbook(xls);
            Sheet sheet = workbook.getSheet(0);

            int workRow = -1;
            int headerRow = -1;

            int firstCell = -1;
            int lastCell = -1;

            // try to find the header ( daynum 1 2 3 4 5 6 and so on )
            for (int rowNum = 0; rowNum < sheet.getRows(); rowNum++) {
                Cell[] cells = sheet.getRow(rowNum);

                //
                if (cells.length > 10) {
                    // this is most likely the first header with daynums
                    headerRow = rowNum;

                    // find the first cell where the data starts, skip the first ( 0 ).
                    for (int cellNum = 1; cellNum < cells.length; cellNum++) {
                        String value = cells[cellNum].getContents().trim();
                        if (value.equals("1")) {
                            firstCell = cellNum;

                        }
                        if (value.equals(maxDayNum)) {
                            lastCell = cellNum;
                        }
                    }

                    break;
                }

            }

            // file can contain multiple serial numbers, find the row to work with.
            for (int rowNum = 0; rowNum < sheet.getRows(); rowNum++) {
                Cell serialCell = sheet.getCell(0, rowNum);
                String serialnr = serialCell.getContents();

                if (serialnr.contains(growattInverter.getM_InverterSN())) {
                    workRow = rowNum;
                    break;
                }
            }

            if (workRow != -1 && headerRow != -1 && firstCell != -1) {


                Cell[] row = sheet.getRow(workRow);

                for (int cell = firstCell; cell <= lastCell; cell++) {
                    int dayNum = cell - firstCell + 1;
                    String dateString = dayNum + "-" + monthString + "-" + yearString;

                    String kWhEntry = row[cell].getContents();


                    if (!"".equals(kWhEntry)) {
                        String dateFormatInFile = "dd-MM-yyyy";
                        SimpleDateFormat format = new SimpleDateFormat(dateFormatInFile);
                        format.setTimeZone(Constants.getLocalTimeZone());

                        try {
                            Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                            cal.setTime(format.parse(dateString));

                            if (init) {
                                inverterData.addInitYearSet(cal);
                            } else {
                                System.out.println(dateString);
                                float kwh = Float.valueOf(kWhEntry.replace(",", "."));
                                int wh = (int) (kwh * 1000);
                                inverterData.addDayYieldForInverter(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), baseInverter, wh, year);

                            }


                        } catch (ParseException pe) {
                            log.error("ParseException on line: " + workRow + ". I cannot process \"" + dateString + "\" as a correct date. ");
                        }


                    }


                }


            } else {
                log.warn(xls.getName() + " is not processed, cannot find header, serialnumber or startpoint to read daydata.");
            }


        } catch (IOException IOe) {
            log.error("An error has occured reading file: " + xls.getName());
        } catch (BiffException be) {
            log.error("Error processing " + xls.getName() + ". Message: " + be.getMessage());
        }
    }

    /**
     *
     * @param init [true/false] depending if the cache is invalid
     * @param year the year to process
     */
    private void process_Month_Files(boolean init, Integer year) {
        FileEntries fe = this.processFiles(new RegularExpressionFilenameFilter("^.*[0-9][0-9][0-9][0-9]-([0-9])?[0-9].xls"), true);

        ExecutorService executor = Executors.newFixedThreadPool(readThreadCount);
        for (FileEntry f : fe.getFileList()) {
            File newFile = new File(f.getFileLocation());
            if (f.isToDelete()) {
                this.monthRemove(newFile);
            }
            if (f.isToLoad()) {
                this.monthAdd(newFile, init, year);
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
     * @param init [true/false] depending if the cache is invalid
     * @param year the year to process
     */
    @Override
    public void dataLoader(boolean init, Integer year) {
        this.process_Month_Files(init, year);

        super.dataLoader(init, year);
    }
}
