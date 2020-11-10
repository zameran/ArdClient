package haven.sloth.gfx;

import haven.Buff;
import haven.Camera;
import haven.Coord;
import haven.Coord3f;
import haven.FastText;
import haven.Fightview;
import haven.GLState;
import haven.GOut;
import haven.Gob;
import haven.Location;
import haven.Matrix4f;
import haven.PView;
import haven.Projection;
import haven.RenderList;
import haven.Sprite;
import haven.Widget;

import java.awt.Color;

public class GobCombatSprite extends Sprite {
    public static final int id = -244942;
    private final Matrix4f mv = new Matrix4f();
    private Fightview.Relation rel;
    private Projection proj;
    private Coord wndsz;
    private Location.Chain loc;
    private Camera camp;
    private Coord3f sc, sczu;

    public GobCombatSprite(final Gob g, final Fightview.Relation relation) {
        super(g, null);
        this.rel = relation;
    }


    public void draw(GOut g) {
        if (rel != null) {
            mv.load(camp.fin(Matrix4f.id)).mul1(loc.fin(Matrix4f.id));
            sc = proj.toscreen(mv.mul4(Coord3f.o), wndsz);
            sczu = proj.toscreen(mv.mul4(Coord3f.zu), wndsz).sub(sc);
            final Coord c = new Coord(sc.add(sczu.mul(16))).sub(0, 60);
            final Coord bc = c.copy();

            //Draw Buffs
            for (Widget wdg = rel.buffs.child; wdg != null; wdg = wdg.next) {
                if (!(wdg instanceof Buff))
                    continue;
                final Buff buf = (Buff) wdg;
                if (buf.ameter >= 0 && buf.isOpening()) {
                    buf.fightdraw(g.reclip(bc.copy(), Buff.scframe.sz()));
                    bc.x += Buff.scframe.sz().x + 2;
                }
            }

            //Draw IP
            g.chcolor(new Color(60, 60, 60, 168));
            g.frect(c.sub(40, 0), FastText.sizes("IP " + rel.ip));
            g.frect(c.sub(40, -15), FastText.sizes("IP " + rel.oip));
            g.chcolor(Color.GREEN);
            FastText.printsf(g, c.sub(40, 0), "IP %d", rel.ip);
            g.chcolor(Color.RED);
            FastText.printsf(g, c.sub(40, -15), "IP %d", rel.oip);
            g.chcolor();
        }
    }

    public void update(final Fightview.Relation rel) {
        this.rel = rel;
    }

    public boolean setup(RenderList rl) {
        rl.prepo(last);
        GLState.Buffer buf = rl.state();
        proj = buf.get(PView.proj);
        wndsz = buf.get(PView.wnd).sz();
        loc = buf.get(PView.loc);
        camp = buf.get(PView.cam);
        return true;
    }

    @Override
    public boolean tick(int dt) {
        return rel == null;
    }

    public Object staticp() {
        return Gob.STATIC;
    }
}
