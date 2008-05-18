#! /usr/bin/perl -w
#
# This file is part of Fetch (the Fact Extraction Tool CHain).
#
# Fetch is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# Fetch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Anastacia; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
# Copyright 2007 University of Antwerp
# Author(s): Matthias Rieger <matthias.rieger@gmail.com>

# Island parser to extract class attributes/fields from Java source code.
# Creates output in the SourceNavigator DBDump format, e.g.
#
#   gpp2famix::node::traversal::FailingNodeTraversal;pred;000017.000 gpp2famix/node/traversal/FailingNodeTraversal.java;17.000;0x1;{NodePredicate};{};{};{}
#
# The parser is able to deal with:
#
#     - comments
#     - nested classes
#     - newlines at possible and impossible places
#     - variable initializers (are successfully ignored)
#     - static class initializers (are successfully ignored)
#
# Invocation:
#
# To traverse a directory hierarchy handling all Java files encountered,
# use this invocation:
#
#   getJavaFields.pl <srcdir> [<srcdir> ...]
#
# If you want to only look a ta number of specific Java files, use this
# invocation:
#
#   getJavaFields.pl -f <file> [-f <file> ...]
#
#
#######################################################################
# Author:  Matthias Rieger
# Created: June 15/18, 2007
# History: + August 24, 2007
#            we now also parse multiple fields from the same declaration.
#            correctly recongizes generic types and (multidimensional)
#            array types.
#
#######################################################################
# $Id: getJavaFields.pl,v 1.1 2007/06/18 15:08:16 rieger Exp $
#######################################################################

#______________________________________________________________________
#_____________________                           ______________________
#_____________________  S t a t e M a c h i n e  ______________________
#______________________________________________________________________
package StateMachine;


# This class represents a statemachine which transforms an input text
# into an output text. The input is read character by character, and the
# following states are selected by looking at one character. The statemachine
# keeps track of the linenumbers.
# The states and actions of the statemachine are configurable.
#
# All states that begin with the string 'found' are (intermediate)
# end states. They halt the statemachine and let the user deal with
# what has been found so far. Afterwards, the statemachine can be continued
# with the execute-method.


sub new {
  my ($class,$stateTable,$text,$offset) = @_;
  my $type = ref($class) || $class;
  $self = {};
  bless $self, $type;
  $self->{STATETABLE} = $stateTable;
  $self->{SOURCE}     = $text;
  $self->{LINENO}     = $offset;
  return $self
}

sub curchar       { return $_[0]->{CURCHAR}              }
sub lineno        { return $_[0]->{LINENO}               }
sub offsetAtStart { return $_[0]->{OFFSETATSTART}        }
sub target        { return $_[0]->{TARGET}               }
# returns the part of the source that has not yet been seen by the state machine
sub rest          { return substr($_[0]->{SOURCE},$_[0]->{POSITION}) }
sub state         { return $_[0]->{STATE}                }
sub resetState    { $_[0]->{STATE} = $_[0]->{INITSTATE}  }
sub setState      { $_[0]->{STATE} = $_[1]               }
sub atEndState    { return ($_[0]->{STATE} =~ m/^found/) }

# peeks one character ahead
sub nextchar {
  my ($self) = @_;
  my $peekpos = $self->{POSITION}+1;
  return ($peekpos <= length $self->{SOURCE} )
         ?  ''
         :  substr($self->{SOURCE},$peekpos,1);
}

# appends one character to the end of the target text
sub appendchar {
  my ($self,$char) = @_;
  $self->{TARGET} .= $char
}

# copies the current character from the original
sub copyNadvance {
  my ($self) = @_;
  $self->appendchar($self->{CURCHAR});
  $self->advance
}

# advances one character
sub advance {
  my ($self) = @_;
  return if($self->hasSeenAll);
  $self->{POSITION}++;
  $self->{CURCHAR} = substr($self->{SOURCE},$self->{POSITION},1);
  if($self->{CURCHAR} =~ m/\n/) { $self->{LINENO}++ }
}

# returns true if we're at the end of the text
sub hasSeenAll {
  my ($self) = @_;
  return ($self->{POSITION} >= length $self->{SOURCE});
}

# starts the state machine
sub initialize {
  my ($self,$initialState) = @_;
  $self->{INITSTATE}  = $initialState;
  $self->resetState;
  $self->{POSITION} = -1;
  $self->{CURCHAR}  = '';
  $self->advance;
}

