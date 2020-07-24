package haven.res.ui.tt;

import haven.ItemInfo;

public class CustName
		implements ItemInfo.InfoFactory {
	public ItemInfo build(ItemInfo.Owner owner, Object ... arrobject) {
		return new ItemInfo.Name((ItemInfo.Owner)owner, (String)((String)arrobject[1]));
	}
}