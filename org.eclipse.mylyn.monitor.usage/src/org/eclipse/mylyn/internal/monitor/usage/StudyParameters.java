/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Leah Findlater - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.mylyn.monitor.usage.AbstractStudyBackgroundPage;
import org.eclipse.mylyn.monitor.usage.AbstractStudyQuestionnairePage;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 */
public class StudyParameters {

	private String title;

	private String description;

	private AbstractStudyQuestionnairePage questionnairePage = null;

	private AbstractStudyBackgroundPage backgroundPage = null;

	private long transmitPromptPeriod = UiUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;

	private String acceptedUrlList;

	private String useContactField;

	private String formsConsent;

	private String version;

	private String uploadServletUrl;

	private String idServletUrl;

	private String customizingPlugin;

	private boolean forceObfuscation = false;

	private String usagePageUrl;

	private String studyName;

	private final Collection<String> filteredIds = new HashSet<String>();

	public String getUploadServletUrl() {
		return uploadServletUrl;
	}

	public void setUploadServletUrl(String servletServerUrl) {
		if (servletServerUrl != null) {
			this.uploadServletUrl = servletServerUrl;
		}
	}

	public String getUserIdServletUrl() {
		return idServletUrl;
	}

	public void setUserIdServletUrl(String servletUserId) {
		if (servletUserId != null) {
			this.idServletUrl = servletUserId;
		}
	}

	public String getFormsConsent() {
		return formsConsent;
	}

	public void setFormsConsent(String formsConsent) {
		if (formsConsent != null) {
			this.formsConsent = formsConsent;
		}
	}

	public long getTransmitPromptPeriod() {
		return transmitPromptPeriod;
	}

	public void setTransmitPromptPeriod(long transmitPromptPeriod) {
		this.transmitPromptPeriod = transmitPromptPeriod;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description != null) {
			this.description = description;
		}
	}

	public AbstractStudyQuestionnairePage getQuestionnairePage() {
		return questionnairePage;
	}

	public void setQuestionnairePage(AbstractStudyQuestionnairePage questionnairePage) {
		if (questionnairePage != null) {
			this.questionnairePage = questionnairePage;
		}
	}

	public AbstractStudyBackgroundPage getBackgroundPage() {
		return backgroundPage;
	}

	public void setBackgroundPage(AbstractStudyBackgroundPage backgroundPage) {
		if (backgroundPage != null) {
			this.backgroundPage = backgroundPage;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if (title != null) {
			this.title = title;
		}
	}

	public String getAcceptedUrlList() {
		return this.acceptedUrlList;
	}

	public void setAcceptedUrlList(String acceptedUrlList) {
		if (acceptedUrlList != null) {
			this.acceptedUrlList = acceptedUrlList;
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		if (version != null) {
			this.version = version;
		}
	}

	public String getUseContactField() {
		return useContactField;
	}

	public void setUseContactField(String useContactField) {
		if (useContactField != null) {
			this.useContactField = useContactField;
		}
	}

	public void setCustomizingPlugin(String name) {
		this.customizingPlugin = name;
	}

	public String getCustomizingPlugin() {
		return customizingPlugin;
	}

	public boolean isBackgroundEnabled() {
		return backgroundPage != null;
	}

	public boolean isQuestionnaireEnabled() {
		return questionnairePage != null;
	}

	public String getCustomizedByMessage() {
		String customizedBy = getCustomizingPlugin();
		String message = "NOTE: You have previously downloaded the " + getStudyName()
				+ " monitor and a user study plug-in with id: " + customizedBy + "\n"
				+ "If you are not familiar with this plug-in do not upload data.";
		return message;
	}

	public boolean usingContactField() {
		if (getUseContactField().equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isComplete() {
		return title != null && description != null && uploadServletUrl != null && idServletUrl != null;
	}

	public boolean isEmpty() {
		return title == null && description == null && uploadServletUrl == null && idServletUrl == null;
	}

	public boolean forceObfuscation() {
		return forceObfuscation;
	}

	public void setForceObfuscation(boolean forceObfuscation) {
		this.forceObfuscation = forceObfuscation;
	}

	public String getUploadFileLabel() {
		return "USAGE";// TODO make this extensible
	}

	public void setUsagePageUrl(String usagePageUrl) {
		this.usagePageUrl = usagePageUrl;
	}

	public String getUsagePageUrl() {
		return usagePageUrl;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Collection<String> getFilteredIds() {
		return Collections.unmodifiableCollection(filteredIds);
	}

	public void addFilteredIdPattern(String id) {
		if (id != null && id.trim().length() > 0) {
			filteredIds.add(id.trim());
		}
	}

	public String getFilteredIdSubmissionText() {
		Collection<String> filteredIds = getFilteredIds();
		if (filteredIds.size() != 0) {

			String filteredIdsString = "";
			for (String id : filteredIds) {
				filteredIdsString += id + "* ";
			}
			return "Only events from " + filteredIdsString + "packages will be submitted to " + getStudyName();
		} else {
			return "All events will be submitted to " + getStudyName();
		}
	}

}
