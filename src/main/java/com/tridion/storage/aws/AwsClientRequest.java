package com.tridion.storage.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.auth.BasicAWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.tridion.storage.aws.Utils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * AwsClientRequest.
 *
 * @author Shalivahan Kanur
 * @version 1.0
 *
 */
public class AwsClientRequest
{
    private static final Logger LOG = LoggerFactory.getLogger(AwsClientRequest.class);
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private static AmazonS3 amazonS3;

    public AwsClientRequest(String accessKeyId, String secretAccessKey, String bucketName)
    {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucketName = bucketName;
    }

    public String getAccessKeyId()
    {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId)
    {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey()
    {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey)
    {
        this.secretAccessKey = secretAccessKey;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public void addBinaries(ConcurrentHashMap<String, BinaryIndexData> binaryAdds) throws AmazonClientException, IOException, ParserConfigurationException {

        getS3Client();

        if (amazonS3 == null)
        {
            throw new AmazonClientException("AWS server not instantiated.");
        }

        for (Map.Entry<String, BinaryIndexData> entry : binaryAdds.entrySet())
        {
            BinaryIndexData data = entry.getValue();

            LOG.debug("Dispatching binary content to AWS S3.");
            String fileName = data.getRelativePath();
            String contentType = Utils.getMimeType(data.getFileName());

            InputStream inputBinaryStream = new ByteArrayInputStream(data.getContent().getContent());
            ObjectMetadata objMeta = new ObjectMetadata();
            objMeta.setContentEncoding("UTF-8");
            objMeta.setContentLength(data.getContent().getContent().length);
            if (contentType != null) {
                objMeta.setContentType(contentType);
            }
           objMeta.addUserMetadata("tcmuri",data.getUniqueIndexId());

            if (amazonS3 != null) {
                /*amazonS3.putObject(new PutObjectRequest(
                        bucketName, "images"+fileName.replace("/Images/","/images/"), inputBinaryStream, objMeta)
                        .withCannedAcl(CannedAccessControlList.PublicRead));*/
                amazonS3.putObject(bucketName, "CMS-Websites"+fileName.replace("/Images/","/images/"), inputBinaryStream, objMeta);

                LOG.info("addBinaryObject: Upload complete " + fileName);

            }
            else {
                LOG.error("addBinaryObject: Upload can't be done amazonS3 null " + fileName);
            }
        }
    }

    public void removeFromAws(ConcurrentHashMap<String, BaseIndexData> itemRemovals) throws AmazonClientException, IOException, ParserConfigurationException
    {
        getS3Client();

        if (amazonS3 == null)
        {
            throw new AmazonClientException("AWS server not instantiated.");
        }

        for (Map.Entry<String, BaseIndexData> entry : itemRemovals.entrySet())
        {
            BaseIndexData data = entry.getValue();
            String fileName = data.getRelativePath();

            LOG.debug("Removing: fileName:" + fileName);

            if (amazonS3 != null) {
                amazonS3.deleteObject(bucketName, "images"+fileName.replace("/Images/","/images/"));
                LOG.info("RemoveBinaryObject: Remove complete " + fileName);
            }
            else {
                LOG.error("RemoveBinaryObject: remove can't be done amazonS3 null " + fileName);
            }
        }
     }

    public void addPages(ConcurrentHashMap<String, CharacterIndexData> pageAdds) throws AmazonClientException, IOException, ParserConfigurationException {

        getS3Client();

        if (amazonS3 == null)
        {
            throw new AmazonClientException("AWS server not instantiated.");
        }

        for (Map.Entry<String, CharacterIndexData> entry : pageAdds.entrySet())
        {
            CharacterIndexData data = entry.getValue();

            LOG.debug("Dispatching Pages content to AWS S3.");
            String fileName = data.getRelativePath();
            LOG.debug("Inside Pages Get FilePath =" + fileName);
            String contentType = Utils.getMimeType(data.getFileName());

           // if(fileName.contains(".xml")){

                LOG.debug("Pages -  File =" + fileName + "---" + data.getContent().getString());

                InputStream inputPagesStream = new ByteArrayInputStream(data.getContent().getString().getBytes(StandardCharsets.UTF_8));
                LOG.debug("inputPagesStream =" + inputPagesStream);

                ObjectMetadata objMeta = new ObjectMetadata();
                objMeta.setContentEncoding("UTF-8");
                objMeta.setContentLength(data.getContent().getString().length());
                LOG.debug("data.getContent().getString().length() =" + data.getContent().getString().length());
                if (contentType != null) {
                    objMeta.setContentType(contentType);
                }
                objMeta.addUserMetadata("tcmuri",data.getUniqueIndexId());

                if (amazonS3 != null) {

                    amazonS3.putObject(bucketName, "CMS-Websites"+fileName.replace("/Pages/","/pages/"), inputPagesStream, objMeta);

                    LOG.info("addPagesObject: Upload complete " + fileName);

                }
                else {
                    LOG.error("addPagesObject: Upload can't be done amazonS3 null " + fileName);
                }
            //}
        }
    }

    public void removePagesFromAws(ConcurrentHashMap<String, BaseIndexData> itemRemovals) throws AmazonClientException, IOException, ParserConfigurationException
    {
        getS3Client();

        if (amazonS3 == null)
        {
            throw new AmazonClientException("AWS server not instantiated.");
        }

        for (Map.Entry<String, BaseIndexData> entry : itemRemovals.entrySet())
        {
            BaseIndexData data = entry.getValue();
            String fileName = data.getRelativePath();

            LOG.debug("Removing: fileName:" + fileName);

            if (amazonS3 != null) {
                amazonS3.deleteObject(bucketName, "CMS-Websites"+fileName.replace("/Pages/","/pages/"));
                LOG.info("RemovePagesObject: Remove complete " + fileName);
            }
            else {
                LOG.error("RemovePageObject: remove can't be done amazonS3 null " + fileName);
            }
        }
    }
    public void addComponentPresentations(ConcurrentHashMap<String, BaseIndexData> cpAdds, String ItemType) throws AmazonClientException, IOException, ParserConfigurationException, SAXException, TransformerException {

        getS3Client();

        if (amazonS3 == null)
        {
            throw new AmazonClientException("AWS server not instantiated.");
        }

        for (Map.Entry<String, BaseIndexData> entry : cpAdds.entrySet())
        {
            BaseIndexData data = entry.getValue();
            String strFileName=data._uniqueIndexId.replace(":", "_").replaceAll("-", "_") + "_" + ItemType + ".xml";
            String strText=data._IndexXMl;
            String str=data.toString();

            LOG.debug("IN AWSClientRequest .java - Dispatching ComponentPresentations content to AWS S3.");
            String fileName = "/en_gb/";
            LOG.debug("Inside ComponentPresentations and get FilePath =" + fileName);
            String contentType = Utils.getMimeType(strFileName);

            //if(fileName.contains(".xml")){

                LOG.debug("ComponentPresentations File XmlFile =" + fileName + "= = =" + strFileName);
                LOG.debug("ComponentPresentations File content  :== == == ==" + strText);

                File newFile = new File(strFileName);
                LOG.debug("NewFile name :========" + newFile.getName());
                java.io.FileWriter fw = new java.io.FileWriter(newFile);
                fw.write(strText);
                fw.close();

                InputStream inputCPStream = new FileInputStream(new File(newFile.getPath()));
                LOG.debug("ComponentPresentations InputCPStream =" + inputCPStream);

                ObjectMetadata objMeta = new ObjectMetadata();
                objMeta.setContentEncoding("UTF-8");
                objMeta.setContentLength(newFile.length());

                if (contentType != null) {
                    objMeta.setContentType(contentType);
                }
                objMeta.addUserMetadata("tcmuri",data._uniqueIndexId);

                fileName = fileName + newFile.getName();

                if (amazonS3 != null) {

                    amazonS3.putObject(bucketName, "CMS-Websites"+fileName.replace("/ComponentPresentations/","/componentPresentations/"), inputCPStream, objMeta);

                    LOG.info("addComponentPresentationObject: Upload complete " + fileName);
                }
                else {
                    LOG.error("addComponentPresentationObject: Upload can't be done amazonS3 null " + fileName);
                }
            //}
       }
    }

    public void removeCPFromAws(ConcurrentHashMap<String, BaseIndexData> itemRemovals) throws AmazonClientException, IOException, ParserConfigurationException
    {
        getS3Client();

        if (amazonS3 == null)
        {
            throw new AmazonClientException("AWS server not instantiated.");
        }

        for (Map.Entry<String, BaseIndexData> entry : itemRemovals.entrySet())
        {
            BaseIndexData data = entry.getValue();
            String fileName = data.getRelativePath();

            LOG.debug("Removing: fileName:" + fileName);

            if (amazonS3 != null) {
                amazonS3.deleteObject(bucketName, "CMS-Websites"+fileName.replace("/ComponentPresentations/","/componentPresentations/"));
                LOG.info("Remove Comp.Presentation XML Pages Object: Remove complete " + fileName);
            }
            else {
                LOG.error("Remove Comp.Presentation XML Pages Object: remove can't be done amazonS3 null " + fileName);
            }
        }
    }
     private void getS3Client()
     {
         if (amazonS3 == null) {
             if(isNotEmpty(accessKeyId) && isNotEmpty(secretAccessKey)) {
                 BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
                 amazonS3 = new AmazonS3Client(awsCredentials);
             }
             else
                 amazonS3 = new AmazonS3Client(); //Default Credential Provider Chain looks for credentials in this order ((AWSCredentialsProvider)(new AWSCredentialsProviderChain(new AWSCredentialsProvider[]{new EnvironmentVariableCredentialsProvider(), new SystemPropertiesCredentialsProvider(), new ProfileCredentialsProvider(), new InstanceProfileCredentialsProvider()}) {
         }
     }

    private static boolean isNotEmpty(String... strings) {
        if (strings == null) {
            return false;
        } else {
            String[] var1 = strings;
            int var2 = strings.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                String string = var1[var3];
                if (isEmpty(string)) {
                    return false;
                }
            }

            return true;
        }
    }
    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

}
