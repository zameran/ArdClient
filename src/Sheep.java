import haven.Coord;
import haven.GOut;
import haven.res.ui.croster.Entry;
import modification.dev;

public class Sheep extends Entry {
    static {
        dev.checkFileVersion("gfx/hud/rosters/sheep", 57);
    }

    public int meat, milk, wool;
    public int meatq, milkq, woolq, hideq;
    public double tmeatq, tmilkq, twoolq, thideq;
    public int seedq;
    public boolean ram, lamb, dead, pregnant;

    public Sheep(long id, String name) {
        super(SIZE, id, name);
    }

    public void draw(GOut g) {
        drawbg(g);
        int i = 0;
        drawcol(g, SheepRoster.cols.get(i), 0, this, namerend, i++);
        drawcol(g, SheepRoster.cols.get(i), 0.5, ram, sex, i++);
        drawcol(g, SheepRoster.cols.get(i), 0.5, lamb, growth, i++);
        drawcol(g, SheepRoster.cols.get(i), 0.5, dead, deadrend, i++);
        drawcol(g, SheepRoster.cols.get(i), 0.5, pregnant, pregrend, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, q, quality, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, meat, null, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, milk, null, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, wool, null, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, meatq, percent, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, tmeatq, quality, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, milkq, percent, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, tmilkq, quality, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, woolq, percent, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, twoolq, quality, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, hideq, percent, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, thideq, quality, i++);
        drawcol(g, SheepRoster.cols.get(i), 1, seedq, null, i++);
        super.draw(g);
    }

    public boolean mousedown(Coord c, int button) {
        if (SheepRoster.cols.get(1).hasx(c.x)) {
            markall(Sheep.class, o -> (o.ram == this.ram));
            return (true);
        }
        if (SheepRoster.cols.get(2).hasx(c.x)) {
            markall(Sheep.class, o -> (o.lamb == this.lamb));
            return (true);
        }
        if (SheepRoster.cols.get(3).hasx(c.x)) {
            markall(Sheep.class, o -> (o.dead == this.dead));
            return (true);
        }
        if (SheepRoster.cols.get(4).hasx(c.x)) {
            markall(Sheep.class, o -> (o.pregnant == this.pregnant));
            return (true);
        }
        return (super.mousedown(c, button));
    }
}
