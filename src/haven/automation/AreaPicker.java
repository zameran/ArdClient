package haven.automation;

import haven.Button;
import haven.CheckListbox;
import haven.CheckListboxItem;
import haven.Config;
import haven.Coord;
import haven.Dropbox;
import haven.GItem;
import haven.GOut;
import haven.Inventory;
import haven.Label;
import haven.ResizableTextEntry;
import haven.Resource;
import haven.Scrollbar;
import haven.Tex;
import haven.Text;
import haven.TextEntry;
import haven.WItem;
import haven.Widget;
import haven.WidgetVerticalAppender;
import haven.Window;
import haven.purus.pbot.PBotGob;
import haven.purus.pbot.PBotGobAPI;
import haven.purus.pbot.PBotInventory;
import haven.purus.pbot.PBotItem;
import haven.purus.pbot.PBotUtils;
import haven.purus.pbot.PBotWindowAPI;
import org.apache.commons.collections4.list.TreeList;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (Beta)
 * Preset = file (JSON)
 * Endless = time
 * Professional options
 * Stop btn, pause btn
 * <p>
 * Main Info + Refresh Button (FlowerMenu)
 * 1. Select area and select gobs . add option check once (not until it disappears) . moving objects? . automilk?
 * 2. FlowerMenu (without flowermenu = just right click?) . favorite?
 * 3. optional. Storage...
 * 4. Storage : select area and chests
 * Drink water? click block for running
 * <p>
 * <p>
 * final. Run
 */

public class AreaPicker extends Window implements Runnable {
    public final static String scriptname = "Area Picker";
    public final static String storagetrigger = "Storage";
    public final static String[] collectstates = new String[]{"Inventory", "Drop out of hand", storagetrigger, "Stockpiles"};
    public final static String[] pfstates = new String[]{"sloth", "purus"}; //both pathfinders are very bad
    public final WidgetVerticalAppender appender = new WidgetVerticalAppender(this);
    public Thread runthread;
    public boolean block = false;

    public final Coord[]
            gobarea = new Coord[2],
            storagearea = new Coord[2];
    public final List<String>
            areagoblist = new ArrayList<>(),
            flowermenulist = new ArrayList<>(Config.flowermenus.keySet()),
            collecttrigger = new ArrayList<>(Arrays.asList(collectstates)),
            areastoragelist = new ArrayList<>(),
            pflist = new ArrayList<>(Arrays.asList(pfstates));
    public final List<PBotGob>
            currentgoblist = new ArrayList<>(),
            currentstoragelist = new ArrayList<>();
    public final List<CheckListboxItem>
            selectedgoblist = new TreeList<>(),
            selectedstoragelist = new TreeList<>(),
            selectedflowerlist = new TreeList<>(),
            selecteditemlist = new TreeList<>();

    public Button refresh, selectgobbtn, selectedgobbtn, selectedflowerbtn, selectstoragebtn, selectedstoragebtn, selecteditembtn, runbtn, stopbtn;
    public Label l1, l2, l3, l4, l5;
    public Label maininfolbl, areagobinfolbl, flowerpetalsinfolbl, areastorageinfolbl, iteminfolbl;
    public Dropbox<String> collecttriggerdbx, pfdbx;
    public Window selectedgobwnd, selectedflowerwnd, selectedstoragewnd, selecteditemwnd;
    public CheckListbox selectedgoblbox, selectedflowerlbox, selectedstoragelbox, selecteditemlbox;
    public TextEntry waitingtimete, selectedgobsearch, selectedflowersearch, selectedstoragesearch, selecteditemsearch;

    public int retry = 5;
    public int waitingtime = 1000;

    public AreaPicker() {
        super(Coord.z, scriptname, scriptname);
    }

