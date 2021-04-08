package haven.res.gfx.terobjs.rubble;

import haven.Coord2d;
import haven.GLState;
import haven.Indir;
import haven.MCache;
import haven.Message;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;
import haven.Sprite.Owner;
import haven.Utils;
import haven.resutil.CSprite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Rubble implements Sprite.Factory {
    public static final GLState nilmat = new GLState.Abstract() {
        public void prep(Buffer buf) {
        }
    };
    public final List<Function<Owner, Rendered>> parts;
    public final double density;

    public Rubble(List<Function<Owner, Rendered>> parts, double density) {
        this.parts = parts;
        this.density = density;
    }

    private static List<Function<Owner, Rendered>> mksprites(Indir<Resource>[] rls) {
        List<Function<Owner, Rendered>> ret = new ArrayList<>();
        for (Indir<Resource> res : rls)
            ret.add(owner -> Sprite.create(owner, res.get(), Message.nil));
        return (ret);
    }

    @SuppressWarnings("unchecked")
    public Rubble(double density, Indir<Resource>... parts) {
        this(mksprites(parts), density);
    }

    @SuppressWarnings("unchecked")
    public Rubble() {
        this(15.0,
                Resource.remote().load("gfx/terobjs/rubble/rubble0", 1),
                Resource.remote().load("gfx/terobjs/rubble/rubble1", 1),
                Resource.remote().load("gfx/terobjs/rubble/rubble2", 1),
                Resource.remote().load("gfx/terobjs/rubble/rubble3", 1),
                Resource.remote().load("gfx/terobjs/rubble/rubble4", 1),
                Resource.remote().load("gfx/terobjs/rubble/rubble5", 1)
        );
    }

    public Sprite create(Owner owner, Resource res, Message sdt) {
        Rendered[] parts = new Rendered[this.parts.size()];
        Iterator<Function<Owner, Rendered>> it = this.parts.iterator();
        for (int i = 0; i < parts.length; i++)
            parts[i] = it.next().apply(owner);
        Coord2d ul, br;
        if (!sdt.eom()) {
            ul = new Coord2d(Utils.hfdec((short) sdt.int16()), Utils.hfdec((short) sdt.int16())).mul(MCache.tilesz);
            br = new Coord2d(Utils.hfdec((short) sdt.int16()), Utils.hfdec((short) sdt.int16())).mul(MCache.tilesz);
        } else {
            ul = new Coord2d(-11, -11);
            br = new Coord2d(11, 11);
        }
        Coord2d sz = br.sub(ul);
        int n = (int) Math.round(sz.x * sz.y * density * (1.0 / (MCache.tilesz.x * MCache.tilesz.y)));
        Random rnd = owner.mkrandoom();
        CSprite spr = new CSprite(owner, res);
        if (n > 2500 || n <= 0) {
            ul = new Coord2d(-5.5, -5.5);
            br = new Coord2d(5.5, 5.5);
            sz = br.sub(ul);
            n = (int) Math.round(sz.x * sz.y * density * (1.0 / (MCache.tilesz.x * MCache.tilesz.y)));
        }
        for (int i = 0; i < n; i++) {
            int p = rnd.nextInt(parts.length);
            double xo = ul.x + (rnd.nextDouble() * sz.x) + rnd.nextGaussian() * 2f;
            double yo = ul.y + (rnd.nextDouble() * sz.y) + rnd.nextGaussian() * 2f;
            spr.addpart((float) xo, (float) yo, nilmat, parts[p]);
        }
        return (spr);
    }
}
