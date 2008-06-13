/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.views;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.core.WeekDateRange;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivityListener;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.ui.PlatformUI;

/**
 * Used by Scheduled task list presentation
 * 
 * @author Rob Elves
 */
public class TaskScheduleContentProvider extends TaskListContentProvider implements ITaskActivityListener {

	private final TaskActivityManager taskActivityManager;

	private Timer timer;

	private final Unscheduled unscheduled;

	private final Calendar END_OF_TIME;

	public TaskScheduleContentProvider(TaskListView taskListView) {
		super(taskListView);
		this.taskActivityManager = TasksUiPlugin.getTaskActivityManager();
		taskActivityManager.addActivityListener(this);
		END_OF_TIME = TaskActivityUtil.getCalendar();
		END_OF_TIME.add(Calendar.YEAR, 5000);
		END_OF_TIME.getTime();
		unscheduled = new Unscheduled(taskActivityManager, new DateRange(END_OF_TIME));
		timer = new Timer();
	}

	@Override
	public Object[] getElements(Object parent) {
		Set<AbstractTaskContainer> containers = new HashSet<AbstractTaskContainer>();

		WeekDateRange week = TaskActivityUtil.getCurrentWeek();

		timer.cancel();
		timer = new Timer();
		timer.schedule(new RolloverCheck(), week.getToday().getEndDate().getTime());

		for (DateRange day : week.getRemainingDays()) {
			containers.add(new ScheduledTaskContainer(TasksUiPlugin.getTaskActivityManager(), day));
		}
		containers.add(new ScheduledTaskContainer(TasksUiPlugin.getTaskActivityManager(), week));

		ScheduledTaskContainer nextWeekContainer = new ScheduledTaskContainer(taskActivityManager, week.next());
		containers.add(nextWeekContainer);

		ScheduledTaskContainer twoWeeksContainer = new ScheduledTaskContainer(taskActivityManager, week.next().next(),
				"Two Weeks");
		containers.add(twoWeeksContainer);

		containers.add(unscheduled);
		Calendar startDate = TaskActivityUtil.getCalendar();
		startDate.setTimeInMillis(twoWeeksContainer.getEnd().getTimeInMillis());
		TaskActivityUtil.snapNextDay(startDate);
		Calendar endDate = TaskActivityUtil.getCalendar();
		endDate.add(Calendar.YEAR, 4999);
		DateRange future = new DateRange(startDate, endDate);

		ScheduledTaskContainer futureContainer = new ScheduledTaskContainer(taskActivityManager, future, "Future");
		containers.add(futureContainer);

		if (parent != null && parent.equals(this.taskListView.getViewSite())) {
			return applyFilter(containers).toArray();
		} else {
			return containers.toArray();
		}
	}

	@Override
	public Object getParent(Object child) {
//		for (Object o : getElements(null)) {
//			ScheduledTaskContainer container = ((ScheduledTaskContainer) o);
//			if (container.getChildren().contains(((ITask) child).getHandleIdentifier())) {
//				return container;
//			}
//		}
		return null;
	}

	@Override
	public boolean hasChildren(Object parent) {
		return getChildren(parent).length > 0;
	}

	@Override
	public Object[] getChildren(Object parent) {
		Set<ITask> result = new HashSet<ITask>();
		if (parent instanceof ITask) {
			// flat presentation (no subtasks revealed in Scheduled mode)
		} else if (parent instanceof ScheduledTaskContainer) {
			for (ITask child : ((ScheduledTaskContainer) parent).getChildren()) {
				if (!filter(parent, child)) {
					result.add(child);
				}
			}

		} else if (parent instanceof ITaskContainer) {
			for (ITask child : ((ITaskContainer) parent).getChildren()) {
				result.add(child);
			}
		}
		return result.toArray();
	}

	private void refresh() {
		if (Platform.isRunning() && PlatformUI.getWorkbench() != null && !PlatformUI.getWorkbench().isClosing()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					taskListView.refresh();
				}
			});
		}
	}

	@Override
	public void dispose() {
		taskActivityManager.removeActivityListener(this);
		super.dispose();
	}

	private class RolloverCheck extends TimerTask {

		@Override
		public void run() {
			refresh();
		}
	}

	public void activityReset() {
		refresh();
	}

	public void elapsedTimeUpdated(ITask task, long newElapsedTime) {
		// ignore
	}

	public void preTaskActivated(ITask task) {
		// ignore
	}

	public void preTaskDeactivated(ITask task) {
		// ignore
	}

	public void taskActivated(ITask task) {
		// ignore
	}

	public void taskDeactivated(ITask task) {
		// ignore
	}

	public class Unscheduled extends ScheduledTaskContainer {

		private final TaskActivityManager activityManager;

		public Unscheduled(TaskActivityManager activityManager, DateRange range) {
			super(activityManager, range, "Unscheduled");
			this.activityManager = activityManager;
		}

		@Override
		public Collection<ITask> getChildren() {
			Set<ITask> all = new HashSet<ITask>();
			for (ITask task : activityManager.getUnscheduled()) {
				if (!task.isCompleted() || (task.isCompleted() && !task.getSynchronizationState().isSynchronized())) {
					all.add(task);
				}
			}
			return all;
		}
	}

}
