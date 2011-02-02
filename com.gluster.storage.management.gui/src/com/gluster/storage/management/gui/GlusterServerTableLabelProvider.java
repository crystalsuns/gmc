package com.gluster.storage.management.gui;

import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.GlusterServersPage.GLUSTER_SERVER_TABLE_COLUMN_INDICES;

public class GlusterServerTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof GlusterServer)) {
			return null;
		}
		
		GlusterServer server = (GlusterServer) element;
		if(columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			SERVER_STATUS status = server.getStatus();
			if(status == SERVER_STATUS.ONLINE) {
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			} else {
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			}
		}
		
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof GlusterServer)) {
			return null;
		}
		
		GlusterServer server = (GlusterServer) element;
		
		if (server.getStatus() == SERVER_STATUS.OFFLINE
				&& columnIndex != GLUSTER_SERVER_TABLE_COLUMN_INDICES.NAME.ordinal()
				&& columnIndex != GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			return "NA";
		}
		
		return (columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.NAME.ordinal() ? server.getName()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS.ordinal() ? server.getStatusStr()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.PREFERRED_NETWORK.ordinal() ? server.getPreferredNetworkInterface().getName()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.NUM_OF_CPUS.ordinal() ? "" + server.getNumOfCPUs()
			//: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.CPU_USAGE.ordinal() ? "" + server.getCpuUsage()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_MEMORY.ordinal() ? "" + server.getTotalMemory()
			//: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.MEMORY_IN_USE.ordinal() ? "" + server.getMemoryInUse()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_DISK_SPACE.ordinal() ? NumberUtil.formatNumber(server.getTotalDiskSpace())
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.IP_ADDRESSES.ordinal() ? server.getIpAddressesAsString() : "Invalid");
	}
}
