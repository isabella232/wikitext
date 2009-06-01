/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchHitCollector;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * @author Rob Elves
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class TaskSearchPage extends DialogPage implements ISearchPage {

	public static final String ID = "org.eclipse.mylyn.tasks.ui.search.page"; //$NON-NLS-1$

	private static final String PAGE_KEY = "page"; //$NON-NLS-1$

	private static final String PAGE_NAME = "TaskSearchPage"; //$NON-NLS-1$

	private static final String STORE_REPO_ID = PAGE_NAME + ".REPO"; //$NON-NLS-1$

	private Combo repositoryCombo;

	private Text keyText;

	private TaskRepository repository;

	private Composite fParentComposite;

	private IDialogSettings fDialogSettings;

	private int currentPageIndex = -1;

	private boolean firstView = true;

	private Control[] queryPages;

	private ISearchPageContainer pageContainer;

	private ITaskSearchPageContainer taskSearchPageContainer;

	public boolean performAction() {
		saveDialogSettings();
		String key = keyText.getText();
		if (key != null && key.trim().length() > 0) {
			key = key.trim();
			boolean openSuccessful = TasksUiUtil.openTask(repository, key);
			if (!openSuccessful) {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.TaskSearchPage_Task_Search, Messages.TaskSearchPage_No_task_found_matching_key_ + key);
			}
			return openSuccessful;
		} else {
			ITaskSearchPage page = (ITaskSearchPage) queryPages[currentPageIndex].getData(PAGE_KEY);
			return page.performSearch();
		}
	}

	public void setContainer(ISearchPageContainer container) {
		this.pageContainer = container;
		this.taskSearchPageContainer = new ITaskSearchPageContainer() {
			public IRunnableContext getRunnableContext() {
				return pageContainer.getRunnableContext();
			}

			public void setPerformActionEnabled(boolean enabled) {
				pageContainer.setPerformActionEnabled(enabled);
			}
		};
	}

	public void createControl(Composite parent) {
		fParentComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		// layout.marginHeight = 0;
		// layout.marginWidth = 0;
		fParentComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fParentComposite.setLayoutData(gd);

		createRepositoryGroup(fParentComposite);
		createSeparator(fParentComposite);
		this.setControl(fParentComposite);

		Dialog.applyDialogFont(fParentComposite);
	}

	private void createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		separator.setVisible(false);
		GridData data = new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1);
		data.heightHint = convertHeightInCharsToPixels(1) / 3;
		separator.setLayoutData(data);
	}

	private void createRepositoryGroup(Composite control) {
		Composite group = new Composite(control, SWT.NONE);
		GridLayout layout = new GridLayout(6, false);
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.TaskSearchPage_Select_Repository_);

		repositoryCombo = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		repositoryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayQueryPage(repositoryCombo.getSelectionIndex());
			}
		});
		label = new Label(group, SWT.NONE);
		label.setText("  "); //$NON-NLS-1$

		Label labelKey = new Label(group, SWT.NONE);
		labelKey.setText(Messages.TaskSearchPage_Task_Key_ID);
		keyText = new Text(group, SWT.BORDER);
		keyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		String findText = null;
		TaskListView taskListView = TaskListView.getFromActivePerspective();
		if (taskListView != null) {
			findText = taskListView.getFilteredTree().getFilterControl().getText();
			if (findText != null && findText.trim().length() > 0 && isTaskKeyCandidate(findText.trim())) {
				pageContainer.setPerformActionEnabled(true);
				keyText.setText(findText.trim());
				keyText.setFocus();
			}
		}

		keyText.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				// ignore
			}

			public void keyReleased(KeyEvent e) {
				updatePageEnablement();
			}
		});

		ImageHyperlink clearKey = new ImageHyperlink(group, SWT.NONE);
		clearKey.setImage(CommonImages.getImage(CommonImages.REMOVE));
		clearKey.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				keyText.setText(""); //$NON-NLS-1$
				updatePageEnablement();
			}
		});
	}

	private void updatePageEnablement() {
		if (keyText.getText() != null && keyText.getText().trim().length() > 0) {
			//setControlsEnabled(queryPages[currentPageIndex], false);
			if (queryPages != null && queryPages[currentPageIndex] != null
					&& queryPages[currentPageIndex].getData(PAGE_KEY) instanceof AbstractRepositoryQueryPage) {
				((AbstractRepositoryQueryPage) queryPages[currentPageIndex].getData(PAGE_KEY)).setControlsEnabled(false);
			}
			if (repositoryCombo.getSelectionIndex() > -1) {
				pageContainer.setPerformActionEnabled(true);
			}
		} else {
			//setControlsEnabled(queryPages[currentPageIndex], true);
			if (queryPages != null && queryPages[currentPageIndex] != null
					&& queryPages[currentPageIndex].getData(PAGE_KEY) instanceof AbstractRepositoryQueryPage) {
				((AbstractRepositoryQueryPage) queryPages[currentPageIndex].getData(PAGE_KEY)).setControlsEnabled(true);
			}
			//setControlsEnabled(queryPages[currentPageIndex], true);
			//pageContainer.setPerformActionEnabled(false);
		}
	}

	private Control createPage(TaskRepository repository, ITaskSearchPage searchPage) {
		// Page wrapper
		final Composite pageWrapper = new Composite(fParentComposite, SWT.NONE);
		pageWrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		pageWrapper.setLayout(layout);

		searchPage.setContainer(taskSearchPageContainer);
		try {
			searchPage.createControl(pageWrapper);
		} catch (Exception e) {
			pageWrapper.dispose();
			searchPage.dispose();

			searchPage = new DeadSearchPage(repository);
			searchPage.setContainer(taskSearchPageContainer);
			searchPage.createControl(fParentComposite);
			StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN,
					"Error occurred while constructing search page for " + repository.getRepositoryUrl() + " [" //$NON-NLS-1$ //$NON-NLS-2$
							+ repository.getConnectorKind() + "]", e)); //$NON-NLS-1$
			searchPage.getControl().setData(PAGE_KEY, searchPage);
			return searchPage.getControl();
		}

		// XXX: work around for initial search page size issue bug#198493
		IDialogSettings searchDialogSettings = SearchPlugin.getDefault().getDialogSettingsSection(
				"DialogBounds_SearchDialog"); //$NON-NLS-1$
		if (searchDialogSettings.get("DIALOG_WIDTH") == null) { //$NON-NLS-1$
			fParentComposite.getParent().getShell().pack();
		}
		pageWrapper.setData(PAGE_KEY, searchPage);
		return pageWrapper;
	}

	private void displayQueryPage(int pageIndex) {
		if (currentPageIndex == pageIndex || pageIndex < 0) {
			return;
		}

		// TODO: if repository == null display invalid page?
		if (currentPageIndex != -1 && queryPages[currentPageIndex] != null) {
			queryPages[currentPageIndex].setVisible(false);
			ITaskSearchPage page = (ITaskSearchPage) queryPages[currentPageIndex].getData(PAGE_KEY);
			page.setVisible(false);
			GridData data = (GridData) queryPages[currentPageIndex].getLayoutData();
			data.exclude = true;
			queryPages[currentPageIndex].setLayoutData(data);
		}

		String repositoryLabel = repositoryCombo.getItem(pageIndex);
		repository = (TaskRepository) repositoryCombo.getData(repositoryLabel);

		if (queryPages[pageIndex] == null) {
			if (repository != null) {
				AbstractRepositoryConnectorUi connectorUi = TasksUiPlugin.getConnectorUi(repository.getConnectorKind());
				if (connectorUi != null) {
					ITaskSearchPage searchPage = getSearchPage(connectorUi);
					if (searchPage != null) {
						queryPages[pageIndex] = createPage(repository, searchPage);
					} else {
						AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(
								repository.getConnectorKind());
						if (connector.canCreateTaskFromKey(repository)) {
							queryPages[pageIndex] = createPage(repository, new NoSearchPage(repository));
						}
					}
				}

			}
		}

		// update enablement of the task id field
		if (repository != null) {
			AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(
					repository.getConnectorKind());
			if (connector.canCreateTaskFromKey(repository)) {
				keyText.setEnabled(true);
			} else {
				keyText.setEnabled(false);
			}
		}

		if (queryPages[pageIndex] != null) {
			GridData data = (GridData) queryPages[pageIndex].getLayoutData();
			if (data == null) {
				data = new GridData();
			}
			data.exclude = false;
			queryPages[pageIndex].setLayoutData(data);
			queryPages[pageIndex].setVisible(true);
			ITaskSearchPage page = (ITaskSearchPage) queryPages[pageIndex].getData(PAGE_KEY);
			page.setVisible(true);
		}

		currentPageIndex = pageIndex;
		fParentComposite.getParent().layout(true, true);
		updatePageEnablement();

	}

	private ITaskSearchPage getSearchPage(AbstractRepositoryConnectorUi connectorUi) {
		if (connectorUi.hasSearchPage()) {
			return connectorUi.getSearchPage(repository, null);
		}
		return null;
	}

	@Override
	public void setVisible(boolean visible) {
		if (firstView) {
			firstView = false;
			getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

			List<TaskRepository> repositories = TasksUi.getRepositoryManager().getAllRepositories();
			List<TaskRepository> searchableRepositories = new ArrayList<TaskRepository>();
			for (TaskRepository repository : repositories) {
				AbstractRepositoryConnectorUi connectorUi = TasksUiPlugin.getConnectorUi(repository.getConnectorKind());
				AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(
						repository.getConnectorKind());
				if ((connectorUi != null && connectorUi.hasSearchPage() && !repository.isOffline())
						|| connector.canCreateTaskFromKey(repository)) {
					searchableRepositories.add(repository);
				}
			}

			Collections.sort(searchableRepositories, new TaskRepositoryComparator());

			String[] repositoryUrls = new String[searchableRepositories.size()];
			int i = 0;
			int indexToSelect = 0;
			for (TaskRepository currRepsitory : searchableRepositories) {
				if (repository != null && repository.equals(currRepsitory)) {
					indexToSelect = i;
				}
				repositoryUrls[i] = currRepsitory.getRepositoryUrl();
				i++;
			}

			IDialogSettings settings = getDialogSettings();
			if (repositoryCombo != null) {
				for (int x = 0; x < searchableRepositories.size(); x++) {
					repositoryCombo.add(searchableRepositories.get(x).getRepositoryLabel());
					repositoryCombo.setData(searchableRepositories.get(x).getRepositoryLabel(),
							searchableRepositories.get(x));
				}
				if (repositoryUrls.length == 0) {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							Messages.TaskSearchPage_Repository_Search, TaskRepositoryManager.MESSAGE_NO_REPOSITORY);
				} else {
					String selectRepo = settings.get(STORE_REPO_ID);
					if (selectRepo != null && repositoryCombo.indexOf(selectRepo) > -1) {
						repositoryCombo.select(repositoryCombo.indexOf(selectRepo));
						repository = (TaskRepository) repositoryCombo.getData(selectRepo);
						if (repository == null) {
							// TODO: Display no repository error
						}
					} else {
						repositoryCombo.select(indexToSelect);
					}

					// TODO: Create one page per connector and repopulate based on repository
					queryPages = new Control[repositoryUrls.length];
					displayQueryPage(repositoryCombo.getSelectionIndex());
					// updateAttributesFromRepository(repositoryCombo.getText(),
					// null, false);
				}
			}
		}

		if (queryPages == null) {
			pageContainer.setPerformActionEnabled(false);
		}

		super.setVisible(visible);

		setDefaultValuesAndFocus();
	}

	private void setDefaultValuesAndFocus() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				String repositoryUrl = null;
				IWorkbenchPart part = page.getActivePart();
				if (part instanceof ISearchResultViewPart) {
					ISearchQuery[] queries = NewSearchUI.getQueries();
					if (queries.length > 0) {
						if (queries[0] instanceof SearchHitCollector) {
							repositoryUrl = ((SearchHitCollector) queries[0]).getRepositoryQuery().getRepositoryUrl();
						}
					}
				}
