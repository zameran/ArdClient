/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import modification.configuration;

import java.awt.*;
import java.awt.event.KeyEvent;

public class LoginScreen extends Widget {
	Login cur;
	Text error;
	IButton btn;
	Button optbtn;
	TextEntry user;
	OptWnd opts;
	static Text.Foundry textf, textfs, special;
	static Tex bg = configuration.imageToTex("modification/yeah.png", 1920, 1000, Resource.loadtex("gfx/loginscr"));      //static Tex bg = Resource.loadtex("gfx/loginscr");
	Text progress = null;

	static {
		textf = new Text.Foundry(Text.sans, 16).aa(true);
		textfs = new Text.Foundry(Text.sans, 14).aa(true);
		special = new Text.Foundry(Text.latin, 14).aa(true);
	}

	public LoginScreen() {
		super(bg.sz());
		setfocustab(true);
		add(new Img(bg), Coord.z);
		optbtn = adda(new Button(100, "Options"), sz.x - 10, 10, 1, 0);
		add(new LoginList(200, 29), new Coord(20, 20));
	}

	private static abstract class Login extends Widget {
		abstract Object[] data();

		abstract boolean enter();
	}

	private class Pwbox extends Login {
		TextEntry /*user,*/ pass;
		CheckBox savepass;

		private Pwbox(String username, boolean save) {
			setfocustab(true);
			add(new Label("User name", textf), Coord.z);
			add(user = new TextEntry(150, username), new Coord(0, 20));
			add(new Label("Password", textf), new Coord(0, 50));
			add(pass = new TextEntry(150, ""), new Coord(0, 70));
			pass.pw = true;
//			add(savepass = new CheckBox("Remember me", true), new Coord(0, 100));
//			savepass.a = save;
			if (user.text.equals(""))
				setfocus(user);
			else
				setfocus(pass);
			resize(new Coord(150, 150));
			LoginScreen.this.add(this, new Coord(LoginScreen.this.sz.x / 2 - 75, LoginScreen.this.sz.y / 2 + 10)); //LoginScreen.this.add(this, new Coord(345, 310));
		}

		public void wdgmsg(Widget sender, String name, Object... args) {
		}

		Object[] data() {
			return (new Object[]{new AuthClient.NativeCred(user.text, pass.text), false/*savepass.a*/});
		}

		boolean enter() {
			if (user.text.equals("")) {
				setfocus(user);
				return (false);
			} else if (pass.text.equals("")) {
				setfocus(pass);
				return (false);
			} else {
				return (true);
			}
		}

		public boolean globtype(char k, KeyEvent ev) {
			if ((k == 'r') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
//				savepass.set(!savepass.a);
				return (true);
			}
			return (false);
		}
	}

	private class Tokenbox extends Login {
		Text label;
		Button btn;

		private Tokenbox(String username) {
			label = textfs.render("Identity is saved for " + username, Color.WHITE);
			add(btn = new Button(100, "Forget me"), new Coord(75, 30));
			resize(new Coord(250, 100));
			LoginScreen.this.add(this, new Coord(LoginScreen.this.sz.x / 2 - 125, LoginScreen.this.sz.y / 2 + 30)); //LoginScreen.this.add(this, new Coord(295, 330));
		}

		Object[] data() {
			return (new Object[0]);
		}

		boolean enter() {
			return (true);
		}

		public void wdgmsg(Widget sender, String name, Object... args) {
			if (sender == btn) {
				LoginScreen.this.wdgmsg("forget");
				return;
			}
			super.wdgmsg(sender, name, args);
		}

		public void draw(GOut g) {
			g.image(label.tex(), new Coord((sz.x / 2) - (label.sz().x / 2), 0));
			super.draw(g);
		}

		public boolean globtype(char k, KeyEvent ev) {
			if ((k == 'f') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
				LoginScreen.this.wdgmsg("forget");
				return (true);
			}
			return (false);
		}
	}

	public class LoginList extends Listbox<LoginData> {
		private final Tex xicon = Text.render("\u2716", Color.RED, special).tex();
		private int hover = -1;
		private final static int ITEM_HEIGHT = 26;
		private Coord lastMouseDown = Coord.z;

		public LoginList(int w, int h) {
			super(w, h, ITEM_HEIGHT);
		}

		@Override
		protected void drawbg(GOut g) {
			g.chcolor(125, 125, 125, 125);
			g.frect(Coord.z, sz);
			g.chcolor();
		}

		@Override
		protected void drawsel(GOut g) {
			g.chcolor(125, 125, 125, 255);
			g.frect(Coord.z, sz);
			g.chcolor();
		}

		@Override
		protected LoginData listitem(int i) {
			synchronized (configuration.logins) {
				return configuration.logins.get(i);
			}
		}

		@Override
		protected int listitems() {
			synchronized (configuration.logins) {
				return configuration.logins.size();
			}
		}

		@Override
		public void mousemove(Coord c) {
			setHoverItem(c);
			super.mousemove(c);
		}

