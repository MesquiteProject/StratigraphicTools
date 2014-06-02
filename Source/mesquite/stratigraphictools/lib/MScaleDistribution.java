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


import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**A class for an array of  continuous character states for many characters, at each of the taxa or nodes.*/
public abstract class MScaleDistribution  extends MScaleStates implements MCharactersDistribution {
	public MScaleDistribution (Taxa taxa) {
		super(taxa);
	}
	/*..........................................MContinuousDistribution................*/
	public abstract CharacterDistribution getCharacterDistribution (int ic);
	/*-----*/
	/*..........................................MContinuousDistribution................*/
	public abstract Double2DArray getItem(int index);
	/*..........................................MContinuousDistribution................*/
	/**return blank adjustable MContinuousDistribution if this same type */
	public MAdjustableDistribution makeBlankAdjustable(){
		MScaleAdjustable mca = new MScaleAdjustable(getTaxa(), getNumChars(), getNumNodes()); 
		mca.setItemsAs(this);
		return mca;
	}
	/*..........................................MContinuousDistribution................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		ScaleData data = new ScaleData(manager, taxa.getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this); 
		if (this instanceof Annotatable && ((Annotatable)this).getAnnotation()!=null)
			data.setAnnotation(((Annotatable)this).getAnnotation(), false);
		else if (getParentData()!=null && getParentData().getAnnotation()!=null)
			data.setAnnotation(getParentData().getAnnotation(), false);
		return data;
	}
	/*..........................................MContinuousDistribution................*/
	/** This readjust procedure can be called to readjust the size of storage of
	states of a character for nodes. */
	public MCharactersHistory adjustHistorySize(Tree tree, MCharactersHistory charStates) {
		int numNodes = tree.getNumNodeSpaces();
		MCharactersHistory soc =charStates;
		if (charStates==null || ! (charStates instanceof MScaleHistory)) 
			soc = new MScaleHistory(tree.getTaxa(), getNumChars(), numNodes);
		else if (numNodes!= charStates.getNumNodes() || charStates.getNumChars()!= getNumChars()) 
			soc = new MScaleHistory(tree.getTaxa(), getNumChars(), numNodes);
		else {
			soc =charStates;
		}
		((MScaleHistory)soc).setItemsAs(this);
		soc.setParentData(getParentData());
		return soc;
	}
}
