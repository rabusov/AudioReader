package ru.rabus.audioreader.Items;

/**
 * Created by Сергей on 24.01.2018.
 */

public class LabelItem {
    public int id;
    public int id_items;
    public long Position;
    public String Title;
    public String Comment;
    public LabelItem(int id_items)
    {
        this.id=0;
        this.id_items=id_items;
        this.Position=0;
        this.Title="Label";
        this.Comment="";
    }
    @Override
    public String toString() {
        return Title;
    }
}
