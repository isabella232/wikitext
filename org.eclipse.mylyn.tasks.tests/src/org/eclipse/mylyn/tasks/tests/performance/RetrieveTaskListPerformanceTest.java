/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.performance;

import java.io.File;

import org.eclipse.mylyn.internal.tasks.ui.util.TaskListWriter;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.tests.TaskTestUtil;
import org.eclipse.mylyn.tasks.ui.TaskListManager;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * @author Jingwen Ou
 */
public class RetrieveTaskListPerformanceTest extends PerformanceTestCase {

	private static final String TASK_LIST_4000 = "testdata/performance/tasklist-4000.xml.zip";

	private TaskList taskList;

	private TaskListManager taskListManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		taskListManager = TasksUiPlugin.getTaskListManager();
		taskList = taskListManager.getTaskList();
		taskList.reset();

		final File file = TaskTestUtil.getLocalFile(TASK_LIST_4000);
		final TaskListWriter taskListWriter = taskListManager.getTaskListWriter();
		taskListWriter.readTaskList(taskList, file, TasksUiPlugin.getTaskDataManager());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		taskList.reset();
	}

	public void testRetrieveQueriesWith4000Tasks() {
		for (int i = 0; i < 10; i++) {
			startMeasuring();
			taskList.getQueries();
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

	public void testRetrieveActiveTasksWith4000Tasks() {
		for (int i = 0; i < 10; i++) {
			startMeasuring();
			taskList.getRepositoryTasks("https://bugs.eclipse.org/bugs");
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

	public void testRetrieveTasksWith4000Tasks() {
		for (int i = 0; i < 10; i++) {
			startMeasuring();
			taskList.getAllTasks();
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

}
