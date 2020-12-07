/* Preprocessed source code */
/* $use: ui/croster */

import haven.GOut;
import haven.res.ui.croster.Entry;
import modification.dev;

public class Pig extends Entry {
	static {
		dev.checkFileVersion("gfx/hud/rosters/pig", 24);
	}
    public int meat, milk;
    public int meatq, milkq, hideq;
    public int seedq;
    public int prc;

    public Pig(long id, String name) {
        super(SIZE, id, name);
    }

    public void draw(GOut g) {
        drawbg(g);
        drawcol(g, PigRoster.cols.get(0), 0, this, namerend, 0);
        drawcol(g, PigRoster.cols.get(1), 1, q, quality, 1);
        drawcol(g, PigRoster.cols.get(2), 1, prc, null, 2);
        drawcol(g, PigRoster.cols.get(3), 1, meat, null, 3);
        drawcol(g, PigRoster.cols.get(4), 1, milk, null, 4);
        drawcol(g, PigRoster.cols.get(5), 1, meatq, percent, 5);
        drawcol(g, PigRoster.cols.get(6), 1, milkq, percent, 6);
        drawcol(g, PigRoster.cols.get(7), 1, hideq, percent, 7);
        drawcol(g, PigRoster.cols.get(8), 1, seedq, null, 7);
        super.draw(g);
    }
}

/* >wdg: PigRoster */
