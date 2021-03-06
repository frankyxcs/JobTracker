package com.pranjals.nsit.jobtracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pranjals.nsit.jobtracker.contentprovider.DBContentProvider;

import java.util.ArrayList;

public class CustomerViewActivity extends AppCompatActivity {

    private long customerIdTobeViewed;
    public static final String START_WITH_ID = "_idOfCustomerToView";
    private ArrayList<String> extraCols;
    private ArrayList<String> extraColDataTypes;
    ArrayList<Order> orders;
    private String[] colValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        customerIdTobeViewed = getIntent().getLongExtra(START_WITH_ID, 1);
        LinearLayout container = (LinearLayout)findViewById(R.id.customerView_container);
        LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        extraCols = DBHelper.getInstance(CustomerViewActivity.this).getExtraCols(1);
        extraColDataTypes = DBHelper.getInstance(CustomerViewActivity.this).getExtraOrderColDataTypes(1);

        TextView name = (TextView)findViewById(R.id.customerView_name);
        TextView mobile = (TextView)findViewById(R.id.customerView_mobile);
        TextView email = (TextView)findViewById(R.id.customerView_email);
        TextView address = (TextView)findViewById(R.id.customerView_address);
        ImageView image = (ImageView)findViewById(R.id.iv_customerView);
        colValues = new String[DBHelper.DEF_CUSTOMER_COLS + extraCols.size()];

