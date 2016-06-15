package com.example;

import com.example.utils.FileUtils;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.util.Base64;
import sun.security.provider.X509Factory;

import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class AssertionEncrypter {

  final String pathToPublicCert;

  public AssertionEncrypter(final String path) {
    this.pathToPublicCert = path;
  }

  private String readPublicCert(final String pathToCert) {
    return FileUtils.readFileFromResources(pathToCert);
  }

  private PublicKey getPublicKey(String certificatString) {
    try {
      byte [] certificatBytes = Base64.decode(certificatString.replaceAll(X509Factory.BEGIN_CERT, "").replaceAll(X509Factory.END_CERT, ""));
      final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate( new ByteArrayInputStream(certificatBytes) );
      final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(certificate.getPublicKey().getEncoded());
      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(publicKeySpec);
    } catch (InvalidKeySpecException |CertificateException |NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Credential getPublicCredential(final PublicKey publicKey) {
    final BasicX509Credential publicCredential = new BasicX509Credential();
    publicCredential.setUsageType(UsageType.SIGNING);
    publicCredential.setPublicKey(publicKey);
    return publicCredential;
  }

  public EncryptedAssertion encrypt(final Assertion assertion) {
    final String certificatString = readPublicCert(pathToPublicCert);
    final PublicKey publicKey = getPublicKey(certificatString);
    final Credential publicCredential = getPublicCredential(publicKey);

    final EncryptionParameters encryptionParameters = new EncryptionParameters();
    encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);

    KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
    keyEncryptionParameters.setEncryptionCredential( publicCredential );
    keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);

    final Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
    encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
    try {
      return encrypter.encrypt( assertion );
    } catch (EncryptionException e) {
      e.printStackTrace();
    }
    return null;
  }

}
