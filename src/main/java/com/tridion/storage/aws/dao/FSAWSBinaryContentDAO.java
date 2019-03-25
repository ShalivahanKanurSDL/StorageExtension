package com.tridion.storage.aws.dao;

import com.tridion.broker.StorageException;
import com.tridion.configuration.ConfigurationException;
import com.tridion.storage.BinaryContent;
import com.tridion.storage.filesystem.FSBinaryContentDAO;
import com.tridion.storage.filesystem.FSEntityManager;
import com.tridion.storage.aws.IndexType;
import com.tridion.storage.aws.TridionBinaryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 * FSAWSBinaryContentDAO.
 * @author Shalivahan Kanur
 * @version 1.0
 */

public class FSAWSBinaryContentDAO extends FSBinaryContentDAO
{
    private static final Logger LOG = LoggerFactory.getLogger(FSAWSBinaryContentDAO.class);
    private String storageId;

    public FSAWSBinaryContentDAO(String storageId, String storageName, File storageLocation, FSEntityManager entityManager) throws ConfigurationException
    {
        super(storageId, storageName, storageLocation, entityManager);

        LOG.trace("FSAWSBinaryContentDAO init. (EM)");
        this.storageId = storageId;
    }

    public FSAWSBinaryContentDAO(String storageId, String storageName,	File storageLocation)
    {
        super(storageId, storageName, storageLocation);
        this.storageId = storageId;
        LOG.trace("FSAWSBinaryContentDAO init.");
    }

    /* (non-Javadoc)
     * @see com.tridion.storage.filesystem.FSBinaryContentDAO#create(com.tridion.storage.BinaryContent, java.lang.String)
     */
    @Override
    public void create(BinaryContent binaryContent, String relativePath) throws StorageException
    {
        super.create(binaryContent, relativePath);

        LOG.info("Found a binary to index (Create): " + relativePath);
        TridionBinaryProcessor.registerAddition(binaryContent, relativePath, relativePath, this.storageId);
    }

    /* (non-Javadoc)
     * @see com.tridion.storage.filesystem.FSBinaryContentDAO#update(com.tridion.storage.BinaryContent, java.lang.String, java.lang.String)
     */
    @Override
    public void update(BinaryContent binaryContent, String originalRelativePath, String newRelativePath) throws StorageException
    {
        super.update(binaryContent, originalRelativePath, newRelativePath);
        LOG.info("Checking update for: " + originalRelativePath);

        LOG.info("Found a binary to index (Update): " + newRelativePath);
        TridionBinaryProcessor.registerAddition(binaryContent, originalRelativePath, newRelativePath, this.storageId);
    }

    /* (non-Javadoc)
     * @see com.tridion.storage.filesystem.FSBinaryContentDAO#remove(int, int, java.lang.String, java.lang.String)
     */
    @Override
    public void remove(int publicationId, int binaryId, String variantId, String relativePath) throws StorageException
    {
        super.remove(publicationId, relativePath);
        TridionBinaryProcessor.registerItemRemoval("binary:" + publicationId + "-" + binaryId, IndexType.BINARY, LOG, Integer.toString(publicationId), relativePath, this.storageId,"");
    }
}