package com.smartpassfime.smartpassfime;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class MetodosUtiles {

    //Elimina emojis
    public InputFilter filters(){
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    int type = Character.getType(source.charAt(i));
                    //System.out.println("Type : " + type);
                    if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                        return "";
                    }
                }
                return null;
            }
        };
        return filter;
    }

    //Metodo para subrayar un textview por medio segundo
    public void Subrayar(final TextView text){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                text.setText(text.getResources().getString(R.string.No_Subrayado)); //termina de subrayar
            }
        }, 500);
    }


    private Toast ToastMessage;
    public void MostrarToast(Context context, String Texto){
        //Para no repetir toasts
        if (ToastMessage != null) {
            ToastMessage.cancel();
        }
        ToastMessage = Toast.makeText(context, Texto, Toast.LENGTH_LONG);
        ToastMessage.show();
    }

    /*private boolean estaVisible = false;
    public void OjosContraseñas(EditText contraseñaTexto, ImageButton VisibilidadOjos){
        if(estaVisible){ //Los caracteres se veran
            contraseñaTexto.setTransformationMethod(PasswordTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_notvisible); //Agregamos la imagen
            estaVisible = false; //La proxima vez que se le oprima hará el efecto contrario
        }else{ //Los caracteres permaneceran ocultos
            contraseñaTexto.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_visible); //Agregamos la imagen
            estaVisible = true; //La proxima vez que se le oprima hará el efecto contrario
        }
    }

    public void OjosContraseñasOnResume(EditText contraseñaTexto, ImageButton VisibilidadOjos){
        if(estaVisible){
            contraseñaTexto.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_visible); //Agregamos la imagen
        }else{
            contraseñaTexto.setTransformationMethod(PasswordTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_notvisible); //Agregamos la imagen
        }
    }

    //Los sigueintes dos metodos son necesarios cuando tienes multiples botones con ojos
    public void OjosContraseñasOnResumeMultiples(EditText contraseñaTexto, ImageButton VisibilidadOjos, boolean visible){
        if(visible){
            contraseñaTexto.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_visible); //Agregamos la imagen
        }else{
            contraseñaTexto.setTransformationMethod(PasswordTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_notvisible); //Agregamos la imagen
        }
    }
    public boolean OjosContraseñasMultiples(EditText contraseñaTexto, ImageButton VisibilidadOjos, boolean visible){
        if(visible){ //Los caracteres se veran
            contraseñaTexto.setTransformationMethod(PasswordTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_notvisible); //Agregamos la imagen
            visible = false;
            return visible;
        }else{ //Los caracteres permaneceran ocultos
            contraseñaTexto.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            contraseñaTexto.setSelection(contraseñaTexto.getText().length()); //Necesario para que el cursor se ponga hasta el final del texto
            VisibilidadOjos.setImageResource(R.mipmap.ic_visible); //Agregamos la imagen
            visible = true; //La proxima vez que se le oprima hará el efecto contrario
            return visible;
        }
    }*/


}


//Clase para detectar si hay internet
class DetectaConexion{
    Context context;

    public DetectaConexion(Context context){
        this.context = context;
    }

    public boolean isConnected(){
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if(connectivity != null){
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if(info != null){
                if(info.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }


    //Variables para revisar la conexion a internet
    Handler h = new Handler();
    Runnable runnable;
    public void ConexionPorSegundos(final ImageButton nowifibutton){
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                if(isConnected()) {
                    nowifibutton.setEnabled(false);
                    nowifibutton.setVisibility(View.GONE);
                }else{
                    nowifibutton.setEnabled(true);
                    nowifibutton.setVisibility(View.VISIBLE);
                }

                h.postDelayed(runnable, 500);
            }
        }, 500);
    }

    public void DetenerContador(){
        h.removeCallbacks(runnable);
    }

    //Solo se ejecuta una vez
    public void startConexion(ImageButton nowifibutton){
        if(isConnected()){
            nowifibutton.setEnabled(false);
            nowifibutton.setVisibility(View.GONE);
        }else{
            nowifibutton.setEnabled(true);
            nowifibutton.setVisibility(View.VISIBLE);
        }
    }

    //Mensaje al oprimir el boton de no internet
    public void mensajeNoInternet(Context Activity){
        //Dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity);
        builder.setTitle("No hay conexión a internet");
        builder.setMessage("Es posible que " + "algunas opciones no funcionen correctamente. Por seguridad de datos mantenga la aplicación abierta u oculta hasta " +
                "que vuelva a entrar la conexión.");
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.show();

        //Align del dialogo
        TextView messageView = dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.FILL);
    }
}

