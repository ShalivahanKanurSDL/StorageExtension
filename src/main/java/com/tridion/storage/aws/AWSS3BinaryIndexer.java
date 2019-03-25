package com.tridion.storage.aws;

import com.amazonaws.AmazonClientException;
import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

 /**
  * @author Shalivahan Kanur
  * * @version 1.0
  *
  */
public class AWSS3BinaryIndexer implements AWSS3BinaryIndex
{
    private static final Logger LOG = LoggerFactory.getLogger(AWSS3BinaryIndexer.class);
    private String accessKeyId = null;
    private String secretAccessKey = null;
    private String bucketName = null;
    private ConcurrentHashMap<String, BaseIndexData> itemRemovals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BinaryIndexData> binaryAdds = new ConcurrentHashMap<>();
    /*
	 * (non-Javadoc)
	 * @see
	 * com.tridion.storage.aws.AWSS3BinaryIndex#configure(com.tridion
	 * .configuration.Configuration)
	 */

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        LOG.debug("Configuration is: " + configuration.toString());
        Configuration indexerConfiguration = configuration.getChild("AWSS3BinaryIndexer");

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
     * com.tridion.storage.aws.AWSS3BinaryIndex#addBinaryToAWSS3
     */
    @Override
    public void addBinaryToAWSS3(BinaryIndexData data) throws BinaryIndexingException
    {
        if (Utils.StringIsNullOrEmpty(data.getUniqueIndexId()))
        {
            LOG.error("Addition failed. Unique ID is empty");
            return;
        }
        this.binaryAdds.put(data.getUniqueIndexId(), data);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tridion.storage.aws.AWSS3BinaryIndex#removeBinaryFromAWSS3
     */
    @Override
    public void removeBinaryFromAWSS3(BaseIndexData data) throws BinaryIndexingException
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
     * @see com.tridion.storage.aws.AWSS3BinaryIndex#commit()
     */
    @Override
    public void commit(String publicationId) throws BinaryIndexingException
    {
        try
        {
            this.commitAddBinariesToAwsS3();
            this.removeItemsFromAwsS3();
        }
        catch (AmazonClientException e)
        {
            LOG.error(e.getLocalizedMessage(),e);
            throw new BinaryIndexingException("AWS S3 Server Exception: " + e.getMessage());
        }
        catch (IOException e)
        {
            LOG.error(e.getLocalizedMessage(),e);
            throw new BinaryIndexingException("IO Exception: " + e.getMessage());
        }
        catch (ParserConfigurationException e)
        {
            LOG.error(e.getLocalizedMessage(),e);
            throw new BinaryIndexingException("ParserConfigurationException: " + e.getMessage());
        }
        finally
        {
            LOG.info("Clearing out registers.");
            this.clearRegisters();
        }
    }

    private void clearRegisters()
    {
        binaryAdds.clear();
        itemRemovals.clear();
    }

    private void commitAddBinariesToAwsS3() throws IOException, ParserConfigurationException, BinaryIndexingException {
        if (this.binaryAdds.size() > 0)
        {
            LOG.info("Adding binaries to AWS S3.");

            AwsClientRequest awsClient = new AwsClientRequest(this.accessKeyId, this.secretAccessKey, this.bucketName);
            awsClient.addBinaries(binaryAdds);
        }
    }

    private void removeItemsFromAwsS3() throws IOException, ParserConfigurationException, BinaryIndexingException {
        if (this.itemRemovals.size() > 0)
        {

            AwsClientRequest awsClient = new AwsClientRequest(this.accessKeyId, this.secretAccessKey, this.bucketName);
            awsClient.removeFromAws(itemRemovals);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tridion.storage.aws.AWSS3BinaryIndex#destroy()
    */
    @Override
    public void destroy()
    {

    }
}
