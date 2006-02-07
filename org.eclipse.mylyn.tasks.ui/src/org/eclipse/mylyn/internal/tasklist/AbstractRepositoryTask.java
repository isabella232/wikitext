/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.tasklist;

import java.util.Date;

/**
 * @author Mik Kersten and Robert Elves
 */
public class AbstractRepositoryTask extends Task {

	/** The last time this task's bug report was downloaded from the server. */
	protected Date lastRefresh;
	
	protected RepositoryTaskSyncState syncState = RepositoryTaskSyncState.SYNCHRONIZED;
		
	public AbstractRepositoryTask(String handle, String label, boolean newTask) {
		super(handle, label, newTask);
	}

	public enum RepositoryTaskSyncState {
		OUTGOING, SYNCHRONIZED, INCOMING, CONFLICT
	}

	/**
	 * TODO: Move
	 */
	public static String getLastRefreshTime(Date lastRefresh) {
		String toolTip = "\n---------------\n" + "Last synchronized: ";
		Date timeNow = new Date();
		if (lastRefresh == null)
			lastRefresh = new Date();
		long timeDifference = (timeNow.getTime() - lastRefresh.getTime()) / 60000;
		long minutes = timeDifference % 60;
		timeDifference /= 60;
		long hours = timeDifference % 24;
		timeDifference /= 24;
		if (timeDifference > 0) {
			toolTip += timeDifference + ((timeDifference == 1) ? " day, " : " days, ");
		}
		if (hours > 0 || timeDifference > 0) {
			toolTip += hours + ((hours == 1) ? " hour, " : " hours, ");
		}
		toolTip += minutes + ((minutes == 1) ? " minute " : " minutes ") + "ago";
		return toolTip;
	}

	/**
	 * @return Returns the lastRefresh.
	 */
	public Date getLastRefresh() {
		return lastRefresh;
	}

	/**
	 * @param lastRefresh
	 *            The lastRefresh to set.
	 */
	public void setLastRefresh(Date lastRefresh) {
		this.lastRefresh = lastRefresh;
	}

	public void setSyncState(RepositoryTaskSyncState syncState) {
		this.syncState = syncState;
	}

	public RepositoryTaskSyncState getSyncState() {
		return syncState;
	}
	
	/**
	 * @return The number of seconds ago that this task's bug report was
	 *         downloaded from the server.
	 */
	public long getTimeSinceLastRefresh() {
		Date timeNow = new Date();
		return (timeNow.getTime() - lastRefresh.getTime()) / 1000;
	}

	@Override
	public String getToolTipText() {
		if (lastRefresh == null)
			return "";
	
		String toolTip = getDescription();
	
		toolTip += AbstractRepositoryTask.getLastRefreshTime(lastRefresh);
	
		return toolTip;
	}

	public String getRepositoryUrl() {
		return TaskRepositoryManager.getRepositoryUrl(getHandleIdentifier());
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	public static long getLastRefreshTimeInMinutes(Date lastRefresh) {
		Date timeNow = new Date();
		if (lastRefresh == null)
			lastRefresh = new Date();
		long timeDifference = (timeNow.getTime() - lastRefresh.getTime()) / 60000;
		return timeDifference;
	}

}
