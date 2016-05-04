package com.horaapps.leafpic.Base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.horaapps.leafpic.SplashScreen;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 27/04/16.
 */
public class HandlingAlbums {

    public final static String TAG = "HandlingAlbums";

    public ArrayList<Album> dispAlbums;
    private ArrayList<Album> selectedAlbums;

    CustomAlbumsHandler customAlbumsHandler;
    private SharedPreferences SP;

    ArrayList<File> excludedfolders;
    AlbumsComparators albumsComparators;

    public HandlingAlbums(Context context) {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);
        customAlbumsHandler = new CustomAlbumsHandler(context);

        excludedfolders = new ArrayList<File>();
        loadExcludedFolders(context);
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    public void loadPreviewAlbums(boolean hidden) {
        dispAlbums = new ArrayList<Album>();
        if (hidden)
            for (File storage : listStorages())
                fetchRecursivelyHiddenFolder(storage);
        else
            for (File storage : listStorages())
                fetchRecursivelyFolder(storage);

        sortAlbums();
    }

    public ArrayList<Album> getValidFolders(boolean hidden) {
        ArrayList<Album> folders = new ArrayList<Album>();
        if (hidden)
            for (File storage : listStorages())
                fetchRecursivelyHiddenFolder(storage, folders);
        else
            for (File storage : listStorages())
                fetchRecursivelyFolder(storage, folders);

        return folders;
    }

    public ArrayList<File> listStorages() {
        ArrayList<File> roots = new ArrayList<File>();
        roots.add(Environment.getExternalStorageDirectory());
        String sdCard = System.getenv("SECONDARY_STORAGE");
        if (sdCard != null) roots.add(new File(sdCard));
        return roots;
    }

    private void fetchRecursivelyFolder(File dir, ArrayList<Album> folders) {
        if (!excludedfolders.contains(dir)) {
            checkAndAddAlbum(dir);
            File[] children = dir.listFiles(new FoldersFileFilter());
            for (File temp : children) {
                File nomedia = new File(temp, ".nomedia");
                if (!excludedfolders.contains(temp) && !temp.isHidden() && !nomedia.exists()) {
                    File[] files = temp.listFiles(new ImageFileFilter());
                    if (files.length > 0)
                        folders.add(new Album(temp.getAbsolutePath(), temp.getName(), files.length));
                    fetchRecursivelyFolder(temp, folders);
                }
            }
        }
    }

    private void fetchRecursivelyHiddenFolder(File dir, ArrayList<Album> folders) {
        if (!excludedfolders.contains(dir)) {
            File[] asdf = dir.listFiles(new FoldersFileFilter());
            for (File temp : asdf) {
                File nomedia = new File(temp, ".nomedia");
                if (!excludedfolders.contains(temp) && nomedia.exists()) {
                    File[] files = temp.listFiles(new ImageFileFilter());
                    if (files.length > 0)
                        folders.add(new Album(temp.getAbsolutePath(), temp.getName(), files.length));
                }
                fetchRecursivelyHiddenFolder(temp, folders);
            }
        }
    }

    private void fetchRecursivelyFolder(File dir) {
        if (!excludedfolders.contains(dir)) {
            checkAndAddAlbum(dir);
            File[] children = dir.listFiles(new FoldersFileFilter());
            for (File temp : children) {
                File nomedia = new File(temp, ".nomedia");
                if (!excludedfolders.contains(temp) && !temp.isHidden() && !nomedia.exists()) {
                    //not excluded/hidden folder
                    fetchRecursivelyFolder(temp);
                }
            }
        }
    }

    private void fetchRecursivelyHiddenFolder(File dir) {
        if (!excludedfolders.contains(dir)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            for (File temp : folders) {
                File nomedia = new File(temp, ".nomedia");
                if (!excludedfolders.contains(temp) && nomedia.exists()) {
                    checkAndAddAlbum(temp);
                }
                fetchRecursivelyHiddenFolder(temp);
            }
        }
    }

    public void checkAndAddAlbum(File temp) {
        File[] files = temp.listFiles(new ImageFileFilter());
        if (files.length > 0) {
            //valid folder
            Album asd = new Album(temp.getAbsolutePath(), temp.getName(), files.length);
            asd.setCoverPath(customAlbumsHandler.getPhotPrevieAlbum(asd.getPath()));

            long lastMod = Long.MIN_VALUE;
            File choice = null;
            for (File file : files) {
                if (file.lastModified() > lastMod) {
                    choice = file;
                    lastMod = file.lastModified();
                }
            }
            if (choice != null)
                asd.media.add(0, new Media(choice.getAbsolutePath(), choice.lastModified()));

            dispAlbums.add(asd);
        }
    }



    public void loadExcludedFolders(Context context) {
        excludedfolders = new ArrayList<File>();
        //forced excluded folder
        excludedfolders.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android"));
        CustomAlbumsHandler handler = new CustomAlbumsHandler(context);
        excludedfolders.addAll(handler.getExcludedFolders());
    }

