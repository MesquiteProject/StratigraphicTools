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
/**A class for an array of  continuous character states for many characters, at each of the taxa or nodes.*/
public class MScaleEmbedded  extends MScaleDistribution {
	
	public MScaleEmbedded (CharacterData data) {
		super(data.getTaxa());
		this.data = data;
	}
	/*..........................................MContinuousEmbedded................*/
	public Double2DArray getItem(int index){
		return ((ScaleData)data).getItem(index);
	}
	/*..........................................MContinuousEmbedded................*/
	public String getItemName(int index){
		return ((ScaleData)data).getItemName(index);
	}
	/*..........................................MContinuousEmbedded................*/
	public int getItemNumber(NameReference nr){
		return ((ScaleData)data).getItemNumber(nr);
	}
	/*..........................................MContinuousEmbedded................*/
	public  int getNumberOfItems() {
		return ((ScaleData)data).getNumItems();
	}
	/*..........................................MContinuousEmbedded................*/
	public  double getState (int ic, int N,  int item){
		return ((ScaleData)data).getState(ic, N, item);
	}
	/*..........................................MContinuousEmbedded................*/
	public  double getState (int ic, int N){
		return getState(ic, N, 0); 
	}
	/*..........................................MContinuousEmbedded................*/
	public CharacterDistribution getCharacterDistribution (int ic){
		return data.getCharacterDistribution(ic);
	}
	public int getNumTaxa(){
		return data.getNumTaxa();
	}
	public int getNumNodes(){
		return data.getNumTaxa();
	}
	public int getNumChars(){
		return data.getNumChars();
	}
	public String getName(){
		return data.getName();
	}
}
