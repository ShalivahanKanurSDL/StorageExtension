package com.tridion.storage.aws;

import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;


/**
 * AWSS3BinaryIndex.
 *
 * Interface which is used to inject AWS S3 index implementations in
 * the factory classes.
 *
 * Configured classes, which are configured in cd_storage_conf.xml,
 * based on this interface are called by the configured factory class upon
 * commit of Tridion item deployment.
 *
 * @author Shalivahan Kanur
 * @version 1.0
 */
public interface AWSS3BinaryIndex
{

    /**
     * Configure.
     *
     * Method to load the Indexer configuration element. This element has to be placed
     * inside each Storage node in cd_storage_conf.xml for which indexing has to be
     * enabled.
     *
     * @param configuration the Storage node configuration element
     * @throws ConfigurationException Any Configuration Exception
     */
    void configure(Configuration configuration) throws ConfigurationException;

    /**
     * Adds a binary item to be indexed.
     *
     * @param data the data
     * @throws BinaryIndexingException the indexing exception
     */
    void addBinaryToAWSS3(BinaryIndexData data) throws BinaryIndexingException;

    /**
     * Removes the binary from index.
     *
     * @param data the data
     * @throws BinaryIndexingException the indexing exception
     */
    void removeBinaryFromAWSS3(BaseIndexData data) throws BinaryIndexingException;

    /**
     * Commit.
     *
     * Handles the actual sending of to be indexed or removed items from a awss3 index.
     *
     * @param publicationId the publication id
     * @throws BinaryIndexingException the indexing exception
     */
    void commit(String publicationId) throws BinaryIndexingException;

    /**
     * Destroy.
     *
     * Should destroy any open clients to free resources.
     */
    void destroy();
}

