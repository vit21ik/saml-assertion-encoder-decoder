package com.example.utils;

import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.impl.EncryptedAssertionMarshaller;
import org.opensaml.saml2.core.impl.EncryptedAssertionUnmarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class EncryptedAssertionConverter {

  public static String toString(final EncryptedAssertion assertion) {
    EncryptedAssertionMarshaller marshaller = new EncryptedAssertionMarshaller();
    try {
      Element plaintextElement = marshaller.marshall(assertion);
      return XMLHelper.nodeToString(plaintextElement);
    } catch (MarshallingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static EncryptedAssertion fromString(final String content) {
    try {
      final Document document = parseString(content);
      final EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller = new EncryptedAssertionUnmarshaller();
      return (EncryptedAssertion) encryptedAssertionUnmarshaller.unmarshall( document.getDocumentElement() );
    } catch (UnmarshallingException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Document parseString(final String content) {
    try {
      final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      final DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
      documentBuilderFactory.setNamespaceAware(true);
      final InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(content));
      return db.parse(is);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
