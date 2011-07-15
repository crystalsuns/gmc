package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;

import com.gluster.storage.management.gui.dialogs.ChangePasswordDialog;

public class ChangePasswordAction extends AbstractActionDelegate {

	@Override
	protected void performAction(IAction action) {
		try {
			// To open a dialog for change password
			ChangePasswordDialog dialog = new ChangePasswordDialog(getShell());
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
	}
}