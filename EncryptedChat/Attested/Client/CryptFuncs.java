
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
import java.util.Arrays;
import java.util.Base64;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import java.io.*;
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
        // System.out.println(encryptedBytes.length);
        return encryptedBytes;
    }
    
    public static byte[] encrypt_private(byte[] privateKey, byte[] inputData) throws Exception{
        PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(inputData);
        // System.out.println(encryptedBytes.length);
        return encryptedBytes;
    }

    public static String encrypt_encode(byte[] publicKey,String data) throws Exception{
        return Base64.getEncoder().encodeToString(encrypt_public_long(publicKey,data.getBytes()));
    }
    public static String encrypt_private_encode(byte[] privateKey,String data) throws Exception{
        return Base64.getEncoder().encodeToString(encrypt_private_long(privateKey,data.getBytes()));
    }
    public static String decrypt_decode(byte[] privateKey, String encoded_data) throws Exception{
        return new String(decrypt_private_long(privateKey,Base64.getDecoder().decode(encoded_data)));
    }
    public static String decrypt_public_decode(byte[] pk, String encoded_data) throws Exception{
        return new String(decrypt_public_long(pk,Base64.getDecoder().decode(encoded_data)));
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
    public static byte[] decrypt_public(byte[] pk, byte[] inputData) throws Exception{
        PublicKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(pk));

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
        keyGen.initialize(2048, random);

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

    public static byte[] encrypt_public_long(byte[] key, byte[] data) throws Exception{
        
        int packets = (int)((data.length-1)/245)+1;
        byte[] output= new byte[256*packets];
        int k = 0;
        for (int i=0;i<packets-1;i++){
            byte[] slice = Arrays.copyOfRange(data, i*245, (i+1)*245);
            byte[] tmp = encrypt(key,slice);
            for(int j=0;j<tmp.length;j++){
                output[k] = tmp[j];
                k +=1;
            }
        }
        byte[] slice = Arrays.copyOfRange(data, (packets-1)*245,data.length);
        byte[] tmp = encrypt(key,slice);
        for(int j=0;j<tmp.length;j++){
            output[k] = tmp[j];
            k +=1;
        }
        return output;
    }

    public static byte[] encrypt_private_long(byte[] key, byte[] data) throws Exception{
        
        int packets = (int)((data.length-1)/245)+1;
        byte[] output= new byte[256*packets];
        int k = 0;
        for (int i=0;i<packets-1;i++){
            byte[] slice = Arrays.copyOfRange(data, i*245, (i+1)*245);
            byte[] tmp = encrypt_private(key,slice);
            for(int j=0;j<tmp.length;j++){
                output[k] = tmp[j];
                k +=1;
            }
        }
        byte[] slice = Arrays.copyOfRange(data, (packets-1)*245,data.length);
        byte[] tmp = encrypt_private(key,slice);
        for(int j=0;j<tmp.length;j++){
            output[k] = tmp[j];
            k +=1;
        }
        return output;
    }
    // public static byte[] encrypt_private_long(byte[] key, byte[] data){


    // }
    // public static byte[] decrypt_public_long(byte[] key, byte[] data){

    // }
    public static byte[] decrypt_private_long(byte[] key, byte[] data) throws Exception{
        byte[] output = new byte[(data.length/256)*245];
        int k = 0;
        for(int i=0;i<data.length/256;i++){
            byte[] slice = Arrays.copyOfRange(data, i*256, (i+1)*256);
            byte[] tmp = decrypt(key,slice);
            for(int j=0;j<tmp.length;j++){
                output[k] = tmp[j];
                k +=1;
            }
        }
        byte[] outp = Arrays.copyOfRange(output, 0,k);
        return outp;
    }

    public static byte[] decrypt_public_long(byte[] key, byte[] data) throws Exception{
        byte[] output = new byte[(data.length/256)*245];
        int k = 0;
        for(int i=0;i<data.length/256;i++){
            byte[] slice = Arrays.copyOfRange(data, i*256, (i+1)*256);
            byte[] tmp = decrypt_public(key,slice);
            for(int j=0;j<tmp.length;j++){
                output[k] = tmp[j];
                k +=1;
            }
        }
        byte[] outp = Arrays.copyOfRange(output, 0,k);
        return outp;
    }

    // public static byte[] encrypt_private_long(byte[] key, byte[] data) throws Exception{

    // }
    // public static encryptPublicKey(byte[] pk, byte[] input_data){

    // }
    // public static void main(String[] args) throws Exception {

    //     KeyPair generateKeyPair = generateKeyPair();
    //     byte[] publicKey = generateKeyPair.getPublic().getEncoded();
    //     byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
    //     String inp1 = "hi there";

    //     String inp = "hi there adsknas;kdn adnas d;asidfalksfm;kasdasn;fn sdafkjnsajdfnasjndf sajnfsan dfiusanfiunsadfn asfn saiudd nfpiuasdnfadns pfunaspiuf subndfpuasdn fpiadsunfuisadnfpiusadn pfnuapsd fnj;ais dasjdiasid AISHD ASIHF PIUSDAHFPIU SFUSDJN SNFnsfnisnf dnfnsifnpsanfudf;as fuadisunfsaiunfosdanidfudsfnsdjfn";
    //     byte[] encryptedData = encrypt_private_long(privateKey,
    //             inp1.getBytes());
        
        
    //     String s=  encode_toString(encryptedData);
    //     System.out.println(encryptedData.length);
    //     System.out.println(s);

    // //     byte[] byte_data = "hi there".getBytes();
    // //     byte[] encoded_ = Base64.getEncoder().encode(byte_data);
    // //     String encoded_1 = new String(encoded_);
    // //     String encoded_2 = Base64.getEncoder().encodeToString(byte_data);
    // //     byte[] decoded_ = Base64.getDecoder().decode(encoded_);
    // //     byte[] decoded_1 = Base64.getDecoder().decode(encoded_1);
    // //     byte[] decoded_2 = Base64.getDecoder().decode(encoded_2);
    // //     String st = new String(decoded_);
    // //     String st_1 = new String(decoded_1);
    // //     String st_2 = new String(decoded_2);
    // //     byte[] ed = encryptedData;
    //     byte[] decryptedData = decrypt_public_long(publicKey, encryptedData);
    //     String dd = new String(decryptedData);
    //     // System.out.println(new String(decryptedData));
    //     System.out.println(inp.length());
    //     System.out.println(dd.length());

    // }
    // // public static void authentication_check() throws Exception{
    // //     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    // //     KeyPair generateKeyPair1 = generateKeyPair();
    // //     byte[] pk1 = generateKeyPair1.getPublic().getEncoded();
    //     byte[] sk1 = generateKeyPair1.getPrivate().getEncoded();
    //     KeyPair generateKeyPair2 = generateKeyPair();
    //     byte[] pk2 = generateKeyPair2.getPublic().getEncoded();
    //     byte[] sk2 = generateKeyPair2.getPrivate().getEncoded();

    //     while(true){
    //         String s= br.readLine();
    //         String s_pk1en = encrypt_encode(pk1,s);
    //         // String s_sken = encrypt_private_encode(sk,s);
    //         String s_hash = hash256(s_pk1en);
    //         String s_hash_sk2en = encrypt_private_encode(sk2,s_hash);
    //         String s_hash_dec = decrypt_public_decode(pk2,s_hash_sk2en);
    //         System.out.println(s_hash);
    //         System.out.println(s_hash_dec);
    //         System.out.println(encode_toString(s_hash.getBytes()));
    //         System.out.println(encode_toString(s_hash_dec.getBytes()));

    //     }
    // }
}

