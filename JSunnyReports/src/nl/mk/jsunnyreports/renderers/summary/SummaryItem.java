package nl.mk.jsunnyreports.renderers.summary;

import org.apache.commons.lang3.StringEscapeUtils;

public class SummaryItem {
    
    public SummaryItem( String key, String value ) {
        this.key = StringEscapeUtils.escapeHtml4(key);
        this.value = StringEscapeUtils.escapeHtml4(value);
    }
    
    
    private String key;
    private String value;


    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
