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
package mesquite.stratigraphictools.STMoveBranch;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class STMoveBranch extends TreeDisplayAssistantI {
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
		STMoveBranchToolExtra newPj = new STMoveBranchToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Move Branch";
	}
	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "A tool that moves branches.";
	}
}

/* ======================================================================== */
class STMoveBranchToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool STMoveBranchTool;
	STMoveBranch STMoveBranchModule;
	Tree tree;
	MesquiteInteger pos = new MesquiteInteger();
	String message="";
	
	public STMoveBranchToolExtra (STMoveBranch ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		STMoveBranchModule = ownerModule;
		STMoveBranchTool = new TreeTool(this, "STMoveBranch", ownerModule.getPath(), "STArrow.gif", 5,2,"Move Branch", "This tool is used to move branches.");
		STMoveBranchTool.setTransferredCommand(MesquiteModule.makeCommand("move",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(STMoveBranchTool);
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
		
		if (checker.compare(this.getClass(), "Move branch", "[branch being moved] [branch onto which first will be attached]", commandName, "move")) {
			MesquiteInteger io = new MesquiteInteger(0);
   			int branchFrom= MesquiteInteger.fromString(arguments, io);
   			int branchTo= MesquiteInteger.fromString(arguments, io);
   			int oldBranchFrom = branchFrom, oldBranchTo = branchTo;
   			
   			MesquiteTree mt = (MesquiteTree) tree;
   			
   			if (branchFrom==branchTo)
   				return null;
   			else if (!mt.nodeExists(branchFrom) || !mt.nodeExists(branchTo))
   				return null;
   			else if  (mt.descendantOf(branchTo,branchFrom))
   				return null;
   			else if  (branchTo == mt.motherOfNode(branchFrom) && !mt.nodeIsPolytomous(branchTo))
   				return null;
   			else if (mt.nodesAreSisters(branchTo, branchFrom) && (mt.numberOfDaughtersOfNode(mt.motherOfNode(branchFrom))==2))
   				return null;
   			else if (mt.numberOfDaughtersOfNode(mt.motherOfNode(branchFrom))==1) //TODO: NOTE that you can't move a branch with 
   				return null;
   			
   			//mt.checkTreeIntegrity(mt.getRoot());
   			
   			double posFrom = getHeightToRoot(mt,branchFrom);
   			double posMotherFrom = getHeightToRoot(mt,mt.motherOfNode(branchFrom));
   			double posTo = getHeightToRoot(mt,branchTo);
   			double posMotherTo = getHeightToRoot(mt,mt.motherOfNode(branchTo));
   			boolean moveBranchKeepingPosition = true;

   			if(posMotherTo>posFrom) { // IMPOSSIBLE DE CONSERVER LA HAUTEUR
   				moveBranchKeepingPosition = false; 
   				MesquiteMessage.notifyUser("Branch height will be modified");
   			}
   			
   			double sisterLength = 0;
   			int sister = 0;
   			int precision = 3;
   			if(moveBranchKeepingPosition && (mt.numberOfDaughtersOfNode(mt.motherOfNode(branchFrom))==2)) {
   				sister = mt.firstDaughterOfNode(mt.motherOfNode(branchFrom));
   				if(sister == branchFrom)
   					sister = mt.lastDaughterOfNode(mt.motherOfNode(branchFrom));
   				sisterLength = mt.getBranchLength(sister,1)+mt.getBranchLength(mt.motherOfNode(sister),1);
   				// round length
   				for(int k =0;k<precision;k++)
   					sisterLength*=10;
   				if((sisterLength*10)%10>5) sisterLength = Math.floor(sisterLength+1);
   				else sisterLength = Math.floor(sisterLength);
   				for(int k =0;k<precision;k++)
   					sisterLength/=10;
   				sister = mt.motherOfNode(sister);
   			}
   			double fromLength = mt.getBranchLength(branchFrom);
   			double toLength = mt.getBranchLength(branchTo);
   					
   			//first, pluck out "branchFrom"
   			//next, attach branchFrom clade onto branchTo
   			if (mt.snipClade(branchFrom, false)) {
   				int newMother = mt.graftClade(branchFrom, branchTo, false);
   				if (mt.hasBranchLengths() && MesquiteDouble.isCombinable(toLength)) { //remember length of branches to adjust afterward

   					if(moveBranchKeepingPosition) {
   						if(posMotherFrom>=posTo) {
   							//MesquiteMessage.notifyUser("CAS 1 : posMotherFrom >= posTo");
   							double lengthFrom = posFrom - posTo;
   							for(int k =0;k<precision;k++) // round length
   								lengthFrom*=10;
   							if((lengthFrom*10)%10>5) lengthFrom = Math.floor(lengthFrom+1);
   							else lengthFrom = Math.floor(lengthFrom);
   							for(int k =0;k<precision;k++)
   								lengthFrom/=10;
   							mt.setBranchLength(newMother, toLength/2.0, false);
   							mt.setBranchLength(branchTo, toLength/2.0, false);
   							mt.setBranchLength(branchFrom, lengthFrom + (toLength/2.0), false);
   							
   						}
   						else if(posMotherFrom>posMotherTo) {
   							//MesquiteMessage.notifyUser("CAS 2 : posMotherFrom > posMotherTo");
   							double newMotherLength = posMotherFrom-posMotherTo;
   							for(int k =0;k<precision;k++) // round length
   								newMotherLength*=10;
   							if((newMotherLength*10)%10>5) newMotherLength = Math.floor(newMotherLength+1);
   							else newMotherLength = Math.floor(newMotherLength);
   							for(int k =0;k<precision;k++)
   								newMotherLength/=10;
   							mt.setBranchLength(newMother, newMotherLength, false);
   							
   							double newBranchToLength = toLength - (posMotherFrom-posMotherTo);
   							for(int k =0;k<precision;k++) // round length
   								newBranchToLength*=10;
   							if((newBranchToLength*10)%10>5) newBranchToLength = Math.floor(newBranchToLength+1);
   							else newBranchToLength = Math.floor(newBranchToLength);
   							for(int k =0;k<precision;k++)
   								newBranchToLength/=10;
   							mt.setBranchLength(branchTo, newBranchToLength, false);
   							
   						}
   						else if(posFrom>posTo){
   							//MesquiteMessage.notifyUser("CAS 3 : posFrom > posTo");
   							double newBranchFromLength = toLength/2 + (posFrom - posTo);
   							for(int k =0;k<precision;k++) // round length
   								newBranchFromLength*=10;
   							if((newBranchFromLength*10)%10>5) newBranchFromLength = Math.floor(newBranchFromLength+1);
   							else newBranchFromLength = Math.floor(newBranchFromLength);
   							for(int k =0;k<precision;k++)
   								newBranchFromLength/=10;
   							mt.setBranchLength(branchFrom, newBranchFromLength, false);
   							mt.setBranchLength(newMother, toLength/2.0, false);
   							mt.setBranchLength(branchTo, toLength/2.0, false);
   							
   						}
   						else {
   							//MesquiteMessage.notifyUser("CAS 4 : posFrom < posTo");
   							double newMotherLength = (posFrom - posMotherTo)/2;
   							for(int k =0;k<precision;k++) // round length
   								newMotherLength*=10;
   							if((newMotherLength*10)%10>5) newMotherLength = Math.floor(newMotherLength+1);
   							else newMotherLength = Math.floor(newMotherLength);
   							for(int k =0;k<precision;k++)
   								newMotherLength/=10;
   							mt.setBranchLength(newMother, newMotherLength, false);
   							mt.setBranchLength(branchFrom, newMotherLength, false);
   							
   							double newBranchToLength = toLength - newMotherLength;
   							for(int k =0;k<precision;k++) // round length
   								newBranchToLength*=10;
   							if((newBranchToLength*10)%10>5) newBranchToLength = Math.floor(newBranchToLength+1);
   							else newBranchToLength = Math.floor(newBranchToLength);
   							for(int k =0;k<precision;k++)
   								newBranchToLength/=10;
   							mt.setBranchLength(branchTo, newBranchToLength, false);
   						}
   						
   						if(sister!=0)
   							mt.setBranchLength(sister, sisterLength, false);
   					}
   					else {
   						mt.setBranchLength(newMother, toLength/2.0, false);
   						mt.setBranchLength(branchTo, toLength/2.0, false);
   						mt.setBranchLength(branchFrom, toLength/2.0, false);
   					}
   				
   				}
   			}
   			/*
   			if (!mt.checkTreeIntegrity(mt.getRoot())) {
   				mt.setLocked(true);
   				//what to do here?  notify?
   			}*/
   			//mt.incrementVersion(MesquiteListener.BRANCHES_REARRANGED,true);
   			mt.interchangeBranches(oldBranchFrom,oldBranchTo,true);
   			mt.interchangeBranches(oldBranchTo,oldBranchFrom,true);
   			return null;
		}
		return null;
	}
	/*-----------------------------------------*/
	/** Returns total height from root to node*/
	private  double getHeightToRoot(Tree t,int node) { 
		if(node==t.getRoot() || !t.nodeExists(node)) return 0;
		else return t.getBranchLength(node,1)+getHeightToRoot(t,t.motherOfNode(node));
	}
}



