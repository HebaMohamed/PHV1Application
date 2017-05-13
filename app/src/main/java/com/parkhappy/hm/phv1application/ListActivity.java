package com.parkhappy.hm.phv1application;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListActivity extends AppCompatActivity {
    ListView lv;
    Button closestbtn,cheapestbtn;

    ArrayAdapter adapter;
    TextView nametxt, addrtxt, pricetxt,disttxt;
    ImageView ccimg,ccimg2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getSupportActionBar().hide();

        lv=(ListView)findViewById(R.id.lv);
        closestbtn = (Button) findViewById(R.id.closestbtn);
        cheapestbtn = (Button) findViewById(R.id.cheapestbtn);
        nametxt = (TextView) findViewById(R.id.nametxt);
        addrtxt = (TextView) findViewById(R.id.addrtxt);
        pricetxt = (TextView) findViewById(R.id.pricetxt);
        disttxt = (TextView) findViewById(R.id.disttxt);
        ccimg = (ImageView) findViewById(R.id.ccimg);
        ccimg2 = (ImageView) findViewById(R.id.ccimg2);
        LinearLayout ccLayout = (LinearLayout) findViewById(R.id.ccLayout);


        //sort the last list
        sortcheap();
        setadapter(MainActivity.myLots);


        closestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closestbtn.setBackgroundColor(Color.WHITE);
                closestbtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                cheapestbtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cheapestbtn.setTextColor(Color.WHITE);
                MainActivity.closecheapF=1;
                MainActivity.getnearest();
                fillMinData();

            }
        });
        cheapestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cheapestbtn.setBackgroundColor(Color.WHITE);
                cheapestbtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                closestbtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                closestbtn.setTextColor(Color.WHITE);
                MainActivity.closecheapF=0;
                MainActivity.getcheapest();
                fillMinData();
            }
        });
        fillMinData();
        ccLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.selectedLotID = MainActivity.minID;
                startActivity(new Intent(ListActivity.this, DetailsActivity.class));
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
//                DetailsActivity.selected = position;
//                startActivity(new Intent(ListActivity.this, DetailsActivity.class));
            }
        });
    }

    void fillMinData(){
        Lot ccLot = MainActivity.myLots.get(0);//MainActivity.minI);
        nametxt.setText(ccLot.name);
        addrtxt.setText(String.valueOf(ccLot.address1+", "+ccLot.city+", "+ccLot.country));
        pricetxt.setText("$"+Math.round(ccLot.priceBasic));
        disttxt.setText(ccLot.distance+"Km");

        if(MainActivity.closecheapF==0){
            ccimg2.setVisibility(View.GONE);
            ccimg.setVisibility(View.VISIBLE);
            sortcheap();
            setadapter(MainActivity.myLots);
        }
        else{
            ccimg.setVisibility(View.GONE);
            ccimg2.setVisibility(View.VISIBLE);
            sortdistance();
            setadapter(MainActivity.myLots);
        }
    }

    void sortcheap(){
        //sort an arraylist of objects by a property
        Collections.sort(MainActivity.myLots, new Comparator<Lot>() {
            @Override public int compare(Lot p1, Lot p2) {
                return (int) (p1.priceBasic - p2.priceBasic); // Ascending
            }
        });
    }
    void sortdistance(){
        //sort an arraylist of objects by a property
        Collections.sort(MainActivity.myLots, new Comparator<Lot>() {
            @Override public int compare(Lot p1, Lot p2) {
                return (int) (p1.distance - p2.distance); // Ascending
            }
        });
    }

    void setadapter(final ArrayList<Lot> a) {
        lv.setAdapter(null);
        adapter = new ArrayAdapter(ListActivity.this, R.layout.lotitemlayout, R.id.nametxt, a) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView nametxt = (TextView) view.findViewById(R.id.nametxt);
                TextView addrtxt = (TextView) view.findViewById(R.id.addrtxt);
                TextView pricetxt = (TextView) view.findViewById(R.id.pricetxt);
                TextView disttxt = (TextView) view.findViewById(R.id.disttxt);
                nametxt.setText(a.get(position).name);
                addrtxt.setText(String.valueOf(a.get(position).address1 + ", " + a.get(position).city + ", " + a.get(position).country));
                pricetxt.setText("$" + Math.round(a.get(position).priceBasic));
                disttxt.setText(a.get(position).distance + "Km");

                final int pos= position;

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.selectedLotID = a.get(pos).id;
                        startActivity(new Intent(ListActivity.this, DetailsActivity.class));
                    }
                });

                return view;
            }
        };
        lv.setAdapter(adapter);

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MainActivity.selectedLotID = MainActivity.myLots.get(position).id;
//                startActivity(new Intent(ListActivity.this, DetailsActivity.class));
//            }
//        });
    }
}
