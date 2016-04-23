package com.pranjals.nsit.jobtracker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.pranjals.nsit.jobtracker.contentprovider.DBContentProvider;

import java.util.ArrayList;

public class OrderListActivity extends AppCompatActivity {

    private String sortOrder;
    private PopupWindow openedPopupWindow;
    ArrayList<Order> orders;
    OrderRecyclerView adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        orders = new ArrayList<>();
        final ArrayList<Order> ordersFinal = orders;

        sortOrder = null;

        String projection[] = {"_id","name", "doo", "doc", "cid", "eid"};
        Cursor c = getContentResolver().query(DBContentProvider.ORDER_URI,projection,null,null,sortOrder);
        if (c.moveToFirst()) {
            do {
                String _id = c.getString(c.getColumnIndex("_id"));
                String name = c.getString(c.getColumnIndex("name"));
                String doo = DBHelper.getDDMMYYYY(c.getString(c.getColumnIndex("doo")));
                String doc = DBHelper.getDDMMYYYY(c.getString(c.getColumnIndex("doc")));
                String cid = c.getString(c.getColumnIndex("cid"));
                String eid = c.getString(c.getColumnIndex("eid"));
                orders.add(new Order(Long.parseLong(_id),name,Long.parseLong(cid),Long.parseLong(eid),doo,doc));
            } while(c.moveToNext());
        }

        recyclerView = (RecyclerView)findViewById(R.id.OrderRecyclerView);
        adapter = new OrderRecyclerView(orders);
        adapter.setOnItemClickListener(new OrderRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startOrderViewActivity(ordersFinal.get(position).get_id());

            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void startOrderViewActivity(Long _id){
        Intent intent = new Intent(this, OrderViewActivity.class);
        intent.putExtra(OrderViewActivity.START_WITH_ID, _id);
        startActivity(intent);
    }

    public void onSortByButtonClicked(View view){
        LayoutInflater layoutInflater = getLayoutInflater();
        final View popupView = layoutInflater.inflate(R.layout.popup_window_sort_order_by, null);
        if(sortOrder != null){
            String tag;
            if(sortOrder.contains(" DESC")){
                tag = sortOrder.substring(0, sortOrder.length() - 5);
                ((ImageView) ((ViewGroup)(((ViewGroup) popupView).findViewWithTag(tag))).getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_expand_more_black_18dp));
            }
            else{
                tag = sortOrder;
                ((ImageView) ((ViewGroup)(((ViewGroup) popupView).findViewWithTag(tag))).getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_expand_less_black_18dp));
            }
        }
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        float offsetPosition = 18f*(getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(view, 0, (int) (-offsetPosition));
        openedPopupWindow = popupWindow;
    }

    public void onSortBySelected(View view){
        String sortBy = view.getTag().toString();
        if(sortOrder == sortBy) {
            sortOrder = (sortBy + " DESC");
        }
        else if(sortOrder == (sortBy + " DESC")) {
            sortOrder = sortBy.substring(0, sortBy.length() - 5);
        }
        else {
            sortOrder = sortBy;
        }

        refreshListOnSort(sortOrder);

        openedPopupWindow.dismiss();
    }

    public void refreshListOnSort(String sortOrder){

        orders = new ArrayList<>();
        String projection[] = {"_id","name", "doo", "doc", "cid", "eid"};

        Cursor c = getContentResolver().query(DBContentProvider.ORDER_URI,projection,null,null,sortOrder);
        if (c.moveToFirst()) {
            do {
                String _id = c.getString(c.getColumnIndex("_id"));
                String name = c.getString(c.getColumnIndex("name"));
                String doo = DBHelper.getDDMMYYYY(c.getString(c.getColumnIndex("doo")));
                String doc = DBHelper.getDDMMYYYY(c.getString(c.getColumnIndex("doc")));
                String cid = c.getString(c.getColumnIndex("cid"));
                String eid = c.getString(c.getColumnIndex("eid"));
                orders.add(new Order(Long.parseLong(_id),name,Long.parseLong(cid),Long.parseLong(eid),doo,doc));
            } while(c.moveToNext());
        }

        adapter = new OrderRecyclerView(orders);
        recyclerView.swapAdapter(adapter, false);

    }

}