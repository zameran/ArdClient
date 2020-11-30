/* Preprocessed source code */
/* $use: ui/croster */

import haven.GOut;
import haven.res.ui.croster.Entry;

public class Ochs extends Entry {
    public int meat, milk;
    public int meatq, milkq, hideq;
    public int seedq;

    public Ochs(long id, String name) {
        super(SIZE, id, name);
    }

    public void draw(GOut g) {
        drawbg(g);
        drawcol(g, CowRoster.cols.get(0), 0, name, null, 0);
        drawcol(g, CowRoster.cols.get(1), 1, q, quality, 1);
        drawcol(g, CowRoster.cols.get(2), 1, meat, null, 2);
        drawcol(g, CowRoster.cols.get(3), 1, milk, null, 3);
        drawcol(g, CowRoster.cols.get(4), 1, meatq, percent, 4);
        drawcol(g, CowRoster.cols.get(5), 1, milkq, percent, 5);
        drawcol(g, CowRoster.cols.get(6), 1, hideq, percent, 6);
        drawcol(g, CowRoster.cols.get(7), 1, seedq, null, 7);
        super.draw(g);
    }
}

/* >wdg: CowRoster */
