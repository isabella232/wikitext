/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.context.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.mylyn.context.core.IDegreeOfInterest;
import org.eclipse.mylyn.context.core.IInteractionContextScaling;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
public class CompositeDegreeOfInterest implements IDegreeOfInterest {

	private final Set<IDegreeOfInterest> composed = new HashSet<IDegreeOfInterest>();

	protected IInteractionContextScaling contextScaling;

	public CompositeDegreeOfInterest(IInteractionContextScaling contextScaling) {
		this.contextScaling = contextScaling;
	}

	public void addEvent(InteractionEvent event) {
		for (IDegreeOfInterest info : composed) {
			((DegreeOfInterest) info).addEvent(event);
		}
	}

	public List<InteractionEvent> getEvents() {
		Set<InteractionEvent> events = new HashSet<InteractionEvent>();
		for (IDegreeOfInterest info : composed) {
			events.addAll(info.getEvents());
		}
		return new ArrayList<InteractionEvent>(events);
	}

	public float getValue() {
		float value = 0;
		for (IDegreeOfInterest info : composed) {
			value += info.getValue();
		}
		return value;
	}

	public float getDecayValue() {
		float value = 0;
		for (IDegreeOfInterest info : composed) {
			value += info.getDecayValue();
		}
		return value;
	}

	public float getEncodedValue() {
		float value = 0;
		for (IDegreeOfInterest info : composed) {
			value += info.getEncodedValue();
		}
		return value;
	}

	/**
	 * @return true if one is interesting
	 */
	public boolean isInteresting() {
		boolean isInteresting = false;
		for (IDegreeOfInterest info : composed) {
			if (info.isInteresting()) {
				isInteresting = true;
			}
		}
		return isInteresting;
	}

	/**
	 * @return true if all are predicted
	 */
	public boolean isPropagated() {
		if (composed.isEmpty()) {
			return false;
		}
		boolean allPropagated = true;
		for (IDegreeOfInterest info : composed) {
			if (!info.isPropagated()) {
				allPropagated = false;
			}
		}
		return allPropagated;
	}

	public boolean isPredicted() {
		if (composed.isEmpty()) {
			return false;
		}
		boolean allPredicted = true;
		for (IDegreeOfInterest info : composed) {
			if (!info.isPredicted()) {
				allPredicted = false;
			}
		}
		return allPredicted;
	}

	public boolean isLandmark() {
		return getValue() >= contextScaling.getLandmark();
	}

	public Set<IDegreeOfInterest> getComposedDegreesOfInterest() {
		return composed;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("composite(");
		for (IDegreeOfInterest info : composed) {
			result.append(info.toString());
		}
		result.append(")");
		return result.toString();
	}
}
