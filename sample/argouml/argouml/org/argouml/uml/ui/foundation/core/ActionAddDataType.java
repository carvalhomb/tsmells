// $Id: ActionAddDataType.java 9430 2005-11-25 15:28:20Z mvw $
// Copyright (c) 2004-2005 The Regents of the University of California. All
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

package org.argouml.uml.ui.foundation.core;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.ui.AbstractActionNewModelElement;


/**
 * This action creates a new datatype.
 *
 * @author mvw@tigris.org
 */
public class ActionAddDataType extends AbstractActionNewModelElement {

    /**
     * The constructor.
     */
    public ActionAddDataType() {
        super("button.new-datatype");
        putValue(Action.NAME, Translator.localize("button.new-datatype"));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object target = TargetManager.getInstance().getModelTarget();
        Object ns = null;
        if (Model.getFacade().isANamespace(target))
            ns = target;
        if (Model.getFacade().isAParameter(target))
            if (Model.getFacade().getBehavioralFeature(target) != null)
                target = Model.getFacade().getBehavioralFeature(target);
        if (Model.getFacade().isAFeature(target))
            if (Model.getFacade().getOwner(target) != null)
                target = Model.getFacade().getOwner(target);
        if (Model.getFacade().isAEvent(target))
            ns = Model.getFacade().getNamespace(target);
        if (Model.getFacade().isAClassifier(target))
            ns = Model.getFacade().getNamespace(target);

        Object newDt = Model.getCoreFactory().buildDataType("", ns);
        TargetManager.getInstance().setTarget(newDt);
        super.actionPerformed(e);
    }
}
