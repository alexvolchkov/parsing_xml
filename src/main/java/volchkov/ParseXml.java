package volchkov;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import volchkov.model.NameField;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Aleksandr Volchkov
 */
public class ParseXml {

    public static List<String> parsingAddrObj(String stringDate, String id) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        List<String> result = new ArrayList<>();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse("src/main/resources/AS_ADDR_OBJ.XML");
            XPath xPath = XPathFactory.newInstance().newXPath();
            LocalDate date = LocalDate.parse(stringDate, DateTimeFormatter.ISO_DATE);
            String[] arrayId = Arrays.stream(id.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
            for (String s : arrayId) {
                String expression = String.format("/ADDRESSOBJECTS/OBJECT[@OBJECTID='%s']", s);
                Optional<Node> node = findById(expression, document, xPath, date);
                result.add(new StringBuilder(s)
                        .append(": ")
                        .append(getTypeAndNameByNode(node))
                        .toString());
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static List<String> parsingAdmHierarchy(String name) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        List<String> result = new ArrayList<>();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document documentAdrObj = builder.parse("src/main/resources/AS_ADDR_OBJ.XML");
            Document documentAdmHierarchy = builder.parse("src/main/resources/AS_ADM_HIERARCHY.XML");
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = String.format("/ADDRESSOBJECTS/OBJECT[@TYPENAME='%s' and @ISACTIVE=1]", name);
            List<Node> nodes = findByField(expression, documentAdrObj, xPath);
            for (Node node : nodes) {
                result.add(getFullAddress(documentAdrObj, documentAdmHierarchy, xPath, getAttribute(node, NameField.OBJECTID.getTitle())));
            }
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static Optional<Node> findById(String expression,
                                           Document document,
                                           XPath xPath,
                                           LocalDate date) throws XPathExpressionException {
        List<Node> nodes = findByField(expression, document, xPath, date);
        return nodes.stream().findFirst();
    }

    private static Optional<Node> findById(String expression,
                                           Document document,
                                           XPath xPath) throws XPathExpressionException {
        List<Node> nodes = findByField(expression, document, xPath);
        return nodes.stream().findFirst();
    }

    private static List<Node> findByField(String expression,
                                          Document document,
                                          XPath xPath,
                                          LocalDate date) throws XPathExpressionException {
        List<Node> nodes = findByField(expression, document, xPath);
        return nodes.stream().filter(el -> isValidate(el, date)).toList();
    }

    private static List<Node> findByField(String expression,
                                         Document document,
                                         XPath xPath) throws XPathExpressionException {
        List<Node> result = new ArrayList<>();
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add(nodeList.item(i));
        }
        return result;
    }

    private static boolean isValidate(Node node, LocalDate date) {
        boolean result = false;
        if (node != null) {
            String startDate = getAttribute(node, NameField.STARTDATE.getTitle());
            String endDate = getAttribute(node, NameField.ENDDATE.getTitle());
            if (date.isAfter(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE))
                    && date.isBefore(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE))) {
                result = true;
            }
        }
        return result;
    }

    private static StringBuilder getTypeAndNameByNode(Optional<Node> optionalNode) {
        StringBuilder stringBuilder = new StringBuilder();
        optionalNode.ifPresent(node -> stringBuilder.append(getTypeAndNameByNode(node)));
        return stringBuilder;
    }

    private static StringBuilder getTypeAndNameByNode(Node node) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getAttribute(node, NameField.TYPENAME.getTitle()))
                .append(" ")
                .append(getAttribute(node, NameField.NAME.getTitle()));
        return stringBuilder;
    }

    private static String getAttribute(Node node, String nameAttribute) {
        NamedNodeMap attributes = node.getAttributes();
        Node nodeAttribute = attributes.getNamedItem(nameAttribute);
        if (nodeAttribute == null) {
            throw new IllegalArgumentException("Не верное имя атрибута");
        }
        return nodeAttribute.getNodeValue();
    }

    private static String getFullAddress(Document documentAdrObj,
                                         Document documentAdmHierarchy,
                                         XPath xPath,
                                         String id) throws XPathExpressionException {
        List<String> nodes = new ArrayList<>();
        nodes.add(id);
        getParentObject(documentAdmHierarchy, xPath, nodes, id);
        Collections.reverse(nodes);
        return nodes.stream().map(el -> {
            try {
                String expression = String.format("/ADDRESSOBJECTS/OBJECT[@OBJECTID='%s' and @ISACTIVE=1]", el);
                return getTypeAndNameByNode(findById(expression, documentAdrObj, xPath));
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining(", "));
    }

    private static void getParentObject(Document document,
                             XPath xPath,
                             List<String> nodes,
                             String id) throws XPathExpressionException {
        String expression = String.format("/ITEMS/ITEM[@OBJECTID='%s' and @ISACTIVE=1]", id);
        Optional<Node> nodeOptional = findById(expression,
                document,
                xPath);
        if (nodeOptional.isPresent()) {
            String parentObjId = getAttribute(nodeOptional.get(), NameField.PARENTOBJID.getTitle());
            if (!"0".equals(parentObjId)) {
                nodes.add(parentObjId);
                getParentObject(document, xPath, nodes, parentObjId);
            }
        }
    }
}
