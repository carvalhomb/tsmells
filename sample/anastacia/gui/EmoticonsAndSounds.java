/**   	Anastacia is a Java ICQ/MSN/Yahoo Instant Messenger
 *   	Copyright (C) 2002,2003 	Benny Van Aerschot, Bart Van Rompaey
 * 	Made as a project in 3th year computer science at the university of Antwerp (UA)
 *
 * 	This file is part of Anastacia.
 *
 *    	Anastacia is free software; you can redistribute it and/or modify
 *    	it under the terms of the GNU General Public License as published by
 *    	the Free Software Foundation; either version 2 of the License, or
 *    	(at your option) any later version.
 *
 *    	Anastacia is distributed in the hope that it will be useful,
 *    	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   	GNU General Public License for more details.
 *
 *    	You should have received a copy of the GNU General Public License
 *    	along with Anastacia; if not, write to the Free Software
 *    	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * 	Contact authors:
 * 		Benny Van Aerschot - bennyva@pi.be
 * 		Bart Van Rompaey - bart@perfectpc.be
 */


package gui;

import java.util.HashMap;
import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * @author benny
 */
public class EmoticonsAndSounds {
	private HashMap fEmoticonMap = null;
	private HashMap fSoundMap = null;
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public EmoticonsAndSounds() {
		fEmoticonMap = new HashMap();
		fSoundMap = new HashMap();
		
		String ep = ".." + File.separator + "multimedia" + File.separator + "emoticons" + File.separator;
		String sp = ".." + File.separator + "multimedia" + File.separator + "sound" + File.separator;
		
		// MSN Messenger emoticons
		fEmoticonMap.put("(Y)", ep + "ok.gif");			fSoundMap.put("(Y)", sp + "yeah.wav");
		fEmoticonMap.put("(N)", ep + "nono.gif");		fSoundMap.put("(N)", null);
		fEmoticonMap.put("(D)", ep + "martini.gif");		fSoundMap.put("(D)", sp + "martini.wav");
		fEmoticonMap.put("(X)", ep + "girl.gif");			fSoundMap.put("(X)", null);
		fEmoticonMap.put("(Z)",ep + "boy.gif"); 			fSoundMap.put("(Z)", null);
		fEmoticonMap.put(":-[", ep + "bat.gif");			fSoundMap.put(":-[", null);
		fEmoticonMap.put(":[", ep + "bat2.gif");			fSoundMap.put(":[", sp + "dracula.wav");
		fEmoticonMap.put("(})", ep + "girl_hug.gif");		fSoundMap.put("(})", null);
		fEmoticonMap.put("({)", ep + "dude_hug.gif");	fSoundMap.put("({)", null);
		fEmoticonMap.put(":-)", ep + "laugh.gif");		fSoundMap.put(":-)", null);
		fEmoticonMap.put(":)", ep + "happy.gif");		fSoundMap.put(":)", null);
		fEmoticonMap.put(":D", ep + "supergrin.gif");	fSoundMap.put(":D", sp + "haha1.wav");
		fEmoticonMap.put(":-D", ep + "supergrin2.gif");fSoundMap.put(":-D", sp + "haha2.wav");
		fEmoticonMap.put(":O", ep + "shockedohno.gif");fSoundMap.put(":O", sp + "ohno.wav");
		fEmoticonMap.put(":-O", ep + "omg.gif");		fSoundMap.put(":-O", sp + "ohno.wav");
		fEmoticonMap.put(":P", ep + "eviltongue.gif");	fSoundMap.put(":P", null);
		fEmoticonMap.put(":-P", ep + "tongue.gif"); fSoundMap.put(":-P", sp + "idiotsong.wav");
		fEmoticonMap.put(";)", ep + "wink.gif");			fSoundMap.put(";)", null);
		fEmoticonMap.put(";-)", ep + "winkhmm.gif");	fSoundMap.put(";-)", null);
		fEmoticonMap.put(":(", ep + "cry.gif");				fSoundMap.put(":(", null);
		fEmoticonMap.put(":-(", ep + "cry2.gif"); 			fSoundMap.put(":-(", sp + "cry.wav");
		fEmoticonMap.put(":S", ep + "confused1.gif"); fSoundMap.put(":S", null);
		fEmoticonMap.put(":-S", ep + "confused2.gif");fSoundMap.put(":-S", null);
		fEmoticonMap.put(":|", ep + "whatsthat.gif");	fSoundMap.put(":|", null);
		fEmoticonMap.put(":-|", ep + "huh.gif");			fSoundMap.put(":-|", null);
		fEmoticonMap.put(":'(", ep + "crying.gif");		fSoundMap.put(":'(", null);
		fEmoticonMap.put(":'-(", ep + "crying2.gif");		fSoundMap.put(":'-(", null);
		fEmoticonMap.put(":-$", ep + "redface.gif");		fSoundMap.put(":-$", null);
		fEmoticonMap.put("(H)", ep + "hot.gif");			fSoundMap.put("(H)", null);
		fEmoticonMap.put(":@", ep + "mad.gif");		fSoundMap.put(":@", sp + "ohdammit.wav");
		fEmoticonMap.put(":-@", ep + "mad2.gif");	fSoundMap.put(":-@", null);
		fEmoticonMap.put("(6)", ep + "devil.gif");			fSoundMap.put("(6)", null);
		fEmoticonMap.put("(L)", ep + "love.gif");		fSoundMap.put("(L)", null);
		fEmoticonMap.put("(&)", ep + "dog.gif"); fSoundMap.put("(&)", sp + "dogbark.wav");	
		fEmoticonMap.put("(U)", ep + "broken_heart.gif"); fSoundMap.put("(U)", null);
		fEmoticonMap.put("(P)", ep + "camera.gif"); fSoundMap.put("(P)", sp + "cameraclick.wav");
		fEmoticonMap.put("(@)", ep + "cat.gif"); fSoundMap.put("(@)", sp + "catmeow.wav");
		fEmoticonMap.put("(O)", ep + "clock.gif"); fSoundMap.put("(O)", sp + "clock.wav");
		fEmoticonMap.put("(C)", ep + "coffee.gif"); fSoundMap.put("(C)", sp + "hacking.wav");
		fEmoticonMap.put("(FU)", ep + "fu.gif"); fSoundMap.put("(FU)", sp + "stfu.wav");
		fEmoticonMap.put("(PARTY)", ep + "partytime.gif" ); fSoundMap.put("(PARTY)", sp + "partytime.wav");	
		fEmoticonMap.put("(CLOWN)", ep + "clown.gif"); fSoundMap.put("(CLOWN)", null);		
		fEmoticonMap.put("(BORED)", ep + "bored.gif"); fSoundMap.put("(BORED)", null);	
		fEmoticonMap.put("(E)", ep + "envelope.gif"); fSoundMap.put("(E)", sp + "mail.wav");	
		fEmoticonMap.put("(K)", ep + "kissa.gif"); fSoundMap.put("(K)", sp + "kiss.wav");
		fEmoticonMap.put("(LMAO)", ep + "lmaa.gif"); fSoundMap.put("(LMAO)", sp + "butt.wav");
		fEmoticonMap.put("(S)", ep + "moon.gif"); fSoundMap.put("(S)", null);
		fEmoticonMap.put("(8)", ep + "musical_note.gif"); fSoundMap.put("(8)", null);		
		fEmoticonMap.put("(OMG)", ep + "omg.gif"); fSoundMap.put("(OMG)", sp + "omg.wav");
		fEmoticonMap.put("(T)", ep + "phone.gif"); fSoundMap.put("(T)", sp + "phone.wav");
		fEmoticonMap.put("(R)", ep + "rainbow.gif"); fSoundMap.put("(R)", null);
		fEmoticonMap.put("(F)", ep + "rose.gif"); fSoundMap.put("(F)", null);
		fEmoticonMap.put("(UHUH)", ep + "sniffNo.gif"); fSoundMap.put("(UHUH)", null);
		fEmoticonMap.put("(#)", ep + "sun.gif"); fSoundMap.put("(#)", null);
		fEmoticonMap.put("(*)", ep + "star.gif"); fSoundMap.put("(*)", null);
		fEmoticonMap.put("(W)", ep + "wilted_rose.gif"); fSoundMap.put("(W)", null);
		fEmoticonMap.put("(ROCK)", ep + "rock.gif"); fSoundMap.put("(ROCK)", null);
		fEmoticonMap.put("(LOVE)", ep + "love2.gif"); fSoundMap.put("(LOVE)", null);
		fEmoticonMap.put("(KNUDDEL)", ep + "knuddel.gif"); fSoundMap.put("(KNUDDEL)", null);
		fEmoticonMap.put("(LOVEYA)", ep + "loveya.gif"); fSoundMap.put("(LOVEYA)", null);
		fEmoticonMap.put("(BOW)", ep + "bow.gif"); fSoundMap.put("(BOW)", null);
		fEmoticonMap.put("(FRUSTRY)", ep + "frusty.gif"); fSoundMap.put("FRUSTRY)", null);
		fEmoticonMap.put("(ICOOL)", ep + "icon_cool.gif"); fSoundMap.put("(ICOOL)", null);
		fEmoticonMap.put("(LOVE2)", ep + "liefde.gif"); fSoundMap.put("(LOVE2)", null);
		fEmoticonMap.put("(KISSES)", ep + "remykiss.gif"); fSoundMap.put("(KISSES)", null);
		fEmoticonMap.put("(2KISS)", ep + "remybussi.gif"); fSoundMap.put("(2KISS)", null);
	}
	
	/**
	 * Method getEmoticon.
	 * @param s
	 * @return String
	 */
	public String getEmoticon(String s) {
		return (String)fEmoticonMap.get(s.toUpperCase()); 
	}
	
	/**
	 * Method getSound.
	 * @param s
	 * @return String
	 */
	public String getSound(String s) {
		return (String)fSoundMap.get(s.toUpperCase());
	}
	
	/**
	 * Method getAllEmoticons.
	 * @return Collection
	 */
	public Collection getAllEmoticons() {
		return fEmoticonMap.values(); 
	}
	
	/**
	 * Method getAllEmoticonKeys.
	 * @return Set
	 */
	public Set getAllEmoticonKeys() {
		return fEmoticonMap.keySet();
	}
}
