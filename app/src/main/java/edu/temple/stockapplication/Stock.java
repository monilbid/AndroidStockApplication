package edu.temple.stockapplication;

public class Stock extends Company{
    String lastPrice;
    String graphURL;

    public Stock(String symbol, String name, String lastPrice, String graphURL) {
        super.setSymbol(symbol);
        super.setName(name);
        this.lastPrice = lastPrice;
        this.graphURL = graphURL;
    }

    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getGraphURL() {
        return graphURL;
    }

    public void setGraphURL(String graphURL) {
        this.graphURL = graphURL;
    }
}
