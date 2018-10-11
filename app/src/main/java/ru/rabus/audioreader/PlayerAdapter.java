package ru.rabus.audioreader;

import java.io.FileDescriptor;
import java.net.URI;

public interface PlayerAdapter {



    void release();

    boolean isPlaying();

    void play();

    void reset();

    void pause();

    void initializeProgressCallback(int position);

    void seekTo(int position);
    void forward(int millisec);
    void back(int millisec);
    void loadMedia(int resourceId, int position);
    void loadMedia(URI uri, int position);
    void loadMedia(String str, int position);
}
