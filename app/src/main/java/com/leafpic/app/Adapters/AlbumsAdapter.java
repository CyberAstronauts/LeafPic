package com.leafpic.app.Adapters;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    ArrayList<Album> albums;
    private int layout_ID;
    boolean selected=false;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public AlbumsAdapter(ArrayList<Album> ph, int id) {
        albums = ph;
        layout_ID = id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout_ID, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumsAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        a.setPath();

        //Glide.clear(holder.picture);

        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);
        holder.name.setTag(a.Path);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(holder.picture.getContext());
        String textColor;

        if (SP.getBoolean("set_dark_theme", false))
            textColor="#FAFAFA";
        else
            textColor="#2b2b2b";

        if (a.isSelected()) {
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.selected_album));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectHolder.setVisibility(View.VISIBLE);
            //White Text On White Theme
            if (SP.getBoolean("set_dark_theme", false)==false){
                selected=true;
                holder.name.setText(Html.fromHtml("<i><font color='#FAFAFA'>" + a.DisplayName + "</font></i>"));
                holder.nPhotos.setText(Html.fromHtml("<b><font color='" + SP.getString("PrefColor", "#03A9F4") + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                        "color='#FAFAFA'> Photos</font>"));
            }
        } else {
            selected=false;
            holder.picture.clearColorFilter();
            holder.selectHolder.setVisibility(View.INVISIBLE);
            if (SP.getBoolean("set_dark_theme", false))
                holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.unselected_album));
            else
                holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.background_material_light));
        }
        if (selected==false) {
            holder.name.setText(Html.fromHtml("<i><font color='" + textColor + "'>" + a.DisplayName + "</font></i>"));
            holder.nPhotos.setText(Html.fromHtml("<b><font color='" + SP.getString("PrefColor", "#03A9F4") + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                    "color='" + textColor + "'> Photos</font>"));
        }
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card_layout;
        ImageView picture;
        ImageView selectHolder;
        TextView name;
        TextView nPhotos;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            card_layout = (LinearLayout) itemView.findViewById(R.id.linear_card_text);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
        }
    }


}



