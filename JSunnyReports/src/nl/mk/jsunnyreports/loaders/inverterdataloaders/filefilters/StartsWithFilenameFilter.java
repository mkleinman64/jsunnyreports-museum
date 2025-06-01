package nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters;

import java.io.File;
import java.io.FilenameFilter;

public class StartsWithFilenameFilter implements FilenameFilter {
    public StartsWithFilenameFilter(String patternName) {
        this.patternName = patternName;
    }
    private String patternName;

    public boolean accept(File directory, String name) {
        return name.startsWith(this.patternName);
    }


}
