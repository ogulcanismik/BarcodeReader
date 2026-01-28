package com.example.barcodereader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ProductDetails extends AppCompatActivity {

    TextView barcodeText, productNameText;
    ImageView productImage;
    RecyclerView recyclerView;
    String barcodeData;

    String apiKey = BuildConfig.JoJAPI_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productImage = findViewById(R.id.productImage);
        productNameText = findViewById(R.id.productNameText);
        barcodeText = findViewById(R.id.barcodeText);
        recyclerView = findViewById(R.id.sellerRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        barcodeData = getIntent().getStringExtra("BARCODE_KEY");
        barcodeText.setText(barcodeData);

        fetchProductWithHttp(barcodeData);
    }

    private void fetchProductWithHttp(String barcode) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://camgoz.jojapi.net/api/external/search?query=" + barcode + "&marketPrices=false";
        String headerName = "X-JoJAPI-Key";
        String myApiKey = apiKey;

        Request request = new Request.Builder()
                .url(url)
                .addHeader(headerName, myApiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        productNameText.setText("Connection Error!");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseAndShowData(myResponse);
                    }
                });
            }
        });
    }

    private void parseAndShowData(String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            if (jsonArray.length() > 0) {
                JSONObject productObj = jsonArray.getJSONObject(0);

                String name = productObj.optString("name", "Unknown Product");
                String imageUrl = productObj.optString("imageUrl", "");

                productNameText.setText(name);

                if (!imageUrl.isEmpty()) {
                    Glide.with(ProductDetails.this).load(imageUrl).into(productImage);
                }

                startScraping(name);

            } else {
                productNameText.setText("Could Not Find Product");
            }

        } catch (Exception e) {

        }
    }

    private void startScraping(String query) {
        Toast.makeText(this, "Looking for sellers...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            GoogleScraper service = new GoogleScraper();
            List<Seller> foundSellers = service.searchProduct(query);

            runOnUiThread(() -> {
                if (foundSellers != null && !foundSellers.isEmpty()) {
                    SellerAdapter adapter = new SellerAdapter(foundSellers);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(ProductDetails.this, "Failed to find sellers.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

}