/* Preprocessed source code */
package haven.res.ui.croster;

import haven.BuddyWnd;
import haven.CharWnd;
import haven.CheckBox;
import haven.Coord;
import haven.GOut;
import haven.Tex;
import haven.UI;
import haven.Utils;
import haven.Widget;
import modification.dev;

import java.awt.Color;
import java.util.Arrays;
import java.util.function.Function;

public class Entry extends Widget {
    static {
        dev.checkFileVersion("ui/croster", 42);
    }

    public static final int WIDTH = CattleRoster.WIDTH;
    public static final int HEIGHT = UI.scale(20);
    public static final Coord SIZE = new Coord(WIDTH, HEIGHT);
    public static final Color every = new Color(255, 255, 255, 16), other = new Color(255, 255, 255, 32);
    public static final Function<Integer, String> percent = v -> String.format("%d%%", v);
    public static final Function<Number, String> quality = v -> Long.toString(Math.round(v.doubleValue()));
    public static final Function<Entry, Tex> namerend = e -> {
        return (CharWnd.attrf.render(e.name, BuddyWnd.gc[e.grp]).tex());
    };
    public final long id;
    public String name;
    public int grp;
    public double q;
    public int idx;
    public CheckBox mark;

    public Entry(Coord sz, long id, String name) {
        super(sz);
        this.id = id;
        this.name = name;
        this.mark = adda(new CheckBox(""), UI.scale(5), sz.y / 2, 0, 0.5);
    }

    protected void drawbg(GOut g) {
        g.chcolor(((idx & 1) == 0) ? every : other);
        g.frect(Coord.z, sz);
        g.chcolor();
    }

    private Tex[] rend = {};
    private Object[] rendv = {};

    public <V> void drawcol(GOut g, Column<?> col, double a, V val, Function<? super V, ?> fmt, int idx) {
        if (fmt == null) fmt = Function.identity();
        if (rend.length <= idx) {
            rend = Arrays.copyOf(rend, idx + 1);
            rendv = Arrays.copyOf(rendv, idx + 1);
        }
        if (!Utils.eq(rendv[idx], val)) {
            if (rend[idx] != null)
                rend[idx].dispose();
            Object rval = fmt.apply(val);
            if (rval instanceof Tex)
                rend[idx] = (Tex) rval;
            else
                rend[idx] = CharWnd.attrf.render(String.valueOf(rval)).tex();
            rendv[idx] = val;
        }
        Coord sz = rend[idx].sz();
        g.image(rend[idx], new Coord(col.x + (int) Math.round((col.w - sz.x) * a), (this.sz.y - sz.y) / 2));
    }

    public boolean mousedown(Coord c, int button) {
        if (super.mousedown(c, button))
            return (true);
        getparent(CattleRoster.class).wdgmsg("click", (int) (id & 0x00000000ffffffffl), (int) ((id & 0xffffffff00000000l) >> 32), button, ui.modflags(), ui.mc);
        return (true);
    }
}

