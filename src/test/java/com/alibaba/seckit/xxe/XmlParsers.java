package com.alibaba.seckit.xxe;

import com.alibaba.seckit.SecurityUtil;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.io.StringWriter;

public class XmlParsers {
    public static interface XmlParser {
        String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception;
    }

    public static class Dom4jSaxReader implements XmlParser {
        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            SAXReader reader;
            if (xInclude) {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                spf.setXIncludeAware(true);

                SAXParser parser = spf.newSAXParser();
                reader = new SAXReader( parser.getXMLReader());
            } else {
                reader = new SAXReader();
            }
            if (secure) {
                reader = SecurityUtil.withXxeProtection(reader);
            }
            Document doc = reader.read(inputStream);
            return (String) doc.getRootElement().getData();
        }
    }

    public static class DocumentBuilderFactoryParser implements XmlParser {
        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            if (xInclude) {
                dbf.setNamespaceAware(true);
                dbf.setXIncludeAware(true);
            }

            if (secure) {
                dbf = SecurityUtil.withXxeProtection(dbf);
            }
            DocumentBuilder builder = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(inputStream);
            return doc.getDocumentElement().getTextContent();
        }
    }

    public static class RootTextHandler extends DefaultHandler {
        private String rootText;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            rootText += new String(ch, start, length);
        }

        public String getRootText() {
            return rootText;
        }
    }

    public static class SaxParserFactoryParser implements XmlParser {
        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            if (xInclude) {
                spf.setXIncludeAware(true);
                spf.setNamespaceAware(true);
            }
            if (secure) {
                spf = SecurityUtil.withXxeProtection(spf);
            }
            SAXParser parser = spf.newSAXParser();
            RootTextHandler handler = new RootTextHandler();
            parser.parse(inputStream, handler);
            return handler.getRootText();
        }
    }

    public static class XMLInputFactoryParser implements XmlParser {

        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            if (secure) {
                factory = SecurityUtil.withXxeProtection(factory);
            }
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
            StringBuilder rootText = new StringBuilder();

            while (reader.hasNext()) {
                int point = reader.next();
                switch (point) {
                    case XMLStreamReader.CHARACTERS:
                        rootText.append(reader.getText());
                }
            }
            return rootText.toString();
        }


    }

    public static class SaxBuilderParser implements XmlParser {
        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            SAXBuilder saxBuilder = new SAXBuilder();
            if (secure) {
                saxBuilder = SecurityUtil.withXxeProtection(saxBuilder);
            }
            org.jdom2.Document doc = saxBuilder.build(inputStream);
            return doc.getRootElement().getText();
        }
    }

    public static class XmlReaderParser implements XmlParser {

        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            if (secure) {
                reader = SecurityUtil.withXxeProtection(reader);
            }
            RootTextHandler handler = new RootTextHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(inputStream));
            return handler.getRootText();
        }
    }

    public static class ValidatorParser implements XmlParser {

        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema();
            Validator validator = schema.newValidator();
            if (secure) {
                validator = SecurityUtil.withXxeProtection(validator);
            }
            Source s = new StreamSource(inputStream);
            validator.validate(s);
            return "root:Doe:Answer";
        }
    }

    public static class SchemaFactoryParser implements XmlParser {

        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            if (secure) {
                factory = SecurityUtil.withXxeProtection(factory);
            }
            Schema schema = factory.newSchema(new StreamSource(inputStream));
            Validator validator = schema.newValidator();
            Source s = new StreamSource(this.getClass().getResourceAsStream("/xxe/simple.xml"));
            validator.validate(s);
            return "root:Doe:Answer";
        }
    }

    public static class TransformerFactoryParser implements XmlParser {

        @Override
        public String parse(InputStream inputStream, boolean xInclude, boolean secure) throws Exception {
            TransformerFactory factory = TransformerFactory.newInstance();
            if (secure) {
                factory = SecurityUtil.withXxeProtection(factory);
            }
            Transformer transformer = factory.newTransformer();
            StringWriter sw = new StringWriter();
            transformer.transform(new StreamSource(inputStream), new StreamResult(sw));
            return sw.toString();
        }
    }

}
