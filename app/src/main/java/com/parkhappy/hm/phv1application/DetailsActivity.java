package com.parkhappy.hm.phv1application;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    ListView priceslv, featureslv;
    ArrayAdapter adapter, adapter2;
    TextView nametxt, addrtxt, disttxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().hide();

        priceslv = (ListView) findViewById(R.id.priceslv);
        featureslv = (ListView) findViewById(R.id.featureslv);
        nametxt = (TextView) findViewById(R.id.nametxt);
        addrtxt = (TextView) findViewById(R.id.addrtxt);
        disttxt = (TextView) findViewById(R.id.disttxt);

        getlot();
    }

    void getlot(){

        StringRequest sr = new StringRequest(Request.Method.GET, "http://159.203.61.141:8080/parkhappy/api/lot_id/"+MainActivity.selectedLotID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject obj = new JSONObject(response);
                    ArrayList<LotPriceBreakDownList> pricesarr = new ArrayList<>();
                    ArrayList<String> featuresarr = new ArrayList<>();

                    int id = obj.getInt("id");
                    int number = obj.getInt("lotNumber");
                    String name = obj.getString("lotName");
                    double latitude = obj.getDouble("latitude");
                    double longitude = obj.getDouble("longitude");

                    JSONArray prices = obj.getJSONArray("lotPriceBreakDownList");
                    for (int j = 0; j < prices.length(); j++) {
                        JSONObject price = prices.getJSONObject(j);
                        double amount = price.getDouble("amount");
                        String amountText = price.getString("amountText");
                        double duration = price.getDouble("duration");
                        JSONObject lotPriceDurationGroup = price.getJSONObject("lotPriceDurationGroup");
                        String label = lotPriceDurationGroup.getString("label");
                        pricesarr.add(new LotPriceBreakDownList(amount, amountText, duration, label));
                    }

                    JSONArray lotFeatures = obj.getJSONArray("lotFeatures");
                    for (int j = 0; j < lotFeatures.length(); j++) {
                        String label = lotFeatures.getJSONObject(j).getString("label");
                        featuresarr.add(label);
                    }

                    JSONObject addr = obj.getJSONObject("lotAddress");
                    String address1 = addr.getString("address1");
                    String address2 = addr.getString("address2");
                    String city = addr.getString("city");
                    String country = addr.getString("country");

                    JSONObject lotPriceBasic = obj.getJSONObject("lotPriceBasic");
                    double basicprice = lotPriceBasic.optDouble("priceTotalAmount");

                    Lot myLot = new Lot(id, name, address1, city, country, latitude, longitude, basicprice);
                    myLot.pricelist=pricesarr;
                    myLot.lotFeatures=featuresarr;
                    myLot.number=number;
                    myLot.address2=address2;

                    fillData(myLot);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MyApplication.getAppContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    getlot();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MyApplication.getAppContext(), "Something went wrong!, Reloading", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                getlot();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
//                params.put("latitude", String.valueOf(lat));
//                params.put("longitude", String.valueOf(lng));
                params.put("latitude", "43.641895");
                params.put("longitude", "-79.374236");
                return params;
            }
        };

        //to retry
        sr.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the queue
        Volley.newRequestQueue(MyApplication.getAppContext()).add(sr);
    }

    void fillData(Lot l){

        //sort an arraylist of objects by a property

        Collections.sort(l.pricelist, new Comparator<LotPriceBreakDownList>() {
            @Override public int compare(LotPriceBreakDownList p1, LotPriceBreakDownList p2) {
                return (int) (p1.amount - p2.amount); // Ascending
            }

        });

        ////////

        nametxt.setText(l.name);
        addrtxt.setText(String.valueOf(l.address1+", "+l.city+", "+l.country));
        disttxt.setText(String.valueOf("$"+Math.round(l.distance)));
        setPriceAdapter(l.pricelist);
        setFeaturesAdapter(l.lotFeatures);

        //set listview hight beacause it is listview inside another one
        ViewGroup.LayoutParams params1 = priceslv.getLayoutParams();
        params1.height = 75*l.pricelist.size();//65
        priceslv.setLayoutParams(params1);

        ViewGroup.LayoutParams params2 = featureslv.getLayoutParams();
        params2.height = 80*l.lotFeatures.size();
        featureslv.setLayoutParams(params2);

    }

    void setPriceAdapter(final ArrayList<LotPriceBreakDownList> a){
        priceslv.setAdapter(null);
        adapter =new ArrayAdapter(DetailsActivity.this, R.layout.pricelayout, R.id.pricetxt, a)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView pricetimetxt = (TextView) view.findViewById(R.id.pricetimetxt);
                TextView pricetxt = (TextView) view.findViewById(R.id.pricetxt);
                pricetimetxt.setText(a.get(position).label);
                pricetxt.setText(String.valueOf("$"+Math.round(a.get(position).amount)));
                return view;
            }
        };
        priceslv.setAdapter(adapter);
    }
    void setFeaturesAdapter(final ArrayList<String> a){
        featureslv.setAdapter(null);
        adapter2 =new ArrayAdapter(DetailsActivity.this, R.layout.featurelayout, R.id.featuretxt, a)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView featuretxt = (TextView) view.findViewById(R.id.featuretxt);
                ImageView featureimg = (ImageView) view.findViewById(R.id.featureimg);
                featuretxt.setText(a.get(position));
                featureimg.setImageDrawable(getFeatureImg(a.get(position)));
                return view;
            }
        };
        featureslv.setAdapter(adapter2);
    }

    Drawable getFeatureImg(String f) {
        Drawable d;
        if (f.equals("Overnight")) {
            d = ContextCompat.getDrawable(DetailsActivity.this, R.drawable.night);
        } else if (f.equals("Lit")) {
            d = ContextCompat.getDrawable(DetailsActivity.this, R.drawable.bulb);
        } else if (f.equals("Disabled spots")) {
            d = ContextCompat.getDrawable(DetailsActivity.this, R.drawable.handicappedicn);
        } else if (f.equals("Manned")) {
            d = ContextCompat.getDrawable(DetailsActivity.this, R.drawable.restpic);
        } else if (f.equals("Valet")) {
            d = ContextCompat.getDrawable(DetailsActivity.this, R.drawable.humanpic);
        } else{
            d = ContextCompat.getDrawable(DetailsActivity.this, R.drawable.openlotpic);
        }
        return d;
    }

}
