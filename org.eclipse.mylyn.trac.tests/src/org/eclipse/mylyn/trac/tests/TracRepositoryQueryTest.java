/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.trac.tests;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.trac.core.ITracClient;
import org.eclipse.mylar.internal.trac.core.TracCorePlugin;
import org.eclipse.mylar.internal.trac.core.TracRepositoryQuery;
import org.eclipse.mylar.internal.trac.core.TracTask;
import org.eclipse.mylar.internal.trac.core.model.TracSearch;
import org.eclipse.mylar.internal.trac.core.model.TracSearchFilter;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.core.TaskRepositoryManager;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Steffen Pingel
 */
public class TracRepositoryQueryTest extends TestCase {

	public void testChangeRepositoryUrl() {
		TaskRepositoryManager manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());

		TaskRepository repository = new TaskRepository(TracCorePlugin.REPOSITORY_KIND, Constants.TEST_TRAC_096_URL);	
		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		TracSearch search = new TracSearch();
		String queryUrl = repository.getUrl() + ITracClient.QUERY_URL + search.toUrl();
		TracRepositoryQuery query = new TracRepositoryQuery(repository.getUrl(), queryUrl, "summary", TasksUiPlugin.getTaskListManager().getTaskList());
		TasksUiPlugin.getTaskListManager().getTaskList().addQuery(query);

		TracTask task = new TracTask(Constants.TEST_TRAC_096_URL, ""+123, "desc", true);
		TasksUiPlugin.getTaskListManager().getTaskList().addTask(task);		
		
		String oldUrl = repository.getUrl();
		String newUrl = Constants.TEST_TRAC_010_URL;
		TasksUiPlugin.getTaskListManager().refactorRepositoryUrl(oldUrl, newUrl);	
		repository.setUrl(newUrl);
		
		assertEquals(newUrl, query.getRepositoryUrl());
		assertEquals(newUrl + ITracClient.QUERY_URL + search.toUrl(), query.getUrl());
		assertEquals(newUrl + ITracClient.TICKET_URL + 123, task.getTaskUrl());
	}
	
	public void testGetFilterList() {
		String repositoryUrl = "https://foo.bar/repo";
		String parameterUrl = "&status=new&status=assigned&status=reopened&milestone=0.1";
		String queryUrl = repositoryUrl + ITracClient.QUERY_URL + parameterUrl;
		TracRepositoryQuery query = new TracRepositoryQuery(repositoryUrl, queryUrl, "summary", TasksUiPlugin.getTaskListManager().getTaskList());

		TracSearch filterList = query.getTracSearch();

		assertEquals(parameterUrl, filterList.toUrl());
		assertEquals("&status=new|assigned|reopened&milestone=0.1", filterList.toQuery());

		List<TracSearchFilter> list = filterList.getFilters();
		TracSearchFilter filter = list.get(0);
		assertEquals("status", filter.getFieldName());
		assertEquals(Arrays.asList("new", "assigned", "reopened"), filter.getValues());
		filter = list.get(1);
		assertEquals("milestone", filter.getFieldName());
		assertEquals(Arrays.asList("0.1"), filter.getValues());
	}

}
