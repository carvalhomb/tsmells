// $Id: AllTests.java 8783 2005-08-20 09:31:09Z linus $
// Copyright (c) 2005 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
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

package org.argouml.model;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all tests in this directory.
 */
public final class AllTests {
    /**
     * Constructor.
     */
    private AllTests() {
    }

    /**
     * Get the list.
     *
     * @return a list of all test cases.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for org.argouml.model");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestCommonBehaviorHelper.class);
        suite.addTestSuite(TestCopyHelper.class);
        suite.addTestSuite(TestUseCasesHelper.class);
        suite.addTestSuite(TestModelManagementFactory.class);
        suite.addTestSuite(TestModelFacade2.class);
        suite.addTestSuite(TestCollaborationsFactory.class);
        suite.addTestSuite(TestCommonBehaviorFactory.class);
        suite.addTestSuite(TestActivityGraphsHelper.class);
        suite.addTestSuite(TestUmlUseCase.class);
        suite.addTest(TestModelFacade3.suite());
        suite.addTestSuite(TestModel2.class);
        suite.addTestSuite(TestUseCasesFactory.class);
        suite.addTest(TestModel.suite());
        suite.addTestSuite(TestUmlFactoryBuildNode.class);
        suite.addTestSuite(TestStateMachinesFactory.class);
        suite.addTestSuite(TestUmlModel.class);
        suite.addTestSuite(TestUmlFactory.class);
        suite.addTestSuite(TestUmlActor.class);
        suite.addTest(TestAgainstUmlModel.suite());
        suite.addTestSuite(TestExtensionMechanismsFactory.class);
        suite.addTestSuite(TestActivityGraphsFactory.class);
        suite.addTestSuite(TestUmlModelElement.class);
        suite.addTestSuite(TestDataTypesFactory.class);
        suite.addTestSuite(TestCoreFactory.class);
        suite.addTestSuite(TestCoreHelper.class);
        suite.addTestSuite(TestStateMachinesHelper.class);
        suite.addTestSuite(TestExtensionMechanismsHelper.class);
        suite.addTestSuite(TestUml.class);
        suite.addTestSuite(TestModelEventPump.class);
        suite.addTestSuite(TestModelManagementHelper.class);
        suite.addTestSuite(TestCollaborationsHelper.class);
        //$JUnit-END$
        return suite;
    }
}
