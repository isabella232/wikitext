/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 26, 2004
 */
package org.eclipse.mylar.tasklist.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.tasklist.IQuery;
import org.eclipse.mylar.tasklist.ITask;
import org.eclipse.mylar.tasklist.ITaskActivityListener;
import org.eclipse.mylar.tasklist.ITaskCategory;
import org.eclipse.mylar.tasklist.MylarTaskListPlugin;

/**
 * @author Mik Kersten
 */
public class TaskListManager {

	private Map<ITask, TaskTimer> timerMap = new HashMap<ITask, TaskTimer>();

	private List<ITaskActivityListener> listeners = new ArrayList<ITaskActivityListener>();
	
	private TaskListWriter taskListWriter;// = new TaskListWriter();
	
	private File taskListFile;

	private TaskList taskList = new TaskList();

	private boolean taskListRead = false;
	
	private int nextTaskId;

	private static final String PREFIX_TASK = "task-";
	
	public static final long INACTIVITY_TIME_MILLIS;
	
	static {
		if (MylarPlugin.getContextManager() != null) {
			INACTIVITY_TIME_MILLIS = MylarPlugin.getContextManager().getActivityTimeoutSeconds() * 1000;
		} else {
			INACTIVITY_TIME_MILLIS = 1 * 60 * 1000;
		}
	}

	public TaskListManager(TaskListWriter taskListWriter, File file, int startId) { 
		this.taskListFile = file;
		this.taskListWriter = taskListWriter;
		this.nextTaskId = startId;
	}
	
	public TaskList createNewTaskList() {
		taskList = new TaskList();
		return taskList;
	}

	public String genUniqueTaskHandle() {
		return PREFIX_TASK + nextTaskId++;
	}

	public boolean readTaskList() {
//		taskListWriter.initExtensions();
		try {
			if (taskListFile.exists()) {
				taskListWriter.readTaskList(taskList, taskListFile);
				int maxHandle = taskList.findLargestTaskHandle();
				if (maxHandle >= nextTaskId) {
					nextTaskId = maxHandle + 1;
				}
				for (ITaskActivityListener listener : listeners) listener.tasksActivated(taskList.getActiveTasks());
			} else {
				createNewTaskList();
			}

			taskListRead = true;
			for (ITaskActivityListener listener : listeners) listener.tasklistRead();
		} catch (Exception e) {
			MylarPlugin.log(e, "Could not read task list");
			return false;
		}
		return true;
	}

	public void saveTaskList() {
		try {
			taskListWriter.writeTaskList(taskList, taskListFile);
			MylarPlugin.getDefault().getPreferenceStore().setValue(MylarTaskListPlugin.TASK_ID, nextTaskId);
		} catch (Exception e) {
			MylarPlugin.fail(e, "Could not save task list", true);
		}
	}

	public TaskList getTaskList() {
		return taskList;
	}

	public void setTaskList(TaskList taskList) {
		this.taskList = taskList;
	}

	public void moveToRoot(ITask task) {
		if (task.getCategory() instanceof TaskCategory) {
			((TaskCategory)task.getCategory()).removeTask(task);
		}
		task.setCategory(null);
		if (!taskList.getRootTasks().contains(task)) taskList.addRootTask(task);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}

	public void moveToCategory(TaskCategory category, ITask task) {
		taskList.removeRootTask(task);
		if (task.getCategory() instanceof TaskCategory) {
			((TaskCategory)task.getCategory()).removeTask(task);
		}
		if (!category.getChildren().contains(task)) {
			category.addTask(task);
		}
		task.setCategory(category);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}

	public void addCategory(ITaskCategory cat) {
		taskList.addCategory(cat);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}
	
	public void removeFromCategoryAndRoot(TaskCategory category, ITask task) {
		category.removeTask(task);
		taskList.addRootTask(task);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}
	
	public void addQuery(IQuery cat) {
		taskList.addQuery(cat);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}

	public void deleteTask(ITask task) {
		TaskTimer activeListener = timerMap.remove(task);
		if (activeListener != null)
			activeListener.stopTimer();
		taskList.setActive(task, false, false);
		taskList.deleteTask(task);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}

	public void deleteCategory(ITaskCategory cat) {
		taskList.deleteCategory(cat);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}

	public void deleteQuery(IQuery query) {
		taskList.deleteQuery(query);
		for (ITaskActivityListener listener : listeners) listener.tasklistModified();
	}

	public void addListener(ITaskActivityListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ITaskActivityListener listener) {
		listeners.remove(listener);
	}

	public void activateTask(ITask task) {
		if (!MylarTaskListPlugin.getDefault().isMultipleMode()) {
			if (taskList.getActiveTasks().size() > 0
					&& taskList.getActiveTasks().get(0).getHandleIdentifier().compareTo(task.getHandleIdentifier()) != 0) {
				for (ITask t : taskList.getActiveTasks()) {
					for (ITaskActivityListener listener : listeners)
						listener.taskDeactivated(t);
				}
				taskList.clearActiveTasks();
			}
		}
		taskList.setActive(task, true, false);
		TaskTimer activeListener = new TaskTimer(task);
		timerMap.put(task, activeListener);
		for (ITaskActivityListener listener : listeners) listener.taskActivated(task);
	}

	public void deactivateTask(ITask task) {
		TaskTimer activeListener = timerMap.remove(task);
		if (activeListener != null)
			activeListener.stopTimer();
		taskList.setActive(task, false, false);
		for (ITaskActivityListener listener : listeners) listener.taskDeactivated(task);
	}

	/**
	 * TODO: refactor into task deltas?
	 */
	public void notifyTaskChanged(ITask task) {
		for (ITaskActivityListener listener : listeners) listener.taskChanged(task);
	}
	
	public void setTaskListFile(File f) {
		this.taskListFile = f;
	}

	public ITask getTaskForHandle(String handle, boolean lookInArchives) {
		if (handle == null)
			return null;
		return taskList.getTaskForHandle(handle, lookInArchives);
	}

	public boolean isTaskListRead() {
		return taskListRead;
	}

	public TaskListWriter getTaskListWriter() {
		return taskListWriter;
	}

	public File getTaskListFile() {
		return taskListFile;
	}

//	public void setTaskListRead(boolean taskListRead) {
//		this.taskListRead = taskListRead;
//	}
	
//	public String toXmlString() {
//		try {
//			return MylarTaskListPlugin.getDefault().getTaskListExternalizer().getTaskListXml(taskList);
//		} catch (Exception e) {
//			MylarPlugin.fail(e, "Could not save task list", true);
//		}
//		return null;
//	}
}
