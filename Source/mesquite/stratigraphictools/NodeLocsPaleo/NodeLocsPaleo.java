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
package mesquite.stratigraphictools.NodeLocsPaleo;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stratigraphictools.ScaleDataWindowCoord.*;


/** Calculates node locations for tree drawing in a standard vertical/horizontal position, as used by DiagonalDrawTree and SquareTree (for example).*/
public class NodeLocsPaleo extends NodeLocsVH {
	//TreeDrawing treeDrawing;
	//Tree tree;
	int lastOrientation = 0;
	Vector extras;
	double fixedDepth = 1;
	boolean leaveScaleAlone = true;
	boolean fixedScale = false;
	MesquiteBoolean stretch;
	MesquiteBoolean showBranchLengths;
	MesquiteBoolean showScale;
	MesquiteBoolean adjustScale;
	MesquiteBoolean fillScale;
	MesquiteBoolean extendScale;
	MesquiteBoolean magnetExt;
	MesquiteBoolean magnetInt;
	
	private boolean saveScaleBeforeClosing = true;
	
	boolean resetShowBranchLengths = false;
	
	static final int totalHeight = 0;
	static final int stretchfactor = 1;
	static final int  scaling = 2;
	
	double namesAngle = MesquiteDouble.unassigned;
	
	int ROOTSIZE = 20;
	MesquiteMenuItemSpec fixedScalingMenuItem, showScaleMenuItem;
	MesquiteMenuItemSpec offFixedScalingMenuItem, stretchMenuItem, evenMenuItem;
	NameReference triangleNameRef;
	static int defaultOrientation = TreeDisplay.UP;
	MesquiteBoolean center;
	MesquiteBoolean even;
	
	ScaleDataWindowCoord mb;
	private CharacterData data = null;
	
	/*.................................................................................................................*/
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		extras = new Vector();
		stretch = new MesquiteBoolean(false);
		center = new MesquiteBoolean(false);
		even = new MesquiteBoolean(false);
		if (getEmployer()!=null && "Square Tree".equalsIgnoreCase(getEmployer().getName())){ //a bit non-standard but a helpful service to use different defaults for square
			even.setValue(true);
			center.setValue(true);
		}
		triangleNameRef = NameReference.getNameReference("triangled");
		showBranchLengths = new MesquiteBoolean(false);
		showScale = new MesquiteBoolean(true);
		adjustScale = new MesquiteBoolean(true);
		fillScale = new MesquiteBoolean(true);
		extendScale = new MesquiteBoolean(true);
		magnetExt = new MesquiteBoolean(false);
		magnetInt = new MesquiteBoolean(false);
		
		mb = (ScaleDataWindowCoord)hireEmployee(ScaleDataWindowCoord.class, "");
		mb.setScaleData(data);
		adjustScale.setValue(true);
		fillScale.setValue(true);
		extendScale.setValue(true);
		magnetExt.setValue(false);
		magnetInt.setValue(false);
		
		addCheckMenuItem(null, "Branches Proportional to Lengths", makeCommand("branchLengthsToggle", this), showBranchLengths);
		
