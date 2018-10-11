package ru.rabus.audioreader.dummy;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.rabus.audioreader.DB;
import ru.rabus.audioreader.ItemDetailFragment;
import ru.rabus.audioreader.Items.LabelItem;

import static ru.rabus.audioreader.DB.getItem;

public class LabelContent {
    public static List<LabelItem> ITEMS = new ArrayList<LabelItem>();
    public static Map<Integer, LabelItem> ITEM_MAP = new HashMap<Integer, LabelItem>();

    public static void addItem(LabelItem item) {
        if(ITEMS!=null) {
            ITEMS.add(item);
            ITEM_MAP.put(item.id, item);
        }
    }
    public static void updItem(LabelItem item)
    {
        if(ITEMS!=null) {
            ITEM_MAP.remove(item.id);
            ITEM_MAP.put(item.id, item);
        }
    }
    public static void delItem(LabelItem item)
    {
        if(ITEMS!=null) {
            ITEMS.remove(item);
            ITEM_MAP.remove(item.id);
        }
    }

    public static void fillItemsList(int id)
    {
        if (ITEMS!=null && ITEMS.size() > 0){
            ITEMS.clear();
            ITEM_MAP.clear();
        }
        ITEMS =  DB.getLabels(id);
        if (ITEMS != null)
        for  (LabelItem item : ITEMS) {
            ITEM_MAP.put(item.id, item);
        }
    }

}