//				if (repositoryUrl == null) {
//					IEditorPart editor = page.getActiveEditor();
//					if (editor instanceof TaskEditor) {
//						repositoryUrl = ((TaskEditor) editor).getTaskEditorInput().getTask().getRepositoryUrl();
//					}
//				}
//				if (repositoryUrl == null) {
//					TaskListView taskListView = TaskListView.getFromActivePerspective();
//					if (taskListView != null) {
//						AbstractTask selectedTask = taskListView.getSelectedTask();
//						if (selectedTask != null) {
//							repositoryUrl = selectedTask.getRepositoryUrl();
//						}
//					}
//				}
				if (repositoryUrl != null) {
					TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(repositoryUrl);
					if (repository != null) {
						int index = 0;
						for (String repositoryLabel : repositoryCombo.getItems()) {
							if (repositoryLabel.equals(repository.getRepositoryLabel())) {
								repositoryCombo.select(index);
							}
							index++;
						}
						displayQueryPage(repositoryCombo.getSelectionIndex());
					}
				}
			}
		}

		if (keyText.getText() != null && keyText.getText().trim().length() > 0) {
			keyText.setFocus();
			keyText.setSelection(0, keyText.getText().length());
		} else {
			Clipboard clipboard = new Clipboard(Display.getDefault());
			TextTransfer transfer = TextTransfer.getInstance();
			String contents = (String) clipboard.getContents(transfer);
			if (contents != null) {
				if (isTaskKeyCandidate(contents.trim())) {
					keyText.setText(contents.trim());
					keyText.setFocus();
					keyText.setSelection(0, keyText.getText().length());
				}
			}
		}
		updatePageEnablement();
	}

	private boolean isTaskKeyCandidate(String contents) {
		boolean looksLikeKey = false;
		try {
			Integer.parseInt(contents);
			looksLikeKey = true;
		} catch (NumberFormatException nfe) {
		}
		if (!looksLikeKey) {
			try {
				Integer.parseInt(contents.substring(contents.lastIndexOf('-')));
				looksLikeKey = true;
			} catch (Exception e) {
			}
		}
		return looksLikeKey;
	}

	public IDialogSettings getDialogSettings() {
		IDialogSettings settings = TasksUiPlugin.getDefault().getDialogSettings();
		fDialogSettings = settings.getSection(PAGE_NAME);
		if (fDialogSettings == null) {
			fDialogSettings = settings.addNewSection(PAGE_NAME);
		}
		return fDialogSettings;
	}

	private void saveDialogSettings() {
		IDialogSettings settings = getDialogSettings();
		settings.put(STORE_REPO_ID, repositoryCombo.getText());
	}

	@Override
	public void dispose() {
		if (queryPages != null) {
			for (Control control : queryPages) {
				if (control != null) {
					ITaskSearchPage page = (ITaskSearchPage) control.getData(PAGE_KEY);
					page.dispose();
				}
			}
		}
		super.dispose();
	}

	private class DeadSearchPage extends AbstractRepositoryQueryPage {

		public DeadSearchPage(TaskRepository rep) {
			super("Search page error", rep); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
			Hyperlink hyperlink = new Hyperlink(parent, SWT.NONE);
			hyperlink.setText(Messages.TaskSearchPage_ERROR_Unable_to_present_query_page);
			hyperlink.setUnderlined(true);
			hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					TaskSearchPage.this.getControl().getShell().close();
					TasksUiUtil.openEditRepositoryWizard(getTaskRepository());
					// TODO: Re-construct this page with new
					// repository data
				}

			});

			GridDataFactory.fillDefaults().applyTo(hyperlink);
			setControl(hyperlink);
		}

		@Override
		public IRepositoryQuery getQuery() {
			return null;
		}

		@Override
		public boolean isPageComplete() {
			return false;
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				getSearchContainer().setPerformActionEnabled(false);
			}
		}

		@Override
		public String getQueryTitle() {
			return null;
		}

		@Override
		public void applyTo(IRepositoryQuery query) {
			// ignore			
		}

	}

	private class NoSearchPage extends AbstractRepositoryQueryPage {

		public NoSearchPage(TaskRepository rep) {
			super("No search page", rep); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			Dialog.applyDialogFont(composite);
			setControl(composite);
		}

		@Override
		public IRepositoryQuery getQuery() {
			return null;
		}

		@Override
		public boolean isPageComplete() {
			return false;
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				getSearchContainer().setPerformActionEnabled(false);
			}
		}

		@Override
		public String getQueryTitle() {
			return null;
		}

		@Override
		public void applyTo(IRepositoryQuery query) {
			// ignore
		}

	}
}
