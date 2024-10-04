package nl.mk.jsunnyreports.renderers.tables.timerecords;

import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.renderers.tables.sorters.YearSortByKwh;

/**
 *
 * Date         Version     Who     What
 * 11-01-2011   1.1.2.0     MvdP    Add peakpower in yearsTopmodel
 * 24-01-2011   1.2.0.0     MKL     Added dfWatt to format Wattcolumn.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 15-12-2011   1.3.2.0     MKL     Do not toLoad language anymore, reference is given to this class, saves IO.
 *
 * @author Martijn van der Pauw and Martin Kleinman
 * @version 1.3.2.0
 * @since 1.1.0.0
 */
public class YearRecords {

   /** Creates a new instance of YearRecords */
   public YearRecords(Settings properties) {
      this.settings = properties;
   }

   private Settings settings;
   private float minrecord = 0;
   private static final int LIST_LENGTH = 15;
   private boolean sort = true;
   private List<Year> recordYears = new ArrayList<Year>();

   public boolean checkRecord(Year newYear) {
      if (recordYears.size() < LIST_LENGTH || newYear.getWh() > minrecord) {
         if (recordYears.size() < LIST_LENGTH)
            recordYears.add(newYear);
         else {
            recordYears.remove(LIST_LENGTH - 1);
            recordYears.add(LIST_LENGTH - 1, newYear);
         }
         if (sort)
            Collections.sort(recordYears, new YearSortByKwh());
         if (recordYears.size() >= LIST_LENGTH)
            minrecord = recordYears.get(LIST_LENGTH - 1).getWh();
         return true;
      }
      return false;
   }

   public List<Year> getRecordList() {
      return recordYears;
   }

}
