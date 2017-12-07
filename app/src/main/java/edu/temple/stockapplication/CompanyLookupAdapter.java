package edu.temple.stockapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CompanyLookupAdapter extends BaseAdapter {
    // Global variables
    Context context;
    ArrayList<Company> companies;

    public CompanyLookupAdapter(Context context, ArrayList<Company> companies) {
        this.context = context;
        this.companies = companies;
    }

    @Override
    public int getCount() {
        return companies.size();
    }

    @Override
    public Object getItem(int position) {
        return companies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View companyItemView = inflater.inflate(R.layout.company_item, parent, false);

        LinearLayout companyItem = (LinearLayout) companyItemView.findViewById(R.id.linearLayoutCompanyItem);
        TextView symbol = (TextView) companyItem.findViewById(R.id.textViewSymbol);
        TextView name = (TextView) companyItem.findViewById(R.id.textViewName);

        symbol.setText(companies.get(position).getSymbol());
        name.setText(companies.get(position).getName());

        return companyItemView;
    }

    public String getSymbol(int position) {
        return companies.get(position).getSymbol();
    }
}
