package com.smartpassfime.smartpassfime;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class ActivityIngresar1 extends AppCompatActivity {

    private EditText Matricula, Contraseña;
    private ImageButton nowifibutton;
    private Button Ingresar, Registro, Registrarse;
    private FirebaseAuth Auth;
    private ImageButton OlvideContraseña;
    private FirebaseAuth.AuthStateListener AuthListener;
    public static String uiid;
    public static boolean isLoged = false;
    DatabaseReference myDB = FirebaseDatabase.getInstance().getReference(); //Referenciamos la base de datos
    DatabaseReference myDBChild = myDB.child("Texto");
    private ProgressDialog Progress;
    private int detectorDeErroresEmail = 0, detectorDeErroresPassword = 0;
    static String UsuarioDeCorreo, NuevoCorreo, ValorCorreo; int testing;

    private String NuevoPassword;
    public static final String FINISH_ALERT = "finish_alert";

    private DatabaseReference Database;
    public int Vueltas = 0;
    //Variable que nos ayuda a conseguir el valor del UID segun el correo
    private String UIDDB;
    private boolean ExisteMatricula = false;
    private boolean ExisteCorreo = false;
    private boolean noMasDataChanges = false;
    private List CantidadDeSnapshots = new ArrayList();
    public String email, password;
    //Variable que nos permite vincular con el metodo que se encuentra en MetodosUtiles de VariablesEstaticas para guardar o cargar data
    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresar);
        this.registerReceiver(this.finishAlert, new IntentFilter(FINISH_ALERT));
        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        //Persistencia de variables
        sharedPreferences = getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences);

        if (!VariablesEstaticas.isLoged) {
            nowifibutton = findViewById(R.id.activity_ingresar_nowifibutton);
            Matricula = findViewById(R.id.matricula);
            Contraseña = findViewById(R.id.contraseña);
            Ingresar = findViewById(R.id.entrar);
            Registro = findViewById(R.id.registro);
            OlvideContraseña = findViewById(R.id.olvidebutton);
            MetodosUtiles EliminaEmoijis = new MetodosUtiles();
            Matricula.setFilters(new InputFilter[]{EliminaEmoijis.filters()});

            Progress = new ProgressDialog(this);
            final DetectaConexion CD = new DetectaConexion(this);
            final MetodosUtiles MandarToast = new MetodosUtiles();
            CD.startConexion(nowifibutton);

            Progress.setCancelable(false);
            Progress.setCanceledOnTouchOutside(false);

            Registro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityIngresar1.this, ActivityRegistrar.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);

                }
            });

            Ingresar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Vueltas = 0;
                    Logear();
                }
            });


            OlvideContraseña.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (VariablesEstaticas.Locked == false) { //Si no esta locked
                        VariablesEstaticas.Locked = true;
                        if (CD.isConnected()) { //Si esta conectado a internet
                            Intent olvidepassword = new Intent(ActivityIngresar1.this, ActivityOlvide.class); //Abre el activity
                            startActivity(olvidepassword);
                            VariablesEstaticas.Locked = false; //Para poder volver a entrar al boton
                        } else {    //Si no esta conectado a internet
                            MandarToast.MostrarToast(ActivityIngresar1.this, "Se necesita conexión a internet para acceder a " +
                                    "esta opción");
                            VariablesEstaticas.Locked = false; //Para volver a entrar al boton
                        }
                    }
                }
            });

            nowifibutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CD.mensajeNoInternet(ActivityIngresar1.this);
                }
            });
        } else { //Si el usuario está ingresado
            Intent MainMenu = new Intent(ActivityIngresar1.this, MainMenu.class);
            startActivity(MainMenu);
            finish();
        }
    }

    BroadcastReceiver finishAlert = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ActivityIngresar1.this.finish();
        }
    };

    @Override
    public void onDestroy() {

        super.onDestroy();
        this.unregisterReceiver(finishAlert);
    }
    //Al iniciar el activity se ejecutara lo siguiente
    @Override
    protected void onStart(){
        super.onStart();

    }

    private void Logear() {
        email = Matricula.getText().toString().trim();
        password = Contraseña.getText().toString().trim();
        Validar(email, password);
        if (detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0) {
            Progress.setMessage("Entrando, por favor espere");
            Progress.show();
            RevisarBD();
        }
    }

    public void LogeoExitosoOffline(String UID) {
        Toast.makeText(ActivityIngresar1.this, "Bienvenido", Toast.LENGTH_SHORT).show();
        //VE.CuentaAbiertaGuardarUID(true, UID);
        VE.GuardarDatos(sharedPreferences, true, UID);
        //VariablesEstaticas.isLoged = true;
        Log.e("TEST", "VA A ENTRAR AL MAINMENU DIOMEO");
        Intent mainmenu = new Intent(ActivityIngresar1.this, MainMenu.class);
        mainmenu.putExtra("El UID", UID);
        startActivity(mainmenu);
        finish();

    }

    @Override
    protected void onResume() {
        //vuelve visible o invisible el boton
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

    public void RevisarBD() {
        ExisteMatricula = false;
        ExisteCorreo = false;
        //Marcamos los setters
        Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("TEST2", "TODO BIEN");
                final int CantidadTotalDeSnaps = (int) dataSnapshot.getChildrenCount();
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) //Recorremos cada campo de la tabla Usuarios
                {
                    if (snapshot.getValue() != null) { //*No se puede usar equals en null aunque sea string*
                        Database.child(snapshot.getKey()).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                CantidadDeSnapshots.add(snapshot.child("Email").getValue().toString());
                                GetterVerificador g2 = snapshot.getValue(GetterVerificador.class);
                                g2.setCorreoVerificador(email);
                                if (g2.getCorreoVerificador().equals(snapshot.child("Email").getValue().toString())) {
                                    NuevoPassword = snapshot.child("Contraseña").getValue().toString();
                                    UIDDB = snapshot.getKey();
                                    Database.child(UIDDB).child("CuentaAbierta").setValue("true");
                                    ExisteMatricula = true;
                                    detectorDeErroresEmail = 0;
                                    Log.e("TEST2", "VALOR DE DETECTORDEERRORESUSUARIO EN DATACHANGE: " + detectorDeErroresEmail + " el snapshot fue: " + snapshot.child("Email").getValue().toString());
                                } else if (g2.getCorreoVerificador().toLowerCase().equals(snapshot.child("Email").getValue().toString())) {
                                    NuevoPassword = snapshot.child("Contraseña").getValue().toString();
                                    UIDDB = snapshot.getKey();
                                    Database.child(UIDDB).child("CuentaAbierta").setValue("true");
                                    ExisteMatricula = true;
                                    detectorDeErroresEmail = 0;
                                    Log.e("TEST2", "VALOR DE DETECTORDEERRORESUSUARIO EN DATACHANGE: " + detectorDeErroresEmail + " el snapshot fue: " + snapshot.child("Email").getValue().toString());
                                } else {
                                    if (ExisteMatricula == false) {
                                        detectorDeErroresEmail = 0;
                                        detectorDeErroresEmail++;
                                    }
                                }
                                if (CantidadTotalDeSnaps == CantidadDeSnapshots.size()) { //Ultimo ciclo del for ejecutar lo siguiente
                                    Log.e("TEST", "Valor de NuevoPassword: " + NuevoPassword);
                                    if (detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0 && NuevoPassword.equals(password)) {
                                        CantidadDeSnapshots.clear();
                                        Progress.dismiss();
                                        noMasDataChanges = true;
                                        //aqui va lo de cargar mainmenu
                                        //VariablesEstaticas.TEMPuid.add(UUID.randomUUID().toString().replace("-", ""));
                                        //logearUsuarioParaBD((String) VariablesEstaticas.TEMPuid.get(VariablesEstaticas.localDBControl));
                                        LogeoExitosoOffline(UIDDB);
                                    } else {
                                        if (noMasDataChanges == false) {
                                            CantidadDeSnapshots.clear();
                                            Progress.dismiss();
                                            //Aqui va si fracaso buscando por correo
                                            Database.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Log.e("TEST2", "TODO BIEN");
                                                    final int CantidadTotalDeSnaps = (int) dataSnapshot.getChildrenCount();
                                                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) //Recorremos cada campo de la tabla Usuarios
                                                    {
                                                        if (snapshot.getValue() != null) { //*No se puede usar equals en null aunque sea string*
                                                            Database.child(snapshot.getKey()).child("Matriculaa").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    CantidadDeSnapshots.add(snapshot.child("Matriculaa").getValue().toString());
                                                                    GetterVerificador g2 = snapshot.getValue(GetterVerificador.class);
                                                                    g2.setCorreoVerificador(email);
                                                                    Log.e("TEST", "Valor de g2.setCorreoVerificador: " + g2.getCorreoVerificador());
                                                                    Log.e("TEST", "Valor de snapshot Usuario: " + snapshot.child("Matriculaa").getValue().toString());


                                                                    if (g2.getCorreoVerificador().equals(snapshot.child("Matriculaa").getValue().toString())) {
                                                                        NuevoPassword = snapshot.child("Contraseña").getValue().toString();
                                                                        UIDDB = snapshot.getKey();
                                                                        Database.child(UIDDB).child("CuentaAbierta").setValue("true");
                                                                        ExisteMatricula = true;
                                                                        detectorDeErroresEmail = 0;
                                                                        Log.e("TEST2", "VALOR DE DETECTORDEERRORESUSUARIO EN DATACHANGE: " + detectorDeErroresEmail + " el snapshot fue: " + snapshot.child("Matriculaa").getValue().toString());
                                                                    } else {
                                                                        if (ExisteMatricula == false) {
                                                                            detectorDeErroresEmail = 0;
                                                                            detectorDeErroresEmail++;
                                                                        }
                                                                    }

                                                                    if (CantidadTotalDeSnaps == CantidadDeSnapshots.size()) { //Ultimo ciclo del for ejecutar lo siguiente
                                                                        Log.e("TEST", "Valor de NuevoPassword: " + NuevoPassword);
                                                                        if (detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0 && NuevoPassword.equals(password)) {
                                                                            CantidadDeSnapshots.clear();
                                                                            Progress.dismiss();
                                                                            noMasDataChanges = true;

                                                                            //aqui va lo de cargar mainmenu
                                                                            //VariablesEstaticas.TEMPuid.add(UUID.randomUUID().toString().replace("-", ""));
                                                                            //logearUsuarioParaBD((String) VariablesEstaticas.TEMPuid.get(VariablesEstaticas.localDBControl));
                                                                            LogeoExitosoOffline(UIDDB);
                                                                        } else {
                                                                            if (noMasDataChanges == false) {
                                                                                CantidadDeSnapshots.clear();
                                                                                Progress.dismiss();
                                                                                Toast.makeText(ActivityIngresar1.this, "Matricula o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    }
                                                                }


                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                    //Toast.makeText(RegisterActivity.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    //Toast.makeText(RegisterActivity.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {
                                            CantidadDeSnapshots.clear();
                                            Progress.dismiss();
                                            Toast.makeText(ActivityIngresar1.this, "Matricula o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                //Toast.makeText(RegisterActivity.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ActivityIngresar1.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Metodo para logear al usuario
    private void LogearAntes() {
        final String email = Matricula.getText().toString().trim();
        final String password = Contraseña.getText().toString().trim();
        Validar(email, password);
        Log.d("TEST", String.valueOf(testing));
        Log.d("TEST", "UsuarioDelCorreo vale: "+UsuarioDeCorreo);
        Log.d("TEST", "NuevoCorreo vale: "+NuevoCorreo);
        Log.d("TEST", "VALORCORREO VALE: "+ValorCorreo);
        if (detectorDeErroresEmail == 0 && detectorDeErroresPassword == 0) {
            Progress.setMessage("Iniciando Sesión, por favor espere un momento");
            Progress.show();
            //email = UsuarioDeCorreo;
            //email = NuevoCorreo;
            //if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Validar(email, password);
                            Progress.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivityIngresar1.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                                isLoged = true;
                                Intent mainmenu = new Intent(ActivityIngresar1.this, MainMenu.class);
                                startActivity(mainmenu);
                                finish();
                            } else {
                                //Toast.makeText(IngresarActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                //Toast.makeText(IngresarActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                Log.d("RECTAFINAL", "El valor de ValorCorreo es: " + ValorCorreo);
                                if (ValorCorreo != null) {
                                    Auth.signInWithEmailAndPassword(ValorCorreo, password)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    Progress.dismiss();
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ActivityIngresar1.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                                                        isLoged = true;
                                                        Intent mainmenu = new Intent(ActivityIngresar1.this, MainMenu.class);
                                                        startActivity(mainmenu);
                                                        finish();
                                                    } else
                                                        //Toast.makeText(IngresarActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        Toast.makeText(ActivityIngresar1.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(ActivityIngresar1.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            /*}
            else{

            }*/
        }
    }

    private void Validar(final String email, String password){
        if(email.isEmpty()){
            Matricula.setError("Matricula o Correo Requerido");
            Matricula.requestFocus();
            detectorDeErroresEmail++;
        }
        //else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            /*else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Usuario.setError("Ingrese un email valido");
            Usuario.requestFocus();
            detectorDeErroresEmail++;
        }*/else{
            detectorDeErroresEmail = 0;
        }

        if(password.isEmpty()){
            Contraseña.setError("Contraseña requerida");
            Contraseña.requestFocus();
            detectorDeErroresPassword++;
        }else if(password.length() < 6){
            Contraseña.setError("La contraseña es muy corta");
            Contraseña.requestFocus();
            detectorDeErroresPassword++;
        }else{
            detectorDeErroresPassword = 0;
        }
    }

    public String GetValorCorreoAntes(final String email){
        if(!email.isEmpty()){
            FirebaseDatabase.getInstance().getReference("Matricula").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange (DataSnapshot dataSnapshot){
                    for(final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        FirebaseDatabase.getInstance().getReference("Matricula").child(snapshot.getKey()).child("Matriculaa").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange (DataSnapshot dataSnapshot){
                                GetterVerificador g2 = snapshot.getValue(GetterVerificador.class);
                                g2.setUsuarioVerificador(email);
                                String ifUsuario = g2.getUsuarioVerificador();
                                //Log.d("Test", email);
                                //Log.d("Test", g2.getUsuarioVerificador());
                                if(g2.getUsuarioVerificador().equals(email)){
                                    UsuarioDeCorreo = g2.getUsuarioVerificador();

                                    FirebaseDatabase.getInstance().getReference("Matricula").child(snapshot.getKey()).child("Email").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange (DataSnapshot dataSnapshot) {
                                            GetterVerificador g2 = snapshot.getValue(GetterVerificador.class);
                                            //g2.setUsuarioVerificador(email);
                                            g2.setCorreoVerificador(snapshot.child("Email").getValue().toString());
                                            //Log.d("Test", UsuarioDeCorreo);
                                            //Log.d("Test", snapshot.child("Usuario").getValue().toString());
                                            if (UsuarioDeCorreo.equals(snapshot.child("Matriculaa").getValue().toString())) {
                                                Log.d("Test","ENTRO!!");
                                                if (g2.getCorreoVerificador().equals(snapshot.child("Email").getValue().toString())) {
                                                    //return;
                                                    //Log.d("Test", "Tu correo es: "+g2.getCorreoVerificador());
                                                    NuevoCorreo = g2.getCorreoVerificador();
                                                    Log.d("TEST", "NuevoCorreo vale: "+NuevoCorreo);
                                                } else {
                                                    Log.d("Test", "Tu correo es otro we");
                                                }
                                            }else{
                                                Log.d("TEST", "Rechazados: "+snapshot.child("Matriculaa").getValue().toString());
                                            }
                                                /*String ifUsuario = g2.getCorreoVerificador();
                                                Log.d("Test", snapshot.child("Correo").getValue().toString());
                                                UsuarioDeCorreo = g2.getCorreoVerificador();
                                                if(UsuarioDeCorreo == "osito@hotmail.com"){
                                                    Log.d("Test", "Tu cuenta es Osos");
                                                }else{
                                                    Log.d("Test","Tu cuenta es otra que no puedo decir :C");
                                                }*/
                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(ActivityIngresar1.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }else{
                                    Matricula.setError("Ingrese un email o un nombre de usuario valido");
                                    Matricula.requestFocus();
                                    detectorDeErroresEmail++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(ActivityIngresar1.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ActivityIngresar1.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                }
            });
        }
        Log.d("TEST", "El valor de valorcorreo dentro del elseif: "+NuevoCorreo);
        return NuevoCorreo;
    }
}