/* Preprocessed source code */

import haven.Coord;
import haven.Equipory;
import haven.GOut;
import haven.Resource;
import haven.UI;
import haven.Widget;

import static haven.Inventory.invsq;

/* >wdg: WardEqu */
public class WardEqu extends Equipory {
    public static final Resource.Image dicon = Resource.remote().loadwait("ui/wd-equ").layer(Resource.imgc);
    public final boolean[] dis = new boolean[ecoords.length];

    public WardEqu() {
        super(-1);
    }

    public static Widget mkwidget(UI ui, Object... args) {
        return (new WardEqu());
    }

    public void drawslots(GOut g) {
        super.drawslots(g);
        for (int i = 0; i < ecoords.length; i++) {
//            g.image(invsq, ecoords[i]);
            if (dis[i])
                g.image(dicon, ecoords[i]);
        }
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "dis") {
            dis[(Integer) args[0]] = ((Integer) args[1]) != 0;
        } else {
            super.uimsg(msg, args);
        }
    }

    public boolean mousedown(Coord c, int button) {
        if (super.mousedown(c, button))
            return (true);
        int ep = epat(c);
        if (ep >= 0) {
            wdgmsg("slot", ep, button, ui.modflags());
            return (true);
        }
        return (false);
    }
}
