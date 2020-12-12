package haven.sloth.script.pathfinding;

import haven.Config;
import haven.Coord;
import haven.Coord2d;
import haven.Gob;
import haven.GobHitbox;
import haven.MessageBuf;
import haven.RenderLink;
import haven.ResDrawable;
import haven.Resource;
import haven.sloth.gob.Type;
import haven.sloth.util.ResHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gob Hitbox
 */
public class Hitbox {
    private static final ResHashMap<Hitbox[]> hitboxes = new ResHashMap<>();
    private static final Hitbox[] NOHIT = new Hitbox[]{new Hitbox(new Coord(-1, -1), new Coord(1, 1))};
    private static final int BUFFER_SIZE = 2;

    static {
        hitboxes.put("gfx/terobjs/herbs", NOHIT);
        hitboxes.put("gfx/terobjs/items", NOHIT);
        hitboxes.put("gfx/terobjs/plants", NOHIT);

        //misc
//        hitboxes.put("gfx/terobjs/consobj", new Hitbox[]{new Hitbox(new Coord(-4, -4), new Coord(8, 8))});
        hitboxes.put("gfx/terobjs/skeleton", new Hitbox[]{new Hitbox(new Coord(-4, -4), new Coord(8, 8))});
        hitboxes.put("gfx/terobjs/clue", NOHIT);
        hitboxes.put("gfx/terobjs/boostspeed", NOHIT);
        hitboxes.put("gfx/kritter/jellyfish/jellyfish", NOHIT);
        //Knarrs seem to just take the hitbox of a player?
//        hitboxes.put("gfx/terobjs/vehicle/knarr", new Hitbox[]{new Hitbox(new Coord(-4, -4), new Coord(8, 8))});

        //stone This looks wrong...
//        hitboxes.put("gfx/terobjs/bumlings", new Hitbox[]{new Hitbox(new Coord(8, 8), new Coord(-16, -16))});

        //walls
        //XXX: loftar's real hitbox size for this is certainly a decimal..
//        final Hitbox[] wallseg = new Hitbox[]{new Hitbox(new Coord(-5, -5), new Coord(5, 5))};
//        final Hitbox[] gate = new Hitbox[]{new Hitbox(new Coord(-5, -10), new Coord(5, 10))};
//        final Hitbox[] biggate = new Hitbox[]{new Hitbox(new Coord(-5, -16), new Coord(5, 16))};
//        hitboxes.put("gfx/terobjs/arch/brickwallcp", wallseg);
//        hitboxes.put("gfx/terobjs/arch/brickwallseg", wallseg);
//        hitboxes.put("gfx/terobjs/arch/brickwallgate", gate);
//        hitboxes.put("gfx/terobjs/arch/brickwallbiggate", biggate);
//        hitboxes.put("gfx/terobjs/arch/palisadecp", wallseg);
//        hitboxes.put("gfx/terobjs/arch/palisadeseg", wallseg);
//        hitboxes.put("gfx/terobjs/arch/palisadegate", gate);
//        hitboxes.put("gfx/terobjs/arch/palisadebiggate", biggate);
//        hitboxes.put("gfx/terobjs/arch/poleseg", wallseg);
//        hitboxes.put("gfx/terobjs/arch/polecp", wallseg);
//        hitboxes.put("gfx/terobjs/arch/polegate", gate);
//        hitboxes.put("gfx/terobjs/arch/polebiggate", biggate);
//        hitboxes.put("gfx/terobjs/arch/drystonewallseg", wallseg);
//        hitboxes.put("gfx/terobjs/arch/drystonewallcp", wallseg);
//        hitboxes.put("gfx/terobjs/arch/drystonewallgate", gate);
//        hitboxes.put("gfx/terobjs/arch/drystonewallbiggate", biggate);
//        hitboxes.put("gfx/terobjs/arch/hwall", new Hitbox[]{new Hitbox(new Coord(-1, 0), new Coord(1, 11))});

        //animals
        hitboxes.put("gfx/kritter/horse", new Hitbox[]{new Hitbox(new Coord(-8, -4), new Coord(8, 4))});
        hitboxes.put("gfx/kritter/cattle/calf", new Hitbox[]{new Hitbox(new Coord(-9, -3), new Coord(9, 3))});
//        hitboxes.put("gfx/kritter/cattle/cattle", new Hitbox[]{new Hitbox(new Coord(-12, -4), new Coord(12, 4))});
        hitboxes.put("gfx/kritter/pig", new Hitbox[]{new Hitbox(new Coord(-6, -3), new Coord(6, 3))});
        hitboxes.put("gfx/kritter/goat", new Hitbox[]{new Hitbox(new Coord(-6, -2), new Coord(6, 2))});
        hitboxes.put("gfx/kritter/sheep/lamb", new Hitbox[]{new Hitbox(new Coord(-6, -2), new Coord(6, 2))});

        hitboxes.put("gfx/terobjs/cupboard", new Hitbox[]{new Hitbox(new Coord(-5, -5), new Coord(5, 5))});
    }

