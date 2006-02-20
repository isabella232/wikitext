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

/**
 * @author Mik Kersten
 */
public abstract class AbstractQueryHit implements ITaskListElement {

	protected String repositoryUrl;
	
	protected String description;

	protected String priority;
	
	protected String id;
	
	protected AbstractQueryHit(String repositoryUrl, String description, String id) {
		this.repositoryUrl = repositoryUrl;
		this.description = description;
		this.id = id;
	}
	
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public abstract AbstractRepositoryTask getOrCreateCorrespondingTask();

	/**
	 * @return null if there is no corresponding report
	 */
	public abstract AbstractRepositoryTask getCorrespondingTask();

	public abstract void setCorrespondingTask(AbstractRepositoryTask task);

	public String getHandleIdentifier() {
		return TaskRepositoryManager.getHandle(repositoryUrl, id);
	}

	public String getId() {
		return id;
	}
}
