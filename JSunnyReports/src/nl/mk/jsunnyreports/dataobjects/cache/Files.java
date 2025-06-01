package nl.mk.jsunnyreports.dataobjects.cache;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;


/**
 * Part of the CacheHandler of jSunnyreports
 *
 * Date         Version     Who     What
 * 26-11-2011   1.3.0.2     MKL     New!
 * 03-12-2011   1.3.1.1     MKL     Did some refactoring and fixed issue 247
 * 02-10-2014   1.5.0.0     MKL     Modified access to the filecache ( now on filename instead of fullpath ).
 * 17-09-2018   2.5.0       MKL     Minor fixes. deduplicated code a bit and changed some method names.
 *
 *
 * @version 1.3.1.1
 * @since   2.5.0
 *
 */
public class Files implements Serializable {

    static final long serialVersionUID = 2282212012101182210L;

    private List<FileCache> filesCached = new ArrayList<FileCache>();
    private Calendar lastSave = null;

    public void setFilesCached(List<FileCache> filesCached) {
        this.filesCached = filesCached;
    }

    public List<FileCache> getFilesCached() {
        return filesCached;
    }

    /**
     * checks if it is an exactmatch.
     *
     * @param fileName
     * @param lastModified
     * @return exactMatch file is in cache.
     */
    public boolean exactMatch(String inverterName, String fileName, long lastModified) {
        if ( getExactMatch( inverterName, fileName, lastModified ) != null ) {
            return true;
        } else { 
            return false;
        }
        
    }
    
    /**
     * gets an exactmatch of a file and returns the cache entry
     *
     * @param fileName
     * @param lastModified
     * @return exactMatch file is in cache.
     */
    public FileCache getExactMatch(String inverterName, String fileName, long lastModified) {
        for (FileCache f : filesCached) {
            if (f.getInverterName().equals(inverterName) && f.getFilename().equals(fileName) && f.getLastModified() == lastModified) {
                return f;
            }
        }
        return null;
    }    

    /**
     *
     * @param inverterName
     * @param fileName
     * @return
     */
    public boolean fileNameMatch(String inverterName, String fileName) {
        if ( getFileCache( inverterName, fileName ) != null ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param inverterName
     * @param fileName
     * @return
     */
    public FileCache getFileCache(String inverterName, String fileName) {
        for (FileCache f : filesCached) {
            if (f.getInverterName().equals(inverterName) && f.getFilename().equals(fileName)) {
                return f;
            }
        }
        return null;
    }

    /**
     *
     * @param fc
     * @param inverterName
     * @param fileName
     * @param lastModified
     */
    private void updateFile(FileCache fc, String inverterName, String fileName, long lastModified) {
        fc.setFilename(fileName);
        fc.setInverterName(inverterName);
        fc.setLastModified(lastModified);
    }

    /**
     *
     * @param inverterName
     * @param fileName
     * @param lastModified
     * @return
     */
    private FileCache addFile(String inverterName, String fileName, long lastModified) {
        FileCache f = new FileCache();
        f.setFilename(fileName);
        f.setLastModified(lastModified);
        f.setInverterName(inverterName);
        this.getFilesCached().add(f);
        
        return f;
    }

    /**
     *
     * @param inverterName
     * @param fileName
     * @param lastModified
     * @return
     */
    public FileCache addUpdate(String inverterName, String fileName, long lastModified) {
        FileCache fc = getFileCache(inverterName, fileName);
        if (fc == null) {
            fc = addFile(inverterName, fileName, lastModified);
        } else {
            updateFile(fc, inverterName, fileName, lastModified);
        }
        
        return fc;
    }

    /**
     *
     * @param lastSave
     */
    public void setLastSave(Calendar lastSave) {
        this.lastSave = lastSave;
    }

    /**
     *
     * @return
     */
    public Calendar getLastSave() {
        return lastSave;
    }
}
