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
package mesquite.stratigraphictools.ScaleFileManager;


import java.awt.FileDialog;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Calendar;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterState;
import mesquite.stratigraphictools.*;
import mesquite.stratigraphictools.ManageScaleTimes.ManageScaleTimes;
import mesquite.stratigraphictools.lib.*;

/** Methods to read and write the passed ScaleData matrix on a CVS file.*/
public class ScaleFileManager extends FileAssistant {

	private String ext, path, projectPath, fileNameOK = "untitled.csv", fileName;
	private String titleOpen = "Open a CSV file for scale matrix editor...", titleSave = "Save the for scale matrix as CSV file...";
	private String matrix = "";
	private CharacterState recup = null;
	private NameReference colorNameRef = NameReference.getNameReference("color");
	private int i;
	private FileDialog dialog;
	
	public String getName(){
		return "ScaleFileFilter";
	}
	public String getFileName(){
		return fileNameOK;
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath(){
		return path;
	}
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public String giveTime() {
		Calendar now = Calendar.getInstance();         
		int hh = now.get(Calendar.HOUR_OF_DAY);         
		int mm = now.get(Calendar.MINUTE);         
		//int ss = now.get(Calendar.SECOND);         
		int month = now.get(Calendar.MONTH) +  1;         
		int day= now.get(Calendar.DAY_OF_MONTH);         
		int year = now.get(Calendar.YEAR);                    
		return (month+"/"+day+"/"+year+" "+ hh+":"+mm);
	}
	
	public boolean write(MesquiteWindow parent, ScaleData data, boolean as) {
		if(as) {
			MainThread.setShowWaitWindow(false);
			
			dialog = new FileDialog(parent.getParentFrame(), titleSave, FileDialog.SAVE);
			dialog.setDirectory(path);
			dialog.setBackground(ColorTheme.getInterfaceBackground());
			dialog.setFile(fileNameOK);
			dialog.setVisible(true);
		
			fileName = dialog.getFile();
			if(fileName == null)
				return false;
			path = dialog.getDirectory();
			
			ext = "";
			i = fileName.lastIndexOf('.');
			if(i == -1) {
				ext = "csv";
				fileName = fileName + ".csv";
			}
			else if(i>0 && i<fileName.length() - 1)
				ext = fileName.substring(i+1).toLowerCase();
			
			if(!ext.equals("csv")) {
				titleSave = "You must select a CSV file !";
				write(parent, data, true);
			}
			fileNameOK = fileName;
			dialog = null;
		
			MainThread.setShowWaitWindow(true);
			}
		
		
		File file = new File(path+fileNameOK);
		
		if(file.exists()) {
			if(!file.canWrite()) {
				MesquiteTrunk.mesquiteTrunk.logln("CSV file  "+fileNameOK+" for scale matrix can't be re-writed\n");
				System.out.println("CSV file  "+fileNameOK+" for scale matrix can't be re-written\n");
				file = null;
				return false; 
			}
			else if(!file.delete()) {
					MesquiteTrunk.mesquiteTrunk.logln("Error saving scale matrix CSV file "+fileNameOK+"\n");
					System.out.println("Error saving scale matrix CSV file "+fileNameOK+"\n");
					file = null;
					return false;
			}
		}
		
		matrix = "";
		NameReference colorNameRef = NameReference.getNameReference("color");
		for(int it=0;it<data.getNumTaxa();it++) {
			matrix = matrix+data.getTaxa().getName(it)+";";
			matrix = matrix+data.getCharacterState(recup,0,it).toString()+";";
			if(data.getCellObject(colorNameRef,0,it).toString() == "null")
				matrix = matrix+"0;\n";
			else
				matrix = matrix+data.getCellObject(colorNameRef,0,it)+";\n";
		}
		try {
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write("#CSV\n");
			writer.write("[Written by Stratigraphic Tools version "+getVersion()+" - Last modification: "+giveTime()+"]\n\n" );
			writer.write("Standart CSV type, allows you to edit with a standart spreadsheet editor (such as Excel);\n");
			writer.write("Field 1: Name of the period;\n");
			writer.write("Field 2: Duration;\n");
			writer.write("Field 3: Associated color;\n\n");
			writer.write("BEGIN STRATPERIODS;\n");
			writer.write(matrix);
			writer.write("END;");
			writer.flush();
			writer.close();
			MesquiteTrunk.mesquiteTrunk.logln("CSV file "+fileNameOK+" for scale matrix succesfully writed in directory "+path);
			System.out.println("CSV file "+fileNameOK+" for scale matrix succesfully writed in directory "+path);
			if(as)
				MesquiteMessage.notifyUser("Don't forget to save your Nexus file (to attach the new scale file) before quitting"); //TODO
		}
		catch( IOException e ) {
			MesquiteTrunk.mesquiteTrunk.logln("Error creating CSV file "+fileNameOK+" for scale matrix editor");
			System.out.println("Error creating CSV file "+fileNameOK+" for scale matrix editor");
		}
		
		file = null;	
				
		return true;
	}
		
	public CharacterData read(MesquiteWindow parent) {
		
		MainThread.setShowWaitWindow(false);

		dialog = new FileDialog(parent.getParentFrame(), titleOpen, FileDialog.LOAD);
		dialog.setDirectory(path);
		dialog.setBackground(ColorTheme.getInterfaceBackground());
		dialog.setFile(fileNameOK);
		dialog.setVisible(true);
		
		fileName = dialog.getFile();
		if(fileName == null)
			return null;
		path = dialog.getDirectory();
		dialog = null;
		
		ext = "";
		i = fileName.lastIndexOf('.');
		if(i>0 && i<fileName.length() - 1)
			ext = fileName.substring(i+1).toLowerCase();
		if(!ext.equals("csv")) {
			titleOpen = "You must select a CSV file !";
			return read(parent);
		}
		
		File file = new File(path+fileName);
		
		if(!file.exists()) {
			file = null;
			titleOpen = "csv file "+fileName+" doesn't exist, please select a csv file !";
			return read(parent);
		}
		else if(!file.canRead()) {
			MesquiteTrunk.mesquiteTrunk.logln("CSV file  "+fileName+" for scale matrix can't be read\n");
			System.out.println("CSV file  "+fileName+" for scale matrix can't be read\n");
			file = null;
			return null; 
		}
		
		fileNameOK = fileName;
		titleOpen = "Open a CSV file for scale matrix editor...";
		
		MainThread.setShowWaitWindow(true);
		
		BufferedReader reader;
		String buffer = "";
		int numTaxa = 0;
		String split[][] = new String[500][5];
		
		try {
			reader = new BufferedReader(new FileReader(file));
			
			while(!buffer.equals("BEGIN STRATPERIODS") && !buffer.equals("BEGIN STRATPERIODS;") && !buffer.equals("BEGIN STRATPERIODS;;"))
				buffer = reader.readLine();
				
			while(true) {
				if(buffer.equals("END") || buffer.equals("END;")|| buffer.equals("END;;")) break;
				
				buffer = reader.readLine();
				split[numTaxa] = buffer.split(";");
				numTaxa++;
			}
			reader.close();
		}
		catch( IOException e ) {
			MesquiteTrunk.mesquiteTrunk.logln("Error reading CSV file "+fileNameOK+" for scale matrix editor");
			System.out.println("Error reading CSV file "+fileNameOK+" for scale matrix editor");
			file = null;
			reader = null;
			return null;
		}
		numTaxa = numTaxa-1;
		//System.out.println(numTaxa);
		
		CharacterData bData;
		Taxa bTax = new STTaxa(numTaxa);
		//bTax.setScaleTaxa();
		ManageScaleTimes ScaleType = new ManageScaleTimes();
		Parser dataValue = new Parser("");
		
		for(int it = 0; it<numTaxa; it++)
			bTax.setTaxon(it,new Taxon(bTax));
		
		bData = ScaleType.getNewData(bTax,3);
		bData.setCharacterName(0,"Duration of each period");
		bData.setCharacterName(1,"End of the period");
		bData.setCharacterName(2,"Beginning of the period");
		
		for(int it = 0; it<numTaxa; it++) {
			bData.getTaxa().setTaxonName(it,split[it][0]);
			dataValue.setString(split[it][1]);
			bData.setState(0, it, dataValue, true, null);
			if(split[it][2].equals("null")) split[it][2] = "4";
			int c = (new Integer(split[it][2])).intValue();
			MesquiteInteger ms = new MesquiteInteger(c);
			bData.setCellObject(colorNameRef,-1,it,ms);
			bData.setCellObject(colorNameRef,0,it,ms);
			bData.setCellObject(colorNameRef,1,it,ms);
			bData.setCellObject(colorNameRef,2,it,ms);
		}
			
		split = null;
		file = null;
		MesquiteTrunk.mesquiteTrunk.logln("CSV file "+fileNameOK+" for scale matrix succesfully read from directory "+path);
		//System.out.println("CSV file "+fileNameOK+" for scale matrix succesfully read from directory "+path);
		return bData;
	}
	
	
	public CharacterData read(String fileName) {
		
		if(fileName == null)
			return null;
		else {
			this.fileName=fileName;
			this.path="";
		}
		
		ext = "";
		i = fileName.lastIndexOf('.');
		if(i>0 && i<fileName.length() - 1)
			ext = fileName.substring(i+1).toLowerCase();
		if(!ext.equals("csv")) {
			return null;
		}
		
		File file = new File(fileName);
		if(!file.exists()) {
			MesquiteTrunk.mesquiteTrunk.logln("CSV file "+file.getName()+" does not exist in the directory "+file.getAbsolutePath()+", searching in the current project path...");
			fileName = projectPath+file.getName();
			file = new File(fileName);
			if(!file.exists()) {
				MesquiteTrunk.mesquiteTrunk.logln("Error: CSV file "+file.getName()+" for scale matrix does not exist");
				file = null;
				return null;
			}
		}
		else if(!file.canRead()) {
			MesquiteTrunk.mesquiteTrunk.logln("CSV file  "+fileName+" for scale matrix can't be read\n");
			System.out.println("CSV file  "+fileName+" for scale matrix can't be read\n");
			file = null;
			return null; 
		}
		
		fileNameOK = fileName;
		path = file.getAbsolutePath();
		titleOpen = "Open a CSV file for scale matrix editor...";
		
		BufferedReader reader;
		String buffer = "";
		int numTaxa = 0;
		String split[][] = new String[500][5];
		
		try {
			reader = new BufferedReader(new FileReader(file));
			
			while(!buffer.equals("BEGIN STRATPERIODS") && !buffer.equals("BEGIN STRATPERIODS;") && !buffer.equals("BEGIN STRATPERIODS;;"))
				buffer = reader.readLine();
				
			while(true) {
				if(buffer.equals("END") || buffer.equals("END;") || buffer.equals("END;;")) break;
				
				buffer = reader.readLine();
				split[numTaxa] = buffer.split(";");
				numTaxa++;
			}
			reader.close();
		}
		catch( IOException e ) {
			MesquiteTrunk.mesquiteTrunk.logln("Error reading CSV file "+fileNameOK+" for scale matrix editor");
			System.out.println("Error reading CSV file "+fileNameOK+" for scale matrix editor");
			file = null;
			reader = null;
			return null;
		}
		numTaxa = numTaxa-1;
		//System.out.println(numTaxa);
		
		CharacterData bData;
		Taxa bTax = new STTaxa(numTaxa);
		//bTax.setScaleTaxa();
		ManageScaleTimes ScaleType = new ManageScaleTimes();
		Parser dataValue = new Parser("");
		
		for(int it = 0; it<numTaxa; it++)
			bTax.setTaxon(it,new Taxon(bTax));
		
		bData = ScaleType.getNewData(bTax,3);
		bData.setCharacterName(0,"Duration of each period");
		bData.setCharacterName(1,"End of the period");
		bData.setCharacterName(2,"Beginning of the period");
		
		for(int it = 0; it<numTaxa; it++) {
			bData.getTaxa().setTaxonName(it,split[it][0]);
			dataValue.setString(split[it][1]);
			bData.setState(0, it, dataValue, true, null);
			if(split[it][2].equals("null")) split[it][2] = "4";
			int c = (new Integer(split[it][2])).intValue();
			MesquiteInteger ms = new MesquiteInteger(c);
			bData.setCellObject(colorNameRef,-1,it,ms);
			bData.setCellObject(colorNameRef,0,it,ms);
			bData.setCellObject(colorNameRef,1,it,ms);
			bData.setCellObject(colorNameRef,2,it,ms);
		}
			
		split = null;
		file = null;
		MesquiteTrunk.mesquiteTrunk.logln("CSV file "+fileNameOK+" for scale matrix succesfully read");
		System.out.println("CSV file "+fileNameOK+" for scale matrix succesfully read");
		return bData;
	}
}