    //Offset and Size with a "buffer" around it to avoid clipping
    //After floating point movement changes and "pushing" effect this might not be needed outside of
    //cliffs and cave walls
//    private final Coord off;
//    private final Coord sz;
    public final Coord2d[] points;

    private final boolean hitable;

    public float zplus = 0;

    public Hitbox(final Coord off, final Coord sz, boolean hitable, boolean buffer) {
        Coord ac = !buffer ? off : off.add(BUFFER_SIZE, BUFFER_SIZE);
        Coord bc = !buffer ? sz : sz.add(BUFFER_SIZE * 2, BUFFER_SIZE * 2);
        this.points = new Coord2d[]{
                new Coord2d(ac.x, -ac.y), new Coord2d(bc.x, -ac.y), new Coord2d(bc.x, -bc.y), new Coord2d(ac.x, -bc.y)
        };
        this.hitable = hitable;
    }

    public Hitbox(final Coord off, final Coord sz) {
        this(off, sz, true, false);
    }

    public Hitbox() {
        this(Coord.z, Coord.z, false, false);
    }

    public Hitbox(final Coord2d[] p, boolean hitable) {
        this.points = new Coord2d[p.length];
        for (int i = 0; i < p.length; i++) {
            this.points[i] = new Coord2d(p[i].x, -p[i].y);
        }
        this.hitable = hitable;
    }


//    public Coord offset() {
//        return Coord.z;
//    }
//
//    public Coord size() {
//        return Coord.z;
//    }

    boolean canHit() {
        return hitable;
    }


