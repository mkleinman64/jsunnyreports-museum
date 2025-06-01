package nl.mk.jsunnyreports.dataobjects.cache;

import java.io.Serializable;

/**
 *
 * Filecache, this file keeps track of the files read in the dataobjects.
 *
 * Date         Version     Who     What
 * 26-11-2011   1.3.0.2     MKL     New!
 * 03-12-2011   1.3.1.1     MKL     Added inverterName to fix issue 248 and 246.
 * 02-10-2014   1.5.0.0     MKL     Instead of fullpath only filename is used. saves space, is faster and seeding can now be done on a normal computer
 *                                  as long as the inverterNames stay the same.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   1.3.1.1
 * @since     2.5.0
 *
 */
public class FileCache implements Serializable {

    @SuppressWarnings("compatibility:8433226119843072665")
    static final long serialVersionUID = 2282212012101182210L;

    private String inverterName;        // name of the inverter to log
    private String filename;            // name of the file
    private long lastModified;          // lastModified
    private boolean processed = false;  // 


    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setInverterName(String inverterName) {
        this.inverterName = inverterName;
    }

    public String getInverterName() {
        return inverterName;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isProcessed() {
        return processed;
    }
}
