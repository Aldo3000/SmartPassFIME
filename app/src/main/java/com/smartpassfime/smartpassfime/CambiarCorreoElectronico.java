package com.smartpassfime.smartpassfime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CambiarCorreoElectronico extends AppCompatActivity {

    private ProgressDialog Progress;
    private DatabaseReference Database;
    private Button CambiarContraseña;
    MetodosUtiles MU = new MetodosUtiles();
    BaseDeDatos BD = new BaseDeDatos();
    private ImageButton nowifibutton;
    private EditText confirmarPassword;
    private EditText correoNuevo;
    private TextView correoActual;
    private Button CambiarCorreo;
    private int detectorDeErroresPassword = 0, detectorDeErroresCorreoNuevo = 0;
    public String correoNuevoStr;
    public String passwordCorreoStr;
    public String ContraseñaReal;

    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_correo_electronico);

        correoActual = findViewById(R.id.correoelectronicoactiual);
        correoNuevo = findViewById(R.id.nuevocorreo);
        confirmarPassword = findViewById(R.id.confirmarcontra);
        CambiarCorreo = findViewById(R.id.aceptarvendermialma);
        Progress = new ProgressDialog(this);

        Progress.setCancelable(false);
        Progress.setCanceledOnTouchOutside(false);

        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);

        sharedPreferences = getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences);

        nowifibutton = findViewById(R.id.activity_estadisticas_nowifibutton);
        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        nowifibutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CD.mensajeNoInternet(CambiarCorreoElectronico.this);
            }
        });

        CambiarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correoNuevoStr = correoNuevo.getText().toString().toLowerCase().trim();
                passwordCorreoStr = confirmarPassword.getText().toString().trim();
                detectorDeErroresPassword = BD.ValidarContraseña(passwordCorreoStr, detectorDeErroresPassword, confirmarPassword, "Se requiere contraseña");
                detectorDeErroresCorreoNuevo = BD.ValidarCorreo(correoNuevoStr, detectorDeErroresCorreoNuevo, correoNuevo, "Se requiere" +
                        " el nuevo correo");
                if (detectorDeErroresPassword == 0 && detectorDeErroresCorreoNuevo == 0) {
                    Progress.setMessage("Modificando, por favor espere");
                    Progress.show();
                    RevisarMatchCorreo(); //Metodo encargado de ver si lo que se escribio coincide
                }
            }
        });
    }
    public void RevisarMatchCorreo(){
        Database.child(VariablesEstaticas.CurrentUserUID).child("Contraseña").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ContraseñaReal = dataSnapshot.getValue().toString(); //Conseguimos el valor real de la contraseña del usuario
                //Si se escribio correctamente:
                if(passwordCorreoStr.equals(ContraseñaReal)){
                    Database.child(VariablesEstaticas.CurrentUserUID).child("Email").setValue(correoNuevoStr); //Actualizamos la database
                    MU.MostrarToast(CambiarCorreoElectronico.this, "El Email se cambió exitosamente");
                    //Limpiamos cajas de texto
                    /*correoNuevo.getText().clear();
                    confirmarPassword.getText().clear();*/
                    Intent intent = new Intent(CambiarCorreoElectronico.this, MainMenu.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }else{
                    confirmarPassword.setError("La contraseña actual escrita no es la correcta");
                    confirmarPassword.requestFocus();
                }
                Progress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public  void onStart(){
        BD.MostrarElementoUsuarioActual(Database, "Email", correoActual, sharedPreferences); //Escribir el correo en el textview
        super.onStart();
    }

    @Override
    protected void onResume() {
        DetectaConexion CD = new DetectaConexion(this);
        CD.ConexionPorSegundos(nowifibutton);
        super.onResume();
    }


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
