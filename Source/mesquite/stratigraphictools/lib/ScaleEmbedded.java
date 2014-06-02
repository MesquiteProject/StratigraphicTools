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
//import mesquite.cont.lib.*;

/* ======================================================================== */
/** Contains an array of  continuous character states for one character, at each of the taxa or nodes */
public class ScaleEmbedded  extends ScaleDistribution {
	ScaleData data;
	public ScaleEmbedded (ScaleData data, int ic) {
		super(data.getTaxa());
		this.data = data;
		this.characterNumber = ic;
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return data.getStateClass();
	}
	/*..........................................ContinuousEmbedded................*/
	public CharacterData getParentData() {
		return data;
	}
	/*..........................................ContinuousEmbedded................*/
	public int getNumItems() {
		return data.getNumItems();
	}
	/*..........................................ContinuousEmbedded................*/
	public NameReference getItemReference(String name){
		return data.getItemReference(name);
		
	}
	/*..........................................ContinuousEmbedded................*/
	public NameReference getItemReference(int index){
		return data.getItemReference(index);
	}
	/*..........................................ContinuousEmbedded................*/
	public int getItemNumber(NameReference nr){
		return data.getItemNumber(nr);
	}
	/*..........................................ContinuousEmbedded................*/
	public String getItemName(int index){
			return data.getItemName(index);
	}
	/*..........................................ContinuousEmbedded................*/
	public int getNumTaxa() {
		return data.getNumTaxa();
	}
	/*..........................................ContinuousEmbedded................*/
	public double getState (int N, int item) {
		return data.getState(characterNumber, N, item);
	}
	/*..........................................ContinuousEmbedded................*/
	public double getState (int N) {
		return data.getState(characterNumber, N, 0);
	}
	public String getName(){
		return data.getCharacterName(characterNumber);
	}
	public boolean isUncertain(int N){
		return false;
	}
}
