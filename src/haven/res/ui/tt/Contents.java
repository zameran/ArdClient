package haven.res.ui.tt;

import haven.ItemInfo;

import java.util.List;

public class Contents implements ItemInfo.InfoFactory {
	public ItemInfo build(ItemInfo.Owner owner, Object... arrobject) {
		return new ItemInfo.Contents((ItemInfo.Owner) owner, (List) ItemInfo.buildinfo((ItemInfo.Owner) owner, (Object[]) ((Object[]) arrobject[1])));
	}
}
