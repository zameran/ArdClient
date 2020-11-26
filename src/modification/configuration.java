package modification;

import haven.Coord;
import haven.Gob;
import haven.MainFrame;
import haven.OCache;
import haven.PUtils;
import haven.Session;
import haven.Tex;
import haven.TexI;
import haven.Utils;
import haven.sloth.gfx.SnowFall;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class configuration {
    public static String modificationPath = "modification";
    public static String soundPath = modificationPath + "/sound";
    public static String picturePath = modificationPath + "/picture";
    public static String errorPath = "errors";
    public static String pbotErrorPath = "pboterrors";

    public static boolean customTitleBoolean = Utils.getprefb("custom-title-bol", false);

    public static String defaultCustomTitleName(String name) {
        return "♂" + name + "♂: ♂right version♂";
    }

    public static String defaultTitle = MainFrame.TITLE;
    public static String defaultCustomTitle = "https://youtu.be/dQw4w9WgXcQ";
    public static String defaultUtilsCustomTitle = Utils.getpref("custom-title", defaultCustomTitle);

    public static String tittleCheck(Session sess) {
        String name, title;

        if (sess == null)
            name = "";
        else
            name = " \u2013 " + sess.username;

        if (configuration.customTitleBoolean)
            title = configuration.defaultUtilsCustomTitle;
        else
            title = defaultTitle;

        return title + name;
    }

    public static Coord savedHavenPanelSize = Utils.getprefc("havpansz", new Coord(800, 600));

    public static boolean autoclick = Utils.getprefb("autoclick", false);

    public static boolean statustooltip = Utils.getprefb("statustooltip", false);

    public static boolean newCropStageOverlay = Utils.getprefb("newCropStageOverlay", false);

    public static boolean newQuickSlotWdg = Utils.getprefb("newQuickSlotWdg", false);

    public static boolean scaletree = Utils.getprefb("scaletree", false);
    public static int scaletreeint = Utils.getprefi("scaletreeint", 25);

    public static boolean proximityspecial = Utils.getprefb("proximityspecial", false);
    public static boolean customquality = Utils.getprefb("customquality", false);
    public static boolean showtroughstatus = Utils.getprefb("showtroughstatus", false);
    public static boolean showbeehivestatus = Utils.getprefb("showbeehivestatus", false);
    public static boolean morethanquility = Utils.getprefb("morethanquility", false);
    public static int morethancolor = Utils.getprefi("morethancolor", -1);
    public static int morethancoloroutline = Utils.getprefi("morethancoloroutline", Color.RED.hashCode());
    public static boolean showpointdist = Utils.getprefb("showpointdist", false);
    public static boolean straightridges = Utils.getprefb("straightridges", false);
    public static boolean gobspeedsprite = Utils.getprefb("gobspeedsprite", false);

    public static List<String> liquids = new ArrayList<String>(Arrays.asList("Water", "Milk", "Aurochs Milk", "Cowsmilk", "Sheepsmilk", "Goatsmilk", "Piping Hot Tea", "Tea", "Applejuice", "Pearjuice", "Grapejuice", "Stale grapejuice", "Cider", "Perry", "Wine", "Beer", "Weißbier", "Mead")) {{
        sort(new Comparator<String>() {
            @Override
            public int compare(String l1, String l2) {
                return l1.compareTo(l2);
            }
        });
    }};
    public static String autoDrinkLiquid = Utils.getpref("autoDrinkLiquid", "Water");
    public static boolean drinkorsip = Utils.getprefb("drinkorsip", false);
    public static boolean autodrinkosip = Utils.getprefb("autodrinkosip", false);
    public static int autosipthreshold = Utils.getprefi("autosipthreshold", 100);
    public static boolean autoDrinkWhatever = Utils.getprefb("autoDrinkWhatever", false);
    public static boolean siponce = Utils.getprefb("siponce", false);
    public static int sipwaiting = Utils.getprefi("sipwaiting", 2000);
    public static boolean drinkmessage = Utils.getprefb("drinkmessage", false);
    public static boolean autocleardamage = Utils.getprefb("autocleardamage", false);

    public static boolean scalingmarks = Utils.getprefb("scalingmarks", false);
    public static boolean bigmapshowgrid = Utils.getprefb("bigmapshowgrid", false);
    public static boolean bigmaphidemarks = Utils.getprefb("bigmapshowmarks", false);

    public static float badcamdistdefault = Utils.getpreff("badcamdistdefault", 50.0f);
    public static float badcamdistminimaldefault = Utils.getpreff("badcamdistminimaldefault", 5.0f);
    public static float badcamelevdefault = Utils.getpreff("badcamelevdefault", (float) Math.PI / 4.0f);
    public static float badcamangldefault = Utils.getpreff("badcamangldefault", 0.0f);

    public static boolean nocursor = Utils.getprefb("nocursor", false);

    public static String[] customMenuGrid = new String[]{Utils.getpref("customMenuGrid0", "6"), Utils.getpref("customMenuGrid1", "4")};

    public static Coord getMenuGrid() {
        return new Coord(Integer.parseInt(configuration.customMenuGrid[0]), Integer.parseInt(configuration.customMenuGrid[1]));
    }

    public static Coord getAutoSize(int w, int h) {
        Coord minSize = new Coord(800, 600);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Coord maxSize = new Coord(screenSize);
        Coord chosenSize = new Coord(w, h);

        if ((w < minSize.x && h > maxSize.y) || (w > maxSize.x && h < minSize.y) || (w < minSize.x && h < minSize.y)) {
            return minSize;
        }

        if (w < minSize.x) {
            chosenSize = new Coord(minSize.x, h * minSize.x / w);
            if (chosenSize.y < minSize.y) {
                chosenSize = new Coord(chosenSize.x * minSize.y / chosenSize.y, minSize.y);
                if (chosenSize.x > maxSize.x) {
                    chosenSize = new Coord(maxSize.x, chosenSize.y * maxSize.x / chosenSize.x);
                    if (chosenSize.y > maxSize.y)
                        chosenSize = new Coord(chosenSize.x * maxSize.y / chosenSize.y, maxSize.y);
                }
            }
        }
        if (h < minSize.y) {
            chosenSize = new Coord(w * minSize.y / h, minSize.y);
            if (chosenSize.x > maxSize.x) {
                chosenSize = new Coord(maxSize.x, chosenSize.y * maxSize.x / chosenSize.x);
                if (chosenSize.y > maxSize.y)
                    chosenSize = new Coord(chosenSize.x * maxSize.y / chosenSize.y, maxSize.y);
            }
        }
        if (w > maxSize.x) {
            chosenSize = new Coord(maxSize.x, h * maxSize.x / w);
            if (chosenSize.y > maxSize.y)
                chosenSize = new Coord(chosenSize.x * maxSize.y / chosenSize.y, maxSize.y);
        }
        if (h > maxSize.y)
            chosenSize = new Coord(w * maxSize.y / h, maxSize.y);

        return chosenSize;
    }

    public static BufferedImage scaleImage(BufferedImage before) {
        try {
            int w = before.getWidth();
            int h = before.getHeight();

            Coord chosenSize = new Coord(w, h);

            double scale1 = (double) chosenSize.x / w;
            double scale2 = (double) chosenSize.y / h;

            return scalingImage(before, chosenSize, scale1, scale2);
        } catch (Exception e) {
            e.printStackTrace();
            return before;
        }
    }

    public static BufferedImage scaleImage(BufferedImage before, Coord chosenSize) {
        try {
            int w = before.getWidth();
            int h = before.getHeight();

            double scale1 = (double) chosenSize.x / w;
            double scale2 = (double) chosenSize.y / h;

            return scalingImage(before, chosenSize, scale1, scale2);
        } catch (Exception e) {
            e.printStackTrace();
            return before;
        }
    }

    public static BufferedImage scaleImage(String name) throws Exception {
        File img = new File(name);
        BufferedImage in = ImageIO.read(img);

        return scaleImage(in);
    }

    public static BufferedImage scaleImage(String name, Coord chosenSize) throws Exception {
        File img = new File(name);
        BufferedImage in = ImageIO.read(img);

        return scaleImage(in, chosenSize);
    }

    public static BufferedImage scaleImage(BufferedImage before, boolean autoSize) {
        try {
            int w = before.getWidth();
            int h = before.getHeight();

            Coord chosenSize;
            if (autoSize) chosenSize = getAutoSize(w, h);
            else chosenSize = new Coord(w, h);

            // Create a new image of the proper size
            int w2 = chosenSize.x;
            int h2 = chosenSize.y;
            double scale1 = (double) w2 / w;
            double scale2 = (double) h2 / h;
            BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
            AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale1, scale2);
            AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);

            scaleOp.filter(before, after);
            return after;
        } catch (Exception e) {
            e.printStackTrace();
            return before;
        }
    }

    public static BufferedImage scaleImage(BufferedImage before, int scale) {
        try {
            int w = before.getWidth();
            int h = before.getHeight();
            // Create a new image of the proper size
            int w2 = (int) (w * scale);
            int h2 = (int) (h * scale);
            BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
            AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
            AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);

            scaleOp.filter(before, after);
            return after;
        } catch (Exception e) {
            e.printStackTrace();
            return before;
        }
    }

    public static BufferedImage scaleImage(String name, BufferedImage defaultImage) {
        try {
            return scaleImage(name);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultImage;
        }
    }

    public static BufferedImage scaleImage(String name, Coord chosenSize, BufferedImage defaultImage) {
        try {
            return scaleImage(name, chosenSize);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultImage;
        }
    }

    public static BufferedImage scalingImage(BufferedImage before, Coord chosenSize, double scale1, double scale2) {
        BufferedImage after = new BufferedImage(chosenSize.x, chosenSize.y, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale1, scale2);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);

        scaleOp.filter(before, after);
        return after;
    }

    public static Tex getTex(String name, Coord chosenSize, boolean autoSize) throws IOException {
        BufferedImage in;
        File img = new File(name);
        in = ImageIO.read(img);

        BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();

        Tex tex;
        if (autoSize)
            tex = new TexI(scaleImage(newImage, true));
        else if (chosenSize != null)
            tex = new TexI(scaleImage(newImage, chosenSize));
        else
            tex = new TexI(newImage);

        return tex;
    }

    public static Tex getTex(String name) throws IOException {
        return getTex(name, null, false);
    }

    public static Tex getTex(String name, Coord chosenSize) throws IOException {
        return getTex(name, chosenSize, false);
    }

    public static Tex getTex(String name, boolean autoSize) throws IOException {
        return getTex(name, null, autoSize);
    }

    public static Tex imageToTex(String name, boolean autoSize, Coord chosenSize, Tex defaultTex) {
        try {
            if (autoSize)
                return getTex(name, autoSize);
            else if (chosenSize == null)
                return getTex(name);
            else
                return getTex(name, chosenSize);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(name);
            if (defaultTex == null)
                return null;
            else
                return defaultTex;
        }
    }

    public static Tex imageToTex(String name) {
        return imageToTex(name, false, null, null);
    }

    public static Tex imageToTex(String name, Coord chosenSize) {
        return imageToTex(name, false, chosenSize, null);
    }

    public static Tex imageToTex(String name, boolean autoSize) {
        return imageToTex(name, autoSize, null, null);
    }

    public static Tex imageToTex(String name, Tex defaultTex) {
        return imageToTex(name, false, null, defaultTex);
    }

    public static Tex imageToTex(String name, Coord chosenSize, Tex defaultTex) {
        return imageToTex(name, false, chosenSize, defaultTex);
    }

    public static Tex imageToTex(String name, boolean autoSize, Tex defaultTex) {
        return imageToTex(name, autoSize, null, defaultTex);
    }

    public static BufferedImage rotate(BufferedImage image, double angle) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(angle, image.getWidth() / 2, image.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return image = op.filter(image, null);
    }

    public static TexI monochrome(TexI texI, Color color) {
        BufferedImage bimg = PUtils.monochromize(texI.back, color);
        return new TexI(bimg);
    }

    public static ArrayList<String> findFiles(String dir, List<String> exts) {
        try {
            File file = new File(dir);

            ArrayList<String> list = new ArrayList<String>();
            if (!file.exists()) System.out.println(dir + " folder not exists");
            for (String ext : exts) {
                File[] listFiles = file.listFiles(new MyFileNameFilter(ext));
                if (listFiles.length == 0) {
                    //System.out.println(dir + " не содержит файлов с расширением " + ext);
                } else {
                    for (File f : listFiles) {
                        list.add(dir + File.separator + f.getName());
                        //System.out.println("File: " + dir + File.separator + f.getName());
                    }
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class MyFileNameFilter implements FilenameFilter {

        private String ext;

        public MyFileNameFilter(String ext) {
            this.ext = ext.toLowerCase();
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(ext);
        }
    }

    public static String getDefaultTextName(String gobname) {
        if (gobname.contains("/")) {
            int p = gobname.lastIndexOf('/');
            if (p < 0) return (gobname);
            return gobname.substring(p + 1, p + 2).toUpperCase() + gobname.substring(p + 2);
        } else return gobname;

    }

    public static boolean snowfalloverlay = Utils.getprefb("snowfalloverlay", false);
    public static boolean blizzardoverlay = Utils.getprefb("blizzardoverlay", false);

    public static int blizzarddensity = Utils.getprefi("blizzarddensity", 5);
    public static int currentsnow = 0;

    public synchronized static int getCurrentsnow(OCache oc) {
        int count = 0;
        for (final Gob g : oc) {
            if (g.isplayer()) continue;
            if (g.findol(-4921) != null)
                count++;
        }
        return currentsnow = count;
    }

    public synchronized static void addsnow(OCache oc) {
        ArrayList<Gob> gobs = new ArrayList<>();
        oc.forEach(gobs::add);

        while (configuration.getCurrentsnow(oc) < configuration.blizzarddensity) {
            Gob g = getRandom(gobs);
            if (g.findol(-4921) != null || g.isplayer()) continue;
            g.addol(new Gob.Overlay(-4921, new SnowFall(g)));
        }
    }

    public synchronized static void deleteAllSnow(OCache oc) {
        for (final Gob g : oc) {
            if (g.isplayer()) continue;
            Gob.Overlay snow = g.findol(-4921);
            if (snow != null)
                g.ols.remove(snow);
        }
    }

    public synchronized static void deleteSnow(OCache oc) {
        ArrayList<Gob> gobs = new ArrayList<>();
        for (final Gob g : oc) {
            if (g.isplayer()) continue;
            Gob.Overlay snow = g.findol(-4921);
            if (snow != null)
                gobs.add(g);
        }

        while (configuration.getCurrentsnow(oc) > configuration.blizzarddensity) {
            Gob g = getRandom(gobs);
            Gob.Overlay snow = g.findol(-4921);
            if (snow != null)
                g.ols.remove(snow);
        }
    }

    public static Gob getRandom(ArrayList<Gob> array) {
        int rnd = new Random().nextInt(array.size());
        return array.get(rnd);
    }

    public static class SnowThread extends Thread {
        private final AtomicBoolean running = new AtomicBoolean(false);
        final OCache oc;

        public SnowThread(OCache oc) {
            super("Snowfall");
            this.oc = oc;
        }

        @Override
        public void run() {
            running.set(true);
            while (running.get() && configuration.blizzardoverlay) {
                synchronized (oc) {
                    configuration.addsnow(oc);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void kill() {
            running.set(false);
            interrupt();
            configuration.deleteAllSnow(oc);
            configuration.snowThread = null;
        }
    }

    public static SnowThread snowThread;
}
