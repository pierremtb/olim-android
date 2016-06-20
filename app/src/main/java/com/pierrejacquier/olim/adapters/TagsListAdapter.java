package com.pierrejacquier.olim.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.helpers.Graphics;

public class TagsListAdapter extends ArrayAdapter<Tag> {
    private final Context context;
    private final Tag[] tags;
    private final View.OnClickListener callback;

    public TagsListAdapter(Context context, Tag[] tags, View.OnClickListener callback ) {
        super(context, R.layout.item_tag, tags);
        this.context = context;
        this.tags = tags;
        this.callback = callback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_tag, parent, false);
        TextView tagPrimaryText = (TextView) rowView.findViewById(R.id.tagPrimaryText);
        TextView tagSecondaryText = (TextView) rowView.findViewById(R.id.tagSecondaryText);
        ImageButton tagIconButton = (ImageButton) rowView.findViewById(R.id.tagIconButton);
        IconicsDrawable tagIcon = new IconicsDrawable(parent.getContext()).sizeDp(20).color(Color.WHITE);
        int hintColor = getContext().getResources().getColor(R.color.colorHintText);
        final Tag tag = tags[position];
        tagPrimaryText.setText(tag.getHashName());
        tagSecondaryText.setText(tag.getComments());

        if (tag.getColor() != null) {
            tagIconButton.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            tagIconButton.setBackgroundDrawable(
                    Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
            );
        }

        if (tag.getIcon() != null) {
            try {
                tagIcon.icon(GoogleMaterial.Icon.valueOf("gmd_" + tag.getIcon()));
            } catch (Exception e ) {
                tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            }
        } else {
            tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
        }

        tagIconButton.setImageDrawable(tagIcon);

        rowView.setOnClickListener(callback);
        rowView.setTag(position);

        return rowView;
    }
} 