package haven.automation;


import haven.Coord;
import haven.GameUI;
import haven.ItemInfo;
import haven.purus.pbot.PBotInventory;
import haven.purus.pbot.PBotItem;
import haven.purus.pbot.PBotUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class FillCheeseTray implements Runnable {
    private GameUI gui;
    List<PBotItem> trays = new ArrayList<>();
    List<PBotItem> trays2 = new ArrayList<>();
    private static final int TIMEOUT = 2000;

    public FillCheeseTray(GameUI gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            for (PBotInventory pInv : PBotUtils.getAllInventories(gui.ui)) {
                for (PBotItem item : pInv.getInventoryContents()) {
                    if (item.getResname().endsWith("cheesetray")) {
                        trays.add(item);
                    }
                }
            }
            System.out.println("trays size : " + trays.size());
            for (PBotItem item : trays) {
                ItemInfo.Contents contents = item.getContents();
                if (contents != null)
                    System.out.println("contents not null");
                else
                    System.out.println("contents null");
                if (contents == null || contents.iscurds)
                    trays2.add(item);
            }
            if (trays2.size() == 0) {
                PBotUtils.sysMsg(gui.ui, "No trays with space found, not running.", Color.white);
                return;
            }
            System.out.println("Number of Cheese trays found is : " + trays2.size());

            for (PBotItem tray : trays2) {
                for (int l = 0; l < 4; l++) {
                    PBotItem hand = PBotUtils.getItemAtHand(gui.ui);
                    List<PBotItem> curds = getCurds();
                    while (hand == null && curds.size() > 0) {
                        curds.get(curds.size() - 1).takeItem(1000);
                        hand = PBotUtils.getItemAtHand(gui.ui);
                    }
                    if (hand == null) {
                        PBotUtils.sysMsg(gui.ui, "Don't appear to have curds, stopping.", Color.white);
                        return;
                    }
                    tray.itemact(3);

                    /*for (int i = 0, sleep = 10; i < 1000; i += sleep) {
                        while (tray.getResname() == null)
                            PBotUtils.sleep(sleep);
                        if (!tray.getResname().endsWith("cheesetray"))
                            break;
                        if (PBotUtils.getItemAtHand(gui.ui) == null)
                            break;
                        PBotUtils.sleep(sleep);
                    }
                    if (PBotUtils.getItemAtHand(gui.ui) != null)
                        break;*/
                }
            }

            Coord slot = PBotUtils.getFreeInvSlot(gui.maininv);
            if (PBotUtils.getItemAtHand(gui.ui) != null)
                PBotUtils.dropItemToInventory(slot, gui.maininv);
            PBotUtils.sysMsg(gui.ui, "Done", Color.white);
        } catch (Exception e) {
            PBotUtils.sysMsg(gui.ui, "Error " + e, Color.white);
        }
    }

    private List<PBotItem> getCurds() {
        List<PBotItem> cs = new ArrayList<>();
        for (PBotInventory pInv : PBotUtils.getAllInventories(gui.ui)) {
            for (PBotItem item : pInv.getInventoryContents()) {
                if (item.getResname().contains("curd-")) {
                    cs.add(item);
                }
            }
        }
        return (cs);
    }
}
