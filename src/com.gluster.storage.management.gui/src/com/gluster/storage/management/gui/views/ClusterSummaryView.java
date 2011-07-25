/**
 * DiscoveredServerView.java
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
package com.gluster.storage.management.gui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.chart.util.CDateTime;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.Activator;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.actions.IActionConstants;
import com.gluster.storage.management.gui.preferences.PreferenceConstants;
import com.gluster.storage.management.gui.utils.ChartViewerComposite;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.ibm.icu.util.Calendar;

/**
 * 
 */
public class ClusterSummaryView extends ViewPart {
	public static final String ID = ClusterSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private Cluster cluster;
	private GlusterDataModel model = GlusterDataModelManager.getInstance().getModel();
	private static final int CHART_WIDTH = 350;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (cluster == null) {
			cluster = model.getCluster();
		}
		setPartName("Summary");
		createSections(parent);
	}

	private int getServerCountByStatus(Cluster cluster, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : cluster.getServers()) {
			if (server.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createServersSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Servers", null, 2, false);

		int onlineServerCount = getServerCountByStatus(cluster, SERVER_STATUS.ONLINE);
		int offlineServerCount = getServerCountByStatus(cluster, SERVER_STATUS.OFFLINE);
		
		toolkit.createLabel(section, "Online : ");
		Label label = toolkit.createLabel(section, "" + onlineServerCount);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		
		toolkit.createLabel(section, "Offline : ");
		label = toolkit.createLabel(section, "" + offlineServerCount);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
	}
	
	private void createDiskSpaceSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Disk Space", null, 3, false);
		if (cluster.getServers().size() == 0) {
			toolkit.createLabel(section, "This section will be populated after at least\none server is added to the storage cloud.");
			return;
		}
		
		double totalDiskSpace = cluster.getTotalDiskSpace();
		double diskSpaceInUse = cluster.getDiskSpaceInUse();
		Double[] values = new Double[] { diskSpaceInUse, totalDiskSpace - diskSpaceInUse };
		createDiskSpaceChart(section, values);
	}

	private void createDiskSpaceChart(Composite section, Double[] values) {
		String[] categories = new String[] { "Used Space: " + NumberUtil.formatNumber((values[0] / 1024)) + " GB",
				"Free Space: " + NumberUtil.formatNumber((values[1] / 1024)) + " GB" };
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 400;
		data.heightHint = 150;
		data.verticalAlignment = SWT.CENTER;
		chartViewerComposite.setLayoutData(data);
	}
	
	public abstract class ChartPeriodLinkListener extends HyperlinkAdapter {
		protected String statsPeriod;
		protected String unit;
		protected int columnCount;

		public String getStatsPeriod() {
			return this.statsPeriod;
		}
		
		public ChartPeriodLinkListener(String statsPeriod) {
			this.statsPeriod = statsPeriod;
		}
		
		public ChartPeriodLinkListener(String statsPeriod, String unit, int columnCount) {
			this.statsPeriod = statsPeriod;
			this.unit = unit;
			this.columnCount = columnCount;
		}
		
		@Override
		public void linkActivated(HyperlinkEvent e) {
			super.linkActivated(e);
			//GlusterDataModelManager.getInstance().initializeAlerts(cluster);
			Composite section = ((Hyperlink)e.getSource()).getParent().getParent();
			for(Control control : section.getChildren()) {
				control.dispose();
			}
			List<Calendar> timestamps = new ArrayList<Calendar>();
			List<Double> data = new ArrayList<Double>();
			ServerStats stats = fetchStats();
			extractChartData(stats, timestamps, data, 2);
			createAreaChart(section, timestamps.toArray(new Calendar[0]), data.toArray(new Double[0]), unit, getTimestampFormatForPeriod(statsPeriod));
			createChartLinks(section, columnCount, this);
			section.layout();
		}
		
		public abstract ChartPeriodLinkListener getInstance(String statsPeriod);

		protected abstract ServerStats fetchStats();
	}
	
	public class CpuChartPeriodLinkListener extends ChartPeriodLinkListener {
		public CpuChartPeriodLinkListener(String statsPeriod) {
			super(statsPeriod);
		}
		
		private CpuChartPeriodLinkListener(String statsPeriod, String unit, int columnCount) {
			super(statsPeriod, unit, columnCount);
		}

		@Override
		protected ServerStats fetchStats() {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			preferenceStore.setValue(PreferenceConstants.P_CPU_CHART_PERIOD, statsPeriod);
			return new GlusterServersClient().getAggregatedCpuStats(statsPeriod);
		}

		@Override
		public ChartPeriodLinkListener getInstance(String statsPeriod) {
			return new CpuChartPeriodLinkListener(statsPeriod, "%", 4);
		}
	}
	
	public class NetworkChartPeriodLinkListener extends ChartPeriodLinkListener {
		public NetworkChartPeriodLinkListener(String statsPeriod) {
			super(statsPeriod);
		}
		
		private NetworkChartPeriodLinkListener(String statsPeriod, String unit, int columnCount) {
			super(statsPeriod, unit, columnCount);
		}

		@Override
		protected ServerStats fetchStats() {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			preferenceStore.setValue(PreferenceConstants.P_NETWORK_CHART_PERIOD, statsPeriod);
			return new GlusterServersClient().getAggregatedNetworkStats(statsPeriod);
		}

		@Override
		public ChartPeriodLinkListener getInstance(String statsPeriod) {
			return new NetworkChartPeriodLinkListener(statsPeriod, "KiB/s", 4);
		}
	}

	private Composite createChartLinks(Composite section, int columnCount, ChartPeriodLinkListener listener) {
		GridLayout layout = new org.eclipse.swt.layout.GridLayout(columnCount, false);
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginLeft = (CHART_WIDTH - (50*columnCount)) / 2;
		Composite graphComposite = toolkit.createComposite(section, SWT.NONE);
		graphComposite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = CHART_WIDTH;
		graphComposite.setLayoutData(data);
		
		createStatsLink(listener, graphComposite, "1 day", GlusterConstants.STATS_PERIOD_1DAY);
		createStatsLink(listener, graphComposite, "1 week", GlusterConstants.STATS_PERIOD_1WEEK);
		createStatsLink(listener, graphComposite, "1 month", GlusterConstants.STATS_PERIOD_1MONTH);
		createStatsLink(listener, graphComposite, "1 year", GlusterConstants.STATS_PERIOD_1YEAR);
		
		return graphComposite;
	}

	private void createStatsLink(ChartPeriodLinkListener listener, Composite parent, String label, String statsPeriod) {
		Hyperlink link1 = toolkit.createHyperlink(parent, label, SWT.NONE);
		link1.addHyperlinkListener(listener.getInstance(statsPeriod));
		if(listener.getStatsPeriod().equals(statsPeriod)) {
			link1.setEnabled(false);
		}
	}

	private ChartViewerComposite createAreaChart(Composite section, Calendar timestamps[], Double values[], String unit, String timestampFormat) {
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, timestamps, values, unit, timestampFormat);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = CHART_WIDTH;
		data.heightHint = 250;
		data.verticalAlignment = SWT.CENTER;
		chartViewerComposite.setLayoutData(data);
		return chartViewerComposite;
	}

	private void createAlertsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 1, false);
		List<Alert> alerts = cluster.getAlerts();

		for (Alert alert : alerts) {
			addAlertLabel(section, alert);
		}
	}
	
	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.FLAT);
		Image alertImage = null;
		switch (alert.getType()) {
		case OFFLINE_VOLUME_BRICKS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.BRICK_OFFLINE_22x22);
			break;
		case DISK_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.LOW_DISK_SPACE_22x22);
			break;
		case OFFLINE_SERVERS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.SERVER_OFFLINE_22x22);
			break;
		case MEMORY_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.MEMORY_USAGE_ALERT_22x22);
			break;
		case CPU_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.SERVER_WARNING_22x22);
			break;
		}
		lblAlert.setImage(alertImage);
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}

	private void createActionsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Actions", null, 1, false);

		ImageHyperlink imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Create Volume");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.CREATE_VOLUME_48x48));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					hs.executeCommand(IActionConstants.COMMAND_CREATE_VOLUME, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Add Server(s)");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.ADD_SERVER_48x48));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				// Open the "discovered servers" view by selecting the corresponding entity in the navigation view
				EntityGroup<Server> autoDiscoveredServersEntityGroup = GlusterDataModelManager.getInstance().getModel()
						.getCluster().getEntityGroup(Server.class);

				NavigationView navigationView = (NavigationView) guiHelper.getView(NavigationView.ID);
				navigationView.selectEntity(autoDiscoveredServersEntityGroup);
			}
		});
	}

	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, "Cluster Summary");

		createServersSection();
		createDiskSpaceSection();
		createCPUUsageSection();
		createNetworkUsageSection();
		createActionsSection();
		createAlertsSection();
		createRunningTasksSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private ChartViewerComposite createAreaChartSection(ServerStats stats, String sectionTitle, int dataColumnIndex, String unit, String timestampFormat, ChartPeriodLinkListener listener) {
		Composite section = guiHelper.createSection(form, toolkit, sectionTitle, null, 1, false);
		
		List<Calendar> timestamps = new ArrayList<Calendar>();
		List<Double> data = new ArrayList<Double>();
		extractChartData(stats, timestamps, data, dataColumnIndex);
		
		if(timestamps.size() == 0) {
			toolkit.createLabel(section, "Server statistics not available!\n Please check if all services are running properly on the cluster servers.");
			return null;
		}

		if (cluster.getServers().size() == 0) {
			toolkit.createLabel(section, "This section will be populated after at least\none server is added to the storage cloud.");
			return null;
		}
		
		ChartViewerComposite chart = createAreaChart(section, timestamps.toArray(new Calendar[0]), data.toArray(new Double[0]), unit, timestampFormat);

//		Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400), new CDateTime(1000l*1310468700),
//				new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new CDateTime(1000l*1310469900),
//				new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new CDateTime(1000l*1310471100),
//				new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new CDateTime(1000l*1310472300),
//				new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new CDateTime(1000l*1310473500),
//				new CDateTime(1000l*1310473800) };
//		
//		Double[] values = new Double[] { 10d, 11.23d, 17.92d, 18.69d, 78.62d, 89.11d, 92.43d, 89.31d, 57.39d, 18.46d, 10.44d, 16.28d, 13.51d, 17.53d, 12.21, 20d, 21.43d, 16.45d, 14.86d, 15.27d };
//		createLineChart(section, timestamps, values, "%");
		createChartLinks(section, 4, listener);
		return chart;
	}

	private void createCPUUsageSection() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String cpuStatsPeriod = preferenceStore.getString(PreferenceConstants.P_CPU_CHART_PERIOD);
		
		// in case of CPU usage, there are three elements in usage data: user, system and total. we use total.
		cpuChart = createAreaChartSection(cluster.getAggregatedCpuStats(), "CPU Usage (Aggregated)", 2, "%", getTimestampFormatForPeriod(cpuStatsPeriod), new CpuChartPeriodLinkListener(cpuStatsPeriod));
	}

	private String getTimestampFormatForPeriod(String statsPeriod) {
		if(statsPeriod.equals(GlusterConstants.STATS_PERIOD_1DAY)) {
			return "HH:mm";
		} else if (statsPeriod.equals(GlusterConstants.STATS_PERIOD_1WEEK)) {
			return "dd-MMM HH:mm";
		} else {
			return "dd-MMM";
		}
	}

	private void extractChartData(ServerStats stats, List<Calendar> timestamps, List<Double> data, int dataColumnIndex) {
		for(ServerStatsRow row : stats.getRows()) {
			Double cpuUsage = row.getUsageData().get(dataColumnIndex);
			if(!cpuUsage.isNaN()) {
				timestamps.add(new CDateTime(row.getTimestamp() * 1000));
				data.add(cpuUsage);
			}
		}
	}

	private void createNetworkUsageSection() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String networkStatsPeriod = preferenceStore.getString(PreferenceConstants.P_NETWORK_CHART_PERIOD);
		
		// in case of network usage, there are three elements in usage data: received, transmitted and total. we use total.
		createAreaChartSection(cluster.getAggregatedNetworkStats(), "Network Usage (Aggregated)", 2, "KiB/s", getTimestampFormatForPeriod(networkStatsPeriod), new NetworkChartPeriodLinkListener(networkStatsPeriod));
	}

	private void createRunningTasksSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Running Tasks", null, 1, false);

		for (TaskInfo taskInfo : cluster.getTaskInfoList()) {
			addTaskLabel(section, taskInfo);
		}
	}

	private void addTaskLabel(Composite section, TaskInfo taskInfo) {
		//TODO: create link and open the task progress view
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		lblAlert.setText(taskInfo.getDescription());
		
		Image taskImage = null;
		switch(taskInfo.getType()) {
		case DISK_FORMAT:
			taskImage = guiHelper.getImage(IImageKeys.DISK_INITIALIZING_22x22);
			break;
		case BRICK_MIGRATE:
			taskImage = guiHelper.getImage(IImageKeys.BRICK_MIGRATE_22x22);
			break;
		case VOLUME_REBALANCE:
			taskImage = guiHelper.getImage(IImageKeys.VOLUME_REBALANCE_22x22);
			break;
		}
		lblAlert.setImage(taskImage);
		lblAlert.redraw();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (form != null) {
			form.setFocus();
		}
	}
}
