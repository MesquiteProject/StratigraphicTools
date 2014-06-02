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
package mesquite.stratigraphictools.RoundBranchLength;

import mesquite.lib.AdjustableTree;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Notification;
import mesquite.lib.duties.BranchLengthsAltererMult;
import mesquite.lib.MesquiteMessage;


public class RoundBranchLength extends BranchLengthsAltererMult  {
	double resultNum;
	int precision;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		precision = MesquiteInteger.queryInteger(containerOfModule(), "Round branch lengths", "Set precision (how many digit behind the coma)", 1);
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
  	 		if (MesquiteInteger.isCombinable(precision) && tree instanceof MesquiteTree){
   				roundAllBranchLengths(tree, precision,true);
				if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				return true;
			}
			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Round off Branch Lengths";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Round off Branch Lengths...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Assigns a round value for branch length for all of a tree's branches." ;
   	 }
 	/*-----------------------------------------*/
	/** Sets the branch lengths of nodes to a round value.*/
	public  void roundAllBranchLengths(AdjustableTree t,int precision,boolean notify) { 
		double length = 0, oldLength = 0;
		int number = 0;
		String message = "";
		for (int i=0; i<t.getNumNodeSpaces(); i++) {
			length = t.getBranchLength(i,1);
			oldLength = length;
			
			for(int k =0;k<precision;k++)
				length*=10;
			
			if((length*10)%10>5) length = Math.floor(length+1);
			else length = Math.floor(length);
			
			for(int k =0;k<precision;k++)
				length/=10;
			
			t.setBranchLength(i,length,false);
			if(oldLength!=length){ 
				message+="node "+i+": "+oldLength+" => "+length+"\n";
				number++;
			}
		}
		if(number>1) message = number+" branches rounded:\n"+message;
		else message = number+" branch rounded:\n"+message;
		if(notify)
			MesquiteMessage.notifyUser(message);
	}
	
}

