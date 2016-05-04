package com.horaapps.leafpic;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.horaapps.leafpic.Adapters.AlbumsAdapter;
import com.horaapps.leafpic.Adapters.PhotosAdapter;
import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.CustomAlbumsHandler;
import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.Base.MediaFolders;
import com.horaapps.leafpic.Base.MediaStoreHandler;
import com.horaapps.leafpic.Views.GridSpacingItemDecoration;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.Measure;
import com.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ThemedActivity {

    public static String TAG = "AlbumsAct";

    HandlingAlbums albums;// = new HandlingAlbums(MainActivity.this);
    CustomAlbumsHandler customAlbumsHandler = new CustomAlbumsHandler(MainActivity.this);
    Album album = new Album(MainActivity.this);
    MediaFolders folders;

    RecyclerView mRecyclerView;
    AlbumsAdapter adapt;
    PhotosAdapter adapter;
    FloatingActionButton fabCamera;
    DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    private SwipeRefreshLayout SwipeContainerRV;
    boolean pickmode = false;
    boolean editmode = false, albumsMode = true, contentReady = false, firstLaunch = true;

    GridSpacingItemDecoration albumsDecoration;
    GridSpacingItemDecoration photosDecoration;

    View.OnLongClickListener photosOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView is = (TextView) v.findViewById(R.id.photo_path);

            if (!editmode) {
                // If it is the first long press
                adapter.notifyItemChanged(album.toggleSelectPhoto(Integer.parseInt(is.getTag().toString())));
                editmode = true;
            } else {
                album.selectAllPhotosUpTo(Integer.parseInt(is.getTag().toString()), adapter);
            }
            invalidateOptionsMenu();
            return true;
        }
    };

    View.OnClickListener photosOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (contentReady) {
                TextView is = (TextView) v.findViewById(R.id.photo_path);
                if (editmode) {
                    adapter.notifyItemChanged(album.toggleSelectPhoto(Integer.parseInt(is.getTag().toString())));
                    invalidateOptionsMenu();
                } else {
                    //Log.wtf("index",)
                    album.setCurrentPhotoIndex(Integer.parseInt(is.getTag().toString()));
                    Intent intent = new Intent(MainActivity.this, PhotoPagerActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("album", album);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        }
    };

    private View.OnLongClickListener albumOnLongCLickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView a = (TextView) v.findViewById(R.id.album_name);
            adapt.notifyItemChanged(albums.toggleSelectAlbum(Integer.parseInt(a.getTag().toString())));
            editmode = true;
            invalidateOptionsMenu();
            return true;
        }
    };

    private View.OnClickListener albumOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (contentReady) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                if (editmode) {
                    adapt.notifyItemChanged(albums.toggleSelectAlbum(Integer.parseInt(a.getTag().toString())));
                    invalidateOptionsMenu();
                } else {
                    openAlbum(albums.getAlbum(Integer.parseInt(a.getTag().toString())));
                    setRecentApp(albums.getAlbum(Integer.parseInt(a.getTag().toString())).DisplayName);
                }
            }
        }
    };

    public class PrepareAlbumsIntenal extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: start");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //album.updatePhotos();
            folders.getMediaAlbums();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "onPostExecute: end");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        albums = new HandlingAlbums(MainActivity.this);
        folders=new MediaFolders();
        //new PrepareAlbumsIntenal().execute();
        /*try {
            Bundle data = getIntent().getExtras();
            albums = data.getParcelable("albums");
            assert albums != null;
            albums.setContext(MainActivity.this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }*/

        /**** SET UP UI ****/
        initUI();
        setupUI();
        displayPrefetchedData(getIntent().getExtras());
    }

    @Override
    public void onResume() {
        super.onResume();
        albums.clearSelectedAlbums();
        album.clearSelectedPhotos();
        setupUI();

        if (albumsMode) {
            if (!firstLaunch) new PrepareAlbumTask().execute();
        } else new PreparePhotosTask().execute();

        invalidateOptionsMenu();
        firstLaunch = false;
    }

    public void openAlbum(Album a) {
        openAlbum(a,true);
    }
    public void openAlbum(Album a, boolean reload) {
        album = a;
        toolbar.setTitle(a.DisplayName);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        mRecyclerView.removeItemDecoration(albumsDecoration);
        mRecyclerView.addItemDecoration(photosDecoration);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Measure.getPhotosColums(getApplicationContext())));
        album.setContext(MainActivity.this);

        adapter = new PhotosAdapter(album.medias, MainActivity.this);
        if (reload) new PreparePhotosTask().execute();

        adapter.setOnClickListener(photosOnClickListener);
        adapter.setOnLongClickListener(photosOnLongClickListener);
        mRecyclerView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlbums();
            }
        });
        albumsMode = editmode = false;
        invalidateOptionsMenu();
    }

    public void displayAlbums() {
        displayAlbums(true);
    }

    public void displayAlbums(boolean reload) {
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
        toolbar.setTitle(getString(R.string.app_name));

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Measure.getAlbumsColums(getApplicationContext())));
        mRecyclerView.removeItemDecoration(photosDecoration);
        mRecyclerView.addItemDecoration(albumsDecoration);

        adapt = new AlbumsAdapter(albums.dispAlbums, getApplicationContext());
        if (reload) new PrepareAlbumTask().execute();

        adapt.setOnClickListener(albumOnClickListener);
        adapt.setOnLongClickListener(albumOnLongCLickListener);
        mRecyclerView.setAdapter(adapt);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        albumsMode = true;
        editmode = false;
        invalidateOptionsMenu();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int nSpan;
        if (albumsMode) {
            nSpan = Measure.getAlbumsColums(MainActivity.this);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, nSpan));
            mRecyclerView.removeItemDecoration(albumsDecoration);
            albumsDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
            mRecyclerView.addItemDecoration(albumsDecoration);
        } else {
            nSpan = Measure.getPhotosColums(MainActivity.this);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, nSpan));
            mRecyclerView.removeItemDecoration(photosDecoration);
            photosDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
            mRecyclerView.addItemDecoration(photosDecoration);
        }

        int status_height = Measure.getStatusBarHeight(getResources());

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            mRecyclerView.setPadding(0, 0, 0, status_height);
            fabCamera.setVisibility(View.GONE);
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            toolbar.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();

            SwipeContainerRV.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();
            mRecyclerView.setPadding(0, 0, 0, status_height + Measure.getNavBarHeight(MainActivity.this));
            fabCamera.animate().translationY(fabCamera.getHeight()*2).start();
            fabCamera.setVisibility(View.VISIBLE);
        }
    }

    public void displayPrefetchedData(Bundle data){

        try {
            int content = data.getInt(SplashScreen.CONTENT);
            if (content == SplashScreen.ALBUMS_PREFETCHED) {
                albums = data.getParcelable("albums");
                assert albums != null;
                albums.setContext(MainActivity.this);
                displayAlbums(false);
                pickmode=data.getBoolean(SplashScreen.PICK_MODE);
            } else if (content == SplashScreen.PHOTS_PREFETCHED) {
                album = data.getParcelable("album");
                assert album != null;
                album.setContext(MainActivity.this);
                openAlbum(album,false);
            }
            contentReady = true;
        } catch (NullPointerException e) { e.printStackTrace(); }
    }

    public void initUI() {

        /**** TOOLBAR ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switch (getBasicTheme()){
            case 1: toolbar.setPopupTheme(R.style.LightActionBarMenu);break;
            //case 2: toolbar.setPopupTheme(R.style.AmoledDarkActionBarMenu);break;
            //case 3: toolbar.setPopupTheme(R.style.AmoledDarkActionBarMenu);break;
            //default: toolbar.setPopupTheme(R.style.LightActionBarMenu);break;
        }
        //toolbar.setPopupTheme(R.style.LightActionBarMenu);
        //TODO:FIX IT PLIS CUZ I KNOW U CAN
        /*else
            toolbar.setPopupTheme(R.style.DarkActionBarMenu);*/
        /**** RECYCLER VIEW ****/
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        albumsDecoration = new GridSpacingItemDecoration(Measure.getAlbumsColums(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);
        photosDecoration = new GridSpacingItemDecoration(Measure.getPhotosColums(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);


        /**** SWIPE TO REFRESH ****/
        SwipeContainerRV = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        SwipeContainerRV.setColorSchemeResources(R.color.accent_blue);
        SwipeContainerRV.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
            }
        });


        /**** DRAWER ****/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                //Put your code here
                // materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            }

            public void onDrawerOpened(View drawerView) {
                //Put your code here
                //materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
            }
        });

        TextView logo = (TextView) findViewById(R.id.txtLogo);
        logo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Figa.ttf"));

        /**** FAB ***/
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fabCamera.animate().translationY(-Measure.getNavBarHeight(MainActivity.this)).setInterpolator(new DecelerateInterpolator(2)).start();
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!albumsMode && album.areFiltersActive()) {
                    album.filterMedias(Album.FILTER_ALL);
                    adapter.updateDataset(album.medias);
                    toolbar.getMenu().findItem(R.id.all_media_filter).setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(MainActivity.this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                } else startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
            }
        });

        int status_height = Measure.getStatusBarHeight(getResources());
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        toolbar.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();

        SwipeContainerRV.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();
        mRecyclerView.setPadding(0, 0, 0, status_height + Measure.getNavBarHeight(MainActivity.this));

        setRecentApp(getString(R.string.app_name));
    }

    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isNavigationBarColored())
                super.setNavBarColor();
            else
                getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 110));
            //getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), 110));
        }
    }

    //region UI/GRAPHIC
    public void setupUI() {

        //toolbar.setPopupTheme(isDarkTheme() ? R.style.MyDarkToolbarStyle : R.style.MyLightToolbarStyle);
        //setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(getPrimaryColor());

        //TODO:FIX IT PLIS CUZ I KNOW U CAN
        /*else
            toolbar.setPopupTheme(R.style.DarkActionBarMenu);*/

        setStatusBarColor();
        setNavBarColor();

        fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        setDrawerTheme();
        mRecyclerView.setBackgroundColor(getBackgroundColor());
    }

    public void setDrawerTheme() {
        RelativeLayout DrawerHeader = (RelativeLayout) findViewById(R.id.Drawer_Header);
        DrawerHeader.setBackgroundColor(getPrimaryColor());

        LinearLayout DrawerBody = (LinearLayout) findViewById(R.id.Drawer_Body);
        DrawerBody.setBackgroundColor(getDrawerBackground());//getBackgroundColor()

        ScrollView DrawerScroll = (ScrollView) findViewById(R.id.Drawer_Body_Scroll);
        DrawerScroll.setBackgroundColor(getDrawerBackground());//getBackgroundColor()

        View DrawerDivider2 = findViewById(R.id.Drawer_Body_Divider);
        DrawerDivider2.setBackgroundColor(ColorPalette.getTransparentColor(
                ContextCompat.getColor(MainActivity.this, R.color.md_black_1000), 150));

        /** drawer items **/
        TextView txtDD = (TextView) findViewById(R.id.Drawer_Default_Item);
        TextView txtDH = (TextView) findViewById(R.id.Drawer_Tags_Item);
        TextView txtDMoments = (TextView) findViewById(R.id.Drawer_Moments_Item);
        TextView txtDS = (TextView) findViewById(R.id.Drawer_Setting_Item);
        TextView txtDDonate = (TextView) findViewById(R.id.Drawer_Donate_Item);
        TextView txtWall = (TextView) findViewById(R.id.Drawer_wallpapers_Item);
        TextView txtAbout = (TextView) findViewById(R.id.Drawer_About_Item);

        IconicsImageView imgDD = (IconicsImageView) findViewById(R.id.Drawer_Default_Icon);
        IconicsImageView imgWall = (IconicsImageView) findViewById(R.id.Drawer_wallpapers_Icon);
        IconicsImageView imgDH = (IconicsImageView) findViewById(R.id.Drawer_Tags_Icon);
        IconicsImageView imgDMoments = (IconicsImageView) findViewById(R.id.Drawer_Moments_Icon);
        IconicsImageView imgDDonate = (IconicsImageView) findViewById(R.id.Drawer_Donate_Icon);
        IconicsImageView imgDS = (IconicsImageView) findViewById(R.id.Drawer_Setting_Icon);
        IconicsImageView imgAbout = (IconicsImageView) findViewById(R.id.Drawer_About_Icon);

        /**textViews Colors*/
        int color = getTextColor();
        txtDD.setTextColor(color);
        txtDH.setTextColor(color);
        txtDMoments.setTextColor(color);
        txtDS.setTextColor(color);
        txtDDonate.setTextColor(color);
        txtWall.setTextColor(color);
        txtAbout.setTextColor(color);

        color = getIconColor();

        imgDD.setColor(color);
        imgDDonate.setColor(color);
        imgDH.setColor(color);
        imgDMoments.setColor(color);
        imgDS.setColor(color);
        imgWall.setColor(color);
        imgAbout.setColor(color);

        /****DRAWER CLICK LISTENER****/
        findViewById(R.id.ll_drawer_Setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ll_drawer_About).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.ll_drawer_Default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        findViewById(R.id.ll_drawer_Moments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CominqSoonDialog("Moments");
            }
        });

        findViewById(R.id.ll_drawer_Tags).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CominqSoonDialog("Tags");
            }
        });
        findViewById(R.id.ll_drawer_Wallpapers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CominqSoonDialog("Wallpapers");
            }
        });
    }
    //endregion

    void CominqSoonDialog(String what) {
        final AlertDialog.Builder CoomingSoonDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

        final View Exclude_dialogLayout = getLayoutInflater().inflate(R.layout.text_dialog, null);
        final TextView txt_Exclude_title = (TextView) Exclude_dialogLayout.findViewById(R.id.text_dialog_title);
        final TextView txt_Exclude_message = (TextView) Exclude_dialogLayout.findViewById(R.id.text_dialog_message);
        CardView cv_Exclude_Dialog = (CardView) Exclude_dialogLayout.findViewById(R.id.message_card);

        cv_Exclude_Dialog.setCardBackgroundColor(getCardBackgroundColor());
        txt_Exclude_title.setBackgroundColor(getPrimaryColor());
        txt_Exclude_title.setText(what);
        txt_Exclude_message.setText("Coming Soon!");
        txt_Exclude_message.setTextColor(getTextColor());
        CoomingSoonDialog.setView(Exclude_dialogLayout);

        CoomingSoonDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        CoomingSoonDialog.show();
    }

    void updateSelectedStuff() {
        int c;
        try {
            if (albumsMode) {
                if ((c = albums.getSelectedCount()) != 0) {
                    toolbar.setTitle(c + "/" + albums.dispAlbums.size());
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_check));
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editmode = false;
                            albums.clearSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (albums.getSelectedCount() == albums.dispAlbums.size())
                                albums.clearSelectedAlbums();
                            else albums.selectAllAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                } else {
                    toolbar.setTitle(getString(R.string.app_name));
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
                    toolbar.setOnClickListener(null);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.openDrawer(GravityCompat.START);
                        }
                    });
                }
            } else {
                if ((c = album.getSelectedCount()) != 0) {
                    toolbar.setTitle(c + "/" + album.medias.size());
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_check));
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishEditMode();
                        }
                    });
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (album.getSelectedCount() == album.medias.size())
                                album.clearSelectedPhotos();
                            else album.selectAllPhotos();
                            adapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                } else {
                    toolbar.setTitle(album.DisplayName);
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
                    toolbar.setOnClickListener(null);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            displayAlbums();
                        }
                    });
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void finishEditMode() {
        editmode = false;
        invalidateOptionsMenu();
        album.clearSelectedPhotos();
        adapter.notifyDataSetChanged();
    }

    public void checkNothing() {
        TextView a = (TextView) findViewById(R.id.nothing_to_show);
        a.setTextColor(getTextColor());
        a.setVisibility((albumsMode && albums.dispAlbums.size() == 0) || (!albumsMode && album.medias.size() == 0) ? View.VISIBLE : View.GONE);
    }

    //region MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_albums, menu);

        if (albumsMode) {
            menu.findItem(R.id.select_all).setTitle(
                    getString(albums.getSelectedCount() == adapt.getItemCount()
                            ? R.string.clear_selected
                            : R.string.select_all));
            menu.findItem(R.id.ascending_sort_action).setChecked(albums.isAscending());
            String column = albums.getColumnSortingMode();
            if (column.equals(MediaStore.Images.ImageColumns.DATE_TAKEN))
                menu.findItem(R.id.date_taken_sort_action).setChecked(true);
            else if (column.equals(MediaStore.Images.ImageColumns.DATA))
                menu.findItem(R.id.name_sort_action).setChecked(true);
            /*else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.SIZE))
                menu.findItem(R.id.size_sort_action).setChecked(true);*/
        } else {
            menu.findItem(R.id.select_all).setTitle(getString(
                    album.getSelectedCount() == adapter.getItemCount()
                            ? R.string.clear_selected
                            : R.string.select_all));

            menu.findItem(R.id.ascending_sort_action).setChecked(album.settings.ascending);
            if (album.settings.columnSortingMode == null || album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DATE_TAKEN))
                menu.findItem(R.id.date_taken_sort_action).setChecked(true);
            else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                menu.findItem(R.id.name_sort_action).setChecked(true);
            else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.SIZE))
                menu.findItem(R.id.size_sort_action).setChecked(true);
        }

        menu.findItem(R.id.search_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_search));
        menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        menu.findItem(R.id.sort_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_sort));
        menu.findItem(R.id.filter_menu).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_filter_list));
        menu.findItem(R.id.sharePhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));

        final MenuItem searchItem = menu.findItem(R.id.search_action);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Coming soon!");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (albumsMode) {
            editmode = albums.getSelectedCount() != 0;
            menu.setGroupVisible(R.id.album_options_menu, editmode);
            menu.setGroupVisible(R.id.photos_option_men, false);
        } else {
            editmode = album.getSelectedCount() != 0;
            menu.setGroupVisible(R.id.photos_option_men, editmode);
            menu.setGroupVisible(R.id.album_options_menu, !editmode);
        }

        togglePrimaryToolbarOptions(menu);
        updateSelectedStuff();

        /** custom items **/
        menu.findItem(R.id.select_all).setVisible(editmode);
        menu.findItem(R.id.installShortcut).setVisible(albumsMode && editmode);
        menu.findItem(R.id.delete_action).setVisible((albumsMode && editmode) || (!albumsMode));
        menu.findItem(R.id.setAsAlbumPreview).setVisible(!albumsMode && album.getSelectedCount() == 1);
        menu.findItem(R.id.clear_album_preview).setVisible(!albumsMode && album.hasCustomCover());
        menu.findItem(R.id.renameAlbum).setVisible((albumsMode && albums.getSelectedCount() == 1) || (!albumsMode && !editmode));
        //TODO: WILL BE IMPLEMENTED
        //menu.findItem(R.id.affixPhoto).setVisible(!albumsMode && album.getSelectedCount() >= 2 && album.getSelectedCount() <= 5);

        return super.onPrepareOptionsMenu(menu);
    }

    private void togglePrimaryToolbarOptions(final Menu menu) {
        menu.setGroupVisible(R.id.general_action, !editmode);

        if (!editmode) {
            menu.findItem(R.id.size_sort_action).setVisible(!albumsMode);
            menu.findItem(R.id.filter_menu).setVisible(!albumsMode);
            menu.findItem(R.id.search_action).setVisible(albumsMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.copyAction:
                final CopyMove_BottomSheet bottomSheetDialogFragment = new CopyMove_BottomSheet();
                bottomSheetDialogFragment.setAlbumArrayList(albums.dispAlbums);
                bottomSheetDialogFragment.setTitle(getString(R.string.copy_to));
                bottomSheetDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = Integer.parseInt(v.findViewById(R.id.Bottom_Sheet_Title_Item).getTag().toString());
                        album.copySelectedPhotos(albums.getAlbum(index).Path);
                        finishEditMode();
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                break;

            case R.id.select_all:
                if (albumsMode) {
                    if (albums.getSelectedCount() == adapt.getItemCount()) {
                        editmode = false;
                        albums.clearSelectedAlbums();
                    } else albums.selectAllAlbums();
                    adapt.notifyDataSetChanged();
                } else {
                    if (album.getSelectedCount() == adapter.getItemCount()) {
                        editmode = false;
                        album.clearSelectedPhotos();
                    } else album.selectAllPhotos();
                    adapter.notifyDataSetChanged();
                }
                invalidateOptionsMenu();
                break;

            case R.id.name_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATA);
                    new PrepareAlbumTask().execute();
                } else {
                    album.setDefaultSortingMode(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                    new PreparePhotosTask().execute();
                }
                item.setChecked(true);
                break;
            case R.id.date_taken_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);
                    new PrepareAlbumTask().execute();
                } else {
                    album.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);
                    new PreparePhotosTask().execute();
                }
                item.setChecked(true);
                break;

            case R.id.size_sort_action:
                if (!albumsMode) {
                    album.setDefaultSortingMode(MediaStore.Images.ImageColumns.SIZE);
                    new PreparePhotosTask().execute();
                    item.setChecked(true);
                }
                break;

            case R.id.ascending_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingAscending(!item.isChecked());
                    new PrepareAlbumTask().execute();
                } else {
                    album.setDefaultSortingAscending(!album.settings.ascending);
                    new PreparePhotosTask().execute();
                }
                item.setChecked(!item.isChecked());
                break;

            case R.id.all_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_ALL);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                }
                break;
            case R.id.video_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_VIDEO);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                break;
            case R.id.image_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_IMAGE);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                break;

            case R.id.gifs_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_GIF);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                break;

            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;

            case R.id.sharePhotos:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sent_to_action));

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (Media f : album.selectedMedias)
                    files.add(f.getUri());

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setType(StringUtils.getGenericMIME(album.selectedMedias.get(0).MIME));
                finishEditMode();
                startActivity(intent);
                break;
            case R.id.installShortcut:
                albums.InstallShortcutForSelectedAlbums(this.getApplicationContext());
                albums.clearSelectedAlbums();
                adapt.notifyDataSetChanged();
                invalidateOptionsMenu();
                break;
            //TODO: WILL BE IMPLEMENTED
            /*
            case  R.id.affixPhoto:
                final AlertDialog.Builder AffixDialog = new AlertDialog.Builder(
                        MainActivity.this,
                        isDarkTheme()
                                ? R.style.AlertDialog_Dark
                                : R.style.AlertDialog_Light);

                final View Affix_dialogLayout = getLayoutInflater().inflate(R.layout.affix_dialog, null);
                final TextView txt_Affix_title = (TextView) Affix_dialogLayout.findViewById(R.id.affix_title);
                txt_Affix_title.setBackgroundColor(getPrimaryColor());
                CardView cv_Affix_Dialog = (CardView) Affix_dialogLayout.findViewById(R.id.affix_card);
                cv_Affix_Dialog.setCardBackgroundColor(getCardBackgroundColor());

                //ITEMS
                final TextView txt_Affix_Vertical_title = (TextView) Affix_dialogLayout.findViewById(R.id.affix_vertical_title);
                final TextView txt_Affix_Vertical_sub = (TextView) Affix_dialogLayout.findViewById(R.id.affix_vertical_sub);
                final SwitchCompat swVertical = (SwitchCompat) Affix_dialogLayout.findViewById(R.id.affix_vertical_switch);
                final IconicsImageView imgAffix = (IconicsImageView) Affix_dialogLayout.findViewById(R.id.affix_vertical_icon);

                txt_Affix_Vertical_title .setTextColor(getTextColor());
                txt_Affix_Vertical_sub .setTextColor(getSubTextColor());
                imgAffix.setColor(getIconColor());

                //SWITCH
                swVertical.setChecked(false);
                updateSwitchColor(swVertical,getAccentColor());

                swVertical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        updateSwitchColor(swVertical, getAccentColor());
                    }
                });

                AffixDialog.setView(Affix_dialogLayout);
                AffixDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO:COMING SOON
                    }
                });
                AffixDialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AffixDialog.show();
                break;
                */

            case R.id.excludeAlbumButton:

                final AlertDialog.Builder ExcludeDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

                final View Exclude_dialogLayout = getLayoutInflater().inflate(R.layout.text_dialog, null);
                final TextView txt_Exclude_title = (TextView) Exclude_dialogLayout.findViewById(R.id.text_dialog_title);
                final TextView txt_Exclude_message = (TextView) Exclude_dialogLayout.findViewById(R.id.text_dialog_message);
                CardView cv_Exclude_Dialog = (CardView) Exclude_dialogLayout.findViewById(R.id.message_card);
                final LinearLayout ll_Exclude_Sub = (LinearLayout) Exclude_dialogLayout.findViewById(R.id.ll_checkbox_dialog);
                final TextView txt_Exclude_Submessage = (TextView) Exclude_dialogLayout.findViewById(R.id.checkbox_text_dialog);
                final CheckBox ckb_Exlude_sub = (CheckBox) Exclude_dialogLayout.findViewById(R.id.checkbox_text_dialog_cb);


                cv_Exclude_Dialog.setCardBackgroundColor(getCardBackgroundColor());
                txt_Exclude_title.setBackgroundColor(getPrimaryColor());
                txt_Exclude_title.setText(getString(R.string.exclude));
                txt_Exclude_message.setText(R.string.exclude_album_message);
                txt_Exclude_message.setTextColor(getTextColor());

                ll_Exclude_Sub.setVisibility(View.VISIBLE);
                txt_Exclude_Submessage.setText(R.string.sub_exclude_album_message);
                txt_Exclude_Submessage.setTextColor(getSubTextColor());

                ExcludeDialog.setView(Exclude_dialogLayout);

                ExcludeDialog.setPositiveButton(this.getString(R.string.exclude), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //if (ckb_Exlude_sub.isChecked()){ }
                        if (albumsMode) {
                            albums.excludeSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        } else {
                            customAlbumsHandler.excludeAlbum(album.ID);
                            displayAlbums();
                        }
                    }
                });
                ExcludeDialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                ExcludeDialog.show();
                break;

            case R.id.hideAlbumButton:

                final AlertDialog.Builder HideDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                final View Hide_dialogLayout = getLayoutInflater().inflate(R.layout.text_dialog, null);
                final TextView txt_Hide_title = (TextView) Hide_dialogLayout.findViewById(R.id.text_dialog_title);
                final TextView txt_Hide_message = (TextView) Hide_dialogLayout.findViewById(R.id.text_dialog_message);
                CardView cv_Hide_Dialog = (CardView) Hide_dialogLayout.findViewById(R.id.message_card);

                cv_Hide_Dialog.setCardBackgroundColor(getCardBackgroundColor());
                txt_Hide_title.setBackgroundColor(getPrimaryColor());
                txt_Hide_title.setText(getString(R.string.hide));
                txt_Hide_message.setText(R.string.hide_album_message);
                txt_Hide_message.setTextColor(getTextColor());
                HideDialog.setView(Hide_dialogLayout);
                //BUTTONS
                HideDialog.setPositiveButton(this.getString(R.string.hide), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (albumsMode) {
                            albums.hideSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        } else {
                            albums.hideAlbum(album.Path);
                            displayAlbums();
                        }
                    }
                });
                HideDialog.setNeutralButton(this.getString(R.string.exclude), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (albumsMode) {
                            albums.excludeSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        } else {
                            customAlbumsHandler.excludeAlbum(album.ID);
                            displayAlbums();
                        }
                    }
                });
                HideDialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                HideDialog.show();
                break;

            case R.id.delete_action:
                class DeletePhotos extends AsyncTask<String, Integer, Void> {
                    @Override
                    protected void onPreExecute() {
                        SwipeContainerRV.setRefreshing(true);
                        contentReady = false;
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(String... arg0) {
                        if (!albumsMode) {
                            if (editmode) {
                                ArrayList<Media> selected = album.getSelectedMedias();
                                for (int i = 0; i < selected.size(); i++) {
                                    getContentResolver().delete(selected.get(i).getUri(), null, null);
                                    album.medias.remove(selected.get(i));
                                }
                                album.clearSelectedPhotos();
                            } else {
                                MediaStoreHandler.deleteAlbumMedia(album, MainActivity.this);
                                album.medias.clear();
                            }
                        } else
                            albums.deleteSelectedAlbums(MainActivity.this);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        if (!albumsMode) {
                            if (album.medias.size() == 0)
                                displayAlbums();
                            else
                                adapter.updateDataset(album.medias);
                        } else
                            adapt.notifyDataSetChanged();

                        contentReady = true;
                        invalidateOptionsMenu();
                        SwipeContainerRV.setRefreshing(false);
                    }
                }

                final AlertDialog.Builder DeleteDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

                final View Delete_dialogLayout = getLayoutInflater().inflate(R.layout.text_dialog, null);
                final TextView txt_Delete_title = (TextView) Delete_dialogLayout.findViewById(R.id.text_dialog_title);
                final TextView txt_Delete_message = (TextView) Delete_dialogLayout.findViewById(R.id.text_dialog_message);
                CardView cv_Delete_Dialog = (CardView) Delete_dialogLayout.findViewById(R.id.message_card);

                cv_Delete_Dialog.setCardBackgroundColor(getCardBackgroundColor());
                txt_Delete_title.setBackgroundColor(getPrimaryColor());
                txt_Delete_title.setText(getString(R.string.delete));
                txt_Delete_message.setText(albumsMode || (!albumsMode && !editmode) ? R.string.delete_album_message : R.string.delete_photos_message);
                txt_Delete_message.setTextColor(getTextColor());
                DeleteDialog.setView(Delete_dialogLayout);

                DeleteDialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                DeleteDialog.setPositiveButton(this.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeletePhotos().execute();
                    }
                });
                DeleteDialog.show();
                break;

            case R.id.moveAction:
                class MovePhotos extends AsyncTask<String, Void, Void> {

                    @Override
                    protected void onPreExecute() {
                        SwipeContainerRV.setRefreshing(true);
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(String... arg0) {
                        try {
                            for (int i = 0; i < album.selectedMedias.size(); i++) {
                                final Uri asd = album.selectedMedias.get(i).getUri();
                                File from = new File(album.selectedMedias.get(i).Path);
                                File to = new File(StringUtils.getPhotoPathMoved(album.selectedMedias.get(i).Path, arg0[0]));

                                if (from.renameTo(to)) {
                                    MediaScannerConnection.scanFile(
                                            getApplicationContext(),
                                            new String[]{to.getAbsolutePath()}, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                @Override
                                                public void onScanCompleted(String path, Uri uri) {
                                                    getContentResolver().delete(asd, null, null);
                                                }
                                            });
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void result) {
                        finishEditMode();
                        invalidateOptionsMenu();
                        SwipeContainerRV.setRefreshing(false);
                    }
                }

                final CopyMove_BottomSheet sheetMove = new CopyMove_BottomSheet();
                sheetMove.setAlbumArrayList(albums.dispAlbums);
                sheetMove.setTitle(getString(R.string.move_to));
                sheetMove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = Integer.parseInt(v.findViewById(R.id.Bottom_Sheet_Title_Item).getTag().toString());
                        new MovePhotos().execute(albums.getAlbum(index).Path);
                        sheetMove.dismiss();
                    }
                });
                sheetMove.show(getSupportFragmentManager(), sheetMove.getTag());
                break;

            case R.id.renameAlbum:
                class ReanameAlbum extends AsyncTask<String, Integer, Integer> {

                    @Override
                    protected void onPreExecute() {
                        SwipeContainerRV.setRefreshing(true);
                        super.onPreExecute();
                    }

                    @Override
                    protected Integer doInBackground(String... arg0) {
                        int res = -1;
                        try {
                            if (albumsMode) {
                                album = albums.getSelectedAlbum(0);
                                res = albums.getIndex(album);
                                album.setContext(MainActivity.this);
                                album.updatePhotos();
                            }

                            File dir = new File(StringUtils.getAlbumPathRenamed(album.Path, arg0[0]));
                            if (dir.mkdir()) {
                                album.Path = dir.getAbsolutePath();
                                album.DisplayName = arg0[0];
                                for (int i = 0; i < album.medias.size(); i++) {
                                    final int asd = i;
                                    File from = new File(album.medias.get(i).Path);
                                    File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(album.medias.get(i).Path, arg0[0]));

                                    if (from.renameTo(to)) {
                                        MediaScannerConnection.scanFile(
                                                getApplicationContext(),
                                                new String[]{to.getAbsolutePath()}, null,
                                                new MediaScannerConnection.OnScanCompletedListener() {
                                                    @Override
                                                    public void onScanCompleted(String path, Uri uri) {
                                                        getContentResolver().delete(album.medias.get(asd).getUri(), null, null);
                                                        album.medias.get(asd).ID = StringUtils.getID(uri + "");
                                                        album.medias.get(asd).Path = path;
                                                        if (asd == 0) {
                                                            MediaStoreHandler h = new MediaStoreHandler(MainActivity.this);
                                                            album.ID = h.getAlbumPhoto(path);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return res;
                    }


                    @Override
                    protected void onPostExecute(Integer result) {
                        if (albumsMode) {
                            if (result != -1) {
                                albums.replaceAlbum(result, album);
                                adapt.notifyItemChanged(result);
                            }

                            albums.clearSelectedAlbums();
                            adapt.notifyDataSetChanged();
                        } else {
                            toolbar.setTitle(album.DisplayName);
                            adapter.notifyDataSetChanged();
                        }
                        invalidateOptionsMenu();
                        SwipeContainerRV.setRefreshing(false);
                    }
                }

                final AlertDialog.Builder RenameDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

                final View Rename_dialogLayout = getLayoutInflater().inflate(R.layout.rename_dialog, null);
                final TextView title = (TextView) Rename_dialogLayout.findViewById(R.id.rename_title);
                final EditText txt_edit = (EditText) Rename_dialogLayout.findViewById(R.id.dialog_txt);
                CardView cv_Rename_Dialog = (CardView) Rename_dialogLayout.findViewById(R.id.rename_card);

                cv_Rename_Dialog.setCardBackgroundColor(getCardBackgroundColor());
                title.setBackgroundColor(getPrimaryColor());
                title.setText(getString(R.string.rename_album));
                txt_edit.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
                txt_edit.setTextColor(getTextColor());
                txt_edit.setText(albumsMode ? albums.getSelectedAlbum(0).DisplayName : album.DisplayName);
                RenameDialog.setView(Rename_dialogLayout);

                RenameDialog.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                RenameDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (txt_edit.length() != 0) {

                            new ReanameAlbum().execute(txt_edit.getText().toString());
                            //onResume();

                        } else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.nothing_changed));

                    }
                });
                RenameDialog.show();
                txt_edit.requestFocus();
                break;

            case R.id.clear_album_preview:
                if (!albumsMode) {
                    CustomAlbumsHandler as = new CustomAlbumsHandler(getApplicationContext());
                    as.clearAlbumPreview(album.ID);
                    album.setSettings();
                }
                break;

            case R.id.setAsAlbumPreview:
                if (!albumsMode) {
                    album.setSelectedPhotoAsPreview();
                    finishEditMode();
                }
                break;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (albumsMode) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawer(GravityCompat.START);
            else finish();
        } else {
            displayAlbums();
            setRecentApp(getString(R.string.app_name));
        }

    }

    public void scanFile(String[] path) {
        MediaScannerConnection.scanFile(getApplicationContext(), path, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                System.out.println("Photo rename COMPLETED: " + path);
            }
        });
    }

    public class PrepareAlbumTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            contentReady = false;
            SwipeContainerRV.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            albums.loadPreviewAlbums();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapt.updateDataset(albums.dispAlbums);
            contentReady = true;
            checkNothing();
            SwipeContainerRV.setRefreshing(false);
        }
    }

    public class PreparePhotosTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            contentReady = false;
            SwipeContainerRV.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            album.updatePhotos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.updateDataset(album.medias);
            contentReady = true;
            checkNothing();
            SwipeContainerRV.setRefreshing(false);
        }
    }
}
