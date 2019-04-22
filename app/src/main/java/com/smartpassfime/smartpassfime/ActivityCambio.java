package com.smartpassfime.smartpassfime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityCambio extends AppCompatActivity {

    private Button BackButton, YesCambio, NoCambio;
    BaseDeDatos BD = new BaseDeDatos();
    private DatabaseReference Database;
    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();
    MetodosUtiles MU = new MetodosUtiles();
    private ImageButton nowifibutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio);
        YesCambio = findViewById(R.id.yesalcambiopormexico);
        NoCambio = findViewById(R.id.noalcambiopormexico);

        nowifibutton = findViewById(R.id.activity_estadisticas_nowifibutton);
        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        nowifibutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CD.mensajeNoInternet(ActivityCambio.this);
            }
        });
        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        //Persistencia de variables
        sharedPreferences= getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences);
        BackButton = findViewById(R.id.back);

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityCambio.this, MainMenu.class);
                startActivity(intent);
                finish();

            }
        });

        YesCambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityCambio.this);
                builder.setPositiveButton(
                        "SI",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent cerrarMainMenu = new Intent(MainMenu.FINISH_ALERT); //Vinculamos la variable de IngresarActivity acá
                                ActivityCambio.this.sendBroadcast(cerrarMainMenu); //Llamamos al Broadcast para que lo cierre
                                Database.child(VariablesEstaticas.CurrentUserUID).child("CuentaAbierta").setValue("false"); //Cuenta ahora está cerrada
                                VE.GuardarDatos(sharedPreferences, false, ""); //Guardamos los cambios, la variable islogged y el uid ahora tienen valores default
                                Intent ingresar = new Intent(ActivityCambio.this, ActivityIngresar1.class);
                                startActivity(ingresar);
                                finish(); //Terminamos
                            }
                        });
                builder.setNegativeButton(
                        "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }

                        });
                //Centrar Titulo
                TextView title = new TextView(ActivityCambio.this);
                title.setText("¿Realmente deseas cerrar sesión?");
                //title.setBackgroundColor(Color.BLACK);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                //title.setTextColor(Color.WHITE);
                title.setTextColor(Color.BLACK);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                AlertDialog dialog = builder.create();
                dialog.show();
                //centrar los botones
                Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);


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

