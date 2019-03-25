/**
 * Copyright 2011-2013 Radagio & SDL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tridion.storage.aws;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;

/**
 * AWSS3PageIndexProcessor.
 *
 * Singleton which processes incoming xml actions.
 * Is used in all overridden Tridion factory classes
 *
 * @author Shalivahan Kanur
 * @version 1.0
 */

public final class AWSS3PageIndexProcessor
{

    private static final String INDEXER_NODE = "AWSS3PageIndexer";
    private static final String INDEXER_CLASS_ATTRIBUTE = "Class";
    private static String INDEXER_FilePath_ATTRIBUTE = "FilePath";
    private Logger log = LoggerFactory.getLogger(AWSS3PageIndexProcessor.class);
    private static ConcurrentHashMap<String, Configuration> indexerConfiguration = new ConcurrentHashMap<String, Configuration>();
    private ConcurrentHashMap<String, AWSS3PageIndex> PAGE_INDEXER = new ConcurrentHashMap<String, AWSS3PageIndex>();
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, BaseIndexData>> notificationRegister = new ConcurrentHashMap<String, ConcurrentHashMap<String, BaseIndexData>>();
    // Test variable
    private static int instanceNumber = 0;

    // private constructor to prevent normal instantiation
    private AWSS3PageIndexProcessor()
    {
    }

    /**
     * SingletonHolder.
     *
    */
    private static class SingletonHolder
    {
        public static final AWSS3PageIndexProcessor INSTANCE = new AWSS3PageIndexProcessor();
    }

    /**
     * Gets the single instance of AWSS3PageIndexProcessor.
     *
     * @return single instance of AWSS3PageIndexProcessor
     */
    public static AWSS3PageIndexProcessor getInstance()
    {
        instanceNumber++;
        return SingletonHolder.INSTANCE;
    }

    /**
     * Configure storage instance.
     *
     * @param storageId
     * @param configuration
     * @throws ConfigurationException
     */
    public void configureStorageInstance(String storageId, Configuration configuration) throws ConfigurationException
    {
        log.info("Configuration is: " + configuration.toString());
        indexerConfiguration.put(storageId, configuration);
        setAWSS3PageIndexClient(storageId);
    }

    /**
     * Sets the aws s3 index client.
     *
     * @param storageId The configured storage Id
     * @throws ConfigurationException
     */
    private void setAWSS3PageIndexClient(String storageId) throws ConfigurationException
    {
        String awsS3PageIndexImplementation = indexerConfiguration.get(storageId).getChild(INDEXER_NODE).getAttribute(INDEXER_CLASS_ATTRIBUTE);
        if (!Utils.StringIsNullOrEmpty(awsS3PageIndexImplementation)) {
            log.info("Using: " + awsS3PageIndexImplementation + " as AWS S3 Page index class for storageId: " + storageId);

            this.loadIndexer(storageId, awsS3PageIndexImplementation, indexerConfiguration.get(storageId));
            return;
        }
        throw new ConfigurationException("Could not find AWSS3PageIndex class. Please add the class=com.tridion.storage.aws.AWSS3PageIndexer attribute");

    }



    /**
     * Load indexer.
     *
     * @param storageId
     * @param awsS3PageIndexImplementation
     * @param indexerConfiguration
     * @throws ConfigurationException
     */
	private void loadIndexer(String storageId, String awsS3PageIndexImplementation, Configuration indexerConfiguration) throws ConfigurationException
	{
		if (PAGE_INDEXER.get(storageId) == null)
		{
			log.info("Loading " + awsS3PageIndexImplementation);

			ClassLoader classLoader = this.getClass().getClassLoader();
			Class<?> indexerClass;
            AWSS3PageIndex s = null;
			try
			{
				indexerClass = classLoader.loadClass(awsS3PageIndexImplementation);
				s = (AWSS3PageIndex) indexerClass.newInstance();
				s.configure(indexerConfiguration);
				log.info("Configured: " + awsS3PageIndexImplementation);

				if (PAGE_INDEXER.containsKey(storageId))
				{
					log.warn("This storage instance already has a configured and loaded Page Index client. Probably storage configuration is wrong.");
					log.warn("Reloading Page index instance");
                    PAGE_INDEXER.remove(storageId);
				}
                PAGE_INDEXER.put(storageId, s);
				log.info("Loaded: " + awsS3PageIndexImplementation);
			}
			catch (ClassNotFoundException e)
			{
                log.error(e.getLocalizedMessage(),e);
				throw new ConfigurationException("Could not find class: " + awsS3PageIndexImplementation, e);
			}
			catch (InstantiationException e)
			{
                log.error(e.getLocalizedMessage(),e);
				throw new ConfigurationException("Could instantiate class: " + awsS3PageIndexImplementation, e);
			}
			catch (IllegalAccessException e)
			{
                log.error(e.getLocalizedMessage(),e);
				throw new ConfigurationException("IllegalAccessException: " + awsS3PageIndexImplementation, e);
			}
		}
	}

