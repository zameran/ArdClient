package haven.automation;

import haven.Button;
import haven.CheckListbox;
import haven.CheckListboxItem;
import haven.Config;
import haven.Coord;
import haven.Coord2d;
import haven.Dropbox;
import haven.FlowerMenu;
import haven.GItem;
import haven.GOut;
import haven.ISBox;
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
import haven.purus.DrinkWater;
import haven.purus.pbot.PBotCharacterAPI;
import haven.purus.pbot.PBotGob;
import haven.purus.pbot.PBotGobAPI;
import haven.purus.pbot.PBotInventory;
import haven.purus.pbot.PBotItem;
import haven.purus.pbot.PBotUtils;
import haven.purus.pbot.PBotWindowAPI;
import haven.sloth.script.pathfinding.Hitbox;
import modification.configuration;
import org.apache.commons.collections4.list.TreeList;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (Beta)
 * Preset = file (JSON)
 * Endless = time
 * Professional options (drink . dead option . wait time . retry .)
 * Stop btn, pause btn
 * <p>
 * Main Info + Refresh Button (FlowerMenu)
 * 1. Select area and select gobs . add option check once (not until it disappears) . moving objects? . automilk?
 * 2. FlowerMenu (without flowermenu = just right click?) . favorite? . disable popup . sequence (1 2 3) . filter selected gobs (auto click all gobs and check)
 * 3. optional. Storage...
 * 4. Storage : select area and chests
 * Drink water? click block for running
 * <p>
 * <p>
 * final. Run
 */

public class AreaPicker extends Window implements Runnable {
    public final static String scriptname = "Area Picker";
    public final static String[] collectstates = new String[]{"Inventory", "Drop out of hand", "Storage", "Create Stockpiles (WIP)"};
    public final WidgetVerticalAppender appender = new WidgetVerticalAppender(this);
    public Thread runthread;
    public boolean block = false;
    public String barrel; //gfx/terobjs/barrel
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public final Coord[]
            gobarea = new Coord[2],
            storagearea = new Coord[2];
    public final List<String>
            areagoblist = new ArrayList<>(),
            flowermenulist = new ArrayList<>(Config.flowermenus.keySet()),
            collecttrigger = new ArrayList<>(Arrays.asList(collectstates)),
            areastoragelist = new ArrayList<>(),
            addeditemlist = new ArrayList<>();
    public final List<PBotGob>
            currentgoblist = new ArrayList<>(),
            currentstoragelist = new ArrayList<>();
    public final List<CheckListboxItem>
            selectedgoblist = new TreeList<>(),
            selectedstoragelist = new TreeList<>(),
            selectedflowerlist = new ArrayList<>(),
            selecteditemlist = new TreeList<>();


