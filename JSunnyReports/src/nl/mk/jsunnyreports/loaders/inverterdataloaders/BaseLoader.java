package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters.StartsWithFilenameFilter;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntries;
import nl.mk.jsunnyreports.dataobjects.processfiles.FileEntry;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

/**
 * Loads and processes Manual.xls exclusively for one baseInverter.
 *
 * Date         Version     Who     What
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to this class.
 * 14-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 * 16-01-2013   1.3.4.0     MKL     A flawed manual.xls file could crash jsunnyreports. Fixed.
 * 17-03-2015   1.5.1.0     MKL     Massive update. Selecting the right files in a directory and comparing them to the cache is now done one method instead
 *                                  of one in every dataloader.
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 1.5.1.0
 * @since 1.3.2.0
 */
public abstract class BaseLoader implements LoaderInterface {

    public BaseLoader(BaseInverter bi, InverterData id, Files f, Settings s) {
        this.baseInverter = bi;
        this.inverterData = id;
        this.fileCache = f;
        this.settings = s;
        this.readThreadCount = settings.getParallelReadThreadCount();
    }

    private static final Logger log = Logger.getLogger(BaseLoader.class);

    protected final BaseInverter baseInverter;
    protected final InverterData inverterData;
    protected final Files fileCache;
    protected final Settings settings;
    protected final int readThreadCount;


