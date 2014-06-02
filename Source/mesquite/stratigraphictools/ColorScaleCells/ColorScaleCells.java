/* Code for stratigraphic tools package (http://mesquiteproject.org/... ).
Copyright 2005 by S�bastien Josse, Thomas Moreau and Michel Laurin.
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
package mesquite.stratigraphictools.ColorScaleCells; 


import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;

/*   to do:
	- deal with Option cursor, Shift cursor
	*new in 1.02*
*/


/* ======================================================================== */
public class ColorScaleCells extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	TableTool colorTool; 
	MesquiteTable table;
	int currentColor = ColorDistribution.numberOfRed;
	String colorString = "Color Red";
	int savedColor = currentColor;
	MesquiteBoolean removeColor;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			colorNameRef = NameReference.getNameReference("color");
			colorTool = new TableTool(this, "ColorCells", getPath(), "color.gif", 1,1,colorString, "This tool colors the cells of a matrix. Periods in the tree will be painted with this color", MesquiteModule.makeCommand("colorCell", this), null, null);
			colorTool.setWorksOnColumnNames(true);
			colorTool.setWorksOnRowNames(true);
			((MesquiteWindow)containerOfModule()).addTool(colorTool);
			colorTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching button
			
		}
		else return false;
		MesquiteSubmenuSpec mss = addSubmenu(null, "Cell paint color", makeCommand("setColor",  this), ColorDistribution.standardColorNames);
		removeColor = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Remove color", makeCommand("removeColor",  this), removeColor);
		addMenuItem(null, "Remove all color", makeCommand("removeAllColor",  this));
		addMenuItem(null, "-", null);
		addMenuItem(null, "Color Selected", makeCommand("colorSelected",  this));
		return true;
	}
   	 public boolean setActiveColors(boolean active){
   		 return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
   	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	NameReference colorNameRef = NameReference.getNameReference("color");
   	private void removeColor(int ic, int it, boolean notify){
   		setColor(-1,it,4);
   		setColor(0,it,4);
   		setColor(1,it,4);
   		setColor(2,it,4);
   		//setColor(ic, it, -1);
		if (notify) {
			//table.redrawCell(ic,it);
			table.redrawCell(-1,it);
			table.redrawCell(0,it);
			table.redrawCell(1,it);
			table.redrawCell(2,it);
			}
		}
   	
    private void colorSelected(){
   		if (data == null)
   			return;
		for (int ic = -1; ic<data.getNumChars(); ic++)
			for (int it = -1; it<data.getNumTaxa(); it++)
				if (table.isCellSelectedAnyWay(ic, it))
					setColor(ic, it, currentColor);
   	}
  	private void removeAllColor(boolean notify){
   		if (data == null)
   			return;
		for (int ic = -1; ic<data.getNumChars(); ic++)
			for (int it = -1; it<data.getNumTaxa(); it++)
				setColor(ic, it, 4);
		if (notify)
			table.repaintAll();
   	}
  	private void setColor(int ic, int it, int c){
   		if (data == null)
   			return;
		if (ic<0 && it<0){
		}
		/*else if (ic<0) { //taxon
			data.getTaxa().setAssociatedLong(colorNameRef, it, c);
		}*/
		else if (it < 0){ //character
			data.setAssociatedLong(colorNameRef, ic, c);
		}
		else if (!MesquiteLong.isCombinable(c) || c<0){
			//data.setCellObject(colorNameRef, ic, it, null);
			data.setCellObject(colorNameRef, -1, it, null);
			data.setCellObject(colorNameRef, 0, it, null);
			data.setCellObject(colorNameRef, 1, it, null);
			data.setCellObject(colorNameRef, 2, it, null);
			}
		else {
			MesquiteInteger ms = new MesquiteInteger(c);
			//data.setCellObject(colorNameRef, ic, it, ms);
			data.setCellObject(colorNameRef, -1, it, ms);
			data.setCellObject(colorNameRef, 0, it, ms);
			data.setCellObject(colorNameRef, 1, it, ms);
			data.setCellObject(colorNameRef, 2, it, ms);
		}
		//table.redrawCell(ic,it);
		table.redrawCell(-1,it);
		table.redrawCell(0,it);
		table.redrawCell(1,it);
		table.redrawCell(2,it);
		
		//parametersChanged(new CommandRecord(false));
		//getEmployer().getModuleWindow().toFront();
	}
   	private int getColor(int ic, int it){
   		
   		if (data == null)
   			return 0;
		/*if (ic<0){  //taxon
			long c = data.getTaxa().getAssociatedLong(colorNameRef, it);
			if (MesquiteLong.isCombinable(c))
				return (int)c;
		}*/
		else if (it<0){ //character
			long c = data.getAssociatedLong(colorNameRef, ic);
			if (MesquiteLong.isCombinable(c))
				return (int)c;
		}
		else {
			//Object obj = data.getCellObject(colorNameRef, ic, it);
			Object obj = data.getCellObject(colorNameRef, 0, it);
	   		if (obj != null && obj instanceof MesquiteInteger)
	   			return ((MesquiteInteger)obj).getValue();
		}
   		return MesquiteInteger.unassigned;
   	}
   	public Color getCellColor(int ic, int it){
   		
   		//int color = getColor(ic, it);
   		int color = getColor(0, it);
   		if (MesquiteLong.isCombinable(color))
   			return ColorDistribution.getStandardColor(color);
   		else
   			return null;
   	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setColor " + ColorDistribution.getStandardColorName(ColorDistribution.getStandardColor((int)currentColor)));
  	 	temp.addLine("removeColor " + removeColor.toOffOnString());
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(),  "Colors cell", "[column touched][row touched]", commandName, "colorCell")) {
	   	 		if (data == null)
	   	 			return null;
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
				if (MesquiteInteger.isCombinable(column)&& (MesquiteInteger.isCombinable(row))) {
		   	 		if (!MesquiteLong.isCombinable(currentColor))
		   	 			removeColor(column, row, true);
		   	 		else
		   	 			setColor(column, row, currentColor);
		   	 	}
				if (!MesquiteThread.isScripting()){
					MesquiteModule mb = getEmployer();
					if (mb instanceof DataWindowMaker)
						((DataWindowMaker)mb).requestCellColorer(this,0, 0, "Do you want the Period to be colored using the colors you are assigning?");
						
				}
			}
    	 	else	if (checker.compare(this.getClass(), "Sets the color to be used to paint the line scale", "[name of color]", commandName, "setColor")) {
    	 		int bc = ColorDistribution.standardColorNames.indexOf(parser.getFirstToken(arguments)); 
			if (bc >=0 && MesquiteLong.isCombinable(bc)){
				removeColor.setValue(false);
				currentColor = bc;
				savedColor = bc;
				colorString = "Color " + ColorDistribution.standardColorNames.getValue(bc);
			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Removes color from the all line", null, commandName, "removeAllColor")) {
		   	 removeAllColor(true);
		   	}
    	 	else if (checker.compare(this.getClass(), "Colors period of selected cells", null, commandName, "colorSelected")) {
		   	 colorSelected();
		   	 table.repaintAll();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the paint brush so that it removes colors from any branches touched", null, commandName, "removeColor")) {
			if (StringUtil.blank(arguments))
				removeColor.setValue(!removeColor.getValue());
			else
				removeColor.toggleValue(parser.getFirstToken(arguments));
			
			if (removeColor.getValue()) {
				colorString = "Remove color";
				//currentColor = MesquiteInteger.unassigned;
				currentColor = 4;
			}
			else {
				colorString = "Color " + ColorDistribution.standardColorNames.getValue((int)currentColor);
				currentColor = savedColor;
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Scale Assigned Colors";
   	 }

	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Provides a tool with which to color cells of a matrix.";
   	 }
  	public void viewChanged(){
  	}
  	public String getColorsExplanation(){
  		return "";
  	}
	public ColorRecord[] getLegendColors(){
		return null;
	}
}


	

