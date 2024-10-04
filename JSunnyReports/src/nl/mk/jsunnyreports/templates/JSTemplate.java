package nl.mk.jsunnyreports.templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Enumeration;

import nl.mk.jsunnyreports.common.Language;

import org.apache.log4j.Logger;

public class JSTemplate {
    private static final Logger log = Logger.getLogger(JSTemplate.class);

    public JSTemplate(String templateName, Language language) {
        readTemplate(templateName);
        this.language = language;
    }

    private StringBuilder contents = new StringBuilder();

    private Language language;


    public void readTemplate(String templateName) {
        StringBuilder sb = new StringBuilder();
        try {
            String currentLine = "";

            BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/template/customjs/" + templateName));

            while ((currentLine = in.readLine()) != null) { // while loop begins here
                sb.append(currentLine);
                sb.append("\n");

            } // end while
            contents = sb;
        } catch (FileNotFoundException e) {
            log.fatal(e);
            System.exit(100);
        } catch (UnsupportedEncodingException e) {
            log.fatal(e);
            System.exit(100);
        } catch (IOException e) {
            log.fatal(e);
            System.exit(100);
        }

    }

    public StringBuilder getContents() {
        return contents;
    }

    public void replaceAll() {
        Enumeration e = language.getTexts().propertyNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = language.getText(key);
            replace(key, value);
        }
    }

    public void replace(String tagName, String valueText) {
        this.contents = new StringBuilder(contents.toString().replace("##" + tagName.toLowerCase() + "##", valueText));
    }

    public void writeFile(String filename, String location) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(location + "/" + filename));
            out.write(this.getContents().toString());
            out.close();
        } catch (IOException e) {
            log.error("Could not write " + filename + " please check your pathnames in your settings.conf file");

        }
    }
}
