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

import haven.BuddyWnd.GroupSelector;
import haven.MapFile.Marker;
import haven.MapFile.PMarker;
import haven.MapFile.SMarker;
import haven.MapFileWidget.Locator;
import haven.MapFileWidget.MapLocator;
import haven.MapFileWidget.SpecLocator;
import haven.purus.pbot.PBotGob;
import haven.purus.pbot.PBotGobAPI;
import haven.sloth.gob.Type;
import haven.sloth.gui.DowseWnd;
import modification.configuration;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;

public class MapWnd extends Window {
    public static final Resource markcurs = Resource.local().loadwait("gfx/hud/curs/flag");
    public static final Tex party = Resource.loadtex("custom/mm/pl/party");

    public final MapFileWidget view;
    public final MapView mv;
    public final MarkerList list;
    private final Locator player;
    private final Widget toolbar;
    public final Widget zoombar;
    private final Frame viewf, listf, fdropf;
    private final Button mebtn, mibtn;
    private final Dropbox<Pair<String, String>> fdrop;
    public final IButton grid, hidemarks;
    private TextEntry namesel;
    private GroupSelector colsel;
    private Button mremove;
    private Comparator<Marker> mcmp = namecmp;
    public List<Marker> markers = Collections.emptyList();
    private int markerseq = -1;
    private boolean domark = false;
    public Tex zoomtex = null;
    private final Collection<Runnable> deferred = new LinkedList<>();
    private static final Tex plx = Text.renderstroked("\u2716", Color.red, Color.BLACK, Text.num12boldFnd).tex();
    private Predicate<Marker> filter = (m -> true);
    private final static Comparator<Marker> namecmp = ((a, b) -> a.nm.compareTo(b.nm));
    private Map<Color, Tex> xmap = new HashMap<Color, Tex>(6);
    private Map<Long, Tex> namemap = new HashMap<>(50);
    private Map<Coord, Coord> questlinemap = new HashMap<>();

    private final List<TempMark> tempMarkList = new ArrayList<>();
    private long lastMarkCheck = System.currentTimeMillis();

    public static class TempMark {
        final long start;
        final long id;
        Coord2d rc;
        MapFileWidget.Location loc;
        TexI icon;

        public TempMark(long id, Coord2d rc, MapFileWidget.Location loc, TexI icon) {
            start = System.currentTimeMillis();
            this.id = id;
            this.rc = rc;
            this.loc = loc;
            this.icon = icon;
        }
    }

    public MapWnd(MapFile file, MapView mv, Coord sz, String title) {
        super(sz, title, title, true);
        this.mv = mv;
        this.player = new MapLocator(mv);
        grid = add(new IButton("gfx/hud/wndmap/btns/grid", "Toggle grid on map", this::toggleMapGrid));
        hidemarks = add(new IButton("gfx/hud/wndmap/btns/viewdist", "Toggle marks vision", this::toggleMapViewDist));
        viewf = add(new Frame(Coord.z, true));
        view = viewf.add(new View(file));
        recenter();
        toolbar = add(new Widget(Coord.z));
        toolbar.add(new Img(Resource.loadtex("gfx/hud/mmap/fgwdg")), Coord.z);
        toolbar.add(new IButton("gfx/hud/mmap/home", "", "-d", "-h") {
            {
                tooltip = RichText.render("Follow ($col[255,255,0]{Home})", 0);
            }

            public void click() {
                questlinemap.clear();
                recenter();
            }
        }, Coord.z);
        toolbar.add(new IButton("gfx/hud/mmap/mark", "", "-d", "-h") {
            {
                tooltip = RichText.render("Add marker", 0);
            }

            public void click() {
                domark = true;
            }
        }, Coord.z);
        toolbar.pack();
        zoombar = add(new ZoomBar());

        fdrop = add(markersFilter());
        fdropf = Frame.around(this, Collections.singletonList(fdrop));

        listf = add(new Frame(new Coord(200, 200), false));
        list = listf.add(new MarkerList(listf.inner().x, 0));

        mebtn = add(new Button(95, "Export...", false) {
            public void click() {
                view.exportmap();
            }
        });
        mibtn = add(new Button(95, "Import...", false) {
            public void click() {
                view.importmap();
            }
        });

        resize(sz);
    }

    public class ZoomBar extends Widget {
        private final static int btnsz = 21;

