/* Preprocessed source code */
/* $use: ui/croster */

import haven.Resource;
import haven.UI;
import haven.res.ui.croster.CattleRoster;
import haven.res.ui.croster.Column;
import haven.res.ui.croster.Entry;
import haven.res.ui.croster.TypeButton;

import java.util.Comparator;
import java.util.List;

public class HorseRoster extends CattleRoster<Horse> {
    public static List<Column> cols = initcols(
            new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/quality", 2), Comparator.comparing((Horse e) -> e.q).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/endurance", 1), Comparator.comparing((Horse e) -> e.end).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/stamina", 1), Comparator.comparing((Horse e) -> e.stam).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/metabolism", 1), Comparator.comparing((Horse e) -> e.mb).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/meatquantity", 1), Comparator.comparing((Horse e) -> e.meat).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/milkquantity", 1), Comparator.comparing((Horse e) -> e.milk).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/meatquality", 1), Comparator.comparing((Horse e) -> e.meatq).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/milkquality", 1), Comparator.comparing((Horse e) -> e.milkq).reversed()),
            new Column<Horse>(Resource.local().load("gfx/hud/rosters/hidequality", 1), Comparator.comparing((Horse e) -> e.hideq).reversed()),

            new Column<Horse>(Resource.local().load("gfx/hud/rosters/breedingquality", 1), Comparator.comparing((Horse e) -> e.seedq).reversed())
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
        return (typebtn(Resource.local().load("gfx/hud/rosters/btn-horse", 2),
                Resource.local().load("gfx/hud/rosters/btn-horse-d", 2)));
    }
}
