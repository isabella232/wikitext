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

package org.eclipse.mylar.bugzilla.core;

import org.eclipse.mylar.internal.bugzilla.core.internal.BugzillaReportElement;

/**
 * @author Rob Elves
 */
public class BugzillaAttributeFactory extends AbstractAttributeFactory {

	@Override
	public AbstractRepositoryReportAttribute createAttribute(Object key) {
		return new BugzillaReportAttribute((BugzillaReportElement) key);
	}

}
