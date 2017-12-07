package edu.temple.stockapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements CompanyListFragment.OnFragmentInteractionListener, PortfolioFragment.OnFragmentInteractionListener, StockDetailsFragment.OnFragmentInteractionListener {

    boolean twoPanes;
    final String INTERNAL_FILE_NAME = "stockFile.txt";
    File file;
    Context context;
    boolean exists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        // Needed to allow receiving of JSON data
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create a file
        file = new File(getFilesDir(), INTERNAL_FILE_NAME);
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start service to update stocks every 60 seconds
        Intent updateStockServiceIntent = new Intent(MainActivity.this, UpdateStocksService.class);
        startService(updateStockServiceIntent);

        // Check if two panes are being displayed
        twoPanes = (findViewById(R.id.stock_details_fragment) != null);

        PortfolioFragment portfolioFragment = new PortfolioFragment();
        loadFragment(R.id.main_fragment, portfolioFragment, true);

        if(twoPanes) {
            StockDetailsFragment stockDetailsFragment = new StockDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("stockSymbol", "NoneSelected");
            stockDetailsFragment.setArguments(bundle);
            loadFragment(R.id.stock_details_fragment, stockDetailsFragment, true);
        } else {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.stock_details_fragment);
            if(fragment != null) {
                getFragmentManager()
                        .beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        // Set up Search View
        SearchView searchView = (SearchView) menu.findItem(R.id.search_stock).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CompanyListFragment companyListFragment = new CompanyListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("stockSymbol", query);
                companyListFragment.setArguments(bundle);
                if(twoPanes) {
                    loadFragment(R.id.stock_details_fragment, companyListFragment, true);
                } else {
                    loadFragment(R.id.main_fragment, companyListFragment, true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    // Load fragment in a specified frame
    private void loadFragment(int paneId, Fragment fragment, boolean placeOnBackStack) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(paneId, fragment);
        if(placeOnBackStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    @Override
    public void addStock(String stockSymbol) {
        getStockDetails(stockSymbol);

        PortfolioFragment portfolioFragment = new PortfolioFragment();

        loadFragment(R.id.main_fragment, portfolioFragment, true);
    }

    private void getStockDetails(final String stockSymbol) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String name;
                String lastPrice;
                String graphURL;
                try {
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

                    Log.d("stockFile.txt", jsonString);
                    JSONArray fileStocks;
                    if(jsonString.length() > 0) {
                        fileStocks = new JSONArray(jsonString);
                    } else {
                        fileStocks = new JSONArray();
                    }

                    // Check if the stock already exists
                    for(int i = 0; i < fileStocks.length(); i++) {
                        JSONObject jsonObject = fileStocks.getJSONObject(i);
                        String savedStockSymbol = jsonObject.getString("stockSymbol");
                        if(stockSymbol.equals(savedStockSymbol)) {
                            exists = true;
                            return;
                        }
                    }

                    URL stockDetailsURL;

                    stockDetailsURL = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + stockSymbol);

                    InputStream inputStream = stockDetailsURL.openStream();

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String response = "";
                    String temporaryResponse;

                    temporaryResponse = bufferedReader.readLine();
                    while(temporaryResponse != null) {
                        response += temporaryResponse;
                        temporaryResponse = bufferedReader.readLine();
                    }

                    // Close connection with InputStreamReader and InputStream
                    inputStream.close();
                    inputStreamReader.close();

                    JSONObject stockObject = new JSONObject(response);
                    name = stockObject.getString("Name");
                    lastPrice = stockObject.getString("LastPrice");
                    graphURL = "https://finance.google.com/finance/getchart?p=5d&q=" + stockSymbol;

                    // Create a JSON Object of the new stock
                    JSONObject fileEntryObject = new JSONObject();
                    fileEntryObject.put("stockSymbol", stockSymbol);
                    fileEntryObject.put("name", name);
                    fileEntryObject.put("lastPrice", lastPrice);
                    fileEntryObject.put("graphURL", graphURL);

                    fileStocks.put(fileEntryObject);

                    FileOutputStream fileOutputStream = context.openFileOutput("stockFile.txt", MODE_PRIVATE);

                    fileOutputStream.write(fileStocks.toString().getBytes());

                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(exists) {
            Toast.makeText(context, "Stock Already Exists", Toast.LENGTH_LONG).show();
            exists = false;
        }
    }

    @Override
    public void displayStockDetails(String stockSymbol) {
        StockDetailsFragment stockDetailsFragment = new StockDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stockSymbol", stockSymbol);
        stockDetailsFragment.setArguments(bundle);

        if(twoPanes) {
            loadFragment(R.id.stock_details_fragment, stockDetailsFragment, true);
        } else {
            loadFragment(R.id.main_fragment, stockDetailsFragment, true);
        }
    }

    @Override
    public void onDeleteDisplayEmpty() {
        if(twoPanes) {
            StockDetailsFragment stockDetailsFragment = new StockDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("stockSymbol", "NoneSelected");
            stockDetailsFragment.setArguments(bundle);
            loadFragment(R.id.stock_details_fragment, stockDetailsFragment, true);
        }

        PortfolioFragment portfolioFragment = new PortfolioFragment();
        loadFragment(R.id.main_fragment, portfolioFragment, true);

    }
}
