package com.smartpassfime.smartpassfime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.ImageButton;
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
import com.google.firebase.database.DatabaseReference;
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

    private DatabaseReference Database;
    private ImageButton nowifibutton;

    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();
    public static final String FINISH_ALERT = "finish_alert";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BackButton = findViewById(R.id.back);


        contador2 = findViewById(R.id.contador2);
        contador3 = findViewById(R.id.contador3);
        contador4 = findViewById(R.id.contador4);

        nowifibutton = findViewById(R.id.activity_estadisticas_nowifibutton);

        this.registerReceiver(this.finishAlert, new IntentFilter(FINISH_ALERT));

        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        sharedPreferences = getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences); //Escencial para que no valgan nulos los valores de isLogged y UID

        nowifibutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CD.mensajeNoInternet(ActivityEstadisticas.this);
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEstadisticas.this, MainMenu.class);
                startActivity(intent);
                finish();

            }
        });



        //contador4.setText(CONTEO_MTRABAJO);
        /*ResulString = convetirAEntero(CONTEO_GENERAL, CONTEO_CUBICULO, CONTEO_MTRABAJO);
        contador.setText(ResulString);*/
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //BroadcastReceiver encargado de cerrar este activity al ser llamado desde otro
    BroadcastReceiver finishAlert = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ActivityEstadisticas.this.finish();
        }
    };

    @Override
    public void onDestroy() {

        super.onDestroy();
        this.unregisterReceiver(finishAlert);
    }


    //Al iniciar el activity se ejecutara lo siguiente
    @Override
    protected void onStart() {
        super.onStart();
        Intent mainmenu = getIntent();
        Bundle b = mainmenu.getExtras();
        if(b!=null){
            VariablesEstaticas.CurrentUserUID =(String) b.get("El UID");
        }else{
            Log.e("TEST", "el bundle esta vacio o no lo detecto");
        }
        CargaDEDatosEstadisticas();
    }

    public void CargaDEDatosEstadisticas(){
        Database.child(VariablesEstaticas.CurrentUserUID).child("Cubiculo").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if necesario por si ya no obtiene el valor del uid por alguna razon, de esta manera no crasheara
                //Se necesitan volver a cargar los datos porque cada cambio en la base de datos activa este ondatachange
                VE.CargarDatos(sharedPreferences);
                if(VariablesEstaticas.CurrentUserUID != null && !VariablesEstaticas.CurrentUserUID.equals("")) {
                    Log.e("TEST", "VALOR: "+VariablesEstaticas.CurrentUserUID);
                    String bienvenidaTrabajo = dataSnapshot.getValue().toString();
                    CONTEO_CUBICULO = bienvenidaTrabajo;
                    contador3.setText(CONTEO_CUBICULO);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Database.child(VariablesEstaticas.CurrentUserUID).child("MesaDeTrabajo").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if necesario por si ya no obtiene el valor del uid por alguna razon, de esta manera no crasheara
                //Se necesitan volver a cargar los datos porque cada cambio en la base de datos activa este ondatachange
                VE.CargarDatos(sharedPreferences);
                if(VariablesEstaticas.CurrentUserUID != null && !VariablesEstaticas.CurrentUserUID.equals("")) {
                    Log.e("TEST", "VALOR: "+VariablesEstaticas.CurrentUserUID);
                    String bienvenidaCubiculo = dataSnapshot.getValue().toString();
                    CONTEO_MTRABAJO = bienvenidaCubiculo;
                    contador4.setText(CONTEO_MTRABAJO);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Database.child(VariablesEstaticas.CurrentUserUID).child("SalaGeneral").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if necesario por si ya no obtiene el valor del uid por alguna razon, de esta manera no crasheara
                //Se necesitan volver a cargar los datos porque cada cambio en la base de datos activa este ondatachange
                VE.CargarDatos(sharedPreferences);
                if(VariablesEstaticas.CurrentUserUID != null && !VariablesEstaticas.CurrentUserUID.equals("")) {
                    Log.e("TEST", "VALOR: "+VariablesEstaticas.CurrentUserUID);
                    String bienvenidaGeneral = dataSnapshot.getValue().toString();
                    CONTEO_GENERAL = bienvenidaGeneral;
                    contador2.setText(CONTEO_GENERAL);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        //vuelve visible o invisible el boton
        DetectaConexion CD = new DetectaConexion(this);
        CD.ConexionPorSegundos(nowifibutton);
        super.onResume();
    }

    //Detiene la busqueda de conexion a internet si se detiene la ventana
    @Override
    protected void onPause() {
        DetectaConexion CD = new DetectaConexion(this);
        CD.DetenerContador();
        super.onPause();
    }

    @Override
    protected void  onStop(){
        super.onStop();
    }
}



    /*@Override
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
    }*/

