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
package mesquite.stratigraphictools.STSquareTree;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stratigraphictools.NodeLocsPaleo.*;

/* ======================================================================== */
public class STSquareTree extends DrawTree {
	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName;
	Vector drawings;
   	int oldEdgeWidth = 6;
   	int ornt;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsPaleo)hireEmployee(NodeLocsPaleo.class, "Calculator of node locations");
		if (nodeLocsTask == null) {
			return sorry(getName() + " couldn't start because no node location module was obtained.");
		}
		System.out.println("");
		System.out.println(nodeLocsTask.getName());
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
		SquareTreeDrawing treeDrawing =  new SquareTreeDrawing (treeDisplay, numTaxa, this);
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
	    	 	if (checker.compare(this.getClass(), "Sets the thickness of drawn branches", "[width in pixels]", commandName, "setEdgeWidth")) {
				int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(newWidth))
					newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, 99);
	    	 		if (newWidth>0 && newWidth<100 && newWidth!=oldEdgeWidth) {
	    	 			oldEdgeWidth=newWidth;
					Enumeration e = drawings.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
	    	 				treeDrawing.setEdgeWidth(newWidth);
	    					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(treeDrawing.edgewidth, 5); //better if only did this if tracing on
	    				}
					if (!MesquiteThread.isScripting()) parametersChanged();
	    	 		}
	    	 		
	    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
    	 		return nodeLocsTask;
    	 	}
     	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.UP);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.DOWN);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.RIGHT);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
		    	 	treeDrawing.reorient(TreeDisplay.LEFT);
			    	 ornt = treeDrawing.treeDisplay.getOrientation();
		    	 }
			orientationName.setValue(orient(ornt));
			parametersChanged();
    	 	}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Square tree with stratigraphic tools";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public String getVersion() {
		return null;
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Draws trees with standard square branches (\"phenogram\") allowing to use stratigraphic tools." ;
   	 }
	/*.................................................................................................................*/
   	 
}


/* ======================================================================== */
class SquareTreeDrawing extends TreeDrawing   {
	public Polygon[] branchPoly;
	public Polygon[] fillBranchPoly;

	public STSquareTree ownerModule;
	public int edgewidth = 6;
	public int preferredEdgeWidth = 6;
	int oldNumTaxa = 0;
 	private int foundBranch;
 	private boolean ready=false;
 	private static final int  inset=1;
 	private Polygon utilityPolygon;
	NameReference triangleNameRef;
 	
