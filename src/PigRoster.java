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

public class PigRoster extends CattleRoster<Pig> {
	static {
		dev.checkFileVersion("gfx/hud/rosters/pig", 24);
	}
    public static List<Column> cols = initcols(
            new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

            new Column<Pig>(Resource.local().load("gfx/hud/rosters/quality"), Comparator.comparing((Pig e) -> e.q).reversed()),

            new Column<Pig>(Resource.local().load("gfx/hud/rosters/trufflesnout"), Comparator.comparing((Pig e) -> e.prc).reversed()),

            new Column<Pig>(Resource.local().load("gfx/hud/rosters/meatquantity"), Comparator.comparing((Pig e) -> e.meat).reversed()),
            new Column<Pig>(Resource.local().load("gfx/hud/rosters/milkquantity"), Comparator.comparing((Pig e) -> e.milk).reversed()),

            new Column<Pig>(Resource.local().load("gfx/hud/rosters/meatquality"), Comparator.comparing((Pig e) -> e.meatq).reversed()),
            new Column<Pig>(Resource.local().load("gfx/hud/rosters/milkquality"), Comparator.comparing((Pig e) -> e.milkq).reversed()),
            new Column<Pig>(Resource.local().load("gfx/hud/rosters/hidequality"), Comparator.comparing((Pig e) -> e.hideq).reversed()),

            new Column<Pig>(Resource.local().load("gfx/hud/rosters/breedingquality"), Comparator.comparing((Pig e) -> e.seedq).reversed())
    );

    protected List<Column> cols() {
        return (cols);
    }

    public static CattleRoster mkwidget(UI ui, Object... args) {
        return (new PigRoster());
    }

    public Pig parse(Object... args) {
        int n = 0;
        long id = (Long) args[n++];
        String name = (String) args[n++];
        Pig ret = new Pig(id, name);
        ret.grp = (Integer) args[n++];
        ret.q = ((Number) args[n++]).doubleValue();
        ret.meat = (Integer) args[n++];
        ret.milk = (Integer) args[n++];
        ret.meatq = (Integer) args[n++];
        ret.milkq = (Integer) args[n++];
        ret.hideq = (Integer) args[n++];
        ret.seedq = (Integer) args[n++];
        ret.prc = (Integer) args[n++];
        return (ret);
    }

    public TypeButton button() {
        return (typebtn(Resource.local().load("gfx/hud/rosters/btn-pig"),
                Resource.local().load("gfx/hud/rosters/btn-pig-d")));
    }
}
