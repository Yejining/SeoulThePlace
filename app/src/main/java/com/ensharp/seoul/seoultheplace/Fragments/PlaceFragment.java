package com.ensharp.seoul.seoultheplace.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ensharp.seoul.seoultheplace.CourseVO;
import com.ensharp.seoul.seoultheplace.DAO;
import com.ensharp.seoul.seoultheplace.DetailInformationVO;
import com.ensharp.seoul.seoultheplace.MainActivity;
import com.ensharp.seoul.seoultheplace.PlaceVO;
import com.ensharp.seoul.seoultheplace.R;
import com.ensharp.seoul.seoultheplace.UIElement.DetailInformationAdapter;
import com.ensharp.seoul.seoultheplace.UIElement.FloatingButton.FloatingActionButton;
import com.ensharp.seoul.seoultheplace.UIElement.PlaceViewPagerAdapter;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressLint("ValidFragment")
public class PlaceFragment extends Fragment {
    public static final int VIA_SEARCH = 0;
    public static final int VIA_COURSE = 1;
    public static final int DURING_EDITTING_COURSE = 2;

    private int enterRoute;
    private String courseCode;
    private int index;
    private PlaceVO place;
    private View rootView;
    private DetailInformationAdapter adapter;
    private ImageView[] dots;
    private int dotCount;
    private CourseVO courseVO;
    private FloatingActionButton createCourse;

    private PullToRefreshView destroyView;

    @SuppressLint("ValidFragment")
    public PlaceFragment(String placeCode, int enterRoute) {
        this.enterRoute = enterRoute;
        courseCode = "";
        index = 0;
        DAO dao = new DAO();
        place = dao.getPlaceData(placeCode);
    }

    @SuppressLint("ValidFragment")
    public PlaceFragment(CourseVO course, int index, int enterRoute) {
        this.enterRoute = enterRoute;
        this.courseCode = courseCode;
        this.index = index;
        DAO dao = new DAO();
        courseVO = course;
        place = dao.getPlaceData(courseVO.getPlaceCode(index - 1));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_place, container, false);
        destroyView = (PullToRefreshView) rootView.findViewById(R.id.pull_to_destroy);

        final MainActivity activity = (MainActivity)getActivity();

        if (enterRoute != VIA_COURSE)
        {
            destroyView.setRefreshing(false);
            destroyView.setEnabled(false);
        }

