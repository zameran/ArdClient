/* Preprocessed source code */
/* $use: ui/croster */

import haven.Coord;
import haven.GOut;
import haven.res.ui.croster.Entry;
import modification.dev;

public class Ochs extends Entry {
    static {
        dev.checkFileVersion("gfx/hud/rosters/cow", 68);
    }

    public int meat, milk;
    public int meatq, milkq, hideq;
    public double tmeatq, tmilkq, thideq;
    public int seedq;
    public boolean bull, calf, dead, pregnant;

    public Ochs(long id, String name) {
        super(SIZE, id, name);
    }

    public void draw(GOut g) {
        drawbg(g);
        int i = 0;
        drawcol(g, CowRoster.cols.get(i), 0, this, namerend, i++);
        drawcol(g, CowRoster.cols.get(i), 0.5, bull, sex, i++);
        drawcol(g, CowRoster.cols.get(i), 0.5, calf, growth, i++);
        drawcol(g, CowRoster.cols.get(i), 0.5, dead, deadrend, i++);
        drawcol(g, CowRoster.cols.get(i), 0.5, pregnant, pregrend, i++);
        drawcol(g, CowRoster.cols.get(i), 1, q, quality, i++);
        drawcol(g, CowRoster.cols.get(i), 1, meat, null, i++);
        drawcol(g, CowRoster.cols.get(i), 1, milk, null, i++);
        drawcol(g, CowRoster.cols.get(i), 1, meatq, percent, i++);
        drawcol(g, CowRoster.cols.get(i), 1, tmeatq, quality, i++);
        drawcol(g, CowRoster.cols.get(i), 1, milkq, percent, i++);
        drawcol(g, CowRoster.cols.get(i), 1, tmilkq, quality, i++);
        drawcol(g, CowRoster.cols.get(i), 1, hideq, percent, i++);
        drawcol(g, CowRoster.cols.get(i), 1, thideq, quality, i++);
        drawcol(g, CowRoster.cols.get(i), 1, seedq, null, i++);
        super.draw(g);
    }

    public boolean mousedown(Coord c, int button) {
        if (CowRoster.cols.get(1).hasx(c.x)) {
            markall(Ochs.class, o -> (o.bull == this.bull));
            return (true);
        }
        if (CowRoster.cols.get(2).hasx(c.x)) {
            markall(Ochs.class, o -> (o.calf == this.calf));
            return (true);
        }
        if (CowRoster.cols.get(3).hasx(c.x)) {
            markall(Ochs.class, o -> (o.dead == this.dead));
            return (true);
        }
        if (CowRoster.cols.get(4).hasx(c.x)) {
            markall(Ochs.class, o -> (o.pregnant == this.pregnant));
            return (true);
        }
        return (super.mousedown(c, button));
    }
}
