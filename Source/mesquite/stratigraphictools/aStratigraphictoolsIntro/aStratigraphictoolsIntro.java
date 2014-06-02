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
package mesquite.stratigraphictools.aStratigraphictoolsIntro;
/*~~  */

import mesquite.lib.duties.*;

/*
To do:
o If only single tree window, these modifying tree sources should not be available, since they will recursively look internally

======== Random adjustments of current tree ========
++ Randomly rearrange tree (makes n randomly chosen branch moves)

++ Augment tree randomly

++ Rarefy tree randomly

++ Random resolutions of polytomy

++ Add noise to branch lengths (uniform normal; binary noise; biased normal, e.g. increase variance deeper)

-- Reshuffling terminals

======== Determinate adjustments ========

++ All rerootings

++ Rearrangements (using tree search rearrangments)

-- Adjust branch lengths of trees (uses branch lengths adjusters available)

-- Partition tree depending on taxa partition

======== Simulations of evolution/ Fully random trees ========
++ All dichotomous trees

++ Equiprobable speciation (pure birth process -- conditioned on what?  terminal branch lengths?)

++ Equiprobable trees

-- Constant birth/death process

-- Randomly varying birth/death parameters

-- b/d depends of simulation of evolution of character affecting diversification rates

-- b/d depends on age of lineage

*/
/* ======================================================================== */
public class aStratigraphictoolsIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aStratigraphictoolsIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Serves as an introduction to the Stratigraphic tools package for Mesquite.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Stratigraphic tools Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Stratigraphic tools";
 	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "S. Josse, T. Moreau and M.Laurin, July 2005.  Stratigraphic tools package for Mesquite, version 1.0.";
 	}
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "1.0c"; //released for compatibility with Mesquite 2.75
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "S. Josse, T. Moreau and M.Laurin.";
   	 }
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		return "http://mesquiteproject.org/packages/stratigraphictools/notices.xml";  
	}
}

