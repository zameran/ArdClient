/* Preprocessed source code */
/* $use: lib/globfx */
/* $use: lib/env */

import haven.BGL;
import haven.Config;
import haven.Coord3f;
import haven.GLState;
import haven.GOut;
import haven.Gob;
import haven.Material;
import haven.Message;
import haven.RenderList;
import haven.Resource;
import haven.Skeleton;
import haven.Sprite;
import haven.States;
import haven.Utils;
import haven.glsl.Expression;
import haven.glsl.FragmentContext;
import haven.glsl.Function;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.Return;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Uniform;
import haven.res.lib.env.Environ;
import modification.dev;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static haven.glsl.Cons.add;
import static haven.glsl.Cons.div;
import static haven.glsl.Cons.l;
import static haven.glsl.Cons.mul;
import static haven.glsl.Cons.pick;
import static haven.glsl.Cons.sub;
import static haven.glsl.Cons.vec4;
import static haven.glsl.Type.FLOAT;
import static haven.glsl.Type.VEC4;

/* >spr: ISmoke */
public class ISmoke extends Sprite implements Gob.Overlay.CDel {
    static {
        dev.checkFileVersion("gfx/fx/ismoke", 104);
    }

    FloatBuffer posb = null, colb = null;
    final Material mat;
    final List<Boll> bollar = new ArrayList<Boll>();
    final Random rnd = new Random();
    final GLState loc;
    final Color col;
    final float sz, den, fadepow, initzv, life, srad;
    boolean spawn = true;

    public ISmoke(Owner owner, Resource res, Message sdt) {
        super(owner, res);
        mat = res.layer(Material.Res.class, sdt.uint8()).get();
        sz = sdt.uint8() / 10.0f;
        String locn = sdt.string();
        if (locn.equals(""))
            loc = null;
        else
            loc = owner.getres().layer(Skeleton.BoneOffset.class, locn).forpose(null);
        col = Utils.col16(sdt.uint16());
        den = sdt.uint8();
        fadepow = sdt.uint8() / 10.0f;
        life = sdt.uint8() / 10.0f;
        int h = sdt.uint8();
        initzv = h / life;
        srad = sdt.uint8() / 10.0f;
    }

    float de = 0;

    public boolean tick(int idt) {
        if (Config.disableAllAnimations) return (false);
        float dt = idt / 1000.0f;
        de += dt;
        while (spawn && (de > 0.1)) {
            de -= 0.1;
            int n = (int) ((1.0f + (rnd.nextFloat() * 0.5f)) * den);
            for (int i = 0; i < n; i++)
                bollar.add(new Boll(Coord3f.o.sadd(0, rnd.nextFloat() * (float) Math.PI * 2, (float) Math.sqrt(rnd.nextFloat()) * srad)));
        }
        Coord3f nv = Environ.get(((Gob) owner).glob).wind().mul(0.4f);
        nv = nv.rot(Coord3f.zu, (float) ((Gob) owner).a);
        for (Iterator<Boll> i = bollar.iterator(); i.hasNext(); ) {
            Boll boll = i.next();
            if (boll.tick(dt, nv))
                i.remove();
        }
        return (!spawn && bollar.isEmpty());
    }

    class Boll {
        static final float sr = 0.3f, sv = 0.3f;
        float x, y, z;
        float xv, yv, zv;
        float t = 0;

        Boll(Coord3f pos) {
            x = pos.x + (float) (rnd.nextGaussian() * sr);
            y = pos.y + (float) (rnd.nextGaussian() * sr);
            z = pos.z;
            xv = (float) rnd.nextGaussian() * sv;
            yv = (float) rnd.nextGaussian() * sv;
            zv = initzv;
        }

