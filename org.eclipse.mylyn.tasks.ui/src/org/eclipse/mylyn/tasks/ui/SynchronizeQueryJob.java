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

package org.eclipse.mylar.tasks.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylar.internal.context.core.util.DateUtil;
import org.eclipse.mylar.internal.tasks.ui.TaskListImages;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskList;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Mik Kersten
 */
class SynchronizeQueryJob extends Job {
	
	private final AbstractRepositoryConnector connector;

	private static final String JOB_LABEL = "Synchronizing queries";

	private Set<AbstractRepositoryQuery> queries;

	private List<AbstractQueryHit> newHits = new ArrayList<AbstractQueryHit>();

	private boolean synchTasks;

	private TaskList taskList;

	public SynchronizeQueryJob(RepositorySynchronizationManager synchronizationManager, AbstractRepositoryConnector connector, Set<AbstractRepositoryQuery> queries,
			TaskList taskList) {
		super(JOB_LABEL + ": " + connector.getRepositoryType());
		this.connector = connector;
		this.queries = queries;
		this.taskList = taskList;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(JOB_LABEL, queries.size());

		for (AbstractRepositoryQuery repositoryQuery : queries) {
			// if (repositoryQuery.isSynchronizing())
			// continue;
			monitor.setTaskName("Synchronizing: " + repositoryQuery.getDescription());
			setProperty(IProgressConstants.ICON_PROPERTY, TaskListImages.REPOSITORY_SYNCHRONIZE);
			// repositoryQuery.setCurrentlySynchronizing(true);
			TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
					repositoryQuery.getRepositoryKind(), repositoryQuery.getRepositoryUrl());
			if (repository == null) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog
								.openInformation(Display.getDefault().getActiveShell(), TasksUiPlugin.TITLE_DIALOG,
										"No task repository associated with this query. Open the query to associate it with a repository.");
					}
				});
			}

			MultiStatus queryStatus = new MultiStatus(TasksUiPlugin.PLUGIN_ID, IStatus.OK, "Query result", null);

			newHits = connector.performQuery(repositoryQuery, new NullProgressMonitor(), queryStatus);
			if (queryStatus.getChildren() != null && queryStatus.getChildren().length > 0) {
				if (queryStatus.getChildren()[0].getException() == null) {
					repositoryQuery.updateHits(newHits, taskList);
					if (synchTasks) {
						// TODO: Should sync changed per repository not per query
						TasksUiPlugin.getSynchronizationManager().synchronizeChanged(connector, repository);
					}

				} else {
					repositoryQuery.setCurrentlySynchronizing(false);
					return queryStatus.getChildren()[0];
				}
			}

			repositoryQuery.setCurrentlySynchronizing(false);
			repositoryQuery.setLastRefreshTimeStamp(DateUtil.getFormattedDate(new Date(), "MMM d, H:mm:ss"));
			TasksUiPlugin.getTaskListManager().getTaskList().notifyContainerUpdated(repositoryQuery);
			monitor.worked(1);
		}
		return Status.OK_STATUS;
	}

	public void setSynchTasks(boolean syncTasks) {
		this.synchTasks = syncTasks;
	}
}