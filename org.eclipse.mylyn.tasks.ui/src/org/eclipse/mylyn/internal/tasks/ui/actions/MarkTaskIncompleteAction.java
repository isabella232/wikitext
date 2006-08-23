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

package org.eclipse.mylar.internal.tasks.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylar.internal.tasks.core.WebTask;
import org.eclipse.mylar.internal.tasks.ui.TaskListImages;
import org.eclipse.mylar.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Mik Kersten and Ken Sueda
 */
public class MarkTaskIncompleteAction extends Action {

	public static final String ID = "org.eclipse.mylar.tasklist.actions.mark.incomplete";

	private final TaskListView view;

	public MarkTaskIncompleteAction(TaskListView view) {
		this.view = view;
		setText("Mark Incomplete");
		setToolTipText("Mark Incomplete");
		setId(ID);
		setImageDescriptor(TaskListImages.TASK_INCOMPLETE);
	}

	@Override
	public void run() {
		for (Object selectedObject : ((IStructuredSelection) this.view.getViewer().getSelection()).toList()) {
			if (selectedObject instanceof ITask) {
				TasksUiPlugin.getTaskListManager().getTaskList().markComplete(((ITask) selectedObject), false);
			} else if (selectedObject instanceof AbstractQueryHit) {
				ITask task = ((AbstractQueryHit)selectedObject).getCorrespondingTask();
				if (task instanceof WebTask) {
					TasksUiPlugin.getTaskListManager().getTaskList().markComplete(task, false);
				}
			}
		}
	}
}
