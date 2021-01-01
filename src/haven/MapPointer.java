package haven;

import haven.purus.pbot.PBotUtils;
import modification.configuration;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.Color;


public class MapPointer extends Widget {
    private static final TextureAtlas.Img icon = Theme.timg("pointer");
    public static final States.ColState col = new States.ColState(241, 227, 157, 255);
    public Coord2d tc = null;
    public Coord lc = null;

    public MapPointer() {
        super(Coord.z);
    }

    public void presize() {
        resize(this.parent.sz);
    }

    protected void added() {
        presize();
        super.added();
    }

    private int signum(int paramInt) {
        if (paramInt < 0) {
            return -1;
        }
        if (paramInt > 0) {
            return 1;
        }
        return 0;
    }

    private void drawarrow(GOut g, Coord sc) {
        Coord hsz = this.sz.div(2);
        sc = sc.sub(hsz);
        if (sc.equals(Coord.z)) {
            sc = new Coord(1, 1);
        }
        double d = Coord.z.dist(sc);
        Coord localCoord2 = sc.mul((d - 25.0D) / d);
        float f = hsz.y / (float) hsz.x;
        if ((Math.abs(localCoord2.x) > hsz.x) || (Math.abs(localCoord2.y) > hsz.y)) {
            if (Math.abs(localCoord2.x) * f < Math.abs(localCoord2.y)) {
                localCoord2 = new Coord(localCoord2.x * hsz.y / localCoord2.y, hsz.y).mul(signum(localCoord2.y));
            } else {
                localCoord2 = new Coord(hsz.x, localCoord2.y * hsz.x / localCoord2.x).mul(signum(localCoord2.x));
            }
        }
        Coord localCoord3 = localCoord2.sub(sc).norm(30.0D);
        localCoord2 = localCoord2.add(hsz);

        g.state2d();
        g.state(col);
        g.apply();
        g.gl.glEnable(GL2.GL_POLYGON_SMOOTH);
        g.gl.glBegin(GL.GL_TRIANGLES);
        g.vertex(localCoord2); //The point
        g.vertex(localCoord2.add(localCoord3).add(-localCoord3.y / 3, localCoord3.x / 3)); //one side
        g.vertex(localCoord2.add(localCoord3).add(localCoord3.y / 3, -localCoord3.x / 3)); //The other
        g.gl.glEnd();
        g.gl.glDisable(GL2.GL_POLYGON_SMOOTH);
        g.aimage(icon, localCoord2.add(localCoord3), 0.5D, 0.5D);
        if (configuration.showpointdist)
            g.aimage(Text.renderstroked(dist + "", Color.WHITE, Color.BLACK, Text.num12boldFnd).tex(), localCoord2.add(localCoord3), 0.5, 0.5);
        this.lc = localCoord2.add(localCoord3);
    }

    private void drawarrow(GOut g, double a) {
        Coord hsz = sz.div(2);
        double ca = -Coord.z.angle(hsz);
        Coord ac;
        if ((a > ca) && (a < -ca)) {
            ac = new Coord(sz.x, hsz.y - (int) (Math.tan(a) * hsz.x));
        } else if ((a > -ca) && (a < Math.PI + ca)) {
            ac = new Coord(hsz.x - (int) (Math.tan(a - Math.PI / 2) * hsz.y), 0);
        } else if ((a > -Math.PI - ca) && (a < ca)) {
            ac = new Coord(hsz.x + (int) (Math.tan(a + Math.PI / 2) * hsz.y), sz.y);
        } else {
            ac = new Coord(0, hsz.y + (int) (Math.tan(a) * hsz.x));
        }
        Coord bc = ac.add(Coord.sc(a, 0));

        g.state2d();
        g.state(col);
        g.apply();
        g.gl.glEnable(GL2.GL_POLYGON_SMOOTH);
        g.gl.glBegin(GL.GL_TRIANGLES);
        g.vertex(bc);
        g.vertex(bc.add(Coord.sc(a + Math.PI / 12, -35)));
        g.vertex(bc.add(Coord.sc(a - Math.PI / 12, -35)));
        g.gl.glEnd();
        g.gl.glDisable(GL2.GL_POLYGON_SMOOTH);

        g.aimage(icon, bc.add(Coord.sc(a, -30)), 0.5, 0.5);
        if (configuration.showpointdist)
            g.aimage(Text.renderstroked(dist + "", Color.WHITE, Color.BLACK, Text.num12boldFnd).tex(), bc.add(Coord.sc(a, -30)), 0.5, 0.5);
        this.lc = bc.add(Coord.sc(a, -30));
    }

    public void draw(GOut g) {
        this.lc = null;
        if (this.tc == null) {
            return;
        }

        if (ui.gui != null && ui.gui.map != null) {
            final Coord3f sc = ui.gui.map.screenxf(this.tc);

            if (tc != null) {
                final Double angle = ui.gui.map.screenangle(tc, true);
                final Gob me = PBotUtils.player(ui);
                if (me != null) {
                    final int cdist = (int) (Math.ceil(me.rc.dist(tc) / 11.0));
                    if (cdist != dist) {
                        dist = cdist;
                    }
                }
                if (!angle.equals(Double.NaN)) {
                    drawarrow(g, ui.gui.map.screenangle(tc, true));
                } else {
                    drawarrow(g, new Coord(sc));
                }
            }
        }
    }

    public void update(Coord2d mc) {
        this.tc = mc;
    }

    private Text.Line tt = null;
    private int dist;

    public Object tooltip(Coord c, Widget wdg) {
        if ((this.lc != null) && (this.lc.dist(c) < 20.0D) && tc != null) {
            if (tt != null && tt.tex() != null)
                tt.tex().dispose();
            return tt = Text.render("Distance: " + dist);
        } else {
            return null;
        }
    }
}
