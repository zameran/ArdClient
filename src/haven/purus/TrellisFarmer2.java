package haven.purus;

import haven.Button;
import haven.Coord;
import haven.FastMesh;
import haven.FlowerMenu;
import haven.GItem;
import haven.Gob;
import haven.IMeter;
import haven.Inventory;
import haven.Label;
import haven.Resource;
import haven.Widget;
import haven.Window;
import haven.purus.pbot.PBotUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

import static haven.OCache.posres;

public class TrellisFarmer2 extends Window implements Runnable {

    private Coord rc1, rc2;

    private ArrayList<Gob> crops = new ArrayList<Gob>();

    private boolean stopThread = false;

    private Label lblProg;

    private ArrayList<String> cropName = new ArrayList<String>();
    private ArrayList<String> seedName = new ArrayList<String>();
    private String trellis = "gfx/terobjs/plants/trellis";

    private boolean harvest = false;
    private boolean destroy = false;
    private boolean replant = false;

    public TrellisFarmer2(Coord rc1, Coord rc2, boolean harvest, boolean destroy, boolean replant) {
        super(new Coord(120, 65), "Trellis Farmer");
        this.rc1 = rc1;
        this.rc2 = rc2;
        this.harvest = harvest;
        this.destroy = destroy;
        this.replant = replant;

        // Initialise arraylists
        seedName.add("gfx/invobjs/peapod");
        seedName.add("gfx/invobjs/peppercorn");
        seedName.add("gfx/invobjs/seed-cucumber");
        seedName.add("gfx/invobjs/seed-grape");
        seedName.add("gfx/invobjs/hopcones");

        cropName.add("gfx/terobjs/plants/pepper");
        cropName.add("gfx/terobjs/plants/peas");
        cropName.add("gfx/terobjs/plants/hops");
        cropName.add("gfx/terobjs/plants/cucumber");
        cropName.add("gfx/terobjs/plants/wine");

        Label lblstxt = new Label("Progress:");
        add(lblstxt, new Coord(15, 35));
        lblProg = new Label("Initialising...");
        add(lblProg, new Coord(65, 35));

        Button stopBtn = new Button(120, "Stop") {
            @Override
            public void click() {
                stop();
            }
        };
        add(stopBtn, new Coord(0, 0));
    }

    public void run() {
        try {
            PBotUtils.sysMsg("Trellis Farmer started!", Color.white);
            if (harvest) {
                // Initialise crop list
                crops = Crops(true);

                // Initialize progression label on window
                int totalCrops = crops.size();
                int cropsHarvested = 0;
                lblProg.settext(cropsHarvested + "/" + totalCrops);

                for (Gob g : crops) {
                    if (stopThread) // Checks if aborted
                        return;

                    // Check if stamina is under 30%, drink if needed
                    IMeter.Meter stam = ui.gui.getmeter("stam", 0);
                    if (stam.a <= 30) {
                        PBotUtils.drink(ui, true);
                    }

                    if (stopThread)
                        return;

                    int stageBefore = g.getStage();

                    // Right click the crop
                    PBotUtils.doClick(ui, g, 3, 0);

                    // Wait for harvest menu to appear
                    while (ui.root.findchild(FlowerMenu.class) == null) {
                        PBotUtils.sleep(10);
                        if (stopThread)
                            return;
                    }

                    // Select the harvest option
                    FlowerMenu menu = ui.root.findchild(FlowerMenu.class);
                    if (menu != null) {
                        for (FlowerMenu.Petal opt : menu.opts) {
                            if (opt.name.equals("Harvest")) {
                                menu.choose(opt);
                                menu.destroy();
                            }
                        }
                    }

                    // Wait until stage has changed = harvested
                    while (true) {
                        if (PBotUtils.findObjectById(ui, g.id) == null
                                || PBotUtils.findObjectById(ui, g.id).getStage() != stageBefore)
                            break;
                        else
                            PBotUtils.sleep(20);
                        if (stopThread)
                            return;
                    }

                    // Drop excess seeds
                    for (Widget w = PBotUtils.playerInventory(ui).inv.child; w != null; w = w.next) {
                        if (w instanceof GItem && (seedName.contains(((GItem) w).res.get().name)
                                || ((GItem) w).res.get().name.equals("gfx/invobjs/grapes"))) {
                            GItem item = (GItem) w;
                            try {
                                item.wdgmsg("drop", Coord.z);
                            } catch (Exception e) {
                                // Shouldnt matter
                            }
                        }
                    }

                    // Update progression
                    cropsHarvested++;
                    lblProg.settext(cropsHarvested + "/" + totalCrops);
                }
            } // End of harvest

            if (destroy) {
                crops = Crops(false);

                // Initialize progression label on window
                int totalCrops = crops.size();
                int cropsHarvested = 0;
                lblProg.settext(cropsHarvested + "/" + totalCrops);

                for (Gob g : crops) {
                    if (stopThread) // Checks if aborted
                        return;

                    // Check if stamina is under 30%, drink if needed
                    IMeter.Meter stam = ui.gui.getmeter("stam", 0);
                    if (stam.a <= 30) {
                        PBotUtils.drink(ui, true);
                    }

                    if (stopThread)
                        return;

                    // Click destroy on gob
                    PBotUtils.destroyGob(ui, g);

                    // Wait until the gob is gone = destroyed
                    while (PBotUtils.findObjectById(ui, g.id) != null) {
                        PBotUtils.sleep(10);
                        if (stopThread)
                            return;
                    }

                    // Update progression
                    cropsHarvested++;
                    lblProg.settext(cropsHarvested + "/" + totalCrops);
                }
            } // End of destroy

            if (replant) {
                crops = Trellises(); // in this case crops = trellis
                // Initialise progression label on window
                int totalCrops = crops.size();
                int cropsHarvested = 0;
                lblProg.settext(cropsHarvested + "/" + totalCrops);

                for (Gob g : crops) {

                    // Take a seed from inventory to hand
                    GItem item = null;
                    while (PBotUtils.getItemAtHand(ui) == null) {
                        Inventory inv = PBotUtils.playerInventory(ui).inv;
                        for (Widget w = inv.child; w != null; w = w.next) {
                            if (w instanceof GItem && seedName.contains(((GItem) w).resource().name)) {
                                item = (GItem) w;
                                break;
                            }
                        }
                        if (item != null)
                            PBotUtils.takeItem(ui, item);
                    }

                    if (stopThread)
                        return;

                    // Right click trellis with the seed
                    PBotUtils.itemClick(ui, g, 0);

                    // Wait until item is gone from hand = Planted
                    int retry = 0; // IF no success for 10 seconds skip
                    while (PBotUtils.getItemAtHand(ui) != null) {
                        PBotUtils.sleep(10);
                        if (stopThread)
                            return;
                        retry++;
                        if (retry > 1000)
                            break;
                    }

                    // Update progression
                    cropsHarvested++;
                    lblProg.settext(cropsHarvested + "/" + totalCrops);
                }
            }

            PBotUtils.sysMsg(ui, "Trellis Farmer finished!", Color.white);
            this.destroy();
        } catch (Resource.Loading l) {

        }
    }

