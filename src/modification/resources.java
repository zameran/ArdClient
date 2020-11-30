package modification;

import haven.Indir;
import haven.Resource;
import haven.Tex;
import haven.Text;
import haven.Utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class resources {
    public static String picturePath = "modification/picture";

    public static boolean defaultUtilsCustomLoginScreenBgBoolean = Utils.getprefb("custom-login-background-bol", false);
    public static String defaultCustomLoginScreenBg = picturePath + "/loginscr.png";
    public static String defaultUtilsCustomLoginScreenBg = Utils.getpref("custom-login-background", defaultCustomLoginScreenBg);

    public static Tex bgCheck() {
        Tex bg;
        if (defaultUtilsCustomLoginScreenBgBoolean)
            bg = configuration.imageToTex(defaultUtilsCustomLoginScreenBg, true, Resource.loadtex("gfx/loginscr"));
        else bg = Resource.loadtex("gfx/loginscr");
        return bg;
    }

    public static Tex invsq = Resource.loadtex("gfx/hud/invsq");

    public static boolean customMarkObj = Utils.getprefb("customMarkObj", false);
    public static Map<String, String> customMarkObjs = new HashMap<String, String>() {{
        put("gfx/tiles/ridges/cavein", getDefaultTextName("gfx/tiles/ridges/cavein"));
        put("gfx/tiles/ridges/caveout", getDefaultTextName("gfx/tiles/ridges/caveout"));
        put("gfx/terobjs/beaverdamdoor", getDefaultTextName("gfx/terobjs/beaverdamdoor"));
        put("gfx/terobjs/dng/batcave", getDefaultTextName("gfx/terobjs/dng/batcave"));
        put("gfx/terobjs/dng/antdungeon", getDefaultTextName("gfx/terobjs/dng/antdungeon"));
        put("gfx/terobjs/wonders/tarpit", getDefaultTextName("gfx/terobjs/wonders/tarpit"));
    }};

    public static String getDefaultTextName(String gobname) {
        if (gobname.contains("/")) {
            int p = gobname.lastIndexOf('/');
            if (p < 0) return (gobname);
            return gobname.substring(p + 1, p + 2).toUpperCase() + gobname.substring(p + 2);
        } else return gobname;
    }

    public static class IndirResource implements Indir<Resource> {
        public Resource res;
        public String resnm;

        public IndirResource(String resnm) {
            this.resnm = resnm;
        }

        public Resource get() {
            try {
                if (res == null)
                    res = Resource.remote().load(resnm).get();
            } catch (Exception e) {
            }
            return (res);
        }

        public String toString() {
            return ("<Resource Icon " + res + ">");
        }

        private void reset() {
            res = null;
        }
    }

    public static Map<Kit, Tex> cachedTex = new HashMap<>();
    public static Map<String, IndirResource> cachedRes = new HashMap<>();

    public static Tex getCachedTex(Kit kit) {
        for (Map.Entry<Kit, Tex> entry : cachedTex.entrySet())
            if (entry.getKey().equals(kit))
                return (entry.getValue());
        return (null);
    }

    public static Tex getCachedTex(String text) {
        Kit kit = new Kit(text);
        Tex tex = getCachedTex(kit);
        if (tex == null) {
            tex = Text.render(text).tex();
            cachedTex.put(kit, tex);
        }
        return (tex);
    }

    public static Tex getCachedTex(String text, Color color, Color bgcol, Text.Foundry fnd) {
        Kit kit = new Kit(text, color, bgcol, fnd);
        Tex tex = getCachedTex(kit);
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

        public Kit(String text) {
            this(text, null, null, null);
        }

        public boolean equals(Kit kit) {
            if (!text.equals(kit.text)) return (false);

            if (color != null && kit.color != null && color.equals(kit.color)) return (false);
            if (bgcol != null && kit.bgcol != null && bgcol.equals(kit.bgcol)) return (false);
            if (fnd != null && kit.fnd != null && fnd.equals(kit.fnd)) return (false);

            if ((color != null && kit.color == null) || (color == null && kit.color != null)) return (false);
            if ((bgcol != null && kit.bgcol == null) || (bgcol == null && kit.bgcol != null)) return (false);
            if ((fnd != null && kit.fnd == null) || (fnd == null && kit.fnd != null)) return (false);

            return (true);
        }
    }

    public static IndirResource getCachedRes(String text) {
        IndirResource res = cachedRes.get(text);
        if (res == null) {
            res = new resources.IndirResource(text);
            cachedRes.put(text, res);
        }
        return (res);
    }
}
