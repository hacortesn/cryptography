package com.hacn.project.logic;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

/**
 * Created by familia on 17/04/2016.
 */
public class ECCSignature implements Crypto {

    long timeMillies = 0;


    PrivateKey privKey;
    PublicKey pubKey;

    ECCurve curve = new ECCurve.F2m(
            239, // m
            36, // k
            new BigInteger("32010857077C5431123A46B808906756F543423E8D27877578125778AC76", 16), // a
            new BigInteger("790408F2EEDAF392B012EDEFB3392F30F4327C0CA3F31FC383C422AA8C16", 16)); // b
    ECParameterSpec params = new ECParameterSpec(
            curve,
            curve.decodePoint(Hex.decode("0457927098FA932E7C0A96D3FD5B706EF7E5F5C156E16B7E7C86038552E91D61D8EE5077C33FECF6F1A16B268DE469C3C7744EA9A971649FC7A9616305")), // G
            new BigInteger("220855883097298041197912187592864814557886993776713230936715041207411783"), // n
            BigInteger.valueOf(4)); // h
    ECPrivateKeySpec priKeySpec = new ECPrivateKeySpec(
            new BigInteger("145642755521911534651321230007534120304391871461646461466464667494947990"), // d
            params);
    ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(
            curve.decodePoint(Hex.decode("045894609CCECF9A92533F630DE713A958E96C97CCB8F5ABB5A688A238DEED6DC2D9D0C94EBFB7D526BA6A61764175B99CB6011E2047F9F067293F57F5")), // Q
            params);


    public ECCSignature() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
        privKey = fact.generatePrivate(priKeySpec);
        pubKey = fact.generatePublic(pubKeySpec);
    }

    @Override
    public byte[] encrypt(byte[] bytes) throws Exception {

        Cipher cipher = Cipher.getInstance("ECIESwithAES");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        long t1 = System.currentTimeMillis();
        byte[] bytes1 = cipher.doFinal(bytes);
        long t2 = System.currentTimeMillis();

        timeMillies = t2 - t1;


        return bytes1;
    }

    @Override
    public byte[] decrypt(byte[] message) throws Exception {

        Cipher cipher = Cipher.getInstance("ECIESwithAES");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        long t1 = System.currentTimeMillis();
        byte[] bytes1 = cipher.doFinal(message);
        long t2 = System.currentTimeMillis();

        timeMillies = t2 - t1;
        return bytes1;

    }


    @Override
    public long getTime() {
        return timeMillies;
    }

    public static void main(String args[]) {
        try {

            ECCSignature eccSignature = new ECCSignature();
            byte[] encode = eccSignature.encrypt("hola.text".getBytes());

            ECCSignature eccSignature2 = new ECCSignature();
            byte[] decode = eccSignature2.decrypt(encode);

            System.out.println(new String(decode));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
