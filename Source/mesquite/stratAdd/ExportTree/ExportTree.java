
package mesquite.stratAdd.ExportTree;


import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import java.io.*;


/** ======================================================================== */


public class ExportTree extends TreeUtility {		
	MesquiteFile mesquiteFile;
	String location;
	CharacterData charaData;
	int[] charac;
	
	/*.................................................................................................................*/
	
	/* (non-Javadoc)
	 * @see mesquite.lib.MesquiteModule#startJob(java.lang.String, java.lang.Object, mesquite.lib.CommandRecord, boolean)
	 */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
	
	/*.................................................................................................................*/
 	
	/* (non-Javadoc)
	 * @see mesquite.lib.duties.TreeUtility#useTree(mesquite.lib.Tree, mesquite.lib.CommandRecord)
	 */
	public void useTree(Tree tree) {		
		
		location = mesquiteFile.saveFileAsDialog("Save file as...");	
		parametreExport();
		charac = chooseChara();
 		
		int drawnRoot = tree.getRoot();
		StringBuffer sb = new StringBuffer(100);
		sb.append("Taxon");
 		for( int i=0; i< charac.length; i++){
 		sb.append("\t" + charaData.getCharacterName(charac[i]) );}
 		sb.append("\n");
		composeText(tree, drawnRoot, charaData, sb);
		
		writeInFile(location, sb);
	}	
	
	/*.................................................................................................................*/
 		
	/* (non-Javadoc)
	 * @see mesquite.lib.MesquiteModule#isSubstantive()
	 */
	public boolean isSubstantive(){
		return false;
	}
	
	/*.................................................................................................................*/
    
	/* (non-Javadoc)
	 * @see mesquite.lib.Listable#getName()
	 */
	public String getName() {
		return "Export Character by Tree";
   	 }
	
	/*.................................................................................................................*/
 	
	/** returns an explanation of what the module does.*/
 	/* (non-Javadoc)
 	 * @see mesquite.lib.Explainable#getExplanation()
 	 */
 	public String getExplanation() {
 		return "Export one character of the tree in a text file";
   	 }   	 
 	/*.................................................................................................................*/
	
 	/**
 	 *  
 	 * Choose the Taxa and the matrice to use for the exportation
 	 *  
 	 * @param commandRec
 	 */
 	
 	public void parametreExport(){
 		Taxa theTaxa = getProject().chooseTaxa(containerOfModule(),"Choose Taxa", false);
 		charaData = getProject().chooseData(containerOfModule(),null,theTaxa,null,"Choose character matrice",false);		
 
 	} 	
 	/*.................................................................................................................*/
	
 	
 	/**
 	 * Return characters to export in an int[]
 	 * 
 	 * @return 
 	 */
 	public int[] chooseChara(){
 		int num = charaData.getNumChars();
 		String[] listeChar = new String[num];
 		
 		ListableVector vector = new ListableVector(num);
 		boolean[] selected = new boolean[num];
 		for (int i=0; i<num;i++) 
 		{
 			Listable lista = charaData.getCharacterDistribution(i);
 			vector.addElement(lista,false);
 			selected[i] = false; 		
 		}
 		
 		Listable[] result = ListDialog.queryListMultiple(containerOfModule(),"Select character","Check characters to export",
 				MesquiteString.helpString,vector ,selected);
 		 
 		int []charac = new int[result.length];
 		for(int i=0;i<result.length;i++)
 				charac[i] = findByName(result[i].getName()); 		
 		return charac;
 	}
 	

 	/*.................................................................................................................*/
 
 	/**
 	 * Return the number of a character from his name target
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public int findByName(String target){
		if (target == null)
			return -1;
		for (int i=0; i<charaData.getNumChars(); i++){
			String id = charaData.getCharacterName(i);
			if (id != null && id.equals(target))
				return i;
		}
		return -1;
	}
 	
 	/*.................................................................................................................*/
 
 	
	/**
	 * Create the file with "location" as a pathname and 
	 * write the content of the StringBuffer in it.
	 * 
	 * @param location
	 * @param stringBuffer
	 */
	public void writeInFile(String location, StringBuffer stringBuffer){
		
		File fichierEcriture=new File(location);
		try {
			FileWriter ecrivain =new FileWriter(fichierEcriture);
//			ecrivain.write("\n\r");	
			ecrivain.write(stringBuffer.toString());			
			ecrivain.flush();
			ecrivain.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 								
	}
	/*.................................................................................................................*/

 	/**
 	 * 
 	 * Reccursive composition of the text to be contained in the export File.
 	 * 
 	 * @param tree
 	 * @param node
 	 * @param charaData
 	 * @param sb
 	 * @param theChara
 	 */
 	public void composeText(Tree tree, int node, CharacterData charaData, StringBuffer sb) {
	
 		int numTaxon;
 		if (charaData == null)
			return;
 		
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			composeText(tree, d, charaData, sb);				
		if (!tree.nodeIsInternal(node))
		{	
			numTaxon = tree.taxonNumberOfNode(node);	
			sb.append(tree.getTaxa().getName(numTaxon));	
			
			for( int i=0; i< charac.length; i++)
			{
				sb.append("\t");
				sb.append(charaData.getCharacterState(charaData.makeCharacterState(),charac[i],numTaxon).toDisplayString());
			}	
				sb.append("\n"); 
				}
		} 	
}
