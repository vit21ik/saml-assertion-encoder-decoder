package com.example;

import com.example.utils.EncryptedAssertionConverter;
import com.example.utils.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.xml.ConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DecodeAssertionTest {

  private final String privateKey = "privatekey.pem";

  private final String encryptedAssertionXML = "encryptedAssertion.xml";

  @BeforeClass
  public static void setUpClass() throws ConfigurationException {
    DefaultBootstrap.bootstrap();
  }

  @Before
  public void setUp() throws ConfigurationException {

  }

  @Test
  public void decodeAssertion() {
    final String content = FileUtils.readFileFromResources(encryptedAssertionXML);
    final EncryptedAssertion encryptedAssertion = EncryptedAssertionConverter.fromString(content);
    final AssertionDecrypter assertionDecrypter = new AssertionDecrypter(privateKey);
    final Assertion assertion = assertionDecrypter.decrypt(encryptedAssertion);

    assertNotNull(assertion);
    assertEquals(EncodeAssertionTest.sessionId, assertion.getAuthnStatements().get(0).getSessionIndex());
    assertEquals(EncodeAssertionTest.issuer, assertion.getIssuer().getValue());
  }

}
