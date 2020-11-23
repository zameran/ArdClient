package haven.res.ui.tt.attrmod;

import haven.CharWnd;
import haven.Coord;
import haven.ItemInfo;
import haven.ItemInfo.Tip;
import haven.PUtils;
import haven.Resource;
import haven.Resource.Image;
import haven.Resource.Resolver;
import haven.Resource.Tooltip;
import haven.RichText;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class AttrMod extends Tip {
    public final Collection<AttrMod.Mod> mods;
    private static String buff = "128,255,128";
    private static String debuff = "255,128,128";

    public AttrMod(Owner var1, Collection<AttrMod.Mod> var2) {
        super(var1);
        this.mods = var2;
        StringBuilder sinfo = new StringBuilder();
        for (Mod mod : mods) {
            sinfo.append(mod.toString()).append(" ");
        }
    }

    public static BufferedImage modimg(Collection<AttrMod.Mod> var0) {
        ArrayList var1 = new ArrayList(var0.size());
        Iterator var2 = var0.iterator();

        while (var2.hasNext()) {
            AttrMod.Mod var3 = (AttrMod.Mod) var2.next();
            BufferedImage var4 = RichText.render(String.format("%s $col[%s]{%s%d}", ((Tooltip) var3.attr.layer(Resource.tooltip)).t, var3.mod < 0 ? debuff : buff, Character.valueOf((char) (var3.mod < 0 ? '-' : '+')), Math.abs(var3.mod)), 0, new Object[0]).img;
            BufferedImage var5 = PUtils.convolvedown(((Image) var3.attr.layer(Resource.imgc)).img, new Coord(var4.getHeight(), var4.getHeight()), CharWnd.iconfilter);
            var1.add(catimgsh(0, new BufferedImage[]{var5, var4}));
        }

        return catimgs(0, (BufferedImage[]) var1.toArray(new BufferedImage[0]));
    }

    public BufferedImage tipimg() {
        return modimg(this.mods);
    }

    public static class Fac implements InfoFactory {
        public Fac() {
        }

        public ItemInfo build(Owner var1, Object... var2) {
            Resolver var3 = (Resolver) var1.context(Resolver.class);
            ArrayList var4 = new ArrayList();

            for (int var5 = 1; var5 < var2.length; var5 += 2) {
                var4.add(new AttrMod.Mod((Resource) var3.getres((Integer) var2[var5]).get(), (Integer) var2[var5 + 1]));
            }

            return new AttrMod(var1, var4);
        }
    }

    public static class Mod {
        public final Resource attr;
        public final int mod;

        public Mod(Resource var1, int var2) {
            this.attr = var1;
            this.mod = var2;
        }
    }
}
