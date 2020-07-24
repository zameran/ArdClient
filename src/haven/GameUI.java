/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import haven.automation.Discord;
import haven.automation.ErrorSysMsgCallback;
import haven.automation.PickForageable;
import haven.automation.Traverse;
import haven.livestock.LivestockManager;
import haven.overlays.OverlayManager;
import haven.purus.DrinkWater;
import haven.purus.ItemClickCallback;
import haven.purus.pbot.PBotAPI;
import haven.purus.pbot.PBotDiscord;
import haven.purus.pbot.PBotScriptlist;
import haven.purus.pbot.PBotScriptlistOld;
import haven.purus.pbot.PBotUtils;
import haven.resutil.FoodInfo;
import haven.sloth.gob.Mark;
import haven.sloth.gui.DeletedManager;
import haven.sloth.gui.HiddenManager;
import haven.sloth.gui.HighlightManager;
import haven.sloth.gui.SessionDisplay;
import haven.sloth.gui.SoundManager;
import integrations.mapv4.MappingClient;
import modification.configuration;
import modification.newQuickSlotsWdg;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import static haven.Action.TOGGLE_CHARACTER;
import static haven.Action.TOGGLE_EQUIPMENT;
import static haven.Action.TOGGLE_INVENTORY;
import static haven.Action.TOGGLE_KIN_LIST;
import static haven.Action.TOGGLE_OPTIONS;
import static haven.Action.TOGGLE_SEARCH;
import static haven.Inventory.invsq;

public class GameUI extends ConsoleHost implements Console.Directory {
    public static final Text.Foundry msgfoundry = new Text.Foundry(Text.dfont, Text.cfg.msg);
    private static final int blpw = 142, brpw = 142;
    public final String chrid, genus;
    public final long plid;
    public final Hidepanel ulpanel, umpanel, urpanel, blpanel, brpanel, menupanel;
    public Avaview portrait;
    public MenuGrid menu;
    public MenuSearch menuSearch;
    public haven.purus.pbot.PBotScriptlist PBotScriptlist;
    public PBotScriptlistOld PBotScriptlistold;
    public MapView map;
    public Fightview fv;
    private List<Widget> meters = new LinkedList<Widget>();
    private List<Widget> cmeters = new LinkedList<Widget>();
    private Text lastmsg;
    private double msgtime;
    public Window invwnd, equwnd, srchwnd;
    public FilterWnd filter;
    public Coord makewndc = Utils.getprefc("makewndc", new Coord(400, 200));
    public Inventory maininv;
    private Boolean temporarilyswimming = false;
    public CharWnd chrwdg;
    public Speedget speed;
    public MapWnd mapfile;
    public Widget qqview;
    public QuestWnd questwnd;
    public boolean discordconnected = false;
    public BuddyWnd buddies;
    public Equipory equipory;
    private Zergwnd zerg;
    public final Collection<Polity> polities = new ArrayList<Polity>();
    public HelpWnd help;
    public OptWnd opts;
    public Collection<DraggedItem> hand = new LinkedList<DraggedItem>();
    private Collection<DraggedItem> handSave = new LinkedList<DraggedItem>();
    private long DrinkTimer = 0, StarvationAlertDelay = 0, SwimTimer;
    public WItem vhand;
    public ChatUI chat;
    public ChatWnd chatwnd;
    private int saferadius = 1;
    private int dangerradius = 1;
    public WeakReference<Speedget> speedget;
    public ChatUI.Channel syslog;
    public Window hidden, deleted, alerted, highlighted, overlayed, gobspawner;
    public double prog = -1;
    private boolean afk = false;
    @SuppressWarnings("unchecked")
    public BeltSlot[] belt = new BeltSlot[144]; //FIXME indir<Resource>
    public Belt beltwdg;
    public final Map<Integer, String> polowners = new HashMap<Integer, String>();
    public Bufflist buffs;
    public LocalMiniMap mmap;
    private MinimapWnd mmapwnd;
    public haven.timers.TimersWnd timerswnd;
    public QuickSlotsWdg quickslots;
    public newQuickSlotsWdg newquickslots;
    public StatusWdg statuswindow;
    public static boolean swimon = false;
    public static boolean crimeon = false;
    public static boolean trackon = false;
    public static boolean partyperm = false;
    public boolean crimeautotgld = false;
    public boolean swimautotgld = false;
    public boolean trackautotgld = false;
    public CraftHistoryBelt histbelt;
    private ErrorSysMsgCallback errmsgcb;
    public StudyWnd studywnd;
    public LivestockManager livestockwnd;
    public ItemClickCallback itemClickCallback;
    public boolean drinkingWater, lastDrinkingSucessful;
    public CraftWindow makewnd;
    public BeltWnd fbelt, nbelt, npbelt;
    public MapPointer pointer;
    public Cal cal;
    public QuestHelper questhelper;
    public Thread DrinkThread;
    public CraftDBWnd craftwnd = null;
    public long inspectedgobid = 0;//used for attaching inspected qualities to gobs.

    private static final OwnerContext.ClassResolver<BeltSlot> beltctxr = new OwnerContext.ClassResolver<BeltSlot>()
            .add(Glob.class, slot -> slot.wdg().ui.sess.glob)
            .add(Session.class, slot -> slot.wdg().ui.sess);

    public class BeltSlot implements GSprite.Owner {
        public final int idx;
        public final Indir<Resource> res;
        public final Message sdt;

        public BeltSlot(int idx, Indir<Resource> res, Message sdt) {
            this.idx = idx;
            this.res = res;
            this.sdt = sdt;
        }

        private GSprite spr = null;

        public GSprite spr() {
            GSprite ret = this.spr;
            if (ret == null)
                ret = this.spr = GSprite.create(this, res.get(), Message.nil);
            return (ret);
        }

        public Resource getres() {
            return (res.get());
        }

        public Random mkrandoom() {
            return (new Random(System.identityHashCode(this)));
        }

        public <T> T context(Class<T> cl) {
            return (beltctxr.context(cl, this));
        }

        private GameUI wdg() {
            return (GameUI.this);
        }
    }

    public abstract class Belt extends Widget {
        public Belt(Coord sz) {
            super(sz);
        }

        public void keyact(final int slot) {
            if (map != null) {
                Coord mvc = map.rootxlate(ui.mc);
                if (mvc.isect(Coord.z, map.sz)) {
                    map.new Hittest(mvc) {
                        protected void hit(Coord pc, Coord2d mc, ClickData inf) {
                            Object[] args = {slot, 1, ui.modflags(), mc.floor(OCache.posres)};
                            if (inf != null)
                                args = Utils.extend(args, inf.clickargs());
                            GameUI.this.wdgmsg("belt", args);
                        }

                        protected void nohit(Coord pc) {
                            GameUI.this.wdgmsg("belt", slot, 1, ui.modflags());
                        }
                    }.run();
                }
            }
        }
    }

    @RName("gameui")
    public static class $_ implements Factory {
        public Widget create(UI ui, Object[] args) {
            String chrid = (String) args[0];
            int plid = (Integer) args[1];
            String genus = "";
            if (args.length > 2)
                genus = (String) args[2];
            return (new GameUI(chrid, plid, genus));
        }
    }

    private final Coord minimapc;
    private final Coord menugridc;

    public GameUI(String chrid, long plid, String genus) {
        this.chrid = chrid;
        this.plid = plid;
        this.genus = genus;
        setcanfocus(true);
        setfocusctl(true);
        chat = add(new ChatUI(0, 0));
        if (Utils.getprefb("chatvis", true)) {
            chat.hresize(chat.savedh);
            chat.show();
        }
        beltwdg.raise();
        blpanel = add(new Hidepanel("gui-bl", null, new Coord(-1, 1)));
        brpanel = add(new Hidepanel("gui-br", null, new Coord(1, 1)) {
            public void move(double a) {
                super.move(a);
                menupanel.move();
            }
        });
        menupanel = add(new Hidepanel("menu", new Indir<Coord>() {
            public Coord get() {
                return (new Coord(GameUI.this.sz.x, Math.min(brpanel.c.y/* - menupanel.sz.y*/ + 1, GameUI.this.sz.y - menupanel.sz.y)));
            }
        }, new Coord(1, 0)));

        // brpanel.add(new Img(Resource.loadtex("gfx/hud/brframe")), 0, 0);
        if (Config.lockedmainmenu)
            menupanel.add(new MainMenu(), 0, 0);
        ulpanel = add(new Hidepanel("gui-ul", null, new Coord(-1, -1)));
        umpanel = add(new Hidepanel("gui-um", null, new Coord(0, -1)));
        urpanel = add(new Hidepanel("gui-ur", null, new Coord(1, -1)));
        Tex lbtnbg = Resource.loadtex("gfx/hud/lbtn-bg");
        blpanel.add(new Img(Resource.loadtex("gfx/hud/blframe")), 0, lbtnbg.sz().y - 33);
        blpanel.add(new Img(lbtnbg), 0, 0);
        minimapc = new Coord(4, 34 + (lbtnbg.sz().y - 33));
        Tex rbtnbg = Resource.loadtex("gfx/hud/csearch-bg");
        Img brframe = brpanel.add(new Img(Resource.loadtex("gfx/hud/brframe")), rbtnbg.sz().x - 22, 0);
        menugridc = brframe.c.add(20, 34);
        Img rbtnimg = brpanel.add(new Img(rbtnbg), 0, brpanel.sz.y - rbtnbg.sz().y);
        menupanel.add(new MainMenu(), 0, 0);
        mapbuttons();
        menubuttons(rbtnimg);
        foldbuttons();
        portrait = ulpanel.add(new Avaview(Avaview.dasz, plid, "avacam") {
            public boolean mousedown(Coord c, int button) {
                return (true);
            }
        }, new Coord(10, 10));
        cal = umpanel.add(new Cal(), new Coord(0, 10));
        if (Config.hidecalendar)
            cal.hide();
        add(new Widget(new Coord(360, 40)) {
            @Override
            public void draw(GOut g) {
                if (Config.showservertime) {
                    Tex time = ui.sess.glob.servertimetex;
                    if (time != null)
                        g.image(time, new Coord(360 / 2 - time.sz().x / 2, 0));
                }
            }
        }, new Coord(HavenPanel.w / 2 - 360 / 2, umpanel.sz.y));
        buffs = ulpanel.add(new Bufflist(), new Coord(95, 65));
        umpanel.add(new Cal(), new Coord(0, 10));
        syslog = chat.add(new ChatUI.Log("System"));
        opts = add(new OptWnd());
        opts.hide();
        zerg = add(new Zergwnd(), Utils.getprefc("wndc-zerg", new Coord(187, 50)));
        zerg.hide();

        quickslots = new QuickSlotsWdg();
        newquickslots = new modification.newQuickSlotsWdg();

        if (!Config.quickslots) {
            quickslots.hide();
            newquickslots.hide();
        } else if (configuration.newQuickSlotWdg)
            quickslots.hide();
        else newquickslots.hide();

        add(quickslots, Utils.getprefc("quickslotsc", new Coord(430, HavenPanel.h - 160)));
        add(newquickslots, Utils.getprefc("newQuickSlotWdgc", new Coord(450, HavenPanel.h - 160)));

        if (Config.statuswdgvisible) {
            statuswindow = new StatusWdg();
            add(statuswindow, new Coord(HavenPanel.w / 2 + 80, 10));
        }


        histbelt = new CraftHistoryBelt(Utils.getprefb("histbelt_vertical", true));
        add(histbelt, Utils.getprefc("histbelt_c", new Coord(70, 200)));
        if (!Config.histbelt)
            histbelt.hide();


        PBotAPI.gui = this;
        if (Config.showTroughrad && Config.showBeehiverad)
            saferadius = 4;
        else if (Config.showTroughrad && Config.showBeehiverad)
            saferadius = 3;
        else if (Config.showTroughrad && !Config.showBeehiverad)
            saferadius = 2;
        else if (!Config.showTroughrad && !Config.showBeehiverad)
            saferadius = 1;
        fixAlarms();
    }