//clase que se encarga de manejar las variables estaticas
class VariablesEstaticas extends  android.app.Application{
    //Persistencia de datos
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String CURRENT_USER_UID = "currentUserUID";
    public static final String IS_LOGGED = "isLogged";

    public static String CurrentUserUID; //Valor del current user UID //VARIABLE QUE DEBE GUARDARSE HASTA CUANDO SE CIERRA LA APP
    public static boolean isLoged = false; //VARIABLE QUE DEBE GUARDARSE HASTA CUANDO SE CIERRA LA APP
    //Listas para guardar los valores temporales para luego ser almacenadas a la base de datos y al auth cuando vuelva internet
    //static para que su valor se guarde, y final para que la lista no cambie, lo que contiene si puede cambiar
    public static String UID = UUID.randomUUID().toString().replace("-", ""); //Generador
    //Variable estatica que tomará un control del uid que se vaya a guardar en la base de datos local
    public static boolean Locked = false;

    //Metodo que nos ayudara a guardar las variables desde la memoria del telefono
    public void GuardarDatos(SharedPreferences sharedPreferences, boolean estado, String uid){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VariablesEstaticas.IS_LOGGED, estado);
        editor.putString(VariablesEstaticas.CURRENT_USER_UID, uid);
        editor.apply();
    }

    //Metodo que nos ayudara a cargar las variables desde la memoria del telefono
    public void CargarDatos(SharedPreferences sharedPreferences){
        VariablesEstaticas.CurrentUserUID = sharedPreferences.getString(VariablesEstaticas.CURRENT_USER_UID, "");
        VariablesEstaticas.isLoged = sharedPreferences.getBoolean(VariablesEstaticas.IS_LOGGED, false);
        Log.e("TEST","CurrentUser "+VariablesEstaticas.CurrentUserUID + " isLoged "+VariablesEstaticas.isLoged);
    }


}

class BaseDeDatos{
    VariablesEstaticas VE = new VariablesEstaticas();
    //Metodo encargado de mostrar algun elemento de la base de datos relacionado al usuario actual
    public void MostrarElementoUsuarioActual(DatabaseReference Database, String ElementoAConseguir, final TextView textView, final SharedPreferences sharedPreferences){
        Log.e("TEST", "CurrentUserUID en Mostrar: "+VariablesEstaticas.CurrentUserUID);
        Database.child(VariablesEstaticas.CurrentUserUID).child(ElementoAConseguir).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if necesario por si ya no obtiene el valor del uid por alguna razon, de esta manera no crasheara
                //Se necesitan volver a cargar los datos porque cada cambio en la base de datos activa este ondatachange
                VE.CargarDatos(sharedPreferences);
                if(VariablesEstaticas.CurrentUserUID != null && !VariablesEstaticas.CurrentUserUID.equals("")) {
                    String elemento = dataSnapshot.getValue().toString();
                    textView.setText(elemento);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public int ValidarContraseña(String contraseñaStr, int detectorDeErroresPassword, EditText contraseña, String mensaje){
        if(contraseñaStr.isEmpty()){
            contraseña.setError(mensaje);
            contraseña.requestFocus();
            detectorDeErroresPassword++;
        }else if(contraseñaStr.length() < 6){
            contraseña.setError("La contraseña es muy corta");
            contraseña.requestFocus();
            detectorDeErroresPassword++;
        }else{
            detectorDeErroresPassword = 0;
        }
        return detectorDeErroresPassword;
    }

    public int ValidarCorreo(String correoStr, int detectorDeErroresCorreo, EditText correo, String mensaje){
        if(correoStr.isEmpty()){
            correo.setError(mensaje/*"Correo requerido"*/);
            correo.requestFocus();
            detectorDeErroresCorreo++;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(correoStr).matches()){
            correo.setError("Ingrese un email valido");
            correo.requestFocus();
            detectorDeErroresCorreo++;
        }else{
            detectorDeErroresCorreo = 0;
        }
        return detectorDeErroresCorreo;
    }

    public int ValidarUsuario(String usuarioStr, int detectorDeErroresUsuario, EditText usuario, String mensaje){
        if(usuarioStr.isEmpty()){
            usuario.setError(mensaje);
            usuario.requestFocus();
            detectorDeErroresUsuario++;
        }else{
            detectorDeErroresUsuario = 0;
        }
        return detectorDeErroresUsuario;
    }
}