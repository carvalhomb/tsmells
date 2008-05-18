# !/usr/bin/python
# This file is part of snavtofamix (Source Navigator to FAMIX).
#
# snavtofamix is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2 of the License, or (at your
# option) any later version.
#
# snavtofamix is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along
# with snavtofamix; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#
# Copyright 2006,2007  University of Antwerp
# Author(s): Bart Van Rompaey <bart.vanrompaey2@ua.ac.be>,
#                     Bart Du Bois <bart.dubois@ua.ac.be>

from MultipleLocationEntity import MultipleLocationEntity
from common.ResolveableInformation import TypeReference

##
# Encapsulation of data necessary to refer to an entity.
# The idea is to replace all low-level data from Entities
# by an instance of an EntityReference (or its future subclasses).
##
class EntityReference:
    def __init__(self):
        self.sourceFile = None
        self.lineNr = None

    def getSourceFile(self):
        return self.sourceFile

    def setSourceFile(self, sourceFile):
        self.sourceFile = sourceFile

    def getLineNr(self):
        return self.lineNr

    def setLineNr(self, lineNr):
        self.lineNr = lineNr

    def __eq__(self,other):
        isEqual = False

        if (other != None) & (type(other) == type(self)):
            isEqual = other.getSourceFile() == self.getSourceFile()
            isEqual = isEqual & (other.getLineNr() == self.getLineNr())

        return isEqual

    def __repr__(self):
        return `self.__dict__`.replace("',","'\n")

##
# Encapsulation of data required to refer to a named entity
# that is possibly located in a namespace.
##
class NamedEntityReference(EntityReference):
    def __init__(self):
        self.name = None
        self.namespaceName = None

    def getName(self):
        if self.name == None:
            return ""
        else:
            return self.name

    def setName(self, name):
        self.name = name

    def __eq__(self,other):
        isEqual = False

        if other != None:
            isEqual = EntityReference.__eq__(self, other)
            isEqual = isEqual & (other.getName() == self.getName())

        return isEqual


class NamespaceElementEntityReference(NamedEntityReference):
    def getNamespaceName(self):
        if self.namespaceName == None:
            return ""
        else:
            return self.namespaceName

    def setNamespaceName(self, namespaceName):
        self.namespaceName = namespaceName

    def __eq__(self,other):
        isEqual = False

        if other != None:
            isEqual = NamedEntityReference.__eq__(self, other)
            isEqual = isEqual & (other.getNamespaceName() == self.getNamespaceName())

        return isEqual

##
# Encapsulation of data specific to refer to an
# attribute or method. This reference does not
# derive from NamespaceElementEntityReference, as it should
# always be subclassed in combination with an
# other superclass that *is* a subclass of NamespaceElementEntityReference.
##
class ClassMemberEntityReference:
    def __init__(self):
        self.ownerName = None
        self.ownerTemplateParameters = None

    def getOwnerName(self):
        if self.ownerName == None:
            return ""
        else:
            return self.ownerName

    def setOwnerName(self, name):
        self.ownerName = name

    def getOwnerTemplateParameters(self):
        if self.ownerTemplateParameters == None:
            return ""
        else:
            return self.ownerTemplateParameters

    def setOwnerTemplateParameters(self, parameters):
        self.ownerTemplateParameters = parameters

    def __eq__(self,other):
        isEqual = False

        if (other != None) and (type(other) == type(self)):
            isEqual = other.getOwnerName() == self.getOwnerName()
            isEqual = isEqual & (other.getOwnerTemplateParameters() == self.getOwnerTemplateParameters())

        return isEqual

