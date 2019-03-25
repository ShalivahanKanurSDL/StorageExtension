import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import com.tridion.storage.BinaryContent;
import com.tridion.storage.aws.*;
import org.apache.commons.io.*;

public class Test {

    public static void main(String[] args)throws Exception {

        String fileName = "ballon-burner_tcm5-279.jpg";

        String path = fileName;

        File file = new File(Test.class.getResource(fileName).toURI());

        byte[] binaryContent =  FileUtils.readFileToByteArray(file);
        BinaryContent binary = new BinaryContent();
        binary.setContent(binaryContent);


        String fileExtension = Utils.GetBinaryFileExtension(path);

        BinaryIndexData data = new BinaryIndexData(FactoryAction.PERSIST, IndexType.BINARY, Integer.toString(5), "DefaultAWS","");
        data.setContent(binary);
        data.setUniqueIndexId("tcm:10-12223");
        data.setFileName(path);
        data.setFileType(fileExtension);
        data.setRelativePath("/resources/" + fileName);

        ConcurrentHashMap<String, BinaryIndexData> binaryAdd = new ConcurrentHashMap<>();
        binaryAdd.put("uniqueId-tcm:123-12312",data);
        try {
            AwsClientRequest awsClient = new AwsClientRequest("AKIAJHTXGY4MNU47PXHQ","Qwb4ivZV/GBRhXdlpntfV7Fvhod0h/UV38YHrCSL","com-sdlproducts-tridion-houseoffraserr8-dev-storage-extension");
            awsClient.addBinaries(binaryAdd);
            System.out.println("Upload complete.");
        } catch (Exception ex) {
            System.out.println("Unable to upload file, upload was aborted."+ex.getMessage());
        }

    }
}

