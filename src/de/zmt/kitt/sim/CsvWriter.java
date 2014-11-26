package de.zmt.kitt.sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;


/**
 * provides the functions to serialize data to a comma separated file
 * 
 * @author oth
 * 
 */
public class CsvWriter {

	/**
	 * Buffered writer instance to write to
	 * @uml.property  name="output"
	 */
	private BufferedWriter output;
	/**
	 * defines the separator for each field in the line
	 */
	private static final String sep="\t";
	/**
	 * @uml.property  name="path"
	 */
	private String path;


	/**
	 * @param path defines the path for the file to be gnerated
	 * @throws Exception 
	 */
	public CsvWriter(String path) throws Exception{
		this.path=new String( path);
		FileOutputStream file = null;
		try {
			file = new FileOutputStream(path);
		} catch (FileNotFoundException e) {
			//System.out.println("could not create file " +path);
			throw new Exception( "could not create file " +path);
		}
		output = new BufferedWriter(new OutputStreamWriter(file));
	}
	
	
	/**
	 * @param str the string to write in the next field of csv file
	 * appends one seperated element to the file
	 * @throws Exception 
	 */
	public void append(String str) throws Exception {
		try {
			output.write(str+sep);
			output.flush();
		} catch (IOException e) {
			throw new Exception( "could not write to file " +path);
			//System.out.println(e);
			//System.exit(-1);
		}
	}
	
	/**
	 * create a new line in the file
	 * @throws Exception 
	 */
	public void newLine() throws Exception{
		try {
			output.newLine();
			output.flush();
		} catch (IOException e) {
			//System.out.println(e);
			throw new Exception( "could not create file " +path);
		}	
	}
	
	/**
	 * close the file, no more write access is possible
	 * @throws Exception 
	 */	
	public void close() throws Exception{	
		if( output == null)return;
		try {
			output.flush();
			output.close();
		} catch (IOException e) {
			//System.out.println("could not close file ");
			//e.printStackTrace();
			throw new Exception( "could not create file " +path);
			//System.exit(-1);
		}	
	}
}