        destroyView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                destroyView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("abcd",getFragmentManager().getBackStackEntryAt(0).getName());
//                        int i = 0;
//                        while (getFragmentManager().getBackStackEntryAt(i).getName() == "PLACE_FRAGMENT") {
//                            getFragmentManager().popBackStack();
//                            i++;
//                        }
                    }
                }, 1000);
            }
        });

        setPlaceImages(place.getImageURL());
        setPlaceCountImage(index);
        setButtons();

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView address = (TextView) rootView.findViewById(R.id.address);
        TextView phone = (TextView) rootView.findViewById(R.id.phone);
        TextView detail = (TextView) rootView.findViewById(R.id.detail);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.map, new MapFragment(place.getCoordinate_x(), place.getCoordinate_y(), place.getName()));
        transaction.commit();

        if (enterRoute != DURING_EDITTING_COURSE)
        {
            FrameLayout space = rootView.findViewById(R.id.space);
            space.setVisibility(View.GONE);
        }

        String parking = place.getParking();
        if(!parking.equals("없음")) parking = place.getParkFee();

        title.setText(place.getName());
        address.setText(place.getLocation());
        phone.setText(place.getPhone());
        detail.setText(place.getDetails());

        createCourse = (FloatingActionButton) rootView.findViewById(R.id.create_course_using_this);
        if (enterRoute == DURING_EDITTING_COURSE) createCourse.setVisibility(View.GONE);
        createCourse.setOnClickListener(onCreateCourseListener);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall();
            }
        });

        ListView information = (ListView) rootView.findViewById(R.id.information);

        ArrayList<DetailInformationVO> detailInformation = new ArrayList<DetailInformationVO>(
                Arrays.asList(new DetailInformationVO[] {
                    new DetailInformationVO("item_time", "운영시간", place.getBusinessHours()),
                    new DetailInformationVO("item_parking", "주차", parking),
                    new DetailInformationVO("item_tip", "팁", place.getTip()),
                    new DetailInformationVO("item_tag", "tag", place.getType()),
                    new DetailInformationVO("item_liked", "이 플레이스를 좋아한 사람들", place.getLikes()+"명")
                }));

        adapter = new DetailInformationAdapter(getContext(), detailInformation);
        information.setAdapter(adapter);

        getTotalHeightOfListView(information);

        return rootView;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int userAnswer) {
            if (userAnswer == DialogInterface.BUTTON_NEGATIVE) return;

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CALL_PHONE}, 0);
            }

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + place.getPhone()));
                getContext().startActivity(intent);
            }
        }
    };

    private void makeCall() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("전화를 거시겠습니까?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void setPlaceImages(String[] images) {
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.images);
        LinearLayout sliderDotsPanel = (LinearLayout) rootView.findViewById(R.id.sliderDots);
        PlaceViewPagerAdapter placeViewPagerAdapter = new PlaceViewPagerAdapter(getContext(), images);

        viewPager.setAdapter(placeViewPagerAdapter);

        dotCount = placeViewPagerAdapter.getCount();
        dots = new ImageView[dotCount];

        for (int i = 0; i < dotCount; i++) {
            dots[i] = new ImageView(rootView.getContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(rootView.getContext(), R.drawable.item_non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8,0,8,0);

            sliderDotsPanel.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(rootView.getContext(), R.drawable.item_active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotCount; i++)
                    dots[i].setImageDrawable(ContextCompat.getDrawable(rootView.getContext(), R.drawable.item_non_active_dot));

                dots[position].setImageDrawable(ContextCompat.getDrawable(rootView.getContext(), R.drawable.item_active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setPlaceCountImage(int index) {
        FrameLayout numberIndicator = (FrameLayout) rootView.findViewById(R.id.numberIndicator);

        getLayoutInflater().inflate(R.layout.item_place_index, numberIndicator);
        TextView indexInImage = (TextView) numberIndicator.findViewById(R.id.index);
        indexInImage.setText(Integer.toString(index));

        if (index != 0 || enterRoute != DURING_EDITTING_COURSE)
            numberIndicator.setVisibility(View.VISIBLE);
        else
            numberIndicator.setVisibility(View.GONE);
    }

    private void setButtons() {
        if (index == 0) return;

        FrameLayout layout = (FrameLayout) rootView.findViewById(R.id.buttons);
        ImageButton previousImage = (ImageButton) rootView.findViewById(R.id.previous_page_image_button);
        Button previous = (Button) rootView.findViewById(R.id.previous_page_text_button);
        ImageButton nextImage = (ImageButton) rootView.findViewById(R.id.next_page_image_button);
        Button next = (Button) rootView.findViewById(R.id.next_page_text_button);

        DAO dao = new DAO();

        if (index == 1) {
            previousImage.setVisibility(View.INVISIBLE);
            previous.setVisibility(View.INVISIBLE);
        } else if (index == dao.getCourseData("c001").getPlaceCount()) {
            nextImage.setVisibility(View.INVISIBLE);
            next.setVisibility(View.INVISIBLE);
        }

        final MainActivity activity = (MainActivity)getActivity();

        previousImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.changeToPlaceFragment(courseVO, index - 1, VIA_COURSE);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.changeToPlaceFragment(courseVO, index - 1, VIA_COURSE);
            }
        });

        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.changeToPlaceFragment(courseVO, index + 1, VIA_COURSE);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.changeToPlaceFragment(courseVO, index + 1, VIA_COURSE);
            }
        });

        layout.setVisibility(View.VISIBLE);
    }

    private FloatingActionButton.OnClickListener onCreateCourseListener = new FloatingActionButton.OnClickListener() {
        MainActivity activity = (MainActivity) getActivity();

        @Override
        public void onClick(View view) {
            List<PlaceVO> places = new ArrayList<PlaceVO>();
            places.add(place);

            changeModifyFragment(places);
        }
    };

    public void changeModifyFragment(List<PlaceVO> places) {
        MainActivity activity = (MainActivity) getActivity();

        activity.changeModifyFragment(places);
    }

    public void getTotalHeightOfListView(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        int totalHeight = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, listView);

            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
