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

package org.eclipse.mylar.internal.tasks.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.tasks.ui.ITasksUiConstants;
import org.eclipse.mylar.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask.RepositoryTaskSyncState;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

/**
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class ContextAttachWizard extends Wizard {

	public static final String WIZARD_TITLE = "Attach context";

	private final TaskRepository repository;

	private final AbstractRepositoryTask task;

	private ContextAttachWizardPage wizardPage;

	public ContextAttachWizard(AbstractRepositoryTask task) {
		repository = TasksUiPlugin.getRepositoryManager().getRepository(task.getRepositoryKind(),
				task.getRepositoryUrl());
		this.task = task;
		setWindowTitle(ContextRetrieveWizard.TITLE);
		setDefaultPageImageDescriptor(TasksUiImages.BANNER_REPOSITORY_CONTEXT);
	}

	@Override
	public void addPages() {
		wizardPage = new ContextAttachWizardPage(repository, task);
		addPage(wizardPage);
		super.addPages();
	}

	@Override
	public final boolean performFinish() {

		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				this.repository.getKind());

		try {
			if (!connector.attachContext(repository, task, wizardPage.getComment())) {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						ITasksUiConstants.TITLE_DIALOG, AbstractRepositoryConnector.MESSAGE_ATTACHMENTS_NOT_SUPPORTED + connector.getLabel());
			} else {
				task.setSyncState(RepositoryTaskSyncState.SYNCHRONIZED);
				IWorkbenchSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
				if (site instanceof IViewSite) {
					IStatusLineManager statusLineManager = ((IViewSite)site).getActionBars().getStatusLineManager();
					statusLineManager.setMessage(TasksUiImages.getImage(TasksUiImages.TASKLIST),
							"Context attached to task: " + task.getSummary());
					TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
				}
			}
		} catch (final CoreException e) {			
			String message = e.getStatus().getMessage() != null ? e.getStatus().getMessage() : "";
			MessageDialog.openError(null, ITasksUiConstants.TITLE_DIALOG, "Attachment of task context FAILED. \n\n"+message);
			MylarStatusHandler.log(e.getStatus());			
			//ErrorDialog.openError(null, TasksUiPlugin.TITLE_DIALOG, "Attachment of task context FAILED.", e.getStatus());
			return false;
		}

		return true;
	}

}
