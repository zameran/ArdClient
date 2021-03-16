package haven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CharacterInfo {

    public final Constipation constipation = new Constipation();

    public static class Constipation {
        public final List<Data> els = new ArrayList<Data>();

        public void update(ResData t, double a) {
            Data el = get(t);
            if (el != null)
                if (a == 1.0)
                    els.remove(el);
                else
                    el.update(a);
            else
                els.add(new Data(t, a));
        }

        public Data get(int i) {
            return els.size() > i ? els.get(i) : null;
        }

        public Data get(ResData resData) {
            for (Data el : els)
                if (el.res.equals(resData))
                    return (el);
            return (null);
        }

        public static class Data {
            private final Map<Class, BufferedImage> renders = new HashMap<>();
            public final ResData res;
            public double value;

            public Data(ResData res, double value) {
                this.res = res;
                this.value = value;
            }

            public void update(double a) {
                value = a;
                renders.clear();
            }

            private BufferedImage render(Class type, Function<Data, BufferedImage> renderer) {
                if (!renders.containsKey(type)) {
                    renders.put(type, renderer.apply(this));
                }
                return renders.get(type);
            }
        }

        private final Map<Class, Function<Data, BufferedImage>> renderers = new HashMap<>();

        public void addRenderer(Class type, Function<Data, BufferedImage> renderer) {
            renderers.put(type, renderer);
        }

        public boolean hasRenderer(Class type) {
            return renderers.containsKey(type);
        }

        public BufferedImage render(Class type, Data data) {
            try {
                return renderers.containsKey(type) ? data.render(type, renderers.get(type)) : null;
            } catch (Loading ignored) {
            }
            return null;
        }
    }
}
