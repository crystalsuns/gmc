/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.gateway.services;

import org.gluster.storage.management.gateway.utils.ServerUtil;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Abstract Gluster Interface - provides functionality common across all versions of GlusterFS e.g. version check.
 */
public abstract class AbstractGlusterInterface implements GlusterInterface {
	
	@Autowired
	protected ServerUtil serverUtil;

	@Override
	public String getVersion(String serverName) {
		return serverUtil.executeOnServer(serverName, "gluster --version").split("\n")[0].replaceAll("glusterfs ", "")
				.replaceAll(" built.*", "");
	}
}