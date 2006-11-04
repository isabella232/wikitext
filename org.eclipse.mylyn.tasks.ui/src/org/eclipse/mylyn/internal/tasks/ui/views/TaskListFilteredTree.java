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

package org.eclipse.mylar.internal.tasks.ui.views;

import java.util.List;
import java.util.Set;

import org.eclipse.mylar.internal.tasks.ui.TaskUiUtil;
import org.eclipse.mylar.tasks.core.AbstractTaskContainer;
import org.eclipse.mylar.tasks.core.DateRangeContainer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskActivityListener;
import org.eclipse.mylar.tasks.core.ITaskListChangeListener;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Mik Kersten
 */
public class TaskListFilteredTree extends AbstractMylarFilteredTree {

	public TaskListFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
		super(parent, treeStyle, filter);
	}

	private static final String LABEL_NO_ACTIVE = "<no active task>";
	
	private Hyperlink activeTaskLabel;
	
	private TaskProgressBar taskProgressBar;
	
	@Override
	protected Composite createProgressComposite(Composite container) {
		Composite progressComposite = new Composite(container, SWT.NONE);
		GridLayout progressLayout = new GridLayout(1, false);
		progressLayout.marginWidth = 2;
		progressLayout.marginHeight = 0;
		progressLayout.marginBottom = 2;
		progressLayout.horizontalSpacing = 0;
		progressLayout.verticalSpacing = 0;
		progressComposite.setLayout(progressLayout);
		progressComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 4, 1));
		
		taskProgressBar = new TaskProgressBar(progressComposite);
		taskProgressBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
		updateTaskProgressBar();
		
		TasksUiPlugin.getTaskListManager().getTaskList().addChangeListener(new ITaskListChangeListener() {

			public void containerAdded(AbstractTaskContainer container) {
			}

			public void containerDeleted(AbstractTaskContainer container) {
			}

			public void containerInfoChanged(AbstractTaskContainer container) {
			}

			public void localInfoChanged(ITask task) {
				updateTaskProgressBar();
			}

			public void repositoryInfoChanged(ITask task) {
			}

			public void taskAdded(ITask task) {
			}

			public void taskDeleted(ITask task) {
			}

			public void taskMoved(ITask task, AbstractTaskContainer fromContainer, AbstractTaskContainer toContainer) {
			}
			
		});
		
		TasksUiPlugin.getTaskListManager().addActivityListener(new ITaskActivityListener() {

			public void activityChanged(DateRangeContainer week) {
				updateTaskProgressBar();
			}

			public void calendarChanged() {
			}

			public void taskActivated(ITask task) {
			}

			public void taskDeactivated(ITask task) {
			}

			public void taskListRead() {
			}

			public void tasksActivated(List<ITask> tasks) {
			}
			
		});
		return progressComposite;
	}
	
	private void updateTaskProgressBar() {
		if (taskProgressBar.isDisposed()) {
			return;
		}
		
		Set<ITask> tasksThisWeek = TasksUiPlugin.getTaskListManager().getScheduledForThisWeek();
		int totalThisWeek = tasksThisWeek.size();
		int totalCompleted = 0;
		for (ITask task : tasksThisWeek) {
			if (task.isCompleted()) {
				totalCompleted++;
			}
		}
		taskProgressBar.setToolTipText("Completed " + totalCompleted + " of " + totalThisWeek + " tasks scheduled for this week");
		taskProgressBar.reset(totalCompleted, totalThisWeek);
	}
	
	protected Composite createStatusComposite(Composite container) {

		activeTaskLabel = new Hyperlink(container, SWT.LEFT);
		activeTaskLabel.setText(LABEL_NO_ACTIVE);
		ITask activeTask = TasksUiPlugin.getTaskListManager().getTaskList().getActiveTask();
		if (activeTask != null) {
			indicateActiveTask(activeTask);
		}
		
		activeTaskLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				if (TaskListFilteredTree.super.filterText.getText().length() > 0) {
					TaskListFilteredTree.super.filterText.setText("");
					TaskListFilteredTree.this.textChanged();
				}
				if (TaskListView.getFromActivePerspective().getDrilledIntoCategory() != null) {
					TaskListView.getFromActivePerspective().goUpToRoot();
				}
//				TaskListView.getFromActivePerspective().selectedAndFocusTask(
//						TasksUiPlugin.getTaskListManager().getTaskList().getActiveTask()
//				);
				TaskUiUtil.openEditor(TasksUiPlugin.getTaskListManager().getTaskList().getActiveTask(), false);
			}

		});
		return activeTaskLabel;
	}

    public void indicateActiveTask(ITask task) {
    	String text = task.getDescription();
    	activeTaskLabel.setText(text);
		activeTaskLabel.setUnderlined(true);
		activeTaskLabel.setToolTipText(task.getDescription());
		filterComposite.layout();
    }
    
    public void indicateNoActiveTask() {
    	activeTaskLabel.setText(LABEL_NO_ACTIVE);
		activeTaskLabel.setUnderlined(false);
		activeTaskLabel.setToolTipText("");
		filterComposite.layout();
    }
    
	public void setFilterText(String string) {
		if (filterText != null){
			filterText.setText(string);
			selectAll();		
		}
	}
}
