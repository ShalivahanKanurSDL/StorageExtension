package com.tridion.storage.aws;

import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;

/**
 * AWSS3CPIndex.
 * 
 * Interface which is used to inject ComponentPresentation index implementations in
 * the factory classes.
 * 
 * Configured classes, which are configured in cd_storage_conf.xml,
 * based on this interface are called by the configured factory class upon
 * commit of Tridion item deployment.
 *
 * @author Shalivahan Kanur
 * @version 1.0
 */

public interface AWSS3CPIndex
{

	/**
	 * Configure.
	 * 
	 * Method to load the Indexer configuration element. This element has to be placed
	 * inside each Storage node in cd_storage_conf.xml for which indexing has to be
	 * enabled. 
	 * 
	 * @param configuration the Storage node configuration element
	 * @throws ConfigurationException
	 */
	void configure(Configuration configuration) throws ConfigurationException;

	/**
	 * Adds a Page item to be indexed.
	 * 
	 * @param data the data
	 * @throws IndexingException the indexing exception
	 */
	void addCPXmlToAWSS3(BaseIndexData data, String value) throws IndexingException;

	/**
	 * Removes the Page item from index.
	 * 
	 * @param data the data
	 * @throws IndexingException the indexing exception
	 */
	void removeCPXmlFromAWSS3(BaseIndexData data) throws IndexingException;

	/**
	 * Commit. 
	 * 
	 * Handles the actual sending of to be indexed or removed items from a xml index.
	 * 
	 * @param publicationId the publication id
	 * @throws IndexingException the indexing exception
	 */
	void commit(String publicationId) throws IndexingException, TransformerException, SAXException;

	/**
	 * Destroy.
	 * 
	 * Should destroy any open clients to free resources.
	 */
	void destroy();
}
