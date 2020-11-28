package haven;

import haven.sloth.gui.MovableWidget;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class FepMeter extends MovableWidget {
    private static final Tex bg = Resource.loadtex("hud/meter/fepmeter");

    private final CharWnd.FoodMeter food;

    public FepMeter(CharWnd.FoodMeter food, String name) {
        super(IMeter.fsz, name);
        this.food = food;
    }

    @Override
    public void draw(GOut g) {
        Coord isz = IMeter.msz;
        Coord off = IMeter.off;
        g.chcolor(0, 0, 0, 255);
        g.frect(off, isz);
        g.chcolor();
        double x = 0;
        int w = isz.x;
        for (CharWnd.FoodMeter.El el : food.els) {
            int l = (int) Math.floor((x / food.cap) * w);
            int r = (int) Math.floor(((x += el.a) / food.cap) * w);
            try {
                Color col = el.ev().col;
                g.chcolor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
                g.frect(off.add(l, 0), new Coord(r - l, isz.y));
            } catch (Loading e) {
            }
        }
        g.chcolor();
        g.image(bg, Coord.z);
        if (Config.showmetertext) {
            List<CharWnd.FoodMeter.El> els = food.els;
            BufferedImage cur = null;
            double sum = 0.0;
            for (CharWnd.FoodMeter.El el : els) {
                CharWnd.FoodMeter.Event ev = el.res.get().layer(CharWnd.FoodMeter.Event.class);
                Color col = Utils.blendcol(ev.col, Color.WHITE, 0.5);
                BufferedImage ln = Text.render(String.format("%s: %s", ev.nm, Utils.odformat2(el.a, 2)), col).img;
                Resource.Image icon = el.res.get().layer(Resource.imgc);
                if (icon != null)
                    ln = ItemInfo.catimgsh(5, icon.img, ln);
                cur = ItemInfo.catimgs(0, cur, ln);
                sum += el.a;
            }
            g.atextstroked(String.format(Resource.getLocString(Resource.BUNDLE_LABEL, "%s/%s"), Utils.odformat2(sum, 2), Utils.odformat(food.cap, 2)), sz.div(2).add(10, -1), 0.5, 0.5, Color.WHITE, Color.BLACK, Text.num10Fnd);
        }

        super.draw(g);
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
        return food.tooltip(c, prev);
    }
}