package com.tridion.storage.aws;

import com.tridion.broker.StorageException;
import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import com.tridion.storage.filesystem.FSDAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * FSAWSDAOFactory
 *
 * An extended factory class responsible for deploying Tridion binary
 *  content into the AWS s3 bucket.
 *
 * Used in case File System storage is configured in the storage layer.
 *
 * @author Shalivahan Kanur
 * * @version 1.0
 *
 */

@Component ("FSAWSDAOFactory")
@Qualifier ("FSAWSDAOFactory")
@Scope ("prototype")
// Primary hides the other DAO factories. Hence, see this.configure()

@Primary
public class FSAWSDAOFactory extends FSDAOFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FSAWSDAOFactory.class);
    private static final String DAO_FACTORY_CLASS_ATTRIBUTE = "Class";
    private String storageId = "";
    private boolean isExtendedDaoFactory = true;

    private final AWSS3BinaryIndexProcessor awsS3BinaryIndexProcessor = AWSS3BinaryIndexProcessor.getInstance();

    public FSAWSDAOFactory (String storageId, String tempFileSystemTransactionLocation) {
        super(storageId, tempFileSystemTransactionLocation);
        this.storageId = storageId;
    }

    /*
     * (non-Javadoc)
     * @see com.tridion.storage.filesystem.FSDAOFactory#configure(com.tridion.
     * configuration.Configuration)
     */
    @Override
    public void configure (Configuration configuration) throws ConfigurationException {
        super.configure(configuration);

        final String daoFactoryClassName = configuration.getAttribute(DAO_FACTORY_CLASS_ATTRIBUTE);

        if (daoFactoryClassName.equalsIgnoreCase(FSDAOFactory.class.getCanonicalName())) {
            LOG.info("This seems to be a normal FSDAOFactory ( {} ) for Storage Id: '{}', so not triggering and configuring the extension.", daoFactoryClassName, this.storageId);
            this.isExtendedDaoFactory = false;
            return;
        }

        LOG.debug("Configuration: {}", configuration.toString());

        awsS3BinaryIndexProcessor.configureStorageInstance(storageId, configuration);
        LOG.debug("Instances of AWS S3 Index: ");
        awsS3BinaryIndexProcessor.logAWSS3BinaryIndexInstances();

    }

    /*
     * Overridden entry point for Tridion deploy commits
     * (non-Javadoc)
     * @see
     * com.tridion.storage.filesystem.FSDAOFactory#commitTransaction(java.lang
     * .String)
     */
    @Override
    public void commitTransaction (String transactionId) throws StorageException {
        try {
            if (this.isExtendedDaoFactory) {
                LOG.info("Start committing transaction: " + transactionId);
                long start = System.currentTimeMillis();
                super.commitTransaction(transactionId);
                long awsS3Start = System.currentTimeMillis();
                LOG.debug("Commit AWSS3Binary Indexing Start");
                awsS3BinaryIndexProcessor.triggerBinaryIndexing(transactionId, this.storageId);
                LOG.info("End committing transaction: " + transactionId);
                LOG.info("Committing AWSS3Binary Indexing took: " + (System.currentTimeMillis() - awsS3Start) + " ms.");
                LOG.info("Total Commit Time was: " + (System.currentTimeMillis() - start) + " ms.");
            } else {
                LOG.info("Not triggering any special stuff, as this instance for storage Id '{}' needs to behave like the normal DAOFactory",this.storageId);
                super.commitTransaction(transactionId);
            }
        } catch (StorageException e) {
            LOG.error(e.getLocalizedMessage(),e);
            throw e;
        } catch (BinaryIndexingException e) {
            LOG.error(e.getLocalizedMessage(),e);
            throw new StorageException(e);

        } finally {
            if (this.isExtendedDaoFactory) {
                AWSS3BinaryIndexProcessor.debugLogRegister();
                AWSS3BinaryIndexProcessor.cleanupRegister(transactionId);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.tridion.storage.filesystem.FSDAOFactory#shutdownFactory()
     */
    @Override
    public void shutdownFactory () {
        if (this.isExtendedDaoFactory) {
            awsS3BinaryIndexProcessor.shutDownFactory(storageId);
        }
        super.shutdownFactory();
    }
}
