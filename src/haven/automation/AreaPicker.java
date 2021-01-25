package haven.automation;

import haven.Button;
import haven.Config;
import haven.Coord;
import haven.Dropbox;
import haven.GOut;
import haven.Label;
import haven.ResizableTextEntry;
import haven.Resource;
import haven.Text;
import haven.TextEntry;
import haven.WidgetVerticalAppender;
import haven.Window;
import haven.purus.pbot.PBotGob;
import haven.purus.pbot.PBotGobAPI;
import haven.purus.pbot.PBotUtils;

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
 * 2. FlowerMenu (whitout flowermenu = just right click?) . favorite?
 * 3. optional. Storage...
 * 4. Storage : select area and chests
 * Drink water?
 * <p>
 * <p>
 * final. Run
 */

public class AreaPicker extends Window implements Runnable {
    public final static String scriptname = "Area Picker";
    public final static String storagetrigger = "Storage";
    public final WidgetVerticalAppender appender = new WidgetVerticalAppender(this);
    public Thread runthread;

    public final Coord[]
            gobsarea = new Coord[2],
            storagearea = new Coord[2];
    public final List<String>
            areagobslist = new ArrayList<>(),
            flowermenulist = new ArrayList<>(Config.flowermenus.keySet()),
            collecttrigger = new ArrayList<>(Arrays.asList("Inventory", "Drop out of hand", storagetrigger)),
            areastorageslist = new ArrayList<>();
    public final List<PBotGob>
            currentgoblist = new ArrayList<>(),
            currentstoragelist = new ArrayList<>();

