/**
 * GlusterCoreUtil.java
 *
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.gluster.storage.management.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Entity;


public class GlusterCoreUtil {
	// Convert from Disk list to Qualified disk name list 
	public static final List<String> getQualifiedDiskNames(List<Disk> diskList) {
		List<String> qualifiedDiskNames = new ArrayList<String>();
		for (Disk disk : diskList) {
			qualifiedDiskNames.add(disk.getQualifiedName());
		}
		return qualifiedDiskNames;
	}
	
	public static final List<String> getQualifiedBrickList(Set<Brick> bricks) {
		List<String> qualifiedBricks = new ArrayList<String>();
		for (Brick brick : bricks) {
			qualifiedBricks.add(brick.getQualifiedName());
		}
		return qualifiedBricks;
	}
	
	/**
	 * Compares the two entity lists and returns the list of entities present only in the second argument
	 * <code>newEntities</code>
	 * 
	 * @param oldEntities
	 * @param newEntities
	 * @param caseInsensitive If true, the entity name comparison will be done in case insensitive manner
	 * @return List of entities that are present only in the second argument <code>newEntities</code>
	 */
	public static <T extends Entity> List<T> getAddedEntities(List<T> oldEntities, List<T> newEntities, boolean caseInsensitive) {
		List<T> addedEntities = new ArrayList<T>();
		for(T newEntity : newEntities) {
			if(!containsEntity(oldEntities, newEntity, caseInsensitive)) {
				// old entity list doesn't contain this entity. mark it as new.
				addedEntities.add(newEntity);
			}
		}
		return addedEntities;
	}

	public static <T extends Entity> boolean containsEntity(List<T> entityList, Entity searchEntity, boolean caseInsensitive) {
		String searchEntityName = searchEntity.getName();
		if(caseInsensitive) {
			searchEntityName = searchEntityName.toUpperCase();
		}
		
		for(T entity : entityList) {
			String nextEntityName = entity.getName();
			if(caseInsensitive) {
				nextEntityName = nextEntityName.toUpperCase();
			}
			if(nextEntityName.equals(searchEntityName)) {
				return true;
			}
		}
		
		return false;
	}
}
