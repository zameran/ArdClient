package haven.automation;


import haven.Button;
import haven.*;
import haven.Label;
import haven.Window;
import haven.Coord;
import haven.purus.BotUtils;
import haven.purus.pbot.PBotAPI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static haven.OCache.posres;

public class CoalToSmelters extends Window implements GobSelectCallback {
    private static final Text.Foundry infof = new Text.Foundry(Text.sans, 10).aa(true);
    private GameUI gui;
    private Coord selectedAreaA, selectedAreaB;
    private Gob gob;
    private static final int TIMEOUT_ACT = 12000;
    private Gob smelter, chest;
    private ArrayList<Gob> stockpiles = new ArrayList<>();
    private ArrayList<Gob> hqstockpiles = new ArrayList<>();
    private ArrayList<Gob> orestockpiles = new ArrayList<>();
    private int count = 4;
    private int stockpilecount, hqstockpilecount, orestockpilecount = 0;
    private final Label lblc, LabelFuel, LabelStockpiles, hqLabelStockpiles, oreLabelStockpiles;
    public boolean terminaterun, terminatelight, terminateore = false;

    private int smeltercount;
    public boolean autodropperon = false;
    private Button clearbtn, stopbtn, fuelbtn, lightallbtn, runbtn3, areaSelBtn, swapbtn, hqareaSelBtn, oreareaSelBtn, orefill;
    private static final int TIMEOUT = 6000;
    private static final int HAND_DELAY = 16;
    public Thread runner, light, autodropper, selectingarea, hqselectingarea, oreselectingarea, orefiller;
    private int countretain;
    private static Window chestwind;
    public static int delay = 100;
    public Equipory e;
    public boolean noltorch;

   List<String> invobjs = Arrays.asList("Bloodstone", "Cassiterite", "Chalcopyrite", "Cinnabar", "Malachite",
            "Heavy Earth", "Iron Ochre","Black Ore","Galena","Silvershrine","Horn Silver","Direvein","Schrifterz","Leaf Ore","Meteorite");

    List<Gob> list = new ArrayList<>();
    List<Gob> biglist = new ArrayList<>();
    List<Gob> activelist = new ArrayList<>();
    List<Gob> torchlist = new ArrayList<>();



