package com.tridion.storage.aws.dao;

import com.tridion.broker.StorageException;
import com.tridion.configuration.ConfigurationException;
import com.tridion.storage.BinaryContent;
import com.tridion.storage.dao.BinaryContentDAO;
import com.tridion.storage.persistence.JPABinaryContentDAO;
import com.tridion.storage.aws.IndexType;
import com.tridion.storage.aws.TridionBinaryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * JPAAWSBinaryContentDAO.
 * @author Shalivahan Kanur
 * @version 1.0
 *
 */
@Component("JPAAWSBinaryContentDAO")
@Scope("prototype")
public class JPAAWSBinaryContentDAO extends JPABinaryContentDAO implements BinaryContentDAO
{
    private static final Logger LOG = LoggerFactory.getLogger(JPAAWSBinaryContentDAO.class);
    private String storageId;

    public JPAAWSBinaryContentDAO(String storageId, EntityManagerFactory entityManagerFactory, EntityManager entityManager, String storageName) throws ConfigurationException
    {
        super(storageId, entityManagerFactory, entityManager, storageName);
        this.storageId = storageId;
        LOG.trace("JPAAWSBinaryContentDAO init. (EM)");
    }

    public JPAAWSBinaryContentDAO(String storageId, EntityManagerFactory entityManagerFactory, String storageName)
    {
        super(storageId, entityManagerFactory, storageName);
        this.storageId = storageId;
        LOG.trace("JPAAWSBinaryContentDAO init.");
    }

    /* (non-Javadoc)
     * @see com.tridion.storage.persistence.JPABinaryContentDAO#create(com.tridion.storage.BinaryContent, java.lang.String)
     */
    @Override
    public void create(BinaryContent binaryContent, String relativePath) throws StorageException
    {
        super.create(binaryContent, relativePath);
        LOG.info("Found a binary to index (Create): " + relativePath);
        TridionBinaryProcessor.registerAddition(binaryContent, relativePath, relativePath, this.storageId);
    }

    /* (non-Javadoc)
     * @see com.tridion.storage.persistence.JPABinaryContentDAO#remove(int, int, java.lang.String, java.lang.String)
     */
    @Override
    public void remove(int publicationId, int binaryId, String variantId, String relativePath) throws StorageException
    {
        super.remove(publicationId, binaryId, variantId, relativePath);
        TridionBinaryProcessor.registerItemRemoval("binary:" + publicationId + "-" + binaryId, IndexType.BINARY, LOG, Integer.toString(publicationId), relativePath, this.storageId,"");
    }

    /* (non-Javadoc)
     * @see com.tridion.storage.persistence.JPABinaryContentDAO#update(com.tridion.storage.BinaryContent, java.lang.String, java.lang.String)
     */
    @Override
    public void update(BinaryContent binaryContent, String originalRelativePath, String newRelativePath) throws StorageException
    {
        super.update(binaryContent, originalRelativePath, newRelativePath);
        LOG.info("Checking update for: " + originalRelativePath);

        LOG.info("Found a binary to index (Update): " + newRelativePath);
        TridionBinaryProcessor.registerAddition(binaryContent, originalRelativePath, newRelativePath, this.storageId);
    }
}