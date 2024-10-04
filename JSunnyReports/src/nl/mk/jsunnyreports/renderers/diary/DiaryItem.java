package nl.mk.jsunnyreports.renderers.diary;

import org.apache.commons.lang3.StringEscapeUtils;

public class DiaryItem {
    
    public DiaryItem( String date, String title, String content ) {
        this.date = date;
        this.title = StringEscapeUtils.escapeHtml4( title );
        this.content = StringEscapeUtils.escapeHtml4( content ); 
    }

   private String date;
   private String title;
   private String content;

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
