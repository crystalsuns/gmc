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
package com.gluster.storage.management.gui.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.utils.GUIHelper;

/**
 *
 */
public class DownloadVolumeLogsAction extends AbstractActionDelegate {
	private GUIHelper guiHelper = GUIHelper.getInstance();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.actions.AbstractActionDelegate#performAction(org.eclipse.jface.action.IAction)
	 */
	@Override
	protected void performAction(IAction action) {
		final VolumesClient client = new VolumesClient();
		
		final Runnable downloadLogsThread = new Runnable() {
			
			@Override
			public void run() {
				Volume volume = guiHelper.getSelectedEntity(getWindow(), Volume.class);
				
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterNames(new String[] {"GZipped Tar (*.tar.gz)"});
				dialog.setFilterExtensions(new String[] {"*.tar.gz"});
				String filePath = dialog.open();
				
				if(filePath == null) {
					return;
				}
				
				String title = "Download Volume Logs [" + volume.getName() + "]";
				try {
					client.downloadLogs(volume.getName(), filePath);
					showInfoDialog(title, "Volume logs downloaded successfully to [" + filePath + "]");
				} catch(Exception e) {
					showErrorDialog(title, e.getMessage());
				}
			}
		};
		
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			
			@Override
			public void run() {
				Display.getDefault().asyncExec(downloadLogsThread);
			}
		});
	}
}
