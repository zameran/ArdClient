package haven.res.ui.tt.attrmod;

import haven.CharWnd;
import haven.Coord;
import haven.ItemInfo;
import haven.ItemInfo.Tip;
import haven.Resource;
import haven.Resource.Resolver;
import haven.RichText;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;

import static haven.PUtils.convolvedown;

public class AttrMod extends Tip {
    public final Collection<AttrMod.Mod> mods;
    private static String buff = "128,255,128";
    private static String debuff = "255,128,128";

    public AttrMod(Owner owner, Collection<AttrMod.Mod> mods) {
        super(owner);
        this.mods = mods;
        /*StringBuilder sinfo = new StringBuilder();
        for (Mod mod : mods) {
            sinfo.append(mod.toString()).append(" ");
        }*/
    }

    public static BufferedImage modimg(Collection<AttrMod.Mod> mods) {
        Collection<BufferedImage> imgs = new LinkedList<>();

        for (AttrMod.Mod mod : mods) {
            BufferedImage head = RichText.render(String.format("%s $col[%s]{%s%d}", mod.attr.layer(Resource.tooltip).t, mod.mod < 0 ? debuff : buff, mod.mod < 0 ? '-' : '+', Math.abs(mod.mod)), 0).img;
            BufferedImage icon = convolvedown((mod.attr.layer(Resource.imgc)).img, new Coord(head.getHeight(), head.getHeight()), CharWnd.iconfilter);
            imgs.add(catimgsh(0, icon, head));
        }

        return catimgs(0, imgs.toArray(new BufferedImage[0]));
    }

    public BufferedImage tipimg() {
        return modimg(mods);
    }

    public static class Fac implements InfoFactory {
        public Fac() {
        }

        public ItemInfo build(Owner owner, Object... attr) {
            Resolver resolver = owner.context(Resolver.class);
            Collection<Mod> mods = new LinkedList<>();

            for (int i = 1; i < attr.length; i += 2) {
                mods.add(new AttrMod.Mod(resolver.getres((Integer) attr[i]).get(), (Integer) attr[i + 1]));
            }

            return new AttrMod(owner, mods);
        }
    }

    public static class Mod {
        public final Resource attr;
        public final int mod;

        public Mod(Resource res, int mod) {
            this.attr = res;
            this.mod = mod;
        }
    }
}
