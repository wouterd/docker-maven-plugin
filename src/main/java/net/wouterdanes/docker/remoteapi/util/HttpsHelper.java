package net.wouterdanes.docker.remoteapi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

/**
 * Helper methods to parse and load Docker certificate files for encrypted https connection to the docker daemon
 */
public final class HttpsHelper {

    private HttpsHelper() {}

    public static KeyStore createKeyStore(final String certPath)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException,
            KeyStoreException {
        KeyPair keyPair = loadPrivateKey(certPath);
        Certificate privCert = loadCertificate(certPath);

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null);

        keyStore.setKeyEntry("docker", keyPair.getPrivate(), "changeit".toCharArray(), new Certificate[]{privCert});
        return keyStore;
    }

    public static KeyStore createTrustStore(final String certPath)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        Path caPath = Paths.get(certPath, "ca.pem");
        BufferedReader reader = Files.newBufferedReader(caPath, Charset.defaultCharset());

        PEMParser parser = new PEMParser(reader);
        X509CertificateHolder object = (X509CertificateHolder) parser.readObject();
        Certificate caCert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(object);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null);
        trustStore.setCertificateEntry("ca", caCert);
        return trustStore;
    }

    private static Certificate loadCertificate(final String certPath) throws IOException, CertificateException {
        Path cert = Paths.get(certPath, "cert.pem");
        BufferedReader reader = Files.newBufferedReader(cert, Charset.defaultCharset());
        PEMParser parser = new PEMParser(reader);

        X509CertificateHolder object = (X509CertificateHolder) parser.readObject();
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(object);
    }

    private static KeyPair loadPrivateKey(final String certPath)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        Path path = Paths.get(certPath, "key.pem");
        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());

        PEMParser parser = new PEMParser(reader);
        Object object = parser.readObject();

        PEMKeyPair keyPair = (PEMKeyPair) object;

        byte[] privateEncoded = keyPair.getPrivateKeyInfo().getEncoded();
        byte[] publicEncoded = keyPair.getPublicKeyInfo().getEncoded();

        KeyFactory factory = KeyFactory.getInstance("RSA");

        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicEncoded);
        PublicKey publicKey = factory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateEncoded);
        PrivateKey privateKey = factory.generatePrivate(privateKeySpec);

        return new KeyPair(publicKey, privateKey);

    }

}
