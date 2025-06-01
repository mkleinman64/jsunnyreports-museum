package nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters;

import java.io.File;
import java.io.FilenameFilter;

public class EndsWithFilenameFilter implements FilenameFilter {
    public EndsWithFilenameFilter(String patternName) {
        this.patternName = patternName;
    }
    private String patternName;

    public boolean accept(File directory, String name) {
        return name.endsWith(this.patternName);
    }
}
