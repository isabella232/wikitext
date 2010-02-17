/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

/**
 * @author Mik Kersten
 * @author Leo Dos Santos
 */
public class ShowTasksUiLegendAction implements IWorkbenchWindowActionDelegate, IViewActionDelegate {

	private IWorkbenchWindow wbWindow;

	public void dispose() {
		// ignore
	}

	public void init(IWorkbenchWindow window) {
		wbWindow = window;
	}

	public void run(IAction action) {
		IIntroManager introMgr = wbWindow.getWorkbench().getIntroManager();
		IIntroPart intro = introMgr.getIntro();
		if (intro != null) {
			try {
				introMgr.setIntroStandby(intro, true);
			} catch (NullPointerException e) {
				// bug 270351: ignore exception
			}
		}

		TasksUiUtil.openTasksViewInActivePerspective();
		UiLegendDialog uiLegendDialog = new UiLegendDialog(WorkbenchUtil.getShell());
		uiLegendDialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// ignore
	}

	public void init(IViewPart view) {
		wbWindow = view.getViewSite().getWorkbenchWindow();
	}
}
