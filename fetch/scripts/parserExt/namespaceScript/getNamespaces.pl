#! /usr/bin/perl -w
#
# Island parser to detect namespaces in C++ files. Their
# names and character positions are recorded. The script
# also reports the scopes of 'using namespace' statements.
# (The reported scope of the 'using namespace' statement
# extends from the end of that statement to the end of the
# enclosing block).
#
# The script writes a tab-separated table to STDOUT:
#
#   IGMP    30      76      Foo.h
#   GIMP    71      72      Foo.h
#   using_namespace	IGMP       67      70      Bar.hpp
#
# The script does some minimal preprocessing, removing code
# in #if 0 ... #endif blocks, but it can be thrown off by
# heavy abuse of the preprocessor.
#
# The invocation needs source directories and filename
# extensions. Additionally, you can request the position
# information given in character positions, not in linenumbers:
#
#   getNamespaces.pl -s <dir>  [-s <dir> ...] <ext> [<ext> ...] [-cpos]
#
# If you dont want to see the error messages about mismatched
# brackets, redirect STDERR to the neant:
#
#   getNamespaces.pl -s mysrc  h cpp cxx c hh 2> /dev/null
#
##############################################################
# Author:  Matthias Rieger and Bart Du Bois
# Created: March 8, 2007
# History: + March 8, 2007
#            print full names of nested namespaces
#          + March 15, 2007
#            fixed error when namespace opening bracket did
#            not occur on the line of the keyword 'namespace'
#          + March 20, 2007
#            - upon customer request, the root directories are
#              now removed from the filenames returned
#            - fixed problem with one-character namespace names
#            - now 'using namespace' statements are also reported
#          + April 17, 2007
#            provides line numbers by default, character
#            positions on demand
#          + June 18, 2007
#            fixed problem with incorrectly closed scopes of
#            using statements
#			+ August 21, 2007
#			  Extended script for class using directives
##############################################################
# $Id: getNamespaces.pl,v 1.2 2007/06/18 11:02:05 rieger Exp $
##############################################################

use File::Spec;
use File::Find;
use File::Basename;

#______________________                 _____________________
#______________________ Begin Constants _____________________
$delim = ','; # no filename- or identifier-characters
$namespaceDelim = '::';
#______________________                 _____________________
#______________________  End Constants  _____________________


#_______________________               _______________________
#_______________________ Begin Globals _______________________
%extensions = (); # stores the extensions of the allowed files
@rootdirs   = (); # the roots of our directory traversal
@namespaces = ();
#_______________________               _______________________
#_______________________  End Globals  _______________________


# callback for find
sub wanted {
  my $file = $_;
  return if(-d $File::Find::name);
  my ($name,$path,$suffix) = fileparse($File::Find::name,qr{\.[^.]+$});
  return unless(exists $extensions{$suffix});
  #print STDERR "Looking at File ${File::Find::name}:\n";
  my $relativeFilename = removeRootDir($File::Find::name);
  push @namespaces, map {$_.="$delim$relativeFilename"}
                         searchForNamespace($File::Find::name);
}

