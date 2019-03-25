package com.tridion.storage.aws;

import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AWSS3BinaryIndexProcessor.
 * <p/>
 * Singleton which processes incoming aws s3 actions.
 * <p/>
 * Is used in all overridden Tridion factory classes
 *
 * @author Shalivahan Kanur
 * * @version 1.0
 *
 */

public final class AWSS3BinaryIndexProcessor {

    private static final String INDEXER_NODE = "AWSS3BinaryIndexer";
    private static final String INDEXER_CLASS_ATTRIBUTE = "Class";
    private static final Logger LOG = LoggerFactory.getLogger(AWSS3BinaryIndexProcessor.class);
    private static final ConcurrentHashMap<String, AWSS3BinaryIndex> INDEXER_HANDLERS = new ConcurrentHashMap<String, AWSS3BinaryIndex>();
    private static final ConcurrentHashMap<String, Configuration> INDEXER_CONFIGURATION = new ConcurrentHashMap<String, Configuration>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, BaseIndexData>> NOTIFICATION_REGISTER = new ConcurrentHashMap<String, ConcurrentHashMap<String, BaseIndexData>>();

    // private constructor to prevent normal instantiation
    private AWSS3BinaryIndexProcessor () {
    }

    private static class SingletonHolder {
        public static final AWSS3BinaryIndexProcessor INSTANCE = new AWSS3BinaryIndexProcessor();
    }

