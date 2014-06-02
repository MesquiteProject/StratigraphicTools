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
package mesquite.stratigraphictools.STCollapse;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class STCollapse extends TreeDisplayAssistantI {
	public Vector extras;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		STCollapseToolExtra newPj = new STCollapseToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Collapse Branch";
	}
	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "A tool that collapses branches.";
	}
}

/* ======================================================================== */
class STCollapseToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool STCollapseTool;
	STCollapse STCollapseModule;
	Tree tree;
	MesquiteInteger pos = new MesquiteInteger();
	String message="";
	
	public STCollapseToolExtra (STCollapse ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		STCollapseModule = ownerModule;
		STCollapseTool = new TreeTool(this, "STCollapse", ownerModule.getPath(), "STCollapse.gif", 5,2,"Collapse Branch", "This tool is used to collapse branches.");
		STCollapseTool.setTouchedCommand(MesquiteModule.makeCommand("collapse",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(STCollapseTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		
		if (checker.compare(this.getClass(), "Collapses branch", "[branch number]", commandName, "collapse")) {
			int node= MesquiteInteger.fromFirstToken(arguments, pos);
			MesquiteTree mt = (MesquiteTree) tree;
			
			if (!mt.nodeExists(node))
				return null;
			if (mt.nodeIsInternal(node) && (node!=mt.getRoot())) {
				int d = mt.firstDaughterOfNode(node);
	
				while (mt.nodeExists(d)) {	// connecting all Daughters to their grandmother
					double newSisterLength = mt.getBranchLength(node,1)+mt.getBranchLength(d,1);
					int precision = 3; // round length
					for(int k =0;k<precision;k++)
						newSisterLength*=10;
					if((newSisterLength*10)%10>5) newSisterLength = Math.floor(newSisterLength+1);
					else newSisterLength = Math.floor(newSisterLength);
					for(int k =0;k<precision;k++)
						newSisterLength/=10;
					mt.setBranchLength(d,newSisterLength,false);
					d = mt.nextSisterOfNode(d);
				}
			}
			mt.collapseBranch(node,true);
		}
		return null;
	}
}



