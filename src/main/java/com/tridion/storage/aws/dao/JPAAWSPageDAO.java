package com.tridion.storage.aws.dao;

import com.tridion.broker.StorageException;
import com.tridion.data.CharacterData;
import com.tridion.storage.aws.IndexType;
import com.tridion.storage.aws.TridionPageProcessor;
import com.tridion.storage.dao.PageDAO;
import com.tridion.storage.persistence.JPAPageDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * JPAAWSPageContentDAO .
 *
 * @author Shalivahan Kanur
 * @version 1.0
 */
@Component("JPAAWSPageDAO")
@Scope("prototype")
public class JPAAWSPageDAO extends JPAPageDAO implements PageDAO
{
	private Logger log = LoggerFactory.getLogger(JPAAWSPageDAO.class);
	private String storageId;
	
	public JPAAWSPageDAO (String storageId, EntityManagerFactory entityManagerFactory, EntityManager entityManager, String storageName)
	{
		super(storageId, entityManagerFactory, entityManager, storageName);
		this.storageId = storageId;
		log.debug("JPAAWSPageDAO init. (EM)");

	}

	public JPAAWSPageDAO (String storageId, EntityManagerFactory entityManagerFactory, String storageName)
	{
		super(storageId, entityManagerFactory, storageName);
		this.storageId = storageId;
		log.debug("JPAAWSPageDAO init.");
	}

	/* (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPAPageDAO#create(com.tridion.data.CharacterData, java.lang.String)
	 */
	public void create(CharacterData page, String relativePath) throws StorageException
	{
		log.debug("Create.");
		super.create(page, relativePath);
		log.info("Found a Pages to index (Create): " + relativePath);
		TridionPageProcessor.registerItemAddition(page, relativePath, relativePath, this.storageId, log);

	}

	/* (non-Javadoc)
	 * @see com.tridion.storage.filesystem.FSPageDAO#update(com.tridion.data.CharacterData, java.lang.String, java.lang.String)
	 */
	// Note: An update triggers a create always. So this might not be needed
	@Override
	public void update(CharacterData page, String originalRelativePath, String newRelativePath) throws StorageException
	{
		log.debug("Update. Orgpath=" + originalRelativePath);
		super.update(page, originalRelativePath, newRelativePath);
		TridionPageProcessor.registerItemAddition(page, originalRelativePath, newRelativePath, this.storageId, log);

	}

	/* (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPAPageDAO#remove(int, int, java.lang.String)
	 */
	@Override
	public void remove(int publicationId, int pageId, String relativePath) throws StorageException
	{
		super.remove(publicationId, pageId, relativePath);
		TridionPageProcessor.registerItemRemoval("tcm:"+publicationId+"-"+pageId+"-64", IndexType.PAGE, log, Integer.toString(publicationId),relativePath, this.storageId,"");
	}
}