    /**
     * Gets the indexer configuration.
     *
     * @param storageId
     * @return the indexer configuration node
     * @throws ConfigurationException
     */
    public static Configuration getIndexerConfiguration(String storageId) throws ConfigurationException
    {
        if (indexerConfiguration != null)
        {
            return indexerConfiguration.get(storageId);
        }
        throw new ConfigurationException("Indexer configuration not set.");
    }

    /**
     * Register xml action.
     *
     * @param transactionId
     * @param indexData
     * @param _log
     */
    public static void registerAction(String transactionId, BaseIndexData indexData, Logger _log)
    {
        _log.info("Registering " + indexData.getUniqueIndexId() + ", for: " + indexData.getAction());
        _log.info("Registering Transaction ID" + transactionId);
        if (!notificationRegister.containsKey(transactionId))
        {
            notificationRegister.put(transactionId, new ConcurrentHashMap<String, BaseIndexData>());
        }
        ConcurrentHashMap<String, BaseIndexData> transactionActions = notificationRegister.get(transactionId);

        if (!transactionActions.containsKey(indexData.getUniqueIndexId()))
        {
            transactionActions.put(indexData.getUniqueIndexId(), indexData);
        }

        else
        {
            // Special case where a publish transaction contains a renamed file
            // plus a file
            // with the same name as the renamed file's old name, we ensure that
            // it is not
            // removed, but only re-persisted (a rename will trigger a remove
            // and a persist)

            if (indexData.getAction() == FactoryAction.PERSIST || indexData.getAction() == FactoryAction.UPDATE)
            {
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
                _log.debug(">>> Special case.");
                transactionActions.put(indexData.getUniqueIndexId(), indexData);
            }
        }
    }

    /**
     * Trigger indexing.
     *
     * @param transactionId
     * @throws IndexingException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ConfigurationException
     * @throws ParseException
     */
    public void triggerIndexing(String transactionId, String storageId) throws IndexingException, ClassNotFoundException, InstantiationException, IllegalAccessException, ConfigurationException, ParseException
    {
        if (notificationRegister.containsKey(transactionId))
        {
            log.info("Triggering Page Indexing for transaction: " + transactionId);

            ConcurrentHashMap<String, BaseIndexData> actions = notificationRegister.get(transactionId);

            for (Iterator<Entry<String, BaseIndexData>> iter = actions.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<String, BaseIndexData> actionEntry = iter.next();
                String itemId = actionEntry.getKey();
                BaseIndexData data = actionEntry.getValue();

                if (data.getStorageId().equalsIgnoreCase(storageId)) {
                    log.trace("Data is: " + data.toString());
                    log.debug("Obtaining ProdSpecs class for: " + data.getStorageId());
                    AWSS3PageIndex awsS3PageXmlIndex = PAGE_INDEXER.get(data.getStorageId());
                    if (awsS3PageXmlIndex == null) {
                        throw new IndexingException("Could not load AWS3PageIndexer. Check your configuration.");
                    }
                    log.debug(data.getStorageId() + "::" + awsS3PageXmlIndex.getClass().getName() + "::" + indexerConfiguration.get(data.getStorageId()).toString());
                    try {
                        processAction(awsS3PageXmlIndex, actions, itemId);
                        String pubId = data.getPublicationItemId();
                        log.debug("Trigger action for item: " + itemId + ", action: " + data.getAction() + ", storageId: " + data.getStorageId());
                        log.debug("Setting Publication Id to: " + pubId);
                        awsS3PageXmlIndex.commit(pubId);
                    } finally {
                        // will trigger commits by default 10 times.
                        // remove from notification register.
                        log.debug("Removing + " + itemId + " for storageId: " + data.getStorageId() + " from register.");
                        // removing like this may mean that other threads running concurrently
                        // will not see this change.
                        // It is expected that one factory will run as Singleton, so this is no problem
                        // as other factories using the same notification register will not read this entry,
                        // because they have a different storageId.
                        // The main reason to remove it here, is so that other configured DAOFactories will not run it again.
                        iter.remove();
                    }
                } else {
                    log.debug("Not processing, this entry is for another factory to process. This factory belongs to {} and the transaction belongs to: {}",storageId,data.getStorageId());
                }
            }
        }
    }

