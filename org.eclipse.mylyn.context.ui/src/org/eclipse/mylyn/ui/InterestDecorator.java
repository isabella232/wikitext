/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 12, 2005
  */
package org.eclipse.mylar.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.mylar.core.IMylarStructureBridge;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.core.model.ITaskscapeNode;
import org.eclipse.mylar.core.model.internal.TaskscapeEdge;
import org.eclipse.mylar.ui.internal.UiUtil;
import org.eclipse.swt.graphics.*;


/**
 * Not currently used.
 * 
 * @author Mik Kersten
 */
public class InterestDecorator implements ILabelDecorator, IFontDecorator, IColorDecorator {

    public InterestDecorator() {
        super();
    }

    private ITaskscapeNode getNode(Object element) {
        ITaskscapeNode node = null;
        if (element instanceof ITaskscapeNode) {
            node = (ITaskscapeNode)element;
        } else {
            IMylarStructureBridge adapter = MylarPlugin.getDefault().getStructureBridge(element);
            node = MylarPlugin.getTaskscapeManager().getNode(adapter.getHandleIdentifier(element));
        }
        return node;
    } 
  
    public void addListener(ILabelProviderListener listener) {
    	// don't care about listeners
    }

    public void dispose() {
    	// don't care when we are disposed
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    	// don't care about listeners
    }

    public Image decorateImage(Image image, Object element) {
        return null;
    }

    public String decorateText(String text, Object element) {
        return null;
    }

    public Font decorateFont(Object element) {
        ITaskscapeNode node = getNode(element);
        if (node != null) {    
            if (node.getDegreeOfInterest().isLandmark() && !node.getDegreeOfInterest().isPredicted()) {
                return UiUtil.BOLD;
            } 
        } 
        return null;
    }

    public Color decorateForeground(Object element) {
        ITaskscapeNode node = getNode(element);
        if (element instanceof TaskscapeEdge) {
            return MylarUiPlugin.getDefault().getColorMap().RELATIONSHIP;
        } else if (node != null) {
            UiUtil.getBackgroundForElement(node);
        }
        return null;
    }

    public Color decorateBackground(Object element) {
        ITaskscapeNode node = getNode(element);
        if (node != null) {
            return UiUtil.getForegroundForElement(node);   
        } else {
            return null;
        }
    }
}
