package com.example.implicitintents;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private EditText websiteEditText;
    private EditText locationEditText;
    private EditText shareEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        websiteEditText = findViewById(R.id.website_edittext);
        locationEditText = findViewById(R.id.location_edittext);
        shareEditText = findViewById(R.id.share_edittext);
        showReceivedUri(getIntent());
    }

    public void openWebsite(View view) {
        String address = readRequired(websiteEditText);
        if (address == null) return;
        if (!address.contains(":")) address = "https://" + address;
        Uri uri = Uri.parse(address).normalizeScheme();
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getHost().isEmpty()
                || address.matches(".*\\s.*")) {
            websiteEditText.setError(getString(R.string.invalid_website));
            websiteEditText.requestFocus();
            return;
        }
        launchViewIntent(new Intent(Intent.ACTION_VIEW, uri), R.string.no_browser);
    }

    public void openLocation(View view) {
        String location = readRequired(locationEditText);
        if (location == null) return;
        // Encode spaces, accents and characters such as & and # in the query.
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
        launchViewIntent(new Intent(Intent.ACTION_VIEW, uri), R.string.no_maps);
    }

    public void shareText(View view) {
        String text = readRequired(shareEditText);
        if (text == null) return;
        try {
            new ShareCompat.IntentBuilder(this)
                    .setType("text/plain")
                    .setText(text)
                    .setChooserTitle(R.string.share_chooser)
                    .startChooser();
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.no_share_app, Toast.LENGTH_LONG).show();
        }
    }

    private String readRequired(EditText field) {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) {
            field.setError(getString(R.string.required_input));
            field.requestFocus();
            return null;
        }
        field.setError(null);
        return value;
    }

    private void launchViewIntent(Intent intent, int errorMessage) {
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException exception) {
                // The receiving application may have been removed in the meantime.
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showReceivedUri(intent);
    }

    private void showReceivedUri(Intent intent) {
        TextView receivedUri = findViewById(R.id.received_uri);
        Uri uri = intent.getData();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && uri != null) {
            receivedUri.setText(getString(R.string.received_link, uri.toString()));
            receivedUri.setVisibility(View.VISIBLE);
        } else {
            receivedUri.setVisibility(View.GONE);
        }
    }
}
