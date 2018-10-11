package ru.rabus.audioreader.dummy;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.rabus.audioreader.Items.AudioItem;

import static ru.rabus.audioreader.BaseCompactActivity.getFileNameFromPath;
import static ru.rabus.audioreader.DB.ListItemsIDS;
import static ru.rabus.audioreader.DB.getItem;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<AudioItem> ITEMS = new ArrayList<AudioItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<Integer, AudioItem> ITEM_MAP = new HashMap<Integer, AudioItem>();

    //private static final int COUNT = 25;

    static {
        // Add some sample items.
        int[] ids = ListItemsIDS();
        for (int i = 0; i < ids.length; i++) {
            addItem(createDummyItem(ids[i]));
        }
    }
    public static void addItem(AudioItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
    public static void updItem(AudioItem item)
    {
        ITEM_MAP.remove(item.id);
        ITEM_MAP.put(item.id, item);
    }
    public static void delItem(AudioItem item)
    {
       ITEMS.remove(item);
       ITEM_MAP.remove(item.id);
    }
    private static AudioItem createDummyItem(int position) {
        return getItem(position);
        //return new DummyItem(String.valueOf(ai.id),  getFileNameFromPath(ai.FullName), ai.FullName);
    }
    public static void fillItemsList()
    {
        if (ITEMS.size() > 0){
            ITEMS.clear();
            ITEM_MAP.clear();
        }
        int[] ids = ListItemsIDS();
        for (int i = 0; i < ids.length; i++) {
            addItem(createDummyItem(ids[i]));
        }
    }
}
