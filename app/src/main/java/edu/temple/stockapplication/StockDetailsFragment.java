package edu.temple.stockapplication;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class StockDetailsFragment extends Fragment {

    Context context;
    private StockDetailsFragment.OnFragmentInteractionListener listener;

    public StockDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock_details, container, false);
        context = getActivity();

        Bundle bundle = getArguments();
        final String stockSymbol = bundle.getString("stockSymbol");

        if(stockSymbol.equals("NoneSelected")) {
            view = inflater.inflate(R.layout.no_results_layout, container, false);
            TextView textView = (TextView) view.findViewById(R.id.textViewNoResults);
            textView.setText("No Company Selected");
        } else {
            Stock stock = getStockDetails(stockSymbol);

            TextView symbolTV = (TextView) view.findViewById(R.id.textViewDetailsSymbol);
            symbolTV.setText(stock.getSymbol());

            TextView nameTV = (TextView) view.findViewById(R.id.textViewDetailsName);
            nameTV.setText(stock.getName());

            TextView lastPriceTV = (TextView) view.findViewById(R.id.textViewDetailsLastPrice);
            lastPriceTV.setText("$" + stock.getLastPrice());

            ImageView stockGraphIV = (ImageView) view.findViewById(R.id.imageViewStockGrapgh);
            Picasso.with(getActivity()).load(stock.getGraphURL()).into(stockGraphIV);

            Button buttonDeleteStock =  (Button) view.findViewById(R.id.buttonDeleteStock);
            buttonDeleteStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteStock(stockSymbol);
                    listener.onDeleteDisplayEmpty();
                }
            });
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private Stock getStockDetails(final String stockSymbol) {
        Stock stock;
        String jsonString = "";
        String name;
        String lastPrice;
        String graphURL;
        try {
            FileInputStream fileInputStream = context.openFileInput("stockFile.txt");
            int c;
            while((c = fileInputStream.read()) != -1) {
                jsonString += Character.toString((char) c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            jsonArray = null;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String savedStockSymbol = jsonObject.getString("stockSymbol");
                // If the saved stock matches the stockSymbol then create a Stock object
                // and return it
                if(stockSymbol.equals(savedStockSymbol)) {
                    name = jsonObject.getString("name");
                    lastPrice = jsonObject.getString("lastPrice");
                    graphURL = jsonObject.getString("graphURL");

                    stock = new Stock(savedStockSymbol, name, lastPrice, graphURL);
                    return stock;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void deleteStock(String stockSymbol) {
        // String that will hold the saved stocks
        String jsonString = "";
        try {
            FileInputStream fileInputStream = context.openFileInput("stockFile.txt");
            int c;
            while((c = fileInputStream.read()) != -1) {
                jsonString += Character.toString((char) c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);

            for(int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String savedStockSymbol = jsonObject.getString("stockSymbol");
                    // If the saved stock matches the stockSymbol passed, then remove it from
                    // the JSON Array
                    if(stockSymbol.equals(savedStockSymbol)) {
                        jsonArray.remove(i);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Write the new JSON Array back to the file
            FileOutputStream fileOutputStream = context.openFileOutput("stockFile.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnFragmentInteractionListener {
        void onDeleteDisplayEmpty();
    }
}
