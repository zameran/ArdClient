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

public class CowRoster extends CattleRoster<Ochs> {
	static {
		dev.checkFileVersion("gfx/hud/rosters/cow", 33);
	}
    public static List<Column> cols = initcols(
	new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/quality"), Comparator.comparing((Ochs e) -> e.q).reversed()),

	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/meatquantity"), Comparator.comparing((Ochs e) -> e.meat).reversed()),
	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/milkquantity"), Comparator.comparing((Ochs e) -> e.milk).reversed()),

	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/meatquality"), Comparator.comparing((Ochs e) -> e.meatq).reversed()),
	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/milkquality"), Comparator.comparing((Ochs e) -> e.milkq).reversed()),
	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/hidequality"), Comparator.comparing((Ochs e) -> e.hideq).reversed()),

	new Column<Ochs>(Resource.local().load("gfx/hud/rosters/breedingquality"), Comparator.comparing((Ochs e) -> e.seedq).reversed())
    );
    protected List<Column> cols() {return(cols);}

    public static CattleRoster mkwidget(UI ui, Object... args) {
	return(new CowRoster());
    }

    public Ochs parse(Object... args) {
	int n = 0;
	long id = (Long)args[n++];
	String name = (String)args[n++];
	Ochs ret = new Ochs(id, name);
	ret.grp = (Integer)args[n++];
	ret.q = ((Number)args[n++]).doubleValue();
	ret.meat = (Integer)args[n++];
	ret.milk = (Integer)args[n++];
	ret.meatq = (Integer)args[n++];
	ret.milkq = (Integer)args[n++];
	ret.hideq = (Integer)args[n++];
	ret.seedq = (Integer)args[n++];
	return(ret);
    }

    public TypeButton button() {
	return(typebtn(Resource.local().load("gfx/hud/rosters/btn-cow"),
		       Resource.local().load("gfx/hud/rosters/btn-cow-d")));
    }
}