    public int toggleSelectAlbum(int index) {
        if (dispAlbums.get(index) != null) {
            dispAlbums.get(index).setSelected(!dispAlbums.get(index).isSelected());
            if (dispAlbums.get(index).isSelected()) selectedAlbums.add(dispAlbums.get(index));
            else selectedAlbums.remove(dispAlbums.get(index));
        }
        return index;
    }

    public Album getAlbum(int index){ return dispAlbums.get(index); }

    public void selectAllAlbums() {
        for (Album dispAlbum : dispAlbums)
            if (!dispAlbum.isSelected()) {
                dispAlbum.setSelected(true);
                selectedAlbums.add(dispAlbum);
            }
    }

    public int getSelectedCount() {
        return selectedAlbums.size();
    }

    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums)
            dispAlbum.setSelected(false);

        selectedAlbums.clear();
    }

    public void InstallShortcutForSelectedAlbums(Context appCtx) {
        for (Album selectedAlbum : selectedAlbums) {

            Intent shortcutIntent;
            shortcutIntent = new Intent(appCtx, SplashScreen.class);
            shortcutIntent.setAction(SplashScreen.ACTION_OPEN_ALBUM);
            shortcutIntent.putExtra("albumPath", selectedAlbum.getPath());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedAlbum.getName());

            File image = new File(selectedAlbum.getCoverAlbum().getPath());
            Bitmap bitmap;

            String mime = StringUtils.getMimeType(image.getAbsolutePath());

            if(mime.startsWith("image")) {
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), new BitmapFactory.Options());
            } else if(mime.startsWith("video")) {
                bitmap = ThumbnailUtils.createVideoThumbnail(selectedAlbum.getCoverAlbum().getPath(),
                        MediaStore.Images.Thumbnails.MINI_KIND);
            } else return;
            bitmap = Bitmap.createScaledBitmap(getCropedBitmap(bitmap), 128, 128, false);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, addWhiteBorder(bitmap, 5));

            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            appCtx.sendBroadcast(addIntent);
        }
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    private Bitmap getCropedBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2, 0,
                    srcBmp.getHeight(), srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(srcBmp, 0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(), srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
    public void scanFile(Context context, String[] path) {   MediaScannerConnection.scanFile(context, path, null, null); }

    public void hideAlbum(String path, Context context) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (!file.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.flush();
                out.close();
                scanFile(context, new String[]{ file.getAbsolutePath() });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void hideSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            hideAlbum(selectedAlbum, context);
        clearSelectedAlbums();
    }

    public void hideAlbum(final Album a, Context context) {
        hideAlbum(a.getPath(), context);
        dispAlbums.remove(a);
    }

    public void unHideAlbum(String path, Context context) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (file.exists()) {
            if (file.delete())
                scanFile(context, new String[]{ file.getAbsolutePath() });
        }
    }
    public void unHideSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            unHideAlbum(selectedAlbum, context);
        clearSelectedAlbums();
    }

    public void unHideAlbum(final Album a, Context context) {
        unHideAlbum(a.getPath(), context);
        dispAlbums.remove(a);
    }

    public void deleteSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums) {
            int index = dispAlbums.indexOf(selectedAlbum);
            deleteAlbum(selectedAlbum, context);
            dispAlbums.remove(index);
        }
    }

    public void deleteAlbum(Album album, Context context) {
        File[] files = new File(album.getPath()).listFiles(new ImageFileFilter());
        for (File file : files) {
            if (file.delete()){
                scanFile(context, new String[]{ file.getAbsolutePath() });
            }
        }
    }



    public void excludeSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            excludeAlbum(context, selectedAlbum);

        clearSelectedAlbums();
    }

    public void excludeAlbum(Context context, Album a) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.excludeAlbum(a.getPath());
        dispAlbums.remove(a);
    }

    public int getColumnSortingMode() {
        return SP.getInt("column_sort", AlbumSettings.SORT_BY_DATE);
    }

    public boolean isAscending() {
        return SP.getBoolean("ascending_mode", false);
    }


    public void setDefaultSortingMode(int column) {
        SharedPreferences.Editor editor = SP.edit();
        editor.putInt("column_sort", column);
        editor.apply();
    }

    public void setDefaultSortingAscending(Boolean ascending) {
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("ascending_mode", ascending);
        editor.apply();
    }

    public void sortAlbums() {
        albumsComparators = new AlbumsComparators(isAscending());
        switch (getColumnSortingMode()) {
            case AlbumSettings.SORT_BY_NAME:
                Collections.sort(dispAlbums, albumsComparators.getNameComparator());
                break;
            case AlbumSettings.SORT_BY_SIZE:
                Collections.sort(dispAlbums, albumsComparators.getSizeComparator());
                break;
            case AlbumSettings.SORT_BY_DATE:
            default:
                Collections.sort(dispAlbums, albumsComparators.getDateComparator());
                break;
        }
    }

    public Album getSelectedAlbum(int index) { return selectedAlbums.get(index); }
}
