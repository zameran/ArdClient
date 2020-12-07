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

public class GoatRoster extends CattleRoster<Goat> {
	static {
		dev.checkFileVersion("gfx/hud/rosters/goat", 25);
	}
    public static List<Column> cols = initcols(
            new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

            new Column<Goat>(Resource.local().load("gfx/hud/rosters/quality"), Comparator.comparing((Goat e) -> e.q).reversed()),

            new Column<Goat>(Resource.local().load("gfx/hud/rosters/meatquantity"), Comparator.comparing((Goat e) -> e.meat).reversed()),
            new Column<Goat>(Resource.local().load("gfx/hud/rosters/milkquantity"), Comparator.comparing((Goat e) -> e.milk).reversed()),
            new Column<Goat>(Resource.local().load("gfx/hud/rosters/woolquantity"), Comparator.comparing((Goat e) -> e.milk).reversed()),

            new Column<Goat>(Resource.local().load("gfx/hud/rosters/meatquality"), Comparator.comparing((Goat e) -> e.meatq).reversed()),
            new Column<Goat>(Resource.local().load("gfx/hud/rosters/milkquality"), Comparator.comparing((Goat e) -> e.milkq).reversed()),
            new Column<Goat>(Resource.local().load("gfx/hud/rosters/woolquality"), Comparator.comparing((Goat e) -> e.milkq).reversed()),
            new Column<Goat>(Resource.local().load("gfx/hud/rosters/hidequality"), Comparator.comparing((Goat e) -> e.hideq).reversed()),

            new Column<Goat>(Resource.local().load("gfx/hud/rosters/breedingquality"), Comparator.comparing((Goat e) -> e.seedq).reversed())
    );

    protected List<Column> cols() {
        return (cols);
    }

    public static CattleRoster mkwidget(UI ui, Object... args) {
        return (new GoatRoster());
    }

    public Goat parse(Object... args) {
        int n = 0;
        long id = (Long) args[n++];
        String name = (String) args[n++];
        Goat ret = new Goat(id, name);
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
        return (typebtn(Resource.local().load("gfx/hud/rosters/btn-goat"),
                Resource.local().load("gfx/hud/rosters/btn-goat-d")));
    }
}
