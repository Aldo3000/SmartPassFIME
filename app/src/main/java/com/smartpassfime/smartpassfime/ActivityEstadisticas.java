package com.smartpassfime.smartpassfime;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class ActivityEstadisticas extends AppCompatActivity {

    private Button BackButton;
    private TextView contador, contador2, contador3, contador4;
    private String CONTEO_GENERAL = "";
    private String CONTEO_CUBICULO = "";
    private String CONTEO_MTRABAJO = "";
    private String ResulString = "";
    public int num = 0, num2 = 0, num3 = 0, total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        BackButton = findViewById(R.id.back);


        contador2 = findViewById(R.id.contador2);
        contador3 = findViewById(R.id.contador3);
        contador4 = findViewById(R.id.contador4);

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEstadisticas.this, MainMenu.class);
                startActivity(intent);
                finish();

            }
        });



        contador4.setText(CONTEO_MTRABAJO);
        /*ResulString = convetirAEntero(CONTEO_GENERAL, CONTEO_CUBICULO, CONTEO_MTRABAJO);
        contador.setText(ResulString);*/
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("General").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String bienvenidaGeneral = dataSnapshot.getValue().toString();
                CONTEO_GENERAL = bienvenidaGeneral;
                contador2.setText(CONTEO_GENERAL);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Cubiculo").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String bienvenidaCubiculo = dataSnapshot.getValue().toString();
                CONTEO_CUBICULO = bienvenidaCubiculo;
                contador3.setText(CONTEO_CUBICULO);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Mesa de Trabajo").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String bienvenidaTrabajo = dataSnapshot.getValue().toString();
                CONTEO_MTRABAJO = bienvenidaTrabajo;
                contador4.setText(CONTEO_MTRABAJO);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public String convetirAEntero(String General, String Cubiculo, String Trabajo){

        num = Integer.parseInt(General);
        num2 = Integer.parseInt(Cubiculo);
        num3 = Integer.parseInt(Trabajo);
        total = num + num2 + num3;

        ResulString = Integer.toString(total);
        return (ResulString);
    }
}
