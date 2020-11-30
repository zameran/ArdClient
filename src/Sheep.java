/* Preprocessed source code */
/* $use: ui/croster */

import haven.GOut;
import haven.res.ui.croster.Entry;

public class Sheep extends Entry {
    public int meat, milk, wool;
    public int meatq, milkq, woolq, hideq;
    public int seedq;

    public Sheep(long id, String name) {
        super(SIZE, id, name);
    }

    public void draw(GOut g) {
        drawbg(g);
        drawcol(g, SheepRoster.cols.get(0), 0, name, null, 0);
        drawcol(g, SheepRoster.cols.get(1), 1, q, quality, 1);
        drawcol(g, SheepRoster.cols.get(2), 1, meat, null, 2);
        drawcol(g, SheepRoster.cols.get(3), 1, milk, null, 3);
        drawcol(g, SheepRoster.cols.get(4), 1, wool, null, 4);
        drawcol(g, SheepRoster.cols.get(5), 1, meatq, percent, 5);
        drawcol(g, SheepRoster.cols.get(6), 1, milkq, percent, 6);
        drawcol(g, SheepRoster.cols.get(7), 1, woolq, percent, 7);
        drawcol(g, SheepRoster.cols.get(8), 1, hideq, percent, 8);
        drawcol(g, SheepRoster.cols.get(9), 1, seedq, null, 9);
        super.draw(g);
    }
}

/* >wdg: SheepRoster */