        public ZoomBar() {
            super(new Coord(btnsz * 2 + 20, btnsz));
            add(new IButton("gfx/hud/worldmap/minus", "", "", "") {
                @Override
                public void click() {
                    if (MapFileWidget.zoom < MapFileWidget.zoomlvls - 1) {
                        zoomtex = null;
                        Coord tc = view.curloc.tc.mul(MapFileWidget.scalef());
                        MapFileWidget.zoom++;
                        Utils.setprefi("zoomlmap", MapFileWidget.zoom);
                        tc = tc.div(MapFileWidget.scalef());
                        view.curloc.tc.x = tc.x;
                        view.curloc.tc.y = tc.y;
                    }
                }
            }, Coord.z);
            add(new IButton("gfx/hud/worldmap/plus", "", "", "") {
                @Override
                public void click() {
                    if (MapFileWidget.zoom > 0) {
                        zoomtex = null;
                        Coord tc = view.curloc.tc.mul(MapFileWidget.scalef());
                        MapFileWidget.zoom--;
                        Utils.setprefi("zoomlmap", MapFileWidget.zoom);
                        tc = tc.div(MapFileWidget.scalef());
                        view.curloc.tc.x = tc.x;
                        view.curloc.tc.y = tc.y;
                    }
                }
            }, new Coord(btnsz + 20, 0));
        }

        @Override
        public void draw(GOut g) {
            super.draw(g);
            g.image(renderz(), new Coord((btnsz * 2 + 20) / 2 - zoomtex.sz().x / 2, btnsz / 2 - zoomtex.sz().y / 2));
        }

        private Tex renderz() {
            if (zoomtex == null)
                zoomtex = Text.renderstroked((MapFileWidget.zoomlvls - MapFileWidget.zoom) + "", Color.WHITE, Color.BLACK).tex();
            return zoomtex;
        }
    }

    private class View extends MapFileWidget {
        View(MapFile file) {
            super(file, Coord.z);
        }

        public boolean clickmarker(DisplayMarker mark, int button) {
            if (button == 1 && ui.modflags() == 0) {
                list.change2(mark.m);
                list.display(mark.m);
                return (true);
            }
            return (false);
        }

        public boolean clickloc(Location loc, int button) {
            if (domark && (button == 1)) {
                Marker nm = new PMarker(loc.seg.id, loc.tc, "New marker", BuddyWnd.gc[new Random().nextInt(BuddyWnd.gc.length)]);
                file.add(nm);
                list.change2(nm);
                list.display(nm);
                domark = false;
                return (true);
            }
            return (false);
        }

        public boolean mousedown(Coord c, int button) {
            if (domark && (button == 3)) {
                domark = false;
                return (true);
            }
            return (super.mousedown(c, button));
        }

        private void drawTracking(GOut g, final Location ploc) {
            final Coord pc = new Coord2d(mv.getcc()).floor(tilesz);
            final double dist = 90000.0D;
            synchronized (ui.gui.dowsewnds) {
                for (final DowseWnd wnd : ui.gui.dowsewnds) {
                    final Coord mc = new Coord2d(wnd.startc).floor(tilesz);
                    final Coord lc = mc.add((int) (Math.cos(Math.toRadians(wnd.a1())) * dist), (int) (Math.sin(Math.toRadians(wnd.a1())) * dist));
                    final Coord rc = mc.add((int) (Math.cos(Math.toRadians(wnd.a2())) * dist), (int) (Math.sin(Math.toRadians(wnd.a2())) * dist));
                    final Coord cloc = xlate(ploc);
                    if (cloc != null) {
                        final Coord gc = cloc.add(mc.sub(pc).div(scalef()));
                        final Coord mlc = cloc.add(lc.sub(pc).div(scalef()));
                        final Coord mrc = cloc.add(rc.sub(pc).div(scalef()));
                        g.chcolor(new Color(configuration.dowsecolor, true));
                        g.dottedline(gc, mlc, 1);
                        g.dottedline(gc, mrc, 1);
                        g.chcolor();
                    }
                }
            }
        }

