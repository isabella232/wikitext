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
 * Created on Feb 18, 2005
  */
package org.eclipse.mylar.tasks.ui.views;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylar.tasks.BugzillaTask;
import org.eclipse.mylar.tasks.ITask;
import org.eclipse.mylar.ui.MylarImages;
import org.eclipse.mylar.ui.MylarUiPlugin;
import org.eclipse.mylar.ui.internal.UiUtil;
import org.eclipse.mylar.ui.internal.views.Highlighter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class TaskListLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {

	private Color backgroundColor = null;
	
    public String getColumnText(Object obj, int columnIndex) {
        if (obj instanceof ITask) {
            ITask task = (ITask) obj;
            switch (columnIndex) {
                case 0: return "  "; // padding for background
                case 1: return "";
                case 2: 
                    if (task.isCategory()) {
                        return "";
                    } else {
                        return task.getPriority(); 
                    }
                case 3: 
                    return task.getLabel();
                case 4:
                	return task.getHandle();
            }
        }
        return null;
    }

    public Font getFont(Object element) {
        if (element instanceof ITask) {
            ITask task = (ITask)element;
            if (task.isCategory()) {
                for (ITask child : task.getChildren()) {
                    if (child.isActive()) return UiUtil.BOLD;
                }
            }
            if (task.isActive()) return UiUtil.BOLD;
            if (task.isCompleted()) return UiUtil.ITALIC;
        }
        return null;
    }
   
    public Image getColumnImage(Object element, int columnIndex) {
        
        if (!(element instanceof ITask)) { 
        	return null;
        }
        if (columnIndex == 0) {
        	if (((ITask) element).isCategory()) {
    			return null;
        	}
        	if (((ITask) element).isActive()) {
        		return MylarImages.getImage(MylarImages.TASK_ACTIVE);
        	} else {
        		return MylarImages.getImage(MylarImages.TASK_INACTIVE);
        	}        	
        } else if (columnIndex == 1) {
        	if (((ITask) element).isCategory()) {
    			return null;
    			// return MylarImages.getImage(MylarImages.CATEGORY);
    		} else if (element instanceof BugzillaTask) {
    			return MylarImages.getImage(MylarImages.TASK_BUGZILLA);
    		} else {
    			return MylarImages.getImage(MylarImages.TASK);
    		} 
        } else {
        	return null;
        }
    }

    public Color getBackground(Object element) {
      if (element instanceof ITask) {
          ITask task = (ITask)element;
          if (task.isCategory()) { 
        	  return backgroundColor;
          }
          Highlighter highlighter = MylarUiPlugin.getDefault().getHighlighterForTaskId("" + task.getHandle());
          if (highlighter != null) return highlighter.getHighlightColor();
      }
      return null;
    }
    
    public Color getForeground(Object element) {
        if (element instanceof ITask) {
            ITask task = (ITask)element;
            if (task.isCompleted()) return MylarUiPlugin.getDefault().getColorMap().GRAY_VERY_LIGHT;
        }
        return null;
    }
    
    public void setBackgroundColor(Color c) {
    	this.backgroundColor = c;
    }
}

//public Image getColumnImage(Object obj, int columnIndex) {
//if (obj instanceof Highlighter) {
//  Highlighter h = (Highlighter) obj;
//  switch (columnIndex) {
//  case 1:
//      HighlighterImageDescriptor des;
//      if (h.isGradient()) {
//          des = new HighlighterImageDescriptor(h.getBase(), h
//                  .getLandmarkColor());
//      } else {
//          des = new HighlighterImageDescriptor(h
//                  .getLandmarkColor(), h.getLandmarkColor());
//      }
//      return des.getImage();
//      default:
//          break;
//      }
//  }
//  return null;
//}

//public Color getBackground(Object element) {
//if (element instanceof Task) {
//  Task task = (Task)element;
//  if (task.isActive()) {
//      Highlighter highlighter = MylarUiPlugin.getDefault().getHighlighterForTaskId(((Task)task).getId());
//      if (highlighter != null) {  
//          return highlighter.getHighlightColor();
//      } else {
//          return null;
//      }
//  }
//}
//return null;
//}

//public class TaskListLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {
//
//    public String getText(Object obj) {
//        if (obj instanceof BugzillaTask) {
//        	String desc = MylarTasksPlugin.getDefault().getBugzillaProvider().getBugzillaDescription(
//        			((BugzillaTask)obj));
//            return desc;
//        } else if (obj instanceof Task) {
//            Task task = (Task)obj;
//            return task.toString();// + "  [" + task.getId() + "]"; 
//        } else { 
//            return obj.toString();
//        }
//    }
//    
//    public Image getImage(Object obj) {
//        String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
//        if (obj instanceof BugzillaTask) {
//        	return MylarImages.getImage(MylarImages.TASK_BUGZILLA);         
//        } else if (obj instanceof Task) {
//            return MylarImages.getImage(MylarImages.TASK);
//        } else {
//            return null;
//        }
//    }
//    public Color getForeground(Object element) {
//        return null;
//    }
//    
//    public Color getBackground(Object element) {
//        if (element instanceof Task) {
//            Task task = (Task)element;
//            if (task.isActive()) {
//                Highlighter highlighter = MylarUiPlugin.getDefault().getHighlighterForTaskId(((Task)task).getId());
//                if (highlighter != null) {  
//                    return highlighter.getHighlightColor();
//                } else {
//                    return null;
//                }
//            }
//        }
//        return null;
//    }
//
//    public Font getFont(Object element) {
//        if (element instanceof Task) {
//            if (((Task)element).isActive()) {
//                return UiUtil.BOLD;
//            }
//        }
//        return null;
//    }
//}