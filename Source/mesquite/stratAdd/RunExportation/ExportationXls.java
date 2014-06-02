package mesquite.stratAdd.RunExportation;

import java.io.*;


public class ExportationXls {

	String data;
	
	public ExportationXls (String data){
		this.data=data;
	}
	
	public boolean exportStats()
	{	
		//int i=0;
		FileOutputStream out; // declare a file output object
	    PrintStream p; // declare a print stream object

	    try
	    {
	    	out = new FileOutputStream("output.xls");
	    	// Connect print stream to the output stream
	    	p = new PrintStream(out);
			p.println ("<table>"+data);
			p.println("</table>");
			p.close();
	    }catch (Exception e)
	    {
	    	System.err.println ("Error writing to file");
	    	return false;
	    }
	    return true;
	 }
}