    public static void debugLogRegister(Logger log)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Register currently contains:");
            for (Entry<String, ConcurrentHashMap<String, BaseIndexData>> x : notificationRegister.entrySet())
            {
                log.debug(x.getKey());
                for (Entry<String, BaseIndexData> c : x.getValue().entrySet())
                {
                    log.trace(c.getKey() + ":: " + c.getValue().toString());
                }
            }
        }
    }

    public static void cleanupRegister(String transactionId, Logger log)
    {
        log.debug("Clearing register for transaction:" + transactionId);
        if (notificationRegister.containsKey(transactionId))
        {
            notificationRegister.remove(transactionId);
        }
    }

    private void processAction(AWSS3PageIndex x, ConcurrentHashMap<String, BaseIndexData> actions, String itemId) throws IndexingException
    {

        BaseIndexData data = actions.get(itemId);
        switch (data.getIndexType())
        {
            case BINARY:
                //this.processBinaryAction(s, data);
                break;
            case PAGE:
                this.processItemAction(x,data,"PAGE");
                break;
            case COMPONENT_PRESENTATION:
                this.processItemAction(x,data,"Component");
                break;
        }
    }



	/*private void processBinaryAction(SearchIndex s, BaseIndexData data) throws IndexingException
	{
		log.trace("Search Data type is: " + data.getClass().getName());
		switch (data.getAction())
		{
			case PERSIST:
				s.addBinaryToIndex((BinaryIndexData)data);
				break;
			case REMOVE:
				log.debug("Removing!");
				s.removeBinaryFromIndex(data);
				break;
			case UPDATE:
				s.addBinaryToIndex((BinaryIndexData)data);
				break;
			default:
				break;
		}
	}*/

    public void shutDownFactory(String storageId)
    {
        if (PAGE_INDEXER != null)
        {
            log.info("Destroying indexer instance for: " + storageId);
            AWSS3PageIndex s = PAGE_INDEXER.get(storageId);
            if (s != null)
            {
                s.destroy();
            }
            PAGE_INDEXER.clear();
        }
    }

    private void processItemAction(AWSS3PageIndex x, BaseIndexData data,String ItemType ) throws IndexingException
    {
        switch (data.getAction())
        {
            case PERSIST:
                log.info(ItemType + " Persist: ");
                x.addPageXmlToAWSS3((CharacterIndexData) data);
                break;
            case REMOVE:
                log.info("Delete from S3");
                x.removePageXmlFromAWSS3(data);
                break;
            case UPDATE:
                x.addPageXmlToAWSS3((CharacterIndexData) data);
                break;
        }
    }

    // Test method
    public int getInstanceNumber()
    {
        return instanceNumber;
    }

    /**
     * Log Page index instances.
     */
    public void logPageIndexInstances()
    {
        if (log.isTraceEnabled())
        {
            for (Entry<String, AWSS3PageIndex> e : PAGE_INDEXER.entrySet())
            {
                log.trace(e.getKey() + "::" + e.getValue().getClass().getName());
            }
        }
    }

	/*private void logException(Exception e)
	{
		log.error(e.getMessage());
		log.error(Utils.stacktraceToString(e.getStackTrace()));
	}*/

}
