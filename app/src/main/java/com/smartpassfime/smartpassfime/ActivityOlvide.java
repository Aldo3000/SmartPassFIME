package com.smartpassfime.smartpassfime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.StrictMode;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

import static android.os.StrictMode.setThreadPolicy;

    public class ActivityOlvide extends AppCompatActivity {

        private ImageButton nowifibutton;
        private Button mandarcorreo;
        private EditText correoText;
        private DatabaseReference Database;
        private ProgressDialog Progress;
        Session session;
        String correo; String password;
        public int detectorDeErroresCorreo = 0;
        private boolean ExisteCorreo = false;
        private boolean noMasDataChanges = false;
        private List CantidadDeSnapshots = new ArrayList();
        private String CorreoAMandar;
        private String UsuarioEncontrado;
        private String ContraseñaEncontrada;


        //Variable que nos permite vincular con el metodo que se encuentra en MetodosUtiles de VariablesEstaticas para guardar o cargar data
        SharedPreferences sharedPreferences;
        VariablesEstaticas VE = new VariablesEstaticas();
        final MetodosUtiles MandarToast = new MetodosUtiles();


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_olvide);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            //Persistencia de datos y referencia
            Database = FirebaseDatabase.getInstance().getReference("Matricula");
            Database.keepSynced(true);
            //Persistencia de variables
            sharedPreferences= getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
            VE.CargarDatos(sharedPreferences);

            nowifibutton = findViewById(R.id.activity_olvide_password_nowifibutton);
            mandarcorreo = findViewById(R.id.activity_olvide_password_mandarcorreobutton);
            correoText = findViewById(R.id.activity_olvide_password_correoedittext);
            Progress = new ProgressDialog(this);

            //Eliminar los emojis
            MetodosUtiles EliminaEmoijis = new MetodosUtiles();
            correoText.setFilters(new InputFilter[]{EliminaEmoijis.filters()});

            //No se puedan cancelar los progress
            Progress.setCancelable(false);
            Progress.setCanceledOnTouchOutside(false);

            //Correo y password de la cuenta que sostiene la aplicacion
            correo = "noreplysmartpassfime@gmail.com";
            password = "FIME1234";

            //Necesario para mostrar la conexión al inicio
            final DetectaConexion CD = new DetectaConexion(this);
            CD.startConexion(nowifibutton);


            nowifibutton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    CD.mensajeNoInternet(ActivityOlvide.this);
                }
            });

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); //Politicas para poder hacer uso del correo y la contraseña
            setThreadPolicy(policy);
            Properties properties = new Properties();
            properties.put("mail.smtp.host", "smtp.googlemail.com");
            //Recibir respuesta del host
            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", "465");
            session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(correo, password);
                }
            });

            mandarcorreo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CorreoAMandar = correoText.getText().toString().toLowerCase().trim();
                    Validar(CorreoAMandar);
                    //OlvideContraseña.setText(Html.fromHtml(getString(R.string.Olvidé_mi_contraseña))); //subrayar texto
                    //Log.e("TEST", ""+VariablesEstaticas.Locked);
                    if (detectorDeErroresCorreo == 0) {
                        if (VariablesEstaticas.Locked == false) { //Si no esta locked
                            VariablesEstaticas.Locked = true;
                            if (CD.isConnected()) { //Revisamos si hay internet
                                Progress.setMessage("Enviando, por favor espere");
                                Progress.show();
                                RevisarCorreoExiste();
                            } else { //Si no hay internet:
                                MandarToast.MostrarToast(ActivityOlvide.this,"Se necesita conexión a internet para acceder a " +
                                        "esta opción");
                                VariablesEstaticas.Locked = false; //Para volver a entrar al boton
                            }
                        }
                    }
                }
            });
        }

        //Mandar correo:
        public void MandarCorreo(final String NombreUsuario, final String ContraseñaUsuario){
            //Ejecutar el envio de correo despues de 3 segundos y medio
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    //Aqui faltaria validar si hay conexion a internet, si no es asi, entonces mandar un toast
                    try {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); //Politicas para poder hacer uso del correo y la contraseña
                        setThreadPolicy(policy);
                        Properties properties = new Properties();
                        properties.put("mail.smtp.host", "smtp.googlemail.com");
                        //Recibir respuesta del host
                        properties.put("mail.smtp.socketFactory.port", "465");
                        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        properties.put("mail.smtp.auth", "true");
                        properties.put("mail.smtp.port", "465");
                        session = Session.getDefaultInstance(properties, new Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(correo, password);
                            }
                        });
                        if (session != null) { //Si pudo ingresar a la cuenta de gmail entonces mandara el mensaje
                            javax.mail.Message message = new MimeMessage(session);
                            message.setFrom(new InternetAddress(correo));
                            message.setSubject("Recuperación de contraseña"); //Titulo del mensaje del correo
                            message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(CorreoAMandar)); //Correo al que se le va a mandar
                            message.setContent("Saludos alumno"+NombreUsuario+",\n Tu contraseña es: "+ContraseñaUsuario+",\n " +
                                    "Si no pediste recuperar tu contraseña, puedes ignorar este correo.\n" +
                                    "Atentamente, quipo de SmartPass FIME.", "text/html; charset=utf-8"); //Correo que se le va a madar
                            Transport.send(message); //Mandamos el correo

                        }
                        MandarToast.MostrarToast(ActivityOlvide.this,"Correo enviado");
                    } catch (Exception e) {
                        e.printStackTrace(); //Manda una excepcion
                        MandarToast.MostrarToast(ActivityOlvide.this,"Ocurrió un error al tratar de enviar el correo");
                    }
                    VariablesEstaticas.Locked = false; //Para volver a entrar al boton
                    finish();
                }
            }, 3500);
        }


        public void RevisarCorreoExiste(){
            ExisteCorreo = false;
            //Marcamos los setters
            Database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e("TEST2", "TODO BIEN");
                    final int CantidadTotalDeSnaps = (int) dataSnapshot.getChildrenCount();
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) //Recorremos cada campo de la tabla Usuarios
                    {
                        if(snapshot.getValue() != null) { //*No se puede usar equals en null aunque sea string*
                            Database.child(snapshot.getKey()).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    CantidadDeSnapshots.add(snapshot.child("Matriculaa").getValue().toString());
                                    GetterVerificador g2 = snapshot.getValue(GetterVerificador.class);
                                    g2.setCorreoVerificador(CorreoAMandar);
                                    if (g2.getCorreoVerificador().equals(snapshot.child("Email").getValue().toString())){
                                        ExisteCorreo = true;
                                        detectorDeErroresCorreo = 0;
                                        UsuarioEncontrado = snapshot.child("Matriculaa").getValue().toString();
                                        ContraseñaEncontrada = snapshot.child("Contraseña").getValue().toString();
                                    }else {
                                        if (ExisteCorreo == false) {
                                            detectorDeErroresCorreo = 1;
                                        }
                                    }
                                    if(CantidadTotalDeSnaps == CantidadDeSnapshots.size()) { //Ultimo ciclo del for ejecutar lo siguiente
                                        if (detectorDeErroresCorreo == 0) {
                                            CantidadDeSnapshots.clear();
                                            noMasDataChanges = true;
                                            //Mandamos el nombre de usuario y la contraseña al correo
                                            MandarCorreo(UsuarioEncontrado, ContraseñaEncontrada);
                                        }else{
                                            if(noMasDataChanges == false) {
                                                CantidadDeSnapshots.clear();
                                                Progress.dismiss();
                                                VariablesEstaticas.Locked = false; //Para volver a entrar al boton
                                                if(detectorDeErroresCorreo != 0){
                                                    correoText.requestFocus();
                                                    correoText.setError("El correo que escribió no pertenece a ningún alumno registrado");
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

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ActivityOlvide.this, "Ocurrio un error al intentar acceder a la base de datos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void Validar(String BuscarErroresCorreo){
            if(BuscarErroresCorreo.isEmpty()){
                correoText.setError("Email requerido");
                correoText.requestFocus();
                detectorDeErroresCorreo++;
            }else if(!Patterns.EMAIL_ADDRESS.matcher(BuscarErroresCorreo).matches()){
                correoText.setError("Ingrese un correo valido");
                correoText.requestFocus();
                detectorDeErroresCorreo++;
            }else{
                detectorDeErroresCorreo = 0;
            }
        }

        //Revisar conexion internet
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
        }

