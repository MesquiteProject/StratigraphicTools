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
package mesquite.stratigraphictools.SetMinBranchLength;

import mesquite.lib.AdjustableTree;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Notification;
import mesquite.lib.duties.BranchLengthsAltererMult;


public class SetMinBranchLength extends BranchLengthsAltererMult  {
	double resultNum;
	double length = 0;
	double intLength = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		length = MesquiteDouble.queryDouble(containerOfModule(), "Set minimal terminal branch length", "Set minimal length for terminal branches to", 1.0);
		intLength = MesquiteDouble.queryDouble(containerOfModule(), "Set minimal internal branch length", "Set minimal length for internal branches to", 1.0);
		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (MesquiteDouble.isCombinable(length) && MesquiteDouble.isCombinable(intLength) && tree instanceof MesquiteTree){
   				
			MesquiteTree mt = (MesquiteTree)tree;
			//((STTree)tree).setMinBranchLengths(length,intLength);

			int[] test=getTermBranchSortedByLevel(tree);
			setMinLengths(tree,test,length,intLength);
			
			
			int[] termTaxa= tree.getTerminalTaxa(tree.getRoot());
			//mt.incrementVersion(BRANCHLENGTHS_CHANGED, false);
			mt.interchangeBranches(termTaxa[0],termTaxa[termTaxa.length-1],true);
   			mt.interchangeBranches(termTaxa[termTaxa.length-1],termTaxa[0],true);
			
			
			if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
			return true;
			}
			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Assign minimum Branch Lengths";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Assign minimum Branch Lengths...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Assigns a minimum value for branch length for all of a tree's branches." ;
   	 }
 	/*-----------------------------------------*/
	/** Returns an array of terminal branch node numbers sorted by level*/
	private int[] getTermBranchSortedByLevel(AdjustableTree t){
		int[] termBranches = getTermBranch(t,t.getRoot());
		int[] termBranchLevel = new int[termBranches.length];
		int[] termBranchSorted = new int[termBranches.length];
		int level=0, temp=0;
		for(int i=0;i<termBranches.length;i++){
			termBranchLevel[i]=getBranchLevel(t,termBranches[i],0);
		}
		for(int j=termBranches.length-1;j>=0;j--){
			level=0;
			for(int k=0;k<termBranches.length;k++){
				if(level<termBranchLevel[k]) {
					level=termBranchLevel[k];
					temp=k;
				}
				else if(level==termBranchLevel[k] && t.getBranchLength(termBranches[k],1)>t.getBranchLength(temp,1)){
					level=termBranchLevel[k];
					temp=k;
				}
			}
			termBranchSorted[j]=termBranches[temp];
			termBranchLevel[temp]=-1;
		}
		return termBranchSorted;
	}
	/*-----------------------------------------*/
	/** Returns an array of terminal branch node numbers*/
	private int[] getTermBranch(AdjustableTree t,int node){
		int[] termBranchNode = new int[t.numberOfTerminalsInClade(node)];
		intI=0;
		findTermNodes(t,node,termBranchNode);
		return termBranchNode;
	}
	private int intI=0;
	private void findTermNodes(AdjustableTree t,int node,int[] termNodes){
		for(int daughter=t.firstDaughterOfNode(node);t.nodeExists(daughter);daughter=t.nextSisterOfNode(daughter)){
			if(!t.nodeExists(t.firstDaughterOfNode(daughter))){
				termNodes[intI]=daughter;
				intI++;
			}
			findTermNodes(t,daughter,termNodes);
		}
	}
	/*-----------------------------------------*/
	/** Returns the number of nodes between one branch and the root*/
	private int getBranchLevel(AdjustableTree t,int node,int level){
		if(node==t.getRoot() || !t.nodeExists(t.motherOfNode(node))) return level;
		else {
			level++;
			return getBranchLevel(t,t.motherOfNode(node),level);
		}
	}
	/*-----------------------------------------*/
	/** Sets the minimal branch length of terminal and internal nodes (stored as a double internally).*/
	private void setMinLengths(AdjustableTree t,int[] node, double termLength, double intLength) {
		double shift =0;
		double newLength = 0;
		
		for (int i = 0; i<node.length; i++){
			//TODO
			if(t.getBranchLength(node[i],1)!=termLength){
				int smallestInternalSister = -1;
				int smallestTerminalSister=node[i];
				for(int j=t.firstDaughterOfNode(t.motherOfNode(node[i]));t.nodeExists(j);j=t.nextSisterOfNode(j)){
					if(t.nodeExists(t.firstDaughterOfNode(j)) && (t.getBranchLength(j,1)<t.getBranchLength(smallestInternalSister,1) || smallestInternalSister == -1))
						smallestInternalSister = j;
					if(!t.nodeExists(t.firstDaughterOfNode(j)) && t.getBranchLength(j,1)<t.getBranchLength(smallestTerminalSister,1)) 
						smallestTerminalSister=j;
				}
				if(smallestInternalSister == -1 || termLength-t.getBranchLength(smallestTerminalSister,1)>0)
					shift =termLength-t.getBranchLength(smallestTerminalSister,1);
				else if((termLength-t.getBranchLength(smallestTerminalSister,1))<0 && (termLength-t.getBranchLength(smallestTerminalSister,1))<(intLength-t.getBranchLength(smallestInternalSister,1)))
					shift =intLength-t.getBranchLength(smallestInternalSister,1);
				for(int j=t.firstDaughterOfNode(t.motherOfNode(node[i]));t.nodeExists(j);j=t.nextSisterOfNode(j)){	
					newLength = t.getBranchLength(j,1)+shift;
					t.setBranchLength(j,newLength,false);
				}
				if(t.motherOfNode(node[i])!=t.getRoot()) {
					newLength = t.getBranchLength(t.motherOfNode(node[i]),1)-shift;
					t.setBranchLength(t.motherOfNode(node[i]),newLength,false);
				}
			}
			int intNode = t.motherOfNode(node[i]);
			while(intNode!=t.getRoot()){
				if(t.getBranchLength(intNode,1)!=intLength){
					int smallestTerminalSister=-1;
					int smallestInternalSister=intNode;
					for(int j=t.firstDaughterOfNode(t.motherOfNode(intNode));t.nodeExists(j);j=t.nextSisterOfNode(j)){
						if(!t.nodeExists(t.firstDaughterOfNode(j)) && (t.getBranchLength(j,1)<t.getBranchLength(smallestTerminalSister,1) || smallestTerminalSister == -1))
							smallestTerminalSister=j;
						if(t.nodeExists(t.firstDaughterOfNode(j)) && t.getBranchLength(j,1)<t.getBranchLength(smallestInternalSister,1)) 
							smallestInternalSister=j;
					}
					if(smallestTerminalSister==-1 || intLength-t.getBranchLength(smallestInternalSister,1)>0)
						shift =intLength-t.getBranchLength(smallestInternalSister,1);
					else if((intLength-t.getBranchLength(smallestInternalSister,1))<0 && (termLength-t.getBranchLength(smallestTerminalSister,1))>(intLength-t.getBranchLength(smallestInternalSister,1)))
						shift =termLength-t.getBranchLength(smallestTerminalSister,1);
					for(int j=t.firstDaughterOfNode(t.motherOfNode(intNode));t.nodeExists(j);j=t.nextSisterOfNode(j)){
						newLength = t.getBranchLength(j,1)+shift;
						t.setBranchLength(j,newLength,false);
					}
					if(t.motherOfNode(intNode)!=t.getRoot()) {
						newLength = t.getBranchLength(t.motherOfNode(intNode),1)-shift;
						t.setBranchLength(t.motherOfNode(intNode),newLength,false);
					}
				}
				intNode = t.motherOfNode(intNode);
			}			
			
		}
	}
	/*-----------------------------------------*/
	/** Returns total height from root to node*/
	public  double getHeightToRoot(AdjustableTree t,int node) { 
		if(node==t.getRoot() || !t.nodeExists(node)) return 0;
		else return t.getBranchLength(node,1)+getHeightToRoot(t,t.motherOfNode(node));
	}
}

