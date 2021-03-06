/* Code for stratigraphic tools package (http://mesquiteproject.org/... ).
Copyright 2005 by S�bastien Josse, Thomas Moreau and Michel Laurin.
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
package mesquite.stratigraphictools.STDiagonalTree;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stratigraphictools.NodeLocsPaleo.*;

/** Draws trees in a basic diagonal-branch style.  See SquareTree and others in mesquite.basic and mesquite.ornamental. */
public class STDiagonalTree extends DrawTree {

	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName;
	Vector drawings;
	int oldEdgeWidth =12;
	int ornt;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsPaleo)hireEmployee(NodeLocsPaleo.class, "Calculator of node locations");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node locator module obtained");
		drawings = new Vector();
 		MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
		ornt = nodeLocsTask.getDefaultOrientation();
		orientationName = new MesquiteString("Up");
		if (ornt != TreeDisplay.UP &&  ornt != TreeDisplay.DOWN && ornt != TreeDisplay.LEFT && ornt != TreeDisplay.RIGHT)
			ornt = TreeDisplay.UP;
		orientationName.setValue(orient(ornt));
 		orientationSubmenu.setSelected(orientationName);
 		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
 		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
 		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
 		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));
 		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		return true;
 	 }
 	 public void employeeQuit(MesquiteModule m){
 	 	iQuit();
 	 }
  	 
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		DiagonalTreeDrawing treeDrawing =  new DiagonalTreeDrawing (treeDisplay, numTaxa, this);
		if (legalOrientation(treeDisplay.getOrientation())){
			orientationName.setValue(orient(treeDisplay.getOrientation()));
			ornt = treeDisplay.getOrientation();
		}
		else
			treeDisplay.setOrientation(ornt);
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	public boolean legalOrientation (int orientation){
		return (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN || orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT);
	}
	/*.................................................................................................................*/
 	public void endJob() {
 		nodeLocsTask= null;
 		drawings.removeAllElements();
 		super.endJob();
   	 }
   
	/*.................................................................................................................*/
	public String orient (int orientation){
		if (orientation == TreeDisplay.UP)
			return "Up";
		else if (orientation == TreeDisplay.DOWN)
			return "Down";
		else if (orientation == TreeDisplay.RIGHT)
			return "Right";
		else if (orientation == TreeDisplay.LEFT)
			return "Left";
		else return "other";
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setEdgeWidth " + oldEdgeWidth); 
  	 	if (ornt== TreeDisplay.UP)
  	 		temp.addLine("orientUp"); 
  	 	else if (ornt== TreeDisplay.DOWN)
  	 		temp.addLine("orientDown"); 
  	 	else if (ornt== TreeDisplay.LEFT)
  	 		temp.addLine("orientLeft"); 
  	 	else if (ornt== TreeDisplay.RIGHT)
  	 		temp.addLine("orientRight"); 
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets thickness of lines used to draw tree", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, 99);
    	 		if (newWidth>0 && newWidth<100 && newWidth!=oldEdgeWidth) {
    	 			oldEdgeWidth=newWidth;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
    	 				treeDrawing.setEdgeWidth(newWidth);
    					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(newWidth, 5); //better if only did this if tracing on
    				}
				if (!MesquiteThread.isScripting()) parametersChanged();
    	 		}
    	 		
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
    	 		return nodeLocsTask;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.UP;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.UP);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.DOWN;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.DOWN);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.RIGHT;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.RIGHT);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			Enumeration e = drawings.elements();
			ornt =TreeDisplay.LEFT;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.LEFT);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
    	 	}
    	 	else {
 			return  super.doCommand(commandName, arguments, checker);
 		}
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Diagonal tree with stratigraphic tools";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Draws trees with standard diagonal branches (\"cladogram\") allowing to use stratigraphic tools." ;
   	 }
	/*.................................................................................................................*/
}

/* ======================================================================== */

