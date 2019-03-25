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
package com.tridion.storage.aws.dao;

import com.tridion.broker.StorageException;
import com.tridion.storage.ComponentPresentation;
import com.tridion.storage.aws.*;
import com.tridion.storage.dao.ComponentPresentationDAO;
import com.tridion.storage.persistence.JPAComponentPresentationDAO;
import com.tridion.storage.util.ComponentPresentationTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collection;

/**
 * JPAAWSComponentPresentationDAO.
 * 
 * @author Shalivahan Kanur
 * @version 1.0
 *
 */
@Component("JPAAWSComponentPresentationDAO")
@Scope("prototype")
public class JPAAWSComponentPresentationDAO extends JPAComponentPresentationDAO implements ComponentPresentationDAO
{
	
	private Logger log = LoggerFactory.getLogger(JPAAWSComponentPresentationDAO.class);
	private String storageId;
	
	public JPAAWSComponentPresentationDAO(String storageId, EntityManagerFactory entityManagerFactory, EntityManager entityManager, String storageType)
	{
		super(storageId, entityManagerFactory, entityManager, storageType);
		this.storageId = storageId;
		log.debug("JPAAWSComponentPresentationDAO Init. (EM)");
	}

	public JPAAWSComponentPresentationDAO(String storageId, EntityManagerFactory entityManagerFactory, String storageType)
	{
		super(storageId, entityManagerFactory, storageType);
		this.storageId = storageId;
		log.debug("JPAAWSComponentPresentationDAO Init.");
	}

	/* (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPAComponentPresentationDAO#create(com.tridion.storage.ComponentPresentation, com.tridion.storage.util.ComponentPresentationTypeEnum)
	 */
	@Override
	public void create(ComponentPresentation itemToCreate, ComponentPresentationTypeEnum componentPresentationType) throws StorageException
	{
		log.debug("Create.");
		TridionPublishableItemProcessor tp = new TridionPublishableItemProcessor
				(
						new String(itemToCreate.getContent()),
						FactoryAction.PERSIST,
						IndexType.COMPONENT_PRESENTATION,
						Integer.toString(itemToCreate.getPublicationId()),
						"dcp:" + itemToCreate.getPublicationId() + "-" + itemToCreate.getComponentId() + "-" + itemToCreate.getTemplateId(),
						this.storageId);

		String strippedItem = tp.processComponentPresentationSource();
		if (!Utils.StringIsNullOrEmpty(strippedItem))
		{
			itemToCreate.setContent(strippedItem.getBytes());
		}
		super.create(itemToCreate, componentPresentationType);
		log.info("Found a Comp.Presentations to index (Create): " + itemToCreate.getComponentId());
		//TridionCPProcessor.registerItemAddition(itemToCreate, log);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPAComponentPresentationDAO#remove(com.tridion.storage.ComponentPresentation, com.tridion.storage.util.ComponentPresentationTypeEnum)
	 */
	@Override
	public void remove(ComponentPresentation itemToRemove, ComponentPresentationTypeEnum componentPresentationType) throws StorageException
	{
		super.remove(itemToRemove, componentPresentationType);
		log.debug("Removal method 1");
		TridionCPProcessor.registerItemRemoval(
				"dcp:"+itemToRemove.getPublicationId()+"-"+itemToRemove.getComponentId() + "-" + itemToRemove.getTemplateId(), IndexType.COMPONENT_PRESENTATION, log, Integer.toString(itemToRemove.getPublicationId()), this.storageId,"");

	}

	/*
	 * (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPAComponentPresentationDAO#remove(int, int, int, com.tridion.storage.util.ComponentPresentationTypeEnum)
	 */
	@Override
	public void remove(int publicationId, int componentId, int componentTemplateId, ComponentPresentationTypeEnum componentPresentationType) throws StorageException
	{

		super.remove(publicationId, componentId, componentTemplateId, componentPresentationType);
		log.debug("Removal method 2");
		TridionCPProcessor.registerItemRemoval(
				"dcp:"+publicationId+"-"+componentId + "-" + componentTemplateId, IndexType.COMPONENT_PRESENTATION, log, Integer.toString(publicationId), this.storageId,"");
	}

	/*
	 * (non-Javadoc)
	 * @see com.tridion.storage.persistence.JPAComponentPresentationDAO#update(com.tridion.storage.ComponentPresentation, com.tridion.storage.util.ComponentPresentationTypeEnum)
	 */
	@Override
	public void update(ComponentPresentation itemToUpdate, ComponentPresentationTypeEnum componentPresentationType) throws StorageException
	{
		log.debug("Update.");
		TridionPublishableItemProcessor tp = new TridionPublishableItemProcessor(
				new String(itemToUpdate.getContent()),
				FactoryAction.UPDATE,
				IndexType.COMPONENT_PRESENTATION,
				Integer.toString(itemToUpdate.getPublicationId()),
				"dcp:" + itemToUpdate.getPublicationId() + "-" + itemToUpdate.getComponentId() + "-" + itemToUpdate.getTemplateId()
				, this.storageId);

		String strippedItem = tp.processComponentPresentationSource();
		if (!Utils.StringIsNullOrEmpty(strippedItem))
		{
			itemToUpdate.setContent(strippedItem.getBytes());
		}
		super.update(itemToUpdate, componentPresentationType);
	}

}
