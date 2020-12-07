/* Preprocessed source code */
package haven.res.ui.croster;

import haven.BuddyWnd;
import haven.CheckBox;
import haven.Coord;
import haven.GAttrib;
import haven.GOut;
import haven.Gob;
import haven.Message;
import haven.PView;
import haven.RenderList;
import haven.Rendered;
import haven.Tex;
import haven.TexI;
import haven.Text;
import haven.Utils;
import modification.dev;

import java.awt.Color;

public class CattleId extends GAttrib implements Rendered, PView.Render2D {
    static {
        dev.checkFileVersion("ui/croster", 42);
    }

    public final long id;
    public Gob gob;

    public CattleId(Gob gob, long id) {
        super(gob);
        this.gob = gob;
        this.id = id;
    }

    public static void parse(Gob gob, Message dat) {
        long id = dat.int64();
        gob.setattr(new CattleId(gob, id));
    }

    private int rmseq = 0, entryseq = 0;
    private RosterWindow wnd = null;
    private CattleRoster<?> roster = null;
    private Entry entry = null;

    public Entry entry() {
        if ((entry == null) || ((roster != null) && (roster.entryseq != entryseq))) {
            if (rmseq != RosterWindow.rmseq) {
                synchronized (RosterWindow.rosters) {
                    RosterWindow wnd = RosterWindow.rosters.get(gob.glob);
                    if (wnd != null) {
                        for (CattleRoster<?> ch : wnd.children(CattleRoster.class)) {
                            if (ch.entries.get(this.id) != null) {
                                this.wnd = wnd;
                                this.roster = ch;
                                this.rmseq = RosterWindow.rmseq;
                                break;
                            }
                        }
                    }
                }
            }
            if (roster != null)
                this.entry = roster.entries.get(this.id);
        }
        return (entry);
    }

    private String lnm;
    private int lgrp;
    private Tex rnm;

    public void draw2d(GOut g) {
//        Coord sc = Homo3D.obj2view(new Coord3f(0, 0, 25), state, Area.sized(g.sz())).round2();
        Coord sc = gob.sc;
        if (sc.isect(Coord.z, g.sz())) {
            Entry entry = entry();
            int grp = (entry != null) ? entry.grp : 0;
            String name = (entry != null) ? entry.name : null;
            if ((name != null) && ((rnm == null) || !name.equals(lnm) || (grp != lgrp))) {
                Color col = BuddyWnd.gc[grp];
                rnm = new TexI(Utils.outline2(Text.render(name, col).img, Utils.contrast(col)));
                lnm = name;
                lgrp = grp;
            }
            if ((rnm != null) && (wnd != null) && wnd.visible) {
                Coord nmc = sc.sub(rnm.sz().x / 2, -rnm.sz().y);
                g.image(rnm, nmc);
                if ((entry != null) && entry.mark.a)
                    g.image(CheckBox.smark, nmc.sub(CheckBox.smark.sz().x, 0));
            }
        }
    }

    @Override
    public boolean setup(RenderList r) {
        return true;
    }

    @Override
    public void draw(GOut g) {
    }
}
