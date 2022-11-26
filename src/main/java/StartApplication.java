import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class StartApplication {

    private static final Logger logger = LoggerFactory.getLogger(StartApplication.class);

    private static final int L = 576;

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        if (args.length == 2) {
            signing(args);
        } else if (args.length == 4) {
            logger.debug("Пришел запрос на проверку документа по пути {} с подписью по пути {} и публичным ключом {}", args[0], args[1], args[2]);

            String stringDoc = FileUtil.getStringFromFile(args[0]);

            MessageDigest digest = MessageDigest.getInstance(args[3]);
            byte[] encodedHash = digest.digest(stringDoc.getBytes(StandardCharsets.UTF_8));
            String currentDocHash = bytesToHex(encodedHash);

            byte[] stringSign = FileUtil.getBytesFromFile(args[1]);

            String publicKeyString = FileUtil.getStringFromFile(args[2]);
            String modulus = publicKeyString.substring(publicKeyString.indexOf("modulus: "));
            String pubExp = modulus.substring(modulus.indexOf("public exponent: ")).split(" ")[2];
            modulus = publicKeyString.substring(publicKeyString.indexOf("modulus: ")).split(" ")[1].split("\n")[0];
            logger.trace(modulus);
            logger.trace(pubExp);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus, 10), new BigInteger(pubExp, 10));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);
            logger.trace(publicKey.toString());

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] result = cipher.doFinal(stringSign);
            String trustDocHash = bytesToHex(result);
            logger.trace(trustDocHash);

            if (trustDocHash.equals(currentDocHash)) {
                System.out.println("Документ подписан верно!");
            } else {
                System.out.println("Подпись неверна или изменен состав документа!");
            }
        } else {
            throw new IllegalArgumentException("Ошибка обработки входных данных. Неверное количество аргументов");
        }

    }

    private static void signing(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        logger.debug("Пришел запрос на шифрование документа по пути {}", args[0]);

        String stringDoc = FileUtil.getStringFromFile(args[0]);
        logger.debug("Пришел документ на подпись. Его текст - \n{}", stringDoc);

        MessageDigest digest = MessageDigest.getInstance(args[1]);
        byte[] encodedHash = digest.digest(stringDoc.getBytes(StandardCharsets.UTF_8));

        String trustDocHash = bytesToHex(encodedHash);
        logger.debug("Вычислен хэш документа - {}", trustDocHash);

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] data = cipher.doFinal(encodedHash);

        String docHash = bytesToHex(data);
        logger.debug("Цифровая подпись документа - {}", docHash);

        FileUtil.createFileFromBytes(data, args[0] + ".sign");
        FileUtil.createFileFromString(keyPair.getPublic().toString(), args[0] + ".publicKey");

        System.out.println("===================================================================");
        System.out.println("====================Документ подписан успешно!=====================");
        System.out.println("===================================================================");
        System.out.println(args[0] + ".sign - подпись");
        System.out.println(args[0] + ".publicKey - публичный ключ");
    }


    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
