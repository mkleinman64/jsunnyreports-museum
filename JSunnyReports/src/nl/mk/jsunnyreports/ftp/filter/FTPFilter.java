package nl.mk.jsunnyreports.ftp.filter;

import java.io.File;
import java.io.FileFilter;

public class FTPFilter implements FileFilter {
   public FTPFilter( long lastModified, boolean uploadAll ) {
     this.lastModified = lastModified;
     this.uploadAll = uploadAll;
   }

   private long lastModified;
   private boolean uploadAll = false;

   public boolean accept(File file) {

      if (file.isDirectory() == true || file.lastModified() >= lastModified || uploadAll == true ) {
         return true;
      } else {
         return false;
      }

   }


}
