package nl.mk.jsunnyreports.dataobjects.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.dataobjects.inverterdata.TimeEntry;
import nl.mk.jsunnyreports.dataobjects.invertersumdata.MergedInverterData;

import org.apache.log4j.Logger;

/**
 *
 *
 * Loads total filecache, which files did we process and what is their size.
 * If the file ( filecache.jsun ) is not found a new empty filecache is returned.
 *
 * Date         Version     Who     What
 * 29-11-2011   1.3.0.2A    MKL     New
 * 29-11-2011   1.3.0.2A    MKL     Added "save once a day" as a further performance improvement.
 * 03-12-2011   1.3.1.1     MKL     Modified write cache for better performance.
 * 22-09-2012   1.3.3.0     MKL     Fixes issue 319, added check if data is present.
 * 21-05-2015   1.6.1.0     MKL     Added retainDays from settings.
 * 16-10-2018   2.5.0       MKL     Minor refactoring.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @since     1.3.0.1A
 * @version   2.5.0
 */
public class CacheHandler {

    private static final String fileCacheFilename = "filecache.jsun";
    private static final String inverterdataFilename = "inverterdata.jsun";

    private static final Logger log = Logger.getLogger(CacheHandler.class);
    private InverterData inverterData;
    private Files fileCache;
    private boolean isCacheStructureInvalid = false;

    private int cacheRetainDays; // determines the number of days for which all information has to be saved in the cache structure.

    public CacheHandler(int cacheRetain) {
        this.cacheRetainDays = cacheRetain;
    }

    public CacheHandler(boolean invalidate, int cacheRetain) {
        this.isCacheStructureInvalid = invalidate;
        this.cacheRetainDays = cacheRetain;
    }

    /**
     *
     */
    private void readFileCache() {
        try {
            FileInputStream fis = new FileInputStream(fileCacheFilename);
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            fileCache = (Files) ois.readObject();
            ois.close();

        } catch (FileNotFoundException fnfe) {
            log.info(fileCacheFilename + " could not be found.");
            isCacheStructureInvalid = true;
        } catch (IOException IOE) {
            log.info("Cannot read the file " + fileCacheFilename);
            isCacheStructureInvalid = true;
        } catch (ClassNotFoundException cnfe) {
            log.info("Cache structure changed.");
            isCacheStructureInvalid = true;
        } catch (Exception e) {
            isCacheStructureInvalid = true;
            log.error("Unknown error occured while reading the file " + fileCacheFilename);
        }
    }

    private void writeFileCache() {
        try {
            log.info("Writing: " + fileCacheFilename + " containing: " + fileCache.getFilesCached().size() + " files ");
            FileOutputStream fos = new FileOutputStream(fileCacheFilename);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(fileCache);
            oos.flush();
            oos.close();
        } catch (FileNotFoundException fnfe) {
            log.error(fileCacheFilename + " could not be found.");
        } catch (IOException IOE) {
            log.error("Cannot write the file " + fileCacheFilename);
        }
    }

    /**
     *
     * Loads inverterDataCache, this is the actual inverterdata which holds all the data.
     * If the file ( inverterdata.jsun ) is not found a new empty data object is returned.
     *
     */
    private void readInverterDataCache() {
        try {
            FileInputStream fis = new FileInputStream(inverterdataFilename);
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            inverterData = (InverterData) ois.readObject();
            ois.close();


        } catch (FileNotFoundException fnfe) {
            log.info(inverterdataFilename + " could not be found.");
            isCacheStructureInvalid = true;
        } catch (IOException IOE) {
            log.info("Cannot read the file " + inverterdataFilename);
            isCacheStructureInvalid = true;
        } catch (ClassNotFoundException cnfe) {
            log.info("Cache structure changed.");
            isCacheStructureInvalid = true;
        } catch (Exception e) {
            isCacheStructureInvalid = true;
            log.error("Unknown error occured while reading the file " + inverterdataFilename);
        }
    }

    /**
     *
     * Loads inverterDataCache, this is the actual inverterdata which holds all the data.
     * If the file ( inverterdata.jsun ) is not found a new empty data object is returned.
     *
     */
    private void writeInverterDataCache() {
        try {
            log.info("Writing: " + inverterdataFilename);
            FileOutputStream fos = new FileOutputStream(inverterdataFilename);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(inverterData);
            oos.flush();
            oos.close();

        } catch (FileNotFoundException fnfe) {
            log.error(inverterdataFilename + " could not be found.");
        } catch (IOException IOE) {
            log.error("Cannot write the file " + inverterdataFilename);
        }
    }
    
    /**
     *
     */
    public void writeCache() {
        TimeZone localTimeZone = Calendar.getInstance().getTimeZone();
        Calendar cal = Calendar.getInstance(localTimeZone);

        if (inverterData.isUpdated()) {
            purgeCache();
            fileCache.setLastSave(cal);
            this.writeFileCache();
            this.writeInverterDataCache();

        } else {
            log.info("Cache didn't change, nothing to write. ");
        }
    }
    



    /**
     *
     */
    public void readCacheFiles() {
        this.readFileCache();
        this.readInverterDataCache();

        if (isCacheStructureInvalid) {
            log.info("Creating new internal cache structure.");
            createNewCacheStructure();
        }

    }

    /**
     * Should be removed! See Mantis issue 184
     */
    public void postProcessFileCacheEntries() {
        for (FileCache fc : fileCache.getFilesCached()) {
            if (!fc.isProcessed()) {
                fc.setProcessed(true);
                
            }
        }
    }
    
    /**
     *
     */
    public void createNewCacheStructure() {
        inverterData = new InverterData();
        fileCache = new Files();

    }    

    /**
     * This method purges the cache from unwanted TimeEntries. ( Days older than retainAge ).
     * This optimizes saving and loading of the cache and has a dramatic effect on memory consumption!
     */
    private void purgeCache() {

        if (cacheRetainDays > 0) {
            // to substract correctly times -1 to get a negative number.
            int retainAgeDays = cacheRetainDays * -1;
            Calendar retainDate = Calendar.getInstance(Constants.getLocalTimeZone());
            // substract X days.
            retainDate.add(Calendar.DATE, retainAgeDays);
            for (Year y : inverterData.getYears()) {
                for (Month m : y.getMonths()) {
                    for (Day d : m.getDays()) {
                        if (d.getCalendarDate().before(retainDate)) {
                            d.setMergedData(new MergedInverterData());

                            for (ComplexInverter ci : d.getInverters()) {
                                if (ci.hasDetailData()) {
                                    inverterData.setUpdated(true);
                                    ci.setHasHadTimeEntries(true);
                                    ci.setTimeEntries(new ArrayList<TimeEntry>());
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /* basic getters and setters */
    public InverterData getInverterData() {
        return inverterData;
    }

    public Files getFileCache() {
        return fileCache;
    }

    public boolean isInvalid() {
        return isCacheStructureInvalid;
    }

}
