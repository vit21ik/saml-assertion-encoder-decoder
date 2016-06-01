package com.example.utils;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSString;

import java.util.Map;
import java.util.Optional;

public class AssertionBuilder {

  private int maxSessionTimeoutInMinutes = 15;

  private static XMLObjectBuilderFactory builderFactory;

  private String issuer;

  private String nameId;

  private String nameQualifier;

  private String sessionId;

  private Map<String, String> customAttributes;

  private static XMLObjectBuilderFactory getBuilderFactory() throws ConfigurationException {
    if(builderFactory == null){
      DefaultBootstrap.bootstrap();
      builderFactory = Configuration.getBuilderFactory();
    }
    return builderFactory;
  }

  public AssertionBuilder issuer(String issuer) {
    this.issuer = issuer;
    return this;
  }

  public AssertionBuilder nameId(String nameId) {
    this.nameId = nameId;
    return this;
  }

  public AssertionBuilder nameQualifier(String nameQualifier) {
    this.nameQualifier = nameQualifier;
    return this;
  }

  public AssertionBuilder sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public AssertionBuilder customAttributes(Map<String, String> customAttributes) {
    this.customAttributes = customAttributes;
    return this;
  }

  public Assertion build() {
    try {
      final SAMLObjectBuilder samlObjectBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(NameID.DEFAULT_ELEMENT_NAME);
      final NameID nameIdObject = (NameID) samlObjectBuilder.buildObject();
      nameIdObject.setValue(nameId);
      nameIdObject.setNameQualifier(nameQualifier);
      nameIdObject.setFormat(NameID.UNSPECIFIED);

      final SAMLObjectBuilder confirmationMethodBuilder = (SAMLObjectBuilder)  getBuilderFactory().getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
      final SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) confirmationMethodBuilder.buildObject();
      DateTime now = new DateTime();
      confirmationMethod.setNotBefore(now);
      confirmationMethod.setNotOnOrAfter(now.plusMinutes(2));

      final SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
      final SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder.buildObject();
      subjectConfirmation.setSubjectConfirmationData(confirmationMethod);

      final SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(Subject.DEFAULT_ELEMENT_NAME);
      final Subject subject = (Subject) subjectBuilder.buildObject();
      subject.setNameID(nameIdObject);
      subject.getSubjectConfirmations().add(subjectConfirmation);

      final SAMLObjectBuilder authStatementBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
      final AuthnStatement authnStatement = (AuthnStatement) authStatementBuilder.buildObject();
      final DateTime now2 = new DateTime();
      authnStatement.setAuthnInstant(now2);
      authnStatement.setSessionIndex(sessionId);
      authnStatement.setSessionNotOnOrAfter(now2.plus(maxSessionTimeoutInMinutes));

      final SAMLObjectBuilder authContextBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
      final AuthnContext authnContext = (AuthnContext) authContextBuilder.buildObject();

      final SAMLObjectBuilder authContextClassRefBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
      final AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) authContextClassRefBuilder.buildObject();
      authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:Password"); // TODO not sure exactly about this

      authnContext.setAuthnContextClassRef(authnContextClassRef);
      authnStatement.setAuthnContext(authnContext);

      final SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
      final AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();

      if( customAttributes != null) {
        customAttributes.keySet().forEach(key -> {
          buildStringAttribute(key,  customAttributes.get(key)).ifPresent(attribute -> {
            attrStatement.getAttributes().add(attribute);
          });
        });
      }

      final SAMLObjectBuilder doNotCacheConditionBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(OneTimeUse.DEFAULT_ELEMENT_NAME);
      final Condition condition = (Condition) doNotCacheConditionBuilder.buildObject();

      final SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
      final Conditions conditions = (Conditions) conditionsBuilder.buildObject();
      conditions.getConditions().add(condition);

      final SAMLObjectBuilder issuerBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
      Issuer issuerObject = (Issuer) issuerBuilder.buildObject();
      issuerObject.setValue(issuer);

      final SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
      Assertion assertion = (Assertion) assertionBuilder.buildObject();
      assertion.setIssuer(issuerObject);
      assertion.setIssueInstant(now);
      assertion.setVersion(SAMLVersion.VERSION_20);

      assertion.getAuthnStatements().add(authnStatement);
      assertion.getAttributeStatements().add(attrStatement);
      assertion.setConditions(conditions);
      return assertion;
    } catch (ConfigurationException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Optional<Attribute> buildStringAttribute(String name, String value) {
    try {
      SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder) getBuilderFactory().getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
      Attribute attrFirstName = (Attribute) attrBuilder.buildObject();
      attrFirstName.setName(name);
      XMLObjectBuilder stringBuilder = getBuilderFactory().getBuilder(XSString.TYPE_NAME);
      XSString attrValueFirstName = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
      attrValueFirstName.setValue(value);
      attrFirstName.getAttributeValues().add(attrValueFirstName);
      return Optional.of(attrFirstName);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }


}
