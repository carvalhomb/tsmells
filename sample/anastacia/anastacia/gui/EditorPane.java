/**     Anastacia is a Java ICQ/MSN/Yahoo Instant Messenger
 *      Copyright (C) 2002,2003         Benny Van Aerschot, Bart Van Rompaey
 *      Made as a project in 3th year computer science at the university of Antwerp (UA)
 *
 *      This file is part of Anastacia.
 *
 *      Anastacia is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *
 *      Anastacia is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with Anastacia; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *      Contact authors:
 *              Benny Van Aerschot - bennyva@pi.be
 *              Bart Van Rompaey - bart@perfectpc.be
 */


package gui;

import javax.swing.JEditorPane;
import java.net.URL;
import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author benny
 */
public class EditorPane extends JEditorPane {
        private RandomAccessFile fFile = null;
        private File f = null;
        private URL fURL = null;
        private final String[] months = { "Jan", "Feb", "Mar", "Apr", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        private boolean writePermission = false;
        private EmoticonsAndSounds fEmoticonsAndSounds = null;
        private Calendar fCalendar = null;
        private String timezone = null;
        
        /**
         * @param contact
         */
        public EditorPane(String contact) {             
                try {
                        timezone = System.getProperties().getProperty("user.timezone");
                        fCalendar =  new GregorianCalendar(TimeZone.getTimeZone(timezone));

                        f = new File(contact + "_" + fCalendar.get(Calendar.DAY_OF_MONTH) 
                                + months[Calendar.MONTH] + fCalendar.get(Calendar.YEAR) + "_" + fCalendar.get(Calendar.HOUR_OF_DAY)
                                + "h" + fCalendar.get(Calendar.MINUTE) + ".html");
                        
                        boolean succes =  f.createNewFile();
                                                
                        if (!succes) {
                                f.delete();
                                f.createNewFile();
                        }
                        
                        writePermission = f.canWrite();
                        fFile = new RandomAccessFile(f, "rw");
                        fFile.setLength(0);
                        if (writePermission) {
                               // fFile.writeBytes("<html>\n</html>");
                               fFile.writeBytes("<html><body>\n</body></html>");
                        }
                        fURL = f.toURL();
                        this.setPage(fURL);
                        this.setEditable(false);
                        this.setDoubleBuffered(true);
                        this.setContentType("text/html");
                        fEmoticonsAndSounds = new EmoticonsAndSounds();
                } catch (Exception e) {
                System.err.println(e.getMessage()); e.printStackTrace();
                }
        }
        
        
        /**
         * @param soundToPlay
         */
        private void playSound(String soundToPlay) {
                if (soundToPlay != null) {
                
                final String sound = fEmoticonsAndSounds.getSound(soundToPlay);
                
                        if (sound != null) {                            
                                (new Thread() {
                                        public void run() {
                                                SimpleAudioPlayer ap = new SimpleAudioPlayer();
                                                ap.play(sound);
                                        }
                                }).start();
                        }
                }
        }
        
        /**
         * @param text
         * @return
         */
        private String insertEmoticons(String text) {
                StringTokenizer st = new StringTokenizer(text);
                Vector tokens = new Vector();
                String soundToPlay = null;
                
                while (st.hasMoreTokens()) {
                tokens.add(st.nextToken());
                }
                
                for (int i = 0; i < tokens.size(); ++i) {
                        String s = (String)tokens.elementAt(i);

                        String emoticon = fEmoticonsAndSounds.getEmoticon(s);

                        if (emoticon != null) {
                                emoticon = "<img src=\"" + emoticon + "\">";
                                soundToPlay = s;
                                tokens.removeElementAt(i);
                                tokens.insertElementAt(emoticon, i);
                        }
                }
                
                StringBuffer sBuf = new StringBuffer();
                for (int i = 0; i < tokens.size(); ++i) {
                        sBuf.append((String)tokens.elementAt(i));
                        sBuf.append(" ");
                }
                
                this.playSound(soundToPlay);
                return sBuf.toString();
        }
        
        /**
         * @param editor
         * @param html
         * @param location
         * @throws IOException
         */
        private void insertHTML(JEditorPane editor, String html, int location) throws IOException {
                try {//assumes editor is already set to "text/html" type
                        HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
                        Document doc = editor.getDocument();
                        StringReader reader = new StringReader(html);
                        kit.read(reader, doc, location);
                } catch (BadLocationException e) {
                        
                }
        }
        
        /**
         * @param who
         * @param text
         */
        public void append(String who, String text) {
                String color = null;
                
                try {
                        Document doc = getDocument();

						text = text.replaceAll("\n", "\n<br>");
                        text = this.insertEmoticons(text);
                       // String s = "</html>";
                        String s = "</body></html>";
                        fFile.setLength(fFile.length() - s.getBytes().length);

                        fCalendar =  new GregorianCalendar(TimeZone.getTimeZone(timezone));
                        int hour = fCalendar.get(Calendar.HOUR_OF_DAY);
                        int min = fCalendar.get(Calendar.MINUTE);
                        
                        StringBuffer sBuf = new StringBuffer();
                        if (who.equals("You say")) {
                                color = "navy";
                        } else {
                                color = "maroon";
                        }
                        sBuf.append("<font size=\"-1\" color=" + color + "><b>[");
                        
                        if (hour < 10) {
                                sBuf.append("0");
                        }
                        sBuf.append(hour);
                        sBuf.append(":");
                        if (min < 10) {
                                sBuf.append("0");
                        }
                        sBuf.append(min);

                        sBuf.append("] ");
                        sBuf.append(this.insertEmoticons(who));
                        sBuf.append(":</b></font><br>\n");
                        sBuf.append("<font color=" + color + ">" + text);
                        sBuf.append("</font><br>\n");
                       	sBuf.append("</body></html>");
                       // sBuf.append("</html>");
                        text = sBuf.toString();
                        if (writePermission) fFile.writeBytes(text);
                        this.insertHTML(this, text, doc.getLength());
                        setCaretPosition(doc.getLength());

                } catch (IOException e) {
                        System.out.println(e.getMessage());
                }
        }
        
        public void close() {

        }

}