    public Button refresh, selectgobbtn, selectedgobbtn, selectedflowerbtn, selectstoragebtn, selectedstoragebtn, selecteditembtn, selecteditemaddbtn, runbtn, stopbtn, pausumebtn;
    public Label l1, l2, l3, l4, l5;
    public Label maininfolbl, areagobinfolbl, flowerpetalsinfolbl, areastorageinfolbl, iteminfolbl;
    public Dropbox<String> collecttriggerdbx;
    public Window selectedgobwnd, selectedflowerwnd, selectedstoragewnd, selecteditemwnd;
    public CheckListbox selectedgoblbox, selectedflowerlbox, selectedstoragelbox, selecteditemlbox;
    public TextEntry waitingtimete, selectedgobsearch, selectedflowersearch, selectedstoragesearch, selecteditemsearch, selecteditemaddtext;

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
                    else
                        selectedgobwnd.reqdestroy();
                }
            }
        };
        selectedgobwnd = new Window(Coord.z, "Selecting gob") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            selectedgoblbox = new CheckListbox(100, 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        super.itemclick(itm, button);
                        for (CheckListboxItem i : selectedgoblist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        items.sort(listboxsort());
                        updatelist("gob");
                        updateinfo("gob");
                    }
                }
                protected void drawitemname(GOut g, CheckListboxItem itm) {
                    Text t = Text.render(configuration.getShortName(itm.name) + " (" + itm.name.substring(0, itm.name.lastIndexOf('/')) + ")");
                    Tex T = t.tex();
                    g.image(T, new Coord(2, 2), t.sz());
                    T.dispose();
                }
            };
            selectedgobsearch = new ResizableTextEntry(selectedgoblbox.sz.x, "") {
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
                    selectedgoblbox.items.sort(listboxsort());
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
                else
                    selectedflowerwnd.reqdestroy();
            }
        };
        selectedflowerwnd = new Window(Coord.z, "Selecting petals") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            flowermenulist.forEach((i) -> selectedflowerlist.add(new CheckListboxItem(i)));
            ArrayList<String> temp = new ArrayList<>();
            flowermenulist.forEach((s) -> {
                String loc = Resource.language.equals("en") ? s : Resource.getLocString(Resource.BUNDLE_FLOWER, s);
                temp.add(Resource.language.equals("en") ? s : loc.equals(s) ? s : s + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, s) + ")");
            });
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
                        items.sort(listboxsort());
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
            selectedflowerlbox.items.sort(listboxsort());
            selectedflowersearch = new ResizableTextEntry(selectedflowerlbox.sz.x, "") {
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
                    selectedflowerlbox.items.sort(listboxsort());
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
                    else
                        selectedstoragewnd.reqdestroy();
                }
            }
        };
        selectedstoragewnd = new Window(Coord.z, "Selecting storage") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            selectedstoragelbox = new CheckListbox(100, 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        super.itemclick(itm, button);
                        for (CheckListboxItem i : selectedgoblist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        items.sort(listboxsort());
                        updatelist("storage");
                        updateinfo("storage");
                    }
                }
                protected void drawitemname(GOut g, CheckListboxItem itm) {
                    Text t = Text.render(configuration.getShortName(itm.name) + " (" + itm.name.substring(0, itm.name.lastIndexOf('/')) + ")");
                    Tex T = t.tex();
                    g.image(T, new Coord(2, 2), t.sz());
                    T.dispose();
                }
            };
            selectedstoragesearch = new ResizableTextEntry(selectedstoragelbox.sz.x, "") {
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
                    selectedstoragelbox.items.sort(listboxsort());
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
                else
                    selecteditemwnd.reqdestroy();
            }
        };
        selecteditemwnd = new Window(Coord.z, "Selecting gob") {{
            WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
            stringInInvContent().forEach(i -> selecteditemlist.add(new CheckListboxItem(i)));
            selecteditemlbox = new CheckListbox(100, 10) {
                protected void itemclick(CheckListboxItem itm, int button) {
                    if (!isblocked()) {
                        if (button == 3) {
                            selecteditemlist.remove(itm);
                            selecteditemlbox.items.remove(itm);
                            addeditemlist.remove(itm.name);
                        } else {
                            super.itemclick(itm, button);
                        }
                        for (CheckListboxItem i : selecteditemlist) {
                            if (i.name.equals(itm.name)) {
                                i.selected = itm.selected;
                                break;
                            }
                        }
                        items.sort(listboxsort());
                        updateinfo("item");
                    }
                }
            };
            selecteditemlbox.items.addAll(selecteditemlist);
            selecteditemlbox.items.sort(listboxsort());
            selecteditemsearch = new ResizableTextEntry(selecteditemlbox.sz.x, "") {
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
                    selecteditemlbox.items.sort(listboxsort());
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
            selecteditemaddtext = new ResizableTextEntry(selecteditemlbox.sz.x / 2, "") {
                public boolean mousedown(Coord mc, int btn) {
                    if (!isblocked()) {
                        if (btn == 3) {
                            settext("");
                            return (true);
                        }
                    }
                    return (super.mousedown(mc, btn));
                }

                public boolean type(char c, KeyEvent ev) {
                    if (!isblocked()) {
                        if (c == '\n' && !text.equals("")) {
                            addeditemlist.add(text);
                        } else {
                            return buf.key(ev);
                        }
                    }
                    return (true);
                }
            };
            selecteditemaddbtn = new Button(50, "Add") {
                public void click() {
                    if (!isblocked()) {
                        if (!selecteditemaddtext.text.equals("")) {
                            addeditemlist.add(selecteditemaddtext.text);
                        }
                    }
                }
            };
            wva.add(selecteditemlbox);
            wva.add(selecteditemsearch);
            wva.addRow(selecteditemaddtext, selecteditemaddbtn);
            pack();
        }};
        iteminfolbl = new Label("");
        updateinfo("item");

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
        pausumebtn = new Button(50, "Pause") {
            public void click() {
                if (runthread.isAlive()) {
                    if (runthread.isInterrupted()) {
                        resume();
                    } else {
                        pause();
                    }
                }
            }
        };

        appender.setHorizontalMargin(5);
        appender.setVerticalMargin(2);
        appender.addRow(maininfolbl);
        appender.addRow(l1 = new Label("1. Objects to collect"), selectgobbtn, selectedgobbtn, areagobinfolbl);
        appender.addRow(l2 = new Label("2. Flower Petal"), selectedflowerbtn, flowerpetalsinfolbl);
        appender.addRow(l3 = new Label("3. Storage type"), collecttriggerdbx);
        appender.addRow(l4 = new Label("4. Objects to storage"), selectstoragebtn, selectedstoragebtn, areastorageinfolbl);
        appender.addRow(l5 = new Label("5. Items for storage"), selecteditembtn, iteminfolbl);

        appender.addRow(runbtn, pausumebtn);
        add(stopbtn, runbtn.c);
        stopbtn.hide();
        pausumebtn.hide();

        pack();
    }

    public void run() {
        if (checkcollectstate() == -2 || checkcollectstate() == 3) {
            botLogPing("Select another storage type", Color.WHITE);
            return;
        }
        runbtn.hide();
        stopbtn.show();
        pausumebtn.change("Pause");
        pausumebtn.show();
        paused = false;
        boolean ad = Config.autodrink;
        if (ad) Config.autodrink = false;
        boolean af = configuration.autoflower;
        if (ad) configuration.autoflower = false;
        block(true);

        collecting();

        runbtn.show();
        stopbtn.hide();
        pausumebtn.hide();
        if (ad) Config.autodrink = true;
        if (af) configuration.autoflower = true;
        block(false);
        maininfolbl.settext("", Color.WHITE);
    }

    public void collecting() {
        try {
            List<PBotGob> storages = new ArrayList<>(currentstoragelist);
            List<PBotGob> objects = new ArrayList<>(currentgoblist);
            byte cr = checkcollectstate();
            for (int p = 1; objects.size() > 0; p++) {
                pauseCheck();
                PBotGob pgob = closestGob(objects);
                if (pgob == null) {
                    objects.remove(pgob);
                    continue;
                }
                for (int i = 0; i < retry; i++) {
                    botLog("Gob is " + p + " of " + currentgoblist.size() + ". Try is " + (i + 1) + " of " + retry, Color.YELLOW);
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
                    if (PBotGobAPI.findGobById(ui, pgob.getGobId()) == null) {
                        botLog("Object not found. Skipping...", Color.WHITE);
                        objects.remove(pgob);
                        break;
                    }

                    if (!checkFlowerMenu(pgob)) {
                        objects.remove(pgob);
                        break;
                    }
                    mark(pgob);
                    if (pfRightClick(pgob)) {
                        if (checkflowers().size() == 0) {
                            botLog("flowermenu not required", Color.WHITE);
                            if (waitForPickUp(pgob.getGobId())) {
                                objects.remove(pgob);
                                break;
                            }
                        } else {
                            waitForFlowerMenu();
                            if (petalExists()) {
                                if (choosePetal()) {
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
                                    } else {
                                        objects.remove(pgob);
                                        break;
                                    }
                                }
                            } else {
                                objects.remove(pgob);
                                break;
                            }
                        }
                    } else {
                        objects.remove(pgob);
                        break;
                    }
                    sleep(1);
                    if (i + 1 == retry)
                        objects.remove(pgob);
                }
            }

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
                if (getInvItems(selecteditemlist).size() == 0) {
                    botLog("Inventory is empty", Color.WHITE);
                    stop();
                }
                storaging(storages);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception other) {
            other.printStackTrace();
            try {
                stop();
            } catch (InterruptedException ignore) {
            }
        }
        botLogPing("Finish!", Color.GREEN);
    }

    public List<PBotGob> storaging(List<PBotGob> storages) throws InterruptedException {
        List<PBotGob> output = new ArrayList<>(storages);
        for (int p = 0; p < storages.size(); p++) {
            for (int i = 0; i < retry; i++) {
                pauseCheck();
                botLog("Storage is " + (p + 1) + " of " + storages.size() + ". Try is " + (i + 1) + " of " + retry, Color.YELLOW);
                if (PBotGobAPI.findGobById(ui, storages.get(p).getGobId()) == null) {
                    botLog("Object not found. Skipping...", Color.WHITE);
                    break;
                }
                mark(storages.get(p));
                List<Window> ow = invWindows();
                if (pfRightClick(storages.get(p))) {
                    Window w = waitForNewInvWindow(ow);
                    if (w != null) {
                        PBotInventory wi = PBotWindowAPI.getInventory(w);
                        ISBox isBox = w.getchild(ISBox.class);
                        if (wi != null) {
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
                        } else if (isBox != null) {
                            if (isBox.getfreespace() > 0) {
                                List<PBotItem> items = getInvItems(selecteditemlist);
                                for (PBotItem pitem : items) {
                                    if (!PBotUtils.playerInventory(ui).inv.containschild(pitem.gitem)) continue;
                                    if (isBox.getfreespace() == 0) {
                                        break;
                                    }
                                    {
                                        int invitems = isBox.getUsedCapacity();
                                        pitem.transferItem();
                                        while (invitems == isBox.getUsedCapacity()) {
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
                            if (isBox.getfreespace() == 0) {
                                PBotWindowAPI.closeWindow(w);
                                waitForWindowClose(w);
                                output.remove(storages.get(p));
                                break;
                            }
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
            pauseCheck();
            botLog("try " + (i + 1) + " of " + retry + " pathfinding " + pgob + "...", Color.WHITE);
            boolean yea = cheakHit(pgob);

            //1. purus check the path
            if (!yea) {
                botLog("purus path", Color.WHITE);
                ui.gui.map.purusPfRightClick(pgob.gob, -1, 1, 0, null);

                while (ui.gui.map.pastaPathfinder.isAlive() && !ui.gui.map.pastaPathfinder.isInterrupted())
                    sleep(10);

                yea = ui.gui.map.foundPath;

                botLog("purus path" + (yea ? "" : " not") + " found", yea ? Color.GREEN : Color.RED);
            }

            //2. sloth check the path
            if (!yea) {
                botLog("sloth path", Color.WHITE);
                yea = ui.gui.map.pathto(pgob.gob);
                for (int t = 0, sleep = 10; !ui.gui.map.isclearmovequeue(); t += sleep) {
                    sleep(10);
                }

                botLog("sloth path" + (yea ? "" : " not ") + " found", yea ? Color.GREEN : Color.RED);
            }

            //3. amber check the path
            if (!yea) {
                botLog("amber path", Color.WHITE);
                ui.gui.map.pfRightClick(pgob.gob, -1, 1, 0, null);
                while (!ui.gui.map.pfthread.isInterrupted() && ui.gui.map.pfthread.isAlive())
                    sleep(10);

                yea = cheakHit(pgob);
            }

            //4. without pf
            if (!yea) {
                botLog("hard path", Color.WHITE);
                Coord2d opc = PBotGobAPI.player(ui).getRcCoords();
                pgob.doClick(1, 0);
                waitMoving();
                Coord2d npc = PBotGobAPI.player(ui).getRcCoords();
                Coord2d dist = npc.div(opc);
                double x = dist.x > 0 ? 1 : -1;
                double y = dist.y > 0 ? 1 : -1;
                PBotUtils.mapClick(ui, PBotGobAPI.player(ui).getRcCoords().add(x, y), 1, 0);
                waitMoving();

                yea = cheakHit(pgob);
            }

            sleep(1);

            if (yea) {
                botLog("path found", Color.GREEN);
                pgob.doClick(3, 0);
                waitMoving();
                return (true);
            } else
                botLog("path not found", Color.RED);
        }
        return (false);
    }

    public boolean cheakHit(PBotGob pgob) {
        PBotGob player = PBotGobAPI.player(ui);
        Hitbox[] box = Hitbox.hbfor(pgob.gob);
        Hitbox[] pbox = Hitbox.hbfor(player.gob);
        if (box != null && pbox != null) {
            boolean hit = false;
            for (Hitbox hb1 : box)
                for (Hitbox hb2 : pbox) {
                    if (hb1.ishitable()) {
                        hit = true;
                        if (configuration.insect(hb1.points, configuration.abs(hb2.points, 1), pgob.gob, player.gob))
                            return (true);
                    }
                }
            if (!hit) {
                return (pgob.getRcCoords().dist(player.getRcCoords()) <= 3);
            }
            return (false);
        } else
            return (pgob.getRcCoords().dist(player.getRcCoords()) <= 3);
    }

    public void waitMoving() throws InterruptedException {
        botLog("moving...", Color.WHITE);
        while (PBotGobAPI.player(ui).isMoving()) {
            sleep(10);
        }
        botLog("move stop", Color.WHITE);
    }

    public void mark(PBotGob pgob) {
        pgob.gob.mark(5000);
    }


    public boolean checkFlowerMenu(PBotGob pgob) throws InterruptedException {
        ArrayList<String> temp = new ArrayList<>();
        for (CheckListboxItem item : selectedflowerlist)
            if (item.selected)
                temp.add(item.name);
        if (temp.size() == 0) return (true);

        pgob.doClick(3, 0);
        waitForFlowerMenu();

        boolean found = false;
        if (petalExists()) {
            FlowerMenu menu = ui.root.getchild(FlowerMenu.class);
            flower:
            for (int i = 0; i < menu.opts.length; i++) {
                for (String item : temp) {
                    if (item.equals(menu.opts[i].name)) {
                        found = true;
                        break flower;
                    }
                }
            }
            if (!closeFlowermenu()) {
                botLog("Can't close the flowermenu", Color.WHITE);
                stop();
            }
        }
        return (found);
    }

    public boolean choosePetal() throws InterruptedException {
        botLog("petal choosing...", Color.WHITE);
        ArrayList<String> temp = new ArrayList<>();
        for (CheckListboxItem item : selectedflowerlist)
            if (item.selected)
                temp.add(item.name);
        for (int i = 0, sleep = 10; i < waitingtime; i += sleep) {
            FlowerMenu menu = ui.root.getchild(FlowerMenu.class);
            if (menu != null) {
                for (FlowerMenu.Petal opt : menu.opts) {
                    for (String item : temp) {
                        if (opt.name.equals(item)) {
                            botLog("choosePetal [" + item + "] true", Color.GREEN);
                            menu.choose(opt);
                            menu.destroy();
                            return (true);
                        }
                    }
                }
            }
            sleep(sleep);
        }
        botLog("choosePetals " + temp.toString() + " false", Color.RED);
        return (false);
    }

    public boolean waitFlowermenuClose() {
        botLog("flowermenu closing waiting...", Color.WHITE);
        for (int i = 0; i < retry; i++)
            if (PBotUtils.waitFlowermenuClose(ui, waitingtime)) {
                botLog("flowermenu closed", Color.WHITE);
                return (true);
            } else
                botLog("flowermenu didn't close", Color.WHITE);
        return (false);
    }

    public boolean waitForPickUp(long id) throws InterruptedException {
        botLog("pick up ground item waiting...", Color.WHITE);
        boolean r = false;
        for (int i = 0, sleep = 10; i < waitingtime; i += sleep) {
            if (PBotGobAPI.findGobById(ui, id) == null) {
                r = true;
                break;
            }
            sleep(sleep);
        }

        if (r)
            botLog("ground item picked", Color.WHITE);
        else
            botLog("ground item didn't pick", Color.WHITE);
        return (r);
    }

    public boolean waitForFlowerMenu() {
        botLog("flowermenu opening waiting...", Color.WHITE);
        boolean r = PBotUtils.waitForFlowerMenu(ui, waitingtime);
        if (r)
            botLog("flowermenu opened", Color.WHITE);
        else
            botLog("flowermenu didn't open", Color.WHITE);
        return (r);
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

    public boolean closeFlowermenu() {
        botLog("flowermenu closing...", Color.WHITE);
        for (int i = 0; i < retry; i++)
            if (PBotUtils.closeFlowermenu(ui, waitingtime)) {
                botLog("flowermenu closed", Color.WHITE);
                return (true);
            } else
                botLog("flowermenu didn't close", Color.WHITE);
        return (false);
    }


    public boolean drink() throws InterruptedException {
        if (!ui.gui.drinkingWater) {
            Thread t = new Thread(new DrinkWater(ui.gui));
            t.start();
            while (t.isAlive() && !t.isInterrupted()) {
                sleep(10);
            }
            if (!ui.gui.lastDrinkingSucessful) {
                botLog("PBotUtils Warning: Couldn't drink, didn't find anything to drink!", Color.ORANGE);
                return (false);
            }
        }
        return (true);
    }


    public byte waitForHourglass() throws InterruptedException {
        boolean repeat = true;
        while (repeat) {
            repeat = false;
            botLog("hourglass waiting...", Color.WHITE);
            double prog = ui.gui.prog;
            for (int i = 0, sleep = 5; prog == ui.gui.prog; i += sleep) {
                if (i > waitingtime)
                    return (0);
                prog = ui.gui.prog;
                sleep(sleep);
            }
            int total = 0;
            for (int sleep = 5; ui.gui.prog >= 0; total += sleep) {
                pauseCheck();
                if (PBotCharacterAPI.getStamina(ui) < 70) {//FIXME custom value
                    botLog("Drinking...", Color.WHITE);
                    if (!drink()) {
                        botLog("Drink failed. Pause!", Color.WHITE);
                        pause();
                    } else {
                        botLog("Drink successful", Color.WHITE);
                        repeat = true;
                        break;
                    }
                }

                byte cr = checkcollectstate();
                if (cr == -1 || cr == 0) {
                    if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                        botLog("Not enough space for item. Stopping...", Color.WHITE);
                        stop();
                        return (-1);
                    }
                }
                if (cr == 1) {
                    if (PBotUtils.getItemAtHand(ui) != null) {
                        botLog("Dropping...", Color.WHITE);
                        if (!dropItemFromHand()) {
                            botLog("Can't drop. Stopping...", Color.WHITE);
                            stop();
                            return (-1);
                        }
                    }
                }
                if (cr == 2) {
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
                sleep(sleep);
            }
            botLog("hourglass finish at " + total + " ms", Color.WHITE);
        }
        return (1);
    }


    public Window waitForNewInvWindow(List<Window> ows) throws InterruptedException {//FIXME stockpile and barrel
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

    public List<Window> invWindows() {
        List<Window> iwnds = new ArrayList<>();
        for (Window w : ui.gui.getchilds(Window.class)) {
            if (w.getchild(Inventory.class) != null || w.getchild(ISBox.class) != null)
                iwnds.add(w);
        }
        return (iwnds);
    }


    public void botLog(String msg, Color clr) {
        try {
            maininfolbl.settext(msg, clr);
            System.out.println("AreaPicker: " + msg);
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


    public void selectArea() {
        botLogPing("Please select an area by dragging!", Color.WHITE);
        ui.gui.map.PBotAPISelect = true;
        while (ui.gui.map.PBotAPISelect) {
            PBotUtils.sleep(25);
        }
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
            ArrayList<String> temp = new ArrayList<>();
            areagoblist.forEach((i) -> {
                selectedgoblist.add(new CheckListboxItem(i));
                temp.add(configuration.getShortName(i) + " (" + i.substring(0, i.lastIndexOf('/')) + ")");
            });
            selectedgoblbox.items.addAll(selectedgoblist);
            selectedgoblbox.resize(calcWidthString(temp), selectedgoblbox.sz.y);
            selectedgobwnd.pack();
            selectedgobsearch.settext("");
            if (selectedgoblbox.items.size() > 0) selectedgobbtn.change(Color.GREEN);
            else selectedgobbtn.change(Color.RED);

            selectgobbtn.change(Color.GREEN);

            updatelist("gob");
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
            ArrayList<String> temp = new ArrayList<>();
            areastoragelist.forEach((i) -> {
                selectedstoragelist.add(new CheckListboxItem(i));
                temp.add(configuration.getShortName(i) + " (" + i.substring(0, i.lastIndexOf('/')) + ")");
            });
            selectedstoragelbox.items.addAll(selectedstoragelist);
            selectedstoragelbox.resize(calcWidthString(temp), selectedstoragelbox.sz.y);
            selectedstoragewnd.pack();
            selectedstoragesearch.settext("");
            if (selectedstoragelbox.items.size() > 0) selectedstoragebtn.change(Color.GREEN);
            else selectedstoragebtn.change(Color.RED);

            selectstoragebtn.change(Color.GREEN);

            updatelist("storage");
            updateinfo("storage");
            pack();
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
            if (i != null) {
                if (big == null)
                    big = i;
                else {
                    Coord bs = big.witem.size();
                    Coord is = i.witem.size();
                    if (bs.x < is.x || bs.y < is.y)
                        big = i;
                }
            }
        }
        return big;
    }


    public byte checkcollectstate() {
        if (collecttriggerdbx.sel == null) return (-1);
        for (int i = 0; i < collectstates.length; i++) {
            if (collecttriggerdbx.sel.equals(collectstates[i])) return ((byte) i);
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
        ArrayList<PBotInventory> ret = new ArrayList<>();
        for (Widget window = ui.gui.lchild; window != null; window = window.prev)
            if (window instanceof Window && !((Window) window).origcap.equalsIgnoreCase("Belt"))
                for (Widget wdg = window.lchild; wdg != null; wdg = wdg.prev)
                    if (wdg instanceof Inventory)
                        ret.add(new PBotInventory((Inventory) wdg));
        for (PBotInventory inv : ret) {
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
        for (String s : addeditemlist) {
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
                flowerpetalsinfolbl.settext(checkflowers().size() > 0 ? checkflowers().toString() : "[Right Click]");
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
                if (item.selected) {
                    currentgoblist.addAll(currentList(PBotUtils.gobsInArea(ui, gobarea[0], gobarea[1]), item.name));
//                    currentgoblist.sort((o1, o2) -> {
//                        Coord2d pc = PBotGobAPI.player(ui).getRcCoords();
//                        return Double.compare(o1.getRcCoords().dist(pc), o2.getRcCoords().dist(pc));
//                    });
                }
            }
        } else if (type.equals("storage")) {
            currentstoragelist.clear();
            for (CheckListboxItem item : selectedstoragelist) {
                if (item.selected) {
                    currentstoragelist.addAll(currentList(PBotUtils.gobsInArea(ui, storagearea[0], storagearea[1]), item.name));
//                    currentstoragelist.sort((o1, o2) -> {
//                        Coord2d pc = PBotGobAPI.player(ui).getRcCoords();
//                        return Double.compare(o1.getRcCoords().dist(pc), o2.getRcCoords().dist(pc));
//                    });
                }
            }
        }
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

    public Comparator<CheckListboxItem> listboxsort() {
        return (o1, o2) -> {
            int b = Boolean.compare(o2.selected, o1.selected);
            return b == 0 ? configuration.getShortName(o1.name).compareTo(configuration.getShortName(o2.name)) : b;
        };
    }

    public PBotGob closestGob(List<PBotGob> list) {
        double min = Double.MAX_VALUE;
        PBotGob pgob = null;
        Coord2d pc = PBotGobAPI.player(ui).getRcCoords();
        for (PBotGob g : list) {
            if (pc.dist(g.getRcCoords()) < min) {
                min = Math.min(min, pc.dist(g.getRcCoords()));
                pgob = g;
            }
        }
        return (pgob);
    }

    public void stop() throws InterruptedException {
        if (runthread != null && runthread.isAlive())
            runthread.interrupt();
        ui.root.wdgmsg("gk", 27);
        sleep(1);
    }

    public void pause() {
        synchronized (pauseLock) {
            paused = true;
            pausumebtn.change("Resume");
        }
    }

    public void pauseCheck() throws InterruptedException {
        if (paused) {
            synchronized (pauseLock) {
                pauseLock.wait();
            }
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
            pausumebtn.change("Pause");
        }
    }

    public void block(boolean state) {
        block = state;
    }

    public boolean isblocked() {
        return block;
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

    public void destroy() {
        super.destroy();
        try {
            stop();
        } catch (InterruptedException ignore) {
        }
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
