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
package com.gluster.storage.management.core.utils;

import java.util.List;

public class StringUtil {
	public static boolean filterString(String sourceString, String filterString, boolean caseSensitive) {
		return caseSensitive ? sourceString.contains(filterString) : sourceString.toLowerCase().contains(
				filterString.toLowerCase());
	}

	public static String removeSpaces(String str) {
		return str.replaceAll("\\s+", "");
	}

	public static String ListToString(List<String> list, String delimiter) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			output.append(list.get(i));
			if (i < (list.size()-1) ) {
				output.append(delimiter);
			}
		}
		return output.toString();
	}
}