    public static final KeyBinding kb_map = KeyBinding.get("map", KeyMatch.forchar('A', KeyMatch.C));
    public static final KeyBinding kb_claim = KeyBinding.get("ol-claim", KeyMatch.nil);
    public static final KeyBinding kb_vil = KeyBinding.get("ol-vil", KeyMatch.nil);
    public static final KeyBinding kb_rlm = KeyBinding.get("ol-rlm", KeyMatch.nil);

    private void mapbuttons() {
        blpanel.add(new MenuButton("lbtn-claim", kb_claim, "Display personal claims") {
            public void click() {
                if ((map != null) && !map.visol(0)) {
                    map.enol(0);
                    map.enol(1);
                } else {
                    map.disol(0);
                    map.disol(1);
                }
            }
        }, 0, 0);
        blpanel.add(new MenuButton("lbtn-vil", kb_vil, "Display village claims") {
            public void click() {
                if ((map != null) && !map.visol(2)) {
                    map.enol(2);
                    map.enol(3);
                } else {
                    map.disol(2);
                    map.disol(3);
                }
            }
        }, 0, 0);
        blpanel.add(new MenuButton("lbtn-rlm", kb_rlm, "Display realms") {
            public void click() {
                if ((map != null) && !map.visol(4)) {
                    map.enol(4);
                    map.enol(5);
                } else {
                    map.disol(4);
                    map.disol(5);
                }
            }
        }, 0, 0);
        blpanel.add(new MenuButton("lbtn-map", kb_map, "Map") {
            public void click() {
                if ((mapfile != null) && mapfile.show(!mapfile.visible)) {
                    mapfile.raise();
                    fitwdg(mapfile);
                    setfocus(mapfile);
                }
            }
        });
    }

    public static final KeyBinding kb_srch = KeyBinding.get("scm-srch", KeyMatch.forchar('Z', KeyMatch.C));

    private void menubuttons(Widget bg) {
        brpanel.add(new MenuButton("csearch", kb_srch, "Search actions...") { //TODO настроить тултипы в поиске
            public void click() {
                if (srchwnd == null) {
                    srchwnd = new MenuSearch(menu);
                    fitwdg(GameUI.this.add(srchwnd, Utils.getprefc("wndc-srch", new Coord(200, 200))));
                } else {
                    if (!srchwnd.hasfocus) {
                        this.setfocus(srchwnd);
                    } else {
                        ui.destroy(srchwnd);
                        srchwnd = null;
                    }
                }
            }
        }, bg.c);
    }

    /* Ice cream */
    private final IButton[] fold_br = new IButton[4];
    private final IButton[] fold_bl = new IButton[2];

    private void updfold(boolean reset) {
        int br;
        if (brpanel.tvis && menupanel.tvis)
            br = 0;
        else if (brpanel.tvis && !menupanel.tvis)
            br = 1;
        else if (!brpanel.tvis && !menupanel.tvis)
            br = 2;
        else
            br = 3;
        for (int i = 0; i < fold_br.length; i++)
            fold_br[i].show(i == br);

        fold_bl[1].show(!blpanel.tvis);

        if (reset)
            resetui();
    }

    private void foldbuttons() {
        final Tex rdnbg = Resource.loadtex("gfx/hud/rbtn-maindwn");
        final Tex rupbg = Resource.loadtex("gfx/hud/rbtn-upbg");
        fold_br[0] = new IButton("gfx/hud/rbtn-dwn", "", "-d", "-h") {
            public void draw(GOut g) {
                g.image(rdnbg, Coord.z);
                super.draw(g);
            }

            public void click() {
                menupanel.cshow(false);
                updfold(true);
            }
        };
        fold_br[1] = new IButton("gfx/hud/rbtn-dwn", "", "-d", "-h") {
            public void draw(GOut g) {
                g.image(rdnbg, Coord.z);
                super.draw(g);
            }

            public void click() {
                brpanel.cshow(false);
                updfold(true);
            }
        };
        fold_br[2] = new IButton("gfx/hud/rbtn-up", "", "-d", "-h") {
            public void draw(GOut g) {
                g.image(rupbg, Coord.z);
                super.draw(g);
            }

            public void click() {
                menupanel.cshow(true);
                updfold(true);
            }

            public void presize() {
                this.c = parent.sz.sub(this.sz);
            }
        };
        fold_br[3] = new IButton("gfx/hud/rbtn-dwn", "", "-d", "-h") {
            public void draw(GOut g) {
                g.image(rdnbg, Coord.z);
                super.draw(g);
            }

            public void click() {
                brpanel.cshow(true);
                updfold(true);
            }
        };
        menupanel.add(fold_br[0], 0, 0);
        fold_br[0].lower();
        brpanel.adda(fold_br[1], brpanel.sz.x, 32, 1, 1);
        adda(fold_br[2], 1, 1);
        fold_br[2].lower();
        menupanel.add(fold_br[3], 0, 0);
        fold_br[3].lower();

        final Tex lupbg = Resource.loadtex("gfx/hud/lbtn-upbg");
        fold_bl[0] = new IButton("gfx/hud/lbtn-dwn", "", "-d", "-h") {
            public void click() {
                blpanel.cshow(false);
                updfold(true);
            }
        };
        fold_bl[1] = new IButton("gfx/hud/lbtn-up", "", "-d", "-h") {
            public void draw(GOut g) {
                g.image(lupbg, Coord.z);
                super.draw(g);
            }

            public void click() {
                blpanel.cshow(true);
                updfold(true);
            }

            public void presize() {
                this.c = new Coord(0, parent.sz.y - sz.y);
            }
        };
        blpanel.add(fold_bl[0], 0, 0);
        adda(fold_bl[1], 0, 1);
        fold_bl[1].lower();

        updfold(false);
    }

    @Override
    protected void attach(UI ui) {
        super.attach(ui);
        ui.gui = this;
    }

    protected void added() {
        resize(parent.sz);
        ui.gui = this;
        ui.cons.out = new java.io.PrintWriter(new java.io.Writer() {
            StringBuilder buf = new StringBuilder();

            public void write(char[] src, int off, int len) {
                List<String> lines = new ArrayList<String>();
                synchronized (this) {
                    buf.append(src, off, len);
                    int p;
                    while ((p = buf.indexOf("\n")) >= 0) {
                        lines.add(buf.substring(0, p));
                        buf.delete(0, p + 1);
                    }
                }
                for (String ln : lines)
                    syslog.append(ln, Color.WHITE);
            }

            public void close() {
            }

            public void flush() {
            }
        });
        Debug.log = ui.cons.out;
        buffs = add(new Bufflist(), new Coord(95, 85));
        if (!chrid.equals("")) {
            Utils.loadprefchklist("boulderssel_" + chrid, Config.boulders);
            Utils.loadprefchklist("bushessel_" + chrid, Config.bushes);
            Utils.loadprefchklist("treessel_" + chrid, Config.trees);
            Utils.loadprefchklist("iconssel_" + chrid, Config.icons);
            opts.setMapSettings();
            Config.discordchat = Utils.getprefb("discordchat_" + chrid, false);
            opts.discordcheckbox.a = Config.discordchat;
        }
        zerg = add(new Zergwnd(), new Coord(187, 50));
        if (!Config.autowindows.get("Kith & Kin").selected)
            zerg.hide();
        questwnd = add(new QuestWnd(), new Coord(0, sz.y - 200));
        chatwnd = add(new ChatWnd(chat = new ChatUI(600, 150)), new Coord(20, sz.y - 200));
        if (Config.autowindows.get("Chat") != null && Config.autowindows.get("Chat").selected)
            chatwnd.visible = false;
        syslog = chat.add(new ChatUI.Log("System"));
        opts.c = sz.sub(opts.sz).div(2);
        pointer = add(new MapPointer());
        livestockwnd = add(new LivestockManager(), new Coord(0, sz.y - 200));
        livestockwnd.hide();
        this.questhelper = add(new QuestHelper(), new Coord(0, sz.y - 200));
        this.questhelper.hide();
        hidden = add(new HiddenManager());
        hidden.hide();
        deleted = add(new DeletedManager());
        deleted.hide();
        alerted = add(new SoundManager());
        alerted.hide();
        gobspawner = add(new GobSpawner());
        gobspawner.hide();
        highlighted = add(new HighlightManager());
        highlighted.hide();
        overlayed = add(new OverlayManager());
        overlayed.hide();
        PBotScriptlist = add(new PBotScriptlist());
        PBotScriptlist.hide();
        PBotScriptlistold = add(new PBotScriptlistOld());
        PBotScriptlistold.hide();
        timerswnd = add(new haven.timers.TimersWnd(this));
        try {
            PBotDiscord.initalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!Config.autowindows.get("Timers").selected)
            timerswnd.hide();
        if (Config.sessiondisplay) {
            ui.root.sessionDisplay.unlink();
            add(ui.root.sessionDisplay);
        }
    }

    @Override
    public void destroy() {
        if (statuswindow != null) {//seems to be a bug that occasionally keeps the status window thread alive.
            statuswindow.reqdestroy();
        }
        ui.root.add(ui.root.sessionDisplay = new SessionDisplay());
        super.destroy();
        ui.gui = null;
    }

    public void beltPageSwitch1() {
        nbelt.page = 0;
        nbelt.upd_page();
    }

    public void beltPageSwitch2() {
        nbelt.page = 1;
        nbelt.upd_page();
    }

    public void beltPageSwitch3() {
        nbelt.page = 2;
        nbelt.upd_page();
    }

    public void beltPageSwitch4() {
        nbelt.page = 3;
        nbelt.upd_page();
    }

    public void beltPageSwitch5() {
        nbelt.page = 4;
        nbelt.upd_page();
    }

    public void toggleDebug() {
        Config.dbtext = !Config.dbtext;
    }


    public void toggleCraftDB() {
        if (craftwnd == null) {
            craftwnd = add(new CraftDBWnd());
        } else {
            craftwnd.close();
        }
    }

    public void toggleMenuSettings() {
        if (!opts.visible) {
            opts.show();
            opts.raise();
            fitwdg(opts);
            setfocus(opts);
            opts.chpanel(opts.flowermenus);
        } else {
            opts.show(false);
        }
    }

