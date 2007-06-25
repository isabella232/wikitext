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
package org.eclipse.mylyn.internal.bugzilla.core;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steffen Pingel
 * @author Robert Elves (adaption for Bugzilla)
 */
public class BugzillaClientFactory {

	public static BugzillaClient createClient(String hostUrl, String username, String password, String htAuthUser,
			String htAuthPass, Proxy proxy, String encoding) throws MalformedURLException {
		return createClient(hostUrl, username, password, htAuthUser, htAuthPass, proxy, encoding,
				new HashMap<String, String>());
	}

	public static BugzillaClient createClient(String hostUrl, String username, String password, String htAuthUser,
			String htAuthPass, Proxy proxy, String encoding, Map<String, String> configParameters)
			throws MalformedURLException {
		URL url = new URL(hostUrl);

		BugzillaClient client = new BugzillaClient(url, username, password, htAuthUser, htAuthPass, encoding,
				configParameters);
		client.setProxy(proxy);
		return client;
	}
}
