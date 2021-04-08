package haven.res.lib.vmat;

import haven.GLState;
import haven.Gob;
import haven.Indir;
import haven.IntMap;
import haven.Material;
import haven.Message;
import haven.Resource;
import modification.dev;

import java.util.Collections;
import java.util.Map;

public class Materials extends Mapping {
    static {
        dev.checkFileVersion("lib/vmat", 35);
    }

    public static final Map<Integer, Material> empty = Collections.<Integer, Material>emptyMap();
    public final Map<Integer, Material> mats;

    public static Map<Integer, Material> decode(Resource.Resolver rr, Message sdt) {
        Map<Integer, Material> ret = new IntMap<Material>();
        int idx = 0;
        while (!sdt.eom()) {
            Indir<Resource> mres = rr.getres(sdt.uint16());
            int mid = sdt.int8();
            Material.Res mat;
            if (mid >= 0)
                mat = mres.get().layer(Material.Res.class, mid);
            else
                mat = mres.get().layer(Material.Res.class);
            ret.put(idx++, mat.get());
        }
        return (ret);
    }

    public static Material stdmerge(Material orig, Material var) {
        haven.resutil.OverTex otex = null;
        for (GLState st : orig.states) {
            if (st instanceof haven.resutil.OverTex) {
                otex = (haven.resutil.OverTex) st;
                break;
            }
        }
        if (otex == null)
            return (var);
        return (new Material(var, otex));
    }

    public Material mergemat(Material orig, int mid) {
        if (!mats.containsKey(mid))
            return (orig);
        Material var = mats.get(mid);
        return (stdmerge(orig, var));
    }

    public Materials(Map<Integer, Material> mats) {
        this.mats = mats;
    }

    public Materials(Gob gob, Message dat) {
        this.mats = decode(gob.context(Resource.Resolver.class), dat);
    }
}

