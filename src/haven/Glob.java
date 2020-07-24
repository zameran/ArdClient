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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;

import haven.render.Pipe;
import haven.render.Render;
import haven.render.State;
import haven.render.sl.Expression;
import haven.render.sl.ShaderMacro;
import haven.render.sl.Type;
import haven.render.sl.Uniform;
import haven.sloth.script.pathfinding.GobHitmap;

public class Glob {
    //TODO: Glob should honestly make the ui, not have the UI attach onto it.
    public WeakReference<UI> ui;
    public final GobHitmap gobhitmap;
    public static final double SERVER_TIME_RATIO = 3.29d;
    public double serverEpoch, localEpoch = Utils.rtime();
    //public final GobHitmap gobhitmap;
    public final OCache oc = new OCache(this);
    public final MCache map;
    public final Session sess;
    public final Loader loader = new Loader();
    public double time, epoch = Utils.rtime();
    public Astronomy ast;
    public Party party;
    public Map<String, CAttr> cattr = new HashMap<String, CAttr>();
    public Color lightamb = null, lightdif = null, lightspc = null;
    public Color olightamb = null, olightdif = null, olightspc = null;
    public Color tlightamb = null, tlightdif = null, tlightspc = null;
    public static Color dlightamb = new Color(200, 200, 200);
    public static Color dlightdif = new Color(200, 200, 200);
    public static Color dlightspc = new Color(255, 255, 255);
    public double lightang = 0.0, lightelev = 0.0;
    public double olightang = 0.0, olightelev = 0.0;
    public double tlightang = 0.0, tlightelev = 0.0;
    public double lchange = -1;
    public Indir<Resource> sky1 = null, sky2 = null;
    public double skyblend = 0.0;
    private Map<Indir<Resource>, Object> wmap = new HashMap<Indir<Resource>, Object>();
    public static haven.timers.TimersThread timersThread;
    public String servertime;
    public Tex servertimetex;
    public int moonid = 0;
    public String mservertime;
    public String lservertime;
    public String rservertime;
    public String bservertime;
    public Tex mservertimetex;
    public Tex lservertimetex;
    public Tex rservertimetex;
    public boolean night = false; //true is night
    public Tex bservertimetex;

    private static WeakReference<Glob> reference = new WeakReference<>(null);

    public static Glob getByReference() {
        return reference.get();
    }

    static {
        timersThread = new haven.timers.TimersThread();
        timersThread.start();
    }

    public Glob(Session sess) {
        this.sess = sess;
        gobhitmap = new GobHitmap();
        map = new MCache(sess);
        party = new Party(this);
        reference = new WeakReference<>(this);
    }

    @Resource.PublishedCode(name = "wtr")
    public static interface Weather {
        public Pipe.Op state();

        public void update(Object... args);

        public boolean tick(double dt);
    }

    public static class CAttr extends Observable {
        public static final Text.Foundry fnd = new Text.Foundry(Text.sans, 12);
        String nm;
        int base, comp;
        private Text.Line compLine = null;
        public Tex comptex;

        public CAttr(String nm, int base, int comp) {
            this.nm = nm.intern();
            this.base = base;
            this.comp = comp;
            compLine = null;
            this.comptex = Text.renderstroked(comp + "", Color.WHITE, Color.BLACK, Text.num12boldFnd).tex();
        }

        public void update(int base, int comp) {
            if ((base == this.base) && (comp == this.comp))
                return;
            this.base = base;
            this.comp = comp;
            compLine = null;
            setChanged();
            notifyObservers(null);
            this.comptex = Text.renderstroked(comp + "", Color.WHITE, Color.BLACK, Text.num12boldFnd).tex();
        }

        public Text.Line compline() {
            if (compLine == null) {
                Color c = Color.WHITE;
                if (comp > base) {
                    c = CharWnd.buff;
                } else if (comp < base) {
                    c = CharWnd.debuff;
                }
                compLine = Text.renderstroked(Integer.toString(comp), c, Color.BLACK, fnd);
            }
            return compLine;
        }
    }

    private static Color colstep(Color o, Color t, double a) {
        int or = o.getRed(), og = o.getGreen(), ob = o.getBlue(), oa = o.getAlpha();
        int tr = t.getRed(), tg = t.getGreen(), tb = t.getBlue(), ta = t.getAlpha();
        return (new Color(or + (int) ((tr - or) * a),
                og + (int) ((tg - og) * a),
                ob + (int) ((tb - ob) * a),
                oa + (int) ((ta - oa) * a)));
    }

