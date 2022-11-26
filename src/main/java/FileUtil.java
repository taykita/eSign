import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    public static String getStringFromFile(String path) throws IOException {
        try (FileReader fileReader = new FileReader(path)) {
            StringBuilder stringBuilder = new StringBuilder();
            int c;
            while ((c = fileReader.read()) != -1) {
                stringBuilder.append((char) c);
            }

            return stringBuilder.toString();

        } catch (IOException e) {
            throw new IOException("Ошибка обработки файла", e);
        }
    }

    public static void createFileFromString(String textFile, String path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path, false)) {
            fileWriter.write(textFile);
            fileWriter.flush();
        } catch (IOException e) {
            throw new IOException("Ошибка создания файла", e);
        }
    }

    public static void createFileFromBytes(byte[] bytesFile, String path) throws IOException {
        try (FileOutputStream fileWriter = new FileOutputStream(path)) {
            fileWriter.write(bytesFile);
        } catch (IOException e) {
            throw new IOException("Ошибка создания файла", e);
        }
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new IOException("Ошибка обработки файла", e);
        }
    }
}
