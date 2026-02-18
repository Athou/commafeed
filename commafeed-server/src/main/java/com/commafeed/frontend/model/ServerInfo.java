package com.commafeed.frontend.model;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Server infos")
@Data
@RegisterForReflection
public class ServerInfo implements Serializable {

	@Schema
	private String announcement;

	@Schema(required = true)
	private String version;

	@Schema(required = true)
	private String gitCommit;

	@Schema(required = true)
	private boolean allowRegistrations;

	@Schema(required = true)
	private boolean emailAddressRequired;

	@Schema(required = true)
	private boolean smtpEnabled;

	@Schema(required = true)
	private boolean demoAccountEnabled;

	@Schema(required = true)
	private boolean websocketEnabled;

	@Schema(required = true)
	private long websocketPingInterval;

	@Schema(required = true)
	private long treeReloadInterval;

	@Schema(required = true)
	private long forceRefreshCooldownDuration;

	@Schema(required = true)
	private boolean initialSetupRequired;

	@Schema(required = true)
	private int minimumPasswordLength;

	@Schema(required = true)
	private boolean pushNotificationsEnabled;

}
