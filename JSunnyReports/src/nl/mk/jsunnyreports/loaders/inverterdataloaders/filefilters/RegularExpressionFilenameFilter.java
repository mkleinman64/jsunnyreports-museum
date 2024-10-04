package nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters;

import java.io.File;

import java.io.FilenameFilter;

import java.util.regex.Pattern;

public class RegularExpressionFilenameFilter implements FilenameFilter {
    private Pattern pattern;

    public RegularExpressionFilenameFilter(String regex) {
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public boolean accept(File dir, String name) {
        // Strip path information, search for regex:
        return pattern.matcher(new File(name).getName()).matches();
    }

}
