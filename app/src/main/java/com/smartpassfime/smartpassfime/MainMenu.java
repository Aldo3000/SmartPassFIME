package com.smartpassfime.smartpassfime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainMenu extends AppCompatActivity {

    private Button buttonnfc, buttonqr, buttonestasdisticas, buttoncambio, buttonajustes, buttoncontac;
    private TextView usuarioBienvenidoText;
    Animation fromButtom, fromButtom1;

    private DatabaseReference Database;
    private ImageButton nowifibutton;

    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();
    public static final String FINISH_ALERT = "finish_alert";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        buttonnfc = findViewById(R.id.buttonNfc);
        buttonqr = findViewById(R.id.buttonQr);
        buttonestasdisticas = findViewById(R.id.buttonEstadisticas);
        buttoncambio = findViewById(R.id.buttonCambio);
        buttonajustes = findViewById(R.id.buttonAjstes);
        buttoncontac = findViewById(R.id.buttonContacto);
        usuarioBienvenidoText = findViewById(R.id.Textbienveida);
        this.registerReceiver(this.finishAlert, new IntentFilter(FINISH_ALERT));

        nowifibutton = findViewById(R.id.activity_main_menu_nowifibutton);

        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        sharedPreferences = getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences); //Escencial para que no valgan nulos los valores de isLogged y UID

        nowifibutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CD.mensajeNoInternet(MainMenu.this);
            }
        });

        fromButtom = AnimationUtils.loadAnimation(this,R.anim.anim);
        fromButtom1 = AnimationUtils.loadAnimation(this,R.anim.anim1);
        buttonnfc.setAnimation(fromButtom);
        buttonestasdisticas.setAnimation(fromButtom);
        buttonajustes.setAnimation(fromButtom);
        buttonqr.setAnimation(fromButtom1);
        buttoncambio.setAnimation(fromButtom1);
        buttoncontac.setAnimation(fromButtom1);

        buttonnfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ActivityNFC.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        buttonqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ActivityQR.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        buttonestasdisticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ActivityEstadisticas.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        buttoncambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ActivityCambio.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        buttonajustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ActivityAjustes.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        buttoncontac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, ActivityContacto.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });






    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //BroadcastReceiver encargado de cerrar este activity al ser llamado desde otro
    BroadcastReceiver finishAlert = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            MainMenu.this.finish();
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
        Saludos();
    }

    public void Saludos(){
        Database.child(VariablesEstaticas.CurrentUserUID).child("Matriculaa").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if necesario por si ya no obtiene el valor del uid por alguna razon, de esta manera no crasheara
                //Se necesitan volver a cargar los datos porque cada cambio en la base de datos activa este ondatachange
                VE.CargarDatos(sharedPreferences);
                if(VariablesEstaticas.CurrentUserUID != null && !VariablesEstaticas.CurrentUserUID.equals("")) {
                    Log.e("TEST", "VALOR: "+VariablesEstaticas.CurrentUserUID);
                    String bienvenida = dataSnapshot.getValue().toString();
                    usuarioBienvenidoText.setText("Bienvenido\n" + bienvenida);
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
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Matriculaa").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String bienvenida = dataSnapshot.getValue().toString();
                usuarioBienvenidoText.setText("Bienvenido\n"+bienvenida);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/


