package nl.mk.jsunnyreports.ftp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.io.IOUtils;

public class QueueManager {
   
   private static final int retryCount = 10;
   private long totalSize = 0; 

   public QueueManager() {
   }

   private Queue fileQueue = new LinkedList();

   public synchronized void enqueue(FTPFile f) {
      if ( f.getRetryCount() <= retryCount ) {
         
         if (f.getRetryCount() == 0 ) {
            totalSize = totalSize + f.getFile().length();
         }
         
         fileQueue.add(f);
      }
   }

   public synchronized FTPFile dequeue() {
      FTPFile f = null;
      if ( fileQueue.peek() != null ) {
         f = (FTPFile)fileQueue.remove();
      }
      return f;
   }
   
   public synchronized boolean isEmpty() {
       if ( fileQueue.peek() != null ) {
          return false;
       }
       return true;
       
   }
   
   // this one is for debug purpose only.
   public void printQueue() {
      boolean running = true;
      FTPFile f;
      int amount = 0;
      while (running) {
         f = dequeue();

         if (f != null) {
           System.out.println( "from: " + f.getFile().getAbsolutePath() + " to: " + f.getRemoteDirectory() + "/" + f.getFile().getName() );
           amount++;

         } else {
            // queue is empty.
            running = false;
         }         
         
      }
      System.out.println( "found: " + amount + " files in the queue to be processed.");
   }


   public long getTotalSize() {
      return totalSize;
   }
}
