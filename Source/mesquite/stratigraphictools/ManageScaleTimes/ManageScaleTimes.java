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
package mesquite.stratigraphictools.ManageScaleTimes;
/*~~  */

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stratigraphictools.lib.*;

/* ======================================================================== 
Manages continuous data matrices  */
public class ManageScaleTimes extends CharMatrixManager {
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
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
	public Class getDataClass(){
		return ScaleData.class;
	}
	/*.................................................................................................................*/
	public  String getDataClassName(){
		return "Scale Data";
	}
	/*.................................................................................................................*/
	public  CharacterData getNewData(Taxa taxa, int numChars){
		return new ScaleData(this, taxa.getNumTaxa(), numChars, taxa);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(Class dataClass){
		return (dataClass == ScaleData.class);
	}
	/*.................................................................................................................*/
	public boolean readsWritesDataType(String dataType){
		return dataType.equalsIgnoreCase("Scale");
	}
	/*.................................................................................................................*/
	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String dataType, String formatCommand, MesquiteInteger stringPos, int numChars) {
		ScaleData data= new ScaleData(this, taxa.getNumTaxa(), numChars, taxa);
		String tok = ParseUtil.getToken(formatCommand, stringPos);
		while (!tok.equals(";")) {
			if (tok.equalsIgnoreCase("ITEMS")) {
				tok = ParseUtil.getToken(formatCommand, stringPos); //getting rid of "="
				tok = ParseUtil.getToken(formatCommand, stringPos); //getting rid of "("
				tok = ParseUtil.getToken(formatCommand, stringPos); //getting first item name
				while (tok != null && !tok.equals(")")) {
					if ("unnamed".equalsIgnoreCase(tok))
						tok = null;
					data.establishItem(tok);
					tok = ParseUtil.getToken(formatCommand, stringPos); //getting item name
				}

			}
			else if (tok.equalsIgnoreCase("TRANSPOSE")) {
				alert("Sorry, Transposed matrices of scale times can't yet be read");
				return null;
			}
			else if (tok.equalsIgnoreCase("interleave")) {
				data.interleaved = true;
			}
			else if (tok.equalsIgnoreCase("MISSING")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setUnassignedSymbol(t.charAt(0));
			}
			else if (tok.equalsIgnoreCase("GAP")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setInapplicableSymbol(t.charAt(0));
			}
			else if (tok.equalsIgnoreCase("MATCHCHAR")) { 
				ParseUtil.getToken(formatCommand, stringPos); //eating up =
				String t = ParseUtil.getToken(formatCommand, stringPos);
				if (t!=null && t.length()==1)
					data.setMatchChar(t.charAt(0));
			}
			else {
				alert("Unrecognized token (\"" + tok+ "\") in FORMAT statement of scale matrix; matrix will be stored as foreign, and not processed.");
				return null;
			}
			tok = ParseUtil.getToken(formatCommand, stringPos);
		}

		return data;
	}
	/*.................................................................................................................*/
	public boolean processCommand(CharacterData data, String commandName, String commandString) {
		if ("CHARSTATELABELS".equalsIgnoreCase(commandName)){
			MesquiteInteger startCharT = new MesquiteInteger(0);
			String cN = ParseUtil.getToken(commandString, startCharT); //eating up command name
			cN = ParseUtil.getToken(commandString, startCharT);
			while (!StringUtil.blank(cN) && !cN.equals(";") ) {
				int charNumber =MesquiteInteger.fromString(cN);
				String charName = (ParseUtil.getToken(commandString, startCharT));
				if (!charName.equals(",")) {
					data.setCharacterName(charNumber-1, charName);
					String stateName = ParseUtil.getToken(commandString, startCharT); // eat up slash
					int stateNumber = 0;
					while (stateName!=null && !stateName.equals(",") &&!stateName.equals(";")) { //skipping state names as they don't make any sense for continuous!
						stateName = (ParseUtil.getToken(commandString, startCharT));
						stateNumber++;
					}
				}
				cN = ParseUtil.getToken(commandString, startCharT);
			}
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getCharStateLabels(CharacterData data) {
		String csl = "CHARSTATELABELS " + StringUtil.lineEnding();
		boolean found = false;
		for (int i = 0; i<data.getNumChars(); i++) {
			String cslC="";
			if (i>0 && found)
				cslC += "," + StringUtil.lineEnding();
			cslC += "\t\t" + Integer.toString(i+1) + " ";    //i+1 to convert to 1 based
			boolean foundInCharacter = false;
			if (data.characterHasName(i)) {
				foundInCharacter = true;
				cslC += StringUtil.tokenize(data.getCharacterName(i));
			}
			if (foundInCharacter) {
				csl += cslC;
				found = true;
			}
		}
		if (found)
			return csl + " ; " + StringUtil.lineEnding();
		else
			return "";
	}
	/*.................................................................................................................*/
	public void writeCharactersBlock(CharacterData data, CharactersBlock cB, MesquiteFile file, ProgressIndicator progIndicator){
		ScaleData cData = (ScaleData)data;
		StringBuffer blocks = new StringBuffer(cData.getNumChars()*cData.getNumTaxa()*10*cData.getNumItems());
		blocks.append("BEGIN SCALE;" + StringUtil.lineEnding());
		if (cData.getName()!=null &&  (getProject().getNumberCharMatrices()>1 || !NexusBlock.suppressTITLE)){
			blocks.append("\tTITLE  " + StringUtil.tokenize(cData.getName()) + ";" + StringUtil.lineEnding());
		}

		if (cData.getTaxa().getName()!=null  && getProject().getNumberTaxas()>1){ //should have an isUntitled method??
			blocks.append("\tLINK PERIOD = " +  StringUtil.tokenize(cData.getTaxa().getName()) + ";" + StringUtil.lineEnding());
		}
		if (data.getAnnotation()!=null) {
			blocks.append("[!" + data.getAnnotation() + "]");
			blocks.append(StringUtil.lineEnding());
		}
		blocks.append("\tDIMENSIONS NTIME=" + cData.getNumChars() + ";" + StringUtil.lineEnding());
		blocks.append("\tFORMAT");
		blocks.append(" DATATYPE = SCALE DATA");
		if (cData.getNumItems()>0 && !(cData.getNumItems()==1 && cData.getItemName(0) ==null)) {
			blocks.append(" ITEMS = (");
			for (int i=0; i<cData.getNumItems(); i++) {
				String iName = cData.getItemName(i);
				if (iName == null)
					blocks.append("unnamed ");
				else
					blocks.append(iName + " ");
			}
			blocks.append(") ");
		}
		blocks.append(";" + StringUtil.lineEnding());
		if (data.isLinked()){
			blocks.append("\tOPTIONS ");
			Vector ds = data.getDataLinkages();
			for (int i = 0; i<ds.size(); i++) {
				blocks.append(" LINKTIMES = ");
				blocks.append(StringUtil.tokenize(((CharacterData)ds.elementAt(i)).getName()));
			}
			blocks.append(";" + StringUtil.lineEnding());
		}
		blocks.append(getCharStateLabels(cData));
		blocks.append("\tMATRIX" + StringUtil.lineEnding());
		String taxonName="";
		int maxNameLength = cData.getTaxa().getLongestTaxonNameLength();
		for (int it=0; it<cData.getTaxa().getNumTaxa(); it++) {
			taxonName = cData.getTaxa().getTaxon(it).getName();
			if (taxonName!=null) {
				blocks.append("\t"+StringUtil.tokenize(taxonName));
				for (int i = 0; i<(maxNameLength-taxonName.length()+2); i++)
					blocks.append(" ");
			}

			for (int ic=0;  ic<cData.getNumChars(); ic++)  {
				blocks.append(' ');
				cData.statesIntoNEXUSStringBuffer(ic, it, blocks);
			}
			blocks.append(StringUtil.lineEnding());
		}
		blocks.append(StringUtil.lineEnding()+ ";" + StringUtil.lineEnding() + cB.getUnrecognizedCommands() + StringUtil.lineEnding());
		blocks.append("END;" + StringUtil.lineEnding());

		file.writeLine( blocks.toString());
	}


	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage Scale Times. matrices";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages scale data matrices (including read/write in NEXUS file)." ;
   	 }
 	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String dataType, String formatCommand, MesquiteInteger stringPos, int numChars, String title, String fileReadingArguments) {
 		return null;
 	}
}
	
	
