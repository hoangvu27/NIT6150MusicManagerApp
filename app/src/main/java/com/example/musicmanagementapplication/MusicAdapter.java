package com.example.musicmanagementapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends BaseAdapter {

    private Context context;
    private List<Music> musicList;
    private List<String> musicKeys;

    public MusicAdapter(Context context, List<Music> musicList, List<String> musicKeys) {
        this.context = context;

        this.musicList = new ArrayList<>(); // Initialize internal lists
        this.musicKeys = new ArrayList<>();

        for (int i = 0; i < musicList.size(); i++) {
            Music originalMusic = musicList.get(i);
            // Create a new Music object to avoid reference sharing
            Music musicCopy = new Music(originalMusic.getTitle(), originalMusic.getArtist());
            this.musicList.add(musicCopy);
            this.musicKeys.add(musicKeys.get(i));
        }
    }

    /**
     * Get music size
     * @return the size of music list
     */
    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get view of music list
     * @param position the position of music in the list
     * @param convertView the final view of music list that will be displaued
     * @param parent the parent view of all layouts and UI components
     * @return return the view of music list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false);
        }

        TextView musicInfo = convertView.findViewById(R.id.musicInfo);
        Button editButton = convertView.findViewById(R.id.editButton);

        Music music = musicList.get(position);
        String musicId = musicKeys.get(position);

        musicInfo.setText("Title: " + music.title + "\n" + "Artist: " + music.artist);

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, MusicManagementActivity.class);
            intent.putExtra("musicId", musicId);
            context.startActivity(intent);
        });

        return convertView;
    }

    public List<String> getMusicKeys() {
        return musicKeys;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void updateData(List<Music> newMusicList, List<String> newMusicKeys) {
        this.musicList.clear();
        this.musicKeys.clear();

        for (int i = 0; i < newMusicList.size(); i++) {
            Music originalMusic = newMusicList.get(i);
            Music musicCopy = new Music(originalMusic.getTitle(), originalMusic.getArtist());
            this.musicList.add(musicCopy);
            this.musicKeys.add(newMusicKeys.get(i));
        }
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
}

