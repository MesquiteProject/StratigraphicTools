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
package mesquite.stratigraphictools.lib;


import mesquite.lib.*;
import mesquite.lib.characters.*;



/* ======================================================================== */
/**A class for an array of  continuous character states for many characters, at each of the nodes.*/
public class MScaleHistory  extends MScaleAdjustable implements MCharactersHistory {
	
	public MScaleHistory (Taxa taxa, int numChars, int numNodes) {
		super(taxa, numChars, numNodes);
	}
	
	public MScaleHistory (Taxa taxa) {
		super(taxa);
	}
	
	/*..........................................MContinuousHistory................*/
	/** extract the states of character ic and return as CharacterHistory*/
	public CharacterHistory getCharacterHistory (int ic){
		ScaleHistory soc = new ScaleHistory(getTaxa(), getNumNodes(), (ScaleData)getParentData());
		soc.setItemsAs(this);
		for (int it = 0; it<getNumNodes(); it++) {
			for (int item=0; item<getNumItems(); item++)
				soc.setState(it, item, getState(ic, it, item)); 
		}
		return soc;
	}
	/*..........................................MContinuousHistory................*/
	/** obtain the states of character ic from the given CharacterDistribution*/
	public void transferFrom(int ic, CharacterHistory s) { 
		if (s instanceof ScaleHistory) {
			setItemsAs(((ScaleHistory)s));
			for (int j=0; j<getNumNodes(); j++)
				for (int item=0; item<((ScaleHistory)s).getNumItems(); item++) {
					if (getItem(item)!=null)
						getItem(item).setValue(ic, j,  ((ScaleHistory)s).getState(j, item));
				}
		}
	}
	double minState = MesquiteDouble.unassigned;
	double maxState = MesquiteDouble.unassigned;
	/*..........................................MContinuousHistory................*/
	private void calcMinMaxStates(Tree tree, int node, int item) {
		for (int ic=0; ic<getNumChars(); ic++){
			double s=getState(ic, node, item); 
			minState = MesquiteDouble.minimum(s, minState);
			maxState = MesquiteDouble.maximum(s, maxState);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				calcMinMaxStates(tree, d, item);
	}
	public void getMinMax(Tree tree, int root, int item, MesquiteDouble min, MesquiteDouble max){
		if (min == null || max==null)
			return;
		minState = MesquiteDouble.unassigned;
		maxState = MesquiteDouble.unassigned;
		calcMinMaxStates(tree, root, item);
		min.setValue(minState);
		max.setValue(maxState);
	}
}
