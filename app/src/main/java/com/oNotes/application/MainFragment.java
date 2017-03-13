package com.oNotes.application;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class MainFragment extends Fragment {


    public static List<MyListItem> list = new ArrayList<MyListItem>();
    public static  ListView listView;
    public static boolean isCommandSend = false;
    public static boolean status = true;
    public static int lastScentPlayed = 0;
    public static CustomAdapter customAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

       // ListData listData = new ListData();
        list = ListData.getData();
         customAdapter = new CustomAdapter(getContext(), list);
         listView = (ListView) view.findViewById(R.id.mainList);
        listView.setAdapter(customAdapter);
        return view;
    }

}
