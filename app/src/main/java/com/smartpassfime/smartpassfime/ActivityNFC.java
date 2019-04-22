package com.smartpassfime.smartpassfime;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ActivityNFC extends AppCompatActivity {

    private String MatriculaTomada = "";
    public static final String ERROR_DETECTED = "Tarjeta NFC no Detectada";
    public static final String WRITE_SUCCESS = "Tarjeta detectada, Comunicacion Exitosa";
    public static final String WRITE_ERROR = "Error en la lectura, mantenga la comunicación estable";

    private String NFC_GENERAL = "ENTRADA GENERAL";
    private String NFC_CUBICULO = "ENTRADA CUBICULO";
    private String NFC_MTRABAJO= "ENTRADA MTRABAJO";
    private String LECTURA_GENERAL = "";
    private String LECTURA_CUBICULO = "";
    private String LECTURA_MTRABAJO = "";


    public int Cantidad = 0;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;

    MetodosUtiles MU = new MetodosUtiles();
    BaseDeDatos BD = new BaseDeDatos();

    private ImageButton nowifibutton;
    private ProgressDialog Progress;
    private DatabaseReference Database;

    SharedPreferences sharedPreferences;
    VariablesEstaticas VE = new VariablesEstaticas();

    private Button BackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        context = this;



        //Sincronizamos
        Database = FirebaseDatabase.getInstance().getReference("Matricula");
        Database.keepSynced(true);
        //Persistencia de variables
        sharedPreferences = getSharedPreferences(VariablesEstaticas.SHARED_PREFS, Context.MODE_PRIVATE);
        VE.CargarDatos(sharedPreferences);

        BackButton = findViewById(R.id.back);

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityNFC.this, MainMenu.class);
                startActivity(intent);

            }
        });



        nowifibutton = findViewById(R.id.activity_nfc_nowifibutton);

        final DetectaConexion CD = new DetectaConexion(this);
        CD.startConexion(nowifibutton);

        nowifibutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CD.mensajeNoInternet(ActivityNFC.this);
            }
        });

        obtenervaloresNFC();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "NO Soporta NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };


    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        //tvNFCContent.setText("NFC Content: " + text);
        SubirDB(text);
    }

    //Aqui se valida cual codigo Qr se identifica con cual entrada
    public void SubirDB(String saca){

        if(saca.equals(NFC_GENERAL)){
            LECTURA_GENERAL = AumentadordeConteo(LECTURA_GENERAL);
            String aGeneral = "SalaGeneral";
            SubirDBFinal(aGeneral, LECTURA_GENERAL);
            //  Toast.makeText(ActivityQR.this, "SijalaGeneral", Toast.LENGTH_LONG).show();
        } else if(saca.equals(NFC_CUBICULO)){
            LECTURA_CUBICULO = AumentadordeConteo(LECTURA_CUBICULO);
            String aCubiculo = "Cubiculo";
            SubirDBFinal(aCubiculo, LECTURA_CUBICULO);
            //  Toast.makeText(ActivityQR.this, "SijalaCubiculo", Toast.LENGTH_LONG).show();
        }else if (saca.equals(NFC_MTRABAJO)){
            LECTURA_MTRABAJO = AumentadordeConteo(LECTURA_MTRABAJO);
            String aMTrabajo = "MesaDeTrabajo";
            SubirDBFinal(aMTrabajo, LECTURA_MTRABAJO);
            //Toast.makeText(ActivityQR.this, "SijalaMtrabajo", Toast.LENGTH_LONG).show();
        } else {


            Intent intent = new Intent(ActivityNFC.this, MainMenu.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //Toast.makeText(ActivityQR.this, "QR NO VÁLIDO.", Toast.LENGTH_LONG).show();
            startActivity(intent);
        }
    }

    public String AumentadordeConteo(String Resultado){
        Cantidad = Integer.parseInt(Resultado);
        Cantidad++;
        Resultado = Integer.toString(Cantidad);
        return (Resultado);
    }

    private void SubirDBFinal(String Resultado1, String Resultado2){

        Database.child(VariablesEstaticas.CurrentUserUID).child(Resultado1).setValue(Resultado2);

        Intent finish = new Intent(ActivityNFC.this, ActivityFinish.class);
        startActivity(finish);
       //Toast.makeText(ActivityNFC.this, "Algo falló al guardar tu entrada", Toast.LENGTH_SHORT).show();

    }

    //Cargar los datos de tus entradas de la sala para luego sobre escribirlas de tu contador
    public void obtenervaloresNFC(){
        Database.child(VariablesEstaticas.CurrentUserUID).child("Cubiculo").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String datoCubiculo = dataSnapshot.getValue().toString();
                LECTURA_CUBICULO = datoCubiculo;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Database.child(VariablesEstaticas.CurrentUserUID).child("SalaGeneral").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String datoGeneral = dataSnapshot.getValue().toString();
                LECTURA_GENERAL = datoGeneral;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Database.child(VariablesEstaticas.CurrentUserUID).child("MesaDeTrabajo").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String datoMTrabajo = dataSnapshot.getValue().toString();
                LECTURA_MTRABAJO = datoMTrabajo;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }



    @Override
    protected void onPause() {
        WriteModeOff();
        DetectaConexion CD = new DetectaConexion(this);
        CD.DetenerContador();
        super.onPause();
    }

    @Override
    public void onResume(){
        WriteModeOn();
        DetectaConexion CD = new DetectaConexion(this);
        CD.ConexionPorSegundos(nowifibutton);
        super.onResume();

    }



    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}

