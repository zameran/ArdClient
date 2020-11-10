package haven.sloth.gob;

import haven.GLState;
import haven.Gob;
import haven.Material;
import haven.Message;
import haven.RenderList;
import haven.Resource;
import haven.SkelSprite;

import java.awt.Color;

public class AggroMark extends SkelSprite implements Gob.Overlay.SetupMod {
    final GLState col = new Material.Colors(Color.RED);
    private static final Resource tgtfx = Resource.local().loadwait("custom/fx/partytgt");
    public static final int id = -4214129;

    private boolean alive = true;
    private boolean current = false;

    public AggroMark() {
        super(null, tgtfx, Message.nil);
    }


    public void rem() {
        alive = false;
    }

    public boolean tick(int dt) {
        super.tick(dt);
        return !alive;
    }

    @Override
    public void setupgob(GLState.Buffer buf) {
    }

    @Override
    public void setupmain(RenderList rl) {
        rl.prepc(col);
    }
}
