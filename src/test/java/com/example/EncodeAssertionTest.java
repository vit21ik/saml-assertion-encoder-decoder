package com.example;

import com.example.utils.AssertionBuilder;
import com.example.utils.AssertionConverter;
import com.example.utils.EncryptedAssertionConverter;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EncodeAssertionTest {

  private final String nameId = "mb";
  private final String nameQualifier = "Worldapp";
  public static final  String sessionId = "abcd";
  public static final String issuer = "worldapp.com";
  private final String firstName = "Moby";
  private final String lastName = "Dick";
  private final Map<String, String> attributes = new HashMap<>();

  private final AssertionBuilder builder = new AssertionBuilder();

  private final String publicCert = "publickey.cer";

  @Before
  public void init() {
    attributes.put("FirstName", firstName);
    attributes.put("LastName", lastName);
  }

  private Assertion createAssertion() {
    return builder.issuer( issuer )
            .nameId( nameId )
            .nameQualifier( nameQualifier )
            .sessionId( sessionId )
            .customAttributes(attributes)
            .build();
  }

  @Test
  public void creatingAssertion() {
    final Assertion assertion = createAssertion();

    System.out.println( AssertionConverter.toString(assertion) );
    assertEquals(sessionId, assertion.getAuthnStatements().get(0).getSessionIndex());
    assertEquals(issuer, assertion.getIssuer().getValue());
  }

  @Test
  public void encodeAssertion() {
    final Assertion assertion = createAssertion();
    final AssertionEncrypter encrypter = new AssertionEncrypter(publicCert);

    final EncryptedAssertion encryptedAssertion = encrypter.encrypt(assertion);
    assertNotNull(encryptedAssertion);
    System.out.println( EncryptedAssertionConverter.toString(encryptedAssertion) );
  }

}
