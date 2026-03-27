package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Stock;

import java.io.IOException;
import java.util.List;

/**
 * Simple entry point that loads stock data from CSV and prints a preview.
 */
public class Main {

    public static void main(String[] args) {
        StockCsvLoader stockCsvLoader = new StockCsvLoader();

        try {
            List<Stock> stocks = stockCsvLoader.loadFromResource("data/sp500.csv");
            System.out.println("Loaded stocks: " + stocks.size());

            int previewCount = Math.min(5, stocks.size());
            for (int index = 0; index < previewCount; index++) {
                Stock stock = stocks.get(index);
                System.out.println((index + 1) + ". " + stock.getSymbol()
                        + " - " + stock.getCompanyName()
                        + " - " + stock.getSalesPrice());
            }
        } catch (IOException exception) {
            System.err.println("Failed to load stock CSV: " + exception.getMessage());
        }
    }
}