/* ======================================================================== */
class DiagonalTreeDrawing extends TreeDrawing  {
	public Polygon[] branchPoly;
	public Polygon[] fillBranchPoly;

	private int lastleft;
	private int taxspacing;
	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public STDiagonalTree ownerModule;
	public int edgeWidth = 12;
	public int preferredEdgeWidth = 12;
	int oldNumTaxa = 0;
	Polygon utilityPolygon;
 	public static final int inset=1;
	private boolean ready=false;

	private int foundBranch;
	NameReference triangleNameRef;
	NameReference widthNameReference;
	DoubleArray widths = null;
	double maxWidth = 0;
	public DiagonalTreeDrawing (TreeDisplay treeDisplay, int numTaxa, STDiagonalTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		widthNameReference = NameReference.getNameReference("width");
	    	treeDisplay.setMinimumTaxonNameDistance(edgeWidth, 5); //better if only did this if tracing on
		triangleNameRef = NameReference.getNameReference("triangled");
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		ready = true;
		utilityPolygon=new Polygon();
		utilityPolygon.xpoints = new int[16];
		utilityPolygon.ypoints = new int[16];
		utilityPolygon.npoints=16;
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Polygon[numNodes];
		fillBranchPoly= new Polygon[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Polygon();
			branchPoly[i].xpoints = new int[16];
			branchPoly[i].ypoints = new int[16];
			branchPoly[i].npoints=16;
			fillBranchPoly[i] = new Polygon();
			fillBranchPoly[i].xpoints = new int[16];
			fillBranchPoly[i].ypoints = new int[16];
			fillBranchPoly[i].npoints=16;
		}
	}
	int branchEdgeWidth(int node){
		if (widths !=null && maxWidth!=0 && MesquiteDouble.isCombinable(maxWidth)) {
			double w = widths.getValue(node);
			if (MesquiteDouble.isCombinable(w))
				return (int)((w/maxWidth) * edgeWidth);
		}	
		return edgeWidth;
	}
	private boolean isUP(){
		return treeDisplay.getOrientation()==TreeDisplay.UP;
	}
	private boolean isDOWN(){
		return treeDisplay.getOrientation()==TreeDisplay.DOWN;
	}
	private boolean isLEFT(){
		return treeDisplay.getOrientation()==TreeDisplay.LEFT;
	}
	private boolean isRIGHT(){
		return treeDisplay.getOrientation()==TreeDisplay.RIGHT;
	}
	/*_________________________________________________*/
	private void UPdefineFillPoly(int node, Polygon poly, boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		if (poly!=null) {
			int sliceWidth=branchEdgeWidth(node);
			if (numSlices>1) {
				Nx+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
				mNx+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
				sliceWidth=(branchEdgeWidth(node)-inset)-((sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices);
			}
			if ((internalNode) && (numSlices==1)){ 
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny);
				poly.addPoint(Nx+sliceWidth/2, Ny-sliceWidth/2-inset);
				poly.addPoint(Nx+sliceWidth-inset, Ny);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx+inset, Ny);
				poly.npoints=6;
			}
			else {
				if (Nx==mNx) {
					if ((internalNode) && (numSlices>1)) {
						Ny-=(branchEdgeWidth(node)-inset)/4;
					}
					poly.npoints=0;
					poly.addPoint(Nx+inset, Ny+inset);
					poly.addPoint(Nx+sliceWidth-inset, Ny+inset);
					poly.addPoint(mNx+sliceWidth-inset, mNy);
					poly.addPoint(mNx+inset, mNy);
					poly.addPoint(Nx+inset, Ny+inset);
					poly.npoints=5;
				}
				else if (Nx>mNx) {
					if ((internalNode) && (numSlices>1)) {
						Nx+=(branchEdgeWidth(node)-inset)/4;
						Ny-=(branchEdgeWidth(node)-inset)/4;
					}
					poly.npoints=0;
					poly.addPoint(Nx, Ny+inset);
					poly.addPoint(Nx+sliceWidth-inset-inset, Ny+inset);
					poly.addPoint(mNx+sliceWidth-inset, mNy);
					poly.addPoint(mNx+inset, mNy);
					poly.addPoint(Nx, Ny+inset);
					poly.npoints=5;
				}
				else if (Nx<mNx) {
					if ((internalNode) && (numSlices>1)) {
						Nx-=(branchEdgeWidth(node)-inset)/4;
						Ny-=(branchEdgeWidth(node)-inset)/4;
					}
					poly.npoints=0;
					poly.addPoint(Nx+inset+inset, Ny+inset);
					poly.addPoint(Nx+sliceWidth, Ny+inset);
					poly.addPoint(mNx+sliceWidth-inset, mNy);
					poly.addPoint(mNx+inset, mNy);
					poly.addPoint(Nx+inset+inset, Ny+inset);
					poly.npoints=5;
				}
			}
		}
	}
	/*_________________________________________________*/
	private void UPCalcfillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPCalcfillBranchPolys(tree, d);
		UPdefineFillPoly(node, fillBranchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void UPdefinePoly(int node, Polygon poly, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			if (internalNode)  {
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node)/2, Ny-branchEdgeWidth(node)/2);//Ny+branchEdgeWidth(node)/2 for down
				poly.addPoint(Nx+branchEdgeWidth(node), Ny);
				poly.addPoint(mNx+branchEdgeWidth(node), mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else {
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node), Ny);
				poly.addPoint(mNx+branchEdgeWidth(node), mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void UPCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcBranchPolys(tree, d);
			UPdefinePoly(node, branchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)]);
		}
		else {
				Polygon poly = branchPoly[node];
				poly.npoints=0;
				int mN = tree.motherOfNode(node);
				int leftN = tree.leftmostTerminalOfNode(node);
				int rightN = tree.rightmostTerminalOfNode(node);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.addPoint((int)x[leftN],  (int)y[leftN]);
				poly.addPoint((int)x[rightN]+branchEdgeWidth(node),  (int)y[rightN]);
				poly.addPoint((int)x[node]+branchEdgeWidth(node),  (int)y[node]);
				poly.addPoint((int)x[mN]+branchEdgeWidth(node),  (int)y[mN]);
				poly.addPoint((int)x[mN],  (int)y[mN]);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void DOWNdefineFillPoly(int node, Polygon poly, boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=branchEdgeWidth(node);
		if (numSlices>1) {
			Nx+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
			mNx+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
			sliceWidth=(branchEdgeWidth(node)-inset)-((sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices);
		}
		if ((internalNode) && (numSlices==1)){ 
			poly.npoints=0;
			poly.addPoint(Nx+inset, Ny);
			poly.addPoint(Nx+sliceWidth/2, Ny+sliceWidth/2+inset);
			poly.addPoint(Nx+sliceWidth-inset, Ny);
			poly.addPoint(mNx+sliceWidth-inset, mNy);
			poly.addPoint(mNx+inset, mNy);
			poly.addPoint(Nx+inset, Ny);
			poly.npoints=6;
		}
		else {
			if (Nx==mNx) {
				if ((internalNode) && (numSlices>1)) {
					Ny+=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny-inset);
				poly.addPoint(Nx+sliceWidth-inset, Ny-inset);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx+inset, Ny-inset);
				poly.npoints=5;
			}
			else if (Nx>mNx) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node)-inset)/4;
					Ny+=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx, Ny-inset);
				poly.addPoint(Nx+sliceWidth-inset-inset, Ny-inset);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx, Ny-inset);
				poly.npoints=5;
			}
			else if (Nx<mNx) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node)-inset)/4;
					Ny+=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset+inset, Ny-inset);
				poly.addPoint(Nx+sliceWidth, Ny-inset);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx+inset+inset, Ny-inset);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void DOWNCalcfillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNCalcfillBranchPolys(tree, d);
		DOWNdefineFillPoly(node, fillBranchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void DOWNdefinePoly(int node, Polygon poly, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (internalNode) 
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx+branchEdgeWidth(node)/2, Ny+branchEdgeWidth(node)/2);
			poly.addPoint(Nx+branchEdgeWidth(node), Ny);
			poly.addPoint(mNx+branchEdgeWidth(node), mNy);
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=6;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx+branchEdgeWidth(node), Ny);
			poly.addPoint(mNx+branchEdgeWidth(node), mNy);
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=5;
			}
	}
	/*_________________________________________________*/
	private void DOWNCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcBranchPolys(tree, d);
			DOWNdefinePoly(node, branchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)]);
		}
		else {
				Polygon poly = branchPoly[node];
				poly.npoints=0;
				int mN = tree.motherOfNode(node);
				int leftN = tree.leftmostTerminalOfNode(node);
				int rightN = tree.rightmostTerminalOfNode(node);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.addPoint((int)x[leftN],  (int)y[leftN]);
				poly.addPoint((int)x[rightN]+branchEdgeWidth(node),  (int)y[rightN]);
				poly.addPoint((int)x[node]+branchEdgeWidth(node),  (int)y[node]);
				poly.addPoint((int)x[mN]+branchEdgeWidth(node),  (int)y[mN]);
				poly.addPoint((int)x[mN],  (int)y[mN]);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void RIGHTdefineFillPoly(int node, Polygon poly,  boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=branchEdgeWidth(node);
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
			mNy+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
			sliceWidth=(branchEdgeWidth(node)-inset)-((sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices);
		}
		if ((internalNode) && (numSlices==1)){ 
			poly.npoints=0;
			poly.addPoint(Nx, Ny+inset);
			poly.addPoint(Nx+sliceWidth/2+inset, Ny+sliceWidth/2);
			poly.addPoint(Nx, Ny+sliceWidth-inset);
			poly.addPoint(mNx, mNy+sliceWidth-inset);
			poly.addPoint(mNx, mNy+inset);
			poly.addPoint(Nx, Ny+inset);
			poly.npoints=6;
		}
		else {
			if (Ny==mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx-inset, Ny+inset);
				poly.addPoint(Nx-inset, Ny+sliceWidth-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx-inset, Ny+inset);
				poly.npoints=5;
			}
			else if (Ny>mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node)-inset)/4;
					Ny+=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx-inset, Ny);
				poly.addPoint(Nx-inset, Ny+sliceWidth-inset-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx-inset, Ny);
				poly.npoints=5;
			}
			else if (Ny<mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node)-inset)/4;
					Ny-=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx-inset, Ny+inset+inset);
				poly.addPoint(Nx-inset, Ny+sliceWidth);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx-inset, Ny+inset+inset);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void RIGHTCalcfillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTCalcfillBranchPolys(tree, d);
		RIGHTdefineFillPoly(node, fillBranchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void RIGHTdefinePoly(int node, Polygon poly, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (internalNode) 
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx+branchEdgeWidth(node)/2, Ny+branchEdgeWidth(node)/2);
			poly.addPoint(Nx, Ny+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=6;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx, Ny+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=5;
			}
	}
	/*_________________________________________________*/
	private void RIGHTCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcBranchPolys(tree, d);
			RIGHTdefinePoly(node, branchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)]);
		}
		else {
				Polygon poly = branchPoly[node];
				poly.npoints=0;
				int mN = tree.motherOfNode(node);
				int leftN = tree.leftmostTerminalOfNode(node);
				int rightN = tree.rightmostTerminalOfNode(node);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.addPoint((int)x[leftN],  (int)y[leftN]);
				poly.addPoint((int)x[rightN],  (int)y[rightN]+branchEdgeWidth(node));
				poly.addPoint((int)x[node],  (int)y[node]+branchEdgeWidth(node));
				poly.addPoint((int)x[mN],  (int)y[mN]+branchEdgeWidth(node));
				poly.addPoint((int)x[mN],  (int)y[mN]);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void LEFTdefineFillPoly(int node, Polygon poly,  boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=branchEdgeWidth(node);
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*((branchEdgeWidth(node)-inset)-inset-inset)/numSlices;
			mNy+= (sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices;
			sliceWidth=(branchEdgeWidth(node)-inset)-((sliceNumber-1)*(branchEdgeWidth(node)-inset)/numSlices);
		}
		if ((internalNode) && (numSlices==1)){ 
			poly.npoints=0;
			poly.addPoint(Nx, Ny+inset);
			poly.addPoint(Nx-sliceWidth/2-inset, Ny+sliceWidth/2);
			poly.addPoint(Nx, Ny+sliceWidth-inset);
			poly.addPoint(mNx, mNy+sliceWidth-inset);
			poly.addPoint(mNx, mNy+inset);
			poly.addPoint(Nx, Ny+inset);
			poly.npoints=6;
		}
		else {
			if (Ny==mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny+inset);
				poly.addPoint(Nx+inset, Ny+sliceWidth-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx+inset, Ny+inset);
				poly.npoints=5;
			}
			else if (Ny>mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node)-inset)/4;
					Ny+=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny);
				poly.addPoint(Nx+inset, Ny+sliceWidth-inset-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx+inset, Ny);
				poly.npoints=5;
			}
			else if (Ny<mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node)-inset)/4;
					Ny-=(branchEdgeWidth(node)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny+inset+inset);
				poly.addPoint(Nx+inset, Ny+sliceWidth);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx+inset, Ny+inset+inset);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void LEFTCalcfillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTCalcfillBranchPolys(tree, d);
		LEFTdefineFillPoly(node, fillBranchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void LEFTdefinePoly(int node, Polygon poly, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (internalNode) 
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx-branchEdgeWidth(node)/2, Ny+branchEdgeWidth(node)/2);
			poly.addPoint(Nx, Ny+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=6;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx, Ny+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy+branchEdgeWidth(node));
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=5;
			}
	}
	/*_________________________________________________*/
	private void LEFTCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcBranchPolys(tree, d);
			LEFTdefinePoly(node, branchPoly[node], tree.nodeIsInternal(node), (int)x[node], (int) (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)]);
		}
		else {
				Polygon poly = branchPoly[node];
				poly.npoints=0;
				int mN = tree.motherOfNode(node);
				int leftN = tree.leftmostTerminalOfNode(node);
				int rightN = tree.rightmostTerminalOfNode(node);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.addPoint((int)x[leftN],  (int)y[leftN]);
				poly.addPoint((int)x[rightN],  (int)y[rightN]+branchEdgeWidth(node));
				poly.addPoint((int)x[node],  (int)y[node]+branchEdgeWidth(node));
				poly.addPoint((int)x[mN],  (int)y[mN]+branchEdgeWidth(node));
				poly.addPoint((int)x[mN],  (int)y[mN]);
				poly.addPoint((int)x[node],  (int)y[node]);
				poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]= (int)y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]= (int)y[tree.motherOfNode(node)];
		lineBaseX[node]=x[tree.motherOfNode(node)];
	}
	/*_________________________________________________*/
	private void calcBranchPolys(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}
		
		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField());  //Graphics g removed as parameter May 02

		calculateLines(tree, drawnRoot);
		edgeWidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgeWidth+2) {
			edgeWidth= treeDisplay.getTaxonSpacing()-2;
			if (edgeWidth<2)
				edgeWidth=2;
		}
		treeDisplay.setMinimumTaxonNameDistance(edgeWidth, 5);
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			UPCalcBranchPolys(tree, drawnRoot);
			UPCalcfillBranchPolys(tree, drawnRoot);
		}

		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
			DOWNCalcBranchPolys(tree, drawnRoot);
			DOWNCalcfillBranchPolys(tree, drawnRoot);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			RIGHTCalcBranchPolys(tree, drawnRoot);
			RIGHTCalcfillBranchPolys(tree, drawnRoot);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
			LEFTCalcBranchPolys(tree, drawnRoot);
			LEFTCalcfillBranchPolys(tree, drawnRoot);
		}
	}
	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(Color.blue);
		if (treeDisplay.getOrientation()==TreeDisplay.DOWN || treeDisplay.getOrientation()==TreeDisplay.UP){
			for (int i=0; i<4; i++)
				g.drawLine((int)x[node]-2 - i,  (int)y[node], (int)x[tree.motherOfNode(node)]-2 - i,  (int)y[tree.motherOfNode(node)]);
		}
		else {
			for (int i=0; i<4; i++)
				g.drawLine((int)x[node],  (int)y[node]-2 - i, (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)]-2 - i);
		}
		g.setColor(tC);
	}
	/*_________________________________________________*/
	private boolean ancestorIsTriangled(Tree tree, int node) {
		if (tree.getAssociatedBit(triangleNameRef, tree.motherOfNode(node)))
			return true;
		if (tree.getRoot() == node || tree.getSubRoot() == node)
			return false;
		return ancestorIsTriangled(tree, tree.motherOfNode(node));
	}
	Color tC;
	/*_________________________________________________*/
	private   void drawBranches(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			//g.setColor(Color.black);//for testing
			g.setColor(treeDisplay.getBranchColor(node));
			if ((tree.getRooted() || tree.getRoot()!=node) && branchPoly[node]!=null) {
				g.fillPolygon(branchPoly[node]);
				if (tree.numberOfParentsOfNode(node)>1) {
					for (int i=1; i<=tree.numberOfParentsOfNode(node); i++) {
						int anc =tree.parentOfNode(node, i);
						if (anc!= tree.motherOfNode(node)) {
							g.drawLine((int)x[node], (int) (int)y[node], (int)x[tree.parentOfNode(node, i)], (int) (int)y[tree.parentOfNode(node, i)]);
							g.drawLine((int)x[node]+1, (int) (int)y[node], (int)x[tree.parentOfNode(node, i)]+1, (int) (int)y[tree.parentOfNode(node, i)]);
							g.drawLine((int)x[node], (int) (int)y[node]+1, (int)x[tree.parentOfNode(node, i)], (int) (int)y[tree.parentOfNode(node, i)]+1);
							g.drawLine((int)x[node]+1, (int) (int)y[node]+1, (int)x[tree.parentOfNode(node, i)]+1, (int) (int)y[tree.parentOfNode(node, i)]+1);
						}
					}
				}
			}
			if (tree.getAssociatedBit(triangleNameRef,node)) {
				if (treeDisplay.getOrientation()==TreeDisplay.UP) {
					/*g.setColor(Color.red);
					for (int i=0; i<edgeWidth; i++) 
						g.drawLine((int)x[node]+i, (int) (int)y[node], (int)x[tree.rightmostTerminalOfNode(node)]+i, (int) (int)y[tree.rightmostTerminalOfNode(node)]);
					
					g.setColor(Color.blue);
					for (int i=0; i<edgeWidth; i++)
						g.drawLine((int)x[node]+i, (int) (int)y[node], (int)x[tree.leftmostTerminalOfNode(node)]+i, (int) (int)y[tree.leftmostTerminalOfNode(node)]);
					
						g.setColor(Color.green);
					for (int i=0; i<edgeWidth*0.71; i++) {
						g.drawLine((int)x[tree.leftmostTerminalOfNode(node)]+i, (int) (int)y[tree.leftmostTerminalOfNode(node)]+i, (int)x[tree.rightmostTerminalOfNode(node)]-i, (int) (int)y[tree.rightmostTerminalOfNode(node)]+i);
					}*/
				}
				else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
					/*g.setColor(Color.blue);
					for (int i=0; i<edgeWidth; i++) {
						g.drawLine((int)x[node]+i, (int) (int)y[node], (int)x[tree.leftmostTerminalOfNode(node)]+i, (int) (int)y[tree.leftmostTerminalOfNode(node)]);
						g.drawLine((int)x[tree.leftmostTerminalOfNode(node)]+i, (int) (int)y[tree.leftmostTerminalOfNode(node)]-i, (int)x[tree.rightmostTerminalOfNode(node)]-i, (int) (int)y[tree.rightmostTerminalOfNode(node)]-i);
						g.drawLine((int)x[node]+i, (int) (int)y[node], (int)x[tree.rightmostTerminalOfNode(node)]+i, (int) (int)y[tree.rightmostTerminalOfNode(node)]);
					}
					*/
				}
			/*	for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					g.drawLine((int)x[node]+i, (int) (int)y[node]+j, (int)x[tree.leftmostTerminalOfNode(node)]+i, (int) (int)y[tree.leftmostTerminalOfNode(node)]+j);
					g.drawLine((int)x[tree.leftmostTerminalOfNode(node)]+i, (int) (int)y[tree.leftmostTerminalOfNode(node)]+j, (int)x[tree.rightmostTerminalOfNode(node)]+i, (int) (int)y[tree.rightmostTerminalOfNode(node)]+j);
					g.drawLine((int)x[node]+i, (int) (int)y[node]+j, (int)x[tree.rightmostTerminalOfNode(node)]+i, (int) (int)y[tree.rightmostTerminalOfNode(node)]+j);
				}*/
			}

			if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawBranches( tree, g, d);

			if (emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(nodePoly(node));
				g.setColor(prev);
			}
}
	}
	/*_________________________________________________*/
	private double findMaxWidth(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			if (tree.nodeIsTerminal(node))
				return widths.getValue(node);
				
			double mw = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				mw = MesquiteDouble.maximum(mw, findMaxWidth(tree, d));
			return mw;
		}
		return (MesquiteDouble.unassigned);
	}
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
	        if (MesquiteTree.OK(tree)) {
	        	
	        	if (!tree.nodeExists(getDrawnRoot()))
	        		setDrawnRoot(tree.getRoot());
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	widths = tree.getWhichAssociatedDouble(widthNameReference);
	        	if (widths!=null)
	        		maxWidth = findMaxWidth(tree, getDrawnRoot());
	        	calcBranchPolys(tree, getDrawnRoot());
	        	
		}
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
	        if (MesquiteTree.OK(tree)) {
	        	//if (tree.getNumNodeSpaces()!=numNodes)
	        	//	resetNumNodes(tree.getNumNodeSpaces());
	        	g.setColor(treeDisplay.branchColor);
	       	 	drawBranches(tree, g, drawnRoot);  
	       	 }
	   }
	
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		int ew = branchEdgeWidth(node)-2;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) 
			box = new Rectangle((int)x[node],  (int)y[node]-ew-3, ew, ew);
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			box = new Rectangle((int)x[node],  (int)y[node]+2, ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) 
			box = new Rectangle((int)x[node]+1,  (int)y[node], ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT)
			box = new Rectangle((int)x[node]-ew-3,  (int)y[node], ew, ew);
		else 
			box = new Rectangle((int)x[node],  (int)y[node], ew, ew);
		g.fillRect(box.x, box.y, box.width, box.height);
		g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}

	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle box;
		int ew = branchEdgeWidth(node)-2;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) 
			box = new Rectangle((int)x[node],  (int)y[node]-ew-3, ew, ew);
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			box = new Rectangle((int)x[node],  (int)y[node]+2, ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) 
			box = new Rectangle((int)x[node]+1,  (int)y[node], ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT)
			box = new Rectangle((int)x[node]-ew-3,  (int)y[node], ew, ew);
		else 
			box = new Rectangle((int)x[node],  (int)y[node], ew, ew);
		for (int i=0; i<colors.getNumColors(); i++) {
			Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
			g.fillRect(box.x + (i*box.width/colors.getNumColors()), box.y, box.width-  (i*box.width/colors.getNumColors()), box.height);
		}
		g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}
	/*_________________________________________________*/
	public  int findTerminalBox(Tree tree, int drawnRoot, int x, int y){
		return -1;
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			int numColors = colors.getNumColors();
			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				for (int i=0; i<numColors; i++) {
					UPdefineFillPoly(node, utilityPolygon, tree.nodeIsInternal(node), (int)x[node],  (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
				for (int i=0; i<numColors; i++) {
					DOWNdefineFillPoly(node, utilityPolygon, tree.nodeIsInternal(node), (int)x[node],  (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				for (int i=0; i<numColors; i++) {
					RIGHTdefineFillPoly(node, utilityPolygon, tree.nodeIsInternal(node), (int)x[node],  (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
				for (int i=0; i<numColors; i++) {
					LEFTdefineFillPoly(node, utilityPolygon, tree.nodeIsInternal(node), (int)x[node],  (int)y[node], (int)x[tree.motherOfNode(node)],  (int)y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			g.setColor(treeDisplay.getBranchColor(node));
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (fillBranchPoly[node] !=null && node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			g.fillPolygon(fillBranchPoly[node]);
		}
	}
	   
	/*_________________________________________________*/
	public Polygon nodePoly(int node) {
		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		int startX =0;
		int startY =0;
		if (treeDisplay.getOrientation()==TreeDisplay.UP || treeDisplay.getOrientation()==TreeDisplay.DOWN){
			startX = (int)x[node]+halfNodeWidth-offset;
			startY=  (int)y[node] -halfNodeWidth;
		}	else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT){
			startX = (int)x[node];
			startY=  (int)y[node]-offset;
		}
		Polygon poly = new Polygon();
		poly.npoints=0;
		poly.addPoint(startX,startY);
		poly.addPoint(startX+halfNodeWidth,startY+halfNodeWidth);
		poly.addPoint(startX,startY+getNodeWidth());
		poly.addPoint(startX-halfNodeWidth,startY+halfNodeWidth);
		poly.addPoint(startX,startY);
		poly.npoints=5;
		return poly;
	}
	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Polygon nodeP = nodePoly(node);
		if (nodeP!=null && nodeP.contains(x,y))
			return true;
		else
			return false;
	}
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, int node, int x, int y, MesquiteDouble fraction)
	{
		if (foundBranch==0) {
			if (branchPoly != null && branchPoly[node] != null && branchPoly[node].contains(x, y) || inNode(node,x,y)){
				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) {
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, (int)this.x[motherNode],  (int)this.y[motherNode], this.x[node],  (int)this.y[node],isRIGHT()||isLEFT(), isUP()||isDOWN()));
						}
					}
			}

			if (!tree.getAssociatedBit(triangleNameRef, node)) 
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				ScanBranches(tree, d, x, y, fraction);

		}
	}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
	        if (MesquiteTree.OK(tree) && ready) {
	        	foundBranch=0;
	       		 ScanBranches(tree, drawnRoot, x, y, fraction);
	       		 if (foundBranch == tree.getRoot() && !tree.getRooted())
	       		 	return 0;
	       		 else
	       		 	return foundBranch;
	       	}
	       	return 0;
	}
	
	/*_________________________________________________*/
	public void reorient(int orientation) {
		treeDisplay.setOrientation(orientation);
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setEdgeWidth(int edw) {
		preferredEdgeWidth = edw;
		edgeWidth = edw;
		treeDisplay.setMinimumTaxonNameDistance(edgeWidth, 5);
	}
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgeWidth;
	}
	/*_________________________________________________*/
	public void dispose(){
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			fillBranchPoly[i] = null;
		}
		ownerModule=null;
		super.dispose(); //calls cleanup
	}

}
	