    public void added() {
        super.added();

        maininfolbl = new Label("");
        waitingtimete = new ResizableTextEntry(1000 + "") { //for prof settings
            public boolean type(char c, KeyEvent ev) {
                if (c >= KeyEvent.VK_0 && c <= KeyEvent.VK_9 || c == '\b') {
                    return (buf.key(ev));
                } else if (c == '\n') {
                    try {
                        waitingtime = text.equals("") ? 0 : Integer.parseInt(text);
                        return (true);
                    } catch (NumberFormatException ignore) {
                    }
                }
                return (false);
            }
        };

        selectgobbtn = new Button(50, "Select area") {
            {
                change(Color.RED);
            }

            public void click() {
                new Thread(new selectinggobarea(), "Selecting Area").start();
            }

            public boolean mousedown(Coord c, int btn) {
                if (isblocked()) return (true);
                else return (super.mousedown(c, btn));
            }
        };
        selectedgobbtn = new Button(50, "Gob list") {
            {
                change(Color.RED);
            }

            public void click() {
                if (selectedgoblist.size() == 0) {
                    botLogPing("Select area with objects", Color.WHITE);
                } else {
                    if (!ui.gui.containschild(selectedgobwnd))
                        ui.gui.add(selectedgobwnd, c.add(parent.c));
                }
            }
        };
        selectedgobwnd = new Window(Coord.z, "Selecting gob") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            selectedgoblbox = new CheckListbox(50, 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        super.itemclick(itm, button);
                        for (CheckListboxItem i : selectedgoblist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        updatelist("gob");
                        updateinfo("gob");
                    }
                }
            };
            selectedgobsearch = new ResizableTextEntry("") {
                public void changed() {
                    update();
                }

                public boolean mousedown(Coord mc, int btn) {
                    if (btn == 3) {
                        settext("");
                        update();
                        return (true);
                    } else {
                        return (super.mousedown(mc, btn));
                    }
                }

                public void update() {
                    selectedgoblbox.items.clear();
                    for (CheckListboxItem i : selectedgoblist) {
                        if (i.name.toLowerCase().contains(text.toLowerCase()))
                            selectedgoblbox.items.add(i);
                    }
                }
            };
            wva.add(selectedgoblbox);
            wva.add(selectedgobsearch);
            pack();
        }};
        areagobinfolbl = new Label("");

        selectedflowerbtn = new Button(50, "Flower list") {
            public void click() {
                if (!ui.gui.containschild(selectedflowerwnd))
                    ui.gui.add(selectedflowerwnd, c.add(parent.c));
            }
        };
        selectedflowerwnd = new Window(Coord.z, "Selecting petals") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            flowermenulist.forEach(i -> selectedflowerlist.add(new CheckListboxItem(i)));
            ArrayList<String> temp = new ArrayList<>();
            for (String s : flowermenulist) {
                String loc = Resource.language.equals("en") ? s : Resource.getLocString(Resource.BUNDLE_FLOWER, s);
                temp.add(Resource.language.equals("en") ? s : loc.equals(s) ? s : s + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, s) + ")");
            }
            selectedflowerlbox = new CheckListbox(calcWidthString(temp), 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        super.itemclick(itm, button);
                        for (CheckListboxItem i : selectedflowerlist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        updateinfo("flower");
                    }
                }

                protected void drawitemname(GOut g, CheckListboxItem itm) {
                    String loc = Resource.language.equals("en") ? itm.name : Resource.getLocString(Resource.BUNDLE_FLOWER, itm.name);
                    Tex t = Text.render(Resource.language.equals("en") ? itm.name : loc.equals(itm.name) ? itm.name : itm.name + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, itm.name) + ")").tex();
                    g.image(t, new Coord(2, 2), t.sz());
                    t.dispose();
                }
            };
            selectedflowerlbox.items.addAll(selectedflowerlist);
            selectedflowersearch = new ResizableTextEntry("") {
                public void changed() {
                    update();
                }

                public boolean mousedown(Coord mc, int btn) {
                    if (btn == 3) {
                        settext("");
                        update();
                        return (true);
                    } else {
                        return (super.mousedown(mc, btn));
                    }
                }

                public void update() {
                    selectedflowerlbox.items.clear();
                    for (CheckListboxItem i : selectedflowerlist) {
                        String s = Resource.language.equals("en") ? i.name : i.name + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, i.name) + ")";
                        if (s.toLowerCase().contains(text.toLowerCase()))
                            selectedflowerlbox.items.add(i);
                    }
                }
            };
            wva.add(selectedflowerlbox);
            wva.add(selectedflowersearch);
            pack();
        }};
        flowerpetalsinfolbl = new Label("");
        updateinfo("flower");

        collecttriggerdbx = new Dropbox<String>(10, collecttrigger) {
            protected String listitem(int i) {
                return collecttrigger.get(i);
            }

            protected int listitems() {
                return collecttrigger.size();
            }

            protected void drawitem(GOut g, String item, int i) {
                g.text(item, Coord.z);
            }

            public void change(String item) {
                super.change(item);
                int x = Text.render(item).sz().x;
                if (sz.x != x + Dropbox.drop.sz().x + 2) resize(new Coord(x + Dropbox.drop.sz().x + 2, sz.y));
            }

            public boolean mousedown(Coord c, int btn) {
                if (!isblocked()) {
                    super.mousedown(c, btn);
                    if (dl != null) resizedl(collecttrigger);
                }
                return (true);
            }
        };

        selectstoragebtn = new Button(50, "Select area") {
            {
                change(Color.RED);
            }

            public void click() {
                new Thread(new selectingstoragearea(), "Selecting Area").start();
            }

            public boolean mousedown(Coord c, int btn) {
                if (isblocked()) return (true);
                else return (super.mousedown(c, btn));
            }
        };
        selectedstoragebtn = new Button(50, "Storage list") {
            {
                change(Color.RED);
            }

            public void click() {
                if (selectedstoragelist.size() == 0) {
                    botLogPing("Select area with objects", Color.WHITE);
                } else {
                    if (!ui.gui.containschild(selectedstoragewnd))
                        ui.gui.add(selectedstoragewnd, c.add(parent.c));
                }
            }
        };
        selectedstoragewnd = new Window(Coord.z, "Selecting storage") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            selectedstoragelbox = new CheckListbox(50, 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        super.itemclick(itm, button);
                        for (CheckListboxItem i : selectedgoblist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        updatelist("storage");
                        updateinfo("storage");
                    }
                }
            };
            selectedstoragesearch = new ResizableTextEntry("") {
                public void changed() {
                    update();
                }

                public boolean mousedown(Coord mc, int btn) {
                    if (btn == 3) {
                        settext("");
                        update();
                        return (true);
                    } else {
                        return (super.mousedown(mc, btn));
                    }
                }

                public void update() {
                    selectedstoragelbox.items.clear();
                    for (CheckListboxItem i : selectedstoragelist) {
                        if (i.name.toLowerCase().contains(text.toLowerCase()))
                            selectedstoragelbox.items.add(i);
                    }
                }
            };
            wva.add(selectedstoragelbox);
            wva.add(selectedstoragesearch);
            pack();
        }};
        areastorageinfolbl = new Label("");

        selecteditembtn = new Button(50, "Item list") {
            public void click() {
                if (!ui.gui.containschild(selecteditemwnd))
                    ui.gui.add(selecteditemwnd, c.add(parent.c));
            }
        };
        selecteditemwnd = new Window(Coord.z, "Selecting gob") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            stringInInvContent().forEach(i -> selecteditemlist.add(new CheckListboxItem(i)));
            selecteditemlbox = new CheckListbox(50, 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        super.itemclick(itm, button);
                        for (CheckListboxItem i : selecteditemlist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        updateinfo("item");
                    }
                }
            };
            selecteditemlbox.items.addAll(selecteditemlist);
            selecteditemsearch = new ResizableTextEntry("") {
                public void changed() {
                    update();
                }

                public boolean mousedown(Coord mc, int btn) {
                    if (btn == 3) {
                        settext("");
                        update();
                        return (true);
                    } else {
                        return (super.mousedown(mc, btn));
                    }
                }

                public void update() {
                    selecteditemlbox.items.clear();
                    for (CheckListboxItem i : selecteditemlist) {
                        if (i.name.toLowerCase().contains(text.toLowerCase()))
                            selecteditemlbox.items.add(i);
                    }
                }

                public void tick(double dt) {
                    super.tick(dt);
                    if (!isblocked()) {
                        updateitemlist();
                        update();
                        int w = calcWidthCheckListbox(selecteditemlist);
                        if (selecteditemlbox.sz.x != w)
                            selecteditemlbox.resize(w, selectedgoblbox.sz.y);
                        selecteditemwnd.pack();
                    }
                }
            };
            wva.add(selecteditemlbox);
            wva.add(selecteditemsearch);
            pack();
        }};
        iteminfolbl = new Label("");
        updateinfo("item");

        pfdbx = new Dropbox<String>(10, pflist) {
            protected String listitem(int i) {
                return pflist.get(i);
            }

            protected int listitems() {
                return pflist.size();
            }

            protected void drawitem(GOut g, String item, int i) {
                g.text(item, Coord.z);
            }

            public void change(String item) {
                super.change(item);
                int x = Text.render(item).sz().x;
                if (sz.x != x + Dropbox.drop.sz().x + 2) resize(new Coord(x + Dropbox.drop.sz().x + 2, sz.y));
            }

            public boolean mousedown(Coord c, int btn) {
                if (!isblocked()) {
                    super.mousedown(c, btn);
                    if (dl != null) resizedl(pflist);
                }
                return (true);
            }
        };
        runbtn = new Button(50, "Run") {
            public void click() {
                (runthread = new Thread(AreaPicker.this, "Area Collecting")).start();
            }
        };
        stopbtn = new Button(50, "Stop") {
            public void click() {
                try {
                    stop();
                } catch (InterruptedException ignore) {
                }
            }
        };

        appender.setHorizontalMargin(5);
        appender.setVerticalMargin(2);
        appender.addRow(maininfolbl); //refreshbtn for flowermenu
        appender.addRow(l1 = new Label("1. Objects to collect"), selectgobbtn, selectedgobbtn, areagobinfolbl);
        appender.addRow(l2 = new Label("2. Flower Petal"), selectedflowerbtn, flowerpetalsinfolbl);
        appender.addRow(l3 = new Label("3. Storage type"), collecttriggerdbx);
        appender.addRow(l4 = new Label("4. Objects to storage"), selectstoragebtn, selectedstoragebtn, areastorageinfolbl);
        appender.addRow(l5 = new Label("5. Items for storage"), selecteditembtn, iteminfolbl);

        appender.addRow(pfdbx, runbtn);
        add(stopbtn, runbtn.c);
        stopbtn.hide();

        pack();

