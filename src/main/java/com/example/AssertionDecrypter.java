package com.example;

import com.example.utils.FileUtils;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import sun.security.provider.X509Factory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class AssertionDecrypter {

  private final String CERT_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
  private final String CERT_FOOTER = "-----END RSA PRIVATE KEY-----";

  private final String privateKeyPath;

  public AssertionDecrypter(final String path) {
    privateKeyPath = path;
  }

  private String readPrivateCert() {
    return FileUtils.readFileFromResources(privateKeyPath);
  }

  private PrivateKey getPrivateKey() {
    java.security.Security.addProvider(
            new org.bouncycastle.jce.provider.BouncyCastleProvider()
    );
    final String privateKeyContent = readPrivateCert();
    final Base64 b64 = new Base64();
    byte [] decoded = b64.decode(privateKeyContent.replaceAll(CERT_HEADER, "").replaceAll(CERT_FOOTER, ""));
    try {
      PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(decoded);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      PrivateKey privKey = kf.generatePrivate(kspec);
      return privKey;
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  private BasicX509Credential getDecryptionCredential() {
    final PrivateKey privateKey = getPrivateKey();
    final BasicX509Credential decryptionCredential = new BasicX509Credential();
    decryptionCredential.setPrivateKey(privateKey);
    return decryptionCredential;
  }

  public Assertion decrypt(final EncryptedAssertion encryptedAssertion) {
    BasicX509Credential decryptionCredential = getDecryptionCredential();
    StaticKeyInfoCredentialResolver skicr = new StaticKeyInfoCredentialResolver(decryptionCredential);
    Decrypter samlDecrypter = new Decrypter(null, skicr, new InlineEncryptedKeyResolver());
    try {
      return samlDecrypter.decrypt(encryptedAssertion);
    } catch (DecryptionException e) {
      e.printStackTrace();
    }
    return null;
  }

}
