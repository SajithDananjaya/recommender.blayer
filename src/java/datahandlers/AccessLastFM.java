/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datahandlers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Sajith
 */
public class AccessLastFM {
    
    private static String BASE_URL = "";

    public static URL getURL(String methodParam, int resultCount) throws IOException {
        String url = BASE_URL + methodParam + "&api_key=";
        URL tempURL = new URL(url);
        return tempURL;
    }

    public static Document getResponse(URL url)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document docXML = docBuilder.parse(url.openStream());
        docXML.getDocumentElement().normalize();
        return docXML;
    }

    public static NodeList getElementList(Document docXML, String elementName) {
        NodeList elementList = docXML.getElementsByTagName(elementName);
        if (elementList.getLength() == 0) {
            return null;
        }
        return elementList;
    }

    public static String extractSingleAttribute(Node node, String attributeName) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            
            Element element = (Element) node;
            String attributeValue = element
                    .getElementsByTagName(attributeName)
                    .item(0)
                    .getTextContent();

            if (attributeValue.length() > 0) {
                return attributeValue;
            }
        }
        return null;
    }

    public static List<String>
            extractAttributes(NodeList elementList, String attributeName) {

        int elementCount = elementList.getLength();
        List<String> attributeList = new ArrayList<>();
        for (int index = 0; index < elementCount; index++) {
            Node node = elementList.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String elementAttribute = element
                        .getElementsByTagName(attributeName)
                        .item(0).getTextContent();
                if (elementAttribute.length() > 0) {
                    attributeList.add(elementAttribute);
                }
            }
        }
        return attributeList;
    }

}
