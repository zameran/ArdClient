/* Preprocessed source code */
/* $use: ui/croster */

import haven.GOut;
import haven.res.ui.croster.Entry;

public class Horse extends Entry {
    public int meat, milk;
    public int meatq, milkq, hideq;
    public int seedq;
    public int end, stam, mb;

    public Horse(long id, String name) {
        super(SIZE, id, name);
    }

    public void draw(GOut g) {
        drawbg(g);
        drawcol(g, HorseRoster.cols.get(0), 0, name, null, 0);
        drawcol(g, HorseRoster.cols.get(1), 1, q, quality, 1);
        drawcol(g, HorseRoster.cols.get(2), 1, end, null, 2);
        drawcol(g, HorseRoster.cols.get(3), 1, stam, null, 3);
        drawcol(g, HorseRoster.cols.get(4), 1, mb, null, 4);
        drawcol(g, HorseRoster.cols.get(5), 1, meat, null, 5);
        drawcol(g, HorseRoster.cols.get(6), 1, milk, null, 6);
        drawcol(g, HorseRoster.cols.get(7), 1, meatq, percent, 7);
        drawcol(g, HorseRoster.cols.get(8), 1, milkq, percent, 8);
        drawcol(g, HorseRoster.cols.get(9), 1, hideq, percent, 9);
        drawcol(g, HorseRoster.cols.get(10), 1, seedq, null, 10);
        super.draw(g);
    }
}

/* >wdg: HorseRoster */
