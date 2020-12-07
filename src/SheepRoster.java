/* Preprocessed source code */
/* $use: ui/croster */

import haven.Resource;
import haven.UI;
import haven.res.ui.croster.CattleRoster;
import haven.res.ui.croster.Column;
import haven.res.ui.croster.Entry;
import haven.res.ui.croster.TypeButton;
import modification.dev;

import java.util.Comparator;
import java.util.List;

public class SheepRoster extends CattleRoster<Sheep> {
	static {
		dev.checkFileVersion("gfx/hud/rosters/sheep", 24);
	}
    public static List<Column> cols = initcols(
            new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/quality"), Comparator.comparing((Sheep e) -> e.q).reversed()),

            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/meatquantity"), Comparator.comparing((Sheep e) -> e.meat).reversed()),
            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/milkquantity"), Comparator.comparing((Sheep e) -> e.milk).reversed()),
            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/woolquantity"), Comparator.comparing((Sheep e) -> e.milk).reversed()),

            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/meatquality"), Comparator.comparing((Sheep e) -> e.meatq).reversed()),
            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/milkquality"), Comparator.comparing((Sheep e) -> e.milkq).reversed()),
            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/woolquality"), Comparator.comparing((Sheep e) -> e.milkq).reversed()),
            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/hidequality"), Comparator.comparing((Sheep e) -> e.hideq).reversed()),

            new Column<Sheep>(Resource.local().load("gfx/hud/rosters/breedingquality"), Comparator.comparing((Sheep e) -> e.seedq).reversed())
    );

    protected List<Column> cols() {
        return (cols);
    }

    public static CattleRoster mkwidget(UI ui, Object... args) {
        return (new SheepRoster());
    }

    public Sheep parse(Object... args) {
        int n = 0;
        long id = (Long) args[n++];
        String name = (String) args[n++];
        Sheep ret = new Sheep(id, name);
        ret.grp = (Integer) args[n++];
        ret.q = ((Number) args[n++]).doubleValue();
        ret.meat = (Integer) args[n++];
        ret.milk = (Integer) args[n++];
        ret.wool = (Integer) args[n++];
        ret.meatq = (Integer) args[n++];
        ret.milkq = (Integer) args[n++];
        ret.woolq = (Integer) args[n++];
        ret.hideq = (Integer) args[n++];
        ret.seedq = (Integer) args[n++];
        return (ret);
    }

    public TypeButton button() {
        return (typebtn(Resource.local().load("gfx/hud/rosters/btn-sheep"),
                Resource.local().load("gfx/hud/rosters/btn-sheep-d")));
    }
}
