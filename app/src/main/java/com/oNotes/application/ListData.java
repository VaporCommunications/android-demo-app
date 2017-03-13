package com.oNotes.application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu on 13/2/17.
 */

public class ListData {

    private static final int[] backImage = {
            R.drawable.newimg01,
            R.drawable.newimg02,
            R.drawable.img03,
            R.drawable.img05,
           /* R.drawable.newimg03,
            R.drawable.newimg01,
            R.drawable.newimg02,
            R.drawable.img03,
            R.drawable.img05,
            R.drawable.newimg03*/
    };

    private static final String[] scentCodes = {
      "BGL","CHM","DIN","EJO"
    };





    public static List<MyListItem> getData(){
        List<MyListItem> list = new ArrayList<>();


        for(int i = 0; i < backImage.length; i++)
        {
            MyListItem myItem = new MyListItem();
            myItem.setBackImage(backImage[i]);
            myItem.setState(true);
            myItem.setScentCode(scentCodes[i]);
            list.add(myItem);
        }

        return list;
    }
}