    private static Hitbox[] loadHitboxFromRes(Gob gob) {
        final Optional<Resource> ores = gob.res();
        if (ores.isPresent()) {
            Resource res = ores.get();

            Optional<Hitbox[]> hitbox = hitboxes.get(res.name);
            if (hitbox.isPresent()) {
                if (res.name.endsWith("/consobj")) {
                    ResDrawable rd = gob.getattr(ResDrawable.class);
                    if (rd != null && rd.sdt.rbuf.length >=4) {
                        MessageBuf buf = rd.sdt.clone();
                        return new Hitbox[]{new Hitbox(new Coord(buf.rbuf[0], buf.rbuf[1]), new Coord(buf.rbuf[2], buf.rbuf[3]))};
                    }
                }
                if (gob.type == Type.WALLSEG && Config.flatwalls) {
                    for (Hitbox h : hitbox.get()) {
                        h.zplus = 10;
                    }
                }
                return hitbox.get();
            }

            Resource.Neg neg = res.layer(Resource.Neg.class);
            if (neg == null) {
                for (RenderLink.Res link : res.layers(RenderLink.Res.class)) {
//                if (link.mesh().isPresent()) {
//                    neg = link.mesh().get().layer(Resource.Neg.class);
//                    break;
//                }
                    try {
                        RenderLink l = link.l;
                        if (l instanceof RenderLink.MeshMat) {
                            RenderLink.MeshMat mm = (RenderLink.MeshMat) l;
                            if (mm.srcres != null) {
                                Resource.Neg ng = mm.srcres.layer(Resource.Neg.class);
                                if (ng != null) {
                                    neg = ng;
                                    break;
                                }
                            }
                            if (mm.mesh != null) {
                                Resource.Neg ng = mm.mesh.get().layer(Resource.Neg.class);
                                if (ng != null) {
                                    neg = ng;
                                    break;
                                }
                            }
//                            if (mm.mat != null) {
//                                Resource.Neg ng = mm.mat.get().layer(Resource.Neg.class);
//                                if (ng != null) {
//                                    neg = ng;
//                                    break;
//                                }
//                            }
                        }
                        if (l instanceof RenderLink.AmbientLink) {
                            RenderLink.AmbientLink al = (RenderLink.AmbientLink) l;
                            if (al.res != null) {
                                Resource.Neg ng = al.res.get().layer(Resource.Neg.class);
                                if (ng != null) {
                                    neg = ng;
                                    break;
                                }
                            }
                        }
                        if (l instanceof RenderLink.Collect) {
                            RenderLink.Collect cl = (RenderLink.Collect) l;
                            if (cl.from != null) {
                                Resource.Neg ng = cl.from.get().layer(Resource.Neg.class);
                                if (ng != null) {
                                    neg = ng;
                                    break;
                                }
                            }
                        }
                        if (l instanceof RenderLink.Parameters) {
                            RenderLink.Parameters pl = (RenderLink.Parameters) l;
                            if (pl.res != null) {
                                Resource.Neg ng = pl.res.get().layer(Resource.Neg.class);
                                if (ng != null) {
                                    neg = ng;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            List<Resource.Obst> obsts = new ArrayList<>(res.layers(Resource.Obst.class));
            if (obsts.size() == 0) {
                for (RenderLink.Res link : res.layers(RenderLink.Res.class)) {
//                if (link.mesh().isPresent()) {
//                    obst = link.mesh().get().layer(Resource.Obst.class);
//                    break;
//                }
                    try {
                        RenderLink l = link.l;
                        if (l instanceof RenderLink.MeshMat) {
                            RenderLink.MeshMat mm = (RenderLink.MeshMat) l;
                            if (mm.srcres != null) {
                                Resource.Obst ng = mm.srcres.layer(Resource.Obst.class);
                                if (ng != null) {
                                    obsts.add(ng);
                                    break;
                                }
                            } else if (mm.mesh != null && mm.mesh.get() != null) {
                                Resource.Obst ng = mm.mesh.get().layer(Resource.Obst.class);
                                if (ng != null) {
                                    obsts.add(ng);
                                    break;
                                }
                            } else if (mm.mat != null && mm.mat.get() != null) {
                                Resource.Obst ng = mm.mat.get().layer(Resource.Obst.class);
                                if (ng != null) {
                                    obsts.add(ng);
                                    break;
                                }
                            }
                        } else if (l instanceof RenderLink.AmbientLink) {
                            RenderLink.AmbientLink al = (RenderLink.AmbientLink) l;
                            if (al.res != null && al.res.get() != null) {
                                Resource.Obst ng = al.res.get().layer(Resource.Obst.class);
                                if (ng != null) {
                                    obsts.add(ng);
                                    break;
                                }
                            }
                        } else if (l instanceof RenderLink.Collect) {
                            RenderLink.Collect cl = (RenderLink.Collect) l;
                            if (cl.from != null && cl.from.get() != null) {
                                Resource.Obst ng = cl.from.get().layer(Resource.Obst.class);
                                if (ng != null) {
                                    obsts.add(ng);
                                    break;
                                }
                            }
                        } else if (l instanceof RenderLink.Parameters) {
                            RenderLink.Parameters pl = (RenderLink.Parameters) l;
                            if (pl.res != null && pl.res.get() != null) {
                                Resource.Obst ng = pl.res.get().layer(Resource.Obst.class);
                                if (ng != null) {
                                    obsts.add(ng);
                                    break;
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (obsts.size() > 0) {
                int obs = 0;
                for (Resource.Obst o : obsts) {
                    for (int i = 0; i < o.ep.length; i++) {
                        obs++;
                    }
                }

                final Hitbox[] hb = new Hitbox[obs];
                obs = 0;
                for (int o = 0; o < obsts.size(); o++) {
                    for (int i = 0; i < obsts.get(o).ep.length; i++) {
                        hb[obs] = new Hitbox(obsts.get(o).ep[i], true);
                        obs++;
                    }
                }

//                Resource.Obst obst = null;
//                for (Resource.Obst ob : obsts) {
//                    ResDrawable rd = gob.getattr(ResDrawable.class);
//                    if (rd != null) {
//                        if (ob.vc == rd.sdt.peekrbuf(0)) {
//                            obst = ob;
//                            break;
//                        }
//                    }
//                }
//                if (obst == null) obst = obsts.get(0);
//
//                final Hitbox[] hb = new Hitbox[obst.ep.length * obsts.size()];
//                for (int i = 0; i < obst.ep.length; i++) {
//                    hb[i] = new Hitbox(obst.ep[i], true);
//                }
                if (gob.type == Type.WALLSEG && Config.flatwalls) {
                    for (Hitbox h : hb) {
                        h.zplus = 10;
                    }
                }
                hitboxes.put(res.name, hb);
                return hb;
            } else if (neg != null) {
                final Hitbox[] hb = new Hitbox[]{new Hitbox(neg.bc, neg.bs, true, false)};
                hitboxes.put(res.name, hb);
                return hb;
            }
        }
        return null;
    }


    public static Hitbox[] hbfor(final String res) {
        return hitboxes.get(res).orElse(null);
    }


    public static Hitbox[] hbfor(final Gob g) {
        return hbfor(g, false);
    }

    public static Hitbox[] hbfor(final Gob g, final boolean force) {
//        final Optional<Resource> res = g.res();
        return loadHitboxFromRes(g);
//        return res.map(resource -> hitboxes.get(resource.name).orElse(loadHitboxFromRes(g))).orElse(null);
//        if (res.isPresent()) {
//            if (!force) {
//                if (!res.get().name.endsWith("gate") && !res.get().name.endsWith("/pow")) {
//                    return hitboxes.get(res.get().name).orElse(loadHitboxFromRes(g));
//                } else if (res.get().name.endsWith("gate") && res.get().name.startsWith("gfx/terobjs/arch")) {
//                    ResDrawable rd = g.getattr(ResDrawable.class);
//                    if (rd != null && (rd.sdtnum() == 1)) {
//                        return NOHIT;
//                    } else {
//                        return hitboxes.get(res.get().name).orElse(loadHitboxFromRes(g));
//                    }
//                } else if (res.get().name.endsWith("/pow")) {
//                    ResDrawable rd = g.getattr(ResDrawable.class);
//                    if (rd != null && (rd.sdtnum() == 17 || rd.sdtnum() == 33)) {
//                        return NOHIT;
//                    } else {
//                        return hitboxes.get(res.get().name).orElse(loadHitboxFromRes(g));
//                    }
//                } else {
//                    return hitboxes.get(res.get().name).orElse(loadHitboxFromRes(g));
//                }
//            } else {
//                return hitboxes.get(res.get().name).orElse(loadHitboxFromRes(g));
//            }
//        } else {
//            return null;
//        }
    }
}
