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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class CompanyListFragment extends Fragment {

    private CompanyListFragment.OnFragmentInteractionListener listener;

    View view;
    boolean emptyList = false;
    ListView companyList;
    Context context;

    public CompanyListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String stockSymbol = bundle.getString("stockSymbol");
        context = getActivity();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_company_list, container, false);

        getCompanies(stockSymbol);

        if(emptyList) {
            // Display the No Results Found layout if no results were found
            view = inflater.inflate(R.layout.no_results_layout, container, false);
        } else {
            companyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String stockSymbol = ((TextView) view.findViewById(R.id.textViewSymbol)).getText().toString();
                    listener.addStock(stockSymbol);
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

    private void getCompanies(final String stockSymbol) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Company> companies = new ArrayList<>();
                try {
                    URL companyListURL;
                    // Create the URL for the stock
                    companyListURL = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=" + stockSymbol);

                    // Create the InputStream for the URL
                    InputStream inputStream = companyListURL.openStream();

                    // Create the InputStreamReader that will read bytes from the URL
                    // and convert them to characters
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                    // Create the BufferedReader that will read the characters from
                    // the InputStreamReader
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    // String that will contain the JSON data from the URL
                    String response = "";

                    // String that will hold the temporary line data from the URL
                    String temporaryResponse;

                    // Loop through the response from the URL and store it in the response String
                    temporaryResponse = bufferedReader.readLine();
                    while(temporaryResponse != null) {
                        response += temporaryResponse;
                        temporaryResponse = bufferedReader.readLine();
                    }

                    // Close connection with the InputStreamReader and InputStream
                    inputStreamReader.close();
                    inputStream.close();

                    // Convert the response String to a JSON array
                    JSONArray companyArray = new JSONArray(response);

                    if(companyArray.length() > 0) {
                        try {
                            emptyList = false;
                            for(int i = 0; i < companyArray.length(); i++) {
                                JSONObject stockObject = companyArray.getJSONObject(i);
                                String symbol = stockObject.getString("Symbol");
                                String name = stockObject.getString("Name");

                                Company company = new Company(symbol, name);

                                companies.add(company);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        emptyList = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // ListView that will display the list of companies
                companyList = (ListView) view.findViewById(R.id.listViewCompanyList);

                // Adapter that will add the companies to a list
                CompanyLookupAdapter companyLookupAdapter = new CompanyLookupAdapter(context, companies);

                // Set the list adapter to display the list
                companyList.setAdapter(companyLookupAdapter);
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnFragmentInteractionListener {
        void addStock(String stockSymbol);
    }
}
