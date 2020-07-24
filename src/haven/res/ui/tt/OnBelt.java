package haven.res.ui.tt;

import java.awt.Color;

import haven.ItemInfo;
import haven.GItem;



public class OnBelt implements ItemInfo.InfoFactory {
	public static final Color olcol;

	public ItemInfo build(final ItemInfo.Owner owner, final Object... array) {
		return (ItemInfo) new Info(this, owner);
	}

	static {
		olcol = new Color(255, 255, 0, 64);
	}

	class Info extends ItemInfo implements GItem.ColorInfo {

		// $FF: synthetic field
		final OnBelt this$0;


		public Info(OnBelt var1, Owner var2) {
			super(var2);
			this.this$0 = var1;
		}

		public Color olcol() {
			return OnBelt.olcol;
		}
	}
}

