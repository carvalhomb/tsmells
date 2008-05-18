/*
Copyright (C) 2001 Chr. Clemens Lee <clemens@kclee.com>.

This file is part of JavaNCSS 
(http://www.kclee.com/clemens/java/javancss/).

JavaNCSS is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2, or (at your option) any
later version.

JavaNCSS is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with JavaNCSS; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.  */

import ccl.util.FileUtil;
import ccl.util.Util;
import java.io.File;

/**
 * 
 * @author  Chr. Clemens Lahme
 * @version $Id: JITFix.java,v 1.3 2001/03/27 15:39:17 clemens Exp clemens $
 */
public class JITFix {
    public JITFix() {
        super();
    }

    public static void main( String[] asArg_ ) {
        Util.println( "JITFIX starting..." );
        String sJavaParserFile = "src" 
               + File.separator 
               + "javancss" 
               + File.separator
               + "JavaParser.java";
 
        String sJavaParser = null;
        try {
            sJavaParser = FileUtil.readFile( sJavaParserFile );
            
        } catch( Exception excRead ) {
            Util.println( "JITFix error: could not read file 'JavaParser.java'" );

            System.exit( 1 );
        }

        int parserLength = sJavaParser.length();
        sJavaParser = Util.replace( sJavaParser, "final private int jj_ntk() {\n" +
                                                           "    if ((jj_nt=token.next) == null)\n" +
                                                           "      return (jj_ntk = (token.next=token_source.getNextToken()).kind);\n" +
                                                           "    else\n" +
                                                           "      return (jj_ntk = jj_nt.kind);\n" +
                                                           "  }",
                                    "final private int jj_ntk() {\n" +
                                    "    try {\n" +
                                    "      if ((jj_nt=token.next) == null)\n" +
                                    "        return (jj_ntk = (token.next=token_source.getNextToken()).kind);\n" +
                                    "      else\n" +
                                    "        return (jj_ntk = jj_nt.kind);\n" +
                                    "    } catch (Exception e) {\n" +
                                    "    }\n" +
                                    "    return EOF;\n" +
                                    "  }" );
        if ( parserLength == sJavaParser.length() ) {
            Util.println( "JITFix error: couldn't find method jj_ntk in JavaParser.java or it looks different as expected!" );
            Util.println( sJavaParser );

            System.exit( 1 );
        }

        // write new parser
        try {
            FileUtil.writeFile( sJavaParserFile, sJavaParser );
        } catch( Exception excWrite ) {
            Util.println( "JITFix error: could not write file 'JavaParser.java'" );

            System.exit( 1 );
        }

        System.exit( 0 );
    }
}
