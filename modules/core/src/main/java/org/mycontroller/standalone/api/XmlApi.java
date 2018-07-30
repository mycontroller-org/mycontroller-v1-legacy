/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.3.0
 */

public class XmlApi {

    private Document getDocument(String uri) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(uri);
    }

    private XPathExpression getXPathExpression(String xpath) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpathObj = xPathfactory.newXPath();
        return xpathObj.compile(xpath);
    }

    public String read(String uri, String xpath) throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        Document document = getDocument(uri);
        XPathExpression expression = getXPathExpression(xpath);
        return expression.evaluate(document);
    }

    public List<Map<String, String>> readAsList(String uri, String xpath) throws SAXException, IOException,
            ParserConfigurationException, XPathExpressionException {
        Document document = getDocument(uri);
        XPathExpression expression = getXPathExpression(xpath);

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        NodeList nodeL = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        for (int index = 0; index < nodeL.getLength(); index++) {
            Node node = nodeL.item(index);
            if (node != null) {
                Map<String, String> map = new HashMap<String, String>();
                NodeList child = node.getChildNodes();
                if (child.getLength() == 1) {
                    map.put(node.getNodeName(), node.getTextContent());
                } else {
                    for (int cIndex = 0; null != child && cIndex < child.getLength(); cIndex++) {
                        Node nod = child.item(cIndex);
                        if (nod.getNodeType() == Node.ELEMENT_NODE) {
                            map.put(nod.getNodeName(), nod.getTextContent());
                        }
                    }
                }
                list.add(map);
            }
        }
        return list;
    }

    public void update(String uri, String xpath, String value) throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
        Document document = getDocument(uri);
        XPathExpression expression = getXPathExpression(xpath);
        Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
        node.setTextContent(value);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(document), new StreamResult(FileUtils.getFile(uri)));
    }

}
