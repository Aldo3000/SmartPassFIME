package com.smartpassfime.smartpassfime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityAjustes extends AppCompatActivity {

   private Button cambiodepssword, reiniciodeestadisticas;
    BaseDeDatos BD = new BaseDeDatos();
    private DatabaseReference Database;
    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();
    MetodosUtiles MU = new MetodosUtiles();
    private ImageButton nowifibutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        //Persistencia de variables
        sharedPreferences= getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences);

        nowifibutton = findViewById(R.id.activity_estadisticas_nowifibutton);
        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        nowifibutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CD.mensajeNoInternet(ActivityAjustes.this);
            }
        });


        cambiodepssword = findViewById(R.id.cambiarpassword);
        reiniciodeestadisticas = findViewById(R.id.reinciciodeestadistcas);

        cambiodepssword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityAjustes.this, CambiarPassword.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        reiniciodeestadisticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityAjustes.this, MainMenu.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
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
