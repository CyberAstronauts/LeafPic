package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.ColorPalette;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

/**
 * Created by Jibo on 02/03/2016.
 */
public class SettingActivity extends ThemedActivity {

    Toolbar toolbar;
    SharedPreferences SP;

    TextView txtGT;
    TextView txtTT;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        txtTT = (TextView) findViewById(R.id.theme_setting_title);
        txtGT = (TextView) findViewById(R.id.general_setting_title);

        setTheme();

        //PRIMARY COLOR PIKER*****************************************
        LinearLayout ll_PC = (LinearLayout) findViewById(R.id.ll_primaryColor);
        ll_PC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrimaryColorPikerDialogShow();
            }
        });

        //ACCENT COLOR PIKER*****************************************
        LinearLayout ll_AC = (LinearLayout) findViewById(R.id.ll_accentColor);
        ll_AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccentColorPikerDialogShow();
            }
        });


        /**** Switches ****/
        /*********** SW COLLAPSING TOOLBAR ************/
        SwitchCompat swCollaps=(SwitchCompat) findViewById(R.id.SetCollapsingToolbar);
        swCollaps.setChecked(thereIsCollapsing());
        swCollaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_collaps_toolbar", !thereIsCollapsing());
                editor.apply();
            }
        });
        /*********** SW DARK THEME ********************/
        SwitchCompat swDarkTheme=(SwitchCompat) findViewById(R.id.SetDarkTheme);
        swDarkTheme.setChecked(isDarkTheme());
        swDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_dark_theme", !isDarkTheme());
                editor.apply();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        /*********** SW COLORED NAV BAR ****************/
        SwitchCompat swNavBar=(SwitchCompat) findViewById(R.id.SetColoredNavBar);
        swNavBar.setChecked(isNavigationBarColored());
        swNavBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("nav_bar", !isNavigationBarColored());
                editor.apply();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    public void PrimaryColorPikerDialogShow(){
        final AlertDialog.Builder PrimaryPikerDialog;
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        if (isDarkTheme()) PrimaryPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Dark);
        else PrimaryPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Light);

        final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
        final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary);
        final LineColorPicker colorPicker2 = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary2);
        final TextView title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_primary_title);
        CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_primary_card);

        colorPicker.setColors(ColorPalette.getBaseColors(getApplicationContext()));
        for (int i : colorPicker.getColors())
            for (int i2 : ColorPalette.getColors(getBaseContext(), i))
                if (i2 == getPrimaryColor()) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPalette.getColors(getBaseContext(), i));
                    colorPicker2.setSelectedColor(i2);
                    break;
                }
                    /*colorPicker.setSelectedColor(getPrimaryColor());

                    colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));

                    colorPicker2.setSelectedColor(colorPicker.getColor());*/
        title.setBackgroundColor(getPrimaryColor());
        if (!isDarkTheme())
            cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.cp_PrimaryLight));
        else cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryDark));

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                title.setBackgroundColor(c);
                getWindow().setStatusBarColor(c);
                toolbar.setBackgroundColor(c);
                //
                colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });
        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                title.setBackgroundColor(c);
                getWindow().setStatusBarColor(c);
                toolbar.setBackgroundColor(c);
            }
        });

        PrimaryPikerDialog.setView(Accent_dialogLayout);
        PrimaryPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getWindow().setStatusBarColor(getPrimaryColor());
                toolbar.setBackgroundColor(getPrimaryColor());
                dialog.cancel();
            }
        });
        PrimaryPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("primary_color", colorPicker2.getColor());
                editor.apply();
                updateTheme();
                setTheme();
            }
        });
        PrimaryPikerDialog.show();
    }

    public void AccentColorPikerDialogShow(){
        final AlertDialog.Builder AccentPikerDialog;
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
        if (isDarkTheme())
            AccentPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Dark);
        else
            AccentPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Light);

        final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
        final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerAccent);
        final TextView title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_accent_title);
        CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_accent_card);

        colorPicker.setColors(ColorPalette.getAccentColors(getApplicationContext()));
        colorPicker.setSelectedColor(getAccentColor());
        title.setBackgroundColor(getAccentColor());

        if (!isDarkTheme())
            cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryLight));
        else cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.cp_PrimaryDark));

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                title.setBackgroundColor(c);
                txtGT.setTextColor(colorPicker.getColor());
                txtTT.setTextColor(colorPicker.getColor());
            }
        });

        AccentPikerDialog.setView(Accent_dialogLayout);

        AccentPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                txtGT.setTextColor(getAccentColor());
                txtTT.setTextColor(getAccentColor());
            }
        });
        AccentPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("accent_color", colorPicker.getColor());
                editor.apply();
                updateTheme();
                txtGT.setTextColor(getAccentColor());
                txtTT.setTextColor(getAccentColor());
            }
        });
        AccentPikerDialog.show();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
    }

    public void setTheme(){

        /**** ToolBar *****/
        toolbar.setBackgroundColor(getPrimaryColor());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_arrow_back)
                        .color(Color.WHITE)
                        .sizeDp(19));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle("Setting");

        /**** Status Bar */
        getWindow().setStatusBarColor(getPrimaryColor());

        /**** Nav Bar ****/
        if (isNavigationBarColored())
            getWindow().setNavigationBarColor(getPrimaryColor());
        else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));

        /**** Recent App */
        setRecentApp("Setting");


        txtGT.setTextColor(getAccentColor());
        txtTT.setTextColor(getAccentColor());
        setThemeOnChangeListener();
    }

    public void setThemeOnChangeListener(){
        //BackGround
        LinearLayout bg = (LinearLayout) findViewById(R.id.setting_background);
        //Card
        CardView cvGeneral = (CardView) findViewById(R.id.general_setting_card);
        CardView cvTheme = (CardView) findViewById(R.id.theme_setting_card);
        //Linear Layout

        if(isDarkTheme()) {

            cvGeneral.setBackgroundColor(ContextCompat.getColor(SettingActivity.this, R.color.md_dark_cards));
            cvTheme.setBackgroundColor(ContextCompat.getColor(SettingActivity.this, R.color.md_dark_cards));
            bg.setBackgroundColor(getBackgroundColor());

            // GENERAL
            // TEXT AND ICON
            TextView txtC = (TextView) findViewById(R.id.collapsing_toolbar_Item);
            ImageView imgCI = (ImageView) findViewById(R.id.collapsing_toolbar_Icon);
            // SET COLOR
            txtC.setTextColor(ContextCompat.getColor(SettingActivity.this, R.color.cp_TextDark));
            imgCI.setImageResource(R.mipmap.ic_gradient_white_24dp);


            // THEME
            // TEXT
            TextView txtPC = (TextView) findViewById(R.id.PrimaryColor_Item);
            TextView txtAC = (TextView) findViewById(R.id.accentColor_Item);
            TextView txtDT = (TextView) findViewById(R.id.DarkTheme_Item);
            TextView txtNB = (TextView) findViewById(R.id.NavBar_Item);
            // TEXT SET COLOR
            txtPC.setTextColor(ContextCompat.getColor(SettingActivity.this, R.color.cp_TextDark));
            txtAC.setTextColor(ContextCompat.getColor(SettingActivity.this, R.color.cp_TextDark));
            txtDT.setTextColor(ContextCompat.getColor(SettingActivity.this, R.color.cp_TextDark));
            txtNB.setTextColor(ContextCompat.getColor(SettingActivity.this, R.color.cp_TextDark));
            // ICON
            ImageView imgPC = (ImageView) findViewById(R.id.PrimaryColor_Icon);
            ImageView imgAC = (ImageView) findViewById(R.id.accentColor_Icon);
            ImageView imgDT = (ImageView) findViewById(R.id.DarkTheme_Icon);
            ImageView imgNB = (ImageView) findViewById(R.id.NavBar_Icon);
            // SET ICON
            imgPC.setImageResource(R.mipmap.ic_color_lens_white_24dp);
            imgAC.setImageResource(R.mipmap.ic_colorize_white_24dp);
            imgDT.setImageResource(R.mipmap.ic_invert_colors_white_24dp);
            imgNB.setImageResource(R.mipmap.ic_video_label_white_24dp);
            ////////////

        } else {
            cvGeneral.setBackgroundColor( ContextCompat.getColor(SettingActivity.this, R.color.md_light_cards));
            cvTheme.setBackgroundColor(ContextCompat.getColor(SettingActivity.this, R.color.md_light_cards));
            bg.setBackgroundColor(getBackgroundColor());
        }
    }




}