    public Button refresh, selectgobsbtn, selectstoragebtn, runbtn, stopbtn;
    public Label maininfolbl, areagobsinfolbl, areastoragesinfolbl;
    public Dropbox<String> selectedgobdbx, flowermenudbx, collecttriggerdbx, selectedstoragedbx; //it may be multiples
    public TextEntry waitingtimete;

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
                    } catch (NumberFormatException e) {
                    }
                }
                return (false);
            }
        };

        selectgobsbtn = new Button(50, "Select area") {
            public void click() {
                new Thread(new selectinggobsarea(), "Selecting Area").start();
            }
        };
        selectedgobdbx = dropbox(areagobslist, "gob");
        areagobsinfolbl = new Label("");

        flowermenudbx = new Dropbox<String>(10, flowermenulist) {
            protected String listitem(int i) {
                return flowermenulist.get(i);
            }

            protected int listitems() {
                return flowermenulist.size();
            }

            protected void drawitem(GOut g, String item, int i) {
                String text = Resource.language.equals("en") ? item : item + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, item) + ")";
                g.text(text, Coord.z);
            }

            public void change(String item) {
                super.change(item);
                String text = Resource.language.equals("en") ? item : item + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, item) + ")";
                int x = Text.render(text).sz().x;
                if (sz.x != x + Dropbox.drop.sz().x) resize(new Coord(x + Dropbox.drop.sz().x, sz.y));
            }

            public boolean mousedown(Coord c, int btn) {
                super.mousedown(c, btn);
                List<String> n = new ArrayList<>();
                flowermenulist.forEach(s -> n.add(s + " (" + Resource.getLocString(Resource.BUNDLE_FLOWER, s) + ")"));
                if (dl != null) resizedl(n);
                return (true);
            }
        };

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
                super.mousedown(c, btn);
                if (dl != null) resizedl(collecttrigger);
                return (true);
            }
        };

        selectstoragebtn = new Button(50, "Select area") {
            public void click() {
                new Thread(new selectingstoragesarea(), "Selecting Area").start();
            }
        };
        selectedstoragedbx = dropbox(areastorageslist, "storage");
        areastoragesinfolbl = new Label("");

        runbtn = new Button(50, "Run") {
            public void click() {
                (runthread = new Thread(AreaPicker.this, "Area Collecting")).start();
            }
        };
        stopbtn = new Button(50, "Stop") {
            public void click() {
                stop();
            }
        };

        appender.setHorizontalMargin(5);
        appender.setVerticalMargin(2);
        appender.addRow(maininfolbl); //refreshbtn for flowermenu
        appender.addRow(new Label("1. Objects to collect"), selectgobsbtn, selectedgobdbx, areagobsinfolbl);
        appender.addRow(new Label("2. Flower Petal"), flowermenudbx); //may be make it multiple? (Take bark and sticks)
        appender.addRow(new Label("3(WIP). Storage type"), collecttriggerdbx);
        appender.addRow(new Label("4(WIP). Objects to storage"), selectstoragebtn, selectedstoragedbx, areastoragesinfolbl);

        appender.add(runbtn);
        add(stopbtn, runbtn.c); stopbtn.hide();

        pack();
    }

    public void run() {
        runbtn.hide();
        stopbtn.show();
        collecting();
        runbtn.show();
        stopbtn.hide();
        maininfolbl.settext("", Color.WHITE);
    }

    public void stop() {
        runthread.interrupt();
    }

    public void collecting() {
        try {
            for (int p = 0; p < currentgoblist.size(); p++) {
                currentgoblist.get(p).toggleMarked();
                for (int i = 0; i < retry; i++) {
                    botLog("Gob is " + (p + 1) + " of " + currentgoblist.size() + ". Try is " + (i + 1) + " of " + retry, Color.YELLOW);
                    if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                        botLog("Not enough space for item", Color.WHITE);
                        return;
                    }
                    if (pfRightClick(currentgoblist.get(p))) {
                        waitForFlowerMenu();
                        if (petalExists()) {
                            if (choosePetal(flowermenudbx.sel)) {
                                if (!waitFlowermenuClose()) {
                                    botLog("Can't close the flowermenu", Color.WHITE);
                                    return;
                                }
                                waitMoving();
                                if (waitForHourglass())
                                    botLog("hourglass is finish", Color.WHITE);
                                else
                                    botLog("hourglass timeout", Color.WHITE);

                            } else {
                                if (!closeFlowermenu()) {
                                    botLog("Can't close the flowermenu", Color.WHITE);
                                    return;
                                } else
                                    break;
                            }
                        } else
                            break;
                    } else
                        break;
                    sleep(1);
                }
                currentgoblist.get(p).toggleMarked();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        botLog("Finish!", Color.GREEN);
    }

    public boolean pfRightClick(PBotGob pgob) throws InterruptedException {
        botLog("pathfinding " + pgob + "...", Color.WHITE);
        for (int i = 0; i < retry; i++) {
            botLog("try " + (i + 1) + " of " + retry + " pathfinding " + pgob + "...", Color.WHITE);
            boolean yea = ui.gui.map.pathto(pgob.gob);
            while (ui.gui != null && ui.gui.map != null && !ui.gui.map.isclearmovequeue())
                sleep(10);
            if (yea) {   //very bad pathfinding
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
        if (PBotGobAPI.player(ui).isMoving()) {
            sleep(10);
        }
        botLog("move stop", Color.WHITE);
    }

    public boolean choosePetal(String petal) throws InterruptedException {
        botLog("petal choosing...", Color.WHITE);
        for (int i = 0, sleep = 10; i < waitingtime; i += sleep) {
            if (PBotUtils.choosePetal(ui, petal)) {
                botLog("choosePetal " + petal + " true", Color.RED);
                return (true);
            }
            sleep(sleep);
        }
        botLog("choosePetal " + petal + " false", Color.RED);
        return (false);
    }

    public boolean waitForHourglass() throws InterruptedException {
        botLog("hourglass waiting...", Color.WHITE);
        double prog = ui.gui.prog;
        int retries = 0;
        while (prog == ui.gui.prog) {
            if (retries > waitingtime / 5)
                return (false);
            retries++;
            prog = ui.gui.prog;
            sleep(5);
        }
        while (ui.gui.prog >= 0) {
            if (!freeSlots() || PBotUtils.getItemAtHand(ui) != null) {
                botLog("Not enough space for item", Color.WHITE);
                ui.root.wdgmsg("gk", 27);
                return (false);
            }
            sleep(25);
        }
        return (true);
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
        boolean r = PBotUtils.waitForFlowerMenu(ui, waitingtime);
        if (r)
            botLog("flowermenu opened", Color.WHITE);
        else
            botLog("flowermenu didn't open", Color.WHITE);
        return (r);
    }

    public void sleep(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    public boolean freeSlots() throws InterruptedException {
        botLog("free slots checking...", Color.WHITE);
        boolean free = false;
        int slots = -1;
        while (slots == -1) {
            try {
                slots = PBotUtils.playerInventory(ui).freeSlotsInv();
                if (slots > 0) free = true;
                botLog("free slots checked " + slots, Color.WHITE);
            } catch (Exception e) {
                botLog("free slots checking failed", Color.WHITE);
                sleep(100);
            }
        }
        botLog("free slots " + free, Color.WHITE);
        return free;
    }

    public void botLog(String msg, Color clr) {
        try {
            maininfolbl.settext(msg, clr);
            System.out.println(msg);
            ui.gui.botlog.append(msg, clr);
        } catch (Exception e) {
        }
    }

    public void tick(int dt) {
        super.tick(dt);
    }

    public class selectinggobsarea implements Runnable {
        @Override
        public void run() {
            PBotUtils.sysMsg(ui, "Drag area over Gobs", Color.WHITE);
            PBotUtils.selectArea(ui);
            gobsarea[0] = PBotUtils.getSelectedAreaA();
            gobsarea[1] = PBotUtils.getSelectedAreaB();

            areagobslist.clear();
            areagobslist.addAll(stringInArea(gobsarea));
            selectedgobdbx.resize(calcDropboxSize(areagobslist));
            selectedgobdbx.sel = null;

            updateinfo("gob");
            pack();
        }
    }

    public class selectingstoragesarea implements Runnable {
        @Override
        public void run() {
            PBotUtils.sysMsg(ui, "Drag area over Gobs", Color.WHITE);
            PBotUtils.selectArea(ui);
            storagearea[0] = PBotUtils.getSelectedAreaA();
            storagearea[1] = PBotUtils.getSelectedAreaB();

            areastorageslist.clear();
            areastorageslist.addAll(stringInArea(storagearea));
            selectedstoragedbx.resize(calcDropboxSize(areastorageslist));
            selectedstoragedbx.sel = null;

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

    public Dropbox<String> dropbox(List<String> list, String type) {
        Coord size = calcDropboxSize(list);
        return new Dropbox<String>(size.x, 10, size.y) {
            protected String listitem(int i) {
                return list.get(i);
            }

            protected int listitems() {
                return list.size();
            }

            protected void drawitem(GOut g, String item, int i) {
                g.text(item, Coord.z);
            }

            public void change(String item) {
                super.change(item);
                resize(Text.render(item).sz().x + drop.sz().x + 2, calcDropboxSize(list).y);
                updatelist(type);
                updateinfo(type);
            }

            public boolean mousedown(Coord c, int btn) {
                super.mousedown(c, btn);
                if (dl != null) resizedl(list);
                return (true);
            }
        };
    }

    public void areainfo(Label lbl, Coord[] area, List<String> list, Dropbox<String> dbx) {
        StringBuilder sb = new StringBuilder();
        int x = Math.abs(area[1].x - area[0].x) / 11;
        int y = Math.abs(area[1].y - area[0].y) / 11;
        sb.append("Area ").append(x).append("x").append(y);
        if (list.size() > 0) {
            sb.append(" : ");
            if (dbx != null && dbx.sel != null) {
                sb.append(currentList(PBotUtils.gobsInArea(ui, area[0], area[1]), dbx.sel).size()).append(" of ").append(dbx.sel);
            } else
                sb.append(list.size()).append(" types of gobs");
        }
        if (lbl != null) {
            if (dbx != null)
                lbl.move(dbx.c.add(dbx.sz.x + 5, 0));
            lbl.settext(sb.toString());
            pack();
        }
    }

    public void updateinfo(String type) {
        if (type.equals("gob"))
            areainfo(areagobsinfolbl, gobsarea, areagobslist, selectedgobdbx);
        else if (type.equals("storage"))
            areainfo(areastoragesinfolbl, storagearea, areastorageslist, selectedstoragedbx);
    }

    public void updatelist(String type) {
        if (type.equals("gob")) {
            currentgoblist.clear();
            currentgoblist.addAll(currentList(PBotUtils.gobsInArea(ui, gobsarea[0], gobsarea[1]), selectedgobdbx.sel));
        } else if (type.equals("storage")) {
            currentstoragelist.clear();
            currentstoragelist.addAll(currentList(PBotUtils.gobsInArea(ui, storagearea[0], storagearea[1]), selectedstoragedbx.sel));
        }
    }


    public Coord calcDropboxSize(List<String> list) {
        Optional<Integer> ow = list.stream().map((v) -> Text.render(v).sz().x).collect(Collectors.toList()).stream().reduce(Integer::max);
        int w = Dropbox.drop.sz().x + 2;
        if (ow.isPresent() && list.size() > 0)
            w += ow.get();
        int h = Math.max(list.size() > 0 ? Text.render(list.get(0)).sz().y : 0, 16);
        return new Coord(w, h);
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
}