    /**
     * Custom file loader, reads Excelsheet, parses the first sheet and processes the data
     * Data in the Excel is located in two Columns (A, date) (B, yield in kWh).
     *
     * @param customFile File to read in ( always an excel (.XLS ) file named "manual.xls" )
     * @throws DataLoadException Exception when data cannot be read further.
     */
    private void loadManual(File customFile, boolean init, Integer year ) {
        int lineNumber = 0;
        try {
            Workbook workbook = Workbook.getWorkbook(customFile);
            Sheet sheet = workbook.getSheet(0);
            for (int iterator = 1; iterator < sheet.getRows(); iterator++) {
                Cell kwhEntry = sheet.getCell(1, iterator);
                lineNumber = iterator;

                if (!"".equals(kwhEntry.getContents())) {

                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTimeZone(Constants.getLocalTimeZone());

                        Cell timeEntry = sheet.getCell(0, iterator);
                        DateCell dateCell = (DateCell)timeEntry;
                        cal.setTime(dateCell.getDate());
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );                            
                        } else {
                            float kWh = Float.parseFloat(kwhEntry.getContents().replace(",", "."));
                            int wh = (int)(kWh * 1000);
                            inverterData.addDayYieldForInverter( cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), baseInverter, wh, year);
                        }

                    } catch (Exception e) {
                        log.error("Exception found: could not proces line " + iterator + " from your manual.xls file, error. If you see this check your manual.xls file integrity.");
                        for (StackTraceElement ste : e.getStackTrace()) {
                            log.error("Class: " + ste.getClassName() + " Method: " + ste.getMethodName() + ". Line: " + ste.getLineNumber());
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + customFile.getName());
        } catch (BiffException b) {
            log.error("Fatal exception found: ( BiffException), please contact jSunnyreports team!");
        } catch (Exception e) {
            log.error("Fatal exception found: Unknown Error! ");
            e.printStackTrace();
        }
    }

    /**
     * Custom file loader, reads Excelsheet, parses the first sheet and processes the data
     * Data in the Excel is located in two Columns (A, date) (B, yield in kWh).
     *
     * @param customFile File to read in ( always an excel (.XLS ) file named "manual.xls" )
     * @throws DataLoadException Exception when data cannot be read further.
     */
    private void removeManual(File customFile) {
        int lineNumber = 0;
        try {
            Workbook workbook = Workbook.getWorkbook(customFile);
            Sheet sheet = workbook.getSheet(0);

            for (int iterator = 1; iterator < sheet.getRows(); iterator++) {
                Cell kwhEntry = sheet.getCell(1, iterator);
                lineNumber = iterator;
                if (!"".equals(kwhEntry.getContents())) {
                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    cal.setTimeZone(Constants.getLocalTimeZone());

                    Cell timeEntry = sheet.getCell(0, iterator);
                    DateCell dateCell = (DateCell)timeEntry;
                    cal.setTime(dateCell.getDate());

                    inverterData.removeDayFromSet(baseInverter, cal);
                }
            }
        } catch (IOException e) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + customFile.getName());
        } catch (BiffException b) {
            log.error("Fatal exception found: ( BiffException), please contact jSunnyreports team!");
        }
    }


    /**
     * 
     */
    private void processManual( boolean init, Integer year ) {
        FileEntries fe = this.processFiles(new StartsWithFilenameFilter("manual.xls"), false );
        
        for ( FileEntry f: fe.getFileList() ) {
            File newFile = new File( f.getFileLocation() );
            if ( f.isToDelete() ) {
                this.removeManual( newFile );
            }
            if ( f.isToLoad() ) {
                this.loadManual(newFile, init, year );
            }
        }
    }
    
    
    /**
     * Processes files in a directory, checks if files exist that match FilenameFilter ff.
     * Matches all the found files with the cache structure. If files are found a second check
     * is performed to see if the file has changed. This is done using the LastModified item from the file.
     *
     * A positive match is added to the FileEntries fe object.
     * Files that are found in the cache are marked for toDelete and toLoad ( effective an update )
     * Files that are NOT found in the cache are marked for toLoad.
     *
     * This object is returned to the caller ( a specific dataloader ) which then can toLoad the files itself.
     *
     * @param ff FilenameFilter to which files must conform
     * @param showWarning Show warning when no files are found
     * @return FileEntries object containing all the files that need to be loaded.
     */
    public FileEntries processFiles( FilenameFilter ff, boolean showWarning ) {
        String[] children = null;
        File dir = new File(baseInverter.getM_InputDirectory());
        children = dir.list( ff );
        FileEntries fe = new FileEntries();
        
        if (children == null ) {
            if ( showWarning ) {
                log.warn("No valid files found in directory " + baseInverter.getM_InputDirectory() + " to process for " + baseInverter.getM_InverterName() );
            }
        } else {
            String fullPath;
            File newFile = null;
            
            if ( !baseInverter.isO_IgnoreLoad()) {
                for (int i = 0; i < children.length; i++) {
                    fullPath = baseInverter.getM_InputDirectory() + "/" + children[i];
                    newFile = new File(fullPath);
                    if (fileCache.fileNameMatch(baseInverter.getM_InverterName(), children[i])) {
                        if (!fileCache.exactMatch(baseInverter.getM_InverterName(), children[i], newFile.lastModified()) ) {
                            FileCache fc = fileCache.addUpdate(baseInverter.getM_InverterName(), newFile.getName(), newFile.lastModified());
                            
                            log.info("Updating: " + fullPath );                        
                            FileEntry f = new FileEntry( fullPath, true, true, fc );
                            fe.addFileEntry(f);
                        } else {
                            // it is an exact match.
                            FileCache fc = fileCache.getExactMatch(baseInverter.getM_InverterName(), children[i], newFile.lastModified());
                            
                            if ( fc != null && !fc.isProcessed() ) {
                                //log.info("Adding for renewed processing: " + fullPath );                        
                                FileEntry f = new FileEntry( fullPath, false, true, fc );
                                fe.addFileEntry(f);
                            }
                        }
                    } else {
                        FileCache fc = fileCache.addUpdate(baseInverter.getM_InverterName(), newFile.getName(), newFile.lastModified());
                        log.info("Loading: " + fullPath );                    
                        FileEntry f = new FileEntry( fullPath, false, true, fc );
                        fe.addFileEntry(f);
                    }
                }
            } 
        }
        return fe;
    }


    /**
     *
     */
    public void dataLoader( boolean init, Integer year ) {
       processManual( init, year );
    }
}
