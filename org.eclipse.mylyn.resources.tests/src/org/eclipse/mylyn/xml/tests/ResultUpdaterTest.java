/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.xml.tests;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.mylar.core.IMylarContextNode;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.core.internal.CompositeContext;
import org.eclipse.mylar.core.internal.MylarContext;
import org.eclipse.mylar.core.internal.MylarContextEdge;
import org.eclipse.mylar.core.search.IMylarSearchOperation;
import org.eclipse.mylar.core.tests.support.ResourceHelper;
import org.eclipse.mylar.core.tests.support.WorkspaceSetupHelper;
import org.eclipse.mylar.core.tests.support.search.ISearchPluginTest;
import org.eclipse.mylar.core.tests.support.search.SearchPluginTestHelper;
import org.eclipse.mylar.core.tests.support.search.ActiveSearchNotifier;
import org.eclipse.mylar.java.JavaStructureBridge;
import org.eclipse.mylar.xml.XmlReferencesProvider;

public class ResultUpdaterTest extends TestCase implements ISearchPluginTest{
	private IType type1;
	private IFile plugin1;
	private IJavaProject jp1;
	private static final String SOURCE_ID = "XMLSearchResultUpdaterTest";
	private SearchPluginTestHelper helper;

	@Override
    protected void setUp() throws Exception {
    	//TODO: clear the relationship providers?
    	WorkspaceSetupHelper.setupWorkspace();
    	jp1 = WorkspaceSetupHelper.getProject1();
    	type1 = WorkspaceSetupHelper.getType(jp1, "org.eclipse.mylar.tests.project1.views.SampleView");
    	plugin1 = WorkspaceSetupHelper.getFile(jp1, "plugin.xml");
    	
    	MylarContext t = WorkspaceSetupHelper.getTaskscape();
    	MylarPlugin.getContextManager().contextActivated(t.getId(), t.getId());
    	helper = new SearchPluginTestHelper(this);
    }
    
    @Override
    protected void tearDown() throws Exception {
    	WorkspaceSetupHelper.clearWorkspace();
        WorkspaceSetupHelper.clearDoiModel();
    }
	
	public void testRemoveFile() throws Exception {
		
		int dos = 4;
		
        CompositeContext t = (CompositeContext)MylarPlugin.getContextManager().getActiveContext();
		ActiveSearchNotifier notifier = new ActiveSearchNotifier(t, SOURCE_ID);
		IMylarContextNode searchNode = notifier.getElement(type1.getHandleIdentifier(), JavaStructureBridge.CONTENT_TYPE);
		
		//
		// we should get all results since we are searching the entire workspace
		searchNode = notifier.getElement(type1.getHandleIdentifier(), JavaStructureBridge.CONTENT_TYPE);
		helper.searchResultsNotNull(notifier, searchNode, dos, 3);
		//
		//
		
		Collection<MylarContextEdge> edges = searchNode.getEdges();
		assertEquals(3, edges.size());		
		
		ResourceHelper.delete(plugin1);
		
		Collection<MylarContextEdge> edgesAfterRemove = searchNode.getEdges();
		assertEquals(0, edgesAfterRemove.size());	
	}
	
	public void testRemoveProject() throws Exception {
		int dos = 4;
		
        CompositeContext t = (CompositeContext)MylarPlugin.getContextManager().getActiveContext();
		ActiveSearchNotifier notifier = new ActiveSearchNotifier(t, SOURCE_ID);
		IMylarContextNode searchNode = notifier.getElement(type1.getHandleIdentifier(), JavaStructureBridge.CONTENT_TYPE);
		
		//
		// we should get all results since we are searching the entire workspace
		searchNode = notifier.getElement(type1.getHandleIdentifier(), JavaStructureBridge.CONTENT_TYPE);
		helper.searchResultsNotNull(notifier, searchNode, dos, 3);
		//
		//

		Collection<MylarContextEdge> edges = searchNode.getEdges();
		assertEquals(3, edges.size());	
		
		ResourceHelper.deleteProject(jp1.getProject().getName());
		
		Collection<MylarContextEdge> edgesAfterRemove = searchNode.getEdges();
		assertEquals(0, edgesAfterRemove.size());	;
	}
	
	public List<?> search(int dos, IMylarContextNode node) throws IOException, CoreException{
		if(node == null)
			return null;
		
		// test with each of the sepatations	
		XmlReferencesProvider prov = new XmlReferencesProvider();

		IMylarSearchOperation o = prov.getSearchOperation(node, 0, dos);
		if(o == null) return null;
		
		XMLResultUpdaterSearchListener l = new XMLResultUpdaterSearchListener(prov, node, dos);
		SearchPluginTestHelper.search(o, l);

		return l.getResults();
	}
}
