package de.zmt.kitt.sim;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Config holds all parameters to initialize the model state.<br />
 * The parameters are read from and written to an Xml file <br />
 * with the help of JAXB lib.
  * Due to JAXB annotations it is defined which properties<br />
 * of the Configuration are written. <br />
 * XmlTransient marked properties are not serialized to xml.<br />
 * the names for the xml nodes are taken from the class-property name<br />
 * if not overriden by annotation.<br />
 * in this case the config file looks like this:<br />
 * <pre>
 * {@code
 * <ModelParams>
 *    <env>
 *        <simtime>2000.0</simtime>
 *        <drawinterval>1.0</drawinterval>
 *        ...
 *    </env>
 *    <prey>
 *        <initialNr>900</initialNr>
 *        <maxNr>1800</maxNr>        
 *        ...
 *    </prey>
 *    <pred>
 *        <initialNr>40</initialNr>
 *        <maxNr>100</maxNr>
 *        ...
 *    </pred>
 *    ..
 * </ModelParams>
 * }  
 * </pre> 
*/
@XmlRootElement( name = "ModelParams")
@XmlAccessorType(XmlAccessType.NONE)
public class ModelParams {

	@XmlElement
	public EnvironmentDefinition environmentDefinition=new EnvironmentDefinition();

	/**
	 * list of species definition
	 */
	@XmlElementWrapper( name="speciesList")
	@XmlElement( name = "speciesDefinition")
	public List<SpeciesDefinition> speciesList= new ArrayList<SpeciesDefinition>();
	
	@XmlTransient
    public String currentPath="";	 	
	@XmlTransient
	public static Color colorSpecies1= new Color(0,0,220);
	@XmlTransient
	public static Color colorSpecies2 = new Color( 220,0,0);
	@XmlTransient
	public static Color bgColor = Color.black;
	
	public ModelParams() {}
		
	
	/** @param path path to the config file
	 * @throws Exception */
	public ModelParams(String path) throws Exception{
				
		currentPath=path;				
		/*  // for creating an xml file with default values uncomment
		speciesList.add( new SpeciesDefinition());
		writeToXml(currentPath); // just for first run
		*/
		try {
			// read data from params file
			readFromXml();			
		} catch (Exception e) {
			e.printStackTrace();
			throw(e);
		}
	}
	
	/**
	 * reads the configuration from xmlfile with currentPath file and sets all parameters.
	 * before it remembers the direction flags in file to later restore them if they have been modified.
	 * @param path
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void readFromXml() throws JAXBException, IOException, Exception{
        JAXBContext context;
		try {
			context = JAXBContext.newInstance(ModelParams.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();	        
	        ModelParams readConfig = (ModelParams)unmarshaller.unmarshal(new FileReader(currentPath));
	        environmentDefinition= readConfig.environmentDefinition;
	        speciesList= readConfig.speciesList;
		} catch (JAXBException e) {
			System.out.println(e);
			throw(e);
		}  
	}
	
	/**
	 * writes the configuration to xml file in currentPath.
	 * before it restores the direction flags to be consistent with previous file state.
	 * 
	 * @param path path to the file that has to be written
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void writeToXml(String path) throws JAXBException, IOException{
		if(path != null){
			currentPath=path;
		}
		// if the directed flag has been modified after reading, recover the original file value
		// for the config files to be consistent
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(ModelParams.class);
		    Marshaller marshaller = context.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);		        		        
		    marshaller.marshal(this, new FileWriter(currentPath)); 		
		} catch (JAXBException e) {
			e.printStackTrace();
			throw(e);			
		} catch (IOException e) {
			e.printStackTrace();
			throw(e);
		} 
	}
			
	/** @return the filename substring of the currentPath */
	public String getFilename(){
		String filename = currentPath.substring(currentPath.lastIndexOf('/')+1);
		return filename;
	}
	
	/** sets the path for the next configuration file to run when start is called again */
	private void setFile(String filename){
		String dir = currentPath.substring(0,currentPath.lastIndexOf('/')+1);
	}

	public void setSpeciesDefinition(int idx,SpeciesDefinition def) {
		speciesList.set(idx, def);
	}

	public void setEnvironmentDefinition(EnvironmentDefinition def) {
		environmentDefinition=def;
	}	
}
