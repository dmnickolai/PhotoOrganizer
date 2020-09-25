
package photoorganizer;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class FileTreeBuilder extends JPanel {
    JTree tree;
    DefaultMutableTreeNode treeRootNode;
    //String treeRootPath = "d:\\Download-D";
    String treeRootPath = "d:\\";
    
    public static void main(String s[]){
      MyJFrame frame = new MyJFrame("Directory explorer");
    }
    
    
    
    public class MyFileNodeObject {
        File thisFile;
        
        //Constructor
        MyFileNodeObject (File f){ 
            String s1 = f.toString();
            String s2 = f.toPath().toString();
            String s3 = f.toURI().toString();
            
            thisFile = f; 
        }
        
        @Override
        public String toString() { 
            String name = thisFile.getName();
            if (name.isEmpty() ){
                name = thisFile.toString();
                int i = 1;
            }
            return name;
            }
          
    }
    // Simple Tree Constuctor
    FileTreeBuilder() {
        JTree fileTree = buildTree(treeRootPath);
        File rootEntry = new File (treeRootPath);
        
        MyFileNodeObject myNode = new MyFileNodeObject(rootEntry);
        treeRootNode = new DefaultMutableTreeNode(myNode, true);
        // Build assuming no files are found
        //boolean b = getList(treeRootNode, rootEntry, false);
        //System.out.println(" Result for entire tree = " + b);
        setLayout(new BorderLayout());
        //tree = new JTree(treeRootNode);
        //fileTree.setVisible(true);
        fileTree.expandRow(0);
        add(new JScrollPane((JTree)fileTree),"Center");
    }
    // Set size of Panel
    public Dimension getPreferredSize(){
      return new Dimension(200, 120);
    }
    
    protected JTree buildTree (String rootPath) {
        //
        File rootEntry = new File (treeRootPath);       
        MyFileNodeObject myRootNodeObject = new MyFileNodeObject(rootEntry);
        treeRootNode = new DefaultMutableTreeNode(myRootNodeObject, true);
        processDirectory(treeRootNode, rootEntry);
        return new JTree(treeRootNode);
    }
    
    protected void processDirectory (DefaultMutableTreeNode parent, File thisDirectory){
        DefaultMutableTreeNode child;
        //String parentName = parent.toString();
        //String childName;
        if(thisDirectory.isDirectory()) { //it had better be
            File[] fList = thisDirectory.listFiles();
            int numTargets = 0;
            if (fList!= null ) {            
                for(int i = 0; i  < fList.length; i++) {
                    if (!fList[i].isHidden()) {
                        if(fList[i].isDirectory() ) {
                            child = new DefaultMutableTreeNode(new MyFileNodeObject(fList[i]));
                            //childName = child.toString();
                            processDirectory (child, fList[i]);
                            int cnt = child.getChildCount();
                            if (child.getChildCount() > 0) {
                                  parent.add(child);
                            }
                         } 
                         else {
                            if (!fList[i].isHidden()) {
                                if (targetFile(fList[i])) numTargets += 1;
                            } 
                         }
                    }
                 }
            
            // If any targets are in this folder or any subfolder reeturn node else null;
                if (numTargets > 0) {
                    DefaultMutableTreeNode numString 
                            = new DefaultMutableTreeNode ("Folder contains " + numTargets + " targets");
                    parent.add(numString);
                }
            } // end of for loop iterating over file list
          
        }  // end of if list is null
        
    }
    
    protected boolean targetFile(File f) {
        String[] targetExt = new String[] { "img", "jpg", "jpeg" };
        String name = f.getName();
        String ext = name.substring(name.lastIndexOf('.') + 1);
        for (int i = 0; i< targetExt.length; i++ ) {
            if (ext.equalsIgnoreCase(targetExt[i])) return true;
        }
        return false;
    }
    public boolean  getList(DefaultMutableTreeNode baseNode, File f, boolean found) {
        boolean result = false;
        DefaultMutableTreeNode child = baseNode;
        //System.out.println(f.getName() + f.isDirectory());
        if(!f.isDirectory()) {
            // Variable "f" is a file (not a directory)
            //We keep only PDF source file for display in this HowTo 
            if (targetFile(f)) { 
               child = new DefaultMutableTreeNode(new MyFileNodeObject(f));
               baseNode.add(child);
               result = true;
            }
            
        }
        else {
            // Process Directory
            
            if (!f.toString().equalsIgnoreCase(treeRootPath)) {
                // Only add directory if not Root
                //System.out.println(" Adding directory " + f.getName() + " to " + baseNode.toString());  
                child = new DefaultMutableTreeNode(new MyFileNodeObject(f));
                baseNode.add(child);
            }
                        // Get list of all the entries in the Directory
            File[] fList = f.listFiles();
            if (fList!= null ) {
               // System.out.println("fList count = " + fList.length);
               // Interate over the list
               for(int i = 0; i  < fList.length; i++) {
                   if (!fList[i].isHidden()) {
                       boolean nextLevel = getList(child, fList[i], found);
                   
                       result = result || nextLevel;
                       //System.out.println("adding " +fList[i].getName() + " to " + child.toString() + " " + nextLevel); 
                   }
                //System.out.println (result + " in tree " + f.getName());
               }
               if (!result) baseNode.remove(child);
               else
                   baseNode.add(new DefaultMutableTreeNode("Contains Image Files") );
            }
            
        };
        //System.out.println("result=" + result);
        return result;
    }
  
    
  } 

class MyJFrame extends JFrame {
  
  FileTreeBuilder panel;
  MyJFrame(String s) {
    super(s);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(500,500);
    getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    panel = new FileTreeBuilder();
    setVisible(true);
    getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    getContentPane().add(panel,"Center");    
  }
}