  	public SquareTreeDrawing(TreeDisplay treeDisplay, int numTaxa, STSquareTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
	    	treeDisplay.setMinimumTaxonNameDistance(edgewidth, 5); //better if only did this if tracing on
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		triangleNameRef = NameReference.getNameReference("triangled");

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
	/*_________________________________________________*/
	private void UPdefineFillPoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Nx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-( (sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx+inset, Ny+inset); // --->
			poly.addPoint(Nx+sliceWidth-inset, Ny+inset);	//down 
			poly.addPoint(Nx+sliceWidth-inset, mNy);
			poly.addPoint(Nx+inset, mNy);
			poly.addPoint(Nx+inset, Ny+inset);
			poly.npoints=4;
		}
		else if (Nx<mNx)
			{
			poly.npoints=0;
			if (numSlices>1)
				mNy+=inset;
			//if (numSlices>1)
			//	mNy-= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.addPoint(Nx+inset, Ny+inset); // --->
			poly.addPoint(Nx+sliceWidth-inset, Ny+inset);	//down 
			poly.addPoint(Nx+sliceWidth-inset, mNy+inset);
			poly.addPoint(mNx, mNy+inset);
			poly.addPoint(mNx, mNy+sliceWidth-inset);
			poly.addPoint(Nx+inset, mNy+sliceWidth-inset);
			poly.addPoint(Nx+inset, Ny+inset);
			poly.npoints=7;
			}
		else {
			poly.npoints=0;
			if (numSlices>1)
				mNy+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.addPoint(Nx+inset, Ny+inset);
			poly.addPoint(Nx+sliceWidth-inset, Ny+inset);
			poly.addPoint(Nx+sliceWidth-inset, mNy+sliceWidth-inset);
			poly.addPoint(mNx, mNy+sliceWidth-inset);
			poly.addPoint(mNx, mNy+inset);
			poly.addPoint(Nx+inset, mNy+inset);
			poly.addPoint(Nx+inset, Ny+inset);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	private void UPCalcFillBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPCalcFillBranchPolys(tree, d);
		UPdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void UPdefinePoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy) {
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx+edgewidth, Ny);	//down 
			poly.addPoint(Nx+edgewidth, mNy);
			poly.addPoint(Nx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=4;
		}
		else if (Nx<mNx)
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx+edgewidth, Ny);	//down 
			poly.addPoint(Nx+edgewidth, mNy);
			poly.addPoint(mNx, mNy);
			poly.addPoint(mNx, mNy+edgewidth);
			poly.addPoint(Nx, mNy+edgewidth);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx+edgewidth, Ny);
			poly.addPoint(Nx+edgewidth, mNy+edgewidth);
			poly.addPoint(mNx, mNy+edgewidth);
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
	}
	/*_________________________________________________*/
	private void UPCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPCalcBranchPolys(tree, d);
		UPdefinePoly(branchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void DOWNdefineFillPoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Nx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-((sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx+inset, Ny-inset); // --->
			poly.addPoint(Nx+sliceWidth-inset, Ny-inset);	//down 
			poly.addPoint(Nx+sliceWidth-inset, mNy);
			poly.addPoint(Nx+inset, mNy);
			poly.addPoint(Nx+inset, Ny-inset);
			poly.npoints=4;
		}
		else if (Nx<mNx)
			{
			poly.npoints=0;
			if (numSlices>1)
				mNy-=inset;
			poly.addPoint(Nx+inset, Ny-inset); // --->
			poly.addPoint(Nx+sliceWidth-inset, Ny-inset);	//down 
			poly.addPoint(Nx+sliceWidth-inset, mNy-inset);
			poly.addPoint(mNx, mNy-inset);
			poly.addPoint(mNx, mNy-sliceWidth+inset);
			poly.addPoint(Nx+inset, mNy-sliceWidth+inset);
			poly.addPoint(Nx+inset, Ny-inset);
			poly.npoints=7;
			}
		else {
			poly.npoints=0;
			if (numSlices>1)
				mNy-= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.addPoint(Nx+inset, Ny-inset);
			poly.addPoint(Nx+sliceWidth-inset, Ny-inset);
			poly.addPoint(Nx+sliceWidth-inset, mNy-sliceWidth+inset);
			poly.addPoint(mNx, mNy-sliceWidth+inset);
			poly.addPoint(mNx, mNy-inset);
			poly.addPoint(Nx+inset, mNy-inset);
			poly.addPoint(Nx+inset, Ny-inset);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	private void DOWNCalcFillBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNCalcFillBranchPolys(tree, d);
		DOWNdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void DOWNdefinePoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy) {
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx+edgewidth, Ny);	//down 
			poly.addPoint(Nx+edgewidth, mNy);
			poly.addPoint(Nx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=4;
		}
		else if (Nx<mNx)
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx+edgewidth, Ny);	//down 
			poly.addPoint(Nx+edgewidth, mNy);
			poly.addPoint(mNx, mNy);
			poly.addPoint(mNx, mNy-edgewidth);
			poly.addPoint(Nx, mNy-edgewidth);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx+edgewidth, Ny);
			poly.addPoint(Nx+edgewidth, mNy-edgewidth);
			poly.addPoint(mNx, mNy-edgewidth);
			poly.addPoint(mNx, mNy);
			poly.addPoint(Nx, mNy);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
	}
	/*_________________________________________________*/
	private void DOWNCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			DOWNCalcBranchPolys(tree, d);
		DOWNdefinePoly(branchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void RIGHTdefineFillPoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-((sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx-inset, Ny+inset); // --->
			poly.addPoint(Nx-inset, Ny+sliceWidth-inset);	//down 
			poly.addPoint(mNx, Ny+sliceWidth-inset);
			poly.addPoint(mNx, Ny+inset);
			poly.addPoint(Nx-inset, Ny+inset);
			poly.npoints=4;
		}
		else if (Ny<mNy)
			{
			poly.npoints=0;
			//if (numSlices>1)
			//	mNx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			if (numSlices>1)
				mNx-=inset;
			poly.addPoint(Nx-inset, Ny+inset); // --->
			poly.addPoint(Nx-inset, Ny+sliceWidth-inset);	//down 
			poly.addPoint(mNx-inset, Ny+sliceWidth-inset);
			poly.addPoint(mNx-inset, mNy);
			poly.addPoint(mNx-sliceWidth+inset, mNy);
			poly.addPoint(mNx-sliceWidth+inset, Ny+inset);
			poly.addPoint(Nx-inset, Ny+inset);
			poly.npoints=7;
			}
		else {
			poly.npoints=0;
			if (numSlices>1)
				mNx-= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.addPoint(Nx-inset, Ny+inset);
			poly.addPoint(Nx-inset, Ny+sliceWidth-inset);
			poly.addPoint(mNx-sliceWidth+inset, Ny+sliceWidth-inset);
			poly.addPoint(mNx-sliceWidth+inset, mNy);
			poly.addPoint(mNx-inset, mNy);
			poly.addPoint(mNx-inset, Ny+inset);
			poly.addPoint(Nx-inset, Ny+inset);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	private void RIGHTCalcFillBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTCalcFillBranchPolys(tree, d);
		RIGHTdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void RIGHTdefinePoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy) {
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx, Ny+edgewidth);	//down 
			poly.addPoint(mNx, Ny+edgewidth);
			poly.addPoint(mNx, Ny);
			poly.addPoint(Nx, Ny);
			poly.npoints=4;
		}
		else if (Ny<mNy)
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx, Ny+edgewidth);	//down 
			poly.addPoint(mNx, Ny+edgewidth);
			poly.addPoint(mNx, mNy);
			poly.addPoint(mNx-edgewidth, mNy);
			poly.addPoint(mNx-edgewidth, Ny);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx, Ny+edgewidth);
			poly.addPoint(mNx-edgewidth, Ny+edgewidth);
			poly.addPoint(mNx-edgewidth, mNy);
			poly.addPoint(mNx, mNy);
			poly.addPoint(mNx, Ny);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
			
	}
	/*_________________________________________________*/
	private void RIGHTCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			RIGHTCalcBranchPolys(tree, d);
		RIGHTdefinePoly(branchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void LEFTdefineFillPoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-((sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx-inset, Ny+inset); // --->
			poly.addPoint(Nx-inset, Ny+sliceWidth-inset);	//down 
			poly.addPoint(mNx, Ny+sliceWidth-inset);
			poly.addPoint(mNx, Ny+inset);
			poly.addPoint(Nx-inset, Ny+inset);
			poly.npoints=4;
		}
		else if (Ny<mNy)
			{
			poly.npoints=0;
			if (numSlices>1)
				mNx+=inset;
			poly.addPoint(Nx+inset, Ny+inset); // --->
			poly.addPoint(Nx+inset, Ny+sliceWidth-inset);	//down 
			poly.addPoint(mNx+inset, Ny+sliceWidth-inset);
			poly.addPoint(mNx+inset, mNy);
			poly.addPoint(mNx+sliceWidth-inset, mNy);
			poly.addPoint(mNx+sliceWidth-inset, Ny+inset);
			poly.addPoint(Nx+inset, Ny+inset);
			poly.npoints=7;
			}
		else {
			poly.npoints=0;
			if (numSlices>1)
				mNx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.addPoint(Nx+inset, Ny+inset);
			poly.addPoint(Nx+inset, Ny+sliceWidth-inset);
			poly.addPoint(mNx+sliceWidth-inset, Ny+sliceWidth-inset);
			poly.addPoint(mNx+sliceWidth-inset, mNy);
			poly.addPoint(mNx+inset, mNy);
			poly.addPoint(mNx+inset, Ny+inset);
			poly.addPoint(Nx+inset, Ny+inset);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	private void LEFTCalcFillBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTCalcFillBranchPolys(tree, d);
		LEFTdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void LEFTdefinePoly(Polygon poly, boolean isRoot, int Nx, int Ny, int mNx, int mNy) {
		if (isRoot) {
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx, Ny+edgewidth);	//down 
			poly.addPoint(mNx, Ny+edgewidth);
			poly.addPoint(mNx, Ny);
			poly.addPoint(Nx, Ny);
			poly.npoints=4;
		}
		else if (Ny>mNy)
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny); // --->
			poly.addPoint(Nx, Ny+edgewidth);	//down 
			poly.addPoint(mNx+edgewidth, Ny+edgewidth);
			poly.addPoint(mNx+edgewidth, mNy);
			poly.addPoint(mNx, mNy);
			poly.addPoint(mNx, Ny);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
		else
			{
			poly.npoints=0;
			poly.addPoint(Nx, Ny);
			poly.addPoint(Nx, Ny+edgewidth);
			poly.addPoint(mNx, Ny+edgewidth);
			poly.addPoint(mNx, mNy);
			poly.addPoint(mNx+edgewidth, mNy);
			poly.addPoint(mNx+edgewidth, Ny);
			poly.addPoint(Nx, Ny);
			poly.npoints=7;
			}
	}
	/*_________________________________________________*/
	private void LEFTCalcBranchPolys(Tree tree, int node)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTCalcBranchPolys(tree, d);
		LEFTdefinePoly(branchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
	}
	/*_________________________________________________*/
	private void UPDOWNcalculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPDOWNcalculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[tree.motherOfNode(node)];
		lineBaseX[node]=x[node];
	}
	/*_________________________________________________*/
	private void LEFTRIGHTcalculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTRIGHTcalculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[node];
		lineBaseX[node]=x[tree.motherOfNode(node)];
	}
	/*_________________________________________________*/
	private void calcBranches(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
		edgewidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgewidth+2) {
			edgewidth= treeDisplay.getTaxonSpacing()-2;
			if (edgewidth<2)
				edgewidth=2;
		}
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 5);
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			UPCalcBranchPolys(tree, drawnRoot);
			UPCalcFillBranchPolys(tree, drawnRoot);
			UPDOWNcalculateLines(tree, drawnRoot);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
			DOWNCalcBranchPolys(tree, drawnRoot);
			DOWNCalcFillBranchPolys(tree, drawnRoot);
			UPDOWNcalculateLines(tree, drawnRoot);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			RIGHTCalcBranchPolys(tree, drawnRoot);
			RIGHTCalcFillBranchPolys(tree, drawnRoot);
			LEFTRIGHTcalculateLines(tree, drawnRoot);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
			LEFTCalcBranchPolys(tree, drawnRoot);
			LEFTCalcFillBranchPolys(tree, drawnRoot);
			LEFTRIGHTcalculateLines(tree, drawnRoot);
		}
	}
	
	/*_________________________________________________*/
	private   void drawOneBranch(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			//g.setColor(Color.black);//for testing
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.getRooted() || tree.getRoot()!=node)
				g.fillPolygon(branchPoly[node]);

			if (tree.numberOfParentsOfNode(node)>1) {
				for (int i=1; i<=tree.numberOfParentsOfNode(node); i++) {
						int anc =tree.parentOfNode(node, i);
						if (anc!= tree.motherOfNode(node)) {
							g.drawLine(x[node],y[node], x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]);
							g.drawLine(x[node]+1,y[node], x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]);
							g.drawLine(x[node],y[node]+1, x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]+1);
							g.drawLine(x[node]+1,y[node]+1, x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]+1);
						}
				}
			}
			if (tree.getAssociatedBit(triangleNameRef,node)) {
				for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					g.drawLine(x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
					g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
					g.drawLine(x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
				}
			}
			if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawOneBranch(tree, g, d);
			//g.setColor(Color.green);//for testing
			//g.fillPolygon(fillBranchPoly[node]); //for testing
			//g.setColor(Color.black);//for testing
			if (emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(nodePoly(node));
				g.setColor(prev);
			}
		}
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
	        if (MesquiteTree.OK(tree)) {
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	g.setColor(treeDisplay.branchColor);
	        	/*if (oldNumTaxa!= tree.getNumTaxa())
	        		adjustNumTaxa(tree.getNumTaxa()); */
	       	 	drawOneBranch(tree, g, drawnRoot);  
	       	 }
	   }
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
	        if (MesquiteTree.OK(tree)) {
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	if (!tree.nodeExists(getDrawnRoot()))
	        		setDrawnRoot(tree.getRoot());
	        	calcBranches(tree, getDrawnRoot());
		}
	}
	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		Color tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(Color.blue);
		if (treeDisplay.getOrientation()==TreeDisplay.DOWN || treeDisplay.getOrientation()==TreeDisplay.UP){
			for (int i=0; i<4; i++)
				g.drawLine(x[node]-2 - i, y[node], x[node]-2 - i, y[tree.motherOfNode(node)]);
		}
		else {
			for (int i=0; i<4; i++)
				g.drawLine(x[node], y[node]-2 - i, x[tree.motherOfNode(node)], y[node]-2 - i);
		}
		g.setColor(tC);
	}
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		int ew = edgewidth-1;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) 
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			box = new Rectangle(x[node], y[node]+1, ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) 
			box = new Rectangle(x[node]+1, y[node], ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT)
			box = new Rectangle(x[node]-ew-3, y[node], ew, ew);
		else 
			box = new Rectangle(x[node], y[node], ew, ew);
		g.fillRect(box.x, box.y, box.width, box.height);
			g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle box;
		int numColors = colors.getNumColors();
		int ew = edgewidth-1;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) 
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			box = new Rectangle(x[node], y[node]+1, ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) 
			box = new Rectangle(x[node]+1, y[node], ew, ew);
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT)
			box = new Rectangle(x[node]-ew-3, y[node], ew, ew);
		else 
			box = new Rectangle(x[node], y[node], ew, ew);
		for (int i=0; i<numColors; i++) {
			g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
			g.fillRect(box.x + (i*box.width/numColors), box.y, box.width-  (i*box.width/numColors), box.height);
		}
			g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}
	/*_________________________________________________*/
	public  int findTerminalBox(Tree tree, int drawnRoot, int x, int y){
		return -1;
	}
	/*_________________________________________________*/
	private boolean ancestorIsTriangled(Tree tree, int node) {
		if (tree.getAssociatedBit(triangleNameRef, tree.motherOfNode(node)))
			return true;
		if (tree.getRoot() == node || tree.getSubRoot() == node)
			return false;
		return ancestorIsTriangled(tree, tree.motherOfNode(node));
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node))
			g.fillPolygon(fillBranchPoly[node]);
	}
	   
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			Color c = g.getColor();
			int numColors = colors.getNumColors();
			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				for (int i=0; i<numColors; i++) {
					UPdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
				for (int i=0; i<numColors; i++) {
					DOWNdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				for (int i=0; i<numColors; i++) {
					RIGHTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
				for (int i=0; i<numColors; i++) {
					LEFTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public Polygon nodePoly(int node) {
		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int doubleOffset = (getNodeWidth()-getEdgeWidth());
		int startX = x[node] - offset;
		int startY= y[node] - offset;
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT){
			startX -= getNodeWidth()-doubleOffset;
		} else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			startY -= getNodeWidth()-doubleOffset;
		Polygon poly = new Polygon();
		poly.npoints=0;
		poly.addPoint(startX,startY);
		poly.addPoint(startX+getNodeWidth(),startY);
		poly.addPoint(startX+getNodeWidth(),startY+getNodeWidth());
		poly.addPoint(startX,startY+getNodeWidth());
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
							if (treeDisplay.getOrientation()==TreeDisplay.UP|| treeDisplay.getOrientation()==TreeDisplay.DOWN)  {
								fraction.setValue( Math.abs(1.0*(y-this.y[motherNode])/(this.y[node]-this.y[motherNode])));
							}
							else if (treeDisplay.getOrientation()==TreeDisplay.LEFT || treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
								fraction.setValue( Math.abs(1.0*(x-this.x[motherNode])/(this.x[node]-this.x[motherNode])));
							}
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
		edgewidth = edw;
		preferredEdgeWidth = edw;
	}
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
	/*_________________________________________________*/
	public   void dispose() { 
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			fillBranchPoly[i] = null;
		}
		super.dispose();
	}
}
	
