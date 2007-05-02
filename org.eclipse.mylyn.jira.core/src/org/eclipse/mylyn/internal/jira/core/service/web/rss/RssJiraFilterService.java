/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.service.web.rss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.web.JiraWebSession;

/**
 * @author Brock Janiczak
 */
public class RssJiraFilterService {

	private final JiraServer server;

	private final boolean useGZipCompression;

	public RssJiraFilterService(JiraServer server) {
		this.server = server;
		this.useGZipCompression = server.useCompression();
	}

	public void findIssues(final FilterDefinition filterDefinition, final IssueCollector collector) throws JiraException {
		// TODO make the callback a full class and pass in the filter and
		// collector
		JiraWebSession session = new JiraWebSession(server);

		session.doInSession(new RssFeedProcessorCallback(useGZipCompression, collector) {

			protected String getRssUrl() throws JiraException {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/IssueNavigator.jspa?view=rss&decorator=none&reset=true&");

				if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&');
				}

				rssUrlBuffer.append(RssJiraFilterConverterFactory.getConverter(server).convert(filterDefinition));

				return rssUrlBuffer.toString();
			}
		});
	}

	public void executeNamedFilter(final NamedFilter filter, final IssueCollector collector) throws JiraException {
		JiraWebSession session = new JiraWebSession(server);

		session.doInSession(new RssFeedProcessorCallback(useGZipCompression, collector) {

			protected String getRssUrl() throws JiraException {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				if (server.getServerInfo().getVersion().compareTo("3.7") >= 0) {
					rssUrlBuffer.append("/sr/jira.issueviews:searchrequest-xml/").append(filter.getId()).append(
							"/SearchRequest-").append(filter.getId()).append(".xml");
					if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
						rssUrlBuffer.append("?tempMax=").append(collector.getMaxHits());
					}
				} else {
					rssUrlBuffer.append("/secure/IssueNavigator.jspa?view=rss&decorator=none&");
					if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
						rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&');
					}
					rssUrlBuffer.append("requestId=").append(filter.getId());
				}
				return rssUrlBuffer.toString();
			}
		});
	}

	public void quickSearch(final String searchString, final IssueCollector collector) throws JiraException {
		JiraWebSession session = new JiraWebSession(server);

		session.doInSession(new RssFeedProcessorCallback(useGZipCompression, collector) {

			protected String getRssUrl() {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/QuickSearch.jspa?view=rss&decorator=none&reset=true&");

				if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&');
				}

				try {
					rssUrlBuffer.append("searchString=").append(URLEncoder.encode(searchString, "UTF-8")); //$NON-NLS-1$
				} catch (UnsupportedEncodingException e) {
					// System must support UTF-8
				}

				return rssUrlBuffer.toString();
			}
		});

	}
}
