package nl.mk.jsunnyreports.dataobjects.processfiles;

import java.util.ArrayList;
import java.util.List;

public class FileEntries {
    
    private List<FileEntry> fileList = new ArrayList<FileEntry>();

    public List<FileEntry> getFileList() {
        return fileList;
    }
    
    public void addFileEntry( FileEntry f ) {
        if ( f != null ) {
            this.fileList.add(( f ));
        }
        
    }
    
}
