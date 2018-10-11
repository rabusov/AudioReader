package ru.rabus.audioreader.Items;

import java.util.Date;

import static ru.rabus.audioreader.BaseCompactActivity.getFileNameFromPath;

/**
 * Created by Сергей on 23.01.2018.
 */

public class AudioItem {
        public int id;
        public String Content;
        public String FullName;
        public long FileSize;
        public int isExists;
        public int LastTimePosition;
        public long haveRead;

        public AudioItem() {
            this.id = this.isExists = this.LastTimePosition = 0;
            this.Content = this.FullName = "";  this.FileSize = 0;
            this.haveRead = 0;
        }
    public AudioItem(String FullName, String Content, long FileSize, int LastTimePosition, int isExists, long _haveRead ) {
        this.id = 0;
        this.FullName = FullName;
        this.Content = Content;
        this.FileSize = FileSize;
        this.isExists = isExists;
        this.LastTimePosition = LastTimePosition;
        this.haveRead = _haveRead;
        }
        @Override
        public String toString() {
            return getFileNameFromPath(FullName);
        }
}