    private void ticklight(double dt) {
        if (lchange >= 0) {
            lchange += dt;
            if (lchange > 2.0) {
                lchange = -1;
                lightamb = tlightamb;
                lightdif = tlightdif;
                lightspc = tlightspc;
                lightang = tlightang;
                lightelev = tlightelev;
            } else {
                double a = lchange / 2.0;
                lightamb = colstep(olightamb, tlightamb, a);
                lightdif = colstep(olightdif, tlightdif, a);
                lightspc = colstep(olightspc, tlightspc, a);
                lightang = olightang + a * Utils.cangle(tlightang - olightang);
                lightelev = olightelev + a * Utils.cangle(tlightelev - olightelev);
            }
        }
    }

    private double lastctick = 0;

    public void ctick() {
        double now = Utils.rtime();
        double dt;
        if (lastctick == 0)
            dt = 0;
        else
            dt = Math.max(now - lastctick, 0.0);

        synchronized (this) {
            ticklight(dt);
            for (Object o : wmap.values()) {
                if (o instanceof Weather)
                    ((Weather) o).tick(dt);
            }
        }

        oc.ctick(dt);
        map.ctick(dt);

        lastctick = now;
    }

    public void gtick(Render g) {
        oc.gtick(g);
        map.gtick(g);
    }

    private final double timefac = 3.0;
    private double lastrep = 0, rgtime = 0;

    public double globtime() {
        double now = Utils.rtime();
        double raw = ((now - epoch) * timefac) + time;
        if (lastrep == 0) {
            rgtime = raw;
        } else {
            double gd = (now - lastrep) * timefac;
            rgtime += gd;
            if (Math.abs(rgtime + gd - raw) > 1.0)
                rgtime = rgtime + ((raw - rgtime) * (1.0 - Math.pow(10.0, -(now - lastrep))));
        }
        lastrep = now;
        return (rgtime);
    }

    private static final long secinday = 60 * 60 * 24;
    private static final long dewyladysmantletimemin = 4 * 60 * 60 + 45 * 60;
    private static final long dewyladysmantletimemax = 7 * 60 * 60 + 15 * 60;
    private static final String[] seasonNames = {"Spring", "Summer", "Autumn", "Winter"};
    private static final String[] mPhaseNames = {
            "New",
            "Waxing Crescent",
            "First Quarter",
            "Waxing Gibbous",
            "Full",
            "Waning Gibbous",
            "Last Quarter",
            "Waning Crescent"
    };

