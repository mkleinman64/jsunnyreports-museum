package nl.mk.jsunnyreports.dataobjects.costdata;

import java.io.File;
import java.io.IOException;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;



import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;



import org.apache.log4j.Logger;


/**
 * CostEntries.java
 *
 * @author  Martin Kleinman
 * @version 2.5.0
 * @since   0.0.0.1
 *
 */
public class CostEntries implements Serializable {
    private static final Logger log = Logger.getLogger(CostEntries.class);

    @SuppressWarnings("compatibility:8608493252676098953")
    static final long serialVersionUID = -8445719442913693241L;
    List<CostEntry> costEntries = new ArrayList<CostEntry>();

    File costs;
    private long lastModified;

    public CostEntries() {
        try {
            costs = new File(System.getProperty("user.dir") + "/" + "conf/costs.xls");
            lastModified = costs.lastModified();

            Workbook workbook = Workbook.getWorkbook(costs);
            Sheet sheet = workbook.getSheet(0);

            // our life/work starts at row 4
            for (int iterator = 3; iterator < sheet.getRows(); iterator++) {

                try {
                    Cell fromCell = sheet.getCell(0, iterator);
                    Cell tillCell = sheet.getCell(1, iterator);
                    Cell costCell = sheet.getCell(2, iterator);
                    Cell inceCell = sheet.getCell(3, iterator);

                    if (fromCell.getContents().length() > 0 && tillCell.getContents().length() > 0 && costCell.getContents().length() > 0 && inceCell.getContents().length() > 0) {

                        DateCell fromDateCell = (DateCell)fromCell;
                        DateCell tillDateCell = (DateCell)tillCell;

                        // setting to first second of the period 
                        Calendar fromCal = Calendar.getInstance();
                        fromCal.setTime(fromDateCell.getDate());
                        fromCal.set(Calendar.HOUR_OF_DAY, 0);
                        fromCal.set(Calendar.MINUTE, 0 );
                        fromCal.set(Calendar.SECOND, 0);
                        fromCal.set(Calendar.MILLISECOND, 0);
                        
                        
                        // setting to last second in the period.
                        Calendar tillCal = Calendar.getInstance();
                        tillCal.setTime(tillDateCell.getDate());
                        tillCal.set(Calendar.HOUR_OF_DAY, 23);
                        tillCal.set(Calendar.MINUTE, 59 );
                        tillCal.set(Calendar.SECOND, 59 );
                        tillCal.set(Calendar.MILLISECOND, 99 );


                        // first get as floats.
                        float cost = Float.parseFloat(costCell.getContents().replace(",", "."));
                        float ince = Float.parseFloat(inceCell.getContents().replace(",", "."));

                        // remove the possible . and we keep the whole number. up to 4 digits behind the . are removed
                        // e.g. 0,2319 ==> 2319
                        cost = cost * 10000;
                        ince = ince * 10000;

                        int costs = (int)cost;
                        int incentive = (int)ince;

                        //    System.out.println( "COSTENTRIES: costs: " + costs + " " + incentive );

                        costEntries.add(new CostEntry(fromCal, tillCal, costs, incentive));
                    }

                } catch (Exception e) {
                    log.error( "----------------------------------------------------------------------");
                    log.error( "Error parsing costs.xls, something went wrong trying to read line " + iterator + ". This line is skipped!" );
                    log.error( "----------------------------------------------------------------------");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException b) {
            b.printStackTrace();
        }
    }


    /**
     * This method returns the energyCosts for a specific date
     * if nothing is found OL is returned.
     *
     * This is done by comparing time in millies.
     *
     * @param cal
     * @return energyCosts as a whole int ( value from XLS * 10000 ).
     */
    public int getEnergyCosts(Calendar cal) {
        long totest = cal.getTimeInMillis();

        for (CostEntry c : costEntries) {

            if (totest >= c.getFromMillis() && totest <= c.getTillMillis()) {
                return c.getEnergyCost();
            }
        }
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = format1.format(cal.getTime());

        log.warn( "Warning! I cannot find a valid costEntry for date: " + formattedDate + ". Are you missing an entry in costs.xls?" );
        return 0;
    }

    /**
     * This method returns the energyCosts for a specific date
     * if nothing is found OL is returned.
     *
     * This is done by comparing time in millies.
     *
     * @param year 
     * @param month
     * @param day
     * @return EnergyCost as a whole int ( value from XLS * 10000 ).
     */
    public int getEnergyCosts(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0 );
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return getEnergyCosts( cal );
    }

    /**
     * This method returns the energyIncentive for a specific date
     * if nothing is found 0f is returned.
     *
     * This is done by comparing time in millies.
     *
     * @param cal Calendar containing the date 
     * @return energyIncentive as a whole int ( value from XLS * 10000 ).
     */
    public int getEnergyIncentive(Calendar cal) {
        long totest = cal.getTimeInMillis();

        for (CostEntry c : costEntries) {
            if (totest >= c.getFromMillis() && totest <= c.getTillMillis()) {
                return c.getIncentive();
            }
        }
        return 0;
    }

    /**
     * This method returns the energyIncentive for a specific date
     * if nothing is found 0f is returned.
     *
     * This is done by comparing time in millies.
     *
     * @param year
     * @param month
     * @param day
     * @return energyIncentive as a whole int ( value from XLS * 10000 ).
     */
    public int getEnergyIncentive(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0 );
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return getEnergyIncentive( cal );

    }
    
    /**
     * Intended for debug purposes only.
     */
    public void printCosts() {
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        for (CostEntry c : costEntries) {
            String from = format1.format(c.getFrom().getTime());          
            String till = format1.format(c.getTill().getTime());          
            System.out.println( "------------------------" );
            System.out.println( "From:  " + from + "( " + c.getFromMillis()  + " )"  );
            System.out.println( "Till:  " + till + "( " + c.getTillMillis()  + " )"  );
            System.out.println( "value: " + c.getEnergyCost() );
            System.out.println( "------------------------" );
        }        
    }

    /**
     *
     * @return
     */
    public long getLastModified() {
        return lastModified;
    }
    
}
