
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.MessageDigest;
import javax.crypto.Cipher;

public class CryptFuncs {

    private static final String ALGORITHM = "RSA";
    // private static final MessageDigest md;

    public static byte[] encrypt(byte[] publicKey, byte[] inputData)
            throws Exception {
        PublicKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }
    public static String encrypt_encode(byte[] publicKey,String data) throws Exception{
        return Base64.getEncoder().encodeToString(encrypt(publicKey,data.getBytes()));
    }
    public static String decrypt_decode(byte[] privateKey, String encoded_data) throws Exception{
        return new String(decrypt(privateKey,Base64.getDecoder().decode(encoded_data)));
    }

    public static byte[] decrypt(byte[] privateKey, byte[] inputData)
            throws Exception {

        PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(inputData);

        return decryptedBytes;
    }

    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

        // 512 is keysize
        keyGen.initialize(512, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
    }
    // public static byte[] get_hash(byte[] encoded_msg){
    //     return md.digest(encoded_msg);
    // }
    public static String encode_toB64(String st){
        return Base64.getEncoder().encodeToString(st.getBytes());
    }
    public static String encode_toString(byte[] bt){
        return Base64.getEncoder().encodeToString(bt);
    }
    public static byte[] decode_fromString(String st){
        return Base64.getDecoder().decode(st);
    }

    public static String decode_fromB64(String st){
        return new String(Base64.getDecoder().decode(st));
    }

    public static String hash256(String m){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return new String(md.digest(m.getBytes()));
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            return "";
        }
    }
    public static void main(String[] args) throws Exception {

        KeyPair generateKeyPair = generateKeyPair();
        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();

        byte[] encryptedData = encrypt(publicKey,
                "hi there".getBytes());

    //     byte[] byte_data = "hi there".getBytes();
    //     byte[] encoded_ = Base64.getEncoder().encode(byte_data);
    //     String encoded_1 = new String(encoded_);
    //     String encoded_2 = Base64.getEncoder().encodeToString(byte_data);
    //     byte[] decoded_ = Base64.getDecoder().decode(encoded_);
    //     byte[] decoded_1 = Base64.getDecoder().decode(encoded_1);
    //     byte[] decoded_2 = Base64.getDecoder().decode(encoded_2);
    //     String st = new String(decoded_);
    //     String st_1 = new String(decoded_1);
    //     String st_2 = new String(decoded_2);
        byte[] ed = encryptedData;
        byte[] decryptedData = decrypt(privateKey, ed);
        String msg = "hi there";
        char[] ss = {'a','b','c','d'};

        System.out.println(new String(ss));

        System.out.println(decrypt_decode(privateKey,encrypt_encode(publicKey,msg)));
        // System.out.println(ed);
        System.out.println(encryptedData.length);
        System.out.println((Base64.getEncoder().encode(encryptedData)).length);
        // System.out.println((Base64.getEncoder().encodeToString(encryptedData)));
        System.out.println();
        System.out.println((new String((new String(encryptedData)).getBytes())).length());
        System.out.println((new String(encryptedData)).length());

    //     System.out.println(st);
    //     System.out.println(st_1);
    //     System.out.println(st_2);
    //     System.out.println(encoded_1);
    //     System.out.println(encoded_2);
        System.out.println(new String(decryptedData));


    }

}