        private Set<Long> drawparty(GOut g, final Location ploc) {
            final Set<Long> ignore = new HashSet<>();
            final Coord pc = new Coord2d(mv.getcc()).floor(tilesz);
            double angle;
            try {
                synchronized (ui.sess.glob.party) {
                    final Coord psz = party.sz();
                    for (Party.Member m : ui.sess.glob.party.memb.values()) {
                        Coord2d ppc = m.getc();

                        if (ppc == null) // chars are located in different worlds
                            continue;

                        if (ui.sess.glob.party.memb.size() == 1) //don't do anything if you don't have a party
                            continue;


                        final Coord mc = new Coord2d(ppc).floor(tilesz);
                        final Coord gc = xlate(new Location(ploc.seg, ploc.tc.add(mc.sub(pc).div(MapFileWidget.scalef()))));
                        ignore.add(m.gobid);
                        if (gc != null) {
                            Gob gob = m.getgob();

                            if (gob == null) {//party member not in draw distance, draw a party colored X instead.
                                Tex tex = xmap.get(m.col);
                                if (tex == null) {
                                    tex = Text.renderstroked("\u2716", m.col, m.col, Text.num12boldFnd).tex();
                                    xmap.put(m.col, tex);
                                }
                                g.chcolor(m.col);
                                g.image(tex, gc.sub(psz.div(2)), psz);
                                Tex nametex = namemap.get(m.gobid);
                                if (nametex != null) { //if we have a nametex for this gobid because we've seen them before, go ahead and apply it
                                    g.chcolor(Color.WHITE);
                                    g.image(nametex, gc.sub(psz.div(2).add(new Coord(-5, -10))));
                                }
                                continue;
                            }
                            angle = gob.geta();
                            final Coord front = new Coord(8, 0).rotate(angle).add(gc);
                            final Coord right = new Coord(-5, 5).rotate(angle).add(gc);
                            final Coord left = new Coord(-5, -5).rotate(angle).add(gc);
                            final Coord notch = new Coord(-2, 0).rotate(angle).add(gc);
                            KinInfo kin = gob.getattr(KinInfo.class);

                            Tex tex = namemap.get(m.gobid);
                            if (tex == null && kin != null) { //if we don't already have this nametex in memory, set one up.
                                System.out.println("tex null kin not null");
                                tex = Text.renderstroked(kin.name, Color.WHITE, Color.BLACK, Text.delfnd2).tex();
                                // tex = kin.rendered();
                                namemap.put(m.gobid, tex);
                            }
                            if (tex != null) { //apply texture if it's been successfully setup.
                                g.chcolor(Color.WHITE);
                                g.image(tex, gc.sub(psz.div(2).add(new Coord(-5, -10))));
                            }


                            g.chcolor(m.col);
                            g.poly(front, right, notch, left);
                            g.chcolor(Color.BLACK);
                            g.polyline(1, front, right, notch, left);
                            g.chcolor();
                        }

                    }
                }
            } catch (Loading l) {
                //Fail silently
            }
            return ignore;
        }

        /**
         * Ideally this will be a line -> X -> line -> X
         * Where X is some icon for destinations
         * Start at map.moveto
         * Then follow map.movequeue
         * XXX: does it need an icon?
         */
        private void drawmovement(GOut g, final Location ploc) {
            final Coord pc = new Coord2d(mv.getcc()).floor(tilesz);
            final Coord2d movingto = mv.movingto();
            final Iterator<Coord2d> queue = mv.movequeue();
            Coord last;
            if (movingto != null) {
                //Make the line first
                g.chcolor(new Color(configuration.pfcolor, true));
                final Coord cloc = xlate(ploc);
                if (cloc != null) {
                    last = cloc.add(movingto.floor(tilesz).sub(pc).div(scalef()));
                    g.dottedline(cloc, last, 2);
                    if (queue.hasNext()) {
                        while (queue.hasNext()) {
                            final Coord next = cloc.add(queue.next().floor(tilesz).sub(pc).div(scalef()));
                            g.dottedline(last, next, 2);
                            last = next;
                        }
                    }
                }
            }
        }

