package haven.sloth.gfx;

import haven.Coord3f;
import haven.GLState;
import haven.GOut;
import haven.Gob;
import haven.Light;
import haven.Material;
import haven.RenderList;
import haven.Sprite;
import haven.Utils;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* it must be done as a weather condition like a narcotic fog
 * */
public class SnowFall extends Sprite {
    private static final Random rnd = new Random();

    public static class Flake {
        Coord3f position;
        Coord3f velocity;
        Coord3f normal;
        boolean done = false;

        Flake(Coord3f pos) {
            this.position = new Coord3f(pos.x, pos.y, pos.z);
            this.velocity = new Coord3f(0.0F, 0.0F, (pos.z - 500F) * .98f / 1000.0F);//неправильная начальная скорость
            this.normal = new Coord3f(0, 0, 0).norm();
        }

        boolean tick(float dt) {
            this.velocity.z = this.velocity.z + -.98f * dt;
            this.velocity.x += dt * (float) rnd.nextGaussian() * 0.3F;
            this.velocity.y += dt * (float) rnd.nextGaussian() * 0.3F;
            this.position.x += dt * this.velocity.x;
            this.position.y += dt * this.velocity.y;
            this.position.z += dt * this.velocity.z;
            if (this.position.z < -500.0F) {
                this.done = true;
            }
            return done;
        }

        void move(Coord3f pos) {
            this.position = this.position.sub(pos);
        }
    }


    private final GLState mat;
    private final List<Flake> flakes = new ArrayList<>();
    private float de = 0.0F;
    private final Coord3f offset = new Coord3f(-1000f, -1000f, 0f);
    private final Coord3f sz = new Coord3f(2000f, 2000f, 0f);
    private final Gob master;
    private Coord3f lastCoord;  //нужно доработать - персонаж разворачивается, нужно добавить угол

    FloatBuffer posb = null;
    FloatBuffer nrmb = null;

    public SnowFall(final Gob g) {
        super(g, null);
        master = g;
        lastCoord = g.getc();

        mat = new Material.Colors(new Color(255, 255, 255),
                new Color(255, 255, 255),
                new Color(255, 255, 255),
                new Color(255, 255, 255), 1.0F);
    }

    public void draw(GOut g) {
//        for (Flake boll : flakes) {
//            boll.move(master.getc().sub(lastCoord));
//        }
//        lastCoord = master.getrc();
        updpos(g);
        if (posb == null)
            return;
        g.apply();
        posb.rewind();
        nrmb.rewind();
        g.gl.glPointSize(2F);
        g.gl.glEnableClientState(32884);
        g.gl.glVertexPointer(3, 5126, 0, posb);
        g.gl.glEnableClientState(32885);
        g.gl.glNormalPointer(5126, 0, nrmb);
        g.gl.glDrawArrays(0, 0, flakes.size());
        g.gl.glDisableClientState(32884);
        g.gl.glDisableClientState(32885);
    }

    void updpos(GOut g) {
        if (flakes.size() < 1) {
            posb = null;
            nrmb = null;
            return;
        }
        if ((posb == null) || (posb.capacity() < flakes.size() * 3)) {
            int i = posb == null ? 512 : posb.capacity() / 3;
            posb = Utils.mkfbuf(i * 2 * 3);
            nrmb = Utils.mkfbuf(i * 2 * 3);
        }
        FloatBuffer pos = Utils.wfbuf(3 * flakes.size());
        FloatBuffer norm = Utils.wfbuf(3 * flakes.size());
        for (Flake boll : flakes) {
            pos.put(boll.position.x).put(boll.position.y).put(boll.position.z);
            norm.put(boll.normal.x).put(boll.normal.y).put(boll.normal.z);
        }
        g.gl.bglCopyBufferf(posb, 0, pos, 0, pos.capacity());
        g.gl.bglCopyBufferf(nrmb, 0, norm, 0, pos.capacity());
    }

    @Override
    public boolean tick(int dt) {
        float f = dt / 1000.0F;
        float str = 1000f;
        de += f * str;
        if (de > 1.0F) {
            de -= 1.0F;
            flakes.add(new Flake(offset.add(rnd.nextFloat() * sz.x, rnd.nextFloat() * sz.y, rnd.nextFloat() * 500F)));
        }
        //Remove dead dust
        flakes.removeIf(boll -> boll.tick(f));
        return false;
    }

    @Override
    public boolean setup(RenderList rl) {
        rl.prepo(Light.deflight);
        rl.prepo(mat);
        return true;
    }
}
