package haven.res.ui.tt;

import haven.*;
import haven.CharWnd.Constipations;
import haven.ItemInfo.Owner;
import haven.ItemInfo.Tip;
import haven.Resource.Image;
import haven.Resource.Tooltip;

import java.awt.image.BufferedImage;

import haven.Indir;
import haven.ItemInfo.InfoFactory;
import haven.ItemInfo.Owner;
import haven.Resource.Resolver;



public class Satiate implements InfoFactory {
	public ItemInfo build(Owner var1, Object... var2) {
		Indir var3 = ((Resolver) var1.context(Resolver.class)).getres(((Integer) var2[1]).intValue());
		double var4 = ((Number) var2[2]).doubleValue();
		return new Satiate$1 (this, var1, var3, var4);
	}


	class Satiate$1 extends Tip {

		// $FF: synthetic field
		final Indir val$res;
		// $FF: synthetic field
		final double val$f;
		// $FF: synthetic field
		final Satiate this$0;


		Satiate$1(Satiate var1, Owner var2, Indir var3, double var4) {
			super(var2);
			this.this$0 = var1;
			this.val$res = var3;
			this.val$f = var4;
		}

		public BufferedImage tipimg() {
			BufferedImage var1 = Text.render("Satiate ").img;
			int var2 = var1.getHeight();
			BufferedImage var3 = PUtils.convolvedown(((Image) ((Resource) this.val$res.get()).layer(Resource.imgc)).img, new Coord(var2, var2), Constipations.tflt);
			BufferedImage var4 = RichText.render(String.format("%s by $col[255,128,128]{%d%%}", new Object[]{((Tooltip) ((Resource) this.val$res.get()).layer(Resource.tooltip)).t, Integer.valueOf((int) Math.round((1.0D - this.val$f) * 100.0D))}), 0, new Object[0]).img;
			return catimgsh(0, new BufferedImage[]{var1, var3, var4});
		}
	}
}


