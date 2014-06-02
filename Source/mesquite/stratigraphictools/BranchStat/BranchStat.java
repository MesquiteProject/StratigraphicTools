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
package mesquite.stratigraphictools.BranchStat;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import mesquite.stratigraphictools.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stratigraphictools.NodeLocsPaleo.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
public class BranchStat extends TreeDisplayAssistantI {
	public Vector extras;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		StatToolExtra newPj = new StatToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Branch Statistics";
	}
	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool that shows statistics about branches.";
	}
}

/* ======================================================================== */
class StatToolExtra extends TreeDisplayExtra implements Commandable, ActionListener  {
	TreeTool statTool;
	BranchStat statModule;
	Tree tree;
	MesquiteInteger pos = new MesquiteInteger();
	String message="";
	
	public StatToolExtra (BranchStat ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		statModule = ownerModule;
		statTool = new TreeTool(this, "BranchStat", ownerModule.getPath(), "branchStat.gif", 5,2,"Branch Stat", "This tool is used to show statistics about a clade.");
		statTool.setTouchedCommand(MesquiteModule.makeCommand("query",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(statTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		
		if (checker.compare(this.getClass(), "Shows message with statistics about the branch", "[branch number]", commandName, "query")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
				NodeLocsPaleo nodeLocs = (NodeLocsPaleo)ownerModule.getEmployer().findEmployeeWithDuty(DrawTreeCoordinator.class).findEmployeeWithDuty(DrawTree.class).findEmployeeWithName("Node Locations (Paleo)");
				CharacterData scaleData;
				CharacterState cs=null;
				
				double tolerance=0.1;//set the level of tolerance in case of not correctly rounded value
				
				boolean count = false, commonAnc=true;
				int[] nodesInClade = getBranch(tree,branchFound);
				int[] termNodesInClade = getTermBranch(tree,branchFound);
				int[] termNodes = getTermBranch(tree,tree.getRoot());
				int[] termNodesInPeriod = new int[tree.numberOfTerminalsInClade(branchFound)];
				int[] ghostTermNodesInPeriod = new int[tree.numberOfTerminalsInClade(branchFound)];
				int[] ghostIntNodesInPeriod = new int[tree.numberOfTerminalsInClade(branchFound)];
				int[] crown = new int[tree.numberOfTerminalsInClade(branchFound)];
				int itnip=0, igtnip=0, iginip=0, ic=0;
				int commonAncestor = branchFound;
				int topNodeInClade = branchFound;
				int topNode = branchFound;
		
				for(int i=0;i<termNodesInClade.length;i++)
					if(getHeightToRoot(tree,termNodesInClade[i])>getHeightToRoot(tree,topNodeInClade))
						topNodeInClade = termNodesInClade[i];
				for(int i = 0; i<termNodes.length; i++)
					if(getHeightToRoot(tree,termNodes[i])>getHeightToRoot(tree,topNode))
						topNode = termNodes[i];
					
				for(int i=0;i<termNodesInClade.length;i++){
					if(Math.abs(getHeightToRoot(tree,topNodeInClade)-getHeightToRoot(tree,termNodesInClade[i]))<tolerance){
						crown[ic]=termNodesInClade[i];
						ic++;
					}
				}
				
				commonAncestor=crown[0];
				commonAnc=false;
				while(!commonAnc && tree.nodeExists(tree.motherOfNode(commonAncestor))){
					commonAnc=true;
					for(int i=0;i<ic;i++)
						if(!tree.descendantOf(crown[i],commonAncestor))
							commonAnc=false;
					if(!commonAnc)
						commonAncestor=tree.motherOfNode(commonAncestor);
				}
				
				int tempNode= branchFound;
				int precision=5, coeff=1;
				for(int i=0;i<precision;i++)
					coeff*=10;
				int bioDiv1=0, bioDiv2=0, bioDiv3=0, bioDiv4=0, bioDiv5=0, BioDivTemp=0;
				
				double bd=0, pdi=0;
				double periodTop=0, periodBase=0, branchTop=0, branchBase=0;
				double depthScale = getHeightToRoot(tree,topNode);
				
				String temp="", periods="", infoByPeriod="";
				String numberOfSpeciesInClade="", phyloDivIndex="";
				
				String clade="Clade: ";
				tempNode= branchFound;
				while(tree.nodeExists(tree.firstDaughterOfNode(tempNode)))
					tempNode= tree.firstDaughterOfNode(tempNode);
				clade+="("+tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(tempNode))+",";
				tempNode= branchFound;
				while(tree.nodeExists(tree.lastDaughterOfNode(tempNode)))
					tempNode= tree.lastDaughterOfNode(tempNode);
				clade+=tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(tempNode))+")";
				
				
				
				if(nodeLocs!=null)
					if(nodeLocs.getScaleData()!=null){
						scaleData = nodeLocs.getScaleData();
						
						numberOfSpeciesInClade="Biodiversity (BD) including all terminal taxa in the clade: "+tree.numberOfTerminalsInClade(branchFound);
						
						phyloDivIndex="Phylogenetic Diversity Index (PDI) including all branch lengths in the clade: ";
						bd=0;
						for(int i=1;i<nodesInClade.length;i++)
							bd+=tree.getBranchLength(nodesInClade[i],1);
						bd*=coeff;
						if(bd%1<0.5){
							bd=Math.floor(bd);
							bd/=coeff;
						}
						else {
							bd=Math.floor(bd);
							bd++;
							bd/=coeff;
						}
						phyloDivIndex+=""+bd;
						
						periods="{Time interval covered by clade}\n";
						int beginPeriod=0, endPeriod=0, endCrown=0;
						for(int i=0;i<scaleData.getNumTaxa();i++){
							periodTop=(depthScale-(new Double(scaleData.getCharacterState(cs,1,i).toString()).doubleValue()));
							periodBase=(depthScale-(new Double(scaleData.getCharacterState(cs,2,i).toString()).doubleValue()));
							if(periodTop>getHeightToRoot(tree,branchFound)-tolerance && Math.abs(periodTop-getHeightToRoot(tree,topNodeInClade))<tolerance)
								beginPeriod=i;
							if(periodTop-tolerance>getHeightToRoot(tree,commonAncestor) && periodBase-tolerance<getHeightToRoot(tree,commonAncestor))
								endCrown=i;
							if(periodTop-tolerance>getHeightToRoot(tree,branchFound) && periodBase-tolerance<=getHeightToRoot(tree,branchFound)){
								endPeriod=i;
							}
						}
						periods+=scaleData.getTaxa().getTaxonName(beginPeriod)+" - "+scaleData.getTaxa().getTaxonName(endPeriod)+" (total).";
						if(beginPeriod==0)
							periods+=" "+scaleData.getTaxa().getTaxonName(beginPeriod)+" - "+scaleData.getTaxa().getTaxonName(endCrown)+" (crown).";
						
						infoByPeriod="";
						BioDivTemp=0;
						for(int i=scaleData.getNumTaxa()-1;i>=0;i--){
							temp=""; pdi=0;bioDiv2=0;bioDiv3=0;bioDiv4=0;bioDiv5=0;bioDiv1=0;
							termNodesInPeriod = new int[tree.numberOfTerminalsInClade(branchFound)];
							ghostTermNodesInPeriod = new int[tree.numberOfTerminalsInClade(branchFound)];
							itnip=0; igtnip=0; iginip=0;
							periodTop=(depthScale-(new Double(scaleData.getCharacterState(cs,1,i).toString()).doubleValue())); 
							periodBase=(depthScale-(new Double(scaleData.getCharacterState(cs,2,i).toString()).doubleValue())); 
							branchTop=getHeightToRoot(tree,branchFound); 
							branchBase=getHeightToRoot(tree,branchFound)-tree.getBranchLength(branchFound,1);
							if(periodTop-tolerance>branchTop && periodTop<=getHeightToRoot(tree,topNodeInClade)+tolerance){
								for(int j=1;j<nodesInClade.length;j++){
									branchTop=getHeightToRoot(tree,nodesInClade[j]); 
									branchBase=getHeightToRoot(tree,nodesInClade[j])-tree.getBranchLength(nodesInClade[j],1);
									if(periodTop-tolerance>branchBase && periodBase<branchTop){
										if(!tree.nodeExists(tree.firstDaughterOfNode(nodesInClade[j]))){
											if(Math.abs(branchTop-periodTop)<tolerance){
												temp+=tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(nodesInClade[j]))+", ";
												bioDiv2++;
												termNodesInPeriod[itnip]=nodesInClade[j];
												itnip++;
											}
											else {
												temp+="("+tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(nodesInClade[j]))+"), ";
												bioDiv3++;
												ghostTermNodesInPeriod[igtnip]=nodesInClade[j];
												igtnip++;
											}
										}
										else {
											if(periodTop-tolerance<=branchTop){
												bioDiv1++;
												ghostIntNodesInPeriod[iginip]=nodesInClade[j];
												iginip++;
											}
											else temp+="ANC";
											tempNode= nodesInClade[j];
											while(tree.nodeExists(tree.firstDaughterOfNode(tempNode)))
												tempNode= tree.firstDaughterOfNode(tempNode);
											temp+="("+tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(tempNode))+",";
											tempNode= nodesInClade[j];
											while(tree.nodeExists(tree.lastDaughterOfNode(tempNode)))
												tempNode= tree.lastDaughterOfNode(tempNode);
											temp+=tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(tempNode))+"), ";
										}
									}
								}
								bioDiv3+=bioDiv2;
								bioDiv4=BioDivTemp+bioDiv2;
								bioDiv5=BioDivTemp+bioDiv3;
								bioDiv1+=bioDiv3;
								BioDivTemp+=bioDiv2;
								
								if(itnip>0)
									commonAncestor=termNodesInPeriod[0];
								else if(igtnip>0)
									commonAncestor=ghostTermNodesInPeriod[0];
								else commonAncestor=ghostIntNodesInPeriod[0];
								commonAnc=false;
								while(!commonAnc && tree.nodeExists(tree.motherOfNode(commonAncestor))){
									commonAnc=true;
									for(int k=0;k<itnip;k++)
										if(!tree.descendantOf(termNodesInPeriod[k],commonAncestor))
											commonAnc=false;
									for(int k=0;k<igtnip;k++)
										if(!tree.descendantOf(ghostTermNodesInPeriod[k],commonAncestor))
											commonAnc=false;
									for(int k=0;k<iginip;k++)
										if(!tree.descendantOf(ghostIntNodesInPeriod[k],commonAncestor))
											commonAnc=false;
									if(!commonAnc)
										commonAncestor=tree.motherOfNode(commonAncestor);
								}
								
								for(int l=1;l<nodesInClade.length;l++){
									if(tree.nodeExists(tree.firstDaughterOfNode(nodesInClade[l])) && tree.descendantOf(nodesInClade[l],commonAncestor)){
										count=false;
										for(int k=0;k<itnip;k++)
											if(tree.descendantOf(termNodesInPeriod[k],nodesInClade[l]))
												count=true;
										if(count){
											pdi+=tree.getBranchLength(nodesInClade[l],1);
										}
										else {
											for(int k=0;k<igtnip;k++)
												if(tree.descendantOf(ghostTermNodesInPeriod[k],nodesInClade[l]))
													count=true;
											if(count){
												pdi+=tree.getBranchLength(nodesInClade[l],1);
											}
											else {
												for(int k=0;k<iginip;k++)
													if(tree.descendantOf(ghostIntNodesInPeriod[k],nodesInClade[l]))
														count=true;
												if(count){
													pdi+=tree.getBranchLength(nodesInClade[l],1);
												}
											}
										}
									}
								}
								for(int k=0;k<itnip;k++){
									pdi+=periodTop-getHeightToRoot(tree,tree.motherOfNode(termNodesInPeriod[k]));
								}
								for(int k=0;k<igtnip;k++){
									pdi+=periodTop-getHeightToRoot(tree,tree.motherOfNode(ghostTermNodesInPeriod[k]));
								}
								for(int k=0;k<iginip;k++){
									pdi+=periodTop-getHeightToRoot(tree,tree.motherOfNode(ghostIntNodesInPeriod[k]));
								}
								pdi*=coeff;
								if(pdi%1<0.5){
									pdi=Math.floor(pdi);
									pdi/=coeff;
								}
								else{
									pdi=Math.floor(pdi);
									pdi++;
									pdi/=coeff;
								}
								if(temp.lastIndexOf(',')>0)
									temp=temp.substring(0,temp.lastIndexOf(','));
								if(temp=="") temp="(none)";
								if(bioDiv1>1)
									temp="--- "+scaleData.getTaxa().getTaxonName(i)+" ---\nBD1 = "+bioDiv1+"\nBD2 = "+bioDiv2+"\nBD3 = "+bioDiv3+"\nBD4 = "+bioDiv4+"\nBD5 = "+bioDiv5+"\n\nPDI = "+pdi+"\n\n{Species present in this period}\n"+temp;
								else temp="--- "+scaleData.getTaxa().getTaxonName(i)+" ---\nBD1 = "+bioDiv1+"\nBD2 = "+bioDiv2+"\nBD3 = "+bioDiv3+"\nBD4 = "+bioDiv4+"\nBD5 = "+bioDiv5+"\n\nPDI = not specified\n\n{Species present in this period}\n"+temp;
								infoByPeriod=temp+"\n\n"+infoByPeriod;
							}
						}
					}
				
				
					
				String legend=
					"BD1 = Biodiversity in the period including all ghost lineages (internal branches included) but excluding taxa that became extinct in earlier periods.\n" +
					"BD2 = Biodiversity in the period without ghost lineages.\n" +
					"BD3 = BD2 + ghost lineages attributable only to terminal taxa (internal branches excluded).\n" +
					"BD4 = BD2 + extinct terminal taxa.\n" +
					"BD5 = BD3 + extinct terminal taxa.\n\n" +
					"PDI = Phylogenetic Diversity Index including all ghost lineages (internal and terminal branches) but excluding taxa that became extinct at earlier periods. This is exactly the PDI proposed by Faith (1992).";
				message="********** Clade Information **********\n"+clade+"\n\n"+numberOfSpeciesInClade+"\n\n"+phyloDivIndex+"\n\n"+periods+"\n\n********** Information by period **********\n\n"+legend+"\n\n"+infoByPeriod;
				String[] extraButtons={"Save"};
				STMessage.displayInformation("Statistics",message,extraButtons,this);
			}
		}
		return null;
	}
	public  void actionPerformed(ActionEvent event) {
		String buttonLabel = event.getActionCommand();
		if(buttonLabel=="Save"){
			MainThread.setShowWaitWindow(false);
			FileDialog dialog = new FileDialog(ownerModule.getEmployer().getModuleWindow().getParentFrame(), "Save Statistics", FileDialog.SAVE);
			dialog.setBackground(ColorTheme.getInterfaceBackground());
			dialog.setFile("Untitled.txt");
			dialog.setVisible(true);
			String fileName = dialog.getFile();
			if(fileName == null)
				return;
			String path = dialog.getDirectory();
			MainThread.setShowWaitWindow(true);
			File file = new File(path+fileName);
			try {
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write(message);
				writer.flush();
				writer.close();
			}catch(IOException e) {MesquiteTrunk.mesquiteTrunk.logln("Error saving Statitics file "+fileName);}
		}
	}
	/*-----------------------------------------*/
	/** Returns an array of terminal branch node numbers*/
	private int[] getTermBranch(Tree t,int node){
		int[] termBranchNode = new int[t.numberOfTerminalsInClade(node)];
		intI=0;
		findTermNodes(t,node,termBranchNode);
		return termBranchNode;
	}
	private int intI=0;
	private void findTermNodes(Tree t,int node,int[] termNodes){
		for(int daughter=t.firstDaughterOfNode(node);t.nodeExists(daughter);daughter=t.nextSisterOfNode(daughter)){
			if(!t.nodeExists(t.firstDaughterOfNode(daughter))){
				termNodes[intI]=daughter;
				intI++;
			}
			findTermNodes(t,daughter,termNodes);
		}
	}
	/*-----------------------------------------*/
	/** Returns all nodes in clade*/
	private int[] getBranch(Tree t,int node){
		int[] branchNodes = new int[t.numberOfNodesInClade(node)];
		intI=0;
		findNodes(t,node,branchNodes);
		return branchNodes;
	}
	private void findNodes(Tree t,int node,int[] branchNodes){
		branchNodes[intI]=node;
		intI++;
		for(int daughter=t.firstDaughterOfNode(node);t.nodeExists(daughter);daughter=t.nextSisterOfNode(daughter)){
			findNodes(t,daughter,branchNodes);
		}
	}
	/*-----------------------------------------*/
	/** Returns total height from root to node*/
	private  double getHeightToRoot(Tree t,int node) { 
		if(node==t.getRoot() || !t.nodeExists(node)) return 0;
		else return t.getBranchLength(node,1)+getHeightToRoot(t,t.motherOfNode(node));
	}
}