# parse the code from $filename and extracts the positions of namespaces
# and 'using namespace' statements
sub searchForNamespace {
  my ($filename) = @_;
  my $code = getCommentFreeCode($filename);
  #open OUT, ">$filename.cleaned";print OUT $code;close OUT;
  return unless($code);
  my (@bracketstack,@namestack,@usinglist,@nslist) = ();
  my ($charpos,$linecount,$openingNamespace) = (0,0,0);				
  
  foreach $line (split /\n/, $code) {
    $linecount++;
    
    while($line) {
      if($line =~ m/using\s+namespace\s+([[:alpha:]][\w:]*)/) {
		# the scope of the 'using namespace' statement begins
		# after the statement, not at the beginning of the last block.
		# Is that correct?
		my $beginpos = $charpos + length($`) + length($&);
		# the 'using-Marker' contains the nesting level at which we have to
		# close the scope of the using-statement
		push @usinglist, sprintf("using_namespace	$1$delim$beginpos$delim$linecount$delim%d",scalar(@bracketstack)-1);
      } elsif($line =~ m/using\s+([[:alpha:]]+::[[:alpha:]]+)+/) {
		# the scope of the 'using namespace' statement begins
		# after the statement, not at the beginning of the last block.
		# Is that correct?
		my $beginpos = $charpos + length($`) + length($&);
		# the 'using-Marker' contains the nesting level at which we have to
		# close the scope of the using-statement
		push @usinglist, sprintf("using_class	$1$delim$beginpos$delim$linecount$delim%d",scalar(@bracketstack)-1);
      } elsif($line =~ m/namespace\s+([[:alpha:]]\w*)/) {
       	push @namestack, $1;
		$openingNamespace = 1;
      } elsif($line =~ m/^[^}]*?{/) {
		my $blockOpenMarker;
		if($openingNamespace) {
		  my $beginpos = $charpos + length($`) + length($&);
		  $blockOpenMarker = join($namespaceDelim,@namestack)."$delim$beginpos$delim$linecount";
		  $openingNamespace=0;
	} else {
	  	$blockOpenMarker = "{$linecount";
	}
	push @bracketstack, $blockOpenMarker;
	#if(scalar @bracketstack == 1) { print STDERR "outer{ #line $linecount\n"; }
      } elsif($line =~ m/^[^}]*}/) {
	if(scalar @bracketstack) {
	  my $endpos = $charpos + length($`) + length($&);
	  # handle the case of a closing namespace
	  if(substr($bracketstack[-1],0,1) ne '{') {
	    my $namespacemarker = $bracketstack[-1];
	    unshift @nslist, "$namespacemarker$delim$endpos$delim$linecount";
	    pop @namestack;
	  }
	  pop @bracketstack;
	  #if(scalar @bracketstack == 0) { print STDERR "     } #line $linecount\n------\n"; }
	  # handle end-of-scope for using statements
	  push @nslist, closeUsingScopes(\@usinglist,scalar @bracketstack,$endpos,$linecount);
	} else {
	  signalUnbalancedBrackets('}',$File::Find::name,$linecount);
	}	
      } else {
	$charpos += length($line);
	$line = '';
	next;
      }
      $charpos += length($`) + length($&);
      $line = $';
    }
    # the newlines are not counted in the matches,
    # we have to add them here
    $charpos++;
  }
  if(scalar @bracketstack) {
    signalUnbalancedBrackets(join(" ",@bracketstack),$File::Find::name,$linecount);
  }
  # don't forget to close the using statements extending to the end of the file
  push @nslist, closeUsingScopes(\@usinglist,-1,$charpos,$linecount);
  return @nslist;
}

# determines which using scopes can be closed at the current nesting level.
# Removes the markers of closed using-scopes from the $usingList and returns
# thme, fully expanded with the information about where they were closed, as
# the resulting list
sub closeUsingScopes {
  my ($usingList,$nestingLevel,$endpos,$linecount) = @_;
  my @closedScopes = ();
  my @oldUsingList = @$usingList;
  @$usingList = ();
  foreach my $using (@oldUsingList) {
    # the nesting level at which the using scope has to be closed
    # is appended to the marker. Note: this can be negative!
    $using =~ m/-?\d+$/;
    if($& == $nestingLevel) {
      push @closedScopes, "$`$endpos$delim$linecount"
    } else {
      push @$usingList, $using
    }
  }
  return @closedScopes
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

# reads C++ code from a file and removes all comments and string literals
# Does not change any character positions.
# Returns undef if the file could not be read.
sub getCommentFreeCode {
  my ($filename) = @_;
  local *IN;
  undef $!;
  open IN, $filename;
  #if($!) {
  #  print STDERR  "Could not open file '$filename' to read: $!\n";
  #  return undef;
  #}
  $/ = undef;
  my $code = <IN>;
  close IN;

  # removing C/C++ comments with single regular expression from
  # http://perldoc.perl.org/perlfaq6.html#How-do-I-use-a-regular-expression-to-strip-C-style-comments-from-a-file%3f
  # We have included the C++ comment removal
  # and some group to be able to retain linebreaks from multiline
  # comments
  $code=~s{
       (      ##      Either a comment

          /\*         ##  Start of /* ... */ comment
           [^*]*\*+   ##  Non-* followed by 1-or-more *'s
          (
           [^/*][^*]*\*+
          )*          ##  0-or-more things which don't start with /
                      ##    but do end with '*'
          /           ##  End of /* ... */ comment

        |

          //[^\n]*     ## C++ style comments

       )
     |         ##     OR  various things which aren't comments:

       (
         "           ##  Start of " ... " string
         (
           \\.           ##  Escaped char
         |               ##    OR
           [^"\\]        ##  Non "\
         )*
         "           ##  End of " ... " string

       |         ##     OR

         '           ##  Start of ' ... ' string
         (
           \\.           ##  Escaped char
         |               ##    OR
           [^'\\]        ##  Non '\
         )*
         '           ##  End of ' ... ' string

       |         ##     OR

         (
          .          ##  Any other char
          [^/"'\\]*  ##  Chars which doesn't start a comment, string or escape
         )
       )
     }{defined $6 ? $6 : whiteOut($&) }gxse;
  return removePreprocessedAwayCode($code);
}


# remove code being preprocessed away by #if 0 .. #endif
# Note: There are people who do something like this:
#
#    #if 0
#     ...
#    #else
#     ...
#    #endif
#
# We also handle stuff like that:
#
#    #if 0
#
#      #ifdef DBG_SUPPORT
#      ...
#      #endif
#
#    #endif
#
sub removePreprocessedAwayCode {
  my ($code) = @_;
  my ($cleanCode,$count,@stack) = ('');
  foreach my $line (split /\n/, $code) {
    $count++;
    if($line=~/^\s*#\s*ifn?(def)?\s+(\S+)/) {
      push @stack, $2;
    } elsif($line =~ /^\s*#\s*else/) {
      # only if the value to be inverted is 0
      # do we have to do something
      if($stack[-1] eq '0') {
	pop @stack;
	push @stack, 1;
      }
    } elsif($line =~ /^\s*#\s*endif/) {
      pop @stack;
    }
    # if there is a '0' on the stack, we have to remove the text
    foreach (@stack){ if($_ eq '0'){$line = whiteOut($line);last} }
    $cleanCode .= "$line\n";
  }
  return $cleanCode;
}


# gets a piece of text and replaces every character by a space,
# except newlines, which are not changed
sub whiteOut {
  my ($text) = @_;
  $text =~ s/./ /g;
  return $text;
}

sub signalUnbalancedBrackets {
  my ($brackets,$filename,$lineno) = @_;
  print STDERR "Unbalanced bracket(s) '$brackets' in file '$filename' on line $lineno\n";
}


############################################################
#                                                          #
#                       M A I N                            #
#                                                          #
############################################################

$printLineNumbers=1;
while (@ARGV) {
    $_ = shift @ARGV;

    if(m/-s$/)    { push @rootdirs, File::Spec->rel2abs(shift);next}
    if(m/-cpos$/) { $printLineNumbers=0;                       next}

    if(m/^-/)     { die "Unknown command line option '$_', abort!\n" }

    # if it's not a command, it's an extension
    s/^\.+//;
    $extensions{".$_"}++;
}

#___________________                     ___________________
#___________________ Begin Safety Checks ___________________
unless(scalar @rootdirs)       { die "No root directory specified, abort!\n\n" }
unless(scalar keys %extensions){ die "No file-extensions specified, abort!\n\n";}
#___________________                     ___________________
#___________________  End Safety Checks  ___________________


#traverse the directories
find(\&wanted, @rootdirs);


# write tab-separated output table
foreach (@namespaces) {
  my ($key,
      $beginCharpos,$beginLineno,
      $endCharpos,$endLineno,$filename) = split /$delim/o;
  my @elems = $printLineNumbers ? ($key,$beginLineno,$endLineno,$filename)
                                : ($key,$beginCharpos,$endCharpos,$filename);
  print join("\t", @elems),"\n";
}

