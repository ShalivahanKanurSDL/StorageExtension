package com.tridion.storage.aws;

import com.tridion.broker.StorageException;
import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import com.tridion.storage.persistence.JPADAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.lang.reflect.Field;
import java.text.ParseException;

/**
 * An extended factory class responsible for deploying Tridion Items 
 * and indexing content. 
 * 
 * Used in case Tridion JPA storage is configured in the storage layer. 
 * This class hooks into the Spring Loader, which is used by Tridion to
 * create JPA based DAO objects.
 *
 * @author Shalivahan Kanur
 * * @version 1.0
 *
 */

@Component("JPAAWSCPDAOFactory")
@Qualifier("JPAAWSCPDAOFactory")
@Scope ("prototype")
// Primary hides the other JPA factories. Hence, see this.configure()
@Primary
public class JPAAWSCPDAOFactory extends JPADAOFactory implements ApplicationContextAware
{
	private Logger log = LoggerFactory.getLogger(JPAAWSCPDAOFactory.class);
	private String storageId = "";
	private AWSS3CPIndexProcessor awss3CPIndexProcessor;
	private boolean isExtendedDaoFactory = true;
	private static final String DAO_FACTORY_CLASS_ATTRIBUTE = "Class";

	/*
	 * Spring specific, thanks to DN
	 */
	private static ApplicationContext APPLICATION_CONTEXT;

	public JPAAWSCPDAOFactory()
	{
		super(null, "MSSQL");// not important what we sent. This instance is
								// never going to be used
		log.trace("Spring Constructor init.");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tridion.storage.persistence.JPADAOFactory#configureBundle(com.tridion
	 * .configuration.Configuration)
	 */
	public void configureBundle(Configuration storageDAOBundleConfiguration) throws ConfigurationException
	{
		// first set the right value for the private field called
		// 'applicationContext'.
		try
		{
			setPrivateField(this, "applicationContext", APPLICATION_CONTEXT, log);
		}
		catch (IllegalAccessException e)
		{
			log.error(e.getMessage());
		}

		// configure the bundle like we normally do
		super.configureBundle(storageDAOBundleConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */

	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
	{
		APPLICATION_CONTEXT = applicationContext;
		log.trace("Setting app context from spring.");

	}

	private static void setPrivateField(final Object fieldOwner, final String fieldName, final Object value, Logger log) throws IllegalAccessException
	{
		final Field privateField = getPrivateFieldRec(fieldOwner.getClass(), fieldName, log);

		if (privateField != null)
		{

			final boolean accessible = privateField.isAccessible();
			privateField.setAccessible(true);

			privateField.set(fieldOwner, value);

			privateField.setAccessible(accessible);
		}
	}

	private static Field getPrivateFieldRec(final Class<?> clazz, final String fieldName, Logger log)
	{
		for (Field field : clazz.getDeclaredFields())
		{
			if (fieldName.equals(field.getName()))
			{
				return field;
			}
		}
		final Class<?> superClazz = clazz.getSuperclass();

		if (superClazz != null)
		{
			return getPrivateFieldRec(superClazz, fieldName, log);
		}

		return null;
	}

	public JPAAWSCPDAOFactory(String storageId, String dialect)
	{
		super(storageId, dialect);
		// Needed to correctly instantiate a xmlindexer
		this.storageId = storageId;
	}

	/*
	 * End Spring specific
	 */
	/*
	 * Extension specific
	 */
	/*
	 * (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPADAOFactory#configure(com.tridion.
	 * configuration.Configuration)
	 */
	@Override
	public void configure(Configuration storageDAOBundleConfiguration) throws ConfigurationException
	{
		super.configure(storageDAOBundleConfiguration);
		// Get the instance here, because Spring instantiates the JPADAOFactory
		// twice.
		log.trace("Fetching awss3PageIndexProcessor instance.");
		awss3CPIndexProcessor = AWSS3CPIndexProcessor.getInstance();
		awss3CPIndexProcessor.configureStorageInstance(storageId, storageDAOBundleConfiguration);
		log.trace("Processor instance number: " + awss3CPIndexProcessor.getInstanceNumber());
		log.trace("Instances of Xml Index: ");
		awss3CPIndexProcessor.logComponentPresentationIndexInstances();
	}

	/*
	 * Overridden entry point for Tridion deploy commits
	 * 
	 * (non-Javadoc)
	 * @see
	 * com.tridion.storage.persistence.JPADAOFactory#commitTransaction(java.
	 * lang.String)
	 */
	@Override
	public void commitTransaction(String transactionId) throws StorageException
	{
		try
		{
			if (this.isExtendedDaoFactory) {
				log.info("Start committing transaction: " + transactionId);
				long start = System.currentTimeMillis();
				super.commitTransaction(transactionId);
				long awsS3Start = System.currentTimeMillis();
				log.debug("Commit AWSS3ComponentPresentations xml's Indexing Start");
				awss3CPIndexProcessor.triggerIndexing(transactionId, this.storageId);
				log.info("End committing transaction: " + transactionId);
				log.info("Committing ComponentPresentations Xml Files took: " + (System.currentTimeMillis() - awsS3Start) + " ms.");
				log.info("Total Commit Time was: " + (System.currentTimeMillis() - start) + " ms.");
			} else {
				log.info("Not triggering any special stuff, as this instance for storage Id '{}' needs to behave like the normal DAOFactory",this.storageId);
				super.commitTransaction(transactionId);
			}
		}
		catch (StorageException e)
		{
			this.logException(e);
			throw e;
		}
		catch (IndexingException e)
		{
			this.logException(e);
			throw new StorageException(e);

		}
		catch (ClassNotFoundException e)
		{
			this.logException(e);
			throw new StorageException(e);
		}
		catch (InstantiationException e)
		{
			this.logException(e);
			throw new StorageException(e);
		}
		catch (IllegalAccessException e)
		{
			this.logException(e);
			throw new StorageException(e);
		}
		catch (ConfigurationException e)
		{
			this.logException(e);
			throw new StorageException(e);
		}
		catch (ParseException e)
		{
			this.logException(e);
			throw new StorageException(e);
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally
		{
			AWSS3CPIndexProcessor.debugLogRegister(log);
			//AWSS3CPIndexProcessor.cleanupRegister(transactionId, log);
		}
	}

	private void logException(Exception e)
	{
		log.error(e.getMessage());
		log.error(Utils.stacktraceToString(e.getStackTrace()));
	}

	@Override
	public void shutdownFactory()
		{ if (this.isExtendedDaoFactory) {
			awss3CPIndexProcessor.shutDownFactory(storageId);
		}
		super.shutdownFactory();
	}
}
