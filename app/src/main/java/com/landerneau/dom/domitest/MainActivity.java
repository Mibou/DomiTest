package com.landerneau.dom.domitest;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    // View
    private Button bSave;
    private Button bScan;
    private TextView tBarcode;
    private EditText tPrice;
    private LinearLayout lBarcodePrice;

    // File management
    private File documentsDirectory = Environment.getExternalStorageDirectory();
    private File filename = new File(documentsDirectory, "gencode_prix.csv");
    private FileOutputStream outputStream;

    // Write gencode and price to the given file
    protected void AppendGencodePrice(String gencode, Double price) {
        try {
            // Prepare line
            String gencodePrice = String.format("%s;%.2f", gencode, price);

            // Check directory
            if(!documentsDirectory.exists())
                documentsDirectory.mkdirs();

            // Write line
            outputStream = new FileOutputStream(filename, true);
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(gencodePrice);
            writer.close();
            outputStream.flush();
            outputStream.close();

            // For MTP refresh
            MediaScannerConnection.scanFile(this, new String[] {filename.toString()}, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reset form
        ResetForm();
    }

    // Clear input, hide price input form
    protected void ResetForm() {
        tPrice.setText("");
        tBarcode.setText("");
        lBarcodePrice.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View components
        tPrice = (EditText) findViewById(R.id.txtPrice);
        tBarcode = (TextView) findViewById(R.id.txtBarcode);
        lBarcodePrice = (LinearLayout) findViewById(R.id.layBarcodePrice);
        bSave = (Button) findViewById(R.id.btnSave);
        bScan = (Button) findViewById(R.id.btnScan);

        // Save button listener
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppendGencodePrice(ReadGencode(), ReadPrice());
            }
        });

        // Scan button listener
        bScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.addExtra("SCAN_FORMATS", "EAN_13,EAN_8,UPC_A,UPC_E");
                integrator.initiateScan();
            }
        });

        // Price changed listener
        tPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { CheckFieldsForSave(); }
        });

    }

    // Get price
    private Double ReadPrice() {
        try {
            return Double.valueOf(tPrice.getText().toString());
        }catch(NumberFormatException e) {
            return -1d;
        }
    }

    // Get gencode
    public String ReadGencode() {
        return MainActivity.this.tBarcode.getText().toString();
    }

    // Check fields in order to enable save button
    private void CheckFieldsForSave() {
        bSave.setEnabled(false);
        if(!tBarcode.getText().toString().isEmpty() || !tPrice.getText().toString().isEmpty())
            if (ReadPrice() > 0)
                bSave.setEnabled(true);
    }


    // When gencode is scanned, show price input form
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            if(re != null) {
                tBarcode.setText(re);
                lBarcodePrice.setVisibility(View.VISIBLE);
                CheckFieldsForSave();
            }
        }
    }
}