    /**
     * Gets the single instance of AWSS3BinaryIndexProcessor.
     *
     * @return single instance of AWSS3BinaryIndexProcessor
     */
    public static AWSS3BinaryIndexProcessor getInstance () {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Configure storage instance.
     *
     * @param storageId The configured storage Id
     * @param configuration The entire
     * @throws ConfigurationException
     */
    public void configureStorageInstance (String storageId, Configuration configuration) throws ConfigurationException {
        LOG.info("Configuration is: " + configuration.toString());
        INDEXER_CONFIGURATION.put(storageId, configuration);
        setAWSS3BinaryIndexClient(storageId);
    }

    /**
     * Sets the aws s3 index client.
     *
     * @param storageId The configured storage Id
     * @throws ConfigurationException
     */
    private void setAWSS3BinaryIndexClient (String storageId) throws ConfigurationException {
        String awsS3BinaryIndexImplementation = INDEXER_CONFIGURATION.get(storageId).getChild(INDEXER_NODE).getAttribute(INDEXER_CLASS_ATTRIBUTE);
        if (!Utils.StringIsNullOrEmpty(awsS3BinaryIndexImplementation)) {
            LOG.info("Using: " + awsS3BinaryIndexImplementation + " as AWS S3 Binary index class for storageId: " + storageId);

            this.loadIndexer(storageId, awsS3BinaryIndexImplementation, INDEXER_CONFIGURATION.get(storageId));
            return;
        }
        throw new ConfigurationException("Could not find AWSS3BinaryIndex class. Please add the class=com.tridion.storage.aws.AWSS3BinaryIndexer attribute");
    }

    /**
     * Load indexer.
     *
     * @param storageId The configured storage Id
     * @param awsS3BinaryIndexImplementation The configured concrete AWSS3BinaryIndex implementation class
     * @param indexerConfiguration The Storage XML node
     * @throws ConfigurationException
     */
    private void loadIndexer (String storageId, String awsS3BinaryIndexImplementation, Configuration indexerConfiguration) throws ConfigurationException {
        if (INDEXER_HANDLERS.get(storageId) == null) {
            LOG.info("Loading " + awsS3BinaryIndexImplementation);

            ClassLoader classLoader = this.getClass().getClassLoader();
            Class<?> indexerClass;
            AWSS3BinaryIndex awsS3BinaryIndex;
            try {
                indexerClass = classLoader.loadClass(awsS3BinaryIndexImplementation);
                awsS3BinaryIndex = (AWSS3BinaryIndex) indexerClass.newInstance();
                awsS3BinaryIndex.configure(indexerConfiguration);
                LOG.info("Configured: " + awsS3BinaryIndexImplementation);

                // is probably useless code.
                if (INDEXER_HANDLERS.containsKey(storageId)) {
                    LOG.warn("This storage instance already has a configured and loaded AWS S3 Index client. Probably storage configuration is wrong.");
                    LOG.warn("Reloading AWS index instance");
                    INDEXER_HANDLERS.remove(storageId);
                }
                INDEXER_HANDLERS.put(storageId, awsS3BinaryIndex);
                LOG.info("Loaded: " + awsS3BinaryIndexImplementation);
            } catch (ClassNotFoundException e) {
                LOG.error(e.getLocalizedMessage(),e);
                throw new ConfigurationException("Could not find class: " + awsS3BinaryIndexImplementation, e);
            } catch (InstantiationException e) {
                LOG.error(e.getLocalizedMessage(),e);
                throw new ConfigurationException("Could instantiate class: " + awsS3BinaryIndexImplementation, e);
            } catch (IllegalAccessException e) {
                LOG.error(e.getLocalizedMessage(),e);
                throw new ConfigurationException("IllegalAccessException: " + awsS3BinaryIndexImplementation, e);
            }
        }
    }

    /**
     * Gets the indexer configuration.
     *
     * @param storageId The configured storage Id
     * @return the indexer configuration node
     * @throws ConfigurationException
     */
    public static Configuration getIndexerConfiguration (String storageId) throws ConfigurationException {

        Configuration configuration = INDEXER_CONFIGURATION.get(storageId);
        if (configuration != null) {
            return configuration;
        }
        throw new ConfigurationException("Indexer configuration not set.");
    }

    /**
     * Register awss3 action.
     *
     * @param transactionId The local thread transaction id. Might correspond with the Tridion transaction Id
     * @param indexData The data object to index.
     */
    public static void registerAction (String transactionId, BaseIndexData indexData) {
        LOG.info("Registering " + indexData.getUniqueIndexId() + ", for: " + indexData.getAction());

        if (!NOTIFICATION_REGISTER.containsKey(transactionId)) {
            NOTIFICATION_REGISTER.put(transactionId, new ConcurrentHashMap<String, BaseIndexData>());
        }
        ConcurrentHashMap<String, BaseIndexData> transactionActions = NOTIFICATION_REGISTER.get(transactionId);



        if (!transactionActions.containsKey(indexData.getUniqueIndexId())) {
            transactionActions.put(indexData.getUniqueIndexId(), indexData);

        } else {
            // Special case where a publish transaction contains a renamed file
            // plus a file
            // with the same name as the renamed file's old name, we ensure that
            // it is not
            // removed, but only re-persisted (a rename will trigger a remove
            // and a persist)

            if (indexData.getAction() == FactoryAction.PERSIST || indexData.getAction() == FactoryAction.UPDATE) {
                // Special case where a publish transaction contains a renamed
                // file
                // plus a file
                // with the same name as the renamed file's old name, we ensure
                // that
                // it is not
                // removed, but only re-persisted (a rename will trigger a
                // remove
                // and a persist)
                // TODO: this might be removed completely.
                LOG.debug(">>> Special case.");
                transactionActions.put(indexData.getUniqueIndexId(), indexData);
            }
        }
    }

    /**
     * Trigger indexing.
     *
     * @param transactionId the Transaction Id
     * @throws BinaryIndexingException
     */
    public void triggerBinaryIndexing (String transactionId, String storageId) throws BinaryIndexingException {
        if (NOTIFICATION_REGISTER.containsKey(transactionId)) {
            LOG.info("Triggering Indexing for transaction: " + transactionId);
            LOG.info("Indexing was requested for Storage Id: " + storageId);
            ConcurrentHashMap<String, BaseIndexData> indexableItems = NOTIFICATION_REGISTER.get(transactionId);

            for (Iterator<Entry<String, BaseIndexData>> iter = indexableItems.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<String, BaseIndexData> actionEntry = iter.next();
                String itemId = actionEntry.getKey();
                BaseIndexData data = actionEntry.getValue();

                if (data.getStorageId().equalsIgnoreCase(storageId)) {
                    LOG.trace("Data is: " + data.toString());
                    LOG.debug("Obtaining ProdSpecs class for: " + data.getStorageId());
                    AWSS3BinaryIndex awsS3BinaryIndex = INDEXER_HANDLERS.get(data.getStorageId());
                    if (awsS3BinaryIndex == null) {
                        throw new BinaryIndexingException("Could not load AWS3BinaryIndexer. Check your configuration.");
                    }
                    LOG.debug(data.getStorageId() + "::" + awsS3BinaryIndex.getClass().getName() + "::" + INDEXER_CONFIGURATION.get(data.getStorageId()).toString());
                    try {
                        processAction(awsS3BinaryIndex, indexableItems, itemId);

                        String pubId = data.getPublicationItemId();
                        LOG.debug("Trigger action for item: " + itemId + ", action: " + data.getAction() + ", storageId: " + data.getStorageId());
                        LOG.debug("Setting Publication Id to: " + pubId);
                        awsS3BinaryIndex.commit(pubId);
                    } finally {
                        // will trigger commits by default 10 times.
                        // remove from notification register.
                        LOG.debug("Removing + " + itemId + " for storageId: " + data.getStorageId() + " from register.");
                        // removing like this may mean that other threads running concurrently
                        // will not see this change.
                        // It is expected that one factory will run as Singleton, so this is no problem
                        // as other factories using the same notification register will not read this entry,
                        // because they have a different storageId.
                        // The main reason to remove it here, is so that other configured DAOFactories will not run it again.
                        iter.remove();
                    }
                } else {
                    LOG.debug("Not processing, this entry is for another factory to process. This factory belongs to {} and the transaction belongs to: {}",storageId,data.getStorageId());
                }
            }
        }
    }

    public static void debugLogRegister () {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Register currently contains:");
            for (Entry<String, ConcurrentHashMap<String, BaseIndexData>> x : NOTIFICATION_REGISTER.entrySet()) {
                LOG.debug(x.getKey());
                for (Entry<String, BaseIndexData> c : x.getValue().entrySet()) {
                    LOG.trace(c.getKey() + ":: " + c.getValue().toString());
                }
            }
        }
    }

