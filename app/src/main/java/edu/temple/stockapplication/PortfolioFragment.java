package edu.temple.stockapplication;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;

public class PortfolioFragment extends Fragment {

    private PortfolioFragment.OnFragmentInteractionListener listener;

    public PortfolioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;

        Context context = getActivity();

        // String that will hold saved stocks
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

        JSONArray fileStocks;

        try{
            fileStocks = new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            fileStocks = new JSONArray();
        }

        if(fileStocks.length() > 0) {
            // If there are saved stocks, then display them
            view = inflater.inflate(R.layout.fragment_portfolio, container, false);
            ArrayList<Company> companies = new ArrayList<>();

            for(int i = 0; i < fileStocks.length(); i++) {
                try {
                    JSONObject jsonObject = fileStocks.getJSONObject(i);
                    String stockSymbol = jsonObject.getString("stockSymbol");
                    String name = jsonObject.getString("name");

                    Company company = new Company(stockSymbol, name);

                    companies.add(company);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ListView companyList = (ListView) view.findViewById(R.id.listViewSavedCompanyList);
            CompanyLookupAdapter companyLookupAdapter = new CompanyLookupAdapter(context, companies);

            companyList.setAdapter(companyLookupAdapter);

            companyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String stockSymbol = ((TextView) view.findViewById(R.id.textViewSymbol)).getText().toString();
                    listener.displayStockDetails(stockSymbol);
                }
            });
        } else {
            // If no saved stocks exist, then display a no saved stocks found message
            view = inflater.inflate(R.layout.no_results_layout, container, false);
            TextView textView = (TextView) view.findViewById(R.id.textViewNoResults);
            textView.setText("No Saved Stocks Found");
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnFragmentInteractionListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {
        void displayStockDetails(String stockSymbol);
    }
}
