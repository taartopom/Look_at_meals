package com.sandra.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_STORAGE = 1;
    private static final int RESULT_LOAD_IMAGE = 0;


    TextView txtResult;
    Button btnScan,btnReset;
    ImageView scanZBar;

    BarcodeDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtresult);
        btnScan = findViewById(R.id.btnScan);
        btnReset = findViewById(R.id.btnReset);
        scanZBar = findViewById(R.id.scanZBar);

        /**
         * Init du detecteur
         */
        detector =  new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()){
            txtResult.setText("Détection impossible");

        }

        /**
         * requete de stoquage permissible
         */

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_STORAGE);
        }

        /**
         * Ecouteur sur les boutons
         */

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pour charger l'image du code barre
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                startActivityForResult(intent, RESULT_LOAD_IMAGE);

            }
        });


        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pour vider l'image et le textView de txtResult

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && requestCode ==  RESULT_OK && null != data){
            Uri selectedImage=  data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor =  getContentResolver().query(selectedImage, filePathColumn, null ,null, null);
            cursor.moveToFirst();
            int columnIndex =  cursor.getColumnIndex(filePathColumn[0]);
            String picturePath =  cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            scanZBar.setImageBitmap(bitmap);

            //chargement des datas
            processData(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_STORAGE:{
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission accordée !",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,"Permission refusée ! !",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
    /**
     * Procedure du code barre
     */
    private void processData(Bitmap myBitmap){
        Frame cadre =  new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes =  detector.detect(cadre);

        Barcode thisCode =  barcodes.valueAt(0);
        txtResult.setText(thisCode.rawValue);
    }
}
