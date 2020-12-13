package haven.sloth.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ObservableCollection<T> implements Iterable<T> {
    private final Collection<T> base;
    private final Set<ObservableListener<T>> listeners = new HashSet<>();

    public ObservableCollection(Collection<T> base) {
        this.base = base;
    }

    public boolean add(T item) {
        if (base.add(item)) {
            synchronized (listeners) {
                if (item != null)
                    listeners.forEach((lst) -> lst.added(item));
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean edit(T olditem, T newitem) {
        if (replaceItem(olditem, newitem)) {
            synchronized (listeners) {
                listeners.forEach((lst) -> lst.edited(olditem, newitem));
            }
            return true;
        }
        return false;
    }

    public boolean remove(T item) {
        if (base.remove(item)) {
            listeners.forEach((lst) -> lst.remove(item));
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return base.size();
    }


    public boolean contains(T other) {
        return base.contains(other);
    }

    public void addListener(final ObservableListener<T> listener) {
        synchronized (listeners) {
            if (listener != null) {
                listeners.add(listener);
                listener.init(base);
            }
        }
    }

    public void removeListener(final ObservableListener<T> listener) {
        synchronized (listeners) {
            if (listener != null)
                listeners.remove(listener);
        }
    }

    public Iterator<T> iterator() {
        return base.iterator();
    }

    public boolean replaceItem(T olditem, T newitem) {
        int n = 0;
        boolean s = false;
        for (T item : base) {
            if (item.equals(olditem)) {
                base.remove(item);
                s = true;
                break;
            }
            n++;
        }
        if (!s) return false;

        ArrayList<T> newbase = new ArrayList<>(base);
        newbase.add(n, newitem);

        base.clear();
        if (!base.addAll(newbase)) return false;
        return true;
    }
}
