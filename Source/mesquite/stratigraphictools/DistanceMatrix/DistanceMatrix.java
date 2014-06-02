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
package mesquite.stratigraphictools.DistanceMatrix;


import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** ======================================================================== */

public class DistanceMatrix extends TreeUtility {
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
		String ext, path, fileNameOK, fileName, titleSave = "Export the current tree in a distance matrix file (txt) ...";
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
				MesquiteTrunk.mesquiteTrunk.logln("TXT file  "+fileNameOK+" for distance matrix can't be re-writed\n");
				file = null;
				return; 
			}
			else if(!file.delete()) {
				MesquiteTrunk.mesquiteTrunk.logln("Error exporting tree in "+fileNameOK+"\n");
				file = null;
				return;
			}
		}
		listTerm=getTermBranch(t.getRoot(),t);
		export.ensureCapacity(listTerm.length*listTerm.length*5);
		int k=1, tempNode1=0,tempNode2=0;
		double path1=0, path2=0, distance=0;
		for(int i=0;i<listTerm.length-1;i++){
			for(int l=0;l<k-1;l++)
				export.append("\t");
			for(int j=k;j<listTerm.length;j++){
				path1=0; path2=0; distance=0; tempNode1=t.getRoot(); tempNode2=t.getRoot();
				//look for first ancestors of same level
				if(getBranchLevel(t,listTerm[i],t.getRoot(),0)<getBranchLevel(t,listTerm[j],t.getRoot(),0)){
					tempNode1=listTerm[i];
					tempNode2=listTerm[j];
					while(getBranchLevel(t,tempNode2,t.getRoot(),0)>getBranchLevel(t,listTerm[i],t.getRoot(),0) && tempNode2!=t.getRoot()){
						path2+=t.getBranchLength(tempNode2,1);
						tempNode2=t.motherOfNode(tempNode2);
					}
				}
				else{
					tempNode1=listTerm[j];
					tempNode2=listTerm[i];
					while(getBranchLevel(t,tempNode2,t.getRoot(),0)>getBranchLevel(t,listTerm[j],t.getRoot(),0) && tempNode2!=t.getRoot()){
						path2+=t.getBranchLength(tempNode2,1);
						tempNode2=t.motherOfNode(tempNode2);
					}
				}
				//look for the common ancestor
				while(tempNode1!=tempNode2 && tempNode1!=t.getRoot() && tempNode2!=t.getRoot()){
					path1+=t.getBranchLength(tempNode1,1);
					path2+=t.getBranchLength(tempNode2,1);
					tempNode1=t.motherOfNode(tempNode1);
					tempNode2=t.motherOfNode(tempNode2);
				}
				distance=path1+path2;
				distance*=10;
				if(distance%1>0.5){
					distance=Math.floor(distance);
					distance++;
					distance/=10;
				}
				else{
					distance=Math.floor(distance);
					distance/=10;
				}
				if(j<listTerm.length-1)
					export.append(distance+"\t");
				else export.append(distance);
			}
			k++;
			if(i<listTerm.length-2)
				export.append("\n");
		}
		// FILE CREATION
		try {
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(new String(export));
			writer.flush();
			writer.close();
			MesquiteTrunk.mesquiteTrunk.logln("TXT file "+fileNameOK+" for distance matrix succesfully writed in directory "+path);
			//System.out.println("TXT file "+fileNameOK+" for distance matrix succesfully writed in directory "+path);
		}
		catch( IOException e ) {
			MesquiteTrunk.mesquiteTrunk.logln("Error creating TXT file "+fileNameOK+" for distance matrix export");
			//System.out.println("Error creating TXT file "+fileNameOK+" for distance matrix export");
		}
		file = null;	
	}
	/*.................................................................................................................*/	
	/** Returns the number of nodes between  branch A and branch B*/
	public int getBranchLevel(Tree t,int nodeA,int nodeB,int level){
		if(nodeA==t.getRoot() || nodeA==nodeB || !t.nodeExists(t.motherOfNode(nodeA))) return level;
		else {
			level++;
			return getBranchLevel(t,t.motherOfNode(nodeA),nodeB,level);
		}
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
		return "Export tree in distance matrix format...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Export to distance matrix file (txt)";
   	 }
   	 
}
