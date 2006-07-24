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
/*
 * Created on Aug 6, 2004
 */
package org.eclipse.mylar.internal.java.ui;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.IMylarRelation;
import org.eclipse.mylar.internal.context.core.MylarContextManager;
import org.eclipse.mylar.internal.context.ui.ContextUiImages;
import org.eclipse.mylar.internal.context.ui.views.DelegatingContextLabelProvider;
import org.eclipse.mylar.internal.java.JavaStructureBridge;
import org.eclipse.mylar.internal.java.MylarJavaPlugin;
import org.eclipse.mylar.internal.java.search.AbstractJavaRelationProvider;
import org.eclipse.mylar.internal.java.search.JUnitReferencesProvider;
import org.eclipse.mylar.internal.java.search.JavaImplementorsProvider;
import org.eclipse.mylar.internal.java.search.JavaReadAccessProvider;
import org.eclipse.mylar.internal.java.search.JavaReferencesProvider;
import org.eclipse.mylar.internal.java.search.JavaWriteAccessProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class JavaContextLabelProvider extends AppearanceAwareLabelProvider {

	private static final ImageDescriptor EDGE_REF_JUNIT = MylarJavaPlugin.getImageDescriptor("icons/elcl16/edge-ref-junit.gif");
	
	public JavaContextLabelProvider() {
		super(AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS);
	}

	@Override
	public String getText(Object object) {
		if (object instanceof IMylarElement) {
			IMylarElement node = (IMylarElement) object;
			if (JavaStructureBridge.CONTENT_TYPE.equals(node.getContentType())) {
				IJavaElement element = JavaCore.create(node.getHandleIdentifier());
				if (element == null) {
					return "<missing element>";
				} else {
					return getTextForElement(element);
				}
			}
		} else if (object instanceof IMylarRelation) {
			return getNameForRelationship(((IMylarRelation) object).getRelationshipHandle());
		} else if (object instanceof IJavaElement) {
			return getTextForElement((IJavaElement) object);
		}
		return super.getText(object);
	}

	private String getTextForElement(IJavaElement element) {
		if (DelegatingContextLabelProvider.isQualifyNamesMode()) {
			if (element instanceof IMember && !(element instanceof IType)) {
				String parentName = ((IMember) element).getParent().getElementName();
				if (parentName != null && parentName != "") {
					return parentName + '.' + super.getText(element);
				}
			}
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object object) {
		if (object instanceof IMylarElement) {
			IMylarElement node = (IMylarElement) object;
			if (node.getContentType().equals(JavaStructureBridge.CONTENT_TYPE)) {
				return super.getImage(JavaCore.create(node.getHandleIdentifier()));
			}
		} else if (object instanceof IMylarRelation) {
			ImageDescriptor descriptor = getIconForRelationship(((IMylarRelation) object).getRelationshipHandle());
			if (descriptor != null) {
				return ContextUiImages.getImage(descriptor);
			} else {
				return null;
			}
		}
		return super.getImage(object);
	}

	private ImageDescriptor getIconForRelationship(String relationshipHandle) {
		if (relationshipHandle.equals(AbstractJavaRelationProvider.ID_GENERIC)) {
			return ContextUiImages.EDGE_REFERENCE;
		} else if (relationshipHandle.equals(JavaReferencesProvider.ID)) {
			return ContextUiImages.EDGE_REFERENCE;
		} else if (relationshipHandle.equals(JavaImplementorsProvider.ID)) {
			return ContextUiImages.EDGE_INHERITANCE;
		} else if (relationshipHandle.equals(JUnitReferencesProvider.ID)) {
			return EDGE_REF_JUNIT;
		} else if (relationshipHandle.equals(JavaWriteAccessProvider.ID)) {
			return ContextUiImages.EDGE_ACCESS_WRITE;
		} else if (relationshipHandle.equals(JavaReadAccessProvider.ID)) {
			return ContextUiImages.EDGE_ACCESS_READ;
		} else {
			return null;
		}
	}

	private String getNameForRelationship(String relationshipHandle) {
		if (relationshipHandle.equals(AbstractJavaRelationProvider.ID_GENERIC)) {
			return AbstractJavaRelationProvider.NAME;
		} else if (relationshipHandle.equals(JavaReferencesProvider.ID)) {
			return JavaReferencesProvider.NAME;
		} else if (relationshipHandle.equals(JavaImplementorsProvider.ID)) {
			return JavaImplementorsProvider.NAME;
		} else if (relationshipHandle.equals(JUnitReferencesProvider.ID)) {
			return JUnitReferencesProvider.NAME;
		} else if (relationshipHandle.equals(JavaWriteAccessProvider.ID)) {
			return JavaWriteAccessProvider.NAME;
		} else if (relationshipHandle.equals(JavaReadAccessProvider.ID)) {
			return JavaReadAccessProvider.NAME;
		} else if (relationshipHandle.equals(MylarContextManager.CONTAINMENT_PROPAGATION_ID)) {
			return "Containment"; // TODO: make this generic?
		} else {
			return null;
		}
	}

	public static AppearanceAwareLabelProvider createJavaUiLabelProvider() {
		AppearanceAwareLabelProvider javaUiLabelProvider = new AppearanceAwareLabelProvider(
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS);
		javaUiLabelProvider.addLabelDecorator(new TreeHierarchyLayoutProblemsDecorator());
		return javaUiLabelProvider;
	}
}