##
# Encapsulation of the information
# required to refer to a function or method.
##
class InvokableEntityReference(NamespaceElementEntityReference):
    def __init__(self):
        NamespaceElementEntityReference.__init__(self)
        self.formalParameters = None
        self.isAnImplementation = None
        self.locationEntity = None
        self.typeReference = None

    def getFormalParameters(self):
        if self.formalParameters == None:
            return ""
        else:
            return self.formalParameters

    def getBracketLessFormalParameters(self):
    	return self.getFormalParameters().lstrip("(").rstrip(")")

    def setFormalParameters(self, formalParameters):
        self.formalParameters = formalParameters.replace(" *","*").replace(" []","[]")

    def getSignature(self):
        return self.getName() + self.getFormalParameters()

    def getOwnerName(self):
        return ""

    def getOwnerTemplateParameters(self):
        return ""

    def isAbstract(self):
        return False

    def isImplementation(self):
        return self.isAnImplementation

    def setIsImplementation(self, isImplementation):
        self.isAnImplementation = isImplementation

    def getUniqueName(self):
        uniqueName = self.getSignature()

        if self.getNamespaceName() != "":
            uniqueName = self.getNamespaceName() + "::" + uniqueName

        return uniqueName

	##
	# Set the MultipleLocationEntity instance
	# characterizing the location of the entity
	# refered to in this reference.
	##
    def setLocation(self, locationEntity):
    	self.locationEntity = locationEntity

    ##
    # Return a MultipleLocationEntity instance
    # characterizing the location of the entity
    # refered to in this reference.
    ##
    def getLocation(self):
    	return self.locationEntity

    def getTypeReference(self):
    	if self.typeReference == None:
    		return TypeReference()
    	else:
			return self.typeReference

    def setTypeReference(self, typeReference):
    	self.typeReference = typeReference

    ##
    # Verify whether this invokeable entity is equal to a given one.
    # This verification is a.o. used to discover whether an invokeable entity
    # might be the implementation of a declaration that is already
    # in the dictionary.
    ##
    def __eq__(self,other):
    	isEqual = (other != None) & (type(other) == type(self))

    	if isEqual:
            isEqual = other.getName() == self.getName()
            isEqual = isEqual & (other.getFormalParameters() == self.getFormalParameters())
            isEqual = isEqual & (other.getNamespaceName() == self.getNamespaceName())
            #isEqual = isEqual & (other.getLocation() == self.getLocation())
			#isEqual = isEqual & (other.isImplementation() == self.isImplementation())
            # by ignoring the isImplementation, we ensure that an implementation
            # is equal to a definition and vice versa
            if isEqual:
	            haveEqualType = (other.getTypeReference() == self.getTypeReference())
	            isEqual = isEqual & haveEqualType

        return isEqual

##
# Encapsulation of the information
# required to refer to a method.
##
class MethodEntityReference(InvokableEntityReference,ClassMemberEntityReference):
    def __init__(self):
        InvokableEntityReference.__init__(self)
        ClassMemberEntityReference.__init__(self)
        self.isAbstractMethod = None
        self.parentReference = None
        self.modifiers = None
        self.hasClassScope = False

    def isConstructor(self):
    	return (self.getName() == self.getOwnerName())

    def setParentReference(self, parentReference):
    	self.parentReference = parentReference

    def getParentReference(self):
    	if self.parentReference == None:
    		return TypeReference()
    	else:
    		return self.parentReference

    def getOwnerName(self):
        return ClassMemberEntityReference.getOwnerName(self)

    def getOwnerTemplateParameters(self):
        return ClassMemberEntityReference.getOwnerTemplateParameters(self)

    def isAbstract(self):
        return self.isAbstractMethod

    def setIsAbstract(self, isAbstract):
        self.isAbstractMethod = isAbstract

    def setClassScope(self, classScope):
    	self.hasClassScope = classScope

    def getClassScope(self):
    	return self.hasClassScope

    def setModifiers(self, modifiers):
    	self.modifiers = modifiers

    def getModifiers(self):
    	return self.modifiers

    def getUniqueName(self):
        uniqueName = self.getSignature()
	
        ownerName = self.getOwnerName()
        
        if self.getOwnerTemplateParameters() != "":
            ownerName += "<" + self.getOwnerTemplateParameters() + ">"

        uniqueName = ownerName + "." + uniqueName

        if self.getNamespaceName() != "":
            uniqueName = self.getNamespaceName() + "::" + uniqueName

        return uniqueName

    def __eq__(self,other):
		isEqual = (other != None) & (type(other) == type(self))

		if isEqual:
			isEqual = InvokableEntityReference.__eq__(self,other)
			isEqual = isEqual & ClassMemberEntityReference.__eq__(self,other)
			isEqual = isEqual & (other.isAbstract() == self.isAbstract())
			#isEqual = isEqual & (other.getLocation() == self.getLocation())

		return isEqual

class AccessReference:
    """ represents a language independent data access """

    def __init__(self):
        self.src_class = None
        self.src_name = None
        self.formalSrcParams = None
        self.dst_owner = None
        self.dst_name = None
        self.sourceFile = None
        self.start = None

    def getSrcUniqueName(self):
        #return ''.join([self.src_class,".",self.src_name,"(",self.formalSrcParams,")"])
		return self.src_class+"."+self.src_name+"("+self.formalSrcParams+")"

