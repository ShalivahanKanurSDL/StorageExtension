import com.tridion.storage.BinaryContent;
import com.tridion.storage.aws.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestPackage {

    public static void main(String[] args) throws Exception {

        String zipFilePath  = "C:\\Projects\\HOF\\Search\\Storage Extension\\src\\test\\resources\\" + "tcm_0-103693-66560.Content.zip";

        String destDir      = "C:\\Projects\\HOF\\Search\\Storage Extension\\src\\test\\resources\\" + "zip";

        //UnZip the "Transport Zip Package"
        unzip(zipFilePath, destDir);

        File dir = new File("C:\\Projects\\HOF\\Search\\Storage Extension\\src\\test\\resources\\zip\\tcm_0-103693-66560.Content\\Binaries");

        String fileName;
        List<File> fileList = new ArrayList<>();

        //Get the List of All Files from "Binaries Folder" from the UnZip Package
        List<File> files = readFileFromDirectory(dir,fileList);

        for (File item : files) {
            fileName = item.getName();
            //File file = new File(Test.class.getResource(fileName).toURI());

            byte[] binaryContent = FileUtils.readFileToByteArray(item);
            BinaryContent binary = new BinaryContent();
            binary.setContent(binaryContent);

            String fileExtension = Utils.GetBinaryFileExtension(fileName);

            BinaryIndexData data = new BinaryIndexData(FactoryAction.PERSIST, IndexType.BINARY, Integer.toString(5), "DefaultAWS","");
            data.setContent(binary);
            data.setUniqueIndexId("tcm:10-12223");
            data.setFileName(fileName);
            data.setFileType(fileExtension);
            data.setRelativePath("/resources/" + fileName);

            ConcurrentHashMap<String, BinaryIndexData> binaryAdd = new ConcurrentHashMap<>();
            binaryAdd.put("uniqueId-tcm:123-12312", data);
            try {
                AwsClientRequest awsClient = new AwsClientRequest("AKIAJHTXGY4MNU47PXHQ", "Qwb4ivZV/GBRhXdlpntfV7Fvhod0h/UV38YHrCSL", "com-sdlproducts-tridion-houseoffraserr8-dev-storage-extension");
                awsClient.addBinaries(binaryAdd);
                System.out.println("Upload complete.");
            } catch (Exception ex) {
                System.out.println("Unable to upload file, upload was aborted." + ex.getMessage());
            }
        }
    }

    public static List<File> readFileFromDirectory(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        for (File subFile : files) {
            if (subFile.isDirectory()) {
                readFileFromDirectory(subFile, fileList);
            } else {
                fileList.add(subFile);
            }
        }
        return fileList;
    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


