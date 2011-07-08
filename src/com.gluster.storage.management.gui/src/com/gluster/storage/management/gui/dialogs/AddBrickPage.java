/**
 * AddDiskPage.java
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
package com.gluster.storage.management.gui.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.richclientgui.toolbox.duallists.DualListComposite.ListContentChangedListener;
import com.richclientgui.toolbox.duallists.IRemovableContentProvider;

/**
 * @author root
 * 
 */
public class AddBrickPage extends WizardPage {
	private List<Disk> availableDisks = new ArrayList<Disk>();
	private List<Disk> selectedDisks = new ArrayList<Disk>();
	private Volume volume = null;
	private BricksSelectionPage page = null;


	public static final String PAGE_NAME = "add.disk.volume.page";

	/**
	 * @param pageName
	 */
	protected AddBrickPage(Volume volume) {
		super(PAGE_NAME);
		this.volume = volume;
		setTitle("Add Brick");

		String description = "Add bricks to [" + volume.getName() + "] ";
		if ( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			description += "(in multiples of " + volume.getReplicaCount() + ")";
		} else if (volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			description += "(in multiples of " + volume.getStripeCount() + ")";
		}
		setDescription(description);

		availableDisks = getAvailableDisks(volume);
		
		setPageComplete(false);
		setErrorMessage("Please select bricks to be added to the volume [" + volume.getName()  +"]");
	}

	
	private boolean  isDiskUsed(Volume volume, Disk disk){
		for (Brick volumeBrick : volume.getBricks()) { // expected form of volumeBrick is "server:/export/diskName/volumeName"
			if ( disk.getQualifiedBrickName(volume.getName()).equals(volumeBrick.getQualifiedName())) {
				return true;
			}
		}
		return false;
	}
	
	protected List<Disk> getAvailableDisks(Volume volume) {
		List<Disk> availableDisks = new ArrayList<Disk>();
		for (Disk disk : GlusterDataModelManager.getInstance().getReadyDisksOfAllServers()) {
			if ( ! isDiskUsed(volume, disk) ) {
				availableDisks.add(disk);
			}
		}
		return availableDisks;
	}

	public Set<Disk> getChosenDisks() {
		return new HashSet<Disk>(page.getChosenDisks());
	}
	
	public Set<Brick> getChosenBricks( String volumeName ) {
		return page.getChosenBricks(volumeName);
	}
	
	private boolean isValidDiskSelection(int diskCount) {
		if ( diskCount == 0) {
			return false;
		}
		switch (volume.getVolumeType()) {
		case DISTRIBUTED_MIRROR:
			return (diskCount % volume.getReplicaCount() == 0);
		case DISTRIBUTED_STRIPE:
			return (diskCount % volume.getStripeCount() == 0);
		}
		return true;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		getShell().setText("Add Brick");
		List<Disk> chosenDisks = new ArrayList<Disk>(); // or volume.getDisks();
		
		page = new BricksSelectionPage(parent, SWT.NONE, availableDisks, chosenDisks, volume.getName());
		page.addDiskSelectionListener(new ListContentChangedListener<Disk>() {
			@Override
			public void listContentChanged(IRemovableContentProvider<Disk> contentProvider) {
				List<Disk> newChosenDisks = page.getChosenDisks();
				
				// validate chosen disks
				if(isValidDiskSelection(newChosenDisks.size())) {
					clearError();
				} else {
					setError();
				}
			}
		});
		setControl(page);
	}

	private void setError() {
		String errorMessage = null;
		if ( volume.getVolumeType() == VOLUME_TYPE.PLAIN_DISTRIBUTE) {
			errorMessage = "Please select at least one brick!";
		} else if( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			errorMessage = "Please select bricks in multiples of " + volume.getReplicaCount();
		} else {
			errorMessage = "Please select bricks in multiples of " + volume.getStripeCount();
		}

		setPageComplete(false);
		setErrorMessage(errorMessage);
	}

	private void clearError() {
		setErrorMessage(null);
		setPageComplete(true);
	}

	public BricksSelectionPage getDialogPage() {
		return this.page;
	}
	
	public void setPageComplete() {
		
	}

}