package com.smartpassfime.smartpassfime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActivityRegistrar extends AppCompatActivity {
    private EditText Matriculaa, Contraseña, Email;
    private Button Ingresar, Registrarse;

    private ImageButton nowifibutton;
    private DatabaseReference Database;
    private int detectorDeErroresMatricula = 0, detectorDeErroresPassword = 0, detectorDeErroresEmail = 0;
    public String emailSrt, matriulaStr, contraseñaStr;
    private String cero = "0";

    private boolean ExisteMatricula = false;
    private boolean ExisteCorreo = false;
    private boolean noMasDataChanges = false;
    private ProgressDialog Progress;
    private List CantidadDeSnapshots = new ArrayList();
    //Variable que nos permite vincular con el metodo que se encuentra en MetodosUtiles de VariablesEstaticas para guardar o cargar data
    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        nowifibutton = findViewById(R.id.activity_register_nowifibutton);
        Matriculaa = findViewById(R.id.matricula);
        Contraseña = findViewById(R.id.contraseña);
        Email = findViewById(R.id.correo);
        Ingresar = findViewById(R.id.entrar);
        Registrarse = findViewById(R.id.registrarse);
        Progress = new ProgressDialog(this);

        Progress.setCancelable(false);
        Progress.setCanceledOnTouchOutside(false);



        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        sharedPreferences = getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences);

        MetodosUtiles EliminaEmoijis = new MetodosUtiles();
        Matriculaa.setFilters(new InputFilter[]{EliminaEmoijis.filters()});
        Email.setFilters(new InputFilter[]{EliminaEmoijis.filters()});

        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        Registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });


        nowifibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CD.mensajeNoInternet(ActivityRegistrar.this);
            }
        });


    }

    private void registrarUsuario() {
        //Al oprimir el boton de registrar tomamos los textos que haya escrito el usuario
        matriulaStr = Matriculaa.getText().toString().trim();
        emailSrt = Email.getText().toString().trim();
        contraseñaStr = Contraseña.getText().toString().trim();

        //Verificamos que el usuario no haya dejado campos sin escribir
        Validar(matriulaStr, emailSrt, contraseñaStr);

        if (detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0) {
            Progress.setMessage("Registrando, por favor espere");
            Progress.show();
            CheckUsuarioExiste(); //Se encarga de revisar si el usuario existe en la base de datos
        }
        /*if (detectorDeErroresMatricula == 0 && detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0) {
            Auth.createUserWithEmailAndPassword(emailSrt, contraseñaStr)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivityRegistrar.this, "Se creó el usuario en auth", Toast.LENGTH_SHORT).show();
                                String uid = ActivityIngresar1.uiid;
                                logearUsuarioParaBD(uid);
                            } else { //AQUI FALTA DEFINIR LOS POSIBLES ERRORES DEL USUARIO COMO POR EJEMPLO SI INTENTO REGISTRARSE CON UN CORREO YA EN USO
                                Toast.makeText(ActivityRegistrar.this, "Algo falló al intentar crear el usuario2", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }*/
    }

    private void logearUsuarioParaBD(final String uid){
        /*Auth.signInWithEmailAndPassword(correoStr, contraseñaStr)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(RegisterActivity.this, FirebaseAuth.getInstance().getCurrentUser().getUid() , Toast.LENGTH_SHORT).show();
                        } else
                            //Toast.makeText(IngresarActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(RegisterActivity.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                    }
                });----v1
        //Marcamos los setters


        GettersDeUsuarios g = new GettersDeUsuarios(matriulaStr, emailSrt);
        FirebaseDatabase.getInstance().getReference("Matricula").child(uid).setValue(g).addOnCompleteListener(new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    creaciondedatos();
                    Toast.makeText(ActivityRegistrar.this, "Registro con Exito", Toast.LENGTH_SHORT).show();
                    Intent mainmenu = new Intent(ActivityRegistrar.this, MainMenu.class);
                    startActivity(mainmenu);
                } else {
                    Toast.makeText(ActivityRegistrar.this, "Algo falló al intentar crear el Alumno1", Toast.LENGTH_SHORT).show();
                }
            }
        });-----v2*/

            GettersDeUsuarios g = new GettersDeUsuarios(matriulaStr, emailSrt, contraseñaStr, "true", cero, cero, cero); //Usuario se logea al crear la cuenta HAY QUE CREAR UN FALSE AL CERRAR SESION
            Database.child(uid).setValue(g);
           // creaciondedatos();
            LogeoExitoso(uid);


    }

    public void LogeoExitoso(String UID){

        Progress.dismiss();
        Toast.makeText(ActivityRegistrar.this, "Alumno generado en BD", Toast.LENGTH_SHORT).show();
        Intent mainmenu = new Intent(ActivityRegistrar.this, MainMenu.class);
        mainmenu.putExtra("El UID",UID);
        startActivity(mainmenu);
        Intent ingresar = new Intent(ActivityIngresar1.FINISH_ALERT); //Vinculamos la variable de ActivityIngresar acá
        ActivityRegistrar.this.sendBroadcast(ingresar); //Llamamos al Broadcast para que lo cierre
        //VE.CuentaAbiertaGuardarUID(true, UID); //isLogged ahora es true
        VE.GuardarDatos(sharedPreferences, true, UID); //Guardamos los cambios, la variable islogged y el uid
        //VariablesEstaticas.isLoged = true;
        finish(); //Terminamos
    }

    /*private void creaciondedatos(){
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Cubiculo").setValue(cero).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ActivityRegistrar.this, "Cubiculo Creado " , Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ActivityRegistrar.this, "Algo falló", Toast.LENGTH_SHORT).show();
                }
            }
        });
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Mesa de Trabajo").setValue(cero).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ActivityRegistrar.this, "Mesa de Trabajo Creado ", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ActivityRegistrar.this, "Algo falló", Toast.LENGTH_SHORT).show();
                }
            }
        });
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("General").setValue(cero).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ActivityRegistrar.this, "Sala General Creado ", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ActivityRegistrar.this, "Algo falló", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    @Override
    protected void onStart() {
        super.onStart();
    }

    //Metodo para validar los campos a ingresar del usuario
    private void Validar(String matriulaStr, String emailSrt, String contraseñaStr){

        if(matriulaStr.isEmpty()){
            Matriculaa.setError("Matricula Requerido");
            Matriculaa.requestFocus();
            detectorDeErroresMatricula++;
        }else if(matriulaStr.length() < 7){
            Matriculaa.setError("La matricula debe contener al menos 7 numeros");
            Matriculaa.requestFocus();
            detectorDeErroresMatricula++;
        }else if(matriulaStr.length() > 7){
            Matriculaa.setError("La matricula debe contener al menos 7 numeros");
            Matriculaa.requestFocus();
            detectorDeErroresMatricula++;
        } else if (isNumeric(matriulaStr) == false) {
            Matriculaa.setError("La matriula debe tener solo numeros");
            Matriculaa.requestFocus();
            detectorDeErroresMatricula++;
        } else {
            detectorDeErroresMatricula = 0;
        }

        if(emailSrt.isEmpty()){
            Email.setError("Email requerido");
            Email.requestFocus();
            detectorDeErroresEmail++;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(emailSrt).matches()){
            Email.setError("Ingrese un email valido");
            Email.requestFocus();
            detectorDeErroresEmail++;
        }else{
            detectorDeErroresEmail = 0;
        }

        if(contraseñaStr.isEmpty()){
            Contraseña.setError("Contraseña requerida");
            Contraseña.requestFocus();
            detectorDeErroresPassword++;
        }else if(contraseñaStr.length() < 6){
            Contraseña.setError("La contraseña es muy corta");
            Contraseña.requestFocus();
            detectorDeErroresPassword++;
        }else{
            detectorDeErroresPassword = 0;
        }
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

    public void CheckUsuarioExiste(){
        ExisteMatricula = false;
        ExisteCorreo = false;
        //Marcamos los setters
        Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("TEST2", "TODO BIEN");
                final int CantidadTotalDeSnaps = (int) dataSnapshot.getChildrenCount();
                if(dataSnapshot.getValue() != null) { //*No se puede usar equals en null aunque sea string*
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) //Recorremos cada campo de la tabla Usuarios
                    {
                        Database.child(snapshot.getKey()).child("Matriculaa").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                CantidadDeSnapshots.add(snapshot.child("Matriculaa").getValue().toString());
                                GetterVerificador g2 = snapshot.getValue(GetterVerificador.class);
                                g2.setUsuarioVerificador(matriulaStr);
                                g2.setCorreoVerificador(emailSrt);


                                if (g2.getUsuarioVerificador().equals(snapshot.child("Matriculaa").getValue().toString())){
                                    ExisteMatricula = true;
                                    detectorDeErroresMatricula = 0;
                                    detectorDeErroresMatricula++;
                                    Log.e("TEST2", "VALOR DE DETECTORDEERRORESUSUARIO EN DATACHANGE: " + detectorDeErroresMatricula + " el snapshot fue: " + snapshot.child("Matriculaa").getValue().toString());
                                }else {
                                    if (ExisteMatricula == false) {
                                        detectorDeErroresMatricula = 0;
                                    }
                                }
                                if(g2.getCorreoVerificador().equals(snapshot.child("Email").getValue().toString())){
                                    ExisteCorreo = true;
                                    detectorDeErroresEmail = 0;
                                    detectorDeErroresEmail++;
                                }else{
                                    if(ExisteCorreo == false){
                                        detectorDeErroresEmail = 0;
                                    }
                                }
                                if(CantidadTotalDeSnaps == CantidadDeSnapshots.size()) { //Ultimo ciclo del for ejecutar lo siguiente
                                    if (detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0 && detectorDeErroresMatricula == 0) {
                                        CantidadDeSnapshots.clear();
                                        Progress.dismiss();
                                        noMasDataChanges = true;
                                        logearUsuarioParaBD(VariablesEstaticas.UID);
                                    }else{
                                        if(noMasDataChanges == false) {
                                            CantidadDeSnapshots.clear();
                                            Progress.dismiss();
                                            if(detectorDeErroresMatricula != 0) {
                                                Matriculaa.requestFocus();
                                                Matriculaa.setError("La matricula ya existe.");
                                            }
                                            if(detectorDeErroresEmail != 0){
                                                Email.requestFocus();
                                                Email.setError("El email ingresado ya existe");
                                            }
                                        }else{
                                            CantidadDeSnapshots.clear();
                                            Progress.dismiss();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                //Toast.makeText(RegisterActivity.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                            }
                        });
                        //Si no hay ningun usuario en la base de datos
                    }
                }else{
                    Progress.dismiss();
                    logearUsuarioParaBD(VariablesEstaticas.UID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ActivityRegistrar.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
        Log.e("TEST2", "VALOR DE DETECTORDEERRORESUSUARIO ANTES DE RETORNAR: "+detectorDeErroresMatricula);
    }

    public static boolean isNumeric(String cadena) {

        boolean resultado;

        try {
            Integer.parseInt(cadena);
            resultado = true;
        } catch (NumberFormatException excepcion) {
            resultado = false;
        }

        return resultado;
    }

}

