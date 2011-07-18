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
package com.gluster.storage.management.gui;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Device;
import com.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Partition;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class DeviceTableLabelProvider extends LabelProvider implements ITableLabelProvider {

	private GUIHelper guiHelper = GUIHelper.getInstance();
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	public enum DEVICE_COLUMN_INDICES {
		DISK, PARTITION, FREE_SPACE, SPACE_IN_USE, STATUS
	};

	FontRegistry registry = new FontRegistry();

	public DeviceTableLabelProvider() {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof Device)) {
			return null;
		}

		Device device = (Device) element;
		if (columnIndex == DEVICE_COLUMN_INDICES.STATUS.ordinal()) {
			DEVICE_STATUS status = device.getStatus();
			
			if (status == null) {
				if (element instanceof Partition) {
					if (columnIndex == DEVICE_COLUMN_INDICES.STATUS.ordinal()) {
						status = device.getStatus();
					}
				}
			}

			if (status == null) {
				return null;
			}

			// TODO: Use different images for all four statuses
			switch (status) {
			case INITIALIZED:
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			case IO_ERROR:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			case UNINITIALIZED:
				return guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED);
			case INITIALIZING:
				return guiHelper.getImage(IImageKeys.WORK_IN_PROGRESS);
			default:
				throw new GlusterRuntimeException("Invalid disk status [" + status + "]");
			}
		}

		return null;
	}

	@Override
	public String getText(Object element) {
		return super.getText(element);
	}
	
	private String getDeviceFreeSpace(Device device) {
		if (device.hasErrors() || device.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber((device.getFreeSpace() / 1024));
		}
	}

	private String getTotalDeviceSpace(Device device) {
		if (device.hasErrors() || device.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber((device.getSpace() / 1024));
		}
	}

	public String getColumnText(Object element, int columnIndex) {
		
		if (element == null) {
			return "";
		}
		
		Device device = (Device) element;
		if (columnIndex == DEVICE_COLUMN_INDICES.DISK.ordinal()) {
			if (device instanceof Disk) {
				return device.getQualifiedName();
			} else {
				return "";
			}
		} else if (columnIndex == DEVICE_COLUMN_INDICES.FREE_SPACE.ordinal()) {
			return "" + getDeviceFreeSpace(device);
		} else if (columnIndex == DEVICE_COLUMN_INDICES.SPACE_IN_USE.ordinal()) {
			return "" + getTotalDeviceSpace(device);
		} else if (columnIndex == DEVICE_COLUMN_INDICES.PARTITION.ordinal()) {
			if (device instanceof Partition) {
				return device.getQualifiedName();
			} else {
				return "";
			}
		} else if (columnIndex == DEVICE_COLUMN_INDICES.STATUS.ordinal()) {
			return modelManager.getDeviceStatus(device);
		} else {
			return "";
		}
	}
}