/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.client;

import java.util.List;

import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerDetailsResponse;
import com.gluster.storage.management.core.model.ServerListResponse;

public class DiscoveredServersClient extends AbstractClient {
	private static final String RESOURCE_NAME = "discoveredservers";

	public DiscoveredServersClient(String serverName) {
		super(serverName);
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	public List<Server> getDiscoveredServers() {
		@SuppressWarnings("unchecked")
		ServerListResponse<Server> response = (ServerListResponse<Server>) fetchResource(ServerListResponse.class);
		return response.getData();
	}

	public Server getServer(String serverName) {
		@SuppressWarnings("unchecked")
		ServerDetailsResponse<Server> response = (ServerDetailsResponse<Server>) fetchSubResource(serverName,
				ServerDetailsResponse.class);
		return response.getData();
	}

	public String getServerXML(String serverName) {
		return ((String) fetchSubResource(serverName, String.class));
	}

	public static void main(String[] args) {
		DiscoveredServersClient ServerResource = new DiscoveredServersClient("localhost");
		List<Server> discoveredServers = ServerResource.getDiscoveredServers();
		System.out.println(discoveredServers.size());

		// Server serverDetails = ServerResource.getServer("localhost");
		// System.out.println(serverDetails.getName());
	}
}
