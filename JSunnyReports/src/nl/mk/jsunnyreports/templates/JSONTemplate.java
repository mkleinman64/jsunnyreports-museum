package nl.mk.jsunnyreports.templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Enumeration;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.loaders.DataLoader;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartUtilities;

public class JSONTemplate {

    private static final Logger log = Logger.getLogger(JSONTemplate.class);

    public JSONTemplate() {
    }

    private StringBuilder contents = new StringBuilder();

    public StringBuilder getContents() {
        return contents;
    }

    // this is soooooo wrong.    
    public void removeLastChar() {
       String b;
       b = contents.substring( 0, contents.length() -1  );
       
       contents = new StringBuilder();
       contents.append( b );
       
    }
    
    public void append( JSONTemplate json) {
        contents.append( json.getContents());
    }

    public void append(String s) {
        contents.append(s);
    }

    public void append(int i) {
        contents.append(i);
    }

    public void append(float f) {
        contents.append(f);
    }

    public void writeFile(String filename, String location) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(location + "/" + filename));
            out.write(this.getContents().toString());
            out.close();
        } catch (IOException e) {
            log.error("Could not write " + filename + " please check your pathnames in your settings.conf file");
            log.error("Tip: Does the outputpath exist?");
            log.error("Tip: Did you use the forward slash / in your paths?");
            
        }
    }

}
