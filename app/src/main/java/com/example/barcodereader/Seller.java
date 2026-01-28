package com.example.barcodereader;

public class Seller {
    private String name;
    private double price;
    private String link;

    public Seller(String name, String priceText, String link) {
        this.name = name;
        this.price = parsePrice(priceText);
        this.link = link;
    }

    private double parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isEmpty()) return 0.0;

        try {
            String clean = rawPrice.replaceAll("[^0-9,.]", "");

            if (clean.contains(",")) {
                clean = clean.replace(".", "");
                clean = clean.replace(",", ".");
            }
            else {
                clean = clean.replace(".", "");
            }

            return Double.parseDouble(clean);

        } catch (Exception e) {
            return 0;
        }
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getLink() {
        return link;
    }

}