        private void questgiverLines(GOut g, final Location ploc) {
            final Coord pc = new Coord2d(mv.getcc()).floor(tilesz);
            final double dist = 90000.0D;
            List<Coord2d> questQueue = new ArrayList<>(mv.questQueue());
            try {
                if (questQueue.size() > 0) {
                    for (Coord2d coord : questQueue) {
                        ui.gui.mapfile.view.follow = false;
                        final Gob player = mv.player();
                        double angle = player.rc.angle(coord);
                        final Coord mc = new Coord2d(player.rc).floor(tilesz);
                        final Coord lc = mc.add((int) (Math.cos(angle) * dist), (int) (Math.sin(angle) * dist));
                        questlinemap.put(mc, lc);
                    }
                    questQueue.clear();
                    mv.questQueue().clear();
                }
                if (questlinemap.size() > 0) {
                    for (Map.Entry<Coord, Coord> entry : questlinemap.entrySet()) {
                        final Coord cloc = xlate(ploc);
                        if (cloc != null) {
                            final Coord gc = cloc.add(entry.getKey().sub(pc).div(scalef()));
                            final Coord mlc = cloc.add(entry.getValue().sub(pc).div(scalef()));
                            g.chcolor(new Color(configuration.questlinecolor, true));
                            g.dottedline(gc, mlc, 2);
                            g.chcolor();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void drawmarks(GOut g, final Location ploc) {
            final Coord pc = new Coord2d(mv.getcc()).floor(tilesz);
            Location tloc = this.curloc;
            synchronized (tempMarkList) {
                for (TempMark cm : tempMarkList) {
                    if (tloc != null && tloc.seg == cm.loc.seg) {
                        TexI tex = cm.icon;
                        Coord c = xlate(ploc);
                        if (c != null)
                            g.aimage(tex, c.add(cm.rc.floor(tilesz).sub(pc).div(scalef())), 0.5, 0.5);
                    }
                }
            }
        }

        public void draw(GOut g) {
            g.chcolor(0, 0, 0, 128);
            g.frect(Coord.z, sz);
            g.chcolor();
            super.draw(g);
            try {
                final Location loc = resolve(player);
                Coord ploc = xlate(resolve(player));
                if (ploc != null) {
                    drawmovement(g.reclip(view.c, view.sz), loc);
                    questgiverLines(g.reclip(view.c, view.sz), loc);
                    drawTracking(g.reclip(view.c, view.sz), loc);

                    if (configuration.tempmarks && !configuration.bigmaphidemarks)
                        drawmarks(g.reclip(view.c, view.sz), loc);
                    double angle = 0;
                    if (mv.player() != null)
                        angle = mv.player().geta();

                    g.chcolor(255, 0, 0, 255);
//                    g.image(plx, ploc.sub(plx.sz().div(2)));

                    final Coord coord1 = new Coord(8, 0).rotate(angle).add(ploc);
                    final Coord coord2 = new Coord(0, -5).rotate(angle).add(ploc);
                    final Coord coord3 = new Coord(0, -1).rotate(angle).add(ploc);
                    final Coord coord4 = new Coord(-8, -1).rotate(angle).add(ploc);
                    final Coord coord5 = new Coord(-8, 1).rotate(angle).add(ploc);
                    final Coord coord6 = new Coord(0, 1).rotate(angle).add(ploc);
                    final Coord coord7 = new Coord(0, 5).rotate(angle).add(ploc);
                    g.poly(coord1, coord2, coord3, coord4, coord5, coord6, coord7);
                    g.chcolor(Color.BLACK);
                    g.polyline(1, coord1, coord2, coord3, coord4, coord5, coord6, coord7);
                    g.chcolor();

                    final Set<Long> ignore;
                    if (Config.mapdrawparty)
                        ignore = drawparty(g, loc);
                    g.chcolor();
                }
            } catch (Loading l) {
            }
        }

        public Resource getcurs(Coord c) {
            if (domark)
                return (markcurs);
            return (super.getcurs(c));
        }

        public void tick(double dt) {
            super.tick(dt);
            if (configuration.tempmarks && System.currentTimeMillis() - lastMarkCheck > configuration.tempmarksfrequency) {
                checkmarks();
                lastMarkCheck = System.currentTimeMillis();
            }
        }

        public void checkmarks() {
            Defer.later(() -> {
                synchronized (tempMarkList) {
                    final List<TempMark> marks = new ArrayList<>(tempMarkList);
                    for (TempMark cm : marks) {
                        PBotGob g = PBotGobAPI.findGobById(ui, cm.id);
                        if (g == null) {
                            if (System.currentTimeMillis() - cm.start > configuration.tempmarkstime * 1000) {
                                tempMarkList.remove(cm);
                            }
                        } else {
                            if (g.getRcCoords() != cm.rc) {
                                for (TempMark customMark : tempMarkList) {
                                    if (customMark.id == cm.id) {
                                        customMark.rc = g.getRcCoords();
                                        break;
                                    }
                                }
                            }
                            Location tloc = this.curloc;
                            if (tloc != null && tloc.seg != cm.loc.seg) {
                                for (TempMark customMark : tempMarkList) {
                                    if (customMark.id == cm.id) {
                                        customMark.loc = tloc;
                                        break;
                                    }
                                }
                            }
                        }
                        Gob dg = null;
                        for (LocalMiniMap.DisplayIcon disp : ui.gui.mmap.icons) {
                            if (disp.sc == null)
                                continue;
                            GobIcon.Image img = disp.img;
                            if (disp.gob.id == cm.id) {
                                if (!img.rot)
                                    dg = disp.gob;
                                break;
                            }
                        }
                        if (dg == null) {
                            if (System.currentTimeMillis() - cm.start > configuration.tempmarkstime * 1000) {
                                tempMarkList.remove(cm);
                            }
                        } else {
                            if (dg.rc != cm.rc) {
                                for (TempMark customMark : tempMarkList) {
                                    if (customMark.id == cm.id) {
                                        customMark.rc = dg.rc;
                                        break;
                                    }
                                }
                            }
                            Location tloc = this.curloc;
                            if (tloc != null && tloc.seg != cm.loc.seg) {
                                for (TempMark customMark : tempMarkList) {
                                    if (customMark.id == cm.id) {
                                        customMark.loc = tloc;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    marks.clear();
                    marks.addAll(tempMarkList);

                    OCache oc = ui.sess.glob.oc;
                    synchronized (oc) {
                        for (Gob gob : oc) {
                            try {
                                Optional<Resource> ores = gob.res();
                                if (!ores.isPresent())
                                    continue;
                                Resource res = ores.get();
                                TempMark m = getTMark(marks, gob.id);
                                if (m == null) {
                                    GobIcon icon = gob.getattr(GobIcon.class);
                                    TexI tex = null;
                                    if (!Config.hideallicons && (icon != null || Config.additonalicons.containsKey(res.name))) {
                                        CheckListboxItem itm = Config.icons.get(res.basename());
                                        if (configuration.tempmarksall || (itm != null && !itm.selected)) {
                                            if (icon != null)
                                                tex = (TexI) cachedtex(gob);
                                            else
                                                tex = (TexI) Config.additonalicons.get(res.name);
                                        }
                                    } else if (gob.type == Type.ROAD && Config.showroadmidpoint) {
                                        tex = (TexI) LocalMiniMap.roadicn;
                                    } else if (gob.type == Type.ROADENDPOINT && Config.showroadendpoint) {
                                        tex = (TexI) LocalMiniMap.roadicn;
                                    } else if (gob.type == Type.DUNGEONDOOR) {
                                        int stage = 0;
                                        if (gob.getattr(ResDrawable.class) != null)
                                            stage = gob.getattr(ResDrawable.class).sdt.peekrbuf(0);
                                        if (stage == 10 || stage == 14)
                                            tex = (TexI) LocalMiniMap.dooricn;
                                    }
                                    if (tex != null && this.curloc != null)
                                        tempMarkList.add(new TempMark(gob.id, gob.rc, this.curloc, tex));
                                }
                            } catch (Loading l) {
                            }
                        }
                    }

                    for (LocalMiniMap.DisplayIcon disp : ui.gui.mmap.icons) {
                        if (disp.sc == null)
                            continue;
                        TempMark m = getTMark(marks, disp.gob.id);
                        if (m == null) {
                            GobIcon.Image img = disp.img;
                            TexI tex = null;
                            if (!img.rot) {
                                tex = (TexI) cachedtex(disp.gob);
                            }
                            if (tex != null && this.curloc != null)
                                tempMarkList.add(new TempMark(disp.gob.id, disp.gob.rc, this.curloc, tex));
                        }
                    }
                }
                return (null);
            });
        }

        private Tex cachedtex(Gob gob) {
            GobIcon icon = gob.getattr(GobIcon.class);
            if (icon != null) {
                boolean isdead = gob.isDead();
                int size = 20;
                Indir<Resource> indir = icon.res;
                if (indir != null) {
                    Resource res = indir.get();
                    if (res != null) {
                        Tex itex = cachedImageTex.get(res.name + (isdead ? "-dead" : ""));
                        if (itex == null) {
                            GobIcon.Image img = icon.img();
                            itex = isdead ? img.texgrey() : img.tex();
                            if ((itex.sz().x > size) || (itex.sz().y > size)) {
                                BufferedImage buf = img.rimg.img;
                                buf = PUtils.convolve(buf, new Coord(size, size), new PUtils.Hanning(1));
                                itex = new TexI(buf);
                            }
                            cachedImageTex.put(res.name + (isdead ? "-dead" : ""), itex);
                        }
                        return (itex);
                    }
                }
            }
            return (null);
        }

        private TempMark getTMark(List<TempMark> marks, long id) {
            for (TempMark cm : marks)
                if (id == cm.id)
                    return (cm);
            return (null);
        }
    }

    public void resolveNames() {//used to load name textures even while the map is closed
        try {
            synchronized (ui.sess.glob.party) {
                for (Party.Member m : ui.sess.glob.party.memb.values()) {
                    Coord2d ppc = m.getc();
                    if (ppc == null) // chars are located in different worlds
                        continue;
                    if (ui.sess.glob.party.memb.size() == 1) //don't do anything if you don't have a party
                        continue;
                    Gob gob = m.getgob();
                    if (gob != null) {
                        KinInfo kin = gob.getattr(KinInfo.class);
                        Tex tex = namemap.get(m.gobid);
                        if (tex == null && kin != null) { //if we don't already have this nametex in memory, set one up.
                            tex = Text.renderstroked(kin.name, Color.WHITE, Color.BLACK, Text.delfnd2).tex();
                            namemap.put(m.gobid, tex);
                        }
                    }
                }
            }
        } catch (Loading l) {
            //Fail silently
        }
    }

    public void tick(double dt) {
        super.tick(dt);
        if (Config.mapdrawparty)
            resolveNames();
        synchronized (deferred) {
            for (Iterator<Runnable> i = deferred.iterator(); i.hasNext(); ) {
                Runnable task = i.next();
                try {
                    task.run();
                } catch (Loading l) {
                    continue;
                }
                i.remove();
            }
        }
        if (visible && (markerseq != view.file.markerseq)) {
            if (view.file.lock.readLock().tryLock()) {
                try {
                    this.markers = view.file.markers.stream().filter(filter).sorted(mcmp).collect(Collectors.toList());
                    markerseq = view.file.markerseq;
                } finally {
                    view.file.lock.readLock().unlock();
                }
            }
        }
    }

    public void selectMarker(String name) {
        if (markers != null && markers.size() > 0) {
            for (Marker marker : markers) {
                if (marker.nm.equals(name)) {
                    list.change2(marker);
                    view.center(new SpecLocator(marker.seg, marker.tc));
                }
            }
        }
    }

    public static final Color every = new Color(255, 255, 255, 16), other = new Color(255, 255, 255, 32);

    public void toggleMapGrid() {
        configuration.bigmapshowgrid = !configuration.bigmapshowgrid;
        Utils.setprefb("bigmapshowgrid", configuration.bigmapshowgrid);
    }

    public void toggleMapViewDist() {
        configuration.bigmaphidemarks = !configuration.bigmaphidemarks;
        Utils.setprefb("bigmaphidemarks", configuration.bigmaphidemarks);
    }

    private static final Pair[] filters = new Pair[]{
            new Pair<>("-- All --", null),
            new Pair<>("--- Custom ---", "flg"),
            new Pair<>("Abyssal Chasm", "abyssalchasm"),
            new Pair<>("Ancient Windthrow", "windthrow"),
            new Pair<>("Fairy Stone", "fairystone"),
            new Pair<>("Lily Pad Lotus", "lilypadlotus"),
            new Pair<>("Clay Pit", "claypit"),
            new Pair<>("Crystal Rock", "crystalpatch"),
            new Pair<>("Geyser", "geyser"),
            new Pair<>("Great Cave Organ", "caveorgan"),
            new Pair<>("Guano Pile", "guanopile"),
            new Pair<>("Headwaters", "headwaters"),
            new Pair<>("Heart of the Woods", "woodheart"),
            new Pair<>("Ice Spire", "icespire"),
            new Pair<>("Jotun Mussel", "jotunmussel"),
            new Pair<>("Quest Givers", "qst"),
            new Pair<>("Salt Basin", "saltbasin"),
            new Pair<>("Swirling Vortex", "watervortex"),
            new Pair<>("Cave", "cave")
    };

    @SuppressWarnings("unchecked")
    private Dropbox<Pair<String, String>> markersFilter() {
        Dropbox<Pair<String, String>> modes = new Dropbox<Pair<String, String>>(195, filters.length, Math.max(Text.render(filters[0].a.toString()).sz().y, 20)) {
            @Override
            protected Pair<String, String> listitem(int i) {
                return filters[i];
            }

            @Override
            protected int listitems() {
                return filters.length;
            }

            @Override
            protected void drawitem(GOut g, Pair<String, String> item, int i) {
                g.text(item.a, Dropbox.itemtextc);
            }

            @Override
            public void change(Pair<String, String> item) {
                super.change(item);
                if (item.b == null)
                    filter = (m -> true);
                else if (item.b.equals("flg"))
                    filter = (m -> m instanceof PMarker);
                else if (item.b.equals("qst"))
                    filter = (m -> m instanceof SMarker && ((SMarker) m).res.name.startsWith("gfx/invobjs/small"));
                else
                    filter = (m -> m instanceof SMarker && ((SMarker) m).res.name.endsWith(item.b));
                markerseq = -1;
                // reset scrollbar
                if (list != null)
                    list.sb.val = list.sb.min;
//                for (MapFile.SMarker marker : view.file.smarkers.values()) {
//                    System.out.println(marker + " [" + marker.nm + "] [" + marker.oid + "] [" + marker.res + " " + marker.res.name + " " + marker.res.ver + "]");
//                }
            }
        };
        modes.change(filters[0]);
        return modes;
    }

    public class MarkerList extends Listbox<Marker> {
        private final Text.Foundry fnd = CharWnd.attrf;

        public Marker listitem(int idx) {
            return (markers.get(idx));
        }

        public int listitems() {
            return (markers.size());
        }

        public MarkerList(int w, int n) {
            super(w, n, 20);
        }

        private Function<String, Text> names = new CachedFunction<>(500, nm -> fnd.render(nm));

        protected void drawbg(GOut g) {
        }

        public void drawitem(GOut g, Marker mark, int idx) {
            g.chcolor(((idx % 2) == 0) ? every : other);
            g.frect(Coord.z, g.sz);
            if (mark instanceof PMarker)
                g.chcolor(((PMarker) mark).color);
            else
                g.chcolor();
            g.aimage(names.apply(mark.nm).tex(), new Coord(5, itemh / 2), 0, 0.5);
        }

        protected void itemclick(Marker mark, int button) {
            if (button == 1)
                change(mark);
            if (button == 3)
                change2(mark);
        }

        public void change(Marker mark) {
            //change2(mark);
            if (mark != null)
                view.center(new SpecLocator(mark.seg, mark.tc));
        }

        public void change2(Marker mark) {
            this.sel = mark;

            if (namesel != null) {
                ui.destroy(namesel);
                namesel = null;
                if (colsel != null) {
                    ui.destroy(colsel);
                    colsel = null;
                }
                if (mremove != null) {
                    ui.destroy(mremove);
                    mremove = null;
                }
            }

            if (mark != null) {
                if (namesel == null) {
                    namesel = MapWnd.this.add(new TextEntry(200, "") {
                        {
                            dshow = true;
                        }

                        public void activate(String text) {
                            mark.nm = text;
                            view.file.update(mark);
                            commit();
                            change2(null);
                        }
                    });
                }
                namesel.settext(mark.nm);
                namesel.buf.point = mark.nm.length();
                namesel.commit();
                if (mark instanceof PMarker) {
                    PMarker pm = (PMarker) mark;
                    colsel = MapWnd.this.add(new GroupSelector(0) {
                        public void changed(int group) {
                            this.group = group;
                            pm.color = BuddyWnd.gc[group];
                            view.file.update(mark);
                        }
                    });
                    if ((colsel.group = Utils.index(BuddyWnd.gc, pm.color)) < 0)
                        colsel.group = 0;
                }
                mremove = MapWnd.this.add(new Button(200, "Remove", false) {
                    public void click() {
                        view.file.remove(mark);
                        change2(null);
                    }
                });
                MapWnd.this.resize(asz);
            }
        }
    }

    public void resize(Coord sz) {
        super.resize(sz);

        grid.c = new Coord(sz.x - 200, 0);
        hidemarks.c = grid.c.add(grid.sz.x + 5, 0);

        fdropf.c = new Coord(sz.x - 200, 20);
        fdrop.c = new Coord(fdropf.c.x + 5, fdropf.c.y + 5);

        listf.resize(listf.sz.x, sz.y - 140);
        listf.c = new Coord(sz.x - listf.sz.x, fdropf.c.y + fdropf.sz.y);
        list.resize(listf.inner());
        mebtn.c = new Coord(sz.x - 200, sz.y - mebtn.sz.y);
        mibtn.c = new Coord(sz.x - 95, sz.y - mibtn.sz.y);

        if (namesel != null) {
            namesel.c = listf.c.add(0, listf.sz.y + 10);
            if (colsel != null)
                colsel.c = namesel.c.add(0, namesel.sz.y + 10);
            mremove.c = new Coord(namesel.c.x, sz.y - mremove.sz.y);
        }
        viewf.resize(new Coord(sz.x - listf.sz.x - 10, sz.y));
        view.resize(viewf.inner());
        toolbar.c = viewf.c.add(0, viewf.sz.y - toolbar.sz.y).add(2, -2);
        zoombar.c = viewf.c.add(viewf.sz.x - zoombar.sz.x, viewf.sz.y - zoombar.sz.y).sub(7, 7);
    }

    public void recenter() {
        view.follow(player);
    }

    private static final Tex sizer = Resource.loadtex("gfx/hud/wnd/sizer");

    protected void drawframe(GOut g) {
        g.image(sizer, ctl.add(csz).sub(sizer.sz()));
        super.drawframe(g);
    }

    public boolean keydown(KeyEvent ev) {
        if (super.keydown(ev))
            return (true);
        if (ev.getKeyCode() == KeyEvent.VK_HOME) {
            questlinemap.clear();
            recenter();
            return (true);
        }
        return (false);
    }

    private UI.Grab drag;
    private Coord dragc;

    public boolean mousedown(Coord c, int button) {
        Coord cc = c.sub(ctl);
        if ((button == 1) && (cc.x < csz.x) && (cc.y < csz.y) && (cc.y >= csz.y - 25 + (csz.x - cc.x))) {
            if (drag == null) {
                drag = ui.grabmouse(this);
                dragc = asz.sub(c);
                return (true);
            }
        }
        return (super.mousedown(c, button));
    }

    public void mousemove(Coord c) {
        if (drag != null) {
            Coord nsz = c.add(dragc);
            nsz.x = Math.max(nsz.x, 300);
            nsz.y = Math.max(nsz.y, 150);
            resize(nsz);
        }
        super.mousemove(c);
    }

    public boolean mouseup(Coord c, int button) {
        if ((button == 1) && (drag != null)) {
            drag.remove();
            drag = null;
            return (true);
        }
        return (super.mouseup(c, button));
    }

    public void markobj(long gobid, long oid, Indir<Resource> resid, String nm) {
        synchronized (deferred) {
            deferred.add(new Runnable() {
                double f = 0;

                public void run() {
                    Resource res = resid.get();
                    String rnm = nm;
                    if (rnm == null) {
                        Resource.Tooltip tt = res.layer(Resource.tooltip);
                        if (tt == null)
                            return;
                        rnm = tt.t;
                    }
                    double now = Utils.rtime();
                    if (f == 0)
                        f = now;
                    Gob gob = ui.sess.glob.oc.getgob(gobid);
                    if (gob == null) {
                        if (now - f < 1.0)
                            throw (new Loading());
                        return;
                    }
                    Coord tc = gob.rc.floor(tilesz);
                    MCache.Grid obg = ui.sess.glob.map.getgrid(tc.div(cmaps));
                    if (!view.file.lock.writeLock().tryLock())
                        throw (new Loading());
                    try {
                        MapFile.GridInfo info = view.file.gridinfo.get(obg.id);
                        if (info == null)
                            throw (new Loading());
                        Coord sc = tc.add(info.sc.sub(obg.gc).mul(cmaps));
                        SMarker prev = view.file.smarkers.get(oid);
                        if (prev == null) {
                            view.file.add(new SMarker(info.seg, sc, rnm, oid, new Resource.Spec(Resource.remote(), res.name, res.ver)));
                        } else {
                            if ((prev.seg != info.seg) || !prev.tc.equals(sc)) {
                                prev.seg = info.seg;
                                prev.tc = sc;
                                view.file.update(prev);
                            }
                        }
                    } finally {
                        view.file.lock.writeLock().unlock();
                    }
                }
            });
        }
    }

    public void markobj(long gobid, Gob gob, String nm) {
        synchronized (deferred) {
            deferred.add(new Runnable() {
                double f = 0;

                public void run() {
                    GobIcon icon = gob.getattr(GobIcon.class);
                    Resource iconRes;
                    if (icon == null) {
                        iconRes = Resource.local().loadwait("gfx/hud/wndmap/btns/center", 1);
                    } else {
                        iconRes = icon.res.get();
                    }

                    Resource res = gob.getres();
                    String rnm = nm;
                    if (rnm == null) {
                        Resource.Tooltip tt = res.layer(Resource.tooltip);
                        if (tt == null)
                            return;
                        rnm = tt.t;
                    }
                    double now = Utils.rtime();
                    if (f == 0)
                        f = now;
                    Gob gob = ui.sess.glob.oc.getgob(gobid);
                    if (gob == null) {
                        if (now - f < 1.0)
                            throw (new Loading());
                        return;
                    }
                    Coord tc = gob.rc.floor(tilesz);
                    MCache.Grid obg = ui.sess.glob.map.getgrid(tc.div(cmaps));
                    if (!view.file.lock.writeLock().tryLock())
                        throw (new Loading());
                    try {
                        MapFile.GridInfo info = view.file.gridinfo.get(obg.id);
                        if (info == null)
                            throw (new Loading());
                        Coord sc = tc.add(info.sc.sub(obg.gc).mul(cmaps));
                        long oid = 0;
                        try {
                            oid = Long.parseLong(Math.abs(sc.x) + "" + Math.abs(sc.y) + "" + Math.abs(sc.x * sc.y) + ""); //FIXME bring true obj id
                        } catch (NumberFormatException e) {
                            oid = Long.MAX_VALUE - Math.abs(sc.x * sc.y);
                        }
                        SMarker prev = view.file.smarkers.get(oid);
                        rnm = rnm + " [" + sc.x + ", " + sc.y + "]";
                        if (prev == null) {
                            view.file.add(new SMarker(info.seg, sc, rnm, oid, new Resource.Spec(Resource.remote(), iconRes.name, iconRes.ver)));
                        } else {
                            if ((prev.seg != info.seg) || !prev.tc.equals(sc)) {
                                prev.seg = info.seg;
                                prev.tc = sc;
                                view.file.update(prev);
                            }
                        }
                    } finally {
                        view.file.lock.writeLock().unlock();
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        show(false);
        mv.questQueue().clear();
        questlinemap.clear();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (sender == cbtn) {
            show(false);
        } else
            super.wdgmsg(sender, msg, args);
    }

    @Override
    public boolean type(char key, KeyEvent ev) {
        if (key == 27) {
            if (cbtn.visible) {
                show(false);
            }
            return true;
        }
        return super.type(key, ev);
    }
}
