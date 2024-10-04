package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import nl.mk.jsunnyreports.inverters.SunnyDataControlXLSInverter;

import org.apache.log4j.Logger;

public class SunnyDataControlXLSFileProcessor extends BaseProcessor implements Runnable {

   private static final Logger log = Logger.getLogger(SunnyDataControlXLSFileProcessor.class);

   public SunnyDataControlXLSFileProcessor(File theFile, InverterData inverterData, SunnyDataControlXLSInverter sdcXLSInverter, Settings settings, boolean init, Integer year, FileCache fc) {
      this.theFile = theFile;
      this.inverterData = inverterData;
      this.sdcXLSInverter = sdcXLSInverter;
      this.settings = settings;
      this.init = init;
      this.year = year;
      this.fc = fc;

   }

   private SunnyDataControlXLSInverter sdcXLSInverter;

   /**
    * Get the right sheet.
    *
    *
    * @param serialnumber of the inverter we need to process
    * @return the right sheet from the Excelfile, or null when nothing was found.
    */
   private Sheet getWorksheet(Workbook workbook, String serialnumber) {
      Sheet sheet = null;

      int numSheets = workbook.getNumberOfSheets();

      for (int i = 0; i < numSheets - 1; i++) {
         Sheet testSheet = workbook.getSheet(i);

         Cell testEntry = testSheet.getCell(1, 6);
         String testString = testEntry.getContents();
         if (serialnumber.equals(testString)) {
            sheet = testSheet;
            break;
         }
      }
      return sheet;

   }

   public void run() {
      inverterData.setUpdated(true);
      int lineNumber = 0;
      try {

         Workbook workbook = Workbook.getWorkbook(theFile);
         Sheet sheet = getWorksheet(workbook, sdcXLSInverter.getM_SerialNumber() + "");

         if (sheet != null) {
            int wattColumn = getWattColumn(sheet);

            // our life/work starts at row 8
            for (int iterator = 7; iterator < sheet.getRows(); iterator++) {
               lineNumber = iterator;
               if (sheet.getRow(iterator).length >= wattColumn) {

                  Cell timeEntry = sheet.getCell(0, iterator);
                  Cell wattEntry = sheet.getCell(wattColumn, iterator);

                  DateCell dateCell = (DateCell) timeEntry;
                  Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                  cal.setTime(dateCell.getDate());
                  int tzOffsetMin = -(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000 * 60);
                  cal.add(Calendar.MINUTE, tzOffsetMin);

                  if (init) {
                     inverterData.addInitYearSet(cal);
                  } else {
                     if (!"".equals(wattEntry.getContents())) {
                        float watt = Float.valueOf(wattEntry.getContents().replace(",", ".")).floatValue();

                        // add the yield to the dataset.
                        boolean added = inverterData.addWattEntryForInverter(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), sdcXLSInverter, watt, year);
                     }
                  }
               }
            }
         }


      } catch (IOException IOe) {
         log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
      } catch (BiffException be) {
         log.error("Error processing " + theFile.getName() + ". Message: " + be.getMessage());
      }
   }

   /**
    *
    *
    * @param   sheet Worksheet ( Excel )
    * @return  the # of the column containing the Pac data.
    */
   private int getWattColumn(Sheet sheet) {
      int wattColumn = 0;

      // first we have to check in what column the Watt information is residing.
      for (int iterator = 1; iterator < sheet.getColumns(); iterator++) {
         Cell testEntry = sheet.getCell(iterator, 1);
         String testString = testEntry.getContents();
         if ("Pac".equals(testString)) {
            wattColumn = iterator;
            break;
         }
      }
      return wattColumn;
   }

}
