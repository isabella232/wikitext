/*******************************************************************************
 * Copyright (c) 2003, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.internal.bugzilla.core.history;

/**
 * @author John Anvik
 */
public enum AttachmentFlagStatus {
	REVIEW /* Firefox */, SUPERREVIEW /* Firefox */, APPROVAL /* Firefox */, UI /* Firefox */, BRANCH /* Firefox */, OBSOLETE, UNKNOWN, COMMITTED /* Gnome */, ACCEPTED /* Gnome accepted-committ_now*/, COMMENTED /* Gnome */, NONE /* Gnome */, REJECTED/* Gnome */;
}
