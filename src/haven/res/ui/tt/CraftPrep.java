package haven.res.ui.tt;

import haven.GItem;
import haven.ItemInfo;

import java.awt.Color;

public class CraftPrep extends ItemInfo implements GItem.ColorInfo {
	public static final Color mycol = new Color((int) 0, (int) 255, (int) 0, (int) 64);
	public static final Color notmycol = new Color((int) 255, (int) 128, (int) 0, (int) 64);
	public final boolean mine;

	public CraftPrep(Owner owner, boolean bl) {
		super((Owner) owner);
		this.mine = bl;
	}

	public static ItemInfo mkinfo(Owner owner, Object... arrobject) {
		boolean bl = true;
		if (arrobject.length > 1) {
			bl = ((Integer) arrobject[1]).intValue() != 0;
		}
		return new CraftPrep((Owner) owner, (boolean) bl);
	}

	public Color olcol() {
		return this.mine ? mycol : notmycol;
	}
}