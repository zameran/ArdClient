package haven.res.ui.tt;

import haven.GItem;
import haven.ItemInfo;

public class Amount
		implements ItemInfo.InfoFactory {
	public ItemInfo build(ItemInfo.Owner owner, Object ... arrobject) {
		int n = ((Integer)arrobject[1]).intValue();
		return new GItem.Amount((ItemInfo.Owner)owner, (int)n);
	}
}