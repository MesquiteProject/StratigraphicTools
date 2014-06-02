/* Code for stratigraphic tools package (http://mesquiteproject.org/... ).Copyright 2005 by S�bastien Josse, Thomas Moreau and Michel Laurin.Based on Mesquite source code copyright 1997-2005 W. & D. Maddison.Available for Mesquite version 1.06Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.stratigraphictools; import mesquite.lib.characters.*;import mesquite.lib.*;//public class STTree extends Associable implements AdjustableTree, Listable, Renamable, Commandable, MesquiteListener, CompatibilityChecker, Identifiable {public class STTree extends MesquiteTree {		/** The constructor, passed the Taxa on which the tree is based */	public STTree (Taxa taxa) {		super(taxa);	}	/** The constructor, passed the Taxa on which the tree is based.  This initializer is used only for cloning,	as it prepares for an exact copy of the tree even if it's currently out of date*/	public STTree (Taxa taxa, int numTaxa, int numNodeSpaces, long taxaVersion) {		super( taxa,  numTaxa,  numNodeSpaces,  taxaVersion);	}	/** The constructor, passed the Taxa on which the tree is based, and a string description of the tree */	public STTree (Taxa taxa, String description) {		super( taxa,  description);	}	/*-----------------------------------------*/	/** Returns total height from root to node*/	public  double getHeightToRoot(int node) { 		if(node==getRoot() || !nodeExists(node)) return 0;		else return getBranchLength(node,1)+getHeightToRoot(motherOfNode(node));	}	/*-----------------------------------------*/	/** Returns an array of terminal branch node numbers sorted by level*/	public int[] getTermBranchSortedByLevel(){		int[] termBranches = getTermBranch(getRoot());		int[] termBranchLevel = new int[termBranches.length];		int[] termBranchSorted = new int[termBranches.length];		int level=0, temp=0;		for(int i=0;i<termBranches.length;i++){			termBranchLevel[i]=getBranchLevel(termBranches[i],0);		}		for(int j=termBranches.length-1;j>=0;j--){			level=0;			for(int k=0;k<termBranches.length;k++){				if(level<termBranchLevel[k]) {					level=termBranchLevel[k];					temp=k;				}				else if(level==termBranchLevel[k] && getBranchLength(termBranches[k],1)>getBranchLength(temp,1)){					level=termBranchLevel[k];					temp=k;				}			}			termBranchSorted[j]=termBranches[temp];			termBranchLevel[temp]=-1;		}		return termBranchSorted;	}	/*-----------------------------------------*/	/** Returns an array of terminal branch node numbers sorted by length*/	public int[] getTermBranchSortedByLength(){		int[] termBranchNode = new int[getTaxa().getNumTaxa()];		double[] termBranchLength = new double[getTaxa().getNumTaxa()];		int[] termBranchSorted = new int[getTaxa().getNumTaxa()];		int count=0, temp=0;		double max = 0;		for(int i=0;count<getTaxa().getNumTaxa();i++){			if(!nodeExists(firstDaughterOfNode(i)) && getHeightToRoot(i)>0){				termBranchNode[count]=i;				termBranchLength[count]= getBranchLength(i,1);				count++;			}		}		for(int j=getTaxa().getNumTaxa()-1;j>=0;j--){			max=0;			for(int k=0;k<getTaxa().getNumTaxa();k++){				if(max<termBranchLength[k]) {					max=termBranchLength[k];					temp=k;				}			}			termBranchSorted[j]=termBranchNode[temp];			termBranchLength[temp]=-1;		}		return termBranchSorted;	}	/*-----------------------------------------*/	/** Returns an array of terminal branch node numbers*/	public int[] getTermBranch(int node){		int[] termBranchNode = new int[numberOfTerminalsInClade(node)];		intI=0;		findTermNodes(node,termBranchNode);		return termBranchNode;	}	private int intI=0;	private void findTermNodes(int node,int[] termNodes){		for(int daughter=firstDaughterOfNode(node);nodeExists(daughter);daughter=nextSisterOfNode(daughter)){			if(!nodeExists(firstDaughterOfNode(daughter))){				termNodes[intI]=daughter;				intI++;			}			findTermNodes(daughter,termNodes);		}	}	/*-----------------------------------------*/	/** Returns all nodes in clade*/	public int[] getBranch(int node){		int[] branchNodes = new int[numberOfNodesInClade(node)];		intI=0;		findNodes(node,branchNodes);		return branchNodes;	}	private void findNodes(int node,int[] branchNodes){		branchNodes[intI]=node;		intI++;		for(int daughter=firstDaughterOfNode(node);nodeExists(daughter);daughter=nextSisterOfNode(daughter)){			findNodes(daughter,branchNodes);		}	}		/*-----------------------------------------*/	/** Returns the number of nodes between one branch and the root*/	public int getBranchLevel(int node,int level){		if(node==getRoot() || !nodeExists(motherOfNode(node))) return level;		else {			level++;			return getBranchLevel(motherOfNode(node),level);		}	}	/** Returns the number of nodes between  branch A and branch B*/	public int getBranchLevel(int nodeA,int nodeB,int level){		if(nodeA==getRoot() || nodeA==nodeB || !nodeExists(motherOfNode(nodeA))) return level;		else {			level++;			return getBranchLevel(motherOfNode(nodeA),nodeB,level);		}	}	/*-----------------------------------------*/	/** Sets the minimal branch length of terminal and internal nodes (stored as a double internally).*/	private void setMinLengths(int[] node, double termLength, double intLength) {		double shift =0;		double newLength = 0;				for (int i = 0; i<node.length; i++){			//TODO			if(getBranchLength(node[i],1)!=termLength){				int smallestInternalSister = -1;				int smallestTerminalSister=node[i];				for(int j=this.firstDaughterOfNode(motherOfNode(node[i]));this.nodeExists(j);j=this.nextSisterOfNode(j)){					if(nodeExists(firstDaughterOfNode(j)) && (getBranchLength(j,1)<getBranchLength(smallestInternalSister,1) || smallestInternalSister == -1))						smallestInternalSister = j;					if(!nodeExists(firstDaughterOfNode(j)) && getBranchLength(j,1)<getBranchLength(smallestTerminalSister,1)) 						smallestTerminalSister=j;				}				if(smallestInternalSister == -1 || termLength-getBranchLength(smallestTerminalSister,1)>0)					shift =termLength-getBranchLength(smallestTerminalSister,1);				else if((termLength-getBranchLength(smallestTerminalSister,1))<0 && (termLength-getBranchLength(smallestTerminalSister,1))<(intLength-getBranchLength(smallestInternalSister,1)))					shift =intLength-getBranchLength(smallestInternalSister,1);				for(int j=this.firstDaughterOfNode(motherOfNode(node[i]));this.nodeExists(j);j=this.nextSisterOfNode(j)){						newLength = getBranchLength(j,1)+shift;					setBranchLength(j,newLength,false);				}				if(motherOfNode(node[i])!=getRoot()) {					newLength = getBranchLength(motherOfNode(node[i]),1)-shift;					setBranchLength(motherOfNode(node[i]),newLength,false);				}			}			int intNode = motherOfNode(node[i]);			while(intNode!=getRoot()){				if(getBranchLength(intNode,1)!=intLength){					int smallestTerminalSister=-1;					int smallestInternalSister=intNode;					for(int j=this.firstDaughterOfNode(motherOfNode(intNode));this.nodeExists(j);j=this.nextSisterOfNode(j)){						if(!nodeExists(firstDaughterOfNode(j)) && (getBranchLength(j,1)<getBranchLength(smallestTerminalSister,1) || smallestTerminalSister == -1))							smallestTerminalSister=j;						if(nodeExists(firstDaughterOfNode(j)) && getBranchLength(j,1)<getBranchLength(smallestInternalSister,1)) 							smallestInternalSister=j;					}					if(smallestTerminalSister==-1 || intLength-getBranchLength(smallestInternalSister,1)>0)						shift =intLength-getBranchLength(smallestInternalSister,1);					else if((intLength-getBranchLength(smallestInternalSister,1))<0 && (termLength-getBranchLength(smallestTerminalSister,1))>(intLength-getBranchLength(smallestInternalSister,1)))						shift =termLength-getBranchLength(smallestTerminalSister,1);					for(int j=this.firstDaughterOfNode(motherOfNode(intNode));this.nodeExists(j);j=this.nextSisterOfNode(j)){						newLength = getBranchLength(j,1)+shift;						setBranchLength(j,newLength,false);					}					if(motherOfNode(intNode)!=getRoot()) {						newLength = getBranchLength(motherOfNode(intNode),1)-shift;						setBranchLength(motherOfNode(intNode),newLength,false);					}				}				intNode = motherOfNode(intNode);			}								}	}	/*-----------------------------------------*/	/** Sets the minimal branch length of terminal and internal nodes (stored as a double internally).*/	private void setMinLengths(int[] node, CharacterData data, double intLength) {		//Initialisation		double shift = 0;		double[] scaleHeightTop = new double[getNumNodeSpaces()];		double[] scaleHeightBase = new double[getNumNodeSpaces()];		double newLength = 0;		double smallestInternalSister = 0;		CharacterState cs = null;		int smallestTermSister=-1;		int topNode = getRoot();		int it = 0;		for(int i = 0; i<node.length; i++){			if(getHeightToRoot(node[i])>getHeightToRoot(topNode)){				topNode = node[i];			}		}		double depthScale = getHeightToRoot(topNode);		int itMin=0;		double min=0;		for(int i = 0;i<node.length;i++){			itMin=0;			scaleHeightTop[node[i]]=depthScale-(new Double(data.getCharacterState(cs,1,0).toString()).doubleValue());			for(it=0; it<data.getNumTaxa(); it++) {				min = depthScale-(new Double(data.getCharacterState(cs,1,it).toString()).doubleValue());				if(Math.abs(min-getHeightToRoot(node[i]))<=Math.abs(scaleHeightTop[node[i]]-getHeightToRoot(node[i]))){ 					scaleHeightTop[node[i]]=min;					itMin=it;				}			}			scaleHeightBase[node[i]] = scaleHeightTop[node[i]]-(new Double(data.getCharacterState(cs,0,itMin).toString()).doubleValue());		}		boolean termNode = true, smallestTerminalSister = true ,smallestInternalSisterInit=false;		//Place terminal branches to the correct position according to the scale		for(int i=0;i<node.length;i++){			smallestTermSister=-1;			for(int k=firstDaughterOfNode(motherOfNode(node[i]));nodeExists(k);k=nextSisterOfNode(k)){				if(!nodeExists(firstDaughterOfNode(k))){					if(smallestTermSister==-1 || getBranchLength(k,1)<getBranchLength(smallestTermSister,1)){						smallestTermSister=k;					}				}			}			shift = scaleHeightTop[node[i]]-getHeightToRoot(node[i]);			setBranchLength(node[i],getBranchLength(node[i],1)+shift,false);			if(smallestTermSister==node[i] || shift<=0){				shift = scaleHeightBase[node[i]]-getHeightToRoot(motherOfNode(node[i]));				if(motherOfNode(node[i])!=getRoot())					setBranchLength(motherOfNode(node[i]),getBranchLength(motherOfNode(node[i]),1)+shift,false);				for(int k = firstDaughterOfNode(motherOfNode(node[i]));nodeExists(k);k=nextSisterOfNode(k)){					setBranchLength(k,getBranchLength(k,1)-shift,false);				}			}		}		//Adjust null and negative terminal branches		for(int i=0;i<node.length;i++){			if(getBranchLength(node[i],1)<0.01){				newLength = scaleHeightTop[node[i]]-scaleHeightBase[node[i]];				shift = scaleHeightBase[node[i]]-getHeightToRoot(motherOfNode(node[i]));				setBranchLength(node[i],newLength,false);				if(motherOfNode(node[i])!=getRoot())					setBranchLength(motherOfNode(node[i]),getBranchLength(motherOfNode(node[i]),1)+shift,false);				for(int k = firstDaughterOfNode(motherOfNode(node[i]));nodeExists(k);k=nextSisterOfNode(k)){					if(k!=node[i]) setBranchLength(k,getBranchLength(k,1)-shift,false);				}			}			//else System.out.print(node[i]+"\t");		}		//Adjust root's terminal daughters		for(int j=firstDaughterOfNode(getRoot());nodeExists(j);j=nextSisterOfNode(j)){			if(!nodeExists(firstDaughterOfNode(j))){				shift = scaleHeightTop[j]-getHeightToRoot(j);				setBranchLength(j,getBranchLength(j,1)+shift,false);			}		}		//Adjust internal and external branches to fit the requirement of the minimum length for internal branches		for (int i = 0; i<node.length; i++){			int intNode = motherOfNode(node[i]);			smallestTermSister=-1;			double temp=0;			while(intNode!=getRoot()){				smallestTermSister=-1;				shift =intLength-getBranchLength(intNode,1);				temp=0;				for(int j=firstDaughterOfNode(motherOfNode(intNode));nodeExists(j);j=nextSisterOfNode(j)){					if(!nodeExists(firstDaughterOfNode(j))){						if(smallestTermSister==-1)							smallestTermSister=j;						else if(getBranchLength(j)<getBranchLength(smallestTermSister))							smallestTermSister=j;						temp=scaleHeightTop[j]-scaleHeightBase[j]-getBranchLength(j,1);					}else if(getBranchLength(j,1)<getBranchLength(intNode,1))							shift =intLength-getBranchLength(j,1);					}				if(smallestTermSister==-1){					for(int j=firstDaughterOfNode(motherOfNode(intNode));nodeExists(j);j=nextSisterOfNode(j)){						newLength = getBranchLength(j,1)+shift;						setBranchLength(j,newLength,false);					}					if(motherOfNode(intNode)!=getRoot()) {						newLength = getBranchLength(motherOfNode(intNode),1)-shift;						setBranchLength(motherOfNode(intNode),newLength,false);					}				} else if(getBranchLength(smallestTermSister,1)-(scaleHeightTop[smallestTermSister]-scaleHeightBase[smallestTermSister])+shift>=0){					for(int j=firstDaughterOfNode(motherOfNode(intNode));nodeExists(j);j=nextSisterOfNode(j)){						newLength = getBranchLength(j,1)+shift;						setBranchLength(j,newLength,false);					}					if(motherOfNode(intNode)!=getRoot()) {						newLength = getBranchLength(motherOfNode(intNode),1)-shift;						setBranchLength(motherOfNode(intNode),newLength,false);					}				}				intNode = motherOfNode(intNode);			}		}	}	/*-----------------------------------------*/	/** Check if terminales branches are set to the wanted value*/	String stringDialog[] = new String[3];	int numTrue = 0, numFalse = 0;	public void checkWantedTermLength(int node, double length) {		for (int daughter = firstDaughterOfNode(node); nodeExists(daughter); daughter = nextSisterOfNode(daughter))			checkWantedTermLength(daughter, length);				if(!nodeExists(firstDaughterOfNode(node))) {			if(getBranchLength(node,1) == length) {				numTrue++;				stringDialog[0] = stringDialog[0]+";"+getTaxa().getTaxonName(taxonNumberOfNode(node));			}			else {				numFalse++;				stringDialog[1] = stringDialog[1]+";"+getTaxa().getTaxonName(taxonNumberOfNode(node));				stringDialog[2] = stringDialog[2]+";"+getBranchLength(node,1);			}		}	}	/*-----------------------------------------*/	/** Sets the branch lengths of nodes to a round value.*/	public  void roundAllBranchLengths(int precision,boolean notify) { 		double length = 0, oldLength = 0;		int number = 0;		String message = "";		for (int i=0; i<getNumNodeSpaces(); i++) {			length = getBranchLength(i,1);			oldLength = length;			for(int k =0;k<precision;k++)				length*=10;			if((length*10)%10>5) length = Math.floor(length+1);			else length = Math.floor(length);			for(int k =0;k<precision;k++)				length/=10;			setBranchLength(i,length,false);			if(oldLength!=length){ 				message+="node "+i+": "+oldLength+" => "+length+"\n";				number++;			}		}		if(number>1) message = number+" branches rounded:\n"+message;		else message = number+" branch rounded:\n"+message;		if(notify)			MesquiteMessage.notifyUser(message);	}	/*-----------------------------------------*/	/** Sets the minimal branch length of terminal node.*/	public void setMinBranchLengths(double length,double intLength){		/*		if (branchLength==null) {			branchLength = new double[getNumNodeSpaces()];			for (int i=0; i<getNumNodeSpaces(); i++) {				branchLength[i]= MesquiteDouble.unassigned;			}		}*/		int[] test=getTermBranchSortedByLevel();		setMinLengths(test,length,intLength);				stringDialog[0]=null; numTrue = 0; 							// Display a dialog box to notify user about modification		stringDialog[1]=null; stringDialog[2]=null; numFalse = 0;		checkWantedTermLength(getRoot(),length);		String dialog = "";		String display = "";		String dialogTaxon[] = null;		if(numTrue>0) {			dialogTaxon = stringDialog[0].split(";");			int cpt = 0;			while(cpt<numTrue) {				cpt++;				dialog = dialog + dialogTaxon[cpt] + "\n";			}			if(numTrue>1) display = numTrue +" terminal branches are correctly updated :\n"+dialog+"\n";			else display = "Only one terminal branche is correctly updated :\n"+dialog+"\n";		}		else			display = "No terminal branche is set to "+length+"\n\n";					dialogTaxon = null;		String dialogLength[] = null;		dialog = "";		if(numFalse>0) {			dialogTaxon = stringDialog[1].split(";");			dialogLength = stringDialog[2].split(";");			int cpt = 0;			while(cpt<numFalse) {				cpt++;				dialog = dialog + dialogTaxon[cpt]+" ("+dialogLength[cpt]+")\n";			}			if(numFalse>1) display = display + numFalse +" terminal branches couldn't be updated to the wanted value ("+length+") :\n"+dialog;			else display = display + "One terminal branche couldn't be updated to the wanted value ("+length+") :\n"+dialog;		}		else			display = display + "All terminal branches are now set to "+length;						MesquiteMessage.notifyUser(display);				//incrementVersion(BRANCHLENGTHS_CHANGED, false);		scaleAllBranchLengths(1, false);	}	/*-----------------------------------------*/	/** Sets the minimal branch length of terminal and internal nodes.*/	public void setMinBranchLengths(CharacterData data,double intLength){		/*		if (branchLength==null) {			branchLength = new double[getNumNodeSpaces()];			for (int i=0; i<getNumNodeSpaces(); i++) {				branchLength[i]= MesquiteDouble.unassigned;			}		}*/		int[] termBranch=getTermBranchSortedByLevel();		setMinLengths(termBranch,data,intLength);			//incrementVersion(BRANCHLENGTHS_CHANGED, false);		scaleAllBranchLengths(1, false);	}}