    public CoalToSmelters(GameUI gui) {


        super(new Coord(270, 215), "Add Coal To Smelters");

        final Label lbl = new Label("Alt + Click to select Ovens or Smelters", infof);
        add(lbl, new Coord(53, 0));

        final Label lbl4 = new Label("Number of coal/branches being added.", infof);
        add(lbl4, new Coord(50, 10));
        LabelFuel = new Label("1", Text.num12boldFnd, Color.WHITE);
        add(LabelFuel, new Coord(120, 20));
        LabelFuel.settext(count + "");


        final Label lbl6 = new Label("Number of fuel stockpiles.", infof);
        add(lbl6, new Coord(137, 35));
        LabelStockpiles = new Label("1", Text.num12boldFnd, Color.WHITE);
        add(LabelStockpiles, new Coord(177, 45));
        LabelStockpiles.settext(stockpilecount + "");


        final Label lbl7 = new Label("Number of HQ stockpiles.", infof);
        add(lbl7, new Coord(0, 35));
        hqLabelStockpiles = new Label("1", Text.num12boldFnd, Color.WHITE);
        add(hqLabelStockpiles, new Coord(50, 45));
        hqLabelStockpiles.settext(hqstockpilecount + "");

        final Label lbl8 = new Label("Number of ore stockpiles.", infof);
        add(lbl8, new Coord(137, 60));
        oreLabelStockpiles = new Label("1", Text.num12boldFnd, Color.WHITE);
        add(oreLabelStockpiles, new Coord(177, 70));
        oreLabelStockpiles.settext(orestockpilecount + "");

        final Label lbl2 = new Label("Count Ovens/Smelters", infof);
        add(lbl2, new Coord(0, 60));
        lblc = new Label("0", Text.num12boldFnd, Color.WHITE);
        add(lblc, new Coord(50, 70));


        fuelbtn = new Button(100, "Fuel Count") {
            @Override
            public void click() {
                gui.error("Adjusting Fuel Count");
                terminaterun = false;
                if (count == 4) {
                    count = 9;
                }else if (count == 9) {
                    count = 12;
                }else if (count == 12) {
                    count = 4;
                }
                LabelFuel.settext(count + "");
            }
        };
        add(fuelbtn, new Coord(80, 88));
        clearbtn = new Button(100, "Clear Lists") {
            @Override
            public void click() {
                terminaterun = false;
                BotUtils.sysMsg("Bank 1 Cleared", Color.white);
                list.clear();
                stockpiles.clear();
                activelist.clear();
                orestockpiles.clear();
                hqstockpiles.clear();
                lblc.settext(list.size() + "");
                LabelStockpiles.settext(stockpiles.size() + "");
                hqLabelStockpiles.settext(hqstockpiles.size()+"");
                oreLabelStockpiles.settext(orestockpiles.size()+"");
            }
        };
        add(clearbtn, new Coord(80, 113));
        runbtn3 = new Button(100, "Run All") {
            @Override
            public void click() {
                if (list.size() == 0) {
                    gameui().error("No Smelters/Ovens in the list.");
                    return;
                }
                this.hide();
                cbtn.hide();
                areaSelBtn.hide();
                stopbtn.show();
                terminaterun = false;
                clearbtn.hide();
                runner = new Thread(new CoalToSmelters.Runner(), "Add Coal To Smelters");
                runner.start();
            }
        };
        add(runbtn3, new Coord(80, 136));
        stopbtn = new Button(100, "Stop") {
            @Override
            public void click() {
                BotUtils.sysMsg("Stopping", Color.white);
                // terminate();
                if (runner != null)
                    runner.interrupt();
                if (light != null)
                    light.interrupt();
                if(orefiller != null)
                    orefiller.interrupt();
                this.hide();
                // terminate = true;
                // terminate2 = true;
                terminate();
                runbtn3.show();
                clearbtn.show();
                orefill.show();
                fuelbtn.show();
                areaSelBtn.show();
                lightallbtn.show();
                cbtn.show();
            }
        };
        stopbtn.hide();
        add(stopbtn, new Coord(80, 130));
        lightallbtn = new Button(100, "Light all") {
            @Override
            public void click() {
                this.hide();
                terminaterun = false;
                terminatelight = false;
                stopbtn.show();
                clearbtn.hide();
                areaSelBtn.hide();
                fuelbtn.hide();
                cbtn.show();
                light = new Thread(new CoalToSmelters.light(), "Add Coal To Smelters");
                light.start();
            }
        };
        add(lightallbtn, new Coord(80, 161));
        swapbtn = new Button(80, "AutoDrop") {
            @Override
            public void click() {
                if(autodropperon){
                    BotUtils.sysMsg("Autodropper stopped",Color.white);
                    autodropperon=false;
                    autodropper.interrupt();
                }
                else {
                    autodropperon = true;
                    autodropper = new Thread(new CoalToSmelters.autodropper(),"Add Coal To Smelters");
                    autodropper.start();
                    BotUtils.sysMsg("Autodropper started.",Color.white);
                }
            }
        };
        add(swapbtn, new Coord(179, 190));
        areaSelBtn = new Button(100, "Select") {
            @Override
            public void click() {
                selectingarea = new Thread(new CoalToSmelters.selectingarea(), "Add Coal To Smelters");
                selectingarea.start();
            }
        };
        add(areaSelBtn, new Coord(80, 185));

        hqareaSelBtn = new Button(80, "HQ Select ") {
            @Override
            public void click() {
                hqselectingarea = new Thread(new CoalToSmelters.hqselectingarea(), "Add Coal To Smelters");
                hqselectingarea.start();
            }
        };
        add(hqareaSelBtn, new Coord(0, 192));

        oreareaSelBtn = new Button(80, "Ore Select") {
            @Override
            public void click() {
                oreselectingarea = new Thread(new CoalToSmelters.oreselectingarea(), "Add Coal To Smelters");
                oreselectingarea.start();
            }
        };
        add(oreareaSelBtn, new Coord(0, 168));

        orefill = new Button(80, "Ore Filller ") {
            @Override
            public void click() {
                if (list.size() == 0) {
                    gameui().error("No Smelters/Ovens in the list.");
                    return;
                }
                this.hide();
                cbtn.hide();
                areaSelBtn.hide();
                stopbtn.show();
                terminaterun = false;
                terminateore = false;
                runbtn3.hide();
                clearbtn.hide();
                orefiller = new Thread(new CoalToSmelters.orefiller(), "Add Coal To Smelters");
                orefiller.start();
            }
        };
        add(orefill, new Coord(0, 143));
if(Config.dropsmelterstones) {
    autodropper = new Thread(new CoalToSmelters.autodropper(), "Add Coal To Smelters");
    autodropper.start();
    autodropperon = true;
}

    }
        public class autodropper implements Runnable {
        @Override
             public void run() {
            gui = gameui();
                        while(autodropperon) {
                            try {
                                BotUtils.waitForWindow("Ore Smelter");
                                while(gui.getwnd("Ore Smelter") == null){
                                    BotUtils.sleep(10);
                                    if(!autodropperon)
                                        break;
                                }
                                if(!autodropperon)
                                    break;
                                BotUtils.sleep(100);
                                Window w = gui.getwnd("Ore Smelter");
                              //  if (w == null)
                                  // return;
                                if (Config.dropsmelterstones) {
                                    for (Widget smelter = w.lchild; smelter != null; smelter = smelter.prev) {
                                        if (smelter instanceof Inventory) {
                                            if (BotUtils.getInventoryContents((Inventory) smelter).size() != 0) {
                                                List<WItem> stones = getstones((Inventory) smelter);
                                                List<WItem> bars = getbars((Inventory) smelter);
                                                List<WItem> quicksilver = getsilver((Inventory) smelter);
                                                for (WItem item : stones) {
                                                    GItem stone = item.item;
                                                    stone.wdgmsg("drop", Coord.z);
                                                }
                                                for (WItem bar : bars) {
                                                    GItem barslol = bar.item;
                                                    barslol.wdgmsg("transfer", Coord.z);
                                                }
                                                if (quicksilver.size() > 0) {
                                                    Equipory f = gui.getequipory();
                                                    WItem l = f.quickslots[6];
                                                    WItem r = f.quickslots[7];

                                                    boolean nolbucket = true;
                                                    boolean norbucket = true;

                                                    if (l != null) {
                                                        String lname = l.item.getname();
                                                        if (lname.contains("Bucket"))
                                                            nolbucket = false;
                                                    }
                                                    if (r != null) {
                                                        String rname = r.item.getname();
                                                        if (rname.contains("Bucket"))
                                                            norbucket = false;
                                                    }

                                                    if (!nolbucket || !norbucket) {
                                                        WItem x = f.quickslots[nolbucket ? 7 : 6];
                                                        x.mousedown(new Coord(x.sz.x / 2, x.sz.y / 2), 1);
                                                        while (BotUtils.getItemAtHand() == null)
                                                            BotUtils.sleep(10);

                                                        try {
                                                            Thread.sleep(100);
                                                        } catch (InterruptedException ie) {
                                                            f.wdgmsg("drop", nolbucket ? 7 : 6);
                                                            return;
                                                        }
                                                        for (WItem qsilver : quicksilver) {
                                                            GItem quicksilverlol = qsilver.item;
                                                            // BotUtils.sysLogAppend("trying to iact","white");
                                                            quicksilverlol.wdgmsg("itemact", 0);
                                                        }
                                                        f.wdgmsg("drop", nolbucket ? 7 : 6);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    while(gui.getwnd("Ore Smelter")!= null)
                                        BotUtils.sleep(10);
                                }
                            } catch (NullPointerException p) {
                                p.printStackTrace();
                            }
                        }
            }
        }
        private class orefiller implements Runnable{
        @Override
            public void run() {
            try {
                gui = gameui();
                BotUtils.sysMsg("Ore filler started.", Color.white);
                if (list.size() == 0) {
                    BotUtils.sysMsg("No Smelters selected.", Color.white);
                    stopbtn.click();
                    return;
                }
                while (!terminateore) {
                    for (Gob gob : list) {
                        if (terminateore)
                            break;
                        int availableore = gui.maininv.getItemsPartial("Bloodstone", "Cassiterite", "Chalcopyrite", "Cinnabar", "Malachite",
                                "Heavy Earth", "Iron Ochre", "Black Ore", "Galena", "Silvershrine", "Horn Silver", "Direvein", "Schrifterz", "Leaf Ore", "Meteorite").size();
                        System.out.println("ore count : " + availableore);
                        if (availableore < 25)
                            getore();
                        System.out.println("ore count : " + availableore);
                        System.out.println("before click");
                        BotUtils.pfRightClick(gob, 0);
                        System.out.println("after click");
                        int unstucktimer = 0;
                        while (gui.getwnd("Ore Smelter") == null) {
                            if (!BotUtils.isMoving())
                                unstucktimer++;
                            if (unstucktimer > 50) {
                                System.out.println("Unstucking.");
                                BotUtils.sysLogAppend("Moving char", "white");
                                Gob player = gui.map.player();
                                Coord location = player.rc.floor(posres);
                                int x = location.x + getrandom();
                                int y = location.y + getrandom();
                                Coord finalloc = new Coord(x, y);
                                gameui().map.wdgmsg("click", Coord.z, finalloc, 1, 0);
                                BotUtils.sleep(1000);
                                BotUtils.pfRightClick(gob, 0);
                                unstucktimer = 0;
                            }
                            BotUtils.sleep(50);
                        }
                        int freeslots = BotUtils.invFreeSlots();
                        BotUtils.sleep(500);
                        //  BotUtils.pfRightClick(gob, 0);
                        while (gui.getwnd("Ore Smelter") == null) {
                            if (!BotUtils.isMoving())
                                unstucktimer++;
                            System.out.println("unstucktimer : " + unstucktimer);
                            if (unstucktimer > 50) {
                                System.out.println("Unstucking.");
                                BotUtils.sysLogAppend("Moving char", "white");
                                Gob player = gui.map.player();
                                Coord location = player.rc.floor(posres);
                                int x = location.x + getrandom();
                                int y = location.y + getrandom();
                                Coord finalloc = new Coord(x, y);
                                gameui().map.wdgmsg("click", Coord.z, finalloc, 1, 0);
                                BotUtils.sleep(1000);
                                BotUtils.pfRightClick(gob, 0);
                                unstucktimer = 0;
                            }
                            BotUtils.sleep(50);
                        }
                        for (WItem item : gui.maininv.getItemsPartial("Bloodstone", "Cassiterite", "Chalcopyrite", "Cinnabar", "Malachite",
                                "Heavy Earth", "Iron Ochre", "Black Ore", "Galena", "Silvershrine", "Horn Silver", "Direvein", "Schrifterz", "Leaf Ore", "Meteorite"))
                            item.item.wdgmsg("transfer", Coord.z);
                        int retry = 0;
                        while (freeslots == BotUtils.invFreeSlots()) {
                            retry++;
                            System.out.println("Waiting to transfer/retrying");
                            BotUtils.sleep(100);
                            if (retry > 50) {
                                for (WItem item : gui.maininv.getItemsPartial("Bloodstone", "Cassiterite", "Chalcopyrite", "Cinnabar", "Malachite",
                                        "Heavy Earth", "Iron Ochre", "Black Ore", "Galena", "Silvershrine", "Horn Silver", "Direvein", "Schrifterz", "Leaf Ore", "Meteorite"))
                                    item.item.wdgmsg("transfer", Coord.z);
                                retry = 0;
                            }
                        }
                        BotUtils.sleep(500);

                    /*for (Widget w = BotUtils.playerInventory().child; w != null; w = w.next) {
                        if (w instanceof GItem && ((GItem) w).getname().contains(invobjs)) {
                            GItem item = (GItem) w;
                            try {
                                item.wdgmsg("transfer", Coord.z);
                            } catch (NullPointerException qip) {}
                        }
                    }*/
                    }
                    terminateore = true;
                }
                BotUtils.sysMsg("Done.", Color.white);
                stopbtn.click();
                terminateore = true;
            } catch (Loading loadingerrorslol) {
            }
        }
        }
        private class Runner implements Runnable {
            @Override
            public void run() {
                GameUI gui = gameui();
                countretain = count;
                try {
                        activelist.addAll(list);
                    int remaining = activelist.size() * count;
                    System.out.println("Coal needed : "+remaining);
                    while (!terminaterun) {
                        BotUtils.sysLogAppend("Filling : " + activelist.size() + " Smelters/Ovens.", "white");
                        for (Gob gob : activelist) {

                            int availableFuelCoal = gui.maininv.getItemPartialCount("Coal");
                            int availableFuelBranch = gui.maininv.getItemPartialCount("Branch");

                            if (availableFuelCoal < activelist.size() && countretain == 1)
                                getHQfuel();
                            else if(availableFuelCoal < 24 && availableFuelBranch < 24 && countretain != 1)
                                getfuel();
                            System.out.println("Coal : "+availableFuelCoal+" Branches : "+availableFuelBranch+" count : "+countretain);

                            remaining = remaining - countretain;
                            count = countretain;
                            WItem coalw = gui.maininv.getItemPartial("Coal");
                            WItem coalw2 = gui.maininv.getItemPartial("Branch");
                            if (coalw == null) {
                                if (coalw2 == null) {
                                    gui.error("No fuel found in the inventory");
                                    stopbtn.click();
                                    return;
                                }
                            }
                            if (coalw != null) {
                                BotUtils.pfRightClick(gob,0);

                                String wndname;
                                if(gob.getres().name.contains("smelter"))
                                    wndname = "Ore Smelter";
                                else
                                    wndname = "Oven";

                                int unstucktimer = 0;
                                while(gui.getwnd(wndname) == null){
                                    if(!BotUtils.isMoving())
                                    unstucktimer++;
                                    if(unstucktimer > 250){
                                        BotUtils.sysLogAppend("Moving char", "white");
                                        Gob player = gui.map.player();
                                        Coord location = player.rc.floor(posres);
                                        int x = location.x + getrandom();
                                        int y = location.y + getrandom();
                                        Coord finalloc = new Coord(x, y);
                                        gameui().map.wdgmsg("click", Coord.z, finalloc, 1, 0);
                                        BotUtils.sleep(1000);
                                        BotUtils.pfRightClick(gob,0);
                                        unstucktimer = 0;
                                    }
                                        BotUtils.sleep(10);
                                }



                                GItem coal = coalw.item;
                                coal.wdgmsg("take", new Coord(coal.sz.x / 2, coal.sz.y / 2));
                                int timeout = 0;
                                while (gui.hand.isEmpty() || gui.vhand == null) {
                                    timeout += HAND_DELAY;
                                    if (timeout >= TIMEOUT) {
                                        gui.error("No coal found in the inventory");
                                        stopbtn.click();
                                        return;
                                    }
                                    try {
                                        Thread.sleep(HAND_DELAY);
                                    } catch (InterruptedException e) {
                                        return;
                                    }
                                }
                                coal = gui.vhand.item;
                                for (; count > 0; count--) {
                                    gui.map.wdgmsg("itemact", Coord.z, gob.rc.floor(posres), count == 1 ? 0 : 1, 0, (int) gob.id, gob.rc.floor(posres), 0, -1);
                                    timeout = 0;
                                    while (true) {
                                        WItem newcoal = gui.vhand;
                                        if (newcoal != null && newcoal.item != coal) {
                                            coal = newcoal.item;
                                            break;
                                        } else if (newcoal == null && count == 1) {
                                            break;
                                        }
                                        timeout += HAND_DELAY;
                                        if (timeout >= TIMEOUT) {
                                            gui.error("Not enough coal. Need to add " + (count - 1) + " more.");
                                            stopbtn.click();
                                            return;
                                        }
                                        try {
                                            Thread.sleep(HAND_DELAY);
                                        } catch (InterruptedException e) {
                                            // return;
                                        }
                                    }
                                }
                            }
                            if (coalw2 != null) {
                                BotUtils.pfRightClick(gob,0);
                                String wndname;
                                if(gob.getres().name.contains("smelter"))
                                    wndname = "Ore Smelter";
                                else
                                    wndname = "Oven";

                                int unstucktimer = 0;
                                while(gui.getwnd(wndname) == null){
                                    if(!BotUtils.isMoving())
                                    unstucktimer++;
                                    if(unstucktimer > 250){
                                        BotUtils.sysLogAppend("Moving char", "white");
                                        Gob player = gui.map.player();
                                        Coord location = player.rc.floor(posres);
                                        int x = location.x + getrandom();
                                        int y = location.y + getrandom();
                                        Coord finalloc = new Coord(x, y);
                                        gameui().map.wdgmsg("click", Coord.z, finalloc, 1, 0);
                                        BotUtils.sleep(1000);
                                        BotUtils.pfRightClick(gob,0);
                                        unstucktimer = 0;
                                    }

                                    BotUtils.sleep(10);
                                }

                                GItem coal = coalw2.item;
                                coal.wdgmsg("take", new Coord(coal.sz.x / 2, coal.sz.y / 2));
                                int timeout = 0;
                                while (gui.hand.isEmpty() || gui.vhand == null) {
                                    timeout += HAND_DELAY;
                                    if (timeout >= TIMEOUT) {
                                        gui.error("No coal found in the inventory");
                                        stopbtn.click();
                                        return;
                                    }
                                    try {
                                        Thread.sleep(HAND_DELAY);
                                    } catch (InterruptedException e) {
                                        return;
                                    }
                                }
                                coal = gui.vhand.item;
                                for (; count > 0; count--) {
                                    gui.map.wdgmsg("itemact", Coord.z, gob.rc.floor(posres), count == 1 ? 0 : 1, 0, (int) gob.id, gob.rc.floor(posres), 0, -1);
                                    timeout = 0;
                                    while (true) {
                                        WItem newcoal = gui.vhand;
                                        if (newcoal != null && newcoal.item != coal) {
                                            coal = newcoal.item;
                                            break;
                                        } else if (newcoal == null && count == 1) {
                                            break;
                                        }
                                        timeout += HAND_DELAY;
                                        if (timeout >= TIMEOUT) {
                                            gui.error("Not enough coal. Need to add " + (count - 1) + " more.");
                                            stopbtn.click();
                                            return;
                                        }
                                        try {
                                            Thread.sleep(HAND_DELAY);
                                        } catch (InterruptedException e) {
                                            // return;
                                        }
                                    }
                                }
                            }
                        }
                        count = countretain;
                        BotUtils.sysMsg("Done", Color.white);
                        activelist.clear();
                        stopbtn.click();
                    }
                }catch(NullPointerException ie){}
            }
        }
        private class light implements Runnable {
            @Override
            public void run() {
                GameUI gui = gameui();
                torchlist.addAll(list);
                while (!terminatelight) {
                    for (Gob gob : torchlist) {
                        try {
                            if (list.size() == 0) {
                                BotUtils.sysMsg("No Smelters/Ovens found", Color.white);
                                stopbtn.click();
                                return;
                            }

                            Equipory e = gui.getequipory();
                            WItem l = e.quickslots[6];
                            WItem r = e.quickslots[7];

                            noltorch = true;
                            boolean nortorch = true;

                            if (l != null) {
                                String lname = l.item.getname();
                                if (lname.contains("Lit Torch") || lname.contains("Lantern"))
                                    noltorch = false;
                            }
                            if (r != null) {
                                String rname = r.item.getname();
                                if (rname.contains("Lit Torch") || rname.contains("Lantern"))
                                    nortorch = false;
                            }

                            // take torch from equipment, otherwise assume it's already in the hand
                            if (!noltorch || !nortorch) {
                                WItem w = e.quickslots[noltorch ? 7 : 6];
                                w.mousedown(new Coord(w.sz.x / 2, w.sz.y / 2), 1);

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    e.wdgmsg("drop", noltorch ? 7 : 6);
                                    return;
                                }
                            }
                            BotUtils.pfRightClick(gob, 0);
                            String wndname;
                            if (gob.getres().name.contains("smelter"))
                                wndname = "Ore Smelter";
                            else
                                wndname = "Oven";

                            int unstucktimer = 0;
                            while (gui.getwnd(wndname) == null) {
                                if(!BotUtils.isMoving())
                                unstucktimer++;
                                if (unstucktimer > 250) {
                                    BotUtils.sysLogAppend("Moving char", "white");
                                    Gob player = gui.map.player();
                                    Coord location = player.rc.floor(posres);
                                    int x = location.x + getrandom();
                                    int y = location.y + getrandom();
                                    Coord finalloc = new Coord(x, y);
                                    gameui().map.wdgmsg("click", Coord.z, finalloc, 1, 0);
                                    BotUtils.sleep(1000);
                                    BotUtils.pfRightClick(gob, 0);
                                    unstucktimer = 0;
                                }

                                BotUtils.sleep(10);
                            }

                            gui.map.wdgmsg("itemact", Coord.z, gob.rc.floor(posres), 0, 0, (int) gob.id, gob.rc.floor(posres), 0, -1);

                            if (!Utils.waitForProgressFinish(gui, TIMEOUT_ACT, "Oops something went wrong. Timeout when trying to light with torch.")) {
                                e.wdgmsg("drop", noltorch ? 7 : 6);
                                return;
                            }

                            // e.wdgmsg("drop", noltorch ? 7 : 6);
                        } catch (InterruptedException ie) {
                        }
                        // e.wdgmsg("drop",noltorch ? 7:6);
                    }
                    BotUtils.sysMsg("Done", Color.white);
                    lblc.settext(list.size() + "");
                    torchlist.clear();
                    stopbtn.click();
                }
            }
        }

private class selectingarea implements Runnable {
    @Override
    public void run() {
      //  list.clear();
      //  stockpiles.clear();
        BotUtils.sysMsg("Drag area over smelters/Ovens/LowQ Fuel", Color.WHITE);
        PBotAPI.selectArea();
        //gui.map.PBotAPISelect = true;
        // while(gui.map.PBotAPISelect)
        //BotUtils.sleep(100);
        // BotUtils.sysMsg("Adding", Color.WHITE);
        try {
            selectedAreaA = PBotAPI.getSelectedAreaA();
            selectedAreaB = PBotAPI.getSelectedAreaB();
            for(Gob gob : Smelters())
                if(!list.contains(gob))
                    list.add(gob);
          //  list.addAll(Smelters());
            for(Gob gob : Stockpiles())
                if(!stockpiles.contains(gob))
                    stockpiles.add(gob);
           // stockpiles.addAll(Stockpiles());
            selectedAreaA = null;
            selectedAreaA = null;

            LabelStockpiles.settext(stockpiles.size()+"");
            lblc.settext(list.size() + "");
            biglist.clear();
        }catch(NullPointerException q){
            BotUtils.sysMsg("Error detected, please reopen the bot and try again.",Color.white);
            q.printStackTrace();
        }
    }
}

    private class hqselectingarea implements Runnable {
        @Override
        public void run() {
            hqstockpiles.clear();
            BotUtils.sysMsg("Drag area over High Quality Coal", Color.WHITE);
            PBotAPI.selectArea();
            //gui.map.PBotAPISelect = true;
            // while(gui.map.PBotAPISelect)
            //BotUtils.sleep(100);
            // BotUtils.sysMsg("Adding", Color.WHITE);
            try {
                selectedAreaA = PBotAPI.getSelectedAreaA();
                selectedAreaB = PBotAPI.getSelectedAreaB();
                hqstockpiles.addAll(Stockpiles());
                selectedAreaA = null;
                selectedAreaA = null;
                hqLabelStockpiles.settext(hqstockpiles.size()+"");
                biglist.clear();
            }catch(NullPointerException q){
                BotUtils.sysMsg("Error detected, please reopen the bot and try again.",Color.white);
                q.printStackTrace();
            }
        }
    }
    private class oreselectingarea implements Runnable {
        @Override
        public void run() {
            orestockpiles.clear();
            BotUtils.sysMsg("Drag area over Ore", Color.WHITE);
            PBotAPI.selectArea();
            //gui.map.PBotAPISelect = true;
            // while(gui.map.PBotAPISelect)
            //BotUtils.sleep(100);
            // BotUtils.sysMsg("Adding", Color.WHITE);
            try {
                selectedAreaA = PBotAPI.getSelectedAreaA();
                selectedAreaB = PBotAPI.getSelectedAreaB();
                orestockpiles.addAll(Stockpiles());
                selectedAreaA = null;
                selectedAreaA = null;
                oreLabelStockpiles.settext(orestockpiles.size()+"");
                biglist.clear();
            }catch(NullPointerException q){
                BotUtils.sysMsg("Error detected, please reopen the bot and try again.",Color.white);
                q.printStackTrace();
            }
        }
    }

        private void registerGobSelect () {
            synchronized (GobSelectCallback.class) {
                BotUtils.sysMsg("Registering Gob", Color.white);
                BotUtils.gui.map.registerGobSelect(this);
            }
        }
//    public void areaselect(Coord a, Coord b){
//        BotUtils.sysMsg("Area Select",Color.white);
//        this.a = a.mul(MCache.tilesz2);
//        this.b = b.mul(MCache.tilesz2).add(11, 11);
//        BotUtils.sysMsg("Area selected!", Color.WHITE);
//        BotUtils.gui.map.unregisterAreaSelect();
//        BotUtils.sysMsg("Coord a : "+a+" Coord b : "+b,Color.white);
//    }
private void getfuel() {
    GameUI gui = gameui();
    Glob glob = gui.map.glob;
    for (Gob s : stockpiles) {
        if (terminaterun)
            return;

        // make sure stockpile still exists
        synchronized (glob.oc) {
            if (glob.oc.getgob(s.id) == null)
                continue;
        }


        // navigate to the stockpile and load up on fuel
        gameui().map.pfRightClick(s, -1, 3, 1, null);
        try {
            gui.map.pfthread.join();
        } catch (InterruptedException e) {
            return;
        }

        // return if got enough fuel
        int availableFuelCoal = gui.maininv.getItemPartialCount("Coal");
        int availableFuelBranch = gui.maininv.getItemPartialCount("Branch");
        if (availableFuelCoal >= 50 || availableFuelBranch >= 50)
            return;
    }
}
    private void getHQfuel() {
        GameUI gui = gameui();
        Glob glob = gui.map.glob;
        for (Gob s : hqstockpiles) {
            if (terminaterun)
                return;

            // make sure stockpile still exists
            synchronized (glob.oc) {
                if (glob.oc.getgob(s.id) == null)
                    continue;
            }


            // navigate to the stockpile and load up on fuel
            gameui().map.pfRightClick(s, -1, 3, 1, null);
            try {
                gui.map.pfthread.join();
            } catch (InterruptedException e) {
                return;
            }

            // return if got enough fuel
            int availableFuelCoal = gui.maininv.getItemPartialCount("Coal");
            if (availableFuelCoal >= 50)
                return;
        }
    }

    private void getore() {
        GameUI gui = gameui();
        Glob glob = gui.map.glob;
        for (Gob s : orestockpiles) {
            if (terminateore)
                return;

            // make sure stockpile still exists
            synchronized (glob.oc) {
                if (glob.oc.getgob(s.id) == null)
                    continue;
            }
            int freeslots = BotUtils.invFreeSlots();


            // navigate to the stockpile and load up on fuel
            gameui().map.pfRightClick(s, -1, 3, 1, null);
            try {
                gui.map.pfthread.join();
            } catch (InterruptedException e) {
                return;
            }

            if (BotUtils.invFreeSlots() == freeslots)
            {
                Gob player = gui.map.player();
                Coord location = player.rc.floor(posres);
                int x = location.x + getrandom();
                int y = location.y + getrandom();
                Coord finalloc = new Coord(x, y);
                gameui().map.wdgmsg("click", Coord.z, finalloc, 1, 0);
                BotUtils.sleep(500);
            }

            // return if got enough fuel
            freeslots = BotUtils.invFreeSlots();
            if (freeslots == 0)
                return;
        }
    }

    public void gobselect (Gob gob){
            Resource res = gob.getres();
            if (res != null) {
                if (res.name.equals("gfx/terobjs/smelter") || res.name.equals("gfx/terobjs/oven")) {
                        list.add(gob);
                        lblc.settext(list.size() + "");
                }
                if (res.name.contains("chest")){
                    chest = gob;
                    BotUtils.sysMsg("Chest Selected",Color.white);
                }
            }
        }
        @Override
        public void wdgmsg (Widget sender, String msg, Object...args){
            if (sender == cbtn) {
                if(runner != null)
                    runner.interrupt();
                if(light!=null)
                    light.interrupt();
                terminate();
                terminaterun = true;
                terminatelight = true;
                terminateore = true;
                reqdestroy();
            }
            else
                super.wdgmsg(sender, msg, args);
        }

        @Override
        public boolean type ( char key, KeyEvent ev){
            if (key == 27) {
                if (cbtn.visible) {
                    if(runner != null)
                        runner.interrupt();
                    if(light!=null)
                        light.interrupt();
                    if(autodropper != null)
                        autodropper.interrupt();
                    reqdestroy();
                    terminaterun = true;
                    terminatelight = true;
                    terminateore = true;
                }
                return true;
            }
            return super.type(key, ev);
        }

        public void terminate () {
            terminateore = true;
            terminaterun = true;
            terminatelight =true;
            if (runner != null)
                runner.interrupt();
            if (light != null)
                light.interrupt();
            if(autodropper != null)
                autodropper.interrupt();
          //  this.destroy();
        }
        private List<WItem> getstones (Inventory inv){
        List<WItem> getstones = inv.getItemsPartial("Gneiss");
        return getstones;
    }
    private List<WItem> getbars (Inventory inv){
        List<WItem> getbars = inv.getItemsPartial("Bar");
        return getbars;
    }
    private List<WItem> getsilver (Inventory inv){
        List<WItem> getsilver = inv.getItemsPartial("Quicksilver");
        return getsilver;
    }


    public ArrayList<Gob> Smelters() {
        // Initialises list of crops to harvest between the selected coordinates
        ArrayList<Gob> gobs = new ArrayList<Gob>();
        double bigX = selectedAreaA.x > selectedAreaB.x ? selectedAreaA.x : selectedAreaB.x;
        double smallX = selectedAreaA.x < selectedAreaB.x ? selectedAreaA.x : selectedAreaB.x;
        double bigY = selectedAreaA.y > selectedAreaB.y ? selectedAreaA.y : selectedAreaB.y;
        double smallY = selectedAreaA.y < selectedAreaB.y ? selectedAreaA.y : selectedAreaB.y;
        synchronized (ui.sess.glob.oc) {
            for (Gob gob : ui.sess.glob.oc) {
                if (gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && gob.getres().name.contains("smelter") ||gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && gob.getres().name.contains("oven")) {
                    gobs.add(gob);
                }
            }
        }
        gobs.sort(new CoordSortSmelters());
        return gobs;
    }

    public ArrayList<Gob> Stockpiles() {
        // Initialises list of crops to harvest between the selected coordinates
        ArrayList<Gob> gobs = new ArrayList<Gob>();
        double bigX = selectedAreaA.x > selectedAreaB.x ? selectedAreaA.x : selectedAreaB.x;
        double smallX = selectedAreaA.x < selectedAreaB.x ? selectedAreaA.x : selectedAreaB.x;
        double bigY = selectedAreaA.y > selectedAreaB.y ? selectedAreaA.y : selectedAreaB.y;
        double smallY = selectedAreaA.y < selectedAreaB.y ? selectedAreaA.y : selectedAreaB.y;
        synchronized (ui.sess.glob.oc) {
            for (Gob gob : ui.sess.glob.oc) {
                if (gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && gob.getres().name.contains("branch") || gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && gob.getres().name.contains("coal") || gob.rc.x <= bigX && gob.rc.x >= smallX && gob.getres() != null && gob.rc.y <= bigY
                        && gob.rc.y >= smallY && gob.getres().name.contains("ore")) {
                    gobs.add(gob);
                }
            }
        }
        gobs.sort(new CoordSort());
        return gobs;
    }
    public int getrandom(){
        Random r = new Random();
        int randomNumber = r.ints(1, -6000, 6000).findFirst().getAsInt();
        return randomNumber;
    }

    class CoordSort implements Comparator<Gob> {
        public int compare(Gob a, Gob b) {
            if (a.rc.floor().x == b.rc.floor().x) {
                if (a.rc.floor().x % 2 == 0)
                    return (a.rc.floor().y < b.rc.floor().y) ? 1 : (a.rc.floor().y > b.rc.floor().y) ? -1 : 0;
                else
                    return (a.rc.floor().y < b.rc.floor().y) ? -1 : (a.rc.floor().y > b.rc.floor().y) ? 1 : 0;
            } else
                return (a.rc.floor().x < b.rc.floor().x) ? -1 : (a.rc.floor().x > b.rc.floor().x) ? 1 : 0;
        }
    }
    class CoordSortSmelters implements Comparator<Gob> {
        public int compare(Gob a, Gob b) {
            if (a.rc.floor().y == b.rc.floor().y) {
                if (a.rc.floor().y % 2 == 0)
                    return (a.rc.floor().x < b.rc.floor().x) ? 1 : (a.rc.floor().x > b.rc.floor().x) ? -1 : 0;
                else
                    return (a.rc.floor().x < b.rc.floor().x) ? -1 : (a.rc.floor().x > b.rc.floor().x) ? 1 : 0;
            } else
                return (a.rc.floor().y < b.rc.floor().y) ? -1 : (a.rc.floor().y > b.rc.floor().y) ? 1 : 0;
        }
    }
}
