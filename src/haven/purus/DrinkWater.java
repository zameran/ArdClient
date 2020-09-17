package haven.purus;

import haven.AltBeltWnd;
import haven.Composite;
import haven.Coord;
import haven.Drawable;
import haven.Equipory;
import haven.FlowerMenu;
import haven.GameUI;
import haven.Gob;
import haven.Inventory;
import haven.ItemInfo;
import haven.Loading;
import haven.ResData;
import haven.WItem;
import haven.Widget;
import haven.Window;
import haven.purus.pbot.PBotGobAPI;
import haven.purus.pbot.PBotUtils;
import modification.configuration;

import java.awt.Color;
import java.util.regex.Pattern;

public class DrinkWater implements Runnable {

    GameUI gui;
    String liquid;

    public DrinkWater(GameUI gui) {
        this.gui = gui;
    }

    public DrinkWater(GameUI gui, String liquid) {
        this.gui = gui;
        this.liquid = liquid;
    }

    @Override
    public void run() {
        drink();
    }

    private void drink() {
        // Don't attempt to drink if flower menu is already open or we are already drinking
        if (gui.ui.root.findchild(FlowerMenu.class) != null || gui.drinkingWater)
            return;
        gui.drinkingWater = true;
        WItem drinkFromThis = null;
        Equipory e = gui.getequipory();
        WItem l = e.quickslots[6];
        WItem r = e.quickslots[7];
        if (canDrinkFrom(l))
            drinkFromThis = l;
        if (canDrinkFrom(r))
            drinkFromThis = r;
        for (Widget w = gui.lchild; w != null; w = w.prev) {
            if (w instanceof Window) {
                Window wnd = (Window) w;
                for (Widget wdg = wnd.lchild; wdg != null; wdg = wdg.prev) {
                    if (wdg instanceof Inventory) {
                        Inventory inv = (Inventory) wdg;
                        for (WItem item : inv.children(WItem.class)) {
                            if (canDrinkFrom(item))
                                drinkFromThis = item;
                        }
                    }
                }
            } else if (w instanceof AltBeltWnd) { // Alternate belt must be separately enabled

                AltBeltWnd invBelt = (AltBeltWnd) w;
                for (WItem item : invBelt.children(WItem.class)) {
                    if (canDrinkFrom(item))
                        drinkFromThis = item;
                }
            }
        }
        if (drinkFromThis != null) {
            if (configuration.drinkorsip) {
                int stamina = PBotUtils.getStamina(gui.ui);
                int sips = configuration.siponce ? 1 : (configuration.autosipthreshold - stamina) / 10;
//                while (PBotUtils.getStamina(gui.ui) < configuration.autosipthreshold && canDrinkFrom(drinkFromThis)) {
                for (int i = 0; i < sips; i++) {
                    if (!canDrinkFrom(drinkFromThis)) {
                        gui.drinkingWater = false;
                        return;
                    }
                    if (PBotUtils.petalExists(gui.ui)) {
                        int limit = configuration.sipwaiting;
                        int sleep = 10;
                        int cycles = 0;
                        while (PBotUtils.petalExists(gui.ui)) {
                            if (cycles >= limit) {
                                PBotUtils.sysMsg(gui.ui, "Petal exist. Timeout expired. Sip failed.", Color.RED);
                                gui.drinkingWater = false;
                                return;
                            }
                            sleep(sleep);
                            cycles += sleep;
                        }
                    }
                    drinkFromThis.item.wdgmsg("iact", Coord.z, 3);
                    FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
                    int retries = 0; // After 100 retries aka. 5 seconds, it will probably never appear
                    while (menu == null) {
                        if (retries++ > 100) {
                            gui.drinkingWater = false;
                            return;
                        }
                        sleep(50);
                        menu = gui.ui.root.findchild(FlowerMenu.class);
                    }
                    for (FlowerMenu.Petal opt : menu.opts) {
                        if (opt.name.equals("Sip")) {
                            menu.choose(opt);
                            menu.destroy();
                        }
                    }
                    if (waitDrinkPose())
                        while (drinkPose()) {
                            sleep(50);
                        }
                    else {
                        PBotUtils.sysMsg(gui.ui, "Drink pose not found. Timeout expired. Sip failed.", Color.RED);
                        gui.drinkingWater = false;
                        return;
                    }
                }
            } else {
                drinkFromThis.item.wdgmsg("iact", Coord.z, 3);
                FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
                int retries = 0; // After 100 retries aka. 5 seconds, it will probably never appear
                while (menu == null) {
                    if (retries++ > 100) {
                        gui.drinkingWater = false;
                        return;
                    }
                    sleep(50);
                    menu = gui.ui.root.findchild(FlowerMenu.class);
                }
                for (FlowerMenu.Petal opt : menu.opts) {
                    if (opt.name.equals("Drink")) {
                        menu.choose(opt);
                        menu.destroy();
                    }
                }
            }
            gui.lastDrinkingSucessful = true;
        } else {
            gui.lastDrinkingSucessful = false;
        }
        gui.drinkingWater = false;
    }

    private boolean waitDrinkPose() {
        int limit = configuration.sipwaiting;
        int sleep = 10;
        int cycles = 0;
        while (!drinkPose()) {
            if (cycles >= limit) break;
            sleep(sleep);
            cycles += sleep;
        }
        return drinkPose();
    }

    private boolean drinkPose() {
        try {
            return PBotGobAPI.player(gui.ui).getPoses().contains("gfx/borka/drinkan");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canDrinkFrom(WItem item) {
        Pattern liquidPattern = Pattern.compile(String.format("[0-9.]+ l of (%s)",
                //	String.join("|", new String[] { "Water", "Piping Hot Tea", "Tea" }), Pattern.CASE_INSENSITIVE));
                String.join("|", configuration.liquids), Pattern.CASE_INSENSITIVE));
        ItemInfo.Contents contents = getContents(item);
        if (contents != null && contents.sub != null && contents.content >= 0.05) {
            synchronized (item.item.ui) {
                for (ItemInfo info : contents.sub) {
                    if (info instanceof ItemInfo.Name) {
                        ItemInfo.Name name = (ItemInfo.Name) info;
                        if (name.str != null)
                            if (liquid != null)
                                if (name.str.text.contains(liquid));
                            else if (configuration.autoDrinkWhatever && liquidPattern.matcher(name.str.text).matches())
                                return true;
                            else if (name.str.text.contains(configuration.autoDrinkLiquid)) //"Water"
                                return true;
                    }
                }
            }
        }
        return false;
    }

    private void sleep(int timeInMs) {
        try {
            Thread.sleep(timeInMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ItemInfo.Contents getContents(WItem item) {
        if (item == null)
            return null;
        synchronized (item.item.ui) {
            try {
                for (ItemInfo info : item.item.info())
                    if (info != null && info instanceof ItemInfo.Contents)
                        return (ItemInfo.Contents) info;
            } catch (Loading ignored) {
            }
        }
        return null;
    }
}
