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
package mesquite.stratigraphictools.FitTermBranchToScale;

import mesquite.lib.AdjustableTree;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Notification;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.BranchLengthsAltererMult;
import mesquite.lib.duties.DrawTree;
import mesquite.lib.duties.DrawTreeCoordinator;
import mesquite.stratigraphictools.NodeLocsPaleo.*;


public class FitTermBranchToScale extends BranchLengthsAltererMult  {
	double resultNum;
	double intLength = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
		NodeLocsPaleo nodeLocs = (NodeLocsPaleo)this.getEmployer().findEmployeeWithDuty(DrawTreeCoordinator.class).findEmployeeWithDuty(DrawTree.class).findEmployeeWithName("Node Locations (Paleo)");
		if (MesquiteDouble.isCombinable(intLength) && nodeLocs!=null && tree instanceof MesquiteTree){
   			
			MesquiteTree mt = (MesquiteTree)tree;
			int[] termBranch=getTermBranchSortedByLevel(tree);
			setMinLengths(tree,termBranch,nodeLocs.getScaleData(),intLength);	
			
			int[] termTaxa= tree.getTerminalTaxa(tree.getRoot());
			//mt.incrementVersion(BRANCHLENGTHS_CHANGED, false);
			mt.scaleAllBranchLengths(1, false);
			//mt.interchangeBranches(termTaxa[0],termTaxa[termTaxa.length-1],true);
   			//mt.interchangeBranches(termTaxa[termTaxa.length-1],termTaxa[0],true);
   			
			if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
			return true;
			}
			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Assign Terminal Branch Lengths To Fit The Scale";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Assign Terminal Branch Lengths To Fit The Scale...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Assigns a minimum value for internal branch length and fit the length of terminal branches to the scale." ;
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
	private void setMinLengths(AdjustableTree t,int[] node, CharacterData data, double intLength) {
		//Initialisation
		double shift = 0;
		double[] scaleHeightTop = new double[t.getNumNodeSpaces()];
		double[] scaleHeightBase = new double[t.getNumNodeSpaces()];
		double newLength = 0;
		double smallestInternalSister = 0;
		CharacterState cs = null;
		int smallestTermSister=-1;
		int topNode = t.getRoot();
		int it = 0;
		for(int i = 0; i<node.length; i++){
			if(getHeightToRoot(t,node[i])>getHeightToRoot(t,topNode)){
				topNode = node[i];
			}
		}
		double depthScale = getHeightToRoot(t,topNode);
		int itMin=0;
		double min=0;
		for(int i = 0;i<node.length;i++){
			itMin=0;
			scaleHeightTop[node[i]]=depthScale-(new Double(data.getCharacterState(cs,1,0).toString()).doubleValue());
			for(it=0; it<data.getNumTaxa(); it++) {
				min = depthScale-(new Double(data.getCharacterState(cs,1,it).toString()).doubleValue());
				if(Math.abs(min-getHeightToRoot(t,node[i]))<=Math.abs(scaleHeightTop[node[i]]-getHeightToRoot(t,node[i]))){ 
					scaleHeightTop[node[i]]=min;
					itMin=it;
				}
			}
			scaleHeightBase[node[i]] = scaleHeightTop[node[i]]-(new Double(data.getCharacterState(cs,0,itMin).toString()).doubleValue());
		}
		boolean termNode = true, smallestTerminalSister = true ,smallestInternalSisterInit=false;
		//Place terminal branches to the correct position according to the scale
		for(int i=0;i<node.length;i++){
			smallestTermSister=-1;
			for(int k=t.firstDaughterOfNode(t.motherOfNode(node[i]));t.nodeExists(k);k=t.nextSisterOfNode(k)){
				if(!t.nodeExists(t.firstDaughterOfNode(k))){
					if(smallestTermSister==-1)
						smallestTermSister=k;
					else if(t.getBranchLength(k,1)<t.getBranchLength(smallestTermSister,1))
						smallestTermSister=k;
				}
			}
			shift = scaleHeightTop[node[i]]-getHeightToRoot(t,node[i]);
			
			t.setBranchLength(node[i],t.getBranchLength(node[i],1)+shift,false);
			if(smallestTermSister==node[i] || shift<=0){
				shift = scaleHeightBase[node[i]]-getHeightToRoot(t,t.motherOfNode(node[i]));
				if(t.motherOfNode(node[i])!=t.getRoot())
					t.setBranchLength(t.motherOfNode(node[i]),t.getBranchLength(t.motherOfNode(node[i]),1)+shift,false);
				for(int k = t.firstDaughterOfNode(t.motherOfNode(node[i]));t.nodeExists(k);k=t.nextSisterOfNode(k)){
					t.setBranchLength(k,t.getBranchLength(k,1)-shift,false);
				}
			}
		}
		//Adjust null and negative terminal branches
		for(int i=0;i<node.length;i++){
			if(t.getBranchLength(node[i],1)<0.01){
				newLength = scaleHeightTop[node[i]]-scaleHeightBase[node[i]];
				shift = scaleHeightBase[node[i]]-getHeightToRoot(t,t.motherOfNode(node[i]));
				t.setBranchLength(node[i],newLength,false);
				if(t.motherOfNode(node[i])!=t.getRoot())
					t.setBranchLength(t.motherOfNode(node[i]),t.getBranchLength(t.motherOfNode(node[i]),1)+shift,false);
				for(int k = t.firstDaughterOfNode(t.motherOfNode(node[i]));t.nodeExists(k);k=t.nextSisterOfNode(k)){
					if(k!=node[i]) t.setBranchLength(k,t.getBranchLength(k,1)-shift,false);
				}
			}
			//else System.out.print(node[i]+"\t");
		}
		//Adjust root's terminal daughters
		for(int j=t.firstDaughterOfNode(t.getRoot());t.nodeExists(j);j=t.nextSisterOfNode(j)){
			if(!t.nodeExists(t.firstDaughterOfNode(j))){
				shift = scaleHeightTop[j]-getHeightToRoot(t,j);
				t.setBranchLength(j,t.getBranchLength(j,1)+shift,false);
			}
		}
		//Adjust internal and external branches to fit the requirement of the minimum length for internal branches
		for (int i = 0; i<node.length; i++){
			int intNode = t.motherOfNode(node[i]);
			smallestTermSister=-1;
			double temp=0;
			while(intNode!=t.getRoot()){
				smallestTermSister=-1;
				shift =intLength-t.getBranchLength(intNode,1);
				temp=0;
				for(int j=t.firstDaughterOfNode(t.motherOfNode(intNode));t.nodeExists(j);j=t.nextSisterOfNode(j)){
					if(!t.nodeExists(t.firstDaughterOfNode(j))){
						if(smallestTermSister==-1)
							smallestTermSister=j;
						else if(t.getBranchLength(j)<t.getBranchLength(smallestTermSister))
							smallestTermSister=j;
						temp=scaleHeightTop[j]-scaleHeightBase[j]-t.getBranchLength(j,1);
					}else if(t.getBranchLength(j,1)<t.getBranchLength(intNode,1))
							shift =intLength-t.getBranchLength(j,1);	
				}
				if(smallestTermSister==-1){
					for(int j=t.firstDaughterOfNode(t.motherOfNode(intNode));t.nodeExists(j);j=t.nextSisterOfNode(j)){
						newLength = t.getBranchLength(j,1)+shift;
						t.setBranchLength(j,newLength,false);
					}
					if(t.motherOfNode(intNode)!=t.getRoot()) {
						newLength = t.getBranchLength(t.motherOfNode(intNode),1)-shift;
						t.setBranchLength(t.motherOfNode(intNode),newLength,false);
					}
				} else if(t.getBranchLength(smallestTermSister,1)-(scaleHeightTop[smallestTermSister]-scaleHeightBase[smallestTermSister])+shift>=0){
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
		//Checks null and negative terminal branches and warns user
		String message="";
		int temp=0;
		for(int i=0;i<node.length;i++){
			if(t.getBranchLength(node[i],1)<0.01){
				if(t.nodeExists(t.firstDaughterOfNode(node[i]))){
					temp=t.firstDaughterOfNode(node[i]);
					while(t.nodeExists(t.firstDaughterOfNode(temp))) 
						temp=t.firstDaughterOfNode(temp);
					message+="("+t.getTaxa().getTaxonName(t.taxonNumberOfNode(temp))+",";
					temp=t.lastDaughterOfNode(node[i]);
					while(t.nodeExists(t.lastDaughterOfNode(temp))) 
						temp=t.lastDaughterOfNode(temp);
					message+=t.getTaxa().getTaxonName(t.taxonNumberOfNode(temp))+")\n";
				}
				else message+=t.getTaxa().getTaxonName(t.taxonNumberOfNode(node[i]))+"\n";
			}
		}
		if(!message.equals("")){
			message="Those branches haven't been modified correctly (negative or null lengths):\n\n"+message;
			MesquiteMessage.notifyUser(message);
		}
	}
	/*-----------------------------------------*/
	/** Returns total height from root to node*/
	public  double getHeightToRoot(AdjustableTree t,int node) { 
		if(node==t.getRoot() || !t.nodeExists(node)) return 0;
		else return t.getBranchLength(node,1)+getHeightToRoot(t,t.motherOfNode(node));
	}

}

