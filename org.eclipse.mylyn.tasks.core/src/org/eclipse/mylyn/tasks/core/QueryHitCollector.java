/*******************************************************************************
 * Copyright (c) 2003 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.tasks.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Collects QueryHits resulting from repository search
 * 
 * @author Shawn Minto
 * @author Rob Elves (generalized from bugzilla)
 */
public class QueryHitCollector {

	private List<AbstractQueryHit> results = new ArrayList<AbstractQueryHit>();

	/** The progress monitor for the search operation */
	private IProgressMonitor monitor = new NullProgressMonitor();

	/** The number of matches found */
	private int matchCount;

	/** The string to display to the user while querying */
	private static final String STARTING = "querying the server";

	/** The string to display to the user when we have 1 match */
	private static final String MATCH = "1 match";

	/** The string to display to the user when we have multiple or no matches */
	private static final String MATCHES = "{0} matches";

	/** The string to display to the user when the query is done */
	private static final String DONE = "done";

	private TaskList taskList;

	public QueryHitCollector(TaskList tasklist) {
		this.taskList = tasklist;
	}

	public void aboutToStart(int startMatchCount) throws CoreException {
		results.clear();
		matchCount = startMatchCount;
		monitor.setTaskName(STARTING);
	}

	public void accept(AbstractQueryHit hit) throws CoreException {

		ITask correspondingTask = taskList.getTask(hit.getHandleIdentifier());
		if (correspondingTask instanceof AbstractRepositoryTask) {
			hit.setCorrespondingTask((AbstractRepositoryTask) correspondingTask);
		}

		addMatch(hit);
		matchCount++;

		if (!getProgressMonitor().isCanceled()) {
			// if the operation is canceled finish with whatever data was
			// already found
			getProgressMonitor().subTask(getFormattedMatchesString(matchCount));
			getProgressMonitor().worked(1);
		}
	}

	public void done() {
		if (monitor != null && !monitor.isCanceled()) {
			// if the operation is cancelled, finish with the data that we
			// already have
			String matchesString = getFormattedMatchesString(matchCount);
			monitor.setTaskName(MessageFormat.format(DONE, new Object[] { matchesString }));
			monitor.done();
		}

		// Cut no longer used references because the collector might be re-used
		monitor = null;
	}

	protected String getFormattedMatchesString(int count) {
		if (count == 1) {
			return MATCH;
		}
		Object[] messageFormatArgs = { new Integer(count) };
		return MessageFormat.format(MATCHES, messageFormatArgs);
	}

	public IProgressMonitor getProgressMonitor() {
		return monitor;
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void addMatch(AbstractQueryHit hit) {
		results.add(hit);
	}

	public List<AbstractQueryHit> getHits() {
		return results;
	}
	
	public void clear() {
		results.clear();
	}

}