//        flowermenudbx = new Dropbox<String>(10, flowermenulist) {
//            protected String listitem(int i) {
//                return flowermenulist.get(i);
//            }
//
//            protected int listitems() {
//                return flowermenulist.size();
//            }
//
//            protected void drawitem(GOut g, String item, int i) {
//                String text = Resource.language.equals("en") ? item : item + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, item) + ")";
//                g.text(text, Coord.z);
//            }
//
//            public void change(String item) {
//                super.change(item);
//                String text = Resource.language.equals("en") ? item : item + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, item) + ")";
//                int x = Text.render(text).sz().x;
//                if (sz.x != x + Dropbox.drop.sz().x) resize(new Coord(x + Dropbox.drop.sz().x, sz.y));
//            }
//
//            public boolean mousedown(Coord c, int btn) {
//                if (!isblocked()) {
//                    super.mousedown(c, btn);
//                    List<String> n = new ArrayList<>();
//                    flowermenulist.forEach(s -> n.add(s + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, s) + ")"));
//                    if (dl != null) resizedl(n);
//                }
//                return (true);
//            }
//        };
    }

    public void run() {
        if (checkcollectstate() == -2 && checkcollectstate() == 3) {
            botLogPing("Select another storage type", Color.WHITE);
            return;
        }
        runbtn.hide();
        stopbtn.show();
        boolean ad = Config.autodrink;
        if (ad) Config.autodrink = false;
        block(true);

        collecting();

        runbtn.show();
        stopbtn.hide();
        if (ad) Config.autodrink = true;
        block(false);
        maininfolbl.settext("", Color.WHITE);
    }

    public void stop() throws InterruptedException {
        runthread.interrupt();
        ui.root.wdgmsg("gk", 27);
        sleep(1);
    }

    public void collecting() {
        try {
            List<PBotGob> storages = new ArrayList<>(currentstoragelist);
            for (int p = 0; p < currentgoblist.size(); p++) {
                mark(currentgoblist.get(p));
                for (int i = 0; i < retry; i++) {
                    botLog("Gob is " + (p + 1) + " of " + currentgoblist.size() + ". Try is " + (i + 1) + " of " + retry, Color.YELLOW);
                    byte cr = checkcollectstate();
                    if (cr == -1 || cr == 0) {
                        if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                            botLog("Not enough space for item. Stopping...", Color.WHITE);
                            stop();
                        }
                    } else if (cr == 1) {
                        if (PBotUtils.getItemAtHand(ui) != null) {
                            botLog("Dropping...", Color.WHITE);
                            if (!dropItemFromHand()) {
                                botLog("Can't drop. Stopping...", Color.WHITE);
                                stop();
                            }
                        }
                    } else if (cr == 2) {
                        if (storages.size() == 0) {
                            botLog("Storages is full", Color.WHITE);
                            if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                                botLog("Not enough space for item. Stopping...", Color.WHITE);
                                stop();
                            }
                        }
                        if (PBotUtils.getItemAtHand(ui) != null) {
                            botLog("Dropping...", Color.WHITE);
                            if (!dropItemFromHand()) {
                                botLog("Can't drop. Stopping...", Color.WHITE);
                                stop();
                            }
                        }
                        PBotItem bigitem = getMaxSizeItem(getInvItems(selecteditemlist));
                        if (bigitem != null && PBotUtils.playerInventory(ui).freeSpaceForItem(bigitem) == null) {
                            storages = storaging(storages);
                        }
                    }
                    if (PBotGobAPI.findGobById(ui, currentgoblist.get(p).getGobId()) == null) {
                        botLog("Object not found. Skipping...", Color.WHITE);
                        break;
                    }

                    if (pfRightClick(currentgoblist.get(p))) {
                        waitForFlowerMenu();
                        if (petalExists()) {
                            if (choosePetal(selectedflowerlist)) {
                                if (!waitFlowermenuClose()) {
                                    botLog("Can't close the flowermenu", Color.WHITE);
                                    stop();
                                }
                                waitMoving();
                                byte wr = waitForHourglass();
                                if (wr == 1)
                                    botLog("hourglass is finish", Color.WHITE);
                                else if (wr == 0)
                                    botLog("hourglass timeout", Color.WHITE);
                                else if (wr == 2) {
                                    botLog("hourglass stopped. folding", Color.WHITE);
                                    storages = storaging(storages);
                                }
                            } else {
                                if (!closeFlowermenu()) {
                                    botLog("Can't close the flowermenu", Color.WHITE);
                                    stop();
                                } else
                                    break;
                            }
                        } else
                            break;
                    } else
                        break;
                    sleep(1);
                }
            }

            byte cr = checkcollectstate();
            if (cr == 2) {
                if (storages.size() == 0) {
                    botLog("Storages is full", Color.WHITE);
                    if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                        botLog("Not enough space for item. Stopping...", Color.WHITE);
                        stop();
                    }
                }
                if (PBotUtils.getItemAtHand(ui) != null) {
                    botLog("Dropping...", Color.WHITE);
                    if (!dropItemFromHand()) {
                        botLog("Can't drop. Stopping...", Color.WHITE);
                        stop();
                    }
                }
                storaging(storages);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        botLogPing("Finish!", Color.GREEN);
    }

    public List<PBotGob> storaging(List<PBotGob> storages) throws InterruptedException {
        List<PBotGob> output = new ArrayList<>(storages);
        for (int p = 0; p < storages.size(); p++) {
            mark(storages.get(p));
            for (int i = 0; i < retry; i++) {
                botLog("Storage is " + (p + 1) + " of " + storages.size() + ". Try is " + (i + 1) + " of " + retry, Color.YELLOW);
                if (PBotGobAPI.findGobById(ui, storages.get(p).getGobId()) == null) {
                    botLog("Object not found. Skipping...", Color.WHITE);
                    break;
                }
                List<Window> ow = invWindows();
                if (pfRightClick(storages.get(p))) {
                    Window w = waitForNewInvWindow(ow);
                    if (w != null) {
                        PBotInventory wi = PBotWindowAPI.getInventory(w);
                        if (wi.freeSlotsInv() > 0) {
                            List<PBotItem> items = getInvItems(selecteditemlist);
                            for (PBotItem pitem : items) {
                                if (!PBotUtils.playerInventory(ui).inv.containschild(pitem.gitem)) continue;
                                if (wi.freeSlotsInv() == 0) {
                                    break;
                                }
                                if (wi.freeSpaceForItem(pitem) != null) {
                                    int invitems = wi.inv.getchilds(GItem.class).size();
                                    pitem.transferItem();
                                    while (wi.inv.getchilds(GItem.class).size() == invitems) {
                                        sleep(25);
                                    }
                                }
                            }
                            if (getInvItems(selecteditemlist).size() == 0) {
                                PBotWindowAPI.closeWindow(w);
                                waitForWindowClose(w);
                                return (output);
                            }
                        }
                        if (wi.freeSlotsInv() == 0) {
                            PBotWindowAPI.closeWindow(w);
                            waitForWindowClose(w);
                            output.remove(storages.get(p));
                            break;
                        }
                    } else
                        break;
                } else
                    break;
                sleep(1);
            }
        }
        return (output);
    }


    public boolean pfRightClick(PBotGob pgob) throws InterruptedException {
        botLog("pathfinding " + pgob + "...", Color.WHITE);
        for (int i = 0; i < retry; i++) {
            botLog("try " + (i + 1) + " of " + retry + " pathfinding " + pgob + "...", Color.WHITE);
            boolean yea;

            if (checkpf() != 1) {
                yea = ui.gui.map.pathto(pgob.gob);
                while (ui.gui != null && ui.gui.map != null && !ui.gui.map.isclearmovequeue())
                    sleep(10);
            } else {
                yea = pgob.pfClick(1, 0);
            }

            if (yea) {
                botLog("path found", Color.GREEN);
                pgob.doClick(3, 0);
                return (true);
            } else
                botLog("path not found", Color.RED);
        }
        return (false);
    }

    public void waitMoving() throws InterruptedException {
        botLog("moving...", Color.WHITE);
        while (PBotGobAPI.player(ui).isMoving()) {
            sleep(10);
        }
        botLog("move stop", Color.WHITE);
    }

    public boolean choosePetal(List<CheckListboxItem> list) throws InterruptedException {
        botLog("petal choosing...", Color.WHITE);
        ArrayList<String> temp = new ArrayList<>();
        for (CheckListboxItem item : list) {
            if (item.selected)
                temp.add(item.name);
        }
        for (int i = 0, sleep = 10; i < waitingtime; i += sleep) {
            for (String item : temp) {
                if (PBotUtils.choosePetal(ui, item)) {
                    botLog("choosePetal " + item + " true", Color.RED);
                    return (true);
                }
            }
            sleep(sleep);
        }
        botLog("choosePetals " + temp.toString() + " false", Color.RED);
        return (false);
    }

    public byte waitForHourglass() throws InterruptedException {
        botLog("hourglass waiting...", Color.WHITE);
        double prog = ui.gui.prog;
        int retries = 0;
        while (prog == ui.gui.prog) {
            if (retries > waitingtime / 5)
                return (0);
            retries++;
            prog = ui.gui.prog;
            sleep(5);
        }
        while (ui.gui.prog >= 0) {
            if (checkcollectstate() == -1 || checkcollectstate() == 0) {
                if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                    botLog("Not enough space for item. Stopping...", Color.WHITE);
                    stop();
                    return (-1);
                }
            }
            if (checkcollectstate() == 1) {
                if (PBotUtils.getItemAtHand(ui) != null) {
                    botLog("Dropping...", Color.WHITE);
                    if (!dropItemFromHand()) {
                        botLog("Can't drop. Stopping...", Color.WHITE);
                        stop();
                        return (-1);
                    }
                }
            }
            if (checkcollectstate() == 2) {
                if (PBotUtils.getItemAtHand(ui) != null) {
                    botLog("Dropping...", Color.WHITE);
                    if (!dropItemFromHand()) {
                        botLog("Can't drop. Stopping...", Color.WHITE);
                        stop();
                        return (-1);
                    }
                }
                PBotItem bigitem = getMaxSizeItem(getInvItems(selecteditemlist));
                if (bigitem != null && PBotUtils.playerInventory(ui).freeSpaceForItem(bigitem) == null) {
                    botLog("Not enough space for item. Folding...", Color.WHITE);
                    ui.root.wdgmsg("gk", 27);
                    return (2);
                }
            }
            sleep(25);
        }
        return (1);
    }

    public boolean waitFlowermenuClose() {
        botLog("flowermenu closing waiting...", Color.WHITE);
        for (int i = 0; i < retry; i++) {
            if (PBotUtils.waitFlowermenuClose(ui, waitingtime)) {
                botLog("flowermenu closed", Color.WHITE);
                return (true);
            } else
                botLog("flowermenu didn't close", Color.WHITE);
        }

        return (false);
    }

    public boolean waitForWindowClose(Window w) throws InterruptedException {
        botLog("window closing waiting...", Color.WHITE);
        for (int i = 0, sleep = 50; i < waitingtime && ui.gui.getwnd(w) != null; i += sleep) {
            sleep(sleep);
        }
        if (ui.gui.getwnd(w) == null) {
            botLog("window closed", Color.WHITE);
            return (true);
        } else {
            botLog("window didn't close", Color.WHITE);
            return (false);
        }
    }

    public boolean closeFlowermenu() {
        botLog("flowermenu closing...", Color.WHITE);
        for (int i = 0; i < retry; i++) {
            if (PBotUtils.closeFlowermenu(ui, waitingtime)) {
                botLog("flowermenu closed", Color.WHITE);
                return (true);
            } else
                botLog("flowermenu didn't close", Color.WHITE);
        }
        return (false);
    }

    public boolean petalExists() {
        botLog("petal checking...", Color.WHITE);
        boolean r = PBotUtils.petalExists(ui);
        if (r)
            botLog("petal found", Color.WHITE);
        else
            botLog("petal not found", Color.WHITE);
        return (r);
    }

    public boolean waitForFlowerMenu() {
        botLog("flowermenu opening waiting...", Color.WHITE);
        if (checkflowers().size() == 0) {
            botLog("flowermenu not required", Color.WHITE);
            return (false);
        }
        boolean r = PBotUtils.waitForFlowerMenu(ui, waitingtime);
        if (r)
            botLog("flowermenu opened", Color.WHITE);
        else
            botLog("flowermenu didn't open", Color.WHITE);
        return (r);
    }

    public List<Window> invWindows() {
        List<Window> iwnds = new ArrayList<>();
        for (Window w : ui.gui.getchilds(Window.class)) {
            if (w.getchild(Inventory.class) != null)
                iwnds.add(w);
        }
        return (iwnds);
    }

    public Window waitForNewInvWindow(List<Window> ows) throws InterruptedException {
        botLog("inventory window opening waiting...", Color.WHITE);
        for (int i = 0, sleep = 10; i < waitingtime; i += sleep) {
            List<Window> iwnds = invWindows();
            if (ows.size() < iwnds.size()) {
                for (Window iw : iwnds) {
                    boolean eq = false;
                    for (Window ow : ows)
                        if (iw.equals(ow)) {
                            eq = true;
                            break;
                        }
                    if (!eq) {
                        botLog("inventory window opened", Color.WHITE);
                        return (iw);
                    }
                }
            } else {
                sleep(sleep);
            }
        }
        botLog("inventory window didn't open", Color.WHITE);
        return (null);
    }

    public void selectArea() {
        botLogPing("Please select an area by dragging!", Color.WHITE);
        ui.gui.map.PBotAPISelect = true;
        while (ui.gui.map.PBotAPISelect) {
            PBotUtils.sleep(25);
        }
    }

    public void sleep(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    public boolean freeSlots() throws InterruptedException {
        //botLog("free slots checking...", Color.WHITE);
        boolean free = false;
        int slots = -1;
        while (slots == -1) {
            Inventory inv = PBotUtils.playerInventory(ui).inv;

            int takenSlots = 0;
            for (Widget i = inv.child; i != null; i = i.next) {
                if (i instanceof WItem) {
                    WItem buf = (WItem) i;
                    int s = 0;
                    for (int t = 0, sleep = 10; s == 0 && t < waitingtime; t += sleep) {
                        s = buf.size().x * buf.size().y;
                        sleep(sleep);
                    }
                    takenSlots += s;
                }
            }
            int allSlots = inv.isz.x * inv.isz.y;
            slots = allSlots - takenSlots;

            if (slots > 0) free = true;
            //botLog("free slots checked " + slots, Color.WHITE);
        }
        //botLog("free slots " + free, Color.WHITE);
        return free;
    }

    public void botLog(String msg, Color clr) {
        try {
            maininfolbl.settext(msg, clr);
            System.out.println(msg);
            ui.gui.botlog.append(msg, clr);
        } catch (Exception ignore) {
        }
    }

    public void botLogPing(String msg, Color clr) {
        try {
            ui.gui.botmsg(msg, clr);
        } catch (Exception ignore) {
        }
    }

    public byte checkcollectstate() {
        if (collecttriggerdbx.sel == null) return (-1);
        for (int i = 0; i < collectstates.length; i++) {
            if (collecttriggerdbx.sel.equals(collectstates[i])) return ((byte) i);
        }
        return (-2);
    }

    public byte checkpf() {
        if (pfdbx.sel == null) return (-1);
        for (int i = 0; i < collectstates.length; i++) {
            if (pfdbx.sel.equals(pfstates[i])) return ((byte) i);
        }
        return (-2);
    }

    public ArrayList<String> checkflowers() {
        ArrayList<String> temp = new ArrayList<>();
        for (CheckListboxItem item : selectedflowerlist) {
            if (item.selected)
                temp.add(item.name);
        }
        return (temp);
    }

    public ArrayList<String> checkitems() {
        ArrayList<String> temp = new ArrayList<>();
        for (CheckListboxItem item : selecteditemlist) {
            if (item.selected)
                temp.add(item.name);
        }
        return (temp);
    }

    public boolean dropItemFromHand() {
        botLog("dropping...", Color.WHITE);
        for (int i = 0; i < retry; i++) {
            if (PBotUtils.dropItemFromHand(ui, 0, 200)) {
                botLog("dropped", Color.WHITE);
                return (true);
            } else
                botLog("dropping failed", Color.WHITE);
        }
        return (false);
    }

    public List<PBotItem> getInvItems(List<CheckListboxItem> clist) {
        List<String> list = new ArrayList<>();
        for (CheckListboxItem i : clist) {
            if (i.selected)
                list.add(i.name);
        }
        return PBotUtils.playerInventory(ui).getInventoryContainsResnames(list);
    }

    public PBotItem getMaxSizeItem(List<PBotItem> list) {
        PBotItem big = null;
        for (PBotItem i : list) {
            if (big == null) big = i;
            else if (big.witem.size().x < i.witem.size().x || big.witem.size().y < i.witem.size().y) big = i;
        }
        return big;
    }

    public void draw(GOut g) {
        if (collecttriggerdbx != null && l4 != null && selectstoragebtn != null && selectedstoragebtn != null && areastorageinfolbl != null && l5 != null && selecteditembtn != null && iteminfolbl != null) {
            byte cr = checkcollectstate();
            if (cr == -1 || cr == 0 || cr == 1) {
                if (l4.visible) l4.hide();
                if (selectstoragebtn.visible) selectstoragebtn.hide();
                if (selectedstoragebtn.visible) selectedstoragebtn.hide();
                if (areastorageinfolbl.visible) areastorageinfolbl.hide();
                if (l5.visible) l5.hide();
                if (selecteditembtn.visible) selecteditembtn.hide();
                if (iteminfolbl.visible) iteminfolbl.hide();
            } else if (cr == 2 || cr == 3) {
                if (!l4.visible) l4.show();
                if (!selectstoragebtn.visible) selectstoragebtn.show();
                if (!selectedstoragebtn.visible) selectedstoragebtn.show();
                if (!areastorageinfolbl.visible) areastorageinfolbl.show();
                if (!l5.visible) l5.show();
                if (!selecteditembtn.visible) selecteditembtn.show();
                if (!iteminfolbl.visible) iteminfolbl.show();
            }
        }
        super.draw(g);
    }

    public class selectinggobarea implements Runnable {
        @Override
        public void run() {
            selectArea();
            gobarea[0] = PBotUtils.getSelectedAreaA();
            gobarea[1] = PBotUtils.getSelectedAreaB();

            areagoblist.clear();
            areagoblist.addAll(stringInArea(gobarea));

            selectedgoblist.clear();
            selectedgoblbox.items.clear();
            areagoblist.forEach(i -> selectedgoblist.add(new CheckListboxItem(i)));
            selectedgoblbox.items.addAll(selectedgoblist);
            selectedgoblbox.resize(calcWidthCheckListbox(selectedgoblist), selectedgoblbox.sz.y);
            selectedgobwnd.pack();
            selectedgobsearch.settext("");
            if (selectedgoblbox.items.size() > 0) selectedgobbtn.change(Color.GREEN);
            else selectedgobbtn.change(Color.RED);

            selectgobbtn.change(Color.GREEN);

            updateinfo("gob");
            pack();
        }
    }

    public class selectingstoragearea implements Runnable {
        @Override
        public void run() {
            selectArea();
            storagearea[0] = PBotUtils.getSelectedAreaA();
            storagearea[1] = PBotUtils.getSelectedAreaB();

            areastoragelist.clear();
            areastoragelist.addAll(stringInArea(storagearea));

            selectedstoragelist.clear();
            selectedstoragelbox.items.clear();
            areastoragelist.forEach(i -> selectedstoragelist.add(new CheckListboxItem(i)));
            selectedstoragelbox.items.addAll(selectedstoragelist);
            selectedstoragelbox.resize(calcWidthCheckListbox(selectedstoragelist), selectedstoragelbox.sz.y);
            selectedstoragewnd.pack();
            selectedstoragesearch.settext("");
            if (selectedstoragelbox.items.size() > 0) selectedstoragebtn.change(Color.GREEN);
            else selectedstoragebtn.change(Color.RED);

            selectstoragebtn.change(Color.GREEN);

            updateinfo("storage");
            pack();
        }
    }

    public ArrayList<String> stringInArea(Coord[] a) {
        ArrayList<PBotGob> gobs = PBotUtils.gobsInArea(ui, a[0], a[1]);
        ArrayList<String> strings = new ArrayList<>();

        for (PBotGob pgob : gobs) {
            if (pgob.getResname() != null && !strings.contains(pgob.getResname()))
                strings.add(pgob.getResname());
        }

        return strings;
    }

    public List<String> stringInInvContent() {
        ArrayList<String> strings = new ArrayList<>();
        for (PBotInventory inv : PBotUtils.getAllInventories(ui)) {
            List<PBotItem> items = inv.getInventoryContents();

            for (PBotItem ptem : items) {
                if (ptem.getResname() != null && !strings.contains(ptem.getResname()))
                    strings.add(ptem.getResname());
            }
        }

        return strings;
    }

    public void updateitemlist() {
        List<String> items = stringInInvContent();
        for (String s : items) {
            boolean contains = false;
            for (CheckListboxItem i : selecteditemlist) {
                if (s.equalsIgnoreCase(i.name)) {
                    contains = true;
                    break;
                }
            }
            if (!contains)
                selecteditemlist.add(new CheckListboxItem(s));
        }
    }

    public void areainfo(Label lbl, Coord[] area, List<CheckListboxItem> checklist) {
        StringBuilder sb = new StringBuilder();
        int x = Math.abs(area[1].x - area[0].x) / 11;
        int y = Math.abs(area[1].y - area[0].y) / 11;
        sb.append("Area ").append(x).append("x").append(y);
        if (checklist.size() > 0) {
            sb.append(" : ");
            int o = 0;
            for (CheckListboxItem item : checklist)
                if (item.selected)
                    o += currentList(PBotUtils.gobsInArea(ui, area[0], area[1]), item.name).size();
            sb.append(o).append(" selected objects");
        }
        if (lbl != null) {
            lbl.settext(sb.toString());
            pack();
        }
    }

    public void updateinfo(String type) {
        switch (type) {
            case "gob":
                areainfo(areagobinfolbl, gobarea, selectedgoblist);
                break;
            case "storage":
                areainfo(areastorageinfolbl, storagearea, selectedstoragelist);
                break;
            case "flower":
                flowerpetalsinfolbl.settext(checkflowers().toString());
                pack();
                break;
            case "item":
                iteminfolbl.settext(checkitems().toString());
                pack();
                break;
        }
    }

    public void updatelist(String type) {
        if (type.equals("gob")) {
            currentgoblist.clear();
            for (CheckListboxItem item : selectedgoblist) {
                if (item.selected)
                    currentgoblist.addAll(currentList(PBotUtils.gobsInArea(ui, gobarea[0], gobarea[1]), item.name));
            }
        } else if (type.equals("storage")) {
            currentstoragelist.clear();
            for (CheckListboxItem item : selectedstoragelist) {
                if (item.selected)
                    currentstoragelist.addAll(currentList(PBotUtils.gobsInArea(ui, storagearea[0], storagearea[1]), item.name));
            }
        }
    }

    public void block(boolean state) {
        block = state;
    }

    public boolean isblocked() {
        return block;
    }

    public void mark(PBotGob pgob) {
        pgob.gob.mark(5000);
    }


    public Coord calcDropboxSize(List<String> list) {
        Optional<Integer> ow = list.stream().map((v) -> Text.render(v).sz().x).collect(Collectors.toList()).stream().reduce(Integer::max);
        int w = Dropbox.drop.sz().x + 5;
        if (ow.isPresent() && list.size() > 0)
            w += ow.get();
        int h = Math.max(list.size() > 0 ? Text.render(list.get(0)).sz().y : 0, 16);
        return new Coord(w, h);
    }

    public int calcWidthCheckListbox(List<CheckListboxItem> list) {
        Optional<Integer> ow = list.stream().map((v) -> Text.render(v.name).sz().x).collect(Collectors.toList()).stream().reduce(Integer::max);
        int w = Scrollbar.sflarp.sz().x + CheckListbox.chk.sz().x + 5;
        if (ow.isPresent() && list.size() > 0)
            w += ow.get();
        return (w);
    }

    public int calcWidthString(List<String> list) {
        Optional<Integer> ow = list.stream().map((v) -> Text.render(v).sz().x).collect(Collectors.toList()).stream().reduce(Integer::max);
        int w = Scrollbar.sflarp.sz().x + CheckListbox.chk.sz().x + 5;
        if (ow.isPresent() && list.size() > 0)
            w += ow.get();
        return (w);
    }

    public List<PBotGob> currentList(List<PBotGob> list, String item) {
        List<PBotGob> total = new ArrayList<>();
        for (PBotGob pgob : list) {
            String s = pgob.getResname();
            if (s != null && s.equals(item))
                total.add(pgob);
        }
        return total;
    }

    public void destroy() {
        super.destroy();
        if (runthread != null && runthread.isAlive())
            runthread.interrupt();
    }


//    public Dropbox<String> dropbox(List<String> list, String type) {
//        Coord size = calcDropboxSize(list);
//        return new Dropbox<String>(size.x, 10, size.y) {
//            protected String listitem(int i) {
//                return list.get(i);
//            }
//
//            protected int listitems() {
//                return list.size();
//            }
//
//            protected void drawitem(GOut g, String item, int i) {
//                g.text(item, Coord.z);
//            }
//
//            public void change(String item) {
//                super.change(item);
//                resize(Text.render(item).sz().x + drop.sz().x + 2, calcDropboxSize(list).y);
//                updatelist(type);
//                updateinfo(type);
//            }
//
//            public boolean mousedown(Coord c, int btn) {
//                if (!isblocked()) {
//                    super.mousedown(c, btn);
//                    if (dl != null) resizedl(list);
//                }
//                return (true);
//            }
//        };
//    }
}