    public static void cleanupRegister (String transactionId) {
        LOG.debug("Clearing register for transaction:" + transactionId);
        if (NOTIFICATION_REGISTER.containsKey(transactionId)) {
            NOTIFICATION_REGISTER.remove(transactionId);
        }
    }

    private void processAction (AWSS3BinaryIndex s, ConcurrentHashMap<String, BaseIndexData> actions, String itemId) throws BinaryIndexingException {

        BaseIndexData data = actions.get(itemId);
        switch (data.getIndexType()) {
            case BINARY:
                this.processBinaryAction(s, data);
                break;
        }
    }

    private void processBinaryAction (AWSS3BinaryIndex s, BaseIndexData data) throws BinaryIndexingException {
        LOG.trace("AWSS3 Data type is: " + data.getClass().getName());
        switch (data.getAction()) {
            case PERSIST:
                s.addBinaryToAWSS3((BinaryIndexData) data);
                break;
            case REMOVE:
                LOG.debug("Removing!");
                s.removeBinaryFromAWSS3(data);
                break;
            case UPDATE:
                s.addBinaryToAWSS3((BinaryIndexData) data);
                break;
            default:
                break;
        }
    }

    public void shutDownFactory (String storageId) {
        if (!INDEXER_HANDLERS.isEmpty()) {
            LOG.info("Destroying indexer instance for: " + storageId);
            AWSS3BinaryIndex s = INDEXER_HANDLERS.get(storageId);
            if (s != null) {
                s.destroy();
            }
        }
    }

    /**
     * Log AWS S3 binary index instances.
     */
    public void logAWSS3BinaryIndexInstances () {
        if (LOG.isTraceEnabled()) {
            for (Entry<String, AWSS3BinaryIndex> e : INDEXER_HANDLERS.entrySet()) {
                LOG.trace(e.getKey() + "::" + e.getValue().getClass().getName());
            }
        }
    }
}
