package haven.overlays;

import haven.Camera;
import haven.Config;
import haven.Coord;
import haven.GLState;
import haven.GOut;
import haven.Location;
import haven.Matrix4f;
import haven.PView;
import haven.Projection;
import haven.RenderList;
import haven.Sprite;
import haven.Tex;
import haven.Text;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class newPlantStageSprite extends Sprite {
    public int stg;
    private Tex tex;
    private static Matrix4f mv = new Matrix4f();
    private Projection proj;
    private Coord wndsz;
    private Location.Chain loc;
    private Camera camp;
    private final boolean multistg, offsetmultisg;

    public static Map<Kit, Tex> cachedTex = new HashMap<>();
    public static Tex getCachedTex(String text, Color color, Color bgcol, Text.Foundry fnd) {
        Kit kit = new Kit(text, color, bgcol, fnd);
        Tex tex = null;
        for (Map.Entry<Kit, Tex> entry : cachedTex.entrySet())
            if (entry.getKey().equals(kit)) {
                tex = entry.getValue();
                break;
            }
        if (tex == null) {
            tex = Text.renderstroked(text, color, bgcol, fnd).tex();
            cachedTex.put(kit, tex);
        }
        return (tex);
    }
    public static class Kit {
        public String text;
        public Color color;
        public Color bgcol;
        public Text.Foundry fnd;

        public Kit(String text, Color color, Color bgcol, Text.Foundry fnd) {
            this.text = text;
            this.color = color;
            this.bgcol = bgcol;
            this.fnd = fnd;
        }

        public boolean equals(Kit kit) {
            return text.equals(kit.text) && color.equals(kit.color) && bgcol.equals(kit.bgcol) && fnd.equals(kit.fnd);
        }
    }

    Tex stg(int stg, int stgmax, Color clr) {
        return getCachedTex(String.format("%d/%d", stg, stgmax), clr, Color.BLACK, Text.num12boldFnd);
    }

    Tex stg(int stg, int stgmax, Color clr, Color border) {
        Tex tex = Text.renderstroked(String.format("%d/%d", stg, stgmax), clr, border, Text.num12boldFnd).tex();
        return tex;
    }

    public newPlantStageSprite(int stg, int stgmax, boolean multistg, boolean offsetmultisg) {
        super(null, null);
        this.multistg = multistg;
        this.offsetmultisg = offsetmultisg;
        update(stg, stgmax);
    }

    public void draw(GOut g) {
        float[] c = mv.load(camp.fin(Matrix4f.id)).mul1(loc.fin(Matrix4f.id)).homoc();
        Coord sc = proj.get2dCoord(c, wndsz);
        sc.x -= tex.sz().x / 2;
        sc.y -= 10;
        g.image(tex, sc);
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

    public void update(int stg, int stgmax) {
        this.stg = stg;
        int truestg = stg + 1;
        int truestgmax = stgmax + 1;

        if (stgmax == 0)
            tex = stg(stg, stgmax, Color.gray);
        else if (stg > stgmax)
            tex = stg(truestg, truestgmax, Color.blue);
        else if ((multistg || offsetmultisg) && stg == stgmax - 1)
            tex = stg(truestg, truestgmax, Color.yellow);
        else if (offsetmultisg && stg == stgmax - 2)
            tex = stg(truestg, truestgmax, new Color(255, 100, 50));
        else if (stg == stgmax)
            tex = stg(truestg, truestgmax, Color.green, Color.gray);
        else {
            if (Config.showfreshcropstage)
                tex = stg(truestg, truestgmax, Color.red);
            else
                tex = stg(truestg, truestgmax, Color.red);
        }
    }

    public Object staticp() {
        return CONSTANS;
    }
}
