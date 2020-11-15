package modification;

import haven.Resource;
import haven.Tex;
import haven.Utils;

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
}
