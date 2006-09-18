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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.mylar.internal.tasks.core.WebTask;
import org.eclipse.mylar.internal.tasks.ui.TaskListImages;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskListElement;
import org.eclipse.mylar.tasks.core.Task;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Mik Kersten
 */
public class MarkTaskCompleteAction extends Action {

	public static final String ID = "org.eclipse.mylar.tasklist.actions.mark.completed";

	private List<ITaskListElement> selectedElements;
	
	public MarkTaskCompleteAction(List<ITaskListElement> selectedElements) {
		this.selectedElements = selectedElements;
		setText("Mark Complete");
		setToolTipText("Mark Complete");
		setId(ID);
		setImageDescriptor(TaskListImages.TASK_COMPLETE);
		setEnabled(selectedElements.size() == 1 && (selectedElements.get(0) instanceof Task || selectedElements.get(0) instanceof AbstractQueryHit));
	}

	@Override
	public void run() {
		for (Object selectedObject : selectedElements) {
			if (selectedObject instanceof ITask) {
				TasksUiPlugin.getTaskListManager().getTaskList().markComplete(((ITask) selectedObject), true);
			} else if (selectedObject instanceof AbstractQueryHit) {
				ITask task = ((AbstractQueryHit)selectedObject).getCorrespondingTask();
				if (task instanceof WebTask) {
					TasksUiPlugin.getTaskListManager().getTaskList().markComplete(task, true);		
				}
			}
		}
	}
}
