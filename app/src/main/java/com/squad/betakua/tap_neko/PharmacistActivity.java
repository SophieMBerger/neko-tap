package com.squad.betakua.tap_neko;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.squad.betakua.tap_neko.azure.AzureInterface;
import com.squad.betakua.tap_neko.azure.AzureInterfaceException;
import com.squad.betakua.tap_neko.nfc.NFCActivity;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.squad.betakua.tap_neko.nfc.NFCActivity.NFC_ID_KEY;
import static com.squad.betakua.tap_neko.nfc.NFCActivity.NFC_REQ_CODE;

/**
 * Created by sherryuan on 2019-01-26.
 */

public class PharmacistActivity extends AppCompatActivity {

    public static final int BARCODE_REQ_CODE = 100;
    public static final String BARCODE_KEY = "barcode";
    public static final String AUDIO_KEY = "audio";
    public static final int AUDIO_REQ_CODE = 101;

    private TableRow audioRecorderButton;
    private String outputFile;
    private boolean hasAudio = false;
    // private File flacFile;

    private TableRow barcodeScannerButton;
    private String barcodeId;
    private boolean hasBarcode = false;

    private TableRow nfcButton;
    private TableRow submitButton;

    private String nfcId;
    private boolean hasNfcId = false;

    private TextView textBarcode;
    private TextView textNFC;
    private TextView textAudio;
    private TextView textSubmit;

    private LottieAnimationView lottieBarcode;
    private LottieAnimationView lottieNFC;
    private LottieAnimationView lottieAudio;
    private LottieAnimationView lottieSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist);
        initAudioRecorderButton();
        initBarcodeScannerButton();
        initNFCButton();
        initSubmitButton();
        refreshSubmitButton();

        textBarcode = findViewById(R.id.scan_text);
        textNFC = findViewById(R.id.nfc_text);
        textAudio = findViewById(R.id.audio_text);
        textSubmit = findViewById(R.id.submit_text);

        lottieBarcode = findViewById(R.id.check_barcode);
        lottieNFC = findViewById(R.id.check_nfc);
        lottieAudio = findViewById(R.id.check_audio);
        lottieSubmit = findViewById(R.id.check_submit);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == BARCODE_REQ_CODE && resultCode == RESULT_OK) {
            barcodeId = data.getStringExtra(BARCODE_KEY);
            hasBarcode = true;
            // Toast.makeText(this, barcodeId, Toast.LENGTH_SHORT).show();

            // change colors
            textBarcode.setTextColor(Color.parseColor("#FFFFFF"));
            barcodeScannerButton.setBackgroundColor(Color.parseColor("#6dcc5b"));
            lottieBarcode.playAnimation();

            refreshSubmitButton();
        } else if (requestCode == AUDIO_REQ_CODE && resultCode == RESULT_OK) {
            // get audio
            hasAudio = true;
            // flacFile = (File) data.getExtras().get(AUDIO_KEY);
            Toast.makeText(this, "got audio", Toast.LENGTH_SHORT).show();

            // change colors
            textAudio.setTextColor(Color.parseColor("#FFFFFF"));
            audioRecorderButton.setBackgroundColor(Color.parseColor("#6dcc5b"));
            lottieAudio.playAnimation();
            refreshSubmitButton();
        } else if (requestCode == NFC_REQ_CODE && resultCode == RESULT_OK) {
            // get NFC id
            nfcId = data.getStringExtra(NFC_ID_KEY);
            hasNfcId = true;

            // change colors
            textNFC.setTextColor(Color.parseColor("#FFFFFF"));
            nfcButton.setBackgroundColor(Color.parseColor("#6dcc5b"));
            lottieNFC.playAnimation();
            refreshSubmitButton();
        }
    }

    private void initAudioRecorderButton() {
        audioRecorderButton = findViewById(R.id.audio_recorder_button);
        audioRecorderButton.setOnClickListener((View view) -> {
            Intent audioRecorderIntent = new Intent(getApplicationContext(), AudioRecorderActivity.class);
            startActivityForResult(audioRecorderIntent, AUDIO_REQ_CODE);
        });
    }

    private void initBarcodeScannerButton() {
        barcodeScannerButton = findViewById(R.id.barcode_scanner_button);
        barcodeScannerButton.setOnClickListener((View view) -> {
            Intent barcodeScannerIntent = new Intent(getApplicationContext(), BarcodeScannerActivity.class);
            startActivityForResult(barcodeScannerIntent, BARCODE_REQ_CODE);
        });
    }

    private void initNFCButton() {
        nfcButton = findViewById(R.id.nfc_button);
        nfcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NFCActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initSubmitButton() {
        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener((View view) -> {
            try {
                textSubmit.setTextColor(Color.parseColor("#FFFFFF"));
                submitButton.setBackgroundColor(Color.parseColor("#6dcc5b"));
                lottieSubmit.playAnimation();
                AzureInterface.getInstance().uploadAudio(nfcId, new FileInputStream(outputFile), -1);
                AzureInterface.getInstance().writeInfoItem(nfcId, barcodeId, "", "https://www.youtube.com/watch?v=uGkbreu169Q");

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // here we reset everything
                    }
                }, 1500);

            } catch (AzureInterfaceException | FileNotFoundException e) {
                e.printStackTrace();
            }
        });

    }

    // submit button should only be enabled if both audio, barcode, and NFC have been prepared
    private void refreshSubmitButton() {
        if (hasAudio && hasBarcode && hasNfcId) {
            submitButton.setEnabled(true);
        } else {
            submitButton.setEnabled(false);
        }
    }

    public void initNfcButton() {
        nfcButton = findViewById(R.id.nfc_button);

        nfcButton.setOnClickListener((View view) -> {
            Intent intent = new Intent(PharmacistActivity.this, NFCActivity.class);
            startActivityForResult(intent, NFC_REQ_CODE);
        });
    }
}
