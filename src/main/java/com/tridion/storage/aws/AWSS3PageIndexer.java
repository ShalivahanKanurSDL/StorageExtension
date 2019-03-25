package com.tridion.storage.aws;

import com.amazonaws.AmazonClientException;
import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import com.tridion.data.CharacterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processing Pages from Transport Packages and picking "Pages" and Storing in S3 Bucket
 *
 * @author Shalivahan Kanur
 * @version 1.0
 */
public class AWSS3PageIndexer implements AWSS3PageIndex
{
    private static final Logger LOG = LoggerFactory.getLogger(AWSS3PageIndexer.class);
    private String accessKeyId = null;
    private String secretAccessKey = null;
    private String bucketName = null;
    private ConcurrentHashMap<String, BaseIndexData> itemRemovals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CharacterIndexData> pageAdds = new ConcurrentHashMap<>();
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

        //IMPORTANT - that below indexerConfiguration "AWSS3PageIndexer" should be same has what we provide in cd_Storage.config.xml
        //Otherwise it will not create bean to process the deployment

        Configuration indexerConfiguration = configuration.getChild("AWSS3PageIndexer");

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
    public void addPageXmlToAWSS3(CharacterIndexData data) throws IndexingException
    {
        if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
        {
            LOG.error("Addition failed. Unique ID is empty");
            return;
        }
        this.pageAdds.put(data.getUniqueIndexId(), data);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tridion.storage.aws.AWSS3PageIndex#removeXmlFromAWSS3
     */
    @Override
    public void removePageXmlFromAWSS3(BaseIndexData data) throws IndexingException
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
    public void commit(String publicationId) throws IndexingException
    {
        try
        {
            this.commitAddPagesToAWSS3();
            this.removeItemsFromAwsS3();
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
        pageAdds.clear();
        itemRemovals.clear();
    }

    private void commitAddPagesToAWSS3() throws IOException, ParserConfigurationException, IndexingException {
        if (this.pageAdds.size() > 0)
        {
            LOG.info("Adding Xml's to AWS S3.");

            AwsClientRequest awsClient = new AwsClientRequest(this.accessKeyId, this.secretAccessKey, this.bucketName);
            awsClient.addPages(pageAdds);
        }
    }

    private void removeItemsFromAwsS3() throws IOException, ParserConfigurationException, IndexingException {
        if (this.itemRemovals.size() > 0)
        {

            AwsClientRequest awsClient = new AwsClientRequest(this.accessKeyId, this.secretAccessKey, this.bucketName);
            awsClient.removePagesFromAws(itemRemovals);
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
