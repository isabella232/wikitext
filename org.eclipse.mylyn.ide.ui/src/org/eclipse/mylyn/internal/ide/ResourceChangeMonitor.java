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
package org.eclipse.mylar.internal.ide;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.provisional.core.InteractionEvent;
import org.eclipse.mylar.provisional.core.MylarPlugin;

/**
 * @author Mik Kersten
 */
public class ResourceChangeMonitor implements IResourceChangeListener {

	private boolean enabled = true;

	public void resourceChanged(IResourceChangeEvent event) { 
		if (!enabled || !MylarPlugin.getContextManager().isContextActive()
				|| MylarPlugin.getContextManager().isContextCapturePaused()) {
			return;
		}
		if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
			return;
		}
		final Set<IResource> addedResources = new HashSet<IResource>();
		final Set<IResource> changedResources = new HashSet<IResource>();
		IResourceDelta rootDelta = event.getDelta();
		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) {
				IResourceDelta[] added = delta.getAffectedChildren(IResourceDelta.ADDED);
				for (int i = 0; i < added.length; i++) {
					IResource resource = added[i].getResource();
					if (resource instanceof IFile) {
						addedResources.add(resource);
					}
				}
				IResourceDelta[] changed = delta.getAffectedChildren(IResourceDelta.CONTENT | IResourceDelta.REMOVED | IResourceDelta.MOVED_TO | IResourceDelta.MOVED_FROM);
				for (int i = 0; i < changed.length; i++) {
					IResource resource = changed[i].getResource();
					if (resource instanceof IFile) {
						changedResources.add(resource);
					} 
				}
				return true;
			}
		};
		try {
			rootDelta.accept(visitor);
			MylarIdePlugin.getDefault().getInterestUpdater().addResourceToContext(addedResources, InteractionEvent.Kind.SELECTION);
			MylarIdePlugin.getDefault().getInterestUpdater().addResourceToContext(changedResources, InteractionEvent.Kind.PREDICTION);
		} catch (CoreException e) {
			MylarStatusHandler.log(e, "could not accept marker visitor");
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}

// private void processMarkerDelata(IMarkerDelta[] markers) {
// for(IMarkerDelta markerDelta: markers){
// try{
// final IMarker marker = markerDelta.getMarker();
// if(marker == null || !marker.exists()){
// final IMylarStructureBridge bridge =
// MylarPlugin.getDefault().getStructureBridge(marker.getResource());
// if(bridge != null){
// if(!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
// PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
// public void run() {
// MylarPlugin.getContextManager().removeErrorPredictedInterest(bridge.getHandleIdentifier(marker.getResource()),
// bridge.getContentType(), true);
// }});
// }
// }
// }
//					
// if(markerDelta.getMarker().isSubtypeOf(IMarker.PROBLEM)){
// final IMylarStructureBridge bridge =
// MylarPlugin.getDefault().getStructureBridge(marker.getResource());
// if(bridge != null){
// if(!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
// PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
// public void run() {
// MylarPlugin.getContextManager().addErrorPredictedInterest(bridge.getHandleIdentifier(marker.getResource()),
// bridge.getContentType(), true);
// }});
// }
// }
// } else
// {//if(!markerDelta.getMarker().getType().equals("org.eclipse.jdt.core.problem")){
// final IMylarStructureBridge bridge =
// MylarPlugin.getDefault().getStructureBridge(marker.getResource());
// if(bridge != null){
// if(!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
// PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
// public void run() {
// MylarPlugin.getContextManager().removeErrorPredictedInterest(bridge.getHandleIdentifier(marker.getResource()),
// bridge.getContentType(), true);
// }});
// }
// }
// }
// }catch (Exception e){
// MylarPlugin.log(e, " could not update marker");
// }
// }
// }