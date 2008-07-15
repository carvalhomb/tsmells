// $Id: UMLTransitionEffectList.java 6754 2004-09-22 17:55:59Z mvw $
// Copyright (c) 1996-2002 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

// $Id: UMLTransitionEffectList.java 6754 2004-09-22 17:55:59Z mvw $
package org.argouml.uml.ui.behavior.state_machines;

import javax.swing.JPopupMenu;

import org.argouml.uml.ui.UMLModelElementListModel2;
import org.argouml.uml.ui.UMLMutableLinkedList;
import org.argouml.uml.ui.behavior.common_behavior.ActionNewAction;
import org.argouml.uml.ui.behavior.common_behavior.PopupMenuNewAction;

/**
 * @since Dec 15, 2002
 * @author jaap.branderhorst@xs4all.nl
 */
public class UMLTransitionEffectList extends UMLMutableLinkedList {

    /**
     * Constructor for UMLTransitionTriggerList.
     * @param dataModel the model
     */
    public UMLTransitionEffectList(
        UMLModelElementListModel2 dataModel) {
        super(dataModel);
    }

    /**
     * @see org.argouml.uml.ui.UMLMutableLinkedList#getPopupMenu()
     */
    public JPopupMenu getPopupMenu() {
        return new PopupMenuNewAction(ActionNewAction.Roles.EFFECT, this);
    }

}
