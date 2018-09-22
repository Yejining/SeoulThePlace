package com.ensharp.seoul.seoultheplace.UIElement;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ensharp.seoul.seoultheplace.EdittedCourseVO;
import com.ensharp.seoul.seoultheplace.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomizedCourseAdapter extends ArrayAdapter<EdittedCourseVO> {

    private Context context;
    private List<EdittedCourseVO> customizedCourseList;
    private ImageView image;
    private TextView name;
    private TextView location;

    public CustomizedCourseAdapter(@NonNull Context context, int resource, @NonNull List<EdittedCourseVO> list) {
        super(context, resource, list);
        this.context = context;
        this.customizedCourseList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.item_customized_course, parent, false);

        EdittedCourseVO currentInformation = customizedCourseList.get(position);

        image = (ImageView) listItem.findViewById(R.id.customized_course_image);
        name = (TextView) listItem.findViewById(R.id.customized_course_name);
        location = (TextView) listItem.findViewById(R.id.customized_course_location);

        Picasso.get().load(currentInformation.getPlaceImage(0)).into(image);
        name.setText(currentInformation.getName());
        location.setText(currentInformation.getPlaceLocation(0));

        return listItem;
    }
}
