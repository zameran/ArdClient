package modification;

import haven.Button;
import haven.CheckListbox;
import haven.Scrollbar;
import haven.Text;
import haven.TextEntry;
import haven.Widget;
import haven.WidgetVerticalAppender;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchCheckListBox extends Widget {
    public CheckListbox cwl;
    public TextEntry search, addentry;
    public Button addbtn;

    public SearchCheckListBox(List<String> list, int h) {
        super();
        WidgetVerticalAppender wva = new WidgetVerticalAppender(this);
        cwl = new CheckListbox(calcWidthString(list), h);
        search = new TextEntry(cwl.sz.x, "");
        addbtn = new Button(45, "Add");
        addentry = new TextEntry(cwl.sz.x - addbtn.sz.x - 1, "");

        wva.add(cwl);
        wva.add(search);
        wva.addRow(addentry, addbtn);
        pack();
    }

    public int calcWidthString(List<String> list) {
        Optional<Integer> ow = list.stream().map((v) -> Text.render(v).sz().x).collect(Collectors.toList()).stream().reduce(Integer::max);
        int w = Scrollbar.sflarp.sz().x + CheckListbox.chk.sz().x + 5;
        if (ow.isPresent() && list.size() > 0)
            w += ow.get();
        return (w);
    }
}
