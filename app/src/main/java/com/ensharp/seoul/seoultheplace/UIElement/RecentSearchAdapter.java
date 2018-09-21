package com.ensharp.seoul.seoultheplace.UIElement;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ensharp.seoul.seoultheplace.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecentSearchAdapter extends ArrayAdapter<String> {
    private EditText editText;

    public RecentSearchAdapter(Activity context, ArrayList<String> recents) { super(context, 0, recents); }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.recent_search_layout, parent, false);
        }
        final TextView word = (TextView) listItemView.findViewById(R.id.recent_word);
        word.setText(getItem(position));

        final LinearLayout recentText = (LinearLayout) listItemView.findViewById(R.id.recent_text);

        recentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(((TextView)recentText.getChildAt(0)).getText());
            }
        });

        return listItemView;
    }
}