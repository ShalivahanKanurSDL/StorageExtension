package com.tridion.storage.aws;

import com.amazonaws.AmazonClientException;
import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processing DCP and picking xml generated files and Storing in S3 Bucket
 *
 * @author Shalivahan Kanur
 * @version 1.0
 */
public class AWSS3CPIndexer implements AWSS3CPIndex
{
    private static final Logger LOG = LoggerFactory.getLogger(AWSS3CPIndexer.class);
    private String accessKeyId = null;
    private String secretAccessKey = null;
    private String bucketName = null;
    private ConcurrentHashMap<String, BaseIndexData> itemRemovals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BaseIndexData> componentPresentationAdds = new ConcurrentHashMap<>();
    /*
	 * (non-Javadoc)
	 * @see
	 * com.tridion.storage.aws.AWSS3PageIndex#configure(com.tridion
	 * .configuration.Configuration)
	 */

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        LOG.debug("Configuration is: " + configuration.toString());

        //IMPORTANT - that below indexerConfiguration "AWSS3CPIndexer" should be same has what we provide in cd_Storage.config.xml
        //Otherwise it will not create bean to process the deployment

        Configuration indexerConfiguration = configuration.getChild("AWSS3CPIndexer");

        if (indexerConfiguration.hasAttribute("AwsAccessKeyId"))
        {
            String accessKeyId = indexerConfiguration.getAttribute("AwsAccessKeyId");

            if (!Utils.StringIsNullOrEmpty(accessKeyId))
            {
                LOG.info("Setting accessKeyId to: " + accessKeyId);
                this.accessKeyId = accessKeyId;
            }
        }

        if (indexerConfiguration.hasAttribute("AwsSecretAccessKeyId"))
        {
            String secretAccessKey = indexerConfiguration.getAttribute("AwsSecretAccessKeyId");

            if (!Utils.StringIsNullOrEmpty(secretAccessKey))
            {
                LOG.info("Setting secretAccessKey to: " + secretAccessKey);
                this.secretAccessKey = secretAccessKey;
            }
        }

        if (indexerConfiguration.hasAttribute("BucketName"))
        {
            String bucketName = indexerConfiguration.getAttribute("BucketName");

            if (!Utils.StringIsNullOrEmpty(bucketName))
            {
                LOG.info("Setting bucketName to: " + bucketName);
                this.bucketName = bucketName;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tridion.storage.aws.AWSS3PageIndex#addXmlToAWSS3
     */
    @Override
    public void addCPXmlToAWSS3(BaseIndexData data,String value ) throws IndexingException
    {
        if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
        {
            LOG.error("Addition failed. Unique ID is empty");
            return;
        }
        this.componentPresentationAdds.put(data.getUniqueIndexId(), data);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tridion.storage.aws.AWSS3PageIndex#removeXmlFromAWSS3
     */
    @Override
    public void removeCPXmlFromAWSS3(BaseIndexData data) throws IndexingException
    {
        if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
        {
            LOG.error("Removal addition failed. Unique ID empty");
            return;
        }
        this.itemRemovals.put(data.getUniqueIndexId(), data);
    }

    /*
     * (non-Javadoc)
     * @see com.tridion.storage.aws.AWSS3PageIndex#commit()
     */
    @Override
    public void commit(String publicationId) throws IndexingException, TransformerException, SAXException {
        try
        {
            this.commitAddCPXmlToAWSS3();
            //this.removeItemsFromAwsS3();
        }
        catch (AmazonClientException e)
        {
            LOG.error(e.getLocalizedMessage(),e);
            throw new IndexingException("AWS S3 Server Exception: " + e.getMessage());
        }
        catch (IOException e)
        {
            LOG.error(e.getLocalizedMessage(),e);
            throw new IndexingException("IO Exception: " + e.getMessage());
        }
        catch (ParserConfigurationException e)
        {
            LOG.error(e.getLocalizedMessage(),e);
            throw new IndexingException("ParserConfigurationException: " + e.getMessage());
        }
        finally
        {
            LOG.info("Clearing out registers.");
            this.clearRegisters();
        }
    }

    private void clearRegisters()
    {
        componentPresentationAdds.clear();
        itemRemovals.clear();
    }

    private void commitAddCPXmlToAWSS3() throws IOException, ParserConfigurationException, IndexingException, TransformerException, SAXException {
        if (this.componentPresentationAdds.size() > 0)
        {
            LOG.info("Adding Component Presentation's Manually Generated Xml Files to AWS S3.");

            AwsClientRequest awsClient = new AwsClientRequest(this.accessKeyId, this.secretAccessKey, this.bucketName);
            awsClient.addComponentPresentations(componentPresentationAdds,"");
        }
    }

    private void removeItemsFromAwsS3() throws IOException, ParserConfigurationException, IndexingException {
        if (this.itemRemovals.size() > 0)
        {

            AwsClientRequest awsClient = new AwsClientRequest(this.accessKeyId, this.secretAccessKey, this.bucketName);
            awsClient.removeCPFromAws(itemRemovals);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tridion.storage.aws.AWSS3PageIndex#destroy()
    */
    @Override
    public void destroy()
    {

    }
}
