/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoorganizer;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.xmp.XmpDirectory;
import java.io.File;
import java.text.DateFormatSymbols;
import java.io.File;
import java.io.IOException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.time.Month;
import java.util.logging.Level;
import java.nio.file.*;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.xmp.XmpDirectory;
import java.io.File;
import java.text.DateFormatSymbols;
import java.io.File;
import java.io.IOException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import java.awt.Cursor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.time.Month;
import java.util.logging.Level;
import java.nio.file.*;
import javax.swing.filechooser.FileSystemView;


/**
 *
 * @author Dennis
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    
    String sourceFolderPath ;
    String destFolderPath;
    // Path strings for duplicate and misc sub folders
    String dupsPath;
    String skippedPath;
    String noStampPath;
    String logPath;
    String videoPath;
    
    PrintWriter logWriter;
    
    // File Extensions to process
    String[] targetExt = new String[] { "img", "jpg", "jpeg", "mov", "mp4", "avi" };
    
    // Counters
    int numFolders = 0;
    Long numTotal = 0L;
    Long numProcessed = 0L;
    Long numDuplicates = 0L;
    Long numSkipped = 0L;
    Long numVideos = 0L;
    Long numNoStamp = 0L;
    
     byte[] buffer;
     boolean sameDrive;
     boolean preview = false;
    
    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private boolean waitCursorIsShowing;
  
    JFileChooser chooser;
    String choosertitle;
    Boolean sourceFolderSelected = false, destFolderSelected = false;
   
    public MainFrame() {
        initComponents();
      
        btnSelectSourceFolder.addActionListener(e -> selectSourceFolder());
        btnSelectDestFolder.addActionListener(e -> selectDestFolder());
        btnPreview.addActionListener(e -> previewSourceFolder());
        btnProcess.addActionListener(e -> processItems());
        //showPreviewAlert();
    }
    
    private void selectSourceFolder() {
    
        JFileChooser chooser = new JFileChooser("K:\\"); 
        chooser.setDialogTitle("Select Photo Source Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //
        // disable the "All files" option.
        chooser.setAcceptAllFileFilterUsed(false);
        //    
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
          System.out.println("getCurrentDirectory(): " 
             +  chooser.getCurrentDirectory());
          System.out.println("getSelectedFile() : " 
             +  chooser.getSelectedFile());

            lblSourceFolder.setText(chooser.getSelectedFile().getPath());
            sourceFolderPath = (chooser.getSelectedFile().getPath());
            sourceFolderSelected = true; 
            btnPreview.setEnabled(true);
        }
       
        btnProcess.setEnabled(sourceFolderSelected && destFolderSelected);
    }
    
    private void selectDestFolder() {
    
    JFileChooser chooser = new JFileChooser("K:\\Sorted Photos"); 
    chooser.setDialogTitle("Select Photo Destination Folder");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    //
    // disable the "All files" option.
    chooser.setAcceptAllFileFilterUsed(false);
    //    
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { 
      System.out.println("getCurrentDirectory(): " 
         +  chooser.getCurrentDirectory());
      System.out.println("getSelectedFile() : " 
         +  chooser.getSelectedFile());
        
          lblDestFolder.setText(chooser.getSelectedFile().getPath());
          destFolderPath = (chooser.getSelectedFile().getPath());
          destFolderSelected = true;        
        }
        btnProcess.setEnabled(sourceFolderSelected && destFolderSelected);
     }
    
    private void previewSourceFolder (){
        preview = true;
        processItems();
        preview = false;
}
    private void processItems() {
        this.setCursor(waitCursor);
       // Initial and validate folders
        initialize();
        boolean subs = chkSubFolders.isSelected();
        // Start at source folder root
        File sourceFolder = new File(sourceFolderPath);
        processFolder(sourceFolder);
        // Finish up 
        if (preview) {
            showTotalsAlert ("PREVIEW") ;
        } else {    
            showTotalsAlert ("FINAL") ;
        }
        this.setCursor(defaultCursor);
        if (preview) return;
        // Log Totals
        logIt (" Total items: " + numTotal);
        logIt (" Total processed: " + numProcessed);
        logIt (" Total duplicates: " + numDuplicates);
        logIt (" Total skipped: " + numSkipped);
        logIt (" Total videos: " + numVideos);
        logIt (" Total without TimeStamp: " + numNoStamp);     
        logWriter.close();
        sourceFolder = null;
        
   }
      // assures that source exists and output foldeer exist with subfolders
    private void initialize() {
       
        numFolders = 0;
        numTotal = 0L;
        numProcessed = 0L;
        numDuplicates = 0L;
        numSkipped = 0L;
        numVideos = 0L;
        numNoStamp = 0L;
        // Fix screw up with blanks
        sourceFolderPath = sourceFolderPath.trim();
        lblProgress.setText("");
        lblProgress.repaint();
        // Make sure the source folder exist
        File sourceFolder = new File(sourceFolderPath);
        if (!(sourceFolder.exists()  && sourceFolder.isDirectory()) ){
            System.out.println ("Cannot find source folder");
            System.exit(4);
        }
        if (preview) return;
        // Validate destination folder
        destFolderPath = destFolderPath.trim();
        assureFolderExists(destFolderPath);
        dupsPath = destFolderPath + "\\Duplicates";
        skippedPath = destFolderPath + "\\Skipped";
        noStampPath = destFolderPath + "\\No Time Stamp";
        logPath = destFolderPath + "\\Log";
        videoPath = destFolderPath + "\\Videos";
        // Validate destination subfolders
        assureFolderExists(dupsPath);
        assureFolderExists(skippedPath);
        assureFolderExists( logPath);
        assureFolderExists(videoPath);
        assureFolderExists(noStampPath);
        String sourceDrive = sourceFolderPath.substring(0,1);
        String destDrive = destFolderPath.substring(0,1);
        sameDrive = sourceDrive.equalsIgnoreCase(destDrive);
        try {                      
            logWriter = new PrintWriter(new FileWriter(appendToPath(logPath, "log.txt"),true));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(2);
        }
}
    
    // recurvise routine to process all the items in a folder
    private void processFolder(File  folder) {
        numFolders += 1;
        int numOfPhotosInFolder = 0;
        // If folder is empty, exit 
         File[] fileList = folder.listFiles();
         if (fileList == null || fileList.length == 0) return;
        int j = fileList.length;
        //process items in folder
        for (int i = 0; i< fileList.length; i++) {
            File thisItem = fileList[i];
            if (thisItem.isDirectory()) {
                if (chkSubFolders.isSelected())  processFolder(thisItem);
            } else {
                numTotal += 1; 
                // not working
                if ( numTotal % 250 == 0)  {
                    lblProgress.setText(numTotal + " files examined");
                    lblProgress.repaint();
                    this.invalidate();
this.validate();
this.repaint();
                }
                if (isTargetFile(thisItem)) {
                    numOfPhotosInFolder += 1;
                    processTargetItem(thisItem);
                }
                else {
                    String name = thisItem.getName();
                    if (name.equalsIgnoreCase("thumbs.db"))
                        thisItem.delete();
                    else
                     moveToSkipped(thisItem);
                }                                       
            }
        }
        if (numOfPhotosInFolder != 0) {
            logIt( numOfPhotosInFolder + " targets in " + folder.getPath() );
        }
        // Refresh item list in this folder
        //  if no more items, remove this folder from tree.
        fileList = folder.listFiles();
        if (fileList == null || fileList.length == 0) {
            if (numFolders != 1) {
                    if (!folder.delete()) {
                    logIt("Failed to delete " + folder.getPath());
                    }
                };
        }
    }
    
    private void showTotalsAlert (String title) {
    String processCounts = buildCountString();   
    JOptionPane.showMessageDialog(this,
        processCounts,
        title,
        JOptionPane.INFORMATION_MESSAGE);   
    }
   
    private String buildCountString() {
        String s;
        s = "Total items inspected: " + numTotal + "\n";
        s += "Total items processed: " + numProcessed + "\n";
        s += "Total folders processed: " + numFolders + "\n";
        s += "Total duplicate items: " + numDuplicates + "\n";
        s += "Total items skipped: " + numSkipped + "\n";
        s += "Total videos processed: " + numVideos + "\n";
        s += "Total with no TimeStamp: " + numNoStamp + "\n\n";
        s += "in folder: " + sourceFolderPath;
        return s;
    }
    
    private void processTargetItem(File thisFile) {
        try {
            String dateTaken = getExifDate(thisFile);
            if (isVideo(thisFile)) {
                moveToVideos(thisFile);
                return;
            }
            if (dateTaken.isEmpty())  {
                moveToNoStamp (thisFile);
                return;
            }
            String yearString = dateTaken.substring(0, 4);
            String monthString = dateTaken.substring(5,7);
            String dayOfMonthString = dateTaken.substring(8,10);
            //Validate timestamp
            int yearNumber = Integer.parseInt(yearString);
            if (yearNumber < 1991 || yearNumber > 2021) {
                logIt (thisFile.getPath() + " invalid timestamp: " + dateTaken);
                moveToSkipped(thisFile);
            }       
            int monthNumber = Integer.parseInt(monthString);
             if (monthNumber < 0 || monthNumber > 12) {
                logIt (thisFile.getPath() + " invalid timestamp: " + dateTaken);
                moveToSkipped(thisFile);
            } 
            // validart or create year subfolder
            String currentYearPath = appendToPath(destFolderPath, yearString);
            assureFolderExists(currentYearPath);
            // make sure month folder exists withing year folder
            String monthFolderName = monthString + "-" + Month.of(monthNumber).name();
            String currentMonthPath = appendToPath(currentYearPath, monthFolderName);
            assureFolderExists(currentMonthPath);
            //System.out.println(thisFile.getName() + " goes in folder" + yearString + " and " + monthFolderName);
            String newFileName = dayOfMonthString + "-" + thisFile.getName().trim();
            String newFilePath = appendToPath(currentMonthPath, newFileName);
            File possibleDupFile = new File (newFilePath);
            boolean g = possibleDupFile.exists();
              if (possibleDupFile.exists()) 
                {moveToDups(thisFile);}
              else  {       
                if (renameItem(newFilePath, thisFile)) {
                    numProcessed += 1;}
                else {
                    moveToSkipped(thisFile);  // Ignore any erros
                }
            }
        }   
        catch (Exception e) { 
            System.out.println(e.getMessage() + " on " + thisFile.getName() );
        }
    }
    
    private boolean renameItem(String path, File thisFile) {
        if(sameDrive) {        
            File destFile = new File(path);
            boolean moveResult = (thisFile.renameTo(destFile));
            return moveResult;
        }
        // differnt drives, move file
        
        return moveBinFile(thisFile, path);
    }
    // return true if this is a file to process
    private boolean isTargetFile (File file) {
        // Validates file object that we want to process this item
        // Must be a file
        if (!file.isFile()) 
            return false;
        if (file.isHidden()) 
            return false;
        if (file.length()== 0) return false;
        if (validFileExtension(file)) return true;
        return false;
    }
    // examines file extension, return true if file to process
    protected boolean validFileExtension(File f) {   
        String name = f.getName();
        String ext = name.substring(name.lastIndexOf('.') + 1);
        for (int i = 0; i< targetExt.length; i++ ) {
            if (ext.equalsIgnoreCase(targetExt[i])) return true;
        }
        return false;
    } 
    protected boolean isVideo (File f) {
        String[] videoExt = new String[] {"mov", "mp4", "avi"};
        String name = f.getName();
        String ext = name.substring(name.lastIndexOf('.') + 1);
        for (int i = 0; i< videoExt.length; i++ ) {
            if (ext.equalsIgnoreCase(videoExt[i])) return true;
        }
        return false;
    }
   // Validates if folder exists or creates it if not
    private void assureFolderExists(String path){
        File testFile = new File(path);
        if (testFile.exists() && testFile.isDirectory()) return;
        makeFolder(path);
    }
    //  Make folder whose path is input parameter
    private void makeFolder (String path) {
        //Creating a File object
         File file = new File(path);
         //Creating the directory
         boolean result = file.mkdir();
         if(!result){
             //Temp test code
             try {
                 String s = path; int n = s.length();
                  Path dir = Paths.get(path);
                    Files.createDirectory(dir);
             }
             catch (IOException e) {
                 System.out.println (e.getMessage());
             }
             catch (Exception e) {
                  System.out.println (e.getMessage());                
             }
           
            System.out.println("Couldn’t create specified directory: " + path );
            System.exit(5);
         }
    }   
    // return date as string of date taken from EXIF     
    private String getExifDate(File file) {

      try {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        int dateTag = ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL;

        if (directory != null && directory.containsTag(dateTag)) {
          Date date = directory.getDate(dateTag, TimeZone.getDefault());
          if (date == null) return "";
          return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
        } else {
          return "";
        }
      } catch (ImageProcessingException | IOException ex) {
            System.out.println(ex.getMessage());
            logIt ("Exif error: " + ex.getLocalizedMessage() + " for " + file.getPath());
            return "";
  }
}
 

    private String appendToPath(String path, String subfolder) {
        return path.trim() + "\\" + subfolder.trim();
    }
    

    private void logIt(String text) {
        if (preview) return;
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        logWriter.println(timeStamp + ": " +text);
        logWriter.flush();
    }
// End of Class

    private void moveToSkipped(File thisFile) {
        numSkipped += 1;
        if (preview) return;
        String qualifiedName = appendToPath(skippedPath, thisFile.getName().trim());
        if (renameItem ( qualifiedName, thisFile)) return;
        logIt("Move to Skipped failed: " + thisFile.getName());
    }

    private void moveToDups(File thisFile) {
        numDuplicates += 1;
        if (preview) return;
        String qualifiedName = appendToPath(dupsPath, thisFile.getName().trim());
        if (renameItem ( qualifiedName, thisFile)) return;
        removeIfDup(qualifiedName, thisFile);
    }

    private void moveToVideos(File thisFile) {
        numVideos += 1;
        if (preview) return;
        String qualifiedName = appendToPath(videoPath, thisFile.getName().trim());
        if (renameItem ( qualifiedName, thisFile)) return;
        removeIfDup(qualifiedName, thisFile);
    }
    
    private void moveToNoStamp(File thisFile) {
        numNoStamp += 1;
        if (preview) return;
        String qualifiedName = appendToPath(noStampPath, thisFile.getName().trim());
        if (renameItem ( qualifiedName, thisFile)) return;
        removeIfDup(qualifiedName, thisFile);
    }
    
    private void removeIfDup(String filePath, File thisFile) {
        if (preview) return;
        File yetAnotherDup = new File (filePath);
        if (yetAnotherDup.exists() && sameDrive) thisFile.delete();
    }
    
    
    private boolean moveBinFile(File sourceFile, String destFileString) {
        if (preview) return true;
        try {
            //create FileInputStream object for source file
            FileInputStream fin = new FileInputStream(sourceFile);
            //create FileOutputStream object for destination file
            FileOutputStream fout = new FileOutputStream(destFileString);
            if (buffer == null) buffer = new byte[16000];
            // copy file big hunk at at time
            int noOfBytes = 0;
 
            //read bytes from source file and write to destination file
            while( (noOfBytes = fin.read(buffer)) != -1 )
                {fout.write(buffer, 0, noOfBytes); }
            //close the streams
            fin.close();
            fout.close(); 
        }   //end of try
        catch(FileNotFoundException fnf)  {
            System.out.println("Specified file not found :" + fnf);
            return false;
        }
        catch(IOException ioe) {
            System.out.println("Error while copying file :" + ioe);
            return false;
        }   
        return true;
 }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnSelectSourceFolder = new javax.swing.JButton();
        lblSourceFolder = new javax.swing.JLabel();
        btnSelectDestFolder = new javax.swing.JButton();
        lblDestFolder = new javax.swing.JLabel();
        chkSubFolders = new javax.swing.JCheckBox();
        btnPreview = new javax.swing.JButton();
        btnProcess = new javax.swing.JButton();
        lblProgress = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Palibre Photo Organizer");

        btnSelectSourceFolder.setText("Select Source");

        lblSourceFolder.setText("No Source Folder Selected");

        btnSelectDestFolder.setText("Select Destination");
        btnSelectDestFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectDestFolderActionPerformed(evt);
            }
        });

        lblDestFolder.setText("No Destination Folder Selected");

        chkSubFolders.setSelected(true);
        chkSubFolders.setText("Process sub-folders");

        btnPreview.setText("Preview");
        btnPreview.setEnabled(false);

        btnProcess.setText("Process");
        btnProcess.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(60, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSelectDestFolder)
                        .addGap(18, 18, 18)
                        .addComponent(lblDestFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnSelectSourceFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(36, 36, 36)
                                        .addComponent(btnProcess)))
                                .addContainerGap(257, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(lblSourceFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(chkSubFolders, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                        .addComponent(btnPreview)
                        .addGap(320, 320, 320))))
            .addGroup(layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(lblProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectSourceFolder)
                    .addComponent(lblSourceFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSubFolders)
                    .addComponent(btnPreview))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectDestFolder)
                    .addComponent(lblDestFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50)
                .addComponent(btnProcess)
                .addGap(87, 87, 87)
                .addComponent(lblProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(145, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectDestFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectDestFolderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSelectDestFolderActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

// */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPreview;
    private javax.swing.JButton btnProcess;
    private javax.swing.JButton btnSelectDestFolder;
    private javax.swing.JButton btnSelectSourceFolder;
    private javax.swing.JCheckBox chkSubFolders;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblDestFolder;
    private javax.swing.JLabel lblProgress;
    private javax.swing.JLabel lblSourceFolder;
    // End of variables declaration//GEN-END:variables

   
}
