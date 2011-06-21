package com.gluster.storage.management.server.resources;

import static com.gluster.storage.management.core.constants.RESTConstants.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.model.AlertListResponse;
import com.gluster.storage.management.core.model.Alert;

@Component
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_ALERTS)
public class AlertsResource {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public AlertListResponse getAlerts() {

		List<Alert> alerts = new ArrayList<Alert>();

		// TODO To implement the following dummy alerts
		// Alert #1
		Alert alert = new Alert();
		alert.setId("0001");
		alert.setReference("Server1"); // Server
		alert.setType(Alert.ALERT_TYPES.CPU_USAGE_ALERT);
		alert.setMessage(alert.getAlertType(alert.getType()) + " in server [" + alert.getReference() + "]");
		alerts.add(alert);

		// Alert #2
		alert = new Alert();
		alert.setId("0002");
		alert.setReference("Server2"); // server:Disk - brick
		alert.setType(Alert.ALERT_TYPES.MEMORY_USAGE_ALERT);
		alert.setMessage(alert.getAlertType(alert.getType()) + " in server [" + alert.getReference() + "]");
		alerts.add(alert);

		// Alert #3
		alert = new Alert();
		alert.setId("0003");
		alert.setReference("server1:sda1"); // Disk name
		alert.setType(Alert.ALERT_TYPES.DISK_USAGE_ALERT);
		alert.setMessage(alert.getAlertType(alert.getType()) + " in disk [" + alert.getReference() + "]");
		alerts.add(alert);

		// Alert #4
		alert = new Alert();
		alert.setId("0004");
		alert.setReference("Volume3:server2:sda1"); // volume:[Disk name]
		alert.setType(Alert.ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT);
		alert.setMessage(alert.getAlertType(alert.getType()) + " in volume [" + alert.getReference().split(":")[0]
				+ "] disk [" + alert.getReference().split(":")[1] + ":" + alert.getReference().split(":")[2] + "]");
		alerts.add(alert);

		// Alert #5
		alert = new Alert();
		alert.setId("0005");
		alert.setReference("Server2"); // Server name
		alert.setType(Alert.ALERT_TYPES.OFFLINE_SERVERS_ALERT);
		alert.setMessage(alert.getAlertType(alert.getType()) + " [" + alert.getReference() + "]");
		alerts.add(alert);

		return new AlertListResponse(alerts);

	}

	public static void main(String[] args) {
		/*
		 * // Unit test code AlertsResource alertResource = new AlertsResource(); AlertsListResponse alertResponse =
		 * alertResource.getAlerts(); List<Alert> alerts = alertResponse.getAlerts(); for (Alert alert: alerts) {
		 * System.out.println(alert.getMessage()); }
		 */
	}

}
