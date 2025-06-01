package nl.mk.jsunnyreports.common;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Website language reader. Displays the right text corresponding to the set language in the configurator.
 * Uses name = cssValueAttribute style system
 * All language files can be found in /languages/
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @since 1.1.0.0beta1
 * @version 1.1.1.0
 *
 * Date         Version     Who     What
 * 04-10-2010   1.1.1.0     MKL     Removed "finally" warning.
 * 12-10-2010   1.1.1.0     MKL     Reformat source
 * 23-04-2010   1.2.0.0b5   MKL     Modified null handling when getting texts.
 * 15-12-2011   1.3.2.0     MKL     Modified, removed settings and added websiteLanguage as inputParam.
 *
 *
 */
public class Language {
    public Language( String websiteLanguage ) {
        texts = new Properties();
        try {
            texts.load(new FileInputStream(System.getProperty("user.dir") + "/languages/" + websiteLanguage + ".conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private Properties texts;

    /**
     * Returns a given text from the language file. if it does not exist it will return ""
     *
     * @param textName for the given language.
     * @return the textValue of the parameter.
     */
    public String getText(String textName) {
        String textValue = "";
        try {
            textValue = texts.getProperty(textName);

        } catch (Exception e) {
            textValue = e.getMessage();
        }
        
        if ( textValue == null ) {
          textValue = "WARNING!, could not find text :" + textName;
        }
        
        // for now disabled, loads of things that need to be unescaped in texts. leaving as is atm.
        //textValue = StringEscapeUtils.escapeHtml4( textValue );

        return textValue;
    }

    public Properties getTexts() {
        return texts;
    }
}
