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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CambiarPassword extends AppCompatActivity {

    private EditText password;
    private EditText passwordNuevo;
    private EditText confirmarPassword;

    private ProgressDialog Progress;
    private DatabaseReference Database;
    private Button CambiarContraseña;
    MetodosUtiles MU = new MetodosUtiles();
    BaseDeDatos BD = new BaseDeDatos();
    private ImageButton nowifibutton;

    private int detectorDeErroresPassword = 0, detectorDeErroresPasswordNuevo = 0, detectorDeErroresConfirmarPassword = 0;

    public String passwordStr;
    public String passwordNuevoStr;
    public String confirmarPassowrdStr;
    public String ContraseñaReal;

    //Variable que nos permite vincular con el metodo que se encuentra en MetodosUtiles de VariablesEstaticas para guardar o cargar data
    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        password =  findViewById(R.id.passwordactual);
        passwordNuevo = findViewById(R.id.passwordnuevo);
        confirmarPassword = findViewById(R.id.passworddenuevo);
        Progress = new ProgressDialog(this);
        CambiarContraseña = findViewById(R.id.acetarcambiarpass);



        //No se puedan cancelar los progress
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
                CD.mensajeNoInternet(CambiarPassword.this);
            }
        });

        CambiarContraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordStr = password.getText().toString().trim();
                passwordNuevoStr = passwordNuevo.getText().toString().trim();
                confirmarPassowrdStr = confirmarPassword.getText().toString().trim();
                detectorDeErroresPassword = BD.ValidarContraseña(passwordStr, detectorDeErroresPassword, password, "Se requiere contraseña");
                detectorDeErroresPasswordNuevo = BD.ValidarContraseña(passwordNuevoStr, detectorDeErroresPasswordNuevo, passwordNuevo, "Se requiere" +
                        " contraseña nueva");
                detectorDeErroresConfirmarPassword = BD.ValidarContraseña(confirmarPassowrdStr, detectorDeErroresConfirmarPassword, confirmarPassword,
                        "Se requiere confirmar contraseña");
                if (detectorDeErroresPassword == 0 && detectorDeErroresPasswordNuevo == 0 && detectorDeErroresConfirmarPassword == 0) {
                    Progress.setMessage("Modificando, por favor espere");
                    Progress.show();
                    RevisarMatchPassowrd(); //Metodo encargado de ver si lo que se escribio coincide
                }
            }
        });
    }

    @Override
    public  void onStart(){
        super.onStart();
    }

    public void RevisarMatchPassowrd(){
        Database.child(VariablesEstaticas.CurrentUserUID).child("Contraseña").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ContraseñaReal = dataSnapshot.getValue().toString(); //Conseguimos el valor real de la contraseña del usuario
                if(!passwordStr.equals(ContraseñaReal)){
                    password.setError("La contraseña actual escrita no es la correcta");
                    password.requestFocus();
                }
                if(!passwordNuevoStr.equals(confirmarPassowrdStr)){
                    confirmarPassword.setError("La contraseña nueva no concuerda con la confirmada");
                    confirmarPassword.requestFocus();
                }
                //Si se escribio correctamente:
                if(passwordStr.equals(ContraseñaReal) && passwordNuevoStr.equals(confirmarPassowrdStr)){
                    Database.child(VariablesEstaticas.CurrentUserUID).child("Contraseña").setValue(passwordNuevoStr); //Actualizamos la database
                    MU.MostrarToast(CambiarPassword.this, "La contraseña se cambió exitosamente");
                    /*//Limpiamos cajas de texto
                    password.getText().clear();
                    passwordNuevo.getText().clear();
                    confirmarPassword.getText().clear();*/
                    Intent intent = new Intent(CambiarPassword.this, MainMenu.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                Progress.dismiss();
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
