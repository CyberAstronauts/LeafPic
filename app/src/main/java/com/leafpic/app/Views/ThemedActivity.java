package com.leafpic.app.Views;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.leafpic.app.utils.ColorPalette;
import com.leafpic.app.R;

/**
 * Created by dnld on 23/02/16.
 */
public class ThemedActivity extends AppCompatActivity {

    SharedPreferences SP;

    private int primaryColor;
    private int accentColor;
    private boolean darkTheme;
    private boolean coloredNavigationBar;
    private boolean collapsing;

    public int getAccentColor() {
        return accentColor;
    }

    public boolean isNavigationBarColored() {
        return coloredNavigationBar;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public boolean thereIsCollapsing() {
        return collapsing;
    }

    public int getBackgroundColor(){
        if(darkTheme) return ColorPalette.getDarkBackgroundColor(getApplicationContext());
        else return ColorPalette.getLightBackgroundColor(getApplicationContext());
    }
    @Override
    public void onResume(){
        super.onResume();
        updateTheme();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        updateTheme();
    }

    public void updateTheme(){
        this.primaryColor = SP.getInt("primary_color", ContextCompat.getColor(getApplicationContext(),R.color.accent_blue));//TEAL CARD BG DEFAULT;
        this.accentColor = SP.getInt("accent_color",ContextCompat.getColor(getApplicationContext(),R.color.accent_green));//TEAL COLOR DEFAULT
        darkTheme = SP.getBoolean("set_dark_theme", false);
        coloredNavigationBar =SP. getBoolean("nav_bar", false);
        collapsing = SP.getBoolean("set_collaps_toolbar",true);
    }
    public void setRecentApp(String text){
        BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
        setTaskDescription(new ActivityManager.TaskDescription(text, drawable.getBitmap(), getPrimaryColor()));
    }
}
