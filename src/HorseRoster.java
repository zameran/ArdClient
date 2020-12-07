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

public class HorseRoster extends CattleRoster<Horse> {
	static {
		dev.checkFileVersion("gfx/hud/rosters/horse", 24);
	}
    public static List<Column> cols = initcols(
            new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/quality"), Comparator.comparing((Horse e) -> e.q).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/endurance"), Comparator.comparing((Horse e) -> e.end).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/stamina"), Comparator.comparing((Horse e) -> e.stam).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/metabolism"), Comparator.comparing((Horse e) -> e.mb).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/meatquantity"), Comparator.comparing((Horse e) -> e.meat).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/milkquantity"), Comparator.comparing((Horse e) -> e.milk).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/meatquality"), Comparator.comparing((Horse e) -> e.meatq).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/milkquality"), Comparator.comparing((Horse e) -> e.milkq).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/hidequality"), Comparator.comparing((Horse e) -> e.hideq).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/breedingquality"), Comparator.comparing((Horse e) -> e.seedq).reversed())
    );

    protected List<Column> cols() {
        return (cols);
    }

    public static CattleRoster mkwidget(UI ui, Object... args) {
        return (new HorseRoster());
    }

    public Horse parse(Object... args) {
        int n = 0;
        long id = (Long) args[n++];
        String name = (String) args[n++];
        Horse ret = new Horse(id, name);
        ret.grp = (Integer) args[n++];
        ret.q = ((Number) args[n++]).doubleValue();
        ret.meat = (Integer) args[n++];
        ret.milk = (Integer) args[n++];
        ret.meatq = (Integer) args[n++];
        ret.milkq = (Integer) args[n++];
        ret.hideq = (Integer) args[n++];
        ret.seedq = (Integer) args[n++];
        ret.end = (Integer) args[n++];
        ret.stam = (Integer) args[n++];
        ret.mb = (Integer) args[n++];
        return (ret);
    }

    public TypeButton button() {
        return (typebtn(Resource.local().load("gfx/hud/rosters/btn-horse"),
                Resource.local().load("gfx/hud/rosters/btn-horse-d")));
    }
}