        Cursor c = getContentResolver().query(DBContentProvider.CUSTOMER_URI, null, "_id = " + customerIdTobeViewed, null, null);
        if(c!=null && c.moveToFirst()){
            for(int i=2;i<=colValues.length;i++) { // 0 is _id and 1 is blob image
                colValues[i - 2] = c.getString(i);
                //Log.v("values", c.getString(i));
            }
            byte[] bb = c.getBlob(c.getColumnIndex("image"));
            image.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));

            c.close();
        }

        name.setText(colValues[0]);
        mobile.setText(colValues[1]);
        email.setText(colValues[2]);
        address.setText(colValues[3]);

        for(int i=0;i<extraCols.size();i++){
            View viewToAdd = inflater.inflate(R.layout.customer_view_dynamic_row,null);
            TextView tv = (TextView)viewToAdd.findViewById(R.id.customerViewDynamic_tv);
            tv.setText(colValues[i+DBHelper.DEF_CUSTOMER_COLS-1]);
            tv = (TextView)viewToAdd.findViewById(R.id.customerViewDynamicColumn_tv);
            tv.setText(extraCols.get(i).replace("_", " "));
            ImageView imageView = (ImageView)viewToAdd.findViewById(R.id.customerViewDynamicTypeImage_tv);
            switch (extraColDataTypes.get(i)){
                case "TEXT" : imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_datatype_text_24dp));
                    break;
                case "NUME" : imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_datatype_numeric_24dp));
                    break;
                case "DATE" : imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_today_black_24dp));
                    break;
                default : imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_datatype_text_24dp));
                    break;
            }
            container.addView(viewToAdd);
        }

        //Setting the recycler view for customer related orders
        orders = new ArrayList<>();
    }


    @Override
    protected void onResume(){
        super.onResume();
        refreshOrderList();
    }

    private void startOrderViewActivity(Long _id){
        Intent intent = new Intent(this, OrderViewActivity.class);
        intent.putExtra(OrderViewActivity.START_WITH_ID, _id);
        startActivity(intent);
    }

    public void onShowAllButtonClicked(View view){
        Intent intent = new Intent(this, OrderListActivity.class);
        intent.putExtra(OrderListActivity.START_FOR_CID, customerIdTobeViewed);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.edit_customer:
                Intent intent = new Intent(CustomerViewActivity.this,CustomerEditActivity.class);
                intent.putExtra("customerId", customerIdTobeViewed);
                startActivityForResult(intent, 15);
                break;

            case R.id.delete_customer:
                getContentResolver().delete(DBContentProvider.CUSTOMER_URI,"_id = "+customerIdTobeViewed,null);
                finish();
                break;

            case android.R.id.home : finish();
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==15&&resultCode==RESULT_OK){

            //refreshUi
            TextView name = (TextView)findViewById(R.id.customerView_name);
            TextView mobile = (TextView)findViewById(R.id.customerView_mobile);
            TextView email = (TextView)findViewById(R.id.customerView_email);
            TextView address = (TextView)findViewById(R.id.customerView_address);
            ImageView image = (ImageView)findViewById(R.id.iv_customerView);


            Cursor c = getContentResolver().query(DBContentProvider.CUSTOMER_URI, null, "_id = "+ customerIdTobeViewed, null, null);
            if(c!=null&&c.moveToFirst()){
                name.setText(c.getString(2));
                mobile.setText(c.getString(3));
                email.setText(c.getString(4));
                address.setText(c.getString(5));

                for(int i=0;i<extraCols.size();i++)
                {
                    ((TextView)findViewById(i)).setText(c.getString(i+6));
                }

                byte[] bb = c.getBlob(c.getColumnIndex("image"));
                image.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));

                c.close();
            }
        }
    }


    public void refreshOrderList(){

        orders = new ArrayList<>();
        String projection[] = {"_id","name", "doo", "doc", "cid", "eid", "curStage", "totalStages"};
        String selectionArgs[] = {Long.toString(customerIdTobeViewed)};
        //Log.v("ordercard", "customerId = " + Long.toString(customerIdTobeViewed));
        Cursor c = getContentResolver().query(DBContentProvider.ORDER_URI,projection,"cid = ?",selectionArgs,"(curStage*1.0)/(totalStages*1.0) LIMIT 5");
        if (c.moveToFirst()) {
            do {

                Log.v("ordercard", "Got a result!");
                String _id = c.getString(c.getColumnIndex("_id"));
                String name = c.getString(c.getColumnIndex("name"));
                String doo = DBHelper.getDDMMYYYY(c.getString(c.getColumnIndex("doo")));
                String doc = DBHelper.getDDMMYYYY(c.getString(c.getColumnIndex("doc")));
                String cid = c.getString(c.getColumnIndex("cid"));
                String eid = c.getString(c.getColumnIndex("eid"));
                int curStage = c.getInt(c.getColumnIndex("curStage"));
                int totalStages = c.getInt(c.getColumnIndex("totalStages"));
                orders.add(new Order(Long.parseLong(_id), name, Long.parseLong(cid), Long.parseLong(eid), doo, doc, curStage, totalStages));
            } while(c.moveToNext());
        }

        LinearLayout orderContainer = (LinearLayout) findViewById(R.id.order_card_container);
        orderContainer.removeAllViews();

        LayoutInflater layoutInflater = getLayoutInflater();

        for (int i = 0; i < orders.size(); i++) {
            View cardView = layoutInflater.inflate(R.layout.order_list_card, null);
            TextView tv = (TextView)cardView.findViewById(R.id.cardView_name);
            tv.setText(orders.get(i).getName());
            cardView.setTag(orders.get(i).get_id());
            tv = (TextView)cardView.findViewById(R.id.cardView_doo);
            tv.setText(orders.get(i).getDoo());
            tv = (TextView)cardView.findViewById(R.id.cardView_doc);
            tv.setText(orders.get(i).getDoc());
            tv = (TextView)cardView.findViewById(R.id.cardView_cid);
            tv.setText(Long.toString(orders.get(i).getCid()));
            ImageView orderImg = (ImageView)cardView.findViewById(R.id.cardView_image);
            ProgressBar progressBar = (ProgressBar)cardView.findViewById(R.id.order_card_progress_bar);
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(ProgressBar.VISIBLE);
            progressBar.setProgress(orders.get(i).getCurStage());
            progressBar.setMax(orders.get(i).getTotalStages());

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startOrderViewActivity(Long.parseLong(v.getTag().toString()));
                }
            });

            orderContainer.addView(cardView);
        }
    }


}
