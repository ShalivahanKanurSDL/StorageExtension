/**
 * Copyright 2011-2013 Radagio & SDL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tridion.storage.aws;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XMLHelpers
{
	public static Document getXMLDocumentFromFile(String filePath) throws ParserConfigurationException, SAXException, IOException
	{
		return XMLHelpers.getXMLDocumentFromFile(new File(filePath));
	}

	public static Document getXMLDocumentFromString(String xml) throws SAXException, IOException, ParserConfigurationException
	{
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);

		DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
		doc = db.parse(new InputSource(new StringReader(xml)));
		return doc;
	}

	public static Document getXMLDocumentFromScratch(String rootElementName) throws SAXException, IOException, ParserConfigurationException
	{
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);

		DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
		doc = db.parse(new InputSource(new StringReader("<" + rootElementName + "></" + rootElementName + ">")));
		return doc;
	}

	public static Document getXMLDocumentFromFile(File xmlFile) throws ParserConfigurationException, SAXException, IOException
	{
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
		doc = db.parse(xmlFile);
		return doc;
	}

	public static String nodeToString(Node node, boolean outputXmlDeclaration)
	{
		DOMSource source = new DOMSource(node);
		return convertDOMToString(source, outputXmlDeclaration);
	}

	public static String nodeToString(Node node)
	{
		DOMSource source = new DOMSource(node);
		return convertDOMToString(source, true);
	}

	public String documentToString(Document doc)
	{
		DOMSource source = new DOMSource(doc);
		return convertDOMToString(source, true);
	}

	private static String convertDOMToString(DOMSource source, boolean outputXmlDeclaration)
	{
		StreamResult result = null;
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			if (!outputXmlDeclaration)
			{
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

			result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
		}
		catch (TransformerException e)
		{

		}
		return result.getWriter().toString();
	}
	
	public  static String XmlUpdate(int param, String xmlRecords) throws IndexingException 
	{		
		try{
	    Document doc=getXMLDocumentFromString(xmlRecords);
	    NodeList xmlUpdateNode = doc.getElementsByTagName("custom");		
		switch (param) {		
		case 1:
			Element pAdd = doc.createElement("action");
			pAdd.appendChild(doc.createTextNode("add"));
			xmlUpdateNode.item(0).getParentNode().insertBefore(pAdd, xmlUpdateNode.item(0));
			break;
			
		case 2:
			Element pUpdate = doc.createElement("action");
			pUpdate.appendChild(doc.createTextNode("update"));
			xmlUpdateNode.item(0).getParentNode().insertBefore(pUpdate, xmlUpdateNode.item(0));
			break;

		case 3:
			Element pDelete = doc.createElement("action");
			pDelete.appendChild(doc.createTextNode("delete"));
			xmlUpdateNode.item(0).getParentNode().insertBefore(pDelete, xmlUpdateNode.item(0));
			break;		
		}		
		DOMSource source = new DOMSource(doc);
		return convertDOMToString(source, true);
		
		}
		catch(Exception ex){
			throw new IndexingException("File Indexing Error While Writing the File");			
		}
	}
}
