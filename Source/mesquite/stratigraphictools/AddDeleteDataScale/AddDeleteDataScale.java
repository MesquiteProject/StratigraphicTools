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
package mesquite.stratigraphictools.AddDeleteDataScale; 

import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* to do:
- how to add to end of matrix using this tool?
*/

/* ======================================================================== */
public class AddDeleteDataScale extends DataWindowAssistantI implements KeyListener {
	MesquiteTable table;
	CharacterData data;
	//TableTool addCharsTool;
	TableTool addTaxaTool;
	MesquiteCommand deleteCommand;
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	 
   	public String getExpectedPath(){
		return getPath() + "addTaxa.gif";
  	 }

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			//addCharsTool = new TableTool(this, "addChars", getPath(), "addChars.gif", 7,7,"Add Characters", "This tool inserts new characters", MesquiteModule.makeCommand("addCharacters",  this) , null, null);
			//addCharsTool.setWorksOnColumnNames(false);
			//addCharsTool.setWorksBeyondLastColumn(true);
			//((MesquiteWindow)containerOfModule()).addTool(addCharsTool);
			addTaxaTool = new TableTool(this, "addTaxa", getPath(),"addTaxa.gif", 7,7,"Add Period", "This tool inserts new period", MesquiteModule.makeCommand("addTaxa",  this) , null, null);
			addTaxaTool.setWorksOnRowNames(true);
			addTaxaTool.setWorksBeyondLastRow(true);
			((MesquiteWindow)containerOfModule()).addTool(addTaxaTool);
		}
		
		//addMenuItem("Add characters...", makeCommand("addCharactersToEnd", this));
		addMenuItem("Add period...", makeCommand("addTaxaToEnd", this));
		addMenuItem("Delete Selected", deleteCommand = makeCommand("deleteSelected", this));
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	public void keyTyped(KeyEvent e){
		int mod = MesquiteEvent.getModifiers(e);
		if (!MesquiteEvent.commandOrControlKeyDown(mod)) {
			MesquiteWindow w = null;
			if (containerOfModule() instanceof MesquiteWindow)
				w = (MesquiteWindow)containerOfModule();
			if (w.getMode() != InfoBar.GRAPHICS || w.annotationHasFocus())
				return;
			if ((table == null || (table.anythingSelected() && !table.editingAnything())) &&(e.getKeyChar()== '\b' || e.getKeyCode()== KeyEvent.VK_BACK_SPACE || e.getKeyCode()== KeyEvent.VK_DELETE)) {
				deleteCommand.doItMainThread(null, "", this);
			}
		}
		
	}
	
	public void keyPressed(KeyEvent e){
	}
	
	public void keyReleased(KeyEvent e){
	}
	/*.................................................................................................................*/
   	 public void endJob(){
		if (containerOfModule() != null && ((MesquiteWindow)containerOfModule()) !=null) 
			MesquiteWindow.removeKeyListener(((MesquiteWindow)containerOfModule()), this);
   	 	super.endJob();
   	 }
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		if (containerOfModule() != null && ((MesquiteWindow)containerOfModule()) !=null) 
			MesquiteWindow.addKeyListener(((MesquiteWindow)containerOfModule()), this);
	}
	
	private void deleteSelected(boolean byCommand){
   	 		if (table!=null && data !=null){
   	 			if (!byCommand && (table.editingAnything() || !(table.anyColumnSelected() || table.anyRowSelected())))
   	 				return;
				 /*if (table.anyColumnSelected()) {
				 	if (!commandRec.scripting() && !AlertDialog.query(containerOfModule(), "Delete Times?","You are not alow to do that !", "Yes", "No"))
						return;
					Vector blocks = new Vector();
					while(table.anyColumnSelected()){
						int lastOfBlock = table.lastColumnSelected();
						int firstOfBlock = table.startOfLastColumnBlockSelected();
						if (lastOfBlock>=0){
							for (int i=firstOfBlock; i<=lastOfBlock; i++)
								table.deselectColumn(i);
							data.deleteParts(firstOfBlock,lastOfBlock-firstOfBlock+1);
							data.deleteInLinked(firstOfBlock,lastOfBlock-firstOfBlock+1, false); 
							blocks.addElement( new int[] {firstOfBlock,lastOfBlock-firstOfBlock+1});  //do as series of contiguous blocks
						}
					}
					
					for (int i=0; i<blocks.size(); i++) {
						data.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED, (int[])blocks.elementAt(i)));  //do as series of contiguous blocks
						data.notifyInLinked(new Notification(MesquiteListener.PARTS_DELETED, (int[])blocks.elementAt(i)));  //do as series of contiguous blocks
					}
				}*/
				if (table.anyRowSelected()) {
					Bits rows = table.getRowsSelected();
					/*if (rows.numBitsOn() == data.getTaxa().getNumTaxa()){
						discreetAlert( "You cannot delete all of the period; \nIf you want to hide the Adjustable Scale, use the command Show Adjustable Scale in the Display menu of the tree;\nthe command will be ignored");
						return;
				 	}*/
				 	if (!MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Delete period?","Are you sure you want to delete the selected period?", "Yes", "No"))
						return;
					
					for (int i=table.getNumRows()-1; i>=0; i--){
						if (rows.isBitOn(i)) {
							data.getTaxa().deleteTaxa(i,1, false);
						}
					}
	 	   			Notification nn;
	 	   			
					data.getTaxa().notifyListeners(this, nn = new Notification(MesquiteListener.PARTS_DELETED));//do as series of contiguous blocks
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED).setNotificationNumber(nn.getNotificationNumber()));//do as series of contiguous blocks
				}
			}
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	/*if (checker.compare(this.getClass(), "Adds characters to data matrix", "[Character # before which new character are to be added] [row on which touched] [how many new characters]", commandName, "addCharacters")) {
   	 		if (data !=null){
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
	   			if (column>=0) {
		   			column--; //to put characters before one touched
		   			int howMany = MesquiteInteger.queryInteger(containerOfModule(), "Add characters", "How many characters to add?", 1,1,MesquiteInteger.infinite);
		   			if (MesquiteInteger.isCombinable(howMany) && howMany>0) {
		   				data.addParts(column, howMany);
		   				data.addInLinked(column, howMany, true);
						data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {column, howMany}));
		   			}
	   			}
	   			else if (column==-2) {
		   			int howMany = MesquiteInteger.queryInteger(containerOfModule(), "Add characters", "How many characters to add?", 1,1,MesquiteInteger.infinite);
		   			if (MesquiteInteger.isCombinable(howMany) && howMany>0) {
		   				data.addParts(table.numColumnsTotal, howMany);
		   				data.addInLinked(table.numColumnsTotal, howMany, true);
						data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {table.numColumnsTotal, howMany}));
		   			}
	   			}
			}
		}*/
    	 	//else if (checker.compare(this.getClass(), "Adds taxa to block of taxa", "[column on which touched] [Taxon # before which new taxa are to be added] [how many new taxa]", commandName, "addTaxa")) {
    	 	if (checker.compare(this.getClass(), "Adds Period to block of Period", "[column on which touched] [Period # before which new Period are to be added] [how many new Period]", commandName, "addTaxa")) {
    	 		if (data !=null){
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
	   			if (row>=0) {
	   				row--; //to put taxa before one touched
		   			int howMany = MesquiteInteger.queryInteger(containerOfModule(), "Add period", "How many period to add?", 1,1,Taxa.MAXNUMTAXA-data.getNumTaxa());
		   			if (MesquiteInteger.isCombinable(howMany) && howMany>0) {
		   				data.getTaxa().addTaxa(row, howMany, true);
		   			}
	   			}
	   			else if (row==-2) {
		   			int howMany = MesquiteInteger.queryInteger(containerOfModule(), "Add period", "How many period to add?", 1,1,Taxa.MAXNUMTAXA-data.getNumTaxa());
		   			if (MesquiteInteger.isCombinable(howMany) && howMany>0) {
		   				data.getTaxa().addTaxa(table.numRowsTotal, howMany, true);
		   			}
	   			}
			}
		}
    	 	/*else if (checker.compare(this.getClass(), "Appends new character to end of matrix", "[How many to add]", commandName, "addCharactersToEnd")) {
   	 		if (data !=null){
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int howMany= MesquiteInteger.fromString(arguments, io);
	   			if (!MesquiteInteger.isCombinable(howMany))
	   				howMany = MesquiteInteger.queryInteger(containerOfModule(), "Add characters", "How many characters to add?", 1,1,MesquiteInteger.infinite);
	   			if (MesquiteInteger.isCombinable(howMany)&& howMany>0) {
	   				data.addParts(data.getNumChars(), howMany);
	   				data.addInLinked(data.getNumChars(), howMany, true);
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {data.getNumChars(), howMany}));
	   			}
			}
		}*/
    	 	else if (checker.compare(this.getClass(), "Appends new period to end of block of period", "[how many to add]", commandName, "addTaxaToEnd")) {
   	 		if (data !=null){
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int howMany= MesquiteInteger.fromString(arguments, io);
	   			if (!MesquiteInteger.isCombinable(howMany))
	   				howMany = MesquiteInteger.queryInteger(containerOfModule(), "Add period", "How many period to add?", 1,1,Taxa.MAXNUMTAXA-data.getNumTaxa());
	   			if (MesquiteInteger.isCombinable(howMany)&& howMany>0) {
	   				int wh = data.getNumTaxa();
	   				data.getTaxa().addTaxa(wh, howMany, true);
	   			}
			}
		}
    	 	//else if (checker.compare(this.getClass(), "Deletes seleted characters or taxa", null, commandName, "deleteSelected")) {
    	 	else if (checker.compare(this.getClass(), "Deletes seleted period", null, commandName, "deleteSelected")) {
   	 		deleteSelected(true);
		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add/Delete Scale Data";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies the tools for adding and deleting scale period for the adjustable scale module." ;
   	 }
   	 
}

