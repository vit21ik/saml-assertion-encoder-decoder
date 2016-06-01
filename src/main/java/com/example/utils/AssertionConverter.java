package com.example.utils;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

public class AssertionConverter {

  public static String toString(final Assertion assertion) {
    AssertionMarshaller marshaller = new AssertionMarshaller();
    try {
      Element plaintextElement = marshaller.marshall(assertion);
      String assertionString = XMLHelper.nodeToString(plaintextElement);
      return assertionString;
    } catch (MarshallingException e) {
      e.printStackTrace();
      return null;
    }
  }

}
