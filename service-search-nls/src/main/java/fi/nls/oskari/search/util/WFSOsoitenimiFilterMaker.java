package fi.nls.oskari.search.util;

import fi.nls.oskari.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class WFSOsoitenimiFilterMaker {

	private static String PROPERTY_OSOITENIMI_KATUNIMI   	= "oso:Osoitenimi/oso:katunimi";
	private static String PROPERTY_OSOITENIMI_KATUNUMERO 	= "oso:Osoitenimi/oso:katunumero";
	private static String PROPERTY_OSOITENIMI_KUNTANIMI_FIN	= "oso:Osoitenimi/oso:kuntanimiFin";
	private static String PROPERTY_OSOITENIMI_KUNTANIMI_SWE	= "oso:Osoitenimi/oso:kuntanimiSwe";
	
	private String xml ="";
	
	// Constructor making 
	public WFSOsoitenimiFilterMaker(QueryParser qp) {
		makeFilter(qp);
	}
	
	// Make filter from QueryParser
	public void makeFilter(QueryParser qp) {
		try {
			/////////////////////////////
			//Creating an empty XML Document

            //We need a Document
			DocumentBuilderFactory dbfac = XmlHelper.newDocumentBuilderFactory();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            ////////////////////////
            //Creating the XML tree

            //create the filter element and add it to the document
            Element filter = doc.createElement("Filter");
            doc.appendChild(filter);

            Element propertyIsLike = makePropertyIsLike(doc,PROPERTY_OSOITENIMI_KATUNIMI,qp.getStreetName());
            
            boolean moreThanOne = false;
            
            Element and = doc.createElement("and");
        	
            if (qp.getHouseNumber() != null && !"null".equals(qp.getHouseNumber())) {
            	
            	if (qp.getHouseNumber().contains("-")) {
            		/*String[] houseNumbers = qp.getHouseNumber().split("-");
            		
            		if (houseNumbers.length > 1) {
            			
            			int startNumber = Integer.parseInt(houseNumbers[0]);
            			int endNumber = Integer.parseInt(houseNumbers[1]);
            			Element or = doc.createElement("or");
            			
            			if (startNumber > endNumber) {
            				startNumber = endNumber;
            				endNumber = Integer.parseInt(houseNumbers[0]);
            			}
            			for (int i = startNumber; i < endNumber+1; i++ ) {
            				or.appendChild(makePropertyIsEqualTo(doc, PROPERTY_OSOITENIMI_KATUNUMERO, String.valueOf(i)));
                		}
            			
            			and.appendChild(or);
            			/*
            			and.appendChild(makePropertyGreaterThanOrEqualTo(doc, PROPERTY_OSOITENIMI_KATUNUMERO, houseNumbers[0]));
            			and.appendChild(makePropertyLessThanOrEqualTo(doc, PROPERTY_OSOITENIMI_KATUNUMERO, houseNumbers[1]));
            			*/
            			
            		//} else {
            		and.appendChild(makePropertyIsEqualTo(doc, PROPERTY_OSOITENIMI_KATUNUMERO,qp.getHouseNumber()));
            		//}
            		
            		
            	}else {
            		and.appendChild(makePropertyIsEqualTo(doc, PROPERTY_OSOITENIMI_KATUNUMERO,qp.getHouseNumber()));
            	}
            	moreThanOne = true;
            }
            if (qp.getVillageName() != null && !"null".equals(qp.getVillageName())) {
            	Element or = doc.createElement("or");
            	or.appendChild(makePropertyIsLike(doc, PROPERTY_OSOITENIMI_KUNTANIMI_FIN,qp.getVillageName()));
            	or.appendChild(makePropertyIsLike(doc, PROPERTY_OSOITENIMI_KUNTANIMI_SWE,qp.getVillageName()));
            	and.appendChild(or);
            	moreThanOne = true;
            }
           
            if (moreThanOne) {
            	and.appendChild(propertyIsLike);
            	filter.appendChild(and);
            } else {
            	filter.appendChild(propertyIsLike);
            }
            
            
            /////////////////
            //Output the XML

            //set up a transformer
            TransformerFactory transformerFactory = XmlHelper.newTransformerFactory();
            Transformer trans = transformerFactory.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //trans.setOutputProperty(OutputKeys.INDENT, "yes");
			
            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            String xmlString = sw.toString();

            this.xml = xmlString;
            

        } catch (Exception e) {
            throw new RuntimeException("Problem on WFSOsoitenimiFilter class :" +e.toString());
        }

	}
	
	public Element makePropertyIsEqualTo(Document doc, String propertyNameValue, String literalValue) {
		
		 Element propertyElement =  doc.createElement("PropertyIsEqualTo");
		 
		 propertyElement.appendChild(getChildProperty(doc,"PropertyName",propertyNameValue));
		 propertyElement.appendChild(getChildProperty(doc,"Literal",literalValue));
        
         return propertyElement;
		
	}
	
	public Element makePropertyLessThanOrEqualTo(Document doc, String propertyNameValue, String literalValue) {
		
		 Element propertyElement =  doc.createElement("PropertyIsLessThanOrEqualTo");
		 
		 propertyElement.appendChild(getChildProperty(doc,"PropertyName",propertyNameValue));
		 propertyElement.appendChild(getChildProperty(doc,"Literal",literalValue));
       
        return propertyElement;
		
	}
	
	public Element makePropertyGreaterThanOrEqualTo(Document doc, String propertyNameValue, String literalValue) {
		
		 Element propertyElement =  doc.createElement("PropertyIsGreaterThanOrEqualTo");
		 
		 propertyElement.appendChild(getChildProperty(doc,"PropertyName",propertyNameValue));
		 propertyElement.appendChild(getChildProperty(doc,"Literal",literalValue));
      
       return propertyElement;
		
	}
	
	
	public Element makePropertyIsLike(Document doc, String propertyNameValue, String literalValue) {
		
		Element propertyIsLike = doc.createElement("PropertyIsLike");
        propertyIsLike.setAttribute("wildCard", "*");
        propertyIsLike.setAttribute("matchCase","false");
        propertyIsLike.setAttribute("singleChar","?");
        propertyIsLike.setAttribute("escapeChar","!");
        
        propertyIsLike.appendChild(getChildProperty(doc,"PropertyName",propertyNameValue));
        propertyIsLike.appendChild(getChildProperty(doc,"Literal",literalValue));
        
        return propertyIsLike;
		
	}
	
	public Element getChildProperty(Document doc, String childElementName, String propertyNameValue) {
		 Element childProperty = doc.createElement(childElementName);
		 Text propertyName = doc.createTextNode(propertyNameValue);
		 childProperty.appendChild(propertyName);
		 
		 return childProperty;
	}
	
	// Return filter string
	public String getFilter() {
		return this.xml;
	}
	
}
