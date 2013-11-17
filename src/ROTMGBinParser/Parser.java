package ROTMGBinParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses the Bin file to determine if it is an xml.
 * If it is an xml, then add it to data to their proper xml.
 */
public class Parser {
	Map<String, Document> docMap = new HashMap<String, Document>();
	ArrayList<String> objId = new ArrayList<String>();
	ArrayList<Integer> objType = new ArrayList<Integer>();
	ArrayList<String> objIdType = new ArrayList<String>();
	public Parser()
	{

	}

	public boolean ParseBin(Path filePath)
	{
		// determine if xml
		File file = new File(filePath.toAbsolutePath().toString());
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			Node rootNode = doc.getDocumentElement();
			rootNode.normalize();
			String rootName = rootNode.getNodeName();
			if (rootName != "Objects")
			{
				if (!docMap.containsKey(rootName))
				{
					// generate a new doc and transfer children nodes over
					docMap.put(rootName, dBuilder.newDocument());
					// adopt and copy over the element
					Node adoptedNode = docMap.get(rootName).adoptNode(rootNode.cloneNode(true));
					docMap.get(rootName).appendChild(adoptedNode);
				}
				else
				{
					// copy the children nodes to be stored in docMap
					NodeList list = rootNode.getChildNodes();
					for (int j = 0; j < list.getLength(); j++)
					{
						Node node = list.item(j);
						// adopt and copy over the element
						Node adoptedNode = docMap.get(rootName).adoptNode(node.cloneNode(true));
						docMap.get(rootName).getDocumentElement().appendChild(adoptedNode);
					}
				}
			}
			else
			{
				// filter "Objects" by their classes
				NodeList list = rootNode.getChildNodes();
				for (int j = 0; j < list.getLength(); j++)
				{
					Node node = list.item(j);
					// check to see if it is a valid node
					if (node.getNodeName().contains("#text"))
						continue;
					/*
					String id = ((Element)node).getAttribute("id");
					int type = Integer.decode(((Element) node).getAttribute("type"));
					if (objId.contains(id))
						ROTMGBinParser.print("Found ID duplicate: " + id);
					else
						objId.add(id);
					if (objType.contains(type))
						ROTMGBinParser.print("Found Type duplicate: " + type);
					else
						objType.add(type);
					if (objIdType.contains(id + type))
						ROTMGBinParser.print("Found ID + Type duplicate: " + id + type);
					else
						objIdType.add(id + type);
                    */
					// retrieve Class name of object
					NodeList childNodes = node.getChildNodes();
					for (int k = 0; k < childNodes.getLength(); k++)
					{
						Node childNode = childNodes.item(k);
						if (childNode.getNodeName() == "Class")
						{
							rootName = childNode.getTextContent(); // override "Objects" rootName
						}
					}//
					if (!docMap.containsKey(rootName))
					{
						// create new doc with <Objects> as the root element
						docMap.put(rootName, dBuilder.newDocument());
						docMap.get(rootName).appendChild(docMap.get(rootName).createElement("Objects"));
					}

					// adopt and copy over the element
					Node adoptedNode = docMap.get(rootName).adoptNode(node.cloneNode(true));
					docMap.get(rootName).getDocumentElement().appendChild(adoptedNode);
				}
			}
		}
		catch (Exception ex)
		{
			// if bin is not a valid xml.
			if (ex.getMessage().contains("Content is not allowed in prolog."))
				return true;
			return false;
		}
		return true;
	}

	public void generateXml()
	{

		for (String key : docMap.keySet())
		{
			try
			{
				File file = new File(key + ".xml");
				if (file.exists())
					file.delete();
				Document doc = docMap.get(key);
				int childrenCount = doc.getDocumentElement().getChildNodes().getLength();
				ROTMGBinParser.print(key + " has " + childrenCount + " elements.");
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				transformer.transform(source, result);
			}
			catch (Exception ex)
			{
				ROTMGBinParser.print("ERR: Generating " + key + " Message: " + ex.getMessage());
			}
		}
	}
}
