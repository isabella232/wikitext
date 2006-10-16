/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *     Eike tepper - commit comment template preferences
 *******************************************************************************/

package org.eclipse.mylar.internal.team.ui.preferences;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.mylar.internal.team.template.TemplateHandlerContentProposalProvider;
import org.eclipse.mylar.team.MylarTeamPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.fieldassist.ContentAssistField;

/**
 * @author Mik Kersten
 */
public class MylarTeamPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Button manageChangeSets;

	private Text commitTemplate = null;

//	private Text commitTemplateProgress = null;

//	private Text regexText;

//	private Button guessButton;

//	private Button autoGuessButton;

	public MylarTeamPreferencePage() {
		super();
		setPreferenceStore(MylarTeamPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		createChangeSetGroup(container);
		createCommitGroup(container);
		return container;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(MylarTeamPlugin.COMMIT_TEMPLATE, commitTemplate.getText());
//		getPreferenceStore().setValue(MylarTeamPlugin.COMMIT_TEMPLATE_PROGRESS, commitTemplateProgress.getText());
//		getPreferenceStore().setValue(MylarTeamPlugin.COMMIT_REGEX_TASK_ID, regexText.getText());
//		getPreferenceStore().setValue(MylarTeamPlugin.COMMIT_REGEX_AUTO_GUESS, autoGuessButton.getSelection());
		getPreferenceStore().setValue(MylarTeamPlugin.CHANGE_SET_MANAGE, manageChangeSets.getSelection());

		if (manageChangeSets.getSelection()) {
			MylarTeamPlugin.getDefault().getChangeSetManager().enable();
		} else {
			MylarTeamPlugin.getDefault().getChangeSetManager().disable();
		}
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	public void performDefaults() {
		super.performDefaults();
		commitTemplate.setText(getPreferenceStore()
				.getDefaultString(MylarTeamPlugin.COMMIT_TEMPLATE));
//		commitTemplateProgress.setText(getPreferenceStore().getDefaultString(MylarTeamPlugin.COMMIT_TEMPLATE_PROGRESS));
		manageChangeSets.setSelection(getPreferenceStore().getDefaultBoolean(MylarTeamPlugin.CHANGE_SET_MANAGE));
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(data);
		return label;
	}

	private void createChangeSetGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Change Set Management");
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		manageChangeSets = new Button(group, SWT.CHECK);
		manageChangeSets.setText("Automatically create and manage with task context");
		manageChangeSets.setSelection(getPreferenceStore().getBoolean(MylarTeamPlugin.CHANGE_SET_MANAGE));
	}

	private void createCommitGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Automatic Commit Messages");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label completedLabel = createLabel(group, "Template: ");
		completedLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		String completedTemplate = getPreferenceStore().getString(MylarTeamPlugin.COMMIT_TEMPLATE);
//		commitTemplate = addTemplateField(group, completedTemplate,
//				new TemplateHandlerContentProposalProvider());
//
//		Label progressLabel = createLabel(group, "In progress task template: ");
//		progressLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
//
//		String progressTemplate = getPreferenceStore().getString(MylarTeamPlugin.COMMIT_TEMPLATE);
		commitTemplate = addTemplateField(group, completedTemplate, new TemplateHandlerContentProposalProvider());
//		commitTemplate.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				if (autoGuessButton.getSelection()) {
//					guessRegex();
//				}
//			}
//		});

//		Label regexLabel = createLabel(group, "Regex to parse task id: ");
//		regexLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
//
//		GridLayout layout = new GridLayout(3, false);
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//
//		Composite composite = new Composite(group, SWT.NONE);
//		composite.setLayout(layout);
//		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

//		boolean autoGuess = getPreferenceStore().getBoolean(MylarTeamPlugin.COMMIT_REGEX_AUTO_GUESS);
//		String regex = getPreferenceStore().getString(MylarTeamPlugin.COMMIT_REGEX_TASK_ID);
//		regexText = addTemplateField(composite, regex, new RegExContentProposalProvider(true));
//
//		guessButton = new Button(composite, SWT.PUSH);
//		guessButton.setText("Guess");
//		guessButton.setEnabled(!autoGuess);
//		guessButton.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//
//			public void widgetSelected(SelectionEvent e) {
//				guessRegex();
//			}
//		});

//		autoGuessButton = new Button(composite, SWT.CHECK);
//		autoGuessButton.setText("Auto");
//		autoGuessButton.setSelection(autoGuess);
//		autoGuessButton.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//
//			public void widgetSelected(SelectionEvent e) {
//				if (autoGuessButton.getSelection()) {
//					guessButton.setEnabled(false);
//					guessRegex();
//				} else {
//					guessButton.setEnabled(true);
//				}
//			}
//		});
	}

	private Text addTemplateField(final Composite parent, final String text, IContentProposalProvider provider) {
		IControlContentAdapter adapter = new TextContentAdapter();
		IControlCreator controlCreator = new IControlCreator() {
			public Control createControl(Composite parent, int style) {
				Text control = new Text(parent, style);
				control.setText(text);
				return control;
			}
		};

		ContentAssistField field = new ContentAssistField(parent, SWT.BORDER, controlCreator, adapter, provider, null,
				new char[] { '$' });

		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = false;
		field.getLayoutControl().setLayoutData(gd);

		return (Text) field.getControl();
	}

//	protected void guessRegex() {
//		String template = commitTemplate.getText();
//		String regex = MylarTeamPlugin.getDefault().getCommitTemplateManager().getTaskIDRegEx(template);
//		regexText.setText(regex);
//	}
}
