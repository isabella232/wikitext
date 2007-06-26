/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package upload;

import java.util.Comparator;

/**
 * This class was copied from org.eclipse.mylyn.internal.monitor.core.collection.
 * 
 * 
 * @author Mik Kersten and Leah Findlater
 */
class PercentUsageComparator implements Comparator<String> {
	public int compare(String o1, String o2) {
		int index1 = o1.indexOf('%');
		int index2 = o2.indexOf('%');
		if (index1 != -1 && index2 != -1) {
			String s1 = o1.substring(0, index1);
			String s2 = o2.substring(0, index2);
			return (-1) * new Float(s1).compareTo(new Float(s2));
		} else {
			return 0;
		}
	}
}
