#!/usr/bin/perl -w
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
# Copyright 2007 Matthias Rieger <matthias.rieger@gmail.com>

##
# Extracts all the preprocessor directives from a C/C++ file.
# Expects the names of the file(s) on the command line, e.g.
#
#   preprocDirectives.pl file.cpp [file.c ... ]
#
# The script performs continuation line collation and comment
# removal.
# The resulting output looks like this:
#
#   samples/test.c:6:#define M(X, Y)  X < Y  
#   samples/test.c:7:#define habberdasher printf(something)
#   samples/test.c:14:#ifdef M(2,3)
#   samples/test.c:16:#endif 
#
# Printing the name of the file can be switched of with the 
# '-nf' option.
#
# To use this on a bunch of files, use this command line:
#
#   find . \( -name "*.h" -o -name "*.hh" -o -name "*.hpp" -o -name "*.c" -o -name "*.cc" -o -name "*.cpp" \) | xargs preprocDirectives.pl
##

##############################################################
# Author:  Matthias Rieger
# Created: June 7, 2007
# History: + June 15, 2007
#            - fixing an odd problem where we had a '\r' terminated 
#              line among regular '\n' terminated ones
#            - reducing whitespace in a macro definition
#            - fixed off-by-one error in the counting of linenumbers
#          + June 18, 2007
#            - fixed linenumber counting, which was botched by 
#              the handling of '\r' terminated lines (PerlUnit!)
#
##############################################################


#_______________________               _______________________
#_______________________ Begin Globals _______________________
# stores all lines of the source file. Each line is stored at
# its index, which means that some indices can be empty after
# the collation step
@lines = ();
# how many lines does the sourcefile contain
$maxlines = 0;
# global variable of the comment removal state machine
$state   = "";  # the state of the state machine
$oldline = "";  # the line we're reading
$c       = "";  # the character of the line we're looking at
$newline = "";  # the line to which we copy cleaned code
#_______________________               _______________________
#_______________________  End Globals  _______________________

#
# The comment removal state machine
#
my ($STATE_TABLE) = {
    incode    => {
	"/"       => sub {  $c=&nextchar;
			    if   ($c eq "/") { &nextline          }
			    elsif($c eq "*") { appendchar(" ");
					       &advance;
					       $state="incomment" }
			    else             { &copyNadvance      }},
        "\""      =>  sub { &copyNadvance; $state="instring" },
        otherwise =>  sub { &copyNadvance                    }
    },
    #-------------------------------------------------------------
    incode    => {
	"<"       => sub {   &copyNadvance; $state="ininclude" },
	otherwise =>  sub { &copyNadvance   }
    },
    #-------------------------------------------------------------
    ininclude    => {
	">"       => sub {  $state="incode" },
	otherwise =>  sub { &copyNadvance   }
    },
    #-------------------------------------------------------------
    instring  => {
	"\\"       => sub { &copyNadvance; &copyNadvance;    },
	"\""       => sub { &copyNadvance; $state="incode"   },
        otherwise  => sub { &copyNadvance                    }
    },
    #-------------------------------------------------------------
    incomment => {
        "*"        => sub { $c=&nextchar;
		 	    if($c eq "/") { &advance; 
			    		    $state="incode" }
			    &advance;                        },
        otherwise  => sub { &advance;                        }
    }
};

sub nextchar     { return substr($oldline,$col+1,1)  }
sub appendchar   { $newline .= $_[0]                 }
sub copyNadvance { appendchar($c); &advance          }
sub advance      { if($col < length $oldline) {
                     $col++;
		     $c=substr($oldline,$col,1);
		   } else {
		     &nextline
		   }
		 }
sub nextline     { if(0<=$row) {
		     $lines[$row] = ($newline=~m/^\s*$/) ? undef 
		                                         : $newline;
		   }
		   do { $row++; }
		     while(! defined $lines[$row] && $row<$maxlines);
		   if($row < $maxlines) {
		     ($oldline,$newline,$col)=($lines[$row],"",-1);
		     &advance;
		   }
	         }

# removes all C/C++ comments
sub removeComments {
  ($state,$row)=("incode",-1);
  &nextline;
  while($row < $maxlines) {
    my $stateDispatcher = $STATE_TABLE->{$state};
    my $actionCode = $stateDispatcher->{$c};
    unless(defined $actionCode) {
      $actionCode = $stateDispatcher->{"otherwise"};
    }
    &$actionCode;
  }
}


# We mimic the preprocessor here. These are the step he follows,
# according to Section 1.2 'Initial processing' of the C Preprocessor
# manual (http://gcc.gnu.org/onlinedocs/cpp/index.html)
#
#   1. The input file is read into memory and broken into lines.
#   2. If trigraphs are enabled, they are replaced by their
#      corresponding single characters.
#   3. Continued lines are merged into one long line.
#   4. All comments are replaced with single spaces.
#
# We do not do step 2.
#
sub initialProcessing {
  my ($filename) = @_;
  local *IN;
  open IN, $filename or die "Could not open file '$filename': $!\n";
  @lines = ();
  
  # Initial Processing Steps 1 and 3  (Step 2 is ignored)
  my ($idx,$lastIdx) = (-1,-1);
  while(<IN>) {
    chomp;
    # Ugly hack: BVR has code where among UNIX lines we suddenly have 
    # a line terminated by '\r'. We have to do an additional split here.
    # Note that thanks to the chomp above, the action will be idempotent
    # if we run under DOS.
    # Note: We cannot just use 'split/\r/', because this returns the empty
    #       set if applied to an empty string.
    foreach ((m/\r/) ? split/\r/ : $_) {
      $idx++;
      if(defined $lines[$lastIdx]    &&
         $lines[$lastIdx] =~ s/\\$//    ) 
      {
        $lines[$lastIdx] .= $_;
      } else {
        $lines[$idx] = (m/^\s*$/) ? undef : $_;
        $lastIdx = $idx;
      }
    }
  }
  $maxlines=$.;

  #$i=-1;
  #foreach my $line (@lines) { $i++; printf "$i:%s\n",$line ? $line : ''; }

  # Initial processing Step 4
  removeComments();
}

##############################################################
#                                                            #
#                          M A I N                           #
#                                                            #
##############################################################

($printfilenames,@files) = (1);
while (@ARGV) {
    $_ = shift @ARGV;
    if(m/^-nf/)  { $printfilenames = 0; next }
    unless(m/^-/) { push @files, $_;    next }
    
    die "Unknown option '$_', abort\n"
}

#_____________________                     ___________________
#_____________________ Begin Safety Checks ___________________
unless(@files) { die "No input file specified, abort\n" }
#_____________________                     ___________________
#_____________________  End Safety Checks  ___________________



foreach $filename (@files) {
  initialProcessing($filename);
  
  # print only preprocessor directives
  $i=0;
  foreach my $line (@lines) {
    $i++;
    next unless(defined $line);
    next unless($line =~ s/^\s*#/#/);
    # reduce whitespace to a single space
    $line =~ s/\s+/ /g;
    if($printfilenames) { print "$filename:" }
    print "$i:$line\n" 
  }
}
