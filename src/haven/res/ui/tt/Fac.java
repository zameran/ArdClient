package haven.res.ui.tt;

import haven.ItemInfo;
import haven.resutil.Curiosity;
import haven.ItemInfo;
import haven.Resource;
import haven.resutil.FoodInfo;
import java.util.LinkedList;
import java.util.List;

public class Fac implements ItemInfo.InfoFactory {
	public ItemInfo build(ItemInfo.Owner owner, Object... arrobject) {
		int n = ((Number) arrobject[1]).intValue();
		int n2 = ((Number) arrobject[2]).intValue();
		int n3 = ((Number) arrobject[3]).intValue();
		int n4 = ((Number) arrobject[4]).intValue();
		return new Curiosity((ItemInfo.Owner) owner, (int) n, (int) n2, (int) n3, (int) n4);
	}

	public ItemInfo build(ItemInfo.Owner owner, ItemInfo.Raw raw, Object ... arrobject) {
		int n;
		int n2;
		int n3 = 1;
		double d = ((Number)arrobject[n3++]).doubleValue();
		double d2 = ((Number)arrobject[n3++]).doubleValue();
		double d3 = 0.0;
		if (arrobject[n3] instanceof Number) {
			d3 = ((Number)arrobject[n3++]).doubleValue();
		}
		Object[] arrobject2 = (Object[])arrobject[n3++];
		Object[] arrobject3 = (Object[])arrobject[n3++];
		Object[] arrobject4 = (Object[])arrobject[n3++];
		LinkedList<FoodInfo.Event> linkedList = new LinkedList<FoodInfo.Event>();
		LinkedList<FoodInfo.Effect> linkedList2 = new LinkedList<FoodInfo.Effect>();
		Resource.Resolver resolver = (Resource.Resolver)owner.context(Resource.Resolver.class);
		for (n2 = 0; n2 < arrobject2.length; n2 += 2) {
			linkedList.add(new FoodInfo.Event((Resource)((Resource)resolver.getres((int)((Integer)arrobject2[n2]).intValue()).get()), (double)((Number)arrobject2[n2 + 1]).doubleValue()));
		}
		for (n2 = 0; n2 < arrobject3.length; n2 += 2) {
			linkedList2.add(new FoodInfo.Effect((List)ItemInfo.buildinfo((ItemInfo.Owner)owner, (Object[])new Object[]{(Object[])arrobject3[n2]}), (double)((Number)arrobject3[n2 + 1]).doubleValue()));
		}
		int[] arrn = new int[arrobject4.length * 32];
		int n4 = 0;
		int n5 = 0;
		for (n = 0; n < arrobject4.length; ++n) {
			int n6 = 0;
			int n7 = 1;
			while (n6 < 32) {
				if ((((Integer)arrobject4[n]).intValue() & n7) != 0) {
					arrn[n4++] = n5;
				}
				++n6;
				n7 <<= 1;
				++n5;
			}
		}
		int[] arrn2 = new int[n4];
		for (n = 0; n < n4; ++n) {
			arrn2[n] = arrn[n];
		}
		try {
			return new FoodInfo((ItemInfo.Owner)owner, (double)d, (double)d2, (double)d3, (FoodInfo.Event[])linkedList.toArray(new FoodInfo.Event[0]), (FoodInfo.Effect[])linkedList2.toArray(new FoodInfo.Effect[0]), (int[])arrn2);
		}
		catch (NoSuchMethodError noSuchMethodError) {
			return new FoodInfo((ItemInfo.Owner)owner, (double)d, (double)d2, (FoodInfo.Event[])linkedList.toArray(new FoodInfo.Event[0]), (FoodInfo.Effect[])linkedList2.toArray(new FoodInfo.Effect[0]), (int[])arrn2);
		}
	}
}
