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
package mesquite.stratigraphictools.ScaleDataWindowCoord; 
/*~~  */


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stratigraphictools.ScaleDataWindowMaker.*;
import mesquite.stratigraphictools.lib.ScaleData;
import mesquite.stratigraphictools.NodeLocsPaleo.*;


/** Coordinates the display of the spreadsheet editor window for character matrices.  This doesn't actually make the window (see BasicDataWindowMaker). */
public class ScaleDataWindowCoord extends FileInit {
	MesquiteSubmenuSpec scale;
	MesquiteMenuItemSpec editor;
	MesquiteCMenuItemSpec show, rect, width, magnetExt, magnetInt;
	CharacterData data;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
		if (getProject()==null || getFileCoordinator()==null)
			return false;

		return true;
  	 }
	
	String dataRef(CharacterData d, boolean internal){
  	 	if (internal)
  	 		return getProject().getCharMatrixReferenceInternal(d);
  	 	return getProject().getCharMatrixReferenceExternal(d);
	
  	 }
  	 
  	/*.................................................................................................................*/
  	 public CharacterData getScaleData(){
  	 	return data;
  	 }
  	 public void setScaleData(CharacterData data){
  	 	this.data = data;
  	 	((NodeLocsPaleo)getEmployer()).setScaleData(data);
  	 }
  	/*.................................................................................................................*/
	public void addScaleMenu(){
		scale = addSubmenu(null,"Adjustable scale");
		editor = addItemToSubmenu(null, scale, "Scale Matrix Editor...", makeCommand("showDataWindow", this));
		show = addCheckMenuItemToSubmenu(null, scale, "Show adjustable scale", makeCommand("toggleAdjustScale", getEmployer()), ((NodeLocsPaleo)getEmployer()).getAdjustScale());
		rect = addCheckMenuItemToSubmenu(null, scale, "Fill scale", makeCommand("toggleFillScale", getEmployer()), ((NodeLocsPaleo)getEmployer()).getFillScale());
		width = addCheckMenuItemToSubmenu(null, scale, "Extend scale under the tree", makeCommand("toggleExtendScale", getEmployer()), ((NodeLocsPaleo)getEmployer()).getExtendScale());
		magnetExt = addCheckMenuItemToSubmenu(null, scale, "Change all terminal branch length with same upper ending", makeCommand("toggleMagnetExt", getEmployer()), ((NodeLocsPaleo)getEmployer()).getMagnetExt());
		magnetInt = addCheckMenuItemToSubmenu(null, scale, "Change all internal branch length with same upper ending", makeCommand("toggleMagnetInt", getEmployer()), ((NodeLocsPaleo)getEmployer()).getMagnetInt());
		return;
	}
	public void deleteScaleMenu(){
		deleteMenuItem(editor);
		deleteMenuItem(show);
		deleteMenuItem(rect);
		deleteMenuItem(width);
		deleteMenuItem(magnetExt);
		deleteMenuItem(magnetInt);
		deleteMenuItem(scale);
	}
  	 /*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
	/**Returns command to hire employee if clonable*/
	public String getClonableEmployeeCommand(MesquiteModule employee){
		if (employee!=null && employee.getEmployer()==this) {
			if (employee.getHiredAs()==DataWindowMaker.class) {
				CharacterData d = (CharacterData)employee.doCommand("getDataSet", null, CommandChecker.defaultChecker);
				if (d != null) {
					return ("showDataWindow " + dataRef(d,true) + "  " + StringUtil.tokenize(employee.getName()) + ";");//quote
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ScaleDataWindowMaker) {
				ScaleDataWindowMaker dwm = (ScaleDataWindowMaker)e;
				CharacterData d = dwm.getCharacterData();
				if (d != null) {
					temp.addLine("showDataWindow " + dataRef(d,true), dwm);
				}
			}
		}
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the data editor window.  If a data editor window for this matrix already exists, it is brought to the front", "[number of data matrix to be shown] [name of data matrix to be shown]", commandName, "showDataWindow")) {  //IF WINDOW ALREADY SHOWN, JUST BRING IT TO FRONT
			for (int i = 0; i<getNumberOfEmployees(); i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ScaleDataWindowMaker) {
					ScaleDataWindowMaker sdwm = (ScaleDataWindowMaker)e;
					sdwm.setData(data); // TODO
					sdwm.doCommand("makeWindow", "", checker);
					sdwm.showScaleDataWindow();
					return sdwm;
				}
			}
			//if no data window module active, hire one
			ScaleDataWindowMaker sdwm = (ScaleDataWindowMaker)hireEmployee(ScaleDataWindowMaker.class, null);
			if (sdwm!=null){
				sdwm.setData(data); // TODO
				sdwm.doCommand("makeWindow", "", checker);
				sdwm.showScaleDataWindow();
			}
			return sdwm;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	
	/*.................................................................................................................*/
	public boolean saveScaleMatrix() {
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ScaleDataWindowMaker) {
				ScaleDataWindowMaker sdwm = (ScaleDataWindowMaker)e;
				sdwm.getScaleFile().write(containerOfModule(), (ScaleData)data, true);
				return true;
			}
		}
		return false;
	}
	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Scale Data Window Coordinator";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates the creation of scale data windows." ;
   	 }
   	 
}
	
