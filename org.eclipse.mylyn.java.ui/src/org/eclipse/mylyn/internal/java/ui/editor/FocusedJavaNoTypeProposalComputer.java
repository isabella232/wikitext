/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.java.ui.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.JavaNoTypeCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

/**
 * @author Mik Kersten
 */
public class FocusedJavaNoTypeProposalComputer extends JavaNoTypeCompletionProposalComputer {

	public FocusedJavaNoTypeProposalComputer() {
		FocusedJavaProposalProcessor.getDefault().addMonitoredComputer(this);
	}

	@Override
	protected CompletionProposalCollector createCollector(JavaContentAssistInvocationContext context) {
		return super.createCollector(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		List proposals = super.computeCompletionProposals(context, monitor);
		return FocusedJavaProposalProcessor.getDefault().projectInterestModel(this, proposals);
	}
}