    public void toggleMapSettings() {
        if (!opts.visible) {
            opts.show();
            opts.raise();
            fitwdg(opts);
            setfocus(opts);
            opts.chpanel(opts.map);
        } else {
            opts.show(false);
        }
    }

    public void toggleGridLines() {
        if (map != null)
            map.togglegrid();
    }

    public void markTarget() {
        try {
            Gob g = null;
            if (fv != null && fv.current != null)
                g = map.glob.oc.getgob(fv.current.gobid);
            if (g != null) {
                g.mark(20000);
                for (Widget wdg = chat.lchild; wdg != null; wdg = wdg.prev) {
                    if (wdg instanceof ChatUI.PartyChat) {
                        final ChatUI.PartyChat chat = (ChatUI.PartyChat) wdg;
                        chat.send(String.format(Mark.CHAT_FMT, g.id, 20000));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closestTarget() {
        try {
            fv.targetClosestCombat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void peaceCurrent() {
        try {
            if (fv != null && fv.current != null && fv.curgive != null) {
                fv.curgive.mousedown(Coord.z, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleInventory() {
        if ((invwnd != null) && invwnd.show(!invwnd.visible)) {
            invwnd.raise();
            fitwdg(invwnd);
        }
    }

    public void toggleEquipment() {
        if ((equwnd != null) && equwnd.show(!equwnd.visible)) {
            equwnd.raise();
            fitwdg(equwnd);
        }
    }

    public void nextSess() {
        HashMap<Integer, UI> sessmap = new HashMap<Integer, UI>();
        int sess = 0;
        int activesess = 1;
        int sesscount = MainFrame.instance.p.sessionCount();
        try {
            if (sesscount > 1) {
                for (UI uiwdg : MainFrame.instance.p.sessions) {
                    sess++;
                    if (uiwdg == ui) {
                        activesess = sess;
                    }
                    sessmap.put(sess, uiwdg);
                }
                System.out.println("active : " + activesess + " count : " + sesscount + " sess : " + sess);
                if (activesess == sess) {//if we're the last sess in the list, loop around
                    MainFrame.instance.p.setActiveUI(sessmap.get(1));
                } else {
                    MainFrame.instance.p.setActiveUI(sessmap.get(activesess + 1));
                }
            } else {
                msg("There appears to only be 1 active session currently, cannot switch.", Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sessmap = null;
            msg("Error trying to switch to next session.", Color.WHITE);
        }
        sessmap = null;
    }

    public void prevSess() {
        HashMap<Integer, UI> sessmap = new HashMap<Integer, UI>();
        int sess = 0;
        int activesess = 1;
        int sesscount = MainFrame.instance.p.sessionCount();
        try {
            if (sesscount > 1) {
                for (UI uiwdg : MainFrame.instance.p.sessions) {
                    sess++;
                    if (uiwdg == ui) {
                        activesess = sess;
                    }
                    sessmap.put(sess, uiwdg);
                }
                System.out.println("active : " + activesess + " count : " + sesscount + " sess : " + sess);
                if (activesess == 1) {//if we're the first sess in the list, loop around
                    MainFrame.instance.p.setActiveUI(sessmap.get(sesscount));
                } else {
                    MainFrame.instance.p.setActiveUI(sessmap.get(activesess - 1));
                }
            } else {
                msg("There appears to only be 1 active session currently, cannot switch.", Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sessmap = null;
            msg("Error trying to switch to next session.", Color.WHITE);
        }
        sessmap = null;
    }

    public void toggleCharacter() {
        if ((chrwdg != null) && chrwdg.show(!chrwdg.visible)) {
            chrwdg.raise();
            fitwdg(chrwdg);
            setfocus(chrwdg);
        }
    }

    public void toggleKinList() {
        if (zerg.show(!zerg.visible)) {
            zerg.raise();
            fitwdg(zerg);
            //  setfocus(zerg);
        }
    }

    public void toggleQuestHelper() {
        if (questhelper.visible) {
            questhelper.show(false);
            questhelper.active = false;
        } else {
            questhelper.show(true);
            questhelper.active = true;
            questhelper.raise();
            questhelper.refresh();
        }
    }

    public void toggleOptions() {
        if (opts.show(!opts.visible)) {
            opts.raise();
            fitwdg(opts);
            setfocus(opts);
        }
    }

    public void toggleMap() {
        if ((mapfile != null) && mapfile.show(!mapfile.visible)) {
            mapfile.raise();
            fitwdg(mapfile);
            setfocus(mapfile);
        }
    }

    public void dispose() {
        Debug.log = new java.io.PrintWriter(System.err);
        ui.cons.clearout();
        super.dispose();
    }

    public class Hidepanel extends Widget {
        public final String id;
        public final Coord g;
        public final Indir<Coord> base;
        public boolean tvis;
        private double cur;

        public Hidepanel(String id, Indir<Coord> base, Coord g) {
            this.id = id;
            this.base = base;
            this.g = g;
            cur = show(tvis = Utils.getprefb(id + "-visible", true)) ? 0 : 1;
        }

        public <T extends Widget> T add(T child) {
            super.add(child);
            pack();
            if (parent != null)
                move();
            return (child);
        }

        public Coord base() {
            if (base != null) return (base.get());
            /*
            return (new Coord((g.x > 0) ? parent.sz.x : (g.x < 0) ? 0 : (parent.sz.x / 2),
                    (g.y > 0) ? parent.sz.y : (g.y < 0) ? 0 : (parent.sz.y / 2)));
                    */
            return (new Coord((g.x > 0) ? parent.sz.x : (g.x < 0) ? 0 : ((parent.sz.x - this.sz.x) / 2),
                    (g.y > 0) ? parent.sz.y : (g.y < 0) ? 0 : ((parent.sz.y - this.sz.y) / 2)));
        }

        public void move(double a) {
            cur = a;
            Coord c = new Coord(base());
            if (g.x < 0)
                c.x -= (int) (sz.x * a);
            else if (g.x > 0)
                c.x -= (int) (sz.x * (1 - a));
            if (g.y < 0)
                c.y -= (int) (sz.y * a);
            else if (g.y > 0)
                c.y -= (int) (sz.y * (1 - a));
            this.c = c;
        }

        public void move() {
            move(cur);
        }

        public void presize() {
            move();
        }

        public boolean mshow(final boolean vis) {
            clearanims(Anim.class);
            if (vis)
                show();
            new NormAnim(0.25) {
                final double st = cur, f = vis ? 0 : 1;

                public void ntick(double a) {
                    if ((a == 1.0) && !vis)
                        hide();
                    move(st + (Utils.smoothstep(a) * (f - st)));
                }
            };
            tvis = vis;
            updfold(false);
            return (vis);
        }

        public boolean mshow() {
            return (mshow(Utils.getprefb(id + "-visible", true)));
        }

        public boolean cshow(boolean vis) {
            Utils.setprefb(id + "-visible", vis);
            if (vis != tvis)
                mshow(vis);
            return (vis);
        }

        public void cdestroy(Widget w) {
            parent.cdestroy(w);
        }
    }

    public static class Hidewnd extends Window {
        Hidewnd(Coord sz, String cap, boolean lg) {
            super(sz, cap, lg);
        }

        Hidewnd(Coord sz, String cap) {
            super(sz, cap);
        }

        public void wdgmsg(Widget sender, String msg, Object... args) {
            if ((sender == this) && msg.equals("close")) {
                this.hide();
                return;
            }
            super.wdgmsg(sender, msg, args);
        }
    }

    static class Zergwnd extends Hidewnd {
        Tabs tabs = new Tabs(Coord.z, Coord.z, this);
        final TButton kin, pol, pol2;

        class TButton extends IButton {
            Tabs.Tab tab = null;
            final Tex inv;

            TButton(String nm, boolean g) {
                super(Resource.loadimg("gfx/hud/buttons/" + nm + "u"), Resource.loadimg("gfx/hud/buttons/" + nm + "d"));
                if (g)
                    inv = Resource.loadtex("gfx/hud/buttons/" + nm + "g");
                else
                    inv = null;
            }

            public void draw(GOut g) {
                if ((tab == null) && (inv != null))
                    g.image(inv, Coord.z);
                else
                    super.draw(g);
            }

            public void click() {
                if (tab != null) {
                    tabs.showtab(tab);
                    repack();
                }
            }
        }

        Zergwnd() {
            super(Coord.z, "Kith & Kin", true);
            kin = add(new TButton("kin", false));
            kin.tooltip = Text.render("Kin");
            pol = add(new TButton("pol", true));
            pol2 = add(new TButton("rlm", true));
        }

        private void repack() {
            tabs.indpack();
            kin.c = new Coord(0, tabs.curtab.contentsz().y + 20);
            pol.c = new Coord(kin.c.x + kin.sz.x + 10, kin.c.y);
            pol2.c = new Coord(pol.c.x + pol.sz.x + 10, pol.c.y);
            this.pack();
        }

        Tabs.Tab ntab(Widget ch, TButton btn) {
            Tabs.Tab tab = add(tabs.new Tab() {
                public void cresize(Widget ch) {
                    repack();
                }
            }, tabs.c);
            tab.add(ch, Coord.z);
            btn.tab = tab;
            repack();
            return (tab);
        }

        void dtab(TButton btn) {
            btn.tab.destroy();
            btn.tab = null;
            repack();
        }

        void addpol(Polity p) {
            /* This isn't very nice. :( */
            TButton btn = p.cap.equals("Village") ? pol : pol2;
            ntab(p, btn);
            btn.tooltip = Text.render(p.cap);
        }

        @Override
        public boolean show(boolean show) {
            if (show)
                gameui().buddies.clearSearch();
            return super.show(show);
        }
    }

    static class DraggedItem {
        final GItem item;
        final Coord dc;

        DraggedItem(GItem item, Coord dc) {
            this.item = item;
            this.dc = dc;
        }
    }

    private void updhand() {
        if ((hand.isEmpty() && (vhand != null)) || ((vhand != null) && !hand.contains(vhand.item))) {
            ui.destroy(vhand);
            vhand = null;
        }
        if (!hand.isEmpty() && (vhand == null)) {
            DraggedItem fi = hand.iterator().next();
            vhand = add(new ItemDrag(fi.dc, fi.item));
            if (map.lastItemactClickArgs != null)
                map.iteminteractreplay();
        }
    }

    public void DiscordToggle() {
        if (Discord.jdalogin != null) {
            msg("Discord Disconnected", Color.white);
            discordconnected = false;
            Discord.jdalogin.shutdownNow();
            Discord.jdalogin = null;
            for (int i = 0; i < 15; i++) {
                for (Widget w = chat.lchild; w != null; w = w.prev) {
                    if (w instanceof ChatUI.DiscordChat)
                        w.destroy();
                }
            }
        }
    }

    private String mapfilename() {
        StringBuilder buf = new StringBuilder();
        buf.append(genus);
        String chrid = Utils.getpref("mapfile/" + this.chrid, "");
        if (!chrid.equals("")) {
            if (buf.length() > 0) buf.append('/');
            buf.append(chrid);
        }
        return (buf.toString());
    }

    public void addcmeter(Widget meter) {
        int x = (meters.size() % 3) * (IMeter.fsz.x + 5);
        int y = (meters.size() / 3) * (IMeter.fsz.y + 2);
        add(meter, portrait.c.x + portrait.sz.x + 10 + x, portrait.c.y + y);
        // ulpanel.add(meter);
        cmeters.add(meter);
        updcmeters();
    }

    public void toggleHand() {
        if (hand.isEmpty()) {
            hand.addAll(handSave);
            handSave.clear();
            updhand();
        } else {
            handSave.addAll(hand);
            hand.clear();
            updhand();
        }
    }

    public void toggleGridBinds() {
        opts.menugridcheckbox.set(!opts.menugridcheckbox.a);
        if (opts.menugridcheckbox.a)
            msg("Menugrid keybinds are now disabled!", Color.white);
        else
            msg("Menugrid keybinds are now enabled!", Color.white);
    }

    public void toggleStudy() {
        studywnd.show(!studywnd.visible);
    }

    public void takeScreenshot() {
        if (Config.screenurl != null) {
            Screenshooter.take(this, Config.screenurl);
        }
    }

    public void localScreenshot() {
        if (Config.screenurl != null)
            HavenPanel.needtotakescreenshot = true;
    }

    public <T extends Widget> void delcmeter(Class<T> cl) {
        Widget widget = null;
        for (Widget meter : cmeters) {
            if (cl.isAssignableFrom(meter.getClass())) {
                widget = meter;
                break;
            }
        }
        if (widget != null) {
            cmeters.remove(widget);
            widget.destroy();
            updcmeters();
        }
    }


    private Coord getMeterPos(int x, int y) {
        return new Coord(portrait.c.x + portrait.sz.x + 10 + x * (IMeter.fsz.x + 5), portrait.c.y + y * (IMeter.fsz.y + 2));
    }

    public void addMeterAt(Widget m, int x, int y) {
        ulpanel.add(m, getMeterPos(x, y));
        ulpanel.pack();
    }


    private void updcmeters() {
        int i = 0;
        for (Widget meter : cmeters) {
            int x = ((meters.size() + i) % 3) * (IMeter.fsz.x + 5);
            int y = ((meters.size() + i) / 3) * (IMeter.fsz.y + 2);
            meter.c = new Coord(portrait.c.x + portrait.sz.x + 10 + x, portrait.c.y + y);
            i++;
        }
    }

    public Coord optplacement(Widget child, Coord org) {
        Set<Window> closed = new HashSet<>();
        Set<Coord> open = new HashSet<>();
        open.add(org);
        Coord opt = null;
        double optscore = Double.NEGATIVE_INFINITY;
        Coord plc = null;
        {
            Gob pl = map.player();
            if (pl != null) {
                Coord3f raw = pl.placed.getc();
                if (raw != null)
                    plc = map.screenxf(raw).round2();
            }
        }
        Area parea = Area.sized(Coord.z, sz);
        while (!open.isEmpty()) {
            Coord cur = Utils.take(open);
            double score = 0;
            Area tarea = Area.sized(cur, child.sz);
            if (parea.isects(tarea)) {
                double outside = 1.0 - (((double) parea.overlap(tarea).area()) / ((double) tarea.area()));
                if ((outside > 0.75) && !cur.equals(org))
                    continue;
                score -= Math.pow(outside, 2) * 100;
            } else {
                if (!cur.equals(org))
                    continue;
                score -= 100;
            }
            {
                boolean any = false;
                for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
                    if (!(wdg instanceof Window))
                        continue;
                    Window wnd = (Window) wdg;
                    if (!wnd.visible)
                        continue;
                    Area warea = wnd.parentarea(this);
                    if (warea.isects(tarea)) {
                        any = true;
                        score -= ((double) warea.overlap(tarea).area()) / ((double) tarea.area());
                        if (!closed.contains(wnd)) {
                            open.add(new Coord(wnd.c.x - child.sz.x, cur.y));
                            open.add(new Coord(cur.x, wnd.c.y - child.sz.y));
                            open.add(new Coord(wnd.c.x + wnd.sz.x, cur.y));
                            open.add(new Coord(cur.x, wnd.c.y + wnd.sz.y));
                            closed.add(wnd);
                        }
                    }
                }
                if (!any)
                    score += 10;
            }
            if (plc != null) {
                if (tarea.contains(plc))
                    score -= 100;
                else
                    score -= (1 - Math.pow(tarea.closest(plc).dist(plc) / sz.dist(Coord.z), 2)) * 1.5;
            }
            score -= (cur.dist(org) / sz.dist(Coord.z)) * 0.75;
            if (score > optscore) {
                optscore = score;
                opt = cur;
            }
        }
        return (opt);
    }

    private void savewndpos() {
        if (invwnd != null)
            Utils.setprefc("wndc-inv", invwnd.c);
        if (equwnd != null)
            Utils.setprefc("wndc-equ", equwnd.c);
        if (chrwdg != null)
            Utils.setprefc("wndc-chr", chrwdg.sz);
        if (zerg != null)
            Utils.setprefc("wndc-zerg", zerg.c);
        if (mapfile != null) {
            Utils.setprefc("wndc-map", mapfile.c);
            Utils.setprefc("wndsz-map", mapfile.asz);
        }
    }

    private final BMap<String, Window> wndids = new HashBMap<String, Window>();

    public void addchild(Widget child, Object... args) {
        String place = ((String) args[0]).intern();
        if (place == "mapview") {
            child.resize(sz);
            map = add((MapView) child, Coord.z);
            map.lower();
            if (mmap != null)
                ui.destroy(mmap);
            if (mapfile != null) {
                ui.destroy(mapfile);
                mapfile = null;
            }
            mmap = blpanel.add(new LocalMiniMap(new Coord(133, 133), map), minimapc);
            mmap.lower();
            ResCache mapstore = ResCache.global;
            if (Config.mapbase != null) {
                try {
                    mapstore = HashDirCache.get(Config.mapbase.toURI());
                } catch (java.net.URISyntaxException e) {
                }
            }
            if (mapstore != null) {
                MapFile file = MapFile.load(mapstore, mapfilename());
                if (Config.vendanMapv4) {
                    MappingClient.getInstance().ProcessMap(file, (m) -> {
                        if (m instanceof MapFile.PMarker && Config.vendanGreenMarkers) {
                            return ((MapFile.PMarker) m).color.equals(Color.GREEN);
                        }
                        return true;
                    });
                }
                mmap.save(file);
                mapfile = new MapWnd(mmap.save, map, Utils.getprefc("wndsz-map", new Coord(700, 500)), "Map");
                mapfile.hide();
                add(mapfile, Utils.getprefc("wndc-map", new Coord(50, 50)));
            }

            if (trackon) {
                buffs.addchild(new Buff(Bufflist.bufftrack.indir()));
                msgnosfx(Resource.getLocString(Resource.BUNDLE_MSG, "Tracking is now turned on."));
            }
            if (crimeon) {
                buffs.addchild(new Buff(Bufflist.buffcrime.indir()));
                msgnosfx(Resource.getLocString(Resource.BUNDLE_MSG, "Criminal acts are now turned on."));
            }
            if (swimon) {
                buffs.addchild(new Buff(Bufflist.buffswim.indir()));
                msgnosfx(Resource.getLocString(Resource.BUNDLE_MSG, "Swimming is now turned on."));
            }
            if (partyperm) {
                buffs.addchild(new Buff(Bufflist.partyperm.indir()));
                msgnosfx(Resource.getLocString(Resource.BUNDLE_MSG, "Party permissions are now turned on."));
            }
        } else if (place == "menu") {
            menu = (MenuGrid) brpanel.add(child, menugridc);
            final BeltData data = new BeltData(Context.accname + "::" + Context.charname);
            fbelt = add(new BeltWnd("fk", data, KeyEvent.VK_F1, KeyEvent.VK_F10, 5, 50), new Coord(0, 50));
            npbelt = add(new BeltWnd("np", data, KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD9, 4, 100), new Coord(0, 100));
            nbelt = add(new BeltWnd("n", data, KeyEvent.VK_0, KeyEvent.VK_9, 5, 0), new Coord(0, 150));
            menuSearch = add(new MenuSearch("Search..."));
            if (!Config.autowindows.get("Search...").selected)
                menuSearch.hide();
            filter = add(new FilterWnd("Filter"));
            filter.hide();
        } else if (place == "fight") {
            fv = urpanel.add((Fightview) child, 0, 0);
        } else if (place == "fsess") {
            add(child, Coord.z);
        } else if (place == "inv") {
            invwnd = new Hidewnd(Coord.z, "Inventory") {
                public void cresize(Widget ch) {
                    pack();
                }
            };
            invwnd.add(maininv = (Inventory) child, Coord.z);
            invwnd.pack();
            invwnd.show(Config.autowindows.get("Inventory").selected);
            add(invwnd, Utils.getprefc("wndc-inv", new Coord(100, 100)));
        } else if (place == "equ") {
            equwnd = new Hidewnd(Coord.z, "Equipment");
            equipory = equwnd.add((Equipory) child, Coord.z);
            equwnd.pack();
            equwnd.hide();
            add(equwnd, Utils.getprefc("wndc-equ", new Coord(400, 10)));
            equwnd.show(Config.autowindows.get("Equipment").selected);
        } else if (place == "hand") {
            GItem g = add((GItem) child);
            Coord lc = (Coord) args[1];
            hand.add(new DraggedItem(g, lc));
            updhand();
        } else if (place == "chr") {
            studywnd = add(new StudyWnd(), new Coord(400, 100));
            if (!Config.autowindows.get("Study").selected)
                studywnd.hide();
            chrwdg = add((CharWnd) child, Utils.getprefc("wndc-chr", new Coord(300, 50)));
            if (!Config.autowindows.get("Character Sheet").selected)
                chrwdg.hide();
            if (Config.hungermeter)
                addcmeter(new HungerMeter(chrwdg.glut, "HungerMeter"));
            if (Config.fepmeter)
                addcmeter(new FepMeter(chrwdg.feps, "FepMeter"));
        } else if (place == "craft") {
            final Widget mkwdg = child;
            if (craftwnd != null) {
                craftwnd.setMakewindow(mkwdg);
            } else {
                makewnd = new Window(Coord.z, "Crafting", true) {
                    public void wdgmsg(Widget sender, String msg, Object... args) {
                        if ((sender == this) && msg.equals("close")) {
                            mkwdg.wdgmsg("close");
                            return;
                        }
                        super.wdgmsg(sender, msg, args);
                    }

                    public void cdestroy(Widget w) {
                        if (w == mkwdg) {
                            ui.destroy(this);
                            makewnd = null;
                        }
                    }

                    public void destroy() {
                        Utils.setprefc("makewndc", makewndc = this.c);
                        super.destroy();
                    }
                };
                makewnd.add(mkwdg, Coord.z);
                makewnd.pack();
                makewnd.raise();
                makewnd.show();
                fitwdg(add(makewnd, makewndc));
            }
        } else if (place == "buddy") {
            zerg.ntab(buddies = (BuddyWnd) child, zerg.kin);
        } else if (place == "pol") {
            Polity p = (Polity) child;
            polities.add(p);
            zerg.addpol(p);
        } else if (place == "chat") {
            chat.addchild(child);
        } else if (place == "party") {
            add(child, 10, 95);
        } else if (place == "meter") {
            int x = (meters.size() % 3) * (IMeter.fsz.x + 5);
            int y = (meters.size() / 3) * (IMeter.fsz.y + 2);
            ulpanel.add(child, portrait.c.x + portrait.sz.x + 10 + x, portrait.c.y + y);
            meters.add(child);
            updcmeters();
        } else if (place == "buff") {
            buffs.addchild(child);
        } else if (place == "qq") {
            if (qqview != null)
                qqview.reqdestroy();
            final Widget cref = qqview = child;
            add(new AlignPanel() {
                {
                    add(cref);
                }

                protected Coord getc() {
                    return (new Coord(10, GameUI.this.sz.y - blpanel.sz.y - this.sz.y - 10));
                }

                public void cdestroy(Widget ch) {
                    qqview = null;
                    destroy();
                }
            });
            questwnd.add(child, Coord.z);
        } else if (place == "misc") {
            Coord c;
            int a = 1;
            if (args[a] instanceof Coord) {
                c = (Coord) args[a++];
            } else if (args[a] instanceof Coord2d) {
                c = ((Coord2d) args[a++]).mul(new Coord2d(this.sz.sub(child.sz))).round();
                c = optplacement(child, c);
            } else if (args[a] instanceof String) {
                c = relpos((String) args[a++], child, (args.length > a) ? ((Object[]) args[a++]) : new Object[]{}, 0);
            } else {
                throw (new UI.UIException("Illegal gameui child", place, args));
            }
            while (a < args.length) {
                Object opt = args[a++];
                if (opt instanceof Object[]) {
                    Object[] opta = (Object[]) opt;
                    switch ((String) opta[0]) {
                        case "id":
                            String wndid = (String) opta[1];
                            if (child instanceof Window) {
                                c = Utils.getprefc(String.format("wndc-misc/%s", (String) opta[1]), c);
                                if (!wndids.containsKey(wndid)) {
                                    c = fitwdg(child, c);
                                    wndids.put(wndid, (Window) child);
                                } else {
                                    c = optplacement(child, c);
                                }
                            }
                            break;
                    }
                }
            }
            add(child, c);
        } else if (place == "abt") {
            add(child, Coord.z);
        } else {
            throw (new UI.UIException("Illegal gameui child", place, args));
        }
    }

    public void cdestroy(Widget w) {
        if (w instanceof Window) {
            String wndid = wndids.reverse().get((Window) w);
            if (wndid != null) {
                wndids.remove(wndid);
                Utils.setprefc(String.format("wndc-misc/%s", wndid), w.c);
            }
        }
        if (w instanceof GItem) {
            for (Iterator<DraggedItem> i = hand.iterator(); i.hasNext(); ) {
                DraggedItem di = i.next();
                if (di.item == w) {
                    i.remove();
                    updhand();
                }
            }
        } else if (w instanceof Polity && polities.contains(w)) {
            polities.remove(w);
            zerg.dtab(zerg.pol);
        } else if (w == chrwdg) {
            chrwdg = null;
        }
        if (meters.remove(w))
            updcmeters();
        cmeters.remove(w);
    }

    private static final Resource.Anim progt = Resource.local().loadwait("gfx/hud/prog").layer(Resource.animc);
    private Tex curprog = null;
    private int curprogf, curprogb;

    private void drawprog(GOut g, double prog) {
        int fr = Utils.clip((int) Math.floor(prog * progt.f.length), 0, progt.f.length - 2);
        int bf = Utils.clip((int) (((prog * progt.f.length) - fr) * 255), 0, 255);
        if ((curprog == null) || (curprogf != fr) || (curprogb != bf)) {
            if (curprog != null)
                curprog.dispose();
            WritableRaster buf = PUtils.imgraster(progt.f[fr][0].sz);
            PUtils.blit(buf, progt.f[fr][0].img.getRaster(), Coord.z);
            PUtils.blendblit(buf, progt.f[fr + 1][0].img.getRaster(), Coord.z, bf);
            curprog = new TexI(PUtils.rasterimg(buf));
            curprogf = fr;
            curprogb = bf;
        }
        Coord hgc = new Coord(sz.x / 2, (sz.y * 4) / 10);
        g.aimage(curprog, hgc, 0.5, 0.5);
        if (Config.showprogressperc)
            g.atextstroked((int) (prog * 100) + "%", hgc, 0.5, 2.5, Color.WHITE, Color.BLACK, Text.num12boldFnd);
    }

    public void draw(GOut g) {
        beltwdg.c = new Coord(chat.c.x, Math.min(chat.c.y - beltwdg.sz.y + 4, sz.y - beltwdg.sz.y));
        super.draw(g);
        if (prog >= 0)
            drawprog(g, prog);
        int by = sz.y;
        if (chat.visible)
            by = Math.min(by, chat.c.y);
        if (beltwdg.visible)
            by = Math.min(by, beltwdg.c.y);
        if (cmdline != null) {
            drawcmd(g, new Coord(blpw + 10, by -= 20));
        } else if (lastmsg != null) {
            if ((Utils.rtime() - msgtime) > 3.0) {
                lastmsg = null;
            } else {
                g.chcolor(0, 0, 0, 192);
                g.frect(new Coord(blpw + 8, by - 22), lastmsg.sz().add(4, 4));
                g.chcolor();
                g.image(lastmsg.tex(), new Coord(blpw + 10, by -= 20));
            }
        }
        if (!chat.visible) {
            chat.drawsmall(g, new Coord(blpw + 10, by), 50);
        }
    }

    private double lastwndsave = 0;

    public void tick(double dt) {
        super.tick(dt);
        double now = Utils.rtime();
        try {
            IMeter.Meter stam = getmeter("stam", 0);
            if (Config.temporaryswimming && temporarilyswimming) {
                if (System.currentTimeMillis() - SwimTimer >= 30000) {
                    SwimTimer = 0;
                    temporarilyswimming = false;
                    PBotUtils.doAct("swim");
                }
            }
            if (!drinkingWater && Config.autodrink && (DrinkThread == null || !DrinkThread.isAlive()) && stam.a < Config.autodrinkthreshold) {
                if (System.currentTimeMillis() - DrinkTimer >= Config.autodrinktime * 1000) {
                    DrinkTimer = System.currentTimeMillis();
                    new Thread(new DrinkWater(this)).start();
                }
            }
            int energy = getmeter("nrj", 0).a;
            if (energy < 21 && System.currentTimeMillis() - StarvationAlertDelay > 10000 && Config.StarveAlert) {
                StarvationAlertDelay = System.currentTimeMillis();
                PBotUtils.sysMsg("You are Starving!", Color.white);
            }
        } catch (Exception e) {
        }//exceptions doing these two things aren't critical, ignore
        if (now - lastwndsave > 60) {
            savewndpos();
            lastwndsave = now;
        }
        double idle = now - ui.lastevent;
        if (!afk && (idle > 300)) {
            afk = true;
            wdgmsg("afk");
            if (Config.afklogouttime != 0) {
                if (idle > Config.afklogouttime * 60)
                    logoutChar();
            }
        } else if (afk && (idle <= 300)) {
            afk = false;
        }

    }

    private void togglebuff(String err, Resource res) {
        String name = res.basename();
        if (err.endsWith("on.") && buffs.gettoggle(name) == null) {
            buffs.addchild(new Buff(res.indir()));
            if (name.equals("swim"))
                swimon = true;
            else if (name.equals("crime"))
                crimeon = true;
            else if (name.equals("tracking"))
                trackon = true;
        } else if (err.endsWith("off.")) {
            Buff tgl = buffs.gettoggle(name);
            if (tgl != null)
                tgl.reqdestroy();
            if (name.equals("swim"))
                swimon = false;
            else if (name.equals("crime"))
                crimeon = false;
            else if (name.equals("tracking"))
                trackon = false;
        }
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "err") {
            String err = (String) args[0];
            error(err);
        } else if (msg == "msg") {
            String text = (String) args[0];
            if (text.startsWith("Swimming is now turned")) {
                togglebuff(text, Bufflist.buffswim);
                if (swimautotgld) {
                    msgnosfx(text);
                    swimautotgld = false;
                    return;
                }
            } else if (text.startsWith("Tracking is now turned")) {
                togglebuff(text, Bufflist.bufftrack);
                if (trackautotgld) {
                    msgnosfx(text);
                    trackautotgld = false;
                    return;
                }
            } else if (text.startsWith("Criminal acts are now turned")) {
                togglebuff(text, Bufflist.buffcrime);
                if (crimeautotgld) {
                    msgnosfx(text);
                    crimeautotgld = false;
                    return;
                }
            } else if (text.startsWith("Party permissions are now")) {
                togglebuff(text, Bufflist.partyperm);
            }
            msg(text);
        } else if (msg == "prog") {
            if (args.length > 0)
                prog = ((Number) args[0]).doubleValue() / 100.0;
            else
                prog = -1;
        } else if (msg == "setbelt") {
            int slot = (Integer) args[0];
            if (args.length < 2) {
                belt[slot] = null;
            } else {
                Indir<Resource> res = ui.sess.getres((Integer) args[1]);
                Message sdt = Message.nil;
                if (args.length > 2)
                    sdt = new MessageBuf((byte[]) args[2]);
                belt[slot] = new BeltSlot(slot, res, sdt);
            }
            if (slot <= 49)
                nbelt.update(slot);
            else if (slot <= 99)
                fbelt.update(slot);
            else
                npbelt.update(slot);
        } else if (msg == "polowner") {
            int id = (Integer) args[0];
            String o = (String) args[1];
            boolean n = ((Integer) args[2]) != 0;
            if (o != null)
                o = o.intern();
            String cur = polowners.get(id);
            if (map != null) {
                if ((o != null) && (cur == null)) {
                    if (Config.DivertPolityMessages)
                        PBotUtils.sysMsg("Entering " + o, Color.GREEN);
                    else
                        map.setpoltext(id, "Entering " + o);
                } else if ((o == null) && (cur != null)) {
                    if (Config.DivertPolityMessages)
                        PBotUtils.sysMsg("Leaving " + cur, Color.GREEN);
                    else
                        map.setpoltext(id, "Leaving " + cur);
                }
            }
            polowners.put(id, o);
        } else if (msg == "showhelp") {
            Indir<Resource> res = ui.sess.getres((Integer) args[0]);
            if (help == null)
                help = adda(new HelpWnd(res), 0.5, 0.5);
            else
                help.res = res;
        } else if (msg == "map-mark") {
            long gobid = ((Integer) args[0]) & 0xffffffff;
            long oid = (Long) args[1];
            Indir<Resource> res = ui.sess.getres((Integer) args[2]);
            String nm = (String) args[3];
            if (mapfile != null)
                mapfile.markobj(gobid, oid, res, nm);
        } else {
            super.uimsg(msg, args);
        }
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (msg.equals("close")) {
            if (sender == chrwdg) {
                chrwdg.hide();
                return;
            } else if (sender == mapfile) {
                mapfile.hide();
                return;
            } else if (sender == help) {
                ui.destroy(help);
                help = null;
                return;
            } else if ((sender == srchwnd)) {
                ui.destroy(srchwnd);
                srchwnd = null;
                return;
            } else if ((polities.contains(sender)) && (msg == "close"))
                sender.hide();
        }
        super.wdgmsg(sender, msg, args);
    }

    private Coord fitwdg(Widget wdg, Coord c) {
        Coord ret = new Coord(c);
        if (ret.x < 0)
            ret.x = 0;
        if (ret.y < 0)
            ret.y = 0;
        if (ret.x + wdg.sz.x > sz.x)
            ret.x = sz.x - wdg.sz.x;
        if (ret.y + wdg.sz.y > sz.y)
            ret.y = sz.y - wdg.sz.y;
        return (ret);
    }

    private void fitwdg(Widget wdg) {
        wdg.c = fitwdg(wdg, wdg.c);
    }

    public static class MenuButton extends IButton {
        private final KeyBinding gkey;
        private final String tt;

        MenuButton(String base, KeyBinding gkey, String tooltip) {
            super("gfx/hud/" + base, "", "-d", "-h");
            this.gkey = gkey;
            this.tt = tooltip;
        }

        public void click() {
        }

        public boolean globtype(char key, KeyEvent ev) {
            if (gkey.key().match(ev)) {
                click();
                return (true);
            }
            return (super.globtype(key, ev));
        }

        private RichText rtt = null;

        public Object tooltip(Coord c, Widget prev) {
            if (!checkhit(c))
                return (null);
            if ((prev != this) || (rtt == null)) {
                String tt = this.tt;
                if (gkey.key() != KeyMatch.nil)
                    tt += String.format(" ($col[255,255,0]{%s})", RichText.Parser.quote(gkey.key().name()));
                if ((rtt == null) || !rtt.text.equals(tt))
                    rtt = RichText.render(tt, 0);
            }
            return (rtt.tex());
        }
    }

    public static class MenuButton2 extends IButton {
        private final Action action;
        private final String tip;

        MenuButton2(String base, String tooltip, Action action) {
            super("gfx/hud/mainmenu/" + base, "", "-d", "-h");
            this.action = action;
            this.tip = tooltip;
            // KeyBinder.add(code, mods, action);
        }

        @Override
        public Object tooltip(Coord c, Widget prev) {
            if (!checkhit(c)) {
                return null;
            }
            KeyBinder.KeyBind bind = KeyBinder.get(action);
            String tt = tip;
            if (bind != null && !bind.isEmpty()) {
                tt = String.format("%s ($col[255,255,0]{%s})", tip, bind.shortcut());
            }
            return RichText.render(tt, 0);
        }

        @Override
        public void click() {
            action.run(ui.gui);
        }
    }

    public static final KeyBinding kb_inv = KeyBinding.get("inv", KeyMatch.forcode(KeyEvent.VK_TAB, 0));
    public static final KeyBinding kb_equ = KeyBinding.get("equ", KeyMatch.forchar('E', KeyMatch.C));
    public static final KeyBinding kb_chr = KeyBinding.get("chr", KeyMatch.forchar('T', KeyMatch.C));
    public static final KeyBinding kb_bud = KeyBinding.get("bud", KeyMatch.forchar('B', KeyMatch.C));
    public static final KeyBinding kb_opt = KeyBinding.get("opt", KeyMatch.forchar('O', KeyMatch.C));
    private static final Tex menubg = Resource.loadtex("gfx/hud/rbtn-bg");

    public class MainMenu extends Widget {
        public MainMenu() {
            super(menubg.sz());
            add(new MenuButton2("rbtn-src", "Menu Search", TOGGLE_SEARCH), 1, 1); //FIXME new menu search
            add(new MenuButton2("rbtn-inv", "Inventory", TOGGLE_INVENTORY), 34, 1);
            add(new MenuButton2("rbtn-equ", "Equipment", TOGGLE_EQUIPMENT), 67, 1);
            add(new MenuButton2("rbtn-chr", "Character Sheet", TOGGLE_CHARACTER), 100, 1);
            add(new MenuButton2("rbtn-bud", "Kith & Kin", TOGGLE_KIN_LIST), 133, 1);
            add(new MenuButton2("rbtn-opt", "Options", TOGGLE_OPTIONS), 166, 1);
        }

        public void draw(GOut g) {
            g.image(menubg, Coord.z);
            super.draw(g);
        }
    }

    public void SwitchTargets() {
        Fightview.Relation cur = fv.current;
        if (cur != null) {
            fv.lsrel.remove(cur);
            fv.lsrel.addLast(cur);
            fv.wdgmsg("bump", (int) fv.lsrel.get(0).gobid);
        }
    }

    public void toggleGobs() {
        Config.showboundingboxes = !Config.showboundingboxes;
        Utils.setprefb("showboundingboxes", Config.showboundingboxes);
        if (map != null)
            map.refreshGobsAll();
    }


    void toggleHighlight() {
        if (highlighted != null && highlighted.show(!highlighted.visible)) {
            highlighted.raise();
            fitwdg(highlighted);
            setfocus(highlighted);
        }
    }

    void toggleOverlay() {
        if (overlayed != null && overlayed.show(!overlayed.visible)) {
            overlayed.raise();
            fitwdg(overlayed);
            setfocus(overlayed);
        }
    }

    void toggleHidden() {
        if (hidden != null && hidden.show(!hidden.visible)) {
            hidden.raise();
            fitwdg(hidden);
            setfocus(hidden);
        }
    }

    public void OpenChat() {
        if (chatwnd != null && chatwnd.show(!chatwnd.visible)) {
            chatwnd.raise();
            fitwdg(chatwnd);
            setfocus(chatwnd);
        }
    }

    void toggleAlerted() {
        if (alerted != null && alerted.show(!alerted.visible)) {
            alerted.raise();
            fitwdg(alerted);
            setfocus(alerted);
        }
    }

    void toggleGobSpawner() {
        if (gobspawner != null && gobspawner.show(!gobspawner.visible)) {
            gobspawner.raise();
            fitwdg(gobspawner);
            setfocus(gobspawner);
        }
    }

    public void toggleKin() {
        if (zerg.show(!zerg.visible)) {
            zerg.raise();
            fitwdg(zerg);
            setfocus(zerg);
        }
    }

    void toggleInv() {
        if ((invwnd != null) && invwnd.show(!invwnd.visible)) {
            invwnd.raise();
            fitwdg(invwnd);
            setfocus(invwnd);
        }
    }


    void toggleCharWnd() {
        if ((chrwdg != null) && chrwdg.show(!chrwdg.visible)) {
            chrwdg.raise();
            fitwdg(chrwdg);
            setfocus(chrwdg);
        }
    }

    public void toggleMapfile() {
        if ((mapfile != null) && mapfile.show(!mapfile.visible)) {
            mapfile.raise();
            fitwdg(mapfile);
            setfocus(mapfile);
        }
    }

    public void toggleChat() {
        if (chatwnd.visible && !chat.hasfocus) {
            setfocus(chat);
        } else if (chatwnd.visible && chat.hasfocus) {
            // OpenChat();
            setfocus(maininv);
        } else {
            if (!chatwnd.visible) {
                OpenChat();
            } else {
                setfocus(chat);
            }
        }
    }

    public void toggleMinimap() {
        if (mmapwnd != null && mmapwnd.show(!mmapwnd.visible)) {
            mmapwnd.raise();
            fitwdg(mmapwnd);
            setfocus(mmapwnd);
        }
    }

    public void toggleMapGrid() {
        Config.mapshowgrid = !Config.mapshowgrid;
        Utils.setprefb("mapshowgrid", Config.mapshowgrid);
    }

    public void toggleMapViewDist() {
        Config.mapshowviewdist = !Config.mapshowviewdist;
        Utils.setprefb("mapshowviewdist", Config.mapshowviewdist);
    }

    public void toggleMute() {
        if (Audio.volume > 0) {
            PBotUtils.sysMsg("Audio muted.", Color.white);
            Audio.volume = 0;
        } else {
            Audio.volume = Double.parseDouble(Utils.getpref("sfxvol", "1.0"));
            PBotUtils.sysMsg("Audio un-muted.", Color.white);
        }
    }

    public void logout() {
        if (Discord.jdalogin != null)
            DiscordToggle();
        //  act("lo");
        ui.sess.close();
    }

    public void logoutChar() {
        if (Discord.jdalogin != null)
            DiscordToggle();
        act("lo", "cs");
    }

    public void toggleTreeStage() {
        Config.showplantgrowstage = !Config.showplantgrowstage;
        Utils.setprefb("showplantgrowstage", Config.showplantgrowstage);
        if (!Config.showplantgrowstage && map != null)
            map.removeCustomSprites(Sprite.GROWTH_STAGE_ID);
        if (map != null)
            map.refreshGobsGrowthStages();
    }

    public void toggleSearch() {
        KeyBinder.KeyBind k = KeyBinder.get(TOGGLE_SEARCH);
        if (menuSearch.show(!menuSearch.visible)) {
            menuSearch.raise();
            fitwdg(menuSearch);
            if (k.mods == 4)
                menuSearch.ignoreinit = true;
        }
    }

    public void doNothing() {
        //ugly hack to stop unbound keybinds from being triggered
    }

    public void toggleDaylight() {
        DefSettings.NIGHTVISION.set(!DefSettings.NIGHTVISION.get());
    }

    public void toggleFilter() {
        if (filter.show(!filter.visible)) {
            filter.raise();
            fitwdg(filter);
        }
    }

    public void toggleUI() {
        TexGL.disableall = !TexGL.disableall;
    }

    public void harvestForageable() {
        Thread t = new Thread(new PickForageable(this), "PickForageable");
        t.start();
    }

    public void traverse() {
        Thread t = new Thread(new Traverse(this), "Traverse");
        t.start();
    }

    public void toggleDangerRadius() {
        Config.showminerad = !Config.showminerad;
        msg("Mine support radii are now : " + Config.showminerad, Color.white);
        Utils.setprefb("showminerad", Config.showminerad);
    }

    public void toggleSafeRadius() {
        if (saferadius == 1) {
            saferadius = 2;
            Config.showTroughrad = false;
            Config.showBeehiverad = true;
            Utils.setprefb("showTroughrad", Config.showTroughrad);
            Utils.setprefb("showBeehiverad", Config.showBeehiverad);
            PBotUtils.sysMsg("Troughs off, Beehives on.", Color.white);
        } else if (saferadius == 2) {
            saferadius = 3;
            Config.showTroughrad = true;
            Config.showBeehiverad = true;
            Utils.setprefb("showTroughrad", Config.showTroughrad);
            Utils.setprefb("showBeehiverad", Config.showBeehiverad);
            PBotUtils.sysMsg("Troughs on, Beehives on.", Color.white);
        } else if (saferadius == 3) {
            saferadius = 4;
            Config.showTroughrad = true;
            Config.showBeehiverad = false;
            Utils.setprefb("showTroughrad", Config.showTroughrad);
            Utils.setprefb("showBeehiverad", Config.showBeehiverad);
            PBotUtils.sysMsg("Troughs on, Beehives off.", Color.white);
        } else if (saferadius == 4) {
            saferadius = 1;
            Config.showTroughrad = false;
            Config.showBeehiverad = false;
            Utils.setprefb("showTroughrad", Config.showTroughrad);
            Utils.setprefb("showBeehiverad", Config.showBeehiverad);
            PBotUtils.sysMsg("Troughs off, Beehives off.", Color.white);
        }

    }

    public void toggleStatusWidget() {
        if (Config.statuswdgvisible) {
            if (statuswindow != null)
                statuswindow.reqdestroy();
            Config.statuswdgvisible = false;
            Utils.setprefb("statuswdgvisible", false);
        } else {
            statuswindow = new StatusWdg();
            add(statuswindow, new Coord(HavenPanel.w / 2 + 80, 10));
            Config.statuswdgvisible = true;
            Utils.setprefb("statuswdgvisible", true);
        }
    }

    void toggleDeleted() {
        if (deleted != null && deleted.show(!deleted.visible)) {
            deleted.raise();
            fitwdg(deleted);
            setfocus(deleted);
        }
    }

    public void toggleHide() {
        Config.hidegobs = !Config.hidegobs;
        Utils.setprefb("hidegobs", Config.hidegobs);
        if (map != null)
            map.refreshGobsAll();
        if (Config.hidegobs)
            msg("Gobs are now hidden.", Color.white);
        else
            msg("Gobs are now NOT hidden.", Color.white);
    }

    public void toggleHiddenGobs() {
        Config.hideuniquegobs = !Config.hideuniquegobs;
        Utils.setprefb("hideuniquegobs", Config.hideuniquegobs);
        if (map != null)
            map.refreshGobsAll();
        if (Config.hideuniquegobs)
            msg("Unique gobs are now hidden.", Color.white);
        else
            msg("Unique gobs are now NOT hidden.", Color.white);
    }

    public void toggleGridCentering() {
        Config.tilecenter = !Config.tilecenter;
        Utils.setprefb("tilecenter", Config.tilecenter);
        msg("Tile centering is now turned " + (Config.tilecenter ? "on." : "off."), Color.WHITE);
    }


    public void togglePathfinding() {
        Config.pf = !Config.pf;
        msg("Pathfinding is now turned " + (Config.pf ? "on" : "off"), Color.WHITE);
    }

    public void aggroClosest() {
        if (map != null)
            map.aggroclosest();
    }

    public void attack() {
        try {
            this.act("aggro");
        } catch (Exception e) {

        }
    }


    public void rightHand() {
        if (configuration.newQuickSlotWdg) {
            newquickslots.drop(newQuickSlotsWdg.items[0].coord, Coord.z);
            newquickslots.simulateclick(newQuickSlotsWdg.items[0].coord);
        } else {
            quickslots.drop(QuickSlotsWdg.lc, Coord.z);
            quickslots.simulateclick(QuickSlotsWdg.lc);
        }
    }

    public void changeDecks(int deck) {
        FightWnd fightwdg = ui.fightwnd.get();
        if (fightwdg != null)
            fightwdg.changebutton(deck);
    }

    public void leftHand() {
        if (configuration.newQuickSlotWdg) {
            newquickslots.drop(newQuickSlotsWdg.items[1].coord, Coord.z);
            newquickslots.simulateclick(newQuickSlotsWdg.items[1].coord);
        } else {
            quickslots.drop(QuickSlotsWdg.rc, Coord.z);
            quickslots.simulateclick(QuickSlotsWdg.rc);
        }
    }

    public void Drink() {
        new Thread(new DrinkWater(this)).start();
    }


    public void crawlSpeed() {
        if (speed != null)
            speed.set(0);
    }

    public void walkSpeed() {
        if (speed != null)
            speed.set(1);
    }

    public void runSpeed() {
        if (speed != null)
            speed.set(2);
    }

    public void sprintSpeed() {
        if (speed != null)
            speed.set(3);
    }

    public void cycleSpeed() {
        if (speed != null) {
            if (speed.max >= 0) {
                int n;
                if (speed.cur > speed.max)
                    n = 0;
                else
                    n = (speed.cur + 1) % (speed.max + 1);
                speed.set(n);
            }
        }
    }

    public void fixAlarms() { //this is to fix me being a retard and relabeling previously boolean values as strings
        if (Config.alarmunknownplayer.toLowerCase().equals("true") || Config.alarmunknownplayer.toLowerCase().equals("false")) {
            Utils.setpref("alarmunknownplayer", "sfx/OhShitItsAGuy");
            Config.alarmunknownplayer = "sfx/OhShitItsAGuy";
        }
        if (Config.alarmredplayer.toLowerCase().equals("true") || Config.alarmredplayer.toLowerCase().equals("false")) {
            Utils.setpref("alarmredplayer", "sfx/Siren");
            Config.alarmredplayer = "sfx/Siren";
        }
        if (Config.alarmstudy.toLowerCase().equals("true") || Config.alarmstudy.toLowerCase().equals("false")) {
            Utils.setpref("alarmstudy", "sfx/Study");
            Config.alarmstudy = "sfx/Study";
        }
        if (Config.cleavesfx.toLowerCase().equals("true") || Config.cleavesfx.toLowerCase().equals("false")) {
            Utils.setpref("cleavesfx", "sfx/oof");
            Config.cleavesfx = "sfx/oof";
        }
    }

    public void toggleres() {
        Config.resinfo = !Config.resinfo;
        Utils.setprefb("resinfo", Config.resinfo);
        map.tooltip = null;
        msg("Resource info on shift/shift+ctrl is now turned " + (Config.resinfo ? "on" : "off"), Color.WHITE);
    }

    public static final KeyBinding kb_shoot = KeyBinding.get("screenshot", KeyMatch.forchar('S', KeyMatch.M));
    public static final KeyBinding kb_chat = KeyBinding.get("chat-toggle", KeyMatch.forchar('C', KeyMatch.C));
    public static final KeyBinding kb_hide = KeyBinding.get("ui-toggle", KeyMatch.nil);

    public boolean globtype(char key, KeyEvent ev) {
        if (key == ':') {
            entercmd();
            return (true);
        } else if ((Config.screenurl != null) && kb_shoot.key().match(ev)) {
            Screenshooter.take(this, Config.screenurl);
            return (true);
        } else if (kb_hide.key().match(ev)) {
            toggleui();
            return (true);
        } else if (kb_chat.key().match(ev)) {
            if (chat.visible && !chat.hasfocus) {
                setfocus(chat);
            } else {
                if (chat.targeth == 0) {
                    chat.sresize(chat.savedh);
                    setfocus(chat);
                } else {
                    chat.sresize(0);
                }
            }
            Utils.setprefb("chatvis", chat.targeth != 0);
            return (true);
        } else if ((key == 27) && (map != null) && !map.hasfocus) {
            setfocus(map);
            return (true);
        } else if (chatfocused()) {
            return true;
        } else {
            return KeyBinder.handle(ui, ev) || (super.globtype(key, ev));
        }
    }

    public boolean chatfocused() {
        boolean isfocused = false;
        for (Widget w = chat.lchild; w != null; w = w.prev) {
            if (w instanceof TextEntry)
                if (w.hasfocus)
                    isfocused = true;
        }

        return isfocused;
    }

    public boolean mousedown(Coord c, int button) {
        return (super.mousedown(c, button));
    }

    private int uimode = 1;

    public void toggleui(int mode) {
        Hidepanel[] panels = {blpanel, brpanel, ulpanel, umpanel, urpanel, menupanel};
        switch (uimode = mode) {
            case 0:
                for (Hidepanel p : panels)
                    p.mshow(true);
                break;
            case 1:
                for (Hidepanel p : panels)
                    p.mshow();
                break;
            case 2:
                for (Hidepanel p : panels)
                    p.mshow(false);
                break;
        }
    }

    public void resetui() {
        Hidepanel[] panels = {blpanel, brpanel, ulpanel, umpanel, urpanel, menupanel};
        for (Hidepanel p : panels)
            p.cshow(p.tvis);
        uimode = 1;
    }

    public void toggleui() {
        toggleui((uimode + 1) % 3);
    }

    public void resize(Coord sz) {
        this.sz = sz;
        chat.resize(sz.x - blpw - brpw);
        chat.move(new Coord(blpw, sz.y));
        if (map != null)
            map.resize(sz);
        if (statuswindow != null)
            statuswindow.c = new Coord(HavenPanel.w / 2 + 80, 10);
        beltwdg.c = new Coord(blpw + 10, sz.y - beltwdg.sz.y - 5);
        super.resize(sz);
    }

    public void presize() {
        resize(parent.sz);
    }

    public void msg(String msg, Color color, Color logcol) {
        msgtime = Utils.rtime();
        if (Config.temporaryswimming && msg.equals("Swimming is now turned on.")) { //grab it here before we localize the message
            temporarilyswimming = true;
            SwimTimer = System.currentTimeMillis();
        }
        if (msg.startsWith("Quality:") && inspectedgobid != 0) {
            Gob gob = ui.sess.glob.oc.getgob(inspectedgobid);
            if (gob != null) {
                try {
                    ui.sess.glob.oc.quality(gob, Integer.valueOf(msg.substring(8).trim()));
                    inspectedgobid = 0;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (Config.temporaryswimming && temporarilyswimming && msg.equals("Swimming is now turned off.")) {//swimming manually toggled back off before the auto-off trigger fired, reset the auto-toggle flags.
            temporarilyswimming = false;
            SwimTimer = 0;
        }
        msg = Resource.getLocString(Resource.BUNDLE_MSG, msg);
        lastmsg = msgfoundry.render(msg, color);
        syslog.append(msg, logcol);
        if (color == Color.WHITE)
            Audio.play(msgsfx);
    }

    public void msg(String msg, Color color) {
        msg(msg, color, color);
    }

    private static final Resource errsfx = Resource.local().loadwait("sfx/error");

    private double lasterrsfx = 0;

    public void error(String msg) {
        msg(msg, DefSettings.ERRORTEXTCOLOR.get(), new Color(255, 0, 0));
        if (errmsgcb != null)
            errmsgcb.notifyErrMsg(msg);
        double now = Utils.rtime();
        if (now - lasterrsfx > 0.1 && Config.errorsounds) {
            Audio.play(errsfx);
            lasterrsfx = now;
        }
    }

    private static final Resource msgsfx = Resource.local().loadwait("sfx/msg");
    private double lastmsgsfx = 0;

    public void msgnosfx(String msg) {
        msg(msg, new Color(255, 255, 254), Color.WHITE);
    }

    private static final String charterMsg = "The name of this charterstone is \"";

    public void msg(String msg) {
        if (msg.startsWith(charterMsg))
            CharterList.addCharter(msg.substring(charterMsg.length(), msg.length() - 2));

        msg(msg, Color.WHITE, Color.WHITE);
        double now = Utils.rtime();
        if (now - lastmsgsfx > 0.1) {
            Audio.play(msgsfx);
            lastmsgsfx = now;
        }
    }

    public void act(String... args) {
        wdgmsg("act", (Object[]) args);
    }

    public void act(int mods, Coord mc, Gob gob, String... args) {
        int n = args.length;
        Object[] al = new Object[n];
        System.arraycopy(args, 0, al, 0, n);
        if (mc != null) {
            al = Utils.extend(al, al.length + 2);
            al[n++] = mods;
            al[n++] = mc;
            if (gob != null) {
                al = Utils.extend(al, al.length + 2);
                al[n++] = (int) gob.id;
                al[n++] = gob.rc;
            }
        }
        wdgmsg("act", al);
    }

    public Window getwnd(String cap) {
        for (Widget w = lchild; w != null; w = w.prev) {
            if (w instanceof Window) {
                Window wnd = (Window) w;
                if (wnd.cap != null && cap.equals(wnd.origcap))
                    return wnd;
            }
        }
        return null;
    }

    private static final int WND_WAIT_SLEEP = 8;

    public Window waitfForWnd(String cap, int timeout) {
        int t = 0;
        while (t < timeout) {
            Window wnd = getwnd(cap);
            if (wnd != null)
                return wnd;
            t += WND_WAIT_SLEEP;
            try {
                Thread.sleep(WND_WAIT_SLEEP);
            } catch (InterruptedException e) {
                return null;
            }
        }
        return null;
    }

    public List<IMeter.Meter> getmeters(String name) {
        for (Widget meter : meters) {
            if (meter instanceof IMeter) {
                IMeter im = (IMeter) meter;
                try {
                    Resource res = im.bg.get();
                    if (res != null && res.basename().equals(name))
                        return im.meters;
                } catch (Loading l) {
                }
            }
        }
        return null;
    }

    public IMeter.Meter getmeter(String name, int midx) {
        List<IMeter.Meter> meters = getmeters(name);
        if (meters != null && midx < meters.size())
            return meters.get(midx);
        return null;
    }

    public Equipory getequipory() {
        if (equwnd != null) {
            for (Widget w = equwnd.lchild; w != null; w = w.prev) {
                if (w instanceof Equipory)
                    return (Equipory) w;
            }
        }
        return null;
    }

    public class FKeyBelt extends Belt implements DTarget, DropTarget {
        public final int beltkeys[] = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
                KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
                KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
        public int curbelt = 0;

        public FKeyBelt() {
            super(new Coord(450, 34));
        }

        private Coord beltc(int i) {
            return (new Coord(((invsq.sz().x + 2) * i) + (10 * (i / 4)), 0));
        }

        private int beltslot(Coord c) {
            for (int i = 0; i < 12; i++) {
                if (c.isect(beltc(i), invsq.sz()))
                    return (i + (curbelt * 12));
            }
            return (-1);
        }

        public void draw(GOut g) {
            for (int i = 0; i < 12; i++) {
                int slot = i + (curbelt * 12);
                Coord c = beltc(i);
                g.image(invsq, beltc(i));
                try {
                    if (belt[slot] != null)
                        belt[slot].spr().draw(g.reclip(c.add(1, 1), invsq.sz().sub(2, 2)));
                } catch (Loading e) {
                }
                g.chcolor(156, 180, 158, 255);
                FastText.aprintf(g, c.add(invsq.sz().sub(2, 0)), 1, 1, "F%d", i + 1);
                g.chcolor();
            }
        }

        public boolean mousedown(Coord c, int button) {
            int slot = beltslot(c);
            if (slot != -1) {
                if (button == 1)
                    GameUI.this.wdgmsg("belt", slot, 1, ui.modflags());
                if (button == 3)
                    GameUI.this.wdgmsg("setbelt", slot, 1);
                return (true);
            }
            return (false);
        }

        public boolean globtype(char key, KeyEvent ev) {
            boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
            for (int i = 0; i < beltkeys.length; i++) {
                if (ev.getKeyCode() == beltkeys[i]) {
                    if (M) {
                        curbelt = i;
                        return (true);
                    } else {
                        keyact(i + (curbelt * 12));
                        return (true);
                    }
                }
            }
            return (false);
        }

        public boolean drop(Coord c, Coord ul) {
            int slot = beltslot(c);
            if (slot != -1) {
                GameUI.this.wdgmsg("setbelt", slot, 0);
                return (true);
            }
            return (false);
        }

        public boolean iteminteract(Coord c, Coord ul) {
            return (false);
        }

        public boolean dropthing(Coord c, Object thing) {
            int slot = beltslot(c);
            if (slot != -1) {
                if (thing instanceof Resource) {
                    Resource res = (Resource) thing;
                    if (res.layer(Resource.action) != null) {
                        GameUI.this.wdgmsg("setbelt", slot, res.name);
                        return (true);
                    }
                }
            }
            return (false);
        }
    }

    private static final Tex nkeybg = Resource.loadtex("gfx/hud/hb-main");

    public class NKeyBelt extends Belt implements DTarget, DropTarget {
        public int curbelt = 0;
        final Coord pagoff = new Coord(5, 25);

        public NKeyBelt() {
            super(nkeybg.sz());
            adda(new IButton("gfx/hud/hb-btn-chat", "", "-d", "-h") {
                Tex glow;

                {
                    this.tooltip = RichText.render("Chat ($col[255,255,0]{Ctrl+C})", 0);
                    glow = new TexI(PUtils.rasterimg(PUtils.blurmask(up.getRaster(), 2, 2, Color.WHITE)));
                }

                public void click() {
                    if (chat.targeth == 0) {
                        chat.sresize(chat.savedh);
                        setfocus(chat);
                    } else {
                        chat.sresize(0);
                    }
                    Utils.setprefb("chatvis", chat.targeth != 0);
                }

                public void draw(GOut g) {
                    super.draw(g);
                    Color urg = chat.urgcols[chat.urgency];
                    if (urg != null) {
                        GOut g2 = g.reclipl(new Coord(-2, -2), g.sz().add(4, 4));
                        g2.chcolor(urg.getRed(), urg.getGreen(), urg.getBlue(), 128);
                        g2.image(glow, Coord.z);
                    }
                }
            }, sz, 1, 1);
        }

        private Coord beltc(int i) {
            return (pagoff.add(((invsq.sz().x + 2) * i) + (10 * (i / 5)), 0));
        }

        private int beltslot(Coord c) {
            for (int i = 0; i < 10; i++) {
                if (c.isect(beltc(i), invsq.sz()))
                    return (i + (curbelt * 12));
            }
            return (-1);
        }

        public void draw(GOut g) {
            g.image(nkeybg, Coord.z);
            for (int i = 0; i < 10; i++) {
                int slot = i + (curbelt * 12);
                Coord c = beltc(i);
                g.image(invsq, beltc(i));
                try {
                    if (belt[slot] != null)
                        belt[slot].spr().draw(g.reclip(c.add(1, 1), invsq.sz().sub(2, 2)));
                } catch (Loading e) {
                }
                g.chcolor(156, 180, 158, 255);
                FastText.aprintf(g, c.add(invsq.sz().sub(2, 0)), 1, 1, "%d", (i + 1) % 10);
                g.chcolor();
            }
            super.draw(g);
        }

        public boolean mousedown(Coord c, int button) {
            int slot = beltslot(c);
            if (slot != -1) {
                if (button == 1)
                    GameUI.this.wdgmsg("belt", slot, 1, ui.modflags());
                if (button == 3)
                    GameUI.this.wdgmsg("setbelt", slot, 1);
                return (true);
            }
            return (super.mousedown(c, button));
        }

        public boolean globtype(char key, KeyEvent ev) {
            int c = ev.getKeyCode();
            if ((c < KeyEvent.VK_0) || (c > KeyEvent.VK_9))
                return (false);
            int i = Utils.floormod(c - KeyEvent.VK_0 - 1, 10);
            boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
            if (M) {
                curbelt = i;
            } else {
                keyact(i + (curbelt * 12));
            }
            return (true);
        }

        public boolean drop(Coord c, Coord ul) {
            int slot = beltslot(c);
            if (slot != -1) {
                GameUI.this.wdgmsg("setbelt", slot, 0);
                return (true);
            }
            return (false);
        }

        public boolean iteminteract(Coord c, Coord ul) {
            return (false);
        }

        public boolean dropthing(Coord c, Object thing) {
            int slot = beltslot(c);
            if (slot != -1) {
                if (thing instanceof Resource) {
                    Resource res = (Resource) thing;
                    if (res.layer(Resource.action) != null) {
                        GameUI.this.wdgmsg("setbelt", slot, res.name);
                        return (true);
                    }
                }
            }
            return (false);
        }
    }

    {
        String val = Utils.getpref("belttype", "n");
        if (val.equals("n")) {
            beltwdg = add(new NKeyBelt());
        } else if (val.equals("f")) {
            beltwdg = add(new FKeyBelt());
        } else {
            beltwdg = add(new NKeyBelt());
        }
    }

    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();

    {
        cmdmap.put("afk", new Console.Command() {
            public void run(Console cons, String[] args) {
                afk = true;
                wdgmsg("afk");
            }
        });
        cmdmap.put("act", new Console.Command() {
            public void run(Console cons, String[] args) {
                Object[] ad = new Object[args.length - 1];
                System.arraycopy(args, 1, ad, 0, ad.length);
                wdgmsg("act", ad);
            }
        });
        cmdmap.put("belt", new Console.Command() {
            public void run(Console cons, String[] args) {
                if (args[1].equals("f")) {
                    beltwdg.destroy();
                    beltwdg = add(new FKeyBelt());
                    Utils.setpref("belttype", "f");
                    resize(sz);
                } else if (args[1].equals("n")) {
                    beltwdg.destroy();
                    beltwdg = add(new NKeyBelt());
                    Utils.setpref("belttype", "n");
                    resize(sz);
                }
            }
        });
        cmdmap.put("chrmap", new Console.Command() {
            public void run(Console cons, String[] args) {
                Utils.setpref("mapfile/" + chrid, args[1]);
            }
        });
        cmdmap.put("tool", new Console.Command() {
            public void run(Console cons, String[] args) {
                try {
                    add(gettype(args[1]).create(ui, new Object[0]), 200, 200);
                } catch (RuntimeException e) {
                    e.printStackTrace(Debug.log);
                }
            }
        });
        cmdmap.put("help", (cons, args) -> {
            cons.out.println("Available console commands:");
            cons.findcmds().forEach((s, cmd) -> cons.out.println(s));
        });
        cmdmap.put("savemap", (cons, args) -> {
            new Thread(() -> mapfile.view.dumpTiles(), "MapDumper").start();
        });
        cmdmap.put("baseq", (cons, args) -> {
            FoodInfo.showbaseq = Utils.parsebool(args[1]);
            msg("q10 FEP values in tooltips are now " + (FoodInfo.showbaseq ? "enabled" : "disabled"));
        });
    }

    public void registerItemCallback(ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public void unregisterItemCallback() {
        this.itemClickCallback = null;
    }

    public Map<String, Console.Command> findcmds() {
        return (cmdmap);
    }

    public void registerErrMsg(ErrorSysMsgCallback callback) {
        this.errmsgcb = callback;
    }
}