    private void servertimecalc() {
        if (ast == null)
            return;

        long secs = (long) globtime();
        long day = secs / secinday;
        long secintoday = secs % secinday;
        long hours = secintoday / 3600;
        long mins = (secintoday % 3600) / 60;
        int nextseason = (int) Math.ceil((1 - ast.sp) * (ast.is == 1 ? 30 : 10));

        String fmt;
        switch (ast.is) {
            case 0:
                fmt = nextseason == 1 ? "Day %d, %02d:%02d. Spring (%d RL day left)." : "Day %d, %02d:%02d. Spring (%d RL days left).";
                break;
            case 1:
                fmt = nextseason == 1 ? "Day %d, %02d:%02d. Summer (%d RL day left)." : "Day %d, %02d:%02d. Summer (%d RL days left).";
                break;
            case 2:
                fmt = nextseason == 1 ? "Day %d, %02d:%02d. Autumn (%d RL day left)." : "Day %d, %02d:%02d. Autumn (%d RL days left).";
                break;
            case 3:
                fmt = nextseason == 1 ? "Day %d, %02d:%02d. Winter (%d RL day left)." : "Day %d, %02d:%02d. Winter (%d RL days left).";
                break;
            default:
                fmt = "Unknown Season";
        }

        servertime = String.format(Resource.getLocString(Resource.BUNDLE_LABEL, fmt), day, hours, mins, nextseason);

        if (secintoday >= dewyladysmantletimemin && secintoday <= dewyladysmantletimemax)
            servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Dewy Lady's Mantle)");
        if (night) {
            if (moonid == 128)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (New Moon)");
            else if (moonid == 129)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Waxing Crescent)");
            else if (moonid == 130)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (First Quarter)");
            else if (moonid == 131)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Waxing Gibbous)");
            else if (moonid == 132)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Full Moon)");
            else if (moonid == 133)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Waning Gibbous)");
            else if (moonid == 134)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Last Quarter)");
            else if (moonid == 135)
                servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Waning Crescent)");
        } else
            servertime += Resource.getLocString(Resource.BUNDLE_LABEL, " (Daytime)");
        servertimetex = Text.render(servertime).tex();
    }

    public void blob(Message msg) {
        boolean inc = msg.uint8() != 0;
        while (!msg.eom()) {
            String t = msg.string().intern();
            Object[] a = msg.list();
            int n = 0;
            if (t == "tm") {
                time = ((Number) a[n++]).doubleValue();
                epoch = Utils.rtime();
                if (!inc)
                    lastrep = 0;
                servertimecalc();
            } else if (t == "astro") {
                double dt = ((Number) a[n++]).doubleValue();
                double mp = ((Number) a[n++]).doubleValue();
                double yt = ((Number) a[n++]).doubleValue();
                boolean night = (Integer) a[n++] != 0;
                Color mc = (Color) a[n++];
                int is = (n < a.length) ? ((Number) a[n++]).intValue() : 1;
                double sp = (n < a.length) ? ((Number) a[n++]).doubleValue() : 0.5;
                double sd = (n < a.length) ? ((Number) a[n++]).doubleValue() : 0.5;
                ast = new Astronomy(dt, mp, yt, night, mc, is, sp, sd);
            } else if (t == "light") {
                synchronized (this) {
                    tlightamb = (Color) a[n++];
                    tlightdif = (Color) a[n++];
                    tlightspc = (Color) a[n++];
                    tlightang = ((Number) a[n++]).doubleValue();
                    tlightelev = ((Number) a[n++]).doubleValue();
                    if (inc) {
                        olightamb = lightamb;
                        olightdif = lightdif;
                        olightspc = lightspc;
                        olightang = lightang;
                        olightelev = lightelev;
                        lchange = 0;
                    } else {
                        lightamb = tlightamb;
                        lightdif = tlightdif;
                        lightspc = tlightspc;
                        lightang = tlightang;
                        lightelev = tlightelev;
                        lchange = -1;
                    }
                }
            } else if (t == "sky") {
                synchronized (this) {
                    if (a.length < 1) {
                        sky1 = sky2 = null;
                        skyblend = 0.0;
                    } else {
                        sky1 = sess.getres(((Number) a[n++]).intValue());
                        if (a.length < 2) {
                            sky2 = null;
                            skyblend = 0.0;
                        } else {
                            sky2 = sess.getres(((Number) a[n++]).intValue());
                            skyblend = ((Number) a[n++]).doubleValue();
                        }
                    }
                }
            } else if (t == "wth") {
                synchronized (this) {
                    if (!inc)
                        wmap.clear();
                    Collection<Object> old = new LinkedList<Object>(wmap.keySet());
                    while (n < a.length) {
                        Indir<Resource> res = sess.getres(((Number) a[n++]).intValue());
                        Object[] args = (Object[]) a[n++];
                        Object curv = wmap.get(res);
                        if (curv instanceof Weather) {
                            Weather cur = (Weather) curv;
                            cur.update(args);
                        } else {
                            wmap.put(res, args);
                        }
                        old.remove(res);
                    }
                    for (Object p : old)
                        wmap.remove(p);
                }
            } else {
                System.err.println("Unknown globlob type: " + t);
            }
        }
    }

    public Collection<Weather> weather() {
        synchronized (this) {
            ArrayList<Weather> ret = new ArrayList<>(wmap.size());
            for (Map.Entry<Indir<Resource>, Object> cur : wmap.entrySet()) {
                Object val = cur.getValue();
                if (val instanceof Weather) {
                    ret.add((Weather) val);
                } else {
                    try {
                        Class<? extends Weather> cl = cur.getKey().get().layer(Resource.CodeEntry.class).getcl(Weather.class);
                        Weather w = Utils.construct(cl.getConstructor(Object[].class), new Object[]{val});
                        cur.setValue(w);
                        ret.add(w);
                    } catch (Loading l) {
                    } catch (NoSuchMethodException e) {
                        throw (new RuntimeException(e));
                    }
                }
            }
            return (ret);
        }
    }

    /* XXX: This is actually quite ugly and there should be a better
     * way, but until I can think of such a way, have this as a known
     * entry-point to be forwards-compatible with compiled
     * resources. */
    public static DirLight amblight(Pipe st) {
        return (((MapView) ((PView.WidgetContext) st.get(RenderContext.slot)).widget()).amblight);
    }

    public void cattr(Message msg) {
        synchronized (cattr) {
            while (!msg.eom()) {
                String nm = msg.string();
                int base = msg.int32();
                int comp = msg.int32();
                CAttr a = cattr.get(nm);
                if (a == null) {
                    a = new CAttr(nm, base, comp);
                    cattr.put(nm, a);
                } else {
                    a.update(base, comp);
                }
            }
        }
    }

    public static class FrameInfo extends State {
        public static final Slot<FrameInfo> slot = new Slot<>(Slot.Type.SYS, FrameInfo.class);
        public static final Uniform u_globtime = new Uniform(Type.FLOAT, "globtime", p -> {
            FrameInfo inf = p.get(slot);
            return ((inf == null) ? 0.0f : (float) (inf.globtime % 10000.0));
        }, slot);
        public final double globtime;

        public FrameInfo(Glob glob) {
            this.globtime = glob.globtime();
        }

        public ShaderMacro shader() {
            return (null);
        }

        public void apply(Pipe p) {
            p.put(slot, this);
        }

        public static Expression globtime() {
            return (u_globtime.ref());
        }

        public String toString() {
            return (String.format("#<globinfo @%fs>", globtime));
        }
    }
}
