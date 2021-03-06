/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.model;

public class Event {
	public enum EVENT_TYPE {
		BRICKS_ADDED,
		BRICKS_REMOVED,
		BRICKS_CHANGED,
		VOLUME_STATUS_CHANGED,
		ALERT_CREATED,
		ALERT_REMOVED,
		VOLUME_OPTIONS_RESET,
		VOLUME_OPTION_SET,
		VOLUME_CHANGED,
		GLUSTER_SERVER_CHANGED,
		DEVICES_ADDED,
		DEVICES_REMOVED,
		DEVICES_CHANGED,
		DISCOVERED_SERVER_CHANGED
	}
	
	private EVENT_TYPE eventType;
	private Object eventData;
	
	public Event(EVENT_TYPE eventType, Object eventData) {
		this.eventType = eventType;
		this.eventData = eventData;
	}

	public EVENT_TYPE getEventType() {
		return eventType;
	}

	public void setEventType(EVENT_TYPE eventType) {
		this.eventType = eventType;
	}

	public Object getEventData() {
		return eventData;
	}

	public void setEventData(Object eventData) {
		this.eventData = eventData;
	}
}
