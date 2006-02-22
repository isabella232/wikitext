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

package org.eclipse.mylar.internal.tasklist.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

/**
 * @author Rob Elves
 */
public class TaskListNotificationManager {

	private static final String CLOSE_NOTIFICATION_JOB = "Close Notification Job";

	private static final String OPEN_NOTIFICATION_JOB = "Open Notification Job";

//	private static final long CLOSE_POPUP_DELAY = 1000 * 10;

	private static final long OPEN_POPUP_DELAY = 1000 * 60;

	private static final boolean runSystem = true;

//	private TaskListNotificationPopup popup;

	private List<ITaskListNotification> notifications = new ArrayList<ITaskListNotification>();

	private List<ITaskListNotification> currentlyNotifying = Collections.synchronizedList(notifications);

	private List<ITaskListNotificationProvider> notificationProviders = new ArrayList<ITaskListNotificationProvider>();

	private Job openJob = new Job(OPEN_NOTIFICATION_JOB) {
		@Override
		protected IStatus run(IProgressMonitor monitor) {

			try {

				if (!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
//							if ((popup != null && popup.close()) || popup == null) {
//								closeJob.cancel();							
//								collectNotifications();
//								synchronized (TaskListNotificationManager.class) {
//									if (currentlyNotifying.size() > 0) {
//										popup = new TaskListNotificationPopup(new Shell(PlatformUI.getWorkbench().getDisplay()));
//										popup.setContents(new ArrayList<ITaskListNotification>(currentlyNotifying));
//										cleanNotified();
//										popup.setBlockOnOpen(false);
//										popup.open();
//										closeJob.setSystem(runSystem);
//										closeJob.schedule(CLOSE_POPUP_DELAY);										
//										popup.getShell().addShellListener(SHELL_LISTENER);
//									}
//								}
//							}
						}
					});
				}
			} finally {
				schedule(OPEN_POPUP_DELAY);
			}

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			return Status.OK_STATUS;
		}

	};

	private Job closeJob = new Job(CLOSE_NOTIFICATION_JOB) {

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
//						if (popup != null) {
//							synchronized (popup) {
//								popup.close();
//							}
//						}
					}
				});
			}
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			return Status.OK_STATUS;
		}

	};

//	private ShellListener SHELL_LISTENER = new ShellListener() {
//
//		public void shellClosed(ShellEvent arg0) {
//		}
//
//		public void shellDeactivated(ShellEvent arg0) {
////			popup.close();
//			// don't want notifications right away
//			openJob.cancel();
//			openJob.schedule(OPEN_POPUP_DELAY);
//		}
//
//		public void shellActivated(ShellEvent arg0) {
//			closeJob.cancel();
//		}
//
//		public void shellDeiconified(ShellEvent arg0) {
//			// ingore
//		}
//
//		public void shellIconified(ShellEvent arg0) {
//			// ignore
//		}
//	};

//	private void cleanNotified() {
//		 for (ITaskListNotification notification : currentlyNotifying) {
//			notification.setNotified(true);
//		 }
//		currentlyNotifying.clear();
//	}
//
//	private void collectNotifications() {
//		for (ITaskListNotificationProvider provider : notificationProviders) {
//			currentlyNotifying.addAll(provider.getNotifications());
//		}		
//	}

	public void startNotification(long initialStartupTime) {
		openJob.setSystem(runSystem);
		openJob.schedule(initialStartupTime);
	}

	public void stopNotification() {
		openJob.cancel();
		closeJob.cancel();
//		if(popup != null) {
//			popup.close();
//		}
	}

	public void addNotificationProvider(ITaskListNotificationProvider notification_provider) {
		notificationProviders.add(notification_provider);
	}

	public void removeNotificationProvider(ITaskListNotificationProvider notification_provider) {
		notificationProviders.remove(notification_provider);
	}

	/**
	 * public for testing purposes
	 */
	public List<ITaskListNotification> getNotifications() {
		synchronized (TaskListNotificationManager.class) {
			return currentlyNotifying;
		}
	}

}