    public ArrayList<Gob> Crops(boolean checkStage) {
        // Initialises list of crops to harvest between selected coordinates
        ArrayList<Gob> gobs = new ArrayList<Gob>();
        double bigX = rc1.x > rc2.x ? rc1.x : rc2.x;
        double smallX = rc1.x < rc2.x ? rc1.x : rc2.x;
        double bigY = rc1.y > rc2.y ? rc1.y : rc2.y;
        double smallY = rc1.y < rc2.y ? rc1.y : rc2.y;
        synchronized (ui.sess.glob.oc) {
            for (Gob gob : ui.sess.glob.oc) {
                if (gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && cropName.contains(gob.getres().name)) {
                    // Add to list if its max stage
                    if (checkStage) {
                        int cropstgmaxval = 0;
                        for (FastMesh.MeshRes layer : gob.getres().layers(FastMesh.MeshRes.class)) {
                            int stg = layer.id / 10;
                            if (stg > cropstgmaxval)
                                cropstgmaxval = stg;
                        }
                        if (gob.getStage() == cropstgmaxval) {
                            gobs.add(gob);
                        }
                    } else
                        gobs.add(gob);
                }
            }
        }
        gobs.sort(new CoordSort());
        return gobs;
    }

    public ArrayList<Gob> Trellises() {
        // Initialises list of crops to harvest between selected coordinates
        ArrayList<Gob> gobs = new ArrayList<Gob>();
        double bigX = rc1.x > rc2.x ? rc1.x : rc2.x;
        double smallX = rc1.x < rc2.x ? rc1.x : rc2.x;
        double bigY = rc1.y > rc2.y ? rc1.y : rc2.y;
        double smallY = rc1.y < rc2.y ? rc1.y : rc2.y;
        synchronized (ui.sess.glob.oc) {
            for (Gob gob : ui.sess.glob.oc) {
                if (gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && gob.getres().name.equals(trellis)) {
                    gobs.add(gob);
                }
            }
        }
        gobs.sort(new CoordSort());
        return gobs;
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (sender == cbtn) {
            stop();
            reqdestroy();
        } else
            super.wdgmsg(sender, msg, args);
    }

    // Sorts coordinate array to efficient sequence
    class CoordSort implements Comparator<Gob> {
        public int compare(Gob a, Gob b) {

            if (a.rc.x == b.rc.x) {
                if (a.rc.x % 2 == 0)
                    return (a.rc.y < b.rc.y) ? 1 : (a.rc.y > b.rc.y) ? -1 : 0;
                else
                    return (a.rc.y < b.rc.y) ? -1 : (a.rc.y > b.rc.y) ? 1 : 0;
            } else
                return (a.rc.x < b.rc.x) ? -1 : (a.rc.x > b.rc.x) ? 1 : 0;
        }
    }

    public void stop() {
        // Stops thread
        PBotUtils.sysMsg(ui, "Trellis Farmer stopped!", Color.white);
        ui.gui.map.wdgmsg("click", Coord.z, ui.gui.map.player().rc.floor(posres), 1, 0);
        if (ui.gui.map.pfthread != null) {
            ui.gui.map.pfthread.interrupt();
        }
        stopThread = true;
        this.destroy();
    }
}