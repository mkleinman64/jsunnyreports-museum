package nl.mk.jsunnyreports.dataobjects.processfiles;

import nl.mk.jsunnyreports.dataobjects.cache.FileCache;

public class FileEntry {

    public FileEntry( String fl, boolean del, boolean load, FileCache fc ) {
        this.fileLocation = fl;
        this.toDelete = del;
        this.toLoad = load;
        this.fc = fc;
    }

    private String fileLocation;
    private boolean toDelete;
    private boolean toLoad;
    private FileCache fc;

    public String getFileLocation() {
        return fileLocation;
    }

    public boolean isToDelete() {
        return toDelete;
    }

    public boolean isToLoad() {
        return toLoad;
    }

    public FileCache getFc() {
        return fc;
    }
}
