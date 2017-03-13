package com.oNotes.application;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.util.List;


public class CustomAdapter extends BaseAdapter {

    static class ViewHolder {
        private RelativeLayout relativeLayout;
        private Button imgBtn;
        private boolean state;
    }

    List<MyListItem> list;
    Context context;
    private static LayoutInflater inflater = null;
    private static int previous = -1;
    private static ViewHolder lastView = null;
    public static boolean commandToExecute ;
    CustomAdapter(Context context, List<MyListItem> list) {

        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {

        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        final View vi = convertView;
        final int p = position;

        convertView = inflater.inflate(R.layout.row, parent, false);
        holder = new ViewHolder();

        holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.layout);
        holder.imgBtn = (Button) convertView.findViewById(R.id.btn);


        holder.state = list.get(position).isState();

        if (holder.state) {

            holder.imgBtn.setText("START");
            holder.imgBtn.setBackgroundColor(Color.rgb(40, 195, 213));
        } else {

            holder.imgBtn.setText("STOP");
            holder.imgBtn.setBackgroundColor(Color.rgb(255, 153, 0));
        }

        holder.imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ControlScentActivity.mConnected) {
                    if(!MainFragment.isCommandSend) {
                   /* if(previous!=-1)if(previous!=p)
                    {
                        lastView.imgBtn.setText("START");
                        lastView.imgBtn.setBackgroundColor(Color.rgb(40,195,213));
                            list.get(previous).setState(!list.get(previous).isState());
                    }*/
                        if (context instanceof ControlScentActivity) {
                            if (list.get(p).isState()) {
                                if(MainFragment.status){
                                MainFragment.status = false;
                                commandToExecute = true;
                                ((ControlScentActivity) context).playScent(list.get(p).getScentCode());
                                    MainFragment.isCommandSend = true;
                                    MainFragment.lastScentPlayed = p;
                                    holder.imgBtn.setText("sending...");
                                    holder.imgBtn.setBackgroundColor(Color.rgb(203, 50, 102));
                                }else{
                                    Toast.makeText(context,"Stop previous command",Toast.LENGTH_SHORT).show();
                                }

                         /*   holder.imgBtn.setText("STOP");
                            holder.imgBtn.setBackgroundColor(Color.rgb(255, 153, 0));
                            list.get(p).setState(false);
                            previous = p;
                            lastView = holder;*/
                            } else {
                                MainFragment.status = true;
                                commandToExecute = false;
                                ((ControlScentActivity) context).stopScent();
                           /* holder.imgBtn.setText("START");
                            holder.imgBtn.setBackgroundColor(Color.rgb(40, 195, 213));
                            list.get(p).setState(true);
                            previous = p;
                            lastView = holder;*/
                                MainFragment.isCommandSend = true;
                                MainFragment.lastScentPlayed = p;
                                holder.imgBtn.setText("sending...");
                                holder.imgBtn.setBackgroundColor(Color.rgb(203, 50, 102));
                            }

                        }
                    }else {
                        Toast.makeText(context,"Command already send...Wait",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context,"Not connected to Device",Toast.LENGTH_SHORT).show();
                }
            }
        });

        convertView.setTag(holder);


        holder.relativeLayout.setBackgroundResource(list.get(p).getBackImage());
        return convertView;
    }


}

