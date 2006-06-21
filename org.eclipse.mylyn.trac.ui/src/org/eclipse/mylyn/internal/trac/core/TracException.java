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

package org.eclipse.mylar.internal.trac.core;

/**
 * Indicates an error during repository access.
 * 
 * @author Steffen Pingel
 */
public class TracException extends Exception {

	private static final long serialVersionUID = 1929614326467463462L;

	public TracException() {
	}

	public TracException(String message) {
		super(message);
	}

	public TracException(Throwable cause) {
		super(cause);
	}

	public TracException(String message, Throwable cause) {
		super(message, cause);
	}

}
