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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VMeter extends Widget {
    static Tex bg = Theme.tex("vm", 0);
    static Tex fg = Theme.tex("vm", 1);
    Color cl;
    public int amount;
    private static final Map<String, Integer> levels = new HashMap<String, Integer>(3) {{
        put("Oven", 3 * 4);   // amount per unit * number of units
        put("Finery Forge", 6 * 2);
        put("Ore Smelter", (int) (3.3 * 12));
    }};
    public static final List<Kit> kits = new ArrayList<Kit>() {{
        add(new Kit("Cauldron", new ArrayList<TypeLimit>() {{
            add(new TypeLimit(new Color(71, 101, 153), 30f, "L"));
            add(new TypeLimit(new Color(255, 128, 0), 10f, "ticks"));
        }}));
        add(new Kit("Tub", new ArrayList<TypeLimit>() {{
            add(new TypeLimit(new Color(165, 117, 62), 40f, "L"));
        }}));
    }};

    public static class Kit {
        public final String windowName;
        public final List<TypeLimit> typeLimit;

        public Kit(String windowName, ArrayList<TypeLimit> typeLimit) {
            this.windowName = windowName;
            this.typeLimit = typeLimit;
        }

        public static Kit getKit(String windowName) {
            for (Kit kit : kits)
                if (kit.windowName.equals(windowName))
                    return kit;
            return null;
        }
    }

    public static class TypeLimit {
        public final Color color;
        public final double limit;
        public final String subText;

        public TypeLimit(Color color, double limit, String subText) {
            this.color = color;
            this.limit = limit;
            this.subText = subText;
        }

        public static TypeLimit getTypeLimit(Kit kit, Color color) {
            for (TypeLimit typeLimit : kit.typeLimit)
                if (typeLimit.color.equals(color))
                    return typeLimit;
            return null;
        }
    }

    @RName("vm")
    public static class $_ implements Factory {
        public Widget create(UI ui, Object[] args) {
            Color cl;
            if (args.length > 4) {
                cl = new Color((Integer) args[1],
                        (Integer) args[2],
                        (Integer) args[3],
                        (Integer) args[4]);
            } else if (args.length > 3) {
                cl = new Color((Integer) args[1],
                        (Integer) args[2],
                        (Integer) args[3]);
            } else {
                cl = (Color) args[1];
            }
            return (new VMeter((Integer) args[0], cl));
        }
    }

    public VMeter(int amount, Color cl) {
        super(bg.sz());
        this.amount = amount;
        this.cl = cl;
    }

    public void draw(GOut g) {
        g.image(bg, Coord.z);
        g.chcolor(cl);
        int hm = (sz.y - 6);
        int h = (hm * amount) / 100;
        g.image(fg, new Coord(0, 0), new Coord(0, sz.y - 3 - h), sz.add(0, h));
        g.chcolor();

        Widget p = this.parent;
        if (p instanceof Window) {
            Integer lvl = this.levels.get(((Window) p).origcap);
            if (lvl != null) {
                g.chcolor(Color.WHITE);
                int y = sz.y - 3 - (hm * lvl) / 100;
                g.line(new Coord(3, y), new Coord(sz.x - 3, y), 1);
                g.chcolor();
            }
        }
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "set") {
            amount = (Integer) args[0];
            if (args.length > 1)
                cl = (Color) args[1];
        } else if (msg == "col") {
            cl = (Color) args[0];
        } else {
            super.uimsg(msg, args);
        }
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
        Widget p = this.parent;
        if (p instanceof Window) {
            if (((Window) p).cap.text.equals("Oven")) {
                if (ui.modctrl) {
                    return RichText.render("$b{$col[255,223,5]{" + amount + "/100 units}}" + ".\n13 units to cook.", -1).tex();
                } else {
                    return RichText.render("$b{$col[255,223,5]{" + amount + "/100 units}}", -1).tex();
                }
            } else if (((Window) p).cap.text.equals("Ore Smelter")) {
                if (ui.modctrl) {
                    return RichText.render("$b{$col[255,223,5]{" + amount + "/100 units.}}" + "\n40 units to smelt.\n30 units to smelt well mined.", -1).tex();
                } else {
                    return RichText.render("$b{$col[255,223,5]{" + amount + "/100 units}}", -1).tex();
                }
            } else if (((Window) p).cap.text.equals("Cauldron")) {
                if (cl.equals(new Color(71, 101, 153))) {
                    return RichText.render("$b{$col[255,223,5]{" + 30f * amount / 100 + "/30L (" + amount + "%)}}", -1).tex();
                } else if (cl.equals(new Color(255, 128, 0))) {
                    if (ui.modctrl) {
                        return RichText.render("$b{$col[255,223,5]{" + 10f * amount / 100 + "/10 ticks (" + amount + "%)}}" +
                                "\n1 tick = 4 minutes 50 seconds\n1 branch = 1 tick\nCoal, Black coal = 2 ticks\nBlock of Wood = 5 ticks\nTarsticks = 20 ticks", -1).tex();
                    } else {
                        return RichText.render("$b{$col[255,223,5]{" + 10f * amount / 100 + "/10 ticks (" + amount + "%)}}", -1).tex();
                    }
                }
            } else if (((Window) p).cap.text.equals("Tub")) {
                if (!ui.modctrl) {
                    return RichText.render("$b{$col[255,223,5]{" + 40f * amount / 100 + "/40L (" + amount + "%)}}", -1).tex();
                }
            }
        }
        return super.tooltip;//RichText.render("$b{$col[255,223,5]{" + amount +"/100 units.}}", -1).tex();

    }
}
