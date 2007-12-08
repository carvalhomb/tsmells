#!/usr/bin/perl
#
# This is free software; you can redistribute it and/or modify it 
# under the terms of the GNU General Public License as published by the 
# Free Software Foundation; either version 2 of the License, or (at your 
# option) any later version.
#
# Distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more 
# details.
#
# You should have received a copy of the GNU General Public License along 
# with outputtest; if not, write to the Free Software Foundation, Inc., 
# 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA 
#

##
# Shamelesly copied from 'getNamespace.pl' @ $FETCH/scripts/parserExt
##

# reads code from stdin and removes all comments and string literals
# Does not change any character positions.
sub getCommentFreeCode {

  $/ = undef;
  my $code = <STDIN>;

  # removing comments with single regular expression from
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
  return $code;
}


# gets a piece of text and replaces every character by a space,
# except newlines, which are not changed
sub whiteOut {
  my ($text) = @_;
  $text =~ s/./ /g;
  return $text;
}

print getCommentFreeCode()