		if (showBranchLengths.getValue()) {
			addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
			addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			mb.addScaleMenu();
			
			resetShowBranchLengths=true;
		}
		else {
			stretchMenuItem = addCheckMenuItem(null, "Stretch tree to Fit", makeCommand("stretchToggle", this), stretch);
			evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);
		}
		addCheckMenuItem(null, "Centered Branches", makeCommand("toggleCenter", this), center);
		if (MesquiteWindow.Java2Davailable)
			addMenuItem("Taxon Name Angle...", makeCommand("namesAngle", this));
		addMenuItem("Set Current Orientation as Default", makeCommand("setDefaultOrientation",  this));
		
		return true;
	}
	/*.................................................................................................................*/
	public void setSaveScaleBeforeClosing(boolean value) {
		saveScaleBeforeClosing = value;
	}
	
	public void endJob(){
		if(saveScaleBeforeClosing) {
			boolean save = MesquiteBoolean.yesNoQuery(containerOfModule(),"Do you want to save the current scale matrix ?\nClick Cancel to close without saving");
			if(save)
				if(!mb.saveScaleMatrix()) MesquiteMessage.notifyUser("No scale Matrix available, press OK to quit");
		}
		storePreferences();
		if (extras!=null) {
			for (int i=0; i<extras.size(); i++){
				TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
				if (extra!=null){
					TreeDisplay td = extra.getTreeDisplay();
					extra.turnOff();
					if (td!=null)
						td.removeExtra(extra);
				}
			}
			extras.removeAllElements();
		}
		
		showBranchLengths.releaseMenuItem();
		showScale.releaseMenuItem();
		adjustScale.releaseMenuItem();
		fillScale.releaseMenuItem();
		extendScale.releaseMenuItem();
		magnetExt.releaseMenuItem();
		magnetInt.releaseMenuItem();
		stretch.releaseMenuItem();
		center.releaseMenuItem();
		even.releaseMenuItem();
		super.endJob();
	}
	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			defaultOrientation = MesquiteInteger.fromString(prefs[0]);
		}
	}
	/*.................................................................................................................*/
	public String[] preparePreferencesForFile () {
		String[] prefs= new String[1];
		prefs[0] = Integer.toString(defaultOrientation);
		return prefs;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("stretchToggle " + stretch.toOffOnString());
		temp.addLine("branchLengthsToggle " + showBranchLengths.toOffOnString());
		temp.addLine("toggleScale " + showScale.toOffOnString());
		temp.addLine("toggleAdjustScale " + adjustScale.toOffOnString());
		temp.addLine("toggleAdjustScale " + adjustScale.toOffOnString());
		temp.addLine("toggleFillScale " + fillScale.toOffOnString());
		temp.addLine("toggleExtendScale " + extendScale.toOffOnString());
		temp.addLine("toggleMagnetExt " + magnetExt.toOffOnString());
		temp.addLine("toggleMagnetInt " + magnetInt.toOffOnString());
		temp.addLine("toggleCenter " + center.toOffOnString());
		temp.addLine("toggleEven " + even.toOffOnString());
		if (fixedScale)
			temp.addLine("setFixedScaling " + MesquiteDouble.toString(fixedDepth) );
		temp.addLine("namesAngle " + MesquiteDouble.toString(namesAngle));
		return temp;
	}
	
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		
		if (checker.compare(this.getClass(), "Sets whether or not to stretch the tree to fit the drawing area", "[on = stretch; off]", commandName, "stretchToggle")) {
			stretch.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to center the nodes between the immediate descendents, or the terminal in the clade", "[on = center over immediate; off]", commandName, "toggleCenter")) {
			center.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to space the nodes evenly from root to tips", "[on = space evenly; off]", commandName, "toggleEven")) {
			even.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the current orientation to be the default", null, commandName, "setDefaultOrientation")) {
			defaultOrientation = lastOrientation;
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "Sets the angle names are shown at in default UP orientation", "[angle in degrees clockwise from horizontal; ? = default]", commandName, "namesAngle")) {
			if (arguments == null && !MesquiteThread.isScripting()){
				double current;
				if (!MesquiteDouble.isCombinable(namesAngle))
					current = namesAngle;
				else
					current = namesAngle/2/Math.PI*360;
				MesquiteDouble d = new MesquiteDouble(current);
				if (!QueryDialogs.queryDouble(containerOfModule(), "Names Angle", "Angle of taxon names, in degrees clockwise from horizontal.  Use \"?\" to indicate default.  Typical settings are between 0 degrees and -90 degrees.  This setting applies only when tree is in UP orientation", d))
					return null;
				namesAngle = d.getValue();
				if (MesquiteDouble.isCombinable(namesAngle))
					namesAngle = namesAngle/360*2*Math.PI;
				if (MesquiteWindow.Java2Davailable)
					parametersChanged();
			}
			else {
				
				double angle = MesquiteDouble.fromString(parser.getFirstToken(arguments));
				namesAngle = angle;
				if (MesquiteWindow.Java2Davailable)
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[on = proportional; off]", commandName, "branchLengthsToggle")) {
			resetShowBranchLengths=true;
			showBranchLengths.toggleValue(parser.getFirstToken(arguments));
			if (!showBranchLengths.getValue()) {
				deleteMenuItem(fixedScalingMenuItem);
				deleteMenuItem(showScaleMenuItem);
				mb.deleteScaleMenu();
				if (stretchMenuItem == null)
					stretchMenuItem = addCheckMenuItem(null, "Stretch tree to Fit", makeCommand("stretchToggle", this), stretch);
				if (evenMenuItem == null)
					evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);
			}
			else {
				fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
				showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
				mb.addScaleMenu();
				deleteMenuItem(stretchMenuItem);
				stretchMenuItem = null;
				deleteMenuItem(evenMenuItem);
				evenMenuItem = null;
			}
			resetContainingMenuBar();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets fixed scale length", "[length of branch lengths scale]", commandName, "setFixedScaling")) {
			double newDepth;
			if (StringUtil.blank(arguments))
				newDepth= MesquiteDouble.queryDouble(containerOfModule(), "Set scaling depth", "Depth:", fixedDepth);
			else 
				newDepth= MesquiteDouble.fromString(arguments);
			if (MesquiteDouble.isCombinable(newDepth) && newDepth>0) {
				//TODO: remember these fixedScaling and depth to set in calcnodelocs below!!!!
				fixedScale = true;
				fixedDepth = newDepth;
				leaveScaleAlone = false;
				if (offFixedScalingMenuItem == null) {
					offFixedScalingMenuItem = addMenuItem( "Off Fixed Scaling", makeCommand("offFixedScaling", this));
					resetContainingMenuBar();
				}
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw the scale for branch lengths", "[on or off]", commandName, "toggleScale")) {
			showScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw the adjust scale for branch lengths", "[on or off]", commandName, "toggleAdjustScale")) {
			if(data == null) {
				if(mb != null)
					mb.doCommand("showDataWindow","",checker);
			}
			adjustScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		/*else if (checker.compare(this.getClass(), "Draw the tree when scale matrix editor change", "", commandName, "drawAdjustScale")) {
		 if(data != null)
		 parametersChanged();
		 }*/
		else if (checker.compare(this.getClass(), "Sets whether or not to fill the adjust scale for branch lengths", "[on or off]", commandName, "toggleFillScale")) {
			fillScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to extend the adjust scale under the tree", "[on or off]", commandName, "toggleExtendScale")) {
			extendScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "External Magnetism", "[on or off]", commandName, "toggleMagnetExt")) {
			magnetExt.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Internal Magnetism", "[on or off]", commandName, "toggleMagnetInt")) {
			magnetInt.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Turns off fixed scaling", null, commandName, "offFixedScaling")) {
			fixedScale = false;
			leaveScaleAlone = false;
			deleteMenuItem(offFixedScalingMenuItem);
			offFixedScalingMenuItem = null;
			resetContainingMenuBar();
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	
	public String getName() {
		return "Node Locations (Paleo)";
	}
	
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the node locations in a tree drawing, for use with vertical or horizontal tree drawers (e.g., the standard diagnonal or square trees)." ;
	}
	public boolean compatibleWithOrientation(int orientation) {
		return (orientation==TreeDisplay.UP || orientation==TreeDisplay.DOWN || orientation==TreeDisplay.RIGHT ||orientation==TreeDisplay.LEFT);
	}
	public void setDefaultOrientation(TreeDisplay treeDisplay) {
		treeDisplay.setOrientation(defaultOrientation);
	}
	public int getDefaultOrientation() {
		return defaultOrientation;
	}
	
	/*_________________________________________________*/
	
	
	public void scaleChanged() {
		parametersChanged();
	}
	
	public CharacterData getScaleData(){
		return data;
	}
	public void setScaleData(CharacterData data){
		this.data = data;
	}
	
	public void setAdjustScale(MesquiteBoolean adjustScale) {
		this.adjustScale = adjustScale;
	}
	
	public MesquiteBoolean getAdjustScale() {
		return adjustScale;
	}
	
	public void setFillScale(MesquiteBoolean fillScale) {
		this.fillScale = fillScale;
	}
	
	public MesquiteBoolean getFillScale() {
		return fillScale;
	}
	
	public void setExtendScale(MesquiteBoolean extendScale) {
		this.extendScale = extendScale;
	}
	
	public MesquiteBoolean getExtendScale() {
		return extendScale;
	}
	
	public void setMagnetExt(MesquiteBoolean magnetExt) {
		this.magnetExt = magnetExt;
	}
	
	public MesquiteBoolean getMagnetExt() {
		return magnetExt;
	}
	
	public void setMagnetInt(MesquiteBoolean magnetInt) {
		this.magnetInt = magnetInt;
	}
	
	public MesquiteBoolean getMagnetInt() {
		return magnetInt;
	}
	
	/*_________________________________________________*/
	
	
	private double getNonZeroBranchLength(Tree tree, int N) {
		if (tree.branchLengthUnassigned(N))
			return 1;
		else
			return tree.getBranchLength(N);
	}
	/*_________________________________________________*/
	private int lastleft;
	/*_________________________________________________*/
	private void UPCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcInternalLocs(treeDrawing, tree, d);
			int fD =tree.firstDaughterOfNode(N);
			int lD =tree.lastDaughterOfNode(N);
			if (lD==fD)   {//only one descendant
				treeDrawing.y[N] = treeDrawing.y[fD];
				treeDrawing.x[N] =treeDrawing.x[fD];
			}
			else {
				int nFDx = treeDrawing.x[fD];
				int nFDy = treeDrawing.y[fD];
				int nLDx = treeDrawing.x[lD];
				int nLDy = treeDrawing.y[lD];
				treeDrawing.y[N] = (-nFDx + nLDx+nFDy + nLDy) / 2;
				treeDrawing.x[N] =(nFDx + nLDx - nFDy + nLDy) / 2;
			}
		}
	}
	/*_________________________________________________*/
	private void UPDOWNCenterInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPDOWNCenterInternalLocs(treeDrawing, tree, d);
			int fD =tree.firstDaughterOfNode(N);
			int lD =tree.lastDaughterOfNode(N);
			if (lD!=fD)   {//> one descendant
				int nFDx = treeDrawing.x[fD];
				int nLDx = treeDrawing.x[lD];
				treeDrawing.x[N] =(nFDx + nLDx) / 2;
			}
		}
	}
	
	/*....................................................................................................*/
	private void UPCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= treeDisplay.getTaxonSpacing(); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.x[tree.leftmostTerminalOfNode(triangleBase)] + 2*treeDisplay.getTaxonSpacing();
					else 
						lastleft+= (treeDisplay.getTaxonSpacing()*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= treeDisplay.getTaxonSpacing();
			treeDrawing.y[N] = treeDisplay.getTipsMargin();
			treeDrawing.x[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (inTriangle)
					UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, d,true, numInTriangle, triangleBase);
				else
					UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, tree.getAssociatedBit(triangleNameRef, d), tree.numberOfTerminalsInClade(d), d);
			}
		}
	}
	/*....................................................................................................*/
	private void UPevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				UPevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.y[d]>deepest)
					deepest = treeDrawing.y[d];
			}
			treeDrawing.y[N] = deepest + evenVertSpacing;
		}
	}
	/*....................................................................................................*/
	private void UPstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPstretchNodeLocs(treeDisplay, treeDrawing, tree, d);
		treeDrawing.y[N] = treeDisplay.getTipsMargin() + (int)((treeDrawing.y[N]-treeDisplay.getTipsMargin())*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	
	/*....................................................................................................*/
	private void UPdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		double base,totalScaleHeight;
		
		if (!treeDisplay.fixedScalingOn){
			base=bottom;
			totalScaleHeight=treeDisplay.nodeLocsParameters[totalHeight];
		}
		else {
			base=bottom+(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])*treeDisplay.nodeLocsParameters[scaling];
			totalScaleHeight=treeDisplay.fixedDepthScale;
		}
		
		if (N==root)
			nH=bottom;
		else
			nH=ancH - (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);
		treeDrawing.y[N]=(int)(nH);
		
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);
		
	}
	/*_________________________________________________*/
	private void DOWNCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcInternalLocs(treeDrawing, tree, d);
			int nFD = tree.firstDaughterOfNode(N);
			int nLD = tree.lastDaughterOfNode(N);
			int nFDx = treeDrawing.x[nFD];
			int nFDy = treeDrawing.y[nFD];
			int nLDx = treeDrawing.x[nLD];
			int nLDy = treeDrawing.y[nLD];
			if (nLD==nFD)   {//only one descendant; put same as descendant, to be adjusted later
				treeDrawing.y[N] = treeDrawing.y[nFD];
				treeDrawing.x[N] =treeDrawing.x[nFD];
			}
			else {
				treeDrawing.y[N] = (nFDx - nLDx + nFDy + nLDy) / 2;
				treeDrawing.x[N] =(nFDx + nLDx + nFDy - nLDy) / 2;
			}
		}
	}
	
	/*....................................................................................................*/
	private void DOWNCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin,  boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= treeDisplay.getTaxonSpacing(); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.x[tree.leftmostTerminalOfNode(triangleBase)] + 2*treeDisplay.getTaxonSpacing();
					else 
						lastleft+= (treeDisplay.getTaxonSpacing()*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= treeDisplay.getTaxonSpacing();
			treeDrawing.y[N] = margin;
			treeDrawing.x[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (inTriangle)
					DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, true, numInTriangle, triangleBase);
				else
					DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, tree.getAssociatedBit(triangleNameRef, d), tree.numberOfTerminalsInClade(d), d);
		}
	}
	/*....................................................................................................*/
	private void DOWNstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNstretchNodeLocs(treeDisplay, treeDrawing, tree, d, margin);
		treeDrawing.y[N] = margin-(int)((margin-treeDrawing.y[N])*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	/*....................................................................................................*/
	private void DOWNevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 10000000;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				DOWNevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.y[d]<deepest)
					deepest = treeDrawing.y[d];
			}
			treeDrawing.y[N] = deepest - evenVertSpacing;
		}
	}
	
	/*....................................................................................................*/
	private void DOWNdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		if (N==root) 
			nH=bottom;
		else
			nH=ancH + (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);
		
		treeDrawing.y[N]=(int)(nH);
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);
		
	}
	/*_________________________________________________*/
	private void RIGHTCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcInternalLocs(treeDrawing, tree,  d);
			int fD = tree.firstDaughterOfNode(N);
			int lD = tree.lastDaughterOfNode(N);
			int nFDx = treeDrawing.x[fD];
			int nFDy = treeDrawing.y[fD];
			int nLDx = treeDrawing.x[lD];
			int nLDy = treeDrawing.y[lD];
			if (lD==fD)   {//only one descendant
				treeDrawing.y[N] = treeDrawing.y[fD];
				treeDrawing.x[N] =treeDrawing.x[fD];
			}
			else {
				treeDrawing.x[N] =(nFDy - nLDy + nFDx + nLDx) / 2;
				treeDrawing.y[N] =(nFDx - nLDx + nFDy + nLDy) / 2;
			}
		}
	}
	/*_________________________________________________*/
	private void RIGHTLEFTCenterInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTLEFTCenterInternalLocs(treeDrawing, tree, d);
			int fD =tree.firstDaughterOfNode(N);
			int lD =tree.lastDaughterOfNode(N);
			if (lD!=fD)   {//> one descendant
				int nFDy = treeDrawing.y[fD];
				int nLDy = treeDrawing.y[lD];
				treeDrawing.y[N] =(nFDy + nLDy) / 2;
			}
		}
	}
	
	/*....................................................................................................*/
	private void RIGHTCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin,  boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= treeDisplay.getTaxonSpacing(); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.y[tree.leftmostTerminalOfNode(triangleBase)] + 2*treeDisplay.getTaxonSpacing();
					else 
						lastleft+= (treeDisplay.getTaxonSpacing()*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= treeDisplay.getTaxonSpacing();
			treeDrawing.x[N] = margin;
			treeDrawing.y[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (inTriangle)
					RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, true, numInTriangle, triangleBase);
				else
					RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, tree.getAssociatedBit(triangleNameRef, d), tree.numberOfTerminalsInClade(d),d);
		}
	}
	/*....................................................................................................*/
	private void RIGHTstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTstretchNodeLocs(treeDisplay, treeDrawing, tree, d, margin);
		treeDrawing.x[N] =  margin- (int)((margin - treeDrawing.x[N])*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	
	/*....................................................................................................*/
	private void RIGHTevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 1000000;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				RIGHTevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.x[d]<deepest)
					deepest = treeDrawing.x[d];
			}
			treeDrawing.x[N] = deepest - evenVertSpacing;
		}
	}
	/*....................................................................................................*/
	private void RIGHTdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		
		if (N==root) 
			nH=bottom;
		else 
			nH=ancH + (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);
		treeDrawing.x[N]=(int)(nH);
		
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);
	}
	/*_________________________________________________*/
	private void LEFTCalcInternalLocs(TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcInternalLocs(treeDrawing, tree, d);
			int fD = tree.firstDaughterOfNode(N);
			int lD = tree.lastDaughterOfNode(N);
			int nFDx = treeDrawing.x[fD];
			int nFDy = treeDrawing.y[fD];
			int nLDx = treeDrawing.x[lD];
			int nLDy = treeDrawing.y[lD];
			if (lD==fD)   {//only one descendant
				treeDrawing.y[N] = treeDrawing.y[fD];
				treeDrawing.x[N] =treeDrawing.x[fD];
			}
			else {
				treeDrawing.x[N] =(nLDy - nFDy + nLDx + nFDx) / 2;
				treeDrawing.y[N] =(nLDx - nFDx + nLDy + nFDy) / 2;
			}
		}
	}
	
	/*....................................................................................................*/
	private void LEFTCalcTerminalLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N, int margin,  boolean inTriangle, int numInTriangle, int triangleBase) {
		if  (tree.nodeIsTerminal(N)) {   //terminal
			if (inTriangle && tree.numberOfTerminalsInClade(triangleBase)>3){
				if (tree.leftmostTerminalOfNode(triangleBase)==N)
					lastleft+= treeDisplay.getTaxonSpacing(); 
				else {
					//more than 2 in triangle; triangle as wide as 3.  Thus each 
					if (tree.rightmostTerminalOfNode(triangleBase)==N)
						lastleft= treeDrawing.y[tree.leftmostTerminalOfNode(triangleBase)] + 2*treeDisplay.getTaxonSpacing();
					else 
						lastleft+= (treeDisplay.getTaxonSpacing()*2)/(numInTriangle-1);
				}
			}
			else
				lastleft+= treeDisplay.getTaxonSpacing();
			treeDrawing.x[N] = margin;
			treeDrawing.y[N] = lastleft;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (inTriangle)
					LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, true, numInTriangle, triangleBase);
				else
					LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, d, margin, tree.getAssociatedBit(triangleNameRef, d),tree.numberOfTerminalsInClade(d),d);
		}
	}
	/*....................................................................................................*/
	private void LEFTstretchNodeLocs(TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int N) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTstretchNodeLocs(treeDisplay, treeDrawing, tree, d);
		treeDrawing.x[N] = treeDisplay.getTipsMargin() + (int)((treeDrawing.x[N]-treeDisplay.getTipsMargin())*treeDisplay.nodeLocsParameters[stretchfactor]);
	}
	
	/*....................................................................................................*/
	private void LEFTevenNodeLocs(TreeDrawing treeDrawing, Tree tree, int N, int evenVertSpacing) {
		if (tree.nodeIsInternal(N)){
			int deepest = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				LEFTevenNodeLocs(treeDrawing, tree, d, evenVertSpacing);
				if (treeDrawing.x[d]>deepest)
					deepest = treeDrawing.x[d];
			}
			treeDrawing.x[N] = deepest + evenVertSpacing;
		}
	}
	/*....................................................................................................*/
	private void LEFTdoAdjustLengths (TreeDisplay treeDisplay, TreeDrawing treeDrawing, Tree tree, int bottom, int N, double ancH, int root) {
		double nH;
		if (N==root) 
			nH=bottom;
		else 
			nH=ancH - (getNonZeroBranchLength(tree, N)*treeDisplay.nodeLocsParameters[scaling]);
		
		treeDrawing.x[N]=(int)(nH);
		
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, bottom, d, nH, root);
	}
	/*....................................................................................................*/
	private int edgeNode (TreeDrawing treeDrawing, Tree tree, int node, boolean x, boolean max) {
		if (tree.nodeIsTerminal(node)) {
			if (x)
				return treeDrawing.x[node];
			else
				return treeDrawing.y[node];
		}
		int t;
		if (max)
			t = 0;
		else
			t = 1000000;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			int e = edgeNode( treeDrawing, tree, d, x, max);
			if (max && e> t)
				t = e;
			else if (!max && e < t)
				t = e;
		}
		return t;
	}
	/*_________________________________________________*/
	private int propAverage(int xd, int xa, int i, int L){
		return (int)(1.0*i*(xa-xd)/L + xd);
	}
	
	private void placeSingletons (TreeDrawing treeDrawing, Tree tree, int N) {
		if (tree.numberOfDaughtersOfNode(N)==1)	{
			int bD = tree.branchingDescendant(N);
			int bA;
			if (N==tree.getRoot()) {
				bA = tree.getSubRoot();
			}
			else {
				bA = tree.branchingAncestor(N);
				if (bA == tree.getRoot() && tree.numberOfDaughtersOfNode(bA)==1)
					bA = tree.getSubRoot();
			}
			int nA = tree.depthToAncestor(N, bA);
			int nD = tree.depthToAncestor(bD, N);
			//	tree.setAssociatedLong(NameReference.getNameReference("color"), N, 5);
			treeDrawing.x[N]=propAverage(treeDrawing.x[bD], treeDrawing.x[bA], nD, nA+nD);
			treeDrawing.y[N]=propAverage(treeDrawing.y[bD], treeDrawing.y[bA], nD, nA+nD);
		}
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			placeSingletons(treeDrawing, tree, d);
	}
	/*....................................................................................................*/
	private void AdjustForUnbranchedNodes(TreeDrawing treeDrawing, Tree tree, int N, int subRoot) {
		if (tree.nodeIsInternal(N)) { //internal
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				AdjustForUnbranchedNodes(treeDrawing, tree, d, subRoot);
			if (tree.lastDaughterOfNode(N) == tree.firstDaughterOfNode(N)) {  // has only one Daughter
				if (tree.numberOfDaughtersOfNode(tree.motherOfNode(N)) != 1 || tree.motherOfNode(N)==subRoot) { //and is base of chain w 1
					//count length of chain of nodes with only one Daughter
					int count = 2;  // at least 2 in chain
					int q = tree.firstDaughterOfNode(N);
					while (tree.nodeIsInternal(q) && tree.firstDaughterOfNode(q) ==tree.lastDaughterOfNode(q)) {
						count++;
						q = tree.firstDaughterOfNode(q);
					}
					//adjust nodes in chain
					int bottomX =treeDrawing.x[tree.motherOfNode(N)] ;
					int bottomY =treeDrawing.y[tree.motherOfNode(N)] ;
					int topX =treeDrawing.x[N] ;
					int topY =treeDrawing.y[N] ;
					treeDrawing.y[N] = (bottomY+topY)/count;
					treeDrawing.x[N] = (bottomX+topX)/count;
					int count2=1;
					q = tree.firstDaughterOfNode(N);
					while (tree.nodeIsInternal(q) && tree.firstDaughterOfNode(q) ==tree.lastDaughterOfNode(q)) {
						count2++;
						treeDrawing.y[q] = (bottomY+topY)*count2/count;
						treeDrawing.x[q] = (bottomX+topX)*count2/count;
						q = tree.firstDaughterOfNode(q);
					}
				}
			}
		}
	}
	/*....................................................................................................*/
	FontMetrics fm;
	private int findMaxNameLength(Tree tree, int N) {
		if (tree.nodeIsTerminal(N)) {
			String s = tree.getTaxa().getName(tree.taxonNumberOfNode(N));
			if (s==null)
				return 0;
			else
				return fm.stringWidth(s);
		}
		else {
			int max = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int cur = findMaxNameLength(tree, d);
				if (cur>max)
					max = cur;
			}
			return max;
		}
	}
	/*.................................................................................................................*/
	public int effectiveNumberOfTerminals(Tree tree, int node){
		if (tree.nodeIsTerminal(node))
			return 1;
		else if (tree.getAssociatedBit(triangleNameRef, node)) {
			if (tree.numberOfTerminalsInClade(node)>2)
				return 3;
			else 
				return 2;
		}
		int num=0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			num += effectiveNumberOfTerminals(tree, d);
		}
		return num;
	}
	/*.................................................................................................................*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect) { //Graphics g removed as parameter May 02
		if (MesquiteTree.OK(tree)) {
			lastOrientation = treeDisplay.getOrientation();
			//this.treeDisplay = treeDisplay; 
			if (!leaveScaleAlone) {
				treeDisplay.fixedDepthScale = fixedDepth;
				treeDisplay.fixedScalingOn = fixedScale;
			}
			TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
			treeDrawing.namesAngle = namesAngle;
			//this.tree = tree;
			//NodeLocsDrawnExtra extra = null;
			NodeLocsBkgdExtra extraGrid = null;
			if (treeDisplay.getExtras() !=null) {
				if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
					//extra = new NodeLocsDrawnExtra(this, treeDisplay);
					extraGrid = new NodeLocsBkgdExtra(this, treeDisplay);
					//treeDisplay.addExtra(extra);
					treeDisplay.addExtra(extraGrid);
					//extras.addElement(extra);
					extras.addElement(extraGrid);
				}
				else {
					Listable[] mine = treeDisplay.getExtras().myElements(this);
					if (mine !=null && mine.length>0)
						//extra =(NodeLocsDrawnExtra) mine[0];
						extraGrid =(NodeLocsBkgdExtra) mine[0];
				}
			}
			//extra.setTree(tree);
			extraGrid.setTree(tree);
			
			int root = drawnRoot;
			int subRoot = tree.motherOfNode(drawnRoot);
			int buffer = 20;
			
			Graphics g = treeDisplay.getGraphics();
			if (g!=null) {
				if (!treeDisplay.suppressNames) {
					DrawNamesTreeDisplay dtn = treeDisplay.getDrawTaxonNames();
					Font f = null;
					if (dtn!=null)
						f = dtn.getFont();
					
					if (f==null)
						f = g.getFont();
					fm=g.getFontMetrics(f);
					if (fm!=null)
						treeDisplay.setTipsMargin(findMaxNameLength(tree, root) + treeDisplay.getTaxonNameBuffer() + treeDisplay.getTaxonNameDistance());
				}
				else 
					treeDisplay.setTipsMargin(treeDisplay.getTaxonNameBuffer());
				g.dispose();
			}
			
			int marginOffset=0;
			if (resetShowBranchLengths)
				treeDisplay.showBranchLengths=showBranchLengths.getValue();
			else {
				if (treeDisplay.showBranchLengths != showBranchLengths.getValue()) {
					showBranchLengths.setValue(treeDisplay.showBranchLengths);
					if (!showBranchLengths.getValue()) {
						deleteMenuItem(fixedScalingMenuItem);
						deleteMenuItem(showScaleMenuItem);
						mb.deleteScaleMenu();
						if (stretchMenuItem == null)
							stretchMenuItem = addCheckMenuItem(null, "Stretch tree to Fit", makeCommand("stretchToggle", this), stretch);
						if (evenMenuItem == null)
							evenMenuItem = addCheckMenuItem(null, "Even root to tip spacing", makeCommand("toggleEven", this), even);
					}
					else {
						fixedScalingMenuItem = addMenuItem( "Fixed Scaling...", makeCommand("setFixedScaling", this));
						showScaleMenuItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
						mb.addScaleMenu();
						deleteMenuItem(stretchMenuItem);
						stretchMenuItem = null;
						deleteMenuItem(evenMenuItem);
						evenMenuItem = null;
					}
					resetContainingMenuBar();
				}
			}
			if (!compatibleWithOrientation(treeDisplay.getOrientation()))
				setDefaultOrientation(treeDisplay);
			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				treeDisplay.setTaxonSpacing( (rect.width - 30) / effectiveNumberOfTerminals(tree, root));
				if (treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2; //TODO: this causes problems for shrunk, since first taxon doesn't move over enough
				UPCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				UPCalcInternalLocs( treeDrawing, tree, root);
				if (center.getValue())
					UPDOWNCenterInternalLocs( treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				marginOffset = treeDisplay.getTipsMargin() + rect.y;
				treeDrawing.y[subRoot] = (treeDrawing.y[root])+ROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-ROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]= tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						UPdoAdjustLengths( treeDisplay, treeDrawing, tree, rect.height-ROOTSIZE-buffer, root, 0, root);
					}
					else {
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.fixedDepthScale); 
						UPdoAdjustLengths( treeDisplay, treeDrawing, tree, rect.height-ROOTSIZE-(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])+buffer), root, 0, root);
					}
					
					treeDrawing.y[subRoot] = (treeDrawing.y[root])+(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]); //rootsize
					treeDrawing.x[subRoot] = (treeDrawing.x[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((treeDrawing.y[subRoot] - edgeNode(treeDrawing, tree, root, false, false))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							UPevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (stretch.getValue()) {
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.height-treeDisplay.getTipsMargin())) / (treeDrawing.y[subRoot] - (int)treeDisplay.getTipsMargin());
						UPstretchNodeLocs(treeDisplay, treeDrawing, tree, root);
						treeDrawing.y[subRoot]=rect.height-5;
					}
				}
				
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
				treeDisplay.setTaxonSpacing( (rect.width - 30) / effectiveNumberOfTerminals(tree,root));
				if (treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				DOWNCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, rect.height-treeDisplay.getTipsMargin(), tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				DOWNCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					UPDOWNCenterInternalLocs(treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				marginOffset = 0;
				treeDrawing.y[subRoot] = (treeDrawing.y[root])-ROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-ROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin() -buffer - ROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, ROOTSIZE+ buffer, root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					else {
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.height-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.fixedDepthScale); 
						DOWNdoAdjustLengths(treeDisplay, treeDrawing, tree, ROOTSIZE+(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight]) + buffer), root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					
					treeDrawing.y[subRoot] = (treeDrawing.y[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
					treeDrawing.x[subRoot] = (treeDrawing.x[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((- treeDrawing.y[subRoot] + edgeNode(treeDrawing, tree, root, false, true))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							DOWNevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (stretch.getValue()) {
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.height-treeDisplay.getTipsMargin())) / (rect.height - treeDrawing.y[subRoot] - treeDisplay.getTipsMargin());
						DOWNstretchNodeLocs(treeDisplay, treeDrawing, tree, root, rect.height-treeDisplay.getTipsMargin());
						treeDrawing.y[subRoot]=5;
					}
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				treeDisplay.setTaxonSpacing( (rect.height - 30) / effectiveNumberOfTerminals(tree,root));
				if (treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				RIGHTCalcTerminalLocs(treeDisplay, treeDrawing, tree, root, rect.width-treeDisplay.getTipsMargin(), tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				RIGHTCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					RIGHTLEFTCenterInternalLocs( treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				treeDrawing.y[subRoot] = (treeDrawing.y[root])-ROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])-ROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, ROOTSIZE + buffer, root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					else {
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.fixedDepthScale); 
						RIGHTdoAdjustLengths(treeDisplay, treeDrawing, tree, ROOTSIZE+(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight]) + buffer), root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					
					treeDrawing.y[subRoot] = (treeDrawing.y[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
					treeDrawing.x[subRoot] = (treeDrawing.x[root])-(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((-treeDrawing.x[subRoot] +edgeNode(treeDrawing, tree, root, true, true))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							RIGHTevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (stretch.getValue()) {
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.width-treeDisplay.getTipsMargin())) / (rect.width - treeDrawing.x[subRoot] -treeDisplay.getTipsMargin());
						RIGHTstretchNodeLocs(treeDisplay,treeDrawing, tree, root,rect.width-treeDisplay.getTipsMargin());
						treeDrawing.x[subRoot]=5;
					}
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
				treeDisplay.setTaxonSpacing( (rect.height - 30) / effectiveNumberOfTerminals(tree,root));
				if (treeDisplay.getTaxonSpacing()/2*2 != treeDisplay.getTaxonSpacing())  //if odd
					treeDisplay.setTaxonSpacing(treeDisplay.getTaxonSpacing()-1);
				lastleft = -treeDisplay.getTaxonSpacing()/3*2;
				LEFTCalcTerminalLocs(treeDisplay, treeDrawing, tree, root,treeDisplay.getTipsMargin(), tree.getAssociatedBit(triangleNameRef, root), tree.numberOfTerminalsInClade(root), root);
				LEFTCalcInternalLocs(treeDrawing, tree, root);
				if (center.getValue())
					RIGHTLEFTCenterInternalLocs(treeDrawing, tree, root);
				//AdjustForUnbranchedNodes(root, subRoot);
				treeDrawing.y[subRoot] = (treeDrawing.y[root])+ROOTSIZE;
				treeDrawing.x[subRoot] = (treeDrawing.x[root])+ROOTSIZE;
				placeSingletons(treeDrawing, tree, root);
				if (treeDisplay.showBranchLengths) {
					treeDisplay.nodeLocsParameters[totalHeight]=tree.tallestPathAboveNode(root, 1.0);
					if (!treeDisplay.fixedScalingOn) {
						treeDisplay.fixedDepthScale = treeDisplay.nodeLocsParameters[totalHeight];
						fixedDepth = treeDisplay.fixedDepthScale;
						if (treeDisplay.nodeLocsParameters[totalHeight]==0)
							treeDisplay.nodeLocsParameters[scaling]=1;
						else
							treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.nodeLocsParameters[totalHeight]); 
						LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, rect.width - ROOTSIZE -buffer, root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					else {
						treeDisplay.nodeLocsParameters[scaling]=((double)(rect.width-treeDisplay.getTipsMargin()-buffer - ROOTSIZE))/(treeDisplay.fixedDepthScale); 
						LEFTdoAdjustLengths(treeDisplay, treeDrawing, tree, rect.width - ROOTSIZE-(int)(treeDisplay.nodeLocsParameters[scaling]*(treeDisplay.fixedDepthScale-treeDisplay.nodeLocsParameters[totalHeight])+buffer), root, 0, root);
						if (showScale.getValue())
							drawGrid(treeDisplay.nodeLocsParameters[totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[scaling], tree, drawnRoot, treeDisplay, g);
					}
					
					treeDrawing.y[subRoot] = (treeDrawing.y[root])+(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
					treeDrawing.x[subRoot] = (treeDrawing.x[root])+(int)(getNonZeroBranchLength(tree, root)*treeDisplay.nodeLocsParameters[scaling]);
				}
				else {
					if (even.getValue()){
						int evenVertSpacing =(int)((treeDrawing.x[subRoot] - edgeNode(treeDrawing, tree, root, true, false))/ (tree.mostStepsAboveNode(root) + 1));
						if (evenVertSpacing > 0)
							LEFTevenNodeLocs(treeDrawing, tree, root, evenVertSpacing);
					}
					if (stretch.getValue()) {
						treeDisplay.nodeLocsParameters[stretchfactor]=((double)(rect.width-treeDisplay.getTipsMargin())) / (treeDrawing.x[subRoot] - (int)treeDisplay.getTipsMargin());
						LEFTstretchNodeLocs(treeDisplay, treeDrawing, tree, root);
						treeDrawing.x[subRoot]=rect.width-5;
					}
				}
			}
			treeDisplay.scaling=treeDisplay.nodeLocsParameters[scaling];
		}
		
	}
	private void drawString(Graphics g, String s, int x, int y){
		if (g == null || StringUtil.blank(s))
			return;
		try {
			g.drawString(s, x, y);
		}
		catch (Exception e){
		}
	}
	/*.................................................................................................................*/
	public   void drawGrid(double totalTreeHeight, double totalScaleHeight, double scaling, Tree tree, int drawnRoot, TreeDisplay treeDisplay, Graphics g) {
		if (g == null)
			return;
		
		String rulerAgeString = new String();
		double rulerAgeValue = 0,rulerAgeLastValue = 0;
		CharacterState recup = null;
		NameReference colorNameRef = NameReference.getNameReference("color");
		String colorCell = "";
		int posA = 0, posB = 0, color = 0;
		
		boolean rulerOnly = false;
		int rulerWidth = 8;
		Color c=g.getColor();
		g.setColor(Color.cyan);
		TreeDrawing treeDrawing = treeDisplay.getTreeDrawing();
		//g.setXORMode(Color.white);
		int buffer = 8;
		
		// double log10 = Math.log(10.0);
		// double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalScaleHeight)/log10)-1));
		// if (totalScaleHeight/hundredthHeight <20.0)
		//	hundredthHeight /= 10.0;
		
		double nbLines=10*totalScaleHeight;
		while(nbLines>200)nbLines/=10;
		while(nbLines<20)nbLines*=10;
		double hundredthHeight=totalScaleHeight/nbLines;
		
		int countTenths = 0;
		double thisHeight = totalScaleHeight + hundredthHeight;
		
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			
			double base = (totalScaleHeight-totalTreeHeight)*scaling +treeDisplay.getTreeDrawing().y[drawnRoot];
			int leftEdge = treeDisplay.getTreeDrawing().x[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().x[tree.rightmostTerminalOfNode(drawnRoot)];
			
			
			if(adjustScale.getValue() && data != null && fillScale.getValue() && extendScale.getValue() ) {
				for(int it=0; it<data.getNumTaxa(); it++) {
					colorCell = data.getCellObject(colorNameRef,0,it).toString();
					if(colorCell.equals("null")){
						g.setColor(Color.white);
					}
					else {
						color = (new Integer(colorCell)).intValue();
						g.setColor(ColorDistribution.getStandardColor(color));
					}
					
					rulerAgeValue = new Double(data.getCharacterState(recup,1,it).toString()).doubleValue();
					rulerAgeString = data.getTaxa().getTaxonName(it) +"    -    "+data.getCharacterState(recup,0,it).toString();
					rulerAgeLastValue = new Double(data.getCharacterState(recup,2,it).toString()).doubleValue();
					
					posA = (int) (base- ((totalScaleHeight-rulerAgeLastValue)*scaling));
					posB = (int) (base- ((totalScaleHeight-rulerAgeValue)*scaling));
					
					
					g.fillRect(leftEdge, posB, (rightEdge-leftEdge) , posA-posB); // under the tree
					g.fillRect(rightEdge + 5*buffer, posB, treeDisplay.getWidth() - buffer , posA-posB); // Right
					
					if((posA - posB) >= 15){
						if(g.getColor().equals(Color.black))
							g.setColor(Color.white);
						else
							g.setColor(Color.black);
						drawString(g, rulerAgeString, rightEdge + buffer*6, (posA+posB)/2);
						
						if(g.getColor().equals(Color.white))
							g.setColor(Color.black);
						if(!showScale.getValue()) {
							drawString(g, (""+rulerAgeValue), rightEdge + buffer, posB);
							drawString(g, (""+rulerAgeLastValue), rightEdge + buffer, posA);
						}
					}
				}
				
			}
			
			/*-----------------------------------------------------------------------------*/
			
			while ( thisHeight>0) {
				
				thisHeight -= hundredthHeight;
				
				if(showScale.getValue()) {
					
					if (countTenths % 10 == 0)
						g.setColor(Color.blue);
					else
						g.setColor(Color.cyan);
					
					if (rulerOnly){
						
						g.drawLine(rightEdge-rulerWidth, (int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)));
					}
					else{
						
						g.drawLine(leftEdge, (int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)));
					}
					
					if (countTenths % 10 == 0){
						
						drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight-thisHeight, totalScaleHeight), rightEdge + buffer, (int)(base- (thisHeight*scaling)));
					}
					countTenths ++;
				}
				
				if (rulerOnly)
					g.drawLine(rightEdge, (int)(base), rightEdge,  (int)(base- (totalScaleHeight*scaling)));
			}
			
			/*-----------------------------------------------------------------------------*/
			
			if(adjustScale.getValue() && data != null && (!fillScale.getValue() || (fillScale.getValue() && !extendScale.getValue()))) {
				for(int it=0; it<data.getNumTaxa(); it++) {
					colorCell = data.getCellObject(colorNameRef,0,it).toString();
					if(colorCell.equals("null")){
						g.setColor(Color.white);
					}
					else {
						color = (new Integer(colorCell)).intValue();
						g.setColor(ColorDistribution.getStandardColor(color));
					}
					
					rulerAgeValue = new Double(data.getCharacterState(recup,1,it).toString()).doubleValue();
					rulerAgeString = data.getTaxa().getTaxonName(it) +" - "+data.getCharacterState(recup,0,it).toString();
					rulerAgeLastValue = new Double(data.getCharacterState(recup,2,it).toString()).doubleValue();
					
					
					posA = (int) (base- ((totalScaleHeight-rulerAgeLastValue)*scaling));
					posB = (int) (base- ((totalScaleHeight-rulerAgeValue)*scaling));
					
					
					
					if(extendScale.getValue()) {					
						g.drawLine(leftEdge, posA, rightEdge, posA); // under the tree
						g.drawLine(leftEdge, posB, rightEdge, posB);
					}
					if(!fillScale.getValue()) {
						g.drawLine(rightEdge + 3*buffer, posA, treeDisplay.getWidth() - buffer , posA); // Right
						g.drawLine(rightEdge + 3*buffer, posB, treeDisplay.getWidth() - buffer , posB);
					}
					else
						g.fillRect(rightEdge + 5*buffer, posB, treeDisplay.getWidth() - buffer , posA-posB); // Right
					
					if((posA - posB) >= 15){
						if(fillScale.getValue()) {
							if(g.getColor().equals(Color.black))
								g.setColor(Color.white);
							else
								g.setColor(Color.black);
						}
						drawString(g, rulerAgeString, rightEdge + buffer*5, (posA+posB)/2);
						
						if(g.getColor().equals(Color.white))
							g.setColor(Color.black);
						if(!showScale.getValue()) {
							drawString(g, (""+rulerAgeValue), rightEdge + buffer, posB);
							drawString(g, (""+rulerAgeLastValue), rightEdge + buffer, posA);
						}	
					}
				}
			}
			
			
			
			
			/*__________________________________________________________________________________________________________________________*/			
			
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			int leftEdge = treeDisplay.getTreeDrawing().x[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().x[tree.rightmostTerminalOfNode(drawnRoot)];
			double base = treeDrawing.y[drawnRoot];
			if (fixedScale)
				base += (totalTreeHeight - fixedDepth)*scaling;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(Color.blue);
				else
					g.setColor(Color.cyan);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine(rightEdge-rulerWidth, (int)(base+ (thisHeight*scaling)), rightEdge,  (int)(base+ (thisHeight*scaling)));
				else
					g.drawLine(leftEdge, (int)(base+ (thisHeight*scaling)), rightEdge,  (int)(base+ (thisHeight*scaling)));
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), rightEdge + buffer, (int)(base+ (thisHeight*scaling)));
				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine(rightEdge, (int)(base), rightEdge,  (int)(base+ (totalScaleHeight*scaling)));
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			fm=g.getFontMetrics(g.getFont());
			int textHeight = fm.getHeight();
			int leftEdge = treeDisplay.getTreeDrawing().y[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)];
			
			//if fixed then base is centered on root!
			double base = (totalScaleHeight-totalTreeHeight)*scaling +treeDisplay.getTreeDrawing().x[drawnRoot];
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(Color.blue);
				else
					g.setColor(Color.cyan);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine((int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)),  rightEdge-rulerWidth);
				else
					g.drawLine((int)(base- (thisHeight*scaling)), rightEdge,  (int)(base- (thisHeight*scaling)),  leftEdge);
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (int)(base- (thisHeight*scaling)), rightEdge + buffer + textHeight);
				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine((int)(base), rightEdge, (int)(base- (totalScaleHeight*scaling)),rightEdge);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			fm=g.getFontMetrics(g.getFont());
			int textHeight = fm.getHeight();
			int leftEdge = treeDisplay.getTreeDrawing().y[tree.leftmostTerminalOfNode(drawnRoot)];
			int rightEdge = treeDisplay.getTreeDrawing().y[tree.rightmostTerminalOfNode(drawnRoot)];
			double base = treeDrawing.x[drawnRoot];
			if (fixedScale)
				base += (totalTreeHeight - fixedDepth)*scaling;
			while ( thisHeight>=0) {
				if (countTenths % 10 == 0)
					g.setColor(Color.blue);
				else
					g.setColor(Color.cyan);
				thisHeight -= hundredthHeight;
				if (rulerOnly)
					g.drawLine((int)(base+ (thisHeight*scaling)), rightEdge-rulerWidth,  (int)(base+ (thisHeight*scaling)),  rightEdge);
				else
					g.drawLine((int)(base+ (thisHeight*scaling)), leftEdge,  (int)(base+ (thisHeight*scaling)),  rightEdge);
				if (countTenths % 10 == 0)
					drawString(g, MesquiteDouble.toStringInRange(totalScaleHeight - thisHeight, totalScaleHeight), (int)(base+ (thisHeight*scaling)), rightEdge + buffer + textHeight);
				countTenths ++;
			}
			if (rulerOnly)
				g.drawLine((int)(base), rightEdge, (int)(base+ (totalScaleHeight*scaling)),rightEdge);
		}
		if (c !=null)
			g.setColor(c);
		g.setPaintMode();
	}
}


class NodeLocsBkgdExtra extends TreeDisplayBkgdExtra {
	NodeLocsPaleo locsModule;
	
	public NodeLocsBkgdExtra (NodeLocsPaleo ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
	}
	/*.................................................................................................................*/
	public   String writeOnTree(Tree tree, int drawnRoot) {
		return null;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (locsModule.showBranchLengths.getValue())
			locsModule.drawGrid(treeDisplay.nodeLocsParameters[NodeLocsPaleo.totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[NodeLocsPaleo.scaling], tree, drawnRoot, treeDisplay, g);
			//locsModule.drawGrid(treeDisplay.nodeLocsParameters[locsModule.totalHeight], treeDisplay.fixedDepthScale, treeDisplay.nodeLocsParameters[locsModule.scaling], tree, drawnRoot, treeDisplay, g);
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	
}


