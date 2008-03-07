import os, com, java, javax.swing, java.lang, jarray, cPickle
from java.awt.geom import GeneralPath
from java.awt import Polygon


class sourceButton(com.hp.hpl.guess.ui.DockableAdapter):

    def __init__(self):
        # create a new button called center
        testButton = JButton("source")

        # every time the button is pressed, center the display
        testButton.actionPerformed = lambda event: openeditor(1,2)

        # add the button to the toolbar
        self.add(testButton)

        # add the toolbar to the main UI window
        ui.dock(self)

    def getTitle(self):
        # define the title in the window
        return("dockexample1")

def openeditor(file, line):
    toExec = "kwrite " + file + " --line " + str(line)
    print "executing " + toExec
    Runtime.getRuntime().exec(toExec)


# define an action for that menu item
def action(targetNode):
    root = srcDict['ProjectSourceRootDirectory']
    print targetNode.name
    for location in srcDict[targetNode.name[0]]:
        print root
        print location
        openeditor(root + location[0], location[1])


xpoints = jarray.array((10,5,0,5),'i')
ypoints = jarray.array((5,10,5,0),'i')

diamond = Polygon(xpoints,ypoints,4);
shapeDB.addShape(104,diamond)

import time

makeFromGDF(os.environ['TSMELLS_GDF'])
pcklFile = open(os.environ['TSMELLS_SRCPICKLE'],'rb')
srcDict = cPickle.load(pcklFile)
remove((entity=='package'))
sourceButton()
gemLayout()
time.sleep(2)
binPackLayout(0)
time.sleep(1)
center()

# create a new menu item
newMenuItem = NodeEditorPopup.addItem("toSource")
# map the events produced by the click events to the action function
newMenuItem.menuEvent = action
setDisplayBackground("black")