# continues executing the statemachine
# returns the transformed code if we have found an end state
# or if we have seen all of the original text
sub execute {
  my ($self) = @_;
  $self->trim;
  $self->{TARGET} = '';
  $self->{OFFSETATSTART} = $self->{LINENO};
  while(! $self->hasSeenAll) {
    my $stateDispatcher = $self->{STATETABLE}->{$self->{STATE}};
    my $actionCode = $stateDispatcher->{$self->{CURCHAR}};
    unless(defined $actionCode) {
      $actionCode = $stateDispatcher->{"otherwise"};
    }
    &$actionCode($self);
    if($self->atEndState) { return $self->{TARGET} }
  }
  return $self->{TARGET};
}


# eats all the whitespace at the current position of the 'SOURCE' text
# without copying it to the 'TARGET' text
sub trim {
  my ($self) = @_;
  while($self->{POSITION} < length $self->{SOURCE} &&
        $self->{CURCHAR} =~ m/\s/s)
  {
        $self->advance
  }
}

#______________________________________________________________________
#_____________________________           ______________________________
#_____________________________  M a i n  ______________________________
#______________________________________________________________________
package Main;

use File::Spec;
use File::Find;
use File::Basename;

#____________________                             _____________________
#____________________  B e g i n   G l o b a l s  _____________________
$currentFilename = '';
$package         = ''; # package of the parsed class
@classStack      = (); # all classes in the file, from out to inner
#______________________                         _______________________
#______________________  E n d   G l o b a l s  _______________________

#___________________                                ___________________
#___________________  Begin Defaults and Constants  ___________________
$packageDelim = '::';  # the FAMIX delimiter between packages
# all potential field modifiers for Java, see Section '2.9.1 Field Modifiers' on
# http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc.html#29888
$fieldModifiers = qr/static|public|private|protected|final|transient|volatile/;
# this encoding for visibility modifiers come from Source Navigator
%visibilityCodes  = (	'private'          => '0x1',
			'protected'        => '0x2',
			'public'           => '0x4',
			'static private'   => '0x8',
			'static protected' => '0x10',
			'static public'    => '0x20'   );
$defaultVisibility = 'public';
#____________________                              ____________________
#____________________  End Defaults and Constants  ____________________


# callback for find
sub wanted {
  my $file = $_;
  return if(-d $File::Find::name);
  return unless($File::Find::name =~ m/\.java$/);
  #my ($name,$path,$suffix) = fileparse($File::Find::name,".java");
  #print STDERR "Looking at File ${File::Find::name}:\n";
  my $relativeFilename = removeRootDir($File::Find::name);
  extractAttributes($File::Find::name);
}

# removes the $rootdir prefix found in $filename
sub removeRootDir {
  my ($filename) =@_;
  foreach my $rootdir (sort {length $b <=> length $a} @rootdirs) {
    my $l = length $rootdir;
    if(substr($filename,0,$l) eq $rootdir) {
      return substr($filename,$l+1)
    }
  }
  return $filename
}