		@Override
		public boolean mousewheel(Coord c, int amount) {
			setHoverItem(c);
			return super.mousewheel(c, amount);
		}

		private void setHoverItem(Coord c) {
			if (c.x > 0 && c.x < sz.x && c.y > 0 && c.y < listitems() * ITEM_HEIGHT)
				hover = c.y / ITEM_HEIGHT + sb.val;
			else
				hover = -1;
		}

		@Override
		protected void drawitem(GOut g, LoginData item, int i) {
			if (hover == i) {
				g.chcolor(64, 64, 64, 96);
				g.frect(Coord.z, g.br);
				g.chcolor();
			}
			Tex tex = Text.render(item.name, Color.WHITE, textfs).tex();
			int y = ITEM_HEIGHT / 2 - tex.sz().y / 2;
			g.image(tex, new Coord(5, y));
			g.image(xicon, new Coord(sz.x - 25, y));
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			lastMouseDown = c;
			return super.mousedown(c, button);
		}

		@Override
		protected void itemclick(LoginData itm, int button) {
			if (button == 1) {
				if (lastMouseDown.x >= sz.x - 25 && lastMouseDown.x <= sz.x - 25 + 20) {
					synchronized (configuration.logins) {
						configuration.logins.remove(itm);
						configuration.saveLogins();
					}
				} else if (c.x < sz.x - 35) {
					parent.wdgmsg("forget");
					parent.wdgmsg("login", new Object[]{new AuthClient.NativeCred(itm.name, itm.pass), false});
					Context.accname = itm.name;
				}
				super.itemclick(itm, button);
			}
		}
	}

	private void mklogin() {
		synchronized (ui) {
			adda(btn = new IButton("gfx/hud/buttons/login", "u", "d", "o") {
				protected void depress() {
					Audio.play(Button.lbtdown.stream());
				}

				protected void unpress() {
					Audio.play(Button.lbtup.stream());
				}
			}, LoginScreen.this.sz.x / 2, LoginScreen.this.sz.y / 2 + btn.sz.y * 3, 0.5, 0.5); //c(352 476)+67 34 sz(135 69) => 800 600 || def 419 510
			progress(null);
		}
	}

	private void error(String error) {
		synchronized (ui) {
			if (this.error != null)
				this.error = null;
			if (error != null)
				this.error = textf.render(error, Color.RED);
		}
	}

	private void progress(String p) {
		synchronized (ui) {
			if (progress != null)
				progress = null;
			if (p != null)
				progress = textf.render(p, Color.WHITE);
		}
	}

	private void clear() {
		if (cur != null) {
			ui.destroy(cur);
			cur = null;
			ui.destroy(btn);
			btn = null;
		}
		progress(null);
	}

	public void wdgmsg(Widget sender, String msg, Object... args) {
		if (sender == btn) {
			if (cur.enter()) {
				Context.accname = user.text;
				super.wdgmsg("login", cur.data());
			}
			return;
		} else if (sender == optbtn) {
			if (opts == null) {
				opts = adda(new OptWnd(false) {
					public void hide() {
						/* XXX */
						reqdestroy();
					}
				}, sz.div(2), 0.5, 0.5);
			} else {
				opts.reqdestroy();
				opts = null;
			}
			return;
		} else if (sender == opts) {
			opts.reqdestroy();
			opts = null;
		}
		super.wdgmsg(sender, msg, args);
	}

	public void cdestroy(Widget ch) {
		if (ch == opts) {
			opts = null;
		}
	}

	public void uimsg(String msg, Object... args) {
		synchronized (ui) {
			if (msg == "passwd") {
				clear();
				cur = new Pwbox((String) args[0], (Boolean) args[1]);
				mklogin();
			} else if (msg == "token") {
				clear();
				cur = new Tokenbox((String) args[0]);
				mklogin();
			} else if (msg == "error") {
				error((String) args[0]);
			} else if (msg == "prg") {
				error(null);
				clear();
				progress((String) args[0]);
			}
		}
	}
	
	public void presize() {
		c = parent.sz.div(2).sub(sz.div(2));
	}

	protected void added() {
		presize();
		parent.setfocus(this);
	}

	public void draw(GOut g) {
		super.draw(g);
		if (error != null)
			g.image(error.tex(), new Coord(LoginScreen.this.sz.x / 2 - (error.sz().x / 2), LoginScreen.this.sz.y / 2 + 150));
		if (progress != null)
			g.image(progress.tex(), new Coord(LoginScreen.this.sz.x / 2 - (progress.sz().x / 2), LoginScreen.this.sz.y / 2 + 50));
	}

	public boolean keydown(KeyEvent ev) {
		if (ev.getKeyChar() == 10) {
			if ((cur != null) && cur.enter()) {
				Context.accname = user.text;
				wdgmsg("login", cur.data());
			}
			return (true);
		}
		return (super.keydown(ev));
	}
}
