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
package com.gluster.storage.management.console.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;

public class StartVolumeAction extends AbstractActionDelegate {
	//private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private List<Volume> selectedVolumes = new ArrayList<Volume>();
	private List<String> selectedVolumeNames = new ArrayList<String>();
	private List<String> offlineVolumeNames = new ArrayList<String>();

	@Override
	protected void performAction(IAction action) {
		final String actionDesc = action.getDescription();
		VolumesClient vc = new VolumesClient();
		
		collectVolumeNames();
		
		if (offlineVolumeNames.size() == 0) {
			showWarningDialog(actionDesc, "Volumes " + selectedVolumeNames + " already started!");
			return; // Volume already started. Don't do anything.
		}
		
		List<String> startedVolumes = new ArrayList<String>();
		List<String> failedVolumes = new ArrayList<String>();
		String errorMessage = "";
		
		for (Volume volume : selectedVolumes.toArray(new Volume[0])) {
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) { 
				continue; // skip if already started
			}
			try {
				vc.startVolume(volume.getName());
				modelManager.updateVolumeStatus(volume, VOLUME_STATUS.ONLINE);
				startedVolumes.add(volume.getName());
			}catch (Exception e) {
				failedVolumes.add(volume.getName());
				// If any post volume start activity failed, update the volume status
				if (vc.getVolume(volume.getName()).getStatus() == VOLUME_STATUS.ONLINE) {
					modelManager.updateVolumeStatus(volume, VOLUME_STATUS.ONLINE);
				}
				errorMessage += e.getMessage();
			}
		}

		// Display the success or failure info
		if (startedVolumes.size() == 0) { // No volume(s) started successfully
			showErrorDialog(actionDesc, "Following volumes " + failedVolumes + " could not be start!"
					+ CoreConstants.NEWLINE + "Error: [" + errorMessage + "]");
		} else {
			String info = "Volumes " + startedVolumes + " started successfully!";
			if (errorMessage != "") {
				info += CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Volumes " + failedVolumes
						+ " failed to start! [" + errorMessage + "]";
			}
			showInfoDialog(actionDesc, info);
		}
	}

	private void collectVolumeNames() {
		selectedVolumeNames.clear();
		offlineVolumeNames.clear();
		for (Volume volume : selectedVolumes) {
			selectedVolumeNames.add(volume.getName());
			if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
				offlineVolumeNames.add(volume.getName());
			}
		}
	}
	
	@Override
	public void dispose() {

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		Set<Volume> selectedVolumeNames = GUIHelper.getInstance().getSelectedEntities(getWindow(), Volume.class);
		selectedVolumes.clear();
		if (selectedVolumeNames == null || selectedVolumeNames.isEmpty()) {
			super.selectionChanged(action, selection);
			if (selectedEntity instanceof Volume) {
				selectedVolumes.add((Volume) selectedEntity);
			}
		} else {
			selectedVolumes.addAll(selectedVolumeNames); //TODO reverse the collection to maintain the selected order
		}

		action.setEnabled(false);
		// To enable the action
		for (Volume volume : selectedVolumes) {
			if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
				action.setEnabled(true);
				break;// If find an online volume, enable the action
			}
		}
	}
}
