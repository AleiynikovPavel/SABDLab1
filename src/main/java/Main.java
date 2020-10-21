import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import org.apache.commons.cli.*;

public class Main {

    private static String source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static String target = "kiMd6GvVlzUPwjg9734pSXbEqC5ZFOQxHyfIN8Bn0LoTA2rKaRhsmJtce1WYDu";

    public static void main(String[] args) {
        Options options = new Options();
        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);
        Option output = new Option("o", "output", true, "output file path");
        output.setRequired(true);
        options.addOption(output);
        Option mode = new Option("m", "mode", true, "mode obfuscate/unobfuscate");
        mode.setRequired(true);
        options.addOption(mode);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("lab1", options);
            System.exit(1);
        }
        if (!cmd.getOptionValue("mode").equals("obfuscate") && !cmd.getOptionValue("mode").equals("unobfuscate")) {
            System.out.println("Incorrect mode");
            System.exit(1);
        }
        try {
            processXML(new File(cmd.getOptionValue("input")), new File(cmd.getOptionValue("output")), cmd.getOptionValue("mode").equals("obfuscate"));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void processXML(File input, File out, boolean obfuscate) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream targetStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(input));
        Document document = documentBuilder.parse(targetStream);
        Element root = document.getDocumentElement();
        if (obfuscate)
            editXML(root, Main::obfuscate);
        else
            editXML(root, Main::unobfuscate);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

    public static void editXML(Node node, StringChange changer) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                Element element = (Element) node;
                for (int i = 0; i < element.getAttributes().getLength(); i++) {
                    element.setAttribute(element.getAttributes().item(i).getNodeName(), changer.change(element.getAttributes().item(i).getNodeValue()));
                }
                for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                    editXML(node.getChildNodes().item(i), changer);
                }
                break;
            case Node.TEXT_NODE:
                node.setTextContent(changer.change(node.getTextContent()));
                break;
        }
    }

    public static String obfuscate(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int index = source.indexOf(c);
            result[i] = index != -1 ? target.charAt(index) : c;
        }
        return new String(result);
    }

    public static String unobfuscate(String s) {
        char[] result = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int index = target.indexOf(c);
            result[i] = index != -1 ? source.charAt(index) : c;
        }
        return new String(result);
    }
}