# reads the code from a file and removes all comments
sub getCleanCode {
  my ($filename) = @_;
  my $code;
  local *IN;
  { $/=undef;
    open IN, $filename or die "Could not open file '$filename': $!\n";
    $code = <IN>;
    close IN;
  }

  # removing C/C++/Java comments with single regular expression from
  # http://perldoc.perl.org/perlfaq6.html#How-do-I-use-a-regular-expression-to-strip-C-style-comments-from-a-file%3f
  # We have included the C++ comment removal
  # and some group to be able to retain linebreaks from multiline
  # comments.
  # This regex also removes strings completely, i.e. including the delimiters
  $code=~s{
       (      ##      Either a comment    (Group $1)

          /\*         ##  Start of /* ... */ comment
           [^*]*\*+   ##  Non-* followed by 1-or-more *'s
          (?:
           [^/*][^*]*\*+
          )*          ##  0-or-more things which don't start with /
                      ##    but do end with '*'
          /           ##  End of /* ... */ comment

        |

          //[^\n]*     ## C++ style comments


        |     ##     OR  strings:

          "           ##  Start of " ... " string
           (?:
            \\.           ##  Escaped char
           |              ##    OR
            [^"\\]        ##  Non "\
           )*
          "           ##  End of " ... " string

        |

          '           ##  Start of ' ... ' string
           (?:
            \\.           ##  Escaped char
           |               ##    OR
            [^'\\]        ##  Non '\
           )*
          '           ##  End of ' ... ' string
      )

       |         ##     OR regular code

      (
         .           ##  Any other char
         [^/"'\\]*   ##  Chars which don't start a comment, string or escape
      )
     }{defined $2 ? $2 : retainLineBreaks($1) }gxse;

  return $code
}

# expects a (mutliline) string. Removes everything
# except the newlines. Makes sure that the returned
# string consists in at least a single space
sub retainLineBreaks {
  my ($text) = @_;
  $text =~ s/[^\n]+//sg;
  $text = ' ' if($text eq "");
  return $text;
}

# expects the name of a java file and extracts all the Java fields
sub extractAttributes {
  my ($filename) = @_;
  $currentFilename = $filename;
  my $code = getCleanCode($filename);
  ($package,@classStack) = ('');
  parseCode($code,1);
}


# This statemachine looks for statements, or blocks.
# It ignores initializers or variable declarations.
($codeReaderInitialState,$codeReader) = ("seeking",
{
    #-------------------------------------------------------------
    # in the initial state we are open for statements, or blocks,
    # and we also recognize the beginning of an initializer
    seeking   => {
	"{"      =>  sub { my ($self) = @_;
	                   $self->copyNadvance;
	                   $self->{BLOCKLEVEL} = 1;
	                   $self->setState("blockreading")    },
        ";"      =>  sub { my ($self) = @_;
                           $self->copyNadvance;
                           $self->setState("foundStatement")  },
        otherwise => sub { $_[0]->copyNadvance                }
    },
    #-------------------------------------------------------------
    # Reading braces until we are back again on the lowest nesting
    # level
    blockreading => {
	"{"        => sub { my ($self) = @_;
	                    $self->copyNadvance;
			    $self->{BLOCKLEVEL}++             },
	"}"        => sub { my ($self) = @_;
	                    $self->copyNadvance;
	                    $self->{BLOCKLEVEL}--;
	                    if($self->{BLOCKLEVEL}==0) {
	                      $self->setState("foundBlock")
	                    }                                 },
        otherwise  => sub { $_[0]->copyNadvance;              }
    },
    #-------------------------------------------------------------
    # Reading an initializer basically means jumping over everything
    # until we encounter a ';'
    initializer => {
        ";"        => sub { $_[0]->setState("foundStatement") },
        otherwise  => sub { $_[0]->advance;                   }
    }
});


# expects source code to look at. This can either be the entire
# contents of a Java file, or a class declaration.
sub parseCode {
  my ($code,$offset) = @_;
  my $sm = StateMachine->new($codeReader,$code,$offset);
  $sm->initialize($codeReaderInitialState);
  while(! $sm->hasSeenAll) {
    my $foundEntity = $sm->execute;
    my $offset      = $sm->offsetAtStart;
    if($sm->state eq "foundStatement") {
      lookAtStatement($foundEntity,$offset);
    } elsif($sm->state eq "foundBlock")   {
      lookAtBlock($foundEntity,$offset);
    } else {
      # no statement or block was found
      # this mostly means that there is some white space left at the
      # end of the last entity found
      if($foundEntity !~ m/^\s*$/) {
         #die "We found an unnamed, nonempty entity, in file '$currentFilename' on line $offset.\n";
      	 print "We found an unnamed, nonempty entity, in file '$currentFilename' on line $offset.\n";
      }
    }
    $sm->resetState;
  }
}

# looking at a block, and going deeper if we have a class declaration on our hands
sub lookAtBlock {
  my ($block,$offset) =@_;
  $block =~ s/\}\s*$//s;
  $block =~ s/^([^{]+){//s;
  my $blockHead = $1;
  my $offsetDelta = $blockHead =~ s/\n//g;
  # we ignore blocks that are not class declarations
  return unless($blockHead =~ /\bclass\s+(\S+)/s);
  push @classStack, $1;
  parseCode($block,$offset+$offsetDelta);
  pop @classStack
}

#parses a statement, looking for stuff we find interesting
sub lookAtStatement {
  my ($statement,$offset) = @_;
  $statement =~ s/;\s*$//s;
  return if($statement =~ m/^\s*$/);
  return if($statement =~ m/^import/);
  # abstract function declarations are recognized as statements by our parser
  return if($statement =~ m/\babstract\b/);
  if($statement =~ m/^package\s+(\S+)/s) {
    $package = $1;
    # replace the java delimiter with the FAMIX delimiter
    $package =~ s/\./$packageDelim/g;
    return
  }
  # here we are sure that it's a field declaration
  extractFieldsFromDeclaration($statement,$offset);
}

# This statemachine looks for the fields in a field declaration.
# It ignores initializers, and deals with multiple fields having the
# same type.
($fieldListInitialState,$fieldListReader) = ("seeking",
{
    #-------------------------------------------------------------
    # We seek for field initializers and commata, which indicate
    # another fields that is declared
    seeking   => {
	"="      =>  sub { $_[0]->advance;
	                   $_[0]->setState("initializer")     },
        ","      =>  sub { $_[0]->advance;
                           $_[0]->setState("foundField")      },
        otherwise => sub { $_[0]->copyNadvance                }
    },
    #-------------------------------------------------------------
    # We ignore everything in an initializer, execept parameter lists
    # of function calls which we have to deal separately with because
    # they may contain our delimiter, the comma
    initializer => {
	"("        => sub { $_[0]->advance;
			    $_[0]->{PARENLEVEL}++;
			    $_[0]->setState("paramlist")      },
	","        => sub { $_[0]->advance;
	                    $_[0]->setState("foundField")     },
        otherwise  => sub { $_[0]->advance;              }
    },
    #-------------------------------------------------------------
    # Parameter lists may be nested.
    # We ignore all the content of a parameter list, because they are
    # part of an initializer which is uninteresting.
    paramlist => {
        "("        => sub { $_[0]->advance;
			    $_[0]->{PARENLEVEL}++             },
	")"        => sub { $_[0]->advance;
	                    $_[0]->{PARENLEVEL}--;
	                    if($_[0]->{PARENLEVEL}==0) {
	                      $_[0]->setState("initializer")
	                    }                                 },
        otherwise  => sub { $_[0]->advance;                   }
    }
});


# expects the code of the decalaration
sub extractFieldsFromDeclaration {
  my ($declaration,$offset) = @_;

  # We first extract the type of the declaration
  # which will be valid for all declared fields
  my ($typeModifiers,$type) = extractType(\$declaration,\$offset);

  # then we extract all field names in the declaration
  my $sm = StateMachine->new($fieldListReader,$declaration,$offset);
  $sm->initialize($fieldListInitialState);
  while(! $sm->hasSeenAll) {
    my $fieldName   = $sm->execute;
    my $fieldOffset = $sm->offsetAtStart;
    my $currentType = $type;
    # fieldnames can have array dimensions after it
    if($fieldName =~ s/[\]\[\s]+$//) { $currentType .= $& }
    outputField($currentType,$typeModifiers,$fieldName,$fieldOffset);
    $sm->resetState;
  }
}

# This statemachine parses the type of a field declaration.
($fieldTypeInitialState,$fieldTypeReader) = ("seeking",
{
    #-------------------------------------------------------------
    # The name of a type ends with the first whitespace character.
    # Of course we have to check outside if there follows an array
    # dimension or a generic specification after some gap.
    seeking   => {
	"["       => sub { $_[0]->copyNadvance;
	                   $_[0]->setState("arraydimension")  },
        "<"       => sub { $_[0]->copyNadvance;
                           $_[0]->{ANGLELEVEL}++;
                           $_[0]->setState("generic")         },
        # since our statemachine dispatches on characters, not on regexes
        # we have to add the same code for all potential whitespace characters
        # TODO: make the statemachine use regexes
        " "       => sub { $_[0]->copyNadvance;
			   $_[0]->setState("foundType")       },
        "\t"      => sub { $_[0]->copyNadvance;
			   $_[0]->setState("foundType")       },
        "\n"      => sub { $_[0]->copyNadvance;
			   $_[0]->setState("foundType")       },
        otherwise => sub { $_[0]->copyNadvance                }
    },
    #-------------------------------------------------------------
    # Reading until we find the closing brace of an array
    arraydimension => {
	"]"        => sub { $_[0]->copyNadvance;
			    $_[0]->setState("foundType")      },
        otherwise  => sub { $_[0]->copyNadvance;              }
    },
    #-------------------------------------------------------------
    # Reading the generic part of a type. Generics can be nested.
    generic => {
        "<"        => sub { $_[0]->copyNadvance;
			    $_[0]->{ANGLELEVEL}++             },
	">"        => sub { $_[0]->copyNadvance;
	                    $_[0]->{ANGLELEVEL}--;
	                    if($_[0]->{ANGLELEVEL}==0) {
	                      $_[0]->setState("foundType")
	                    }                                 },
        otherwise  => sub { $_[0]->copyNadvance;              }
    }
});



# expects the complete field declaration, e.g. including the modifiers, the type
# and the name(s) (as a reference).
# Extracts the type modifiers and the type.
sub extractType {
  my ($declaration,$offset) = @_;
  # first we remove all type modifiers
  my $typemodifiers = extractAndCanonicalizeTypeModifiers($declaration,$offset);

  # then we extract the type of the field
  leftTrimWhiteSpace($declaration,$offset);
  my $sm = StateMachine->new($fieldTypeReader,$$declaration,$$offset);
  $sm->initialize($fieldTypeInitialState);

  my ($fieldType,$foundEndOfType) = ('',0);
  while(! $foundEndOfType) {
    $fieldType .= $sm->execute;
    $$declaration = $sm->rest();
    # the only way the type can go on is if there is an additional array
    # dimension or the beginning of a generic specifier
    if($$declaration =~ /^\s*(\[|<)/) {
       $sm->resetState;
     } else {
       $foundEndOfType = 1
     }
  }
  # remove all spaces from the type
  $fieldType =~ s/\s+//g;
  return ($typemodifiers,$fieldType)
}

# expects the complete field declaration, e.g. including the modifiers, the type
# and the name(s) (as a reference).
# Removes all modifiers from the specification, and returns them in a canonicalized
# form.
sub extractAndCanonicalizeTypeModifiers {
  my ($declaration,$offset) = @_;
  my ($visibilityIsSet,@canonicalModifiers) = (0);

  # eat all modifiers, starting from left
  leftTrimWhiteSpace($declaration,$offset);
  while($$declaration =~ s/^$fieldModifiers//o) {
  	my $modifier = $&;
  	# we are only interested in the 'static' and the visibility modifiers
  	if($modifier eq 'static') {
  	  # the canonical place of the static modifier is at the beginning
  	  unshift @canonicalModifiers, $modifier;
        } elsif($modifier =~ m/public|private|protected/) {
          # the canonical place of the visibility modifiers is at the end
          push  @canonicalModifiers, $modifier;
          $visibilityIsSet = 1;
        }
  	leftTrimWhiteSpace($declaration,$offset);
  }
  unless ($visibilityIsSet) { push @canonicalModifiers, $defaultVisibility }
  return join(' ',@canonicalModifiers)
}

# We output the field in the format of the SourceNavigator DBDump.
# The format looks like this (C++ example):
#
#  IMatrixFormula fArgument 000121.015 spreadsheet/iMatrixFormula.h;121.24 0x2 {FormulaGrid []} {} {} {}
#
# Fields are delimited by a single space.The columns have the following meanings:
#
#  ClassName AttributeName LineNo.??? Filename;LineNo.?? VisibilityCode {Type} {} {} {}
#
sub outputField {
  my ($type,$typeModifiers,$name,$offset) = @_;
  my $visibilityCode = $visibilityCodes{$typeModifiers};
  unless(defined $visibilityCode) {
    die "Unknown VisibilityCode for modifiers '$typeModifiers'\n";
  }
  foreach($type,$name) { s/\s+//g }
  printf "%s;%s;%06d.000;%s;%d.00;%s;{%s};{};{};{}\n",
         fullyQualifiedClassName(),$name,$offset,
         $currentFilename,$offset,$visibilityCode,$type;
}

# returns the current classname, fully qualified by the package and
# eventual outer classes
sub fullyQualifiedClassName {
 my $classname = $package ? "$package$packageDelim" : '';
 $classname .=  join($packageDelim,@classStack);
 return $classname;
}


# removes all whitespace from the left side of the given string,
# increasing $offset when encoutering newlines.
# We are working with references here.
sub leftTrimWhiteSpace {
  my ($textRef,$offsetRef) = @_;
  $$textRef =~ s/^\s+//;
  my $whitespace = $&;
  while($whitespace =~ m/\n/g) { $$offsetRef++ }
}

#######################################################################
#                                                                     #
#                               M A I N                               #
#                                                                     #
#######################################################################

@rootdirs = ();
@files    = ();
while (@ARGV) {
    $_ = shift @ARGV;

    if(m/^-f$/)   { push @files, shift;           next }
    if(m/^-/)     { die "Unknown command line option '$_', abort!\n" }

    # if it's not a command, it's a source directory
    push @rootdirs, File::Spec->rel2abs($_);
}

#_______________________                       ________________________
#_______________________  Begin Safety Checks  ________________________
unless(scalar @files || scalar @rootdirs){ die "No root directory specified, abort!\n\n" }
#________________________                     _________________________
#________________________  End Safety Checks  _________________________

if(scalar @files) {
  # enumerate all specified files
  foreach my $f (@files) {  extractAttributes($f)  }
}

if(scalar @rootdirs) {
  #traverse the directories
  find(\&wanted, @rootdirs);
}

