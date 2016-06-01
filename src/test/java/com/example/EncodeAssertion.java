package com.example;

import com.example.utils.AssertionBuilder;
import com.example.utils.AssertionConverter;
import org.junit.Test;
import org.opensaml.saml2.core.Assertion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by vitalii on 6/1/16.
 */
public class EncodeAssertion {

  private final String nameId = "mb";
  private final String nameQualifier = "Worldapp";
  private final String sessionId = "abcd";
  private final String issuer = "worldapp.com";
  private final String firstName = "Moby";
  private final String lastName = "Dick";

  @Test
  public void creatingAssertion() {
    final AssertionBuilder builder = new AssertionBuilder();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("FirstName", firstName);
    attributes.put("LastName", lastName);

    final Assertion assertion = builder.issuer( issuer )
           .nameId( nameId )
           .nameQualifier( nameQualifier )
           .sessionId( sessionId )
           .customAttributes(attributes)
           .build();

    System.out.println( AssertionConverter.toString(assertion) );
    assertEquals(sessionId, assertion.getAuthnStatements().get(0).getSessionIndex());
    assertEquals(issuer, assertion.getIssuer().getValue());
  }

}
