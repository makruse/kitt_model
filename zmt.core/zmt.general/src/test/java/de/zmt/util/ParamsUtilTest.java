package de.zmt.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import sim.engine.params.BaseParams;
import sim.engine.params.Params;
import sim.engine.params.TestParams;
import sim.engine.params.def.ParamDefinition;

public class ParamsUtilTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsUtilTest.class.getName());

    private static final String PARAMS_PATH = "params_temp.xml";

    public static final String VALID_PARAMS_PATH = "params-valid.xml";
    private static final String INVALID_PARAMS_PATH = "params-invalid.xml";
    private static final String SCHEMA_PATH = "www.zmt-bremen.de.xsd";

    private static final String STRING_VALUE = "correct value";
    private static final int INT_VALUE = 33;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testWriteRead() throws JAXBException, IOException {
	TestParams writeParams = new TestParams();
	writeParams.getDefinition().setStringValue(STRING_VALUE);
	writeParams.getDefinition().setIntValue(INT_VALUE);

	File paramsFile = folder.newFile(PARAMS_PATH);

	ParamsUtil.writeToXml(writeParams, paramsFile);

	TestParams readParams = ParamsUtil.readFromXml(paramsFile, TestParams.class);
	assertEquals(writeParams.getDefinition().getStringValue(), readParams.getDefinition().getStringValue());
	assertEquals(writeParams.getDefinition().getIntValue(), readParams.getDefinition().getIntValue());
    }

    @Test
    public void testValidate() throws SAXException, IOException, JAXBException, URISyntaxException {
	Path validParamsPath = Paths.get(ParamsUtilTest.class.getResource(VALID_PARAMS_PATH).toURI());
	Path invalidParamsPath = Paths.get(ParamsUtilTest.class.getResource(INVALID_PARAMS_PATH).toURI());
	Path schemaPath = Paths.get(ParamsUtilTest.class.getResource(SCHEMA_PATH).toURI());

	// create unmarshaller with strict validation
	Unmarshaller strictUnmarshaller = JAXBContext.newInstance(ValidatingParams.class).createUnmarshaller();
	strictUnmarshaller.setEventHandler(new DefaultValidationEventHandler());

	assertThat(ParamsUtil.readFromXml(validParamsPath, ValidatingParams.class, schemaPath, strictUnmarshaller),
		is(new ValidatingParams()));

	try {
	    ParamsUtil.readFromXml(invalidParamsPath, ValidatingParams.class, schemaPath, strictUnmarshaller);
	} catch (UnmarshalException e) {
	    if (e.getLinkedException() instanceof SAXParseException) {
		logger.info("Validation failed with invalid xml file. This is correct.");
		return;
	    }
	}
	fail("Validation was successful with invalid xml file. This is not correct.");
    }

    /**
     * {@link Params} implementation matching schema.
     * 
     * @author mey
     *
     */
    @XmlRootElement(name = "validatingParams", namespace = "http://www.zmt-bremen.de/")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class ValidatingParams extends BaseParams {
	private static final long serialVersionUID = 1L;

	private final ValidatingDefinition validatingDefinition = new ValidatingDefinition();

	@Override
	public Collection<ParamDefinition> getDefinitions() {
	    return Collections.singleton(validatingDefinition);
	}
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class ValidatingDefinition implements ParamDefinition {
	private static final long serialVersionUID = 1L;

	private String attribute = "attribute value";

	@Override
	public String getTitle() {
	    return toString();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    ValidatingDefinition other = (ValidatingDefinition) obj;
	    if (attribute == null) {
		if (other.attribute != null) {
		    return false;
		}
	    } else if (!attribute.equals(other.attribute)) {
		return false;
	    }
	    return true;
	}
    }
}
