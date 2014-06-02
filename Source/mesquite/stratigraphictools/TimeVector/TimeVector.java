/* Code for stratigraphic tools package (http://mesquiteproject.org/... ).
Copyright 2005 by Sébastien Josse, Thomas Moreau and Michel Laurin.
Based on Mesquite source code copyright 1997-2005 W. & D. Maddison.
Available for Mesquite version 1.06
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stratigraphictools.TimeVector;


import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** ======================================================================== */

public class TimeVector extends TreeUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
	/*.................................................................................................................*/
	public  void useTree(Tree t) {
		int listTerm[];
		StringBuffer export = new StringBuffer();
		String ext, path, fileNameOK, fileName, titleSave = "Export time vector for terminal taxa (txt)...";
		int ex;
		FileDialog dialog;
		Frame parent = new Frame();
		//FILE SELECTION
		fileNameOK = "untitled.txt";
		path = getProject().getHomeDirectoryName();
		ext = "";
		MainThread.setShowWaitWindow(false);
		dialog = new FileDialog(parent, titleSave, FileDialog.SAVE);
		dialog.setDirectory(path);
		dialog.setBackground(ColorTheme.getInterfaceBackground());
		dialog.setFile(fileNameOK);
		dialog.setVisible(true);
		MainThread.setShowWaitWindow(true);
		fileName = dialog.getFile();
		if(fileName == null)
			return;
		path = dialog.getDirectory();
		ext = "";
		ex = fileName.lastIndexOf('.');
		if(ex == -1) {
			ext = "txt";
			fileName = fileName + ".txt";
		}
		else if(ex>0 && ex<fileName.length() - 1)
			ext = fileName.substring(ex+1).toLowerCase();
		if(!ext.equals("txt")) {
			titleSave = "You must select a TXT file !";
			useTree(t);
			return;
		}
		fileNameOK = fileName;
		dialog = null;
		File file = new File(path+fileNameOK);
		if(file.exists()) {
			if(!file.canWrite()) {
				MesquiteTrunk.mesquiteTrunk.logln("TXT file  "+fileNameOK+" for time vector can't be re-writed\n");
				file = null;
				return; 
			}
			else if(!file.delete()) {
				MesquiteTrunk.mesquiteTrunk.logln("Error exporting tree in "+fileNameOK+"\n");
				file = null;
				return;
			}
		}
		
		// CALCULATION
		listTerm=getTermBranch(t.getRoot(),t);
		export.ensureCapacity(listTerm.length*3);
		double precision = MesquiteInteger.queryInteger(containerOfModule(), "Round lengths", "Set precision (how many digit behind the coma)", 1);
		double heigth = 0;
		for(int i=0;i<listTerm.length;i++){
			heigth = getHeigthFromRoot(t,listTerm[i]);
			
			for(int k =0;k<precision;k++) // round length
				heigth*=10;
			if((heigth*10)%10>5) heigth = Math.floor(heigth+1);
			else heigth = Math.floor(heigth);
			for(int k =0;k<precision;k++)
				heigth/=10;
			
			export.append(t.getNodeLabel(listTerm[i])+"\t"+heigth+"\n");
		}
		
		// FILE CREATION
		try {
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(new String(export));
			writer.flush();
			writer.close();
			MesquiteTrunk.mesquiteTrunk.logln("TXT file "+fileNameOK+" for time vector succesfully writed in directory "+path);
			//System.out.println("TXT file "+fileNameOK+" for distance matrix succesfully writed in directory "+path);
		}
		catch( IOException e ) {
			MesquiteTrunk.mesquiteTrunk.logln("Error creating TXT file "+fileNameOK+" for time vector export");
			//System.out.println("Error creating TXT file "+fileNameOK+" for distance matrix export");
		}
		file = null;	
	}
	
	/*.................................................................................................................*/	
	/** Returns the length between a node and the root*/
	public double getHeigthFromRoot(Tree t, int node) {
		int n = node;
		double heigth = t.getBranchLength(n,1);
		while(t.motherOfNode(n) != t.getRoot()) {
			n = t.motherOfNode(n);
			heigth += t.getBranchLength(n,1);
		}
		
		
		
		return heigth;
	}
	/*-----------------------------------------*/
	/** Returns an array of terminal branch node numbers*/
	public int[] getTermBranch(int node, Tree t){
		int[] termBranchNode = new int[t.numberOfTerminalsInClade(node)];
		intI=0;
		findTermNodes(node,termBranchNode,t);
		return termBranchNode;
	}
	private int intI=0;
	private void findTermNodes(int node,int[] termNodes, Tree t){
		for(int daughter=t.firstDaughterOfNode(node);t.nodeExists(daughter);daughter=t.nextSisterOfNode(daughter)){
			if(!t.nodeExists(t.firstDaughterOfNode(daughter))){
				termNodes[intI]=daughter;
				intI++;
			}
			findTermNodes(daughter,termNodes,t);
		}
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Export time vector from taxa in tree ...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Export to time vector file (txt)";
   	 }
   	 
}
