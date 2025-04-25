package com.YouTech.jamba.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.YouTech.jamba.R;
import com.YouTech.jamba.models.PageModel;


public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    PageModel PagerAppList;
    boolean folder_opened = false;
    String op_folder = "Folder_Closed";
    String op_albums = "Album_Closed";
    String op_artists = "Artist_Closed";
    ViewGroup viewGroup;
    String folder_name;
    static ViewPagerAdapter viewPagerAdapter;
    String[] titles = {"Tracks", "Folders", "Albums", "Artists"};

    public ViewPagerAdapter(Context context, PageModel PagerAppList) {
        this.context = context;
        this.PagerAppList = PagerAppList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        viewPagerAdapter = this;
        viewGroup = container;
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = null;
        if (position == 0) {
            layout = (ViewGroup) inflater.inflate(R.layout.fragment_tracks, container, false);
            RecyclerView recyclerView = layout.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            songRecycleAdapter adapter = new songRecycleAdapter(PagerAppList.getTracks(), context);
            recyclerView.setAdapter(adapter);
        } else if (position == 1) {
            layout = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
            RecyclerView recyclerView = layout.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            FolderRecycleAdapter adapter = new FolderRecycleAdapter(PagerAppList.getFolders(), "folders", context);
            recyclerView.setAdapter(adapter);
        } else if (position == 2) {
            layout = (ViewGroup) inflater.inflate(R.layout.fragment_albums, container, false);
            RecyclerView recyclerView = layout.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            AlbumRecycleAdapter adapter = new AlbumRecycleAdapter(PagerAppList.getAlbums(), "albums", context);
            recyclerView.setAdapter(adapter);
        } else if (position == 3) {
            layout = (ViewGroup) inflater.inflate(R.layout.fragment_artists, container, false);
            RecyclerView recyclerView = layout.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            ArtistRecycleAdapter adapter = new ArtistRecycleAdapter(PagerAppList.getArtists(), "artists", context);
            recyclerView.setAdapter(adapter);
        }
        /*else if(position == 1){
            layout[0] = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
            ListView recyclerView = layout[0].findViewById(R.id.list);
            list_adapter adapter = new list_adapter(PagerAppList.getFolders(), "folders", context);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    layout[0] = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
                    ListView recyclerView = layout[0].findViewById(R.id.list);
                    list_adapter2 adapter = new list_adapter2(PagerAppList.getFolders().get(position), context);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position1, long id) {
                            MainActivity.recClicked(PagerAppList.getFolders().get(position).get(position1).third);
                        }
                    });
                }
            });
        }else if(position == 2){
            layout[0] = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
            ListView recyclerView = layout[0].findViewById(R.id.list);
            list_adapter adapter = new list_adapter(PagerAppList.getAlbums(), "albums", context);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    layout[0] = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
                    ListView recyclerView = layout[0].findViewById(R.id.list);
                    list_adapter2 adapter = new list_adapter2(PagerAppList.getAlbums().get(position), context);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position1, long id) {
                            MainActivity.recClicked(PagerAppList.getAlbums().get(position).get(position1).third);
                        }
                    });
                }
            });
        }else if(position == 3){
            layout[0] = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
            ListView recyclerView = layout[0].findViewById(R.id.list);
            list_adapter adapter = new list_adapter(PagerAppList.getArtists(), "folders", context);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    layout[0] = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
                    ListView recyclerView = layout[0].findViewById(R.id.list);
                    list_adapter2 adapter = new list_adapter2(PagerAppList.getArtists().get(position), context);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position1, long id) {
                            MainActivity.recClicked(PagerAppList.getArtists().get(position).get(position1).third);
                        }
                    });
                }
            });
        }*/
        container.addView(layout);
        return layout;
    }

    @Override
    public int getCount() {
        return 4;
    }

    public static void restart_folder(String folder_name, String op) {
        viewPagerAdapter.op_folder = op;
        viewPagerAdapter.folder_name = folder_name;
        viewPagerAdapter.destroyItem(viewPagerAdapter.viewGroup, 1, null);
        viewPagerAdapter.instantiateItem(viewPagerAdapter.viewGroup, 1);
        viewPagerAdapter.notifyDataSetChanged();

    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public static void restart_album(String folder_name, String op) {
        viewPagerAdapter.op_albums = op;
        viewPagerAdapter.folder_name = folder_name;
        viewPagerAdapter.destroyItem(viewPagerAdapter.viewGroup, 2, null);
        viewPagerAdapter.instantiateItem(viewPagerAdapter.viewGroup, 2);
        viewPagerAdapter.notifyDataSetChanged();
    }

    public static void restart_artist(String folder_name, String op) {
        viewPagerAdapter.op_artists = op;
        viewPagerAdapter.folder_name = folder_name;
        viewPagerAdapter.destroyItem(viewPagerAdapter.viewGroup, 3, null);
        viewPagerAdapter.instantiateItem(viewPagerAdapter.viewGroup, 3);
        viewPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
