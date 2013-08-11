package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.log4j.Level;

@Entity
@Table(name = "APPLICATIONSETTINGS")
@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationSettings extends AbstractModel {

	private String publicUrl;
	private boolean allowRegistrations = false;
	private String googleAnalyticsTrackingCode;
	private String googleClientId;
	private String googleClientSecret;
	private int backgroundThreads = 3;
	private int databaseUpdateThreads = 1;
	private String smtpHost;
	private int smtpPort;
	private boolean smtpTls;
	private String smtpUserName;
	private String smtpPassword;
	private boolean heavyLoad;
	private boolean pubsubhubbub;
	private boolean feedbackButton = true;
	private String logLevel = Level.INFO.toString();
	private boolean imageProxyEnabled;
	private int queryTimeout;
	private boolean crawlingPaused;
	private int keepStatusDays = 0;
	private int refreshIntervalMinutes = 5;
	@Column(length = 255)
	private String announcement;

}