        public boolean tick(float dt, Coord3f nv) {
            if (Config.disableAllAnimations) return (false);
            float xvd = xv - nv.x, yvd = yv - nv.y, zvd = zv - nv.z;
            float xa = (-xvd * 0.2f) + ((float) rnd.nextGaussian() * 0.5f), ya = (-yvd * 0.2f) + ((float) rnd.nextGaussian() * 0.5f), za = ((-zvd + initzv) * 0.2f) + ((float) rnd.nextGaussian() * 2.0f);
            xv += dt * xa;
            yv += dt * ya;
            zv += dt * za;
            x += xv * dt;
            y += yv * dt;
            z += zv * dt;
            t += dt;
            return (t > life);
        }
    }

    public void draw(GOut g) {
        if (Config.disableAllAnimations) return;
        updpos(g);
        if (posb == null)
            return;
        g.apply();
        BGL gl = g.gl;
        gl.glEnable(GL2.GL_POINT_SPRITE);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, posb);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL.GL_FLOAT, 0, colb);
        gl.glNormal3f(0, 0, 1);
        gl.glDrawArrays(GL.GL_POINTS, 0, bollar.size());
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        gl.glDisable(GL2.GL_POINT_SPRITE);
    }

    private void updpos(GOut d) {
        if (bollar.size() < 1) {
            posb = colb = null;
            return;
        }
        if ((posb == null) || (posb.capacity() < bollar.size() * 3)) {
            int n = 3 * bollar.size() / 2;
            posb = Utils.mkfbuf(n * 3);
            colb = Utils.mkfbuf(n * 4);
        }
        float r = col.getRed() / 255.0f;
        float g = col.getGreen() / 255.0f;
        float b = col.getBlue() / 255.0f;
        float a = col.getAlpha() / 255.0f;
        FloatBuffer tpos = Utils.wfbuf(3 * bollar.size()), tcol = Utils.wfbuf(4 * bollar.size());
        for (Boll boll : bollar) {
            tpos.put(boll.x).put(boll.y).put(boll.z);
            tcol.put(r).put(g).put(b).put(a * (float) Utils.clip(1.0 - Math.pow(boll.t / life, fadepow), 0, 1));
        }
        d.gl.bglCopyBufferf(posb, 0, tpos, 0, tpos.capacity());
        d.gl.bglCopyBufferf(colb, 0, tcol, 0, tcol.capacity());
    }

    private static final Uniform bollsz = new Uniform(FLOAT);
    private static final ShaderMacro prog = new ShaderMacro() {
        public void modify(final ProgramContext prog) {
            prog.vctx.ptsz.mod(new Macro1<Expression>() {
                final Function pdiv = new Function.Def(FLOAT) {{
                    Expression vec = param(PDir.IN, VEC4).ref();
                    code.add(new Return(div(pick(vec, "x"), pick(vec, "w"))));
                }};

                public Expression expand(Expression in) {
                    return (mul(sub(pdiv.call(prog.vctx.projxf(add(prog.vctx.eyev.depref(), vec4(bollsz.ref(), l(0.0), l(0.0), l(0.0))))),
                            pdiv.call(prog.vctx.posv.depref())),
                            pick(MiscLib.screensize.ref(), "x")));
                }
            }, 0);
            Tex2D.texcoord(prog.fctx).mod(new Macro1<Expression>() {
                public Expression expand(Expression in) {
                    return (FragmentContext.gl_PointCoord.ref());
                }
            }, 0);
        }
    };

    private final GLState projsz = new States.ProgPointSize(prog) {
        public void reapply(GOut g) {
            g.gl.glUniform1f(g.st.prog.uniform(bollsz), sz);
        }

        public void apply(GOut g) {
            super.apply(g);
            reapply(g);
        }
    };

    public boolean setup(RenderList r) {
        r.prepo(mat);
        r.prepo(States.presdepth);
        if (loc != null)
            r.prepo(loc);
        r.prepo(eyesort);
        r.prepo(States.vertexcolor);
        r.prepo(projsz);
        r.state().put(States.fsaa.slot, null);
        return (true);
    }

    public void delete() {
        spawn = false;
    }
}
