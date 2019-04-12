package com.smartpassfime.smartpassfime;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class ActivityQR extends AppCompatActivity {

    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static String texto = "";
    private static String textoanterior = "";

    private Button Back;
    private TextView Prueba;
    private String QR_GENERAL = "ENTRADA GENERAL";
    private String QR_CUBICULO = "ENTRADA CUBICULO";
    private String QR_MTRABAJO= "ENTRADA MTRABAJO";
    private String LECTURA_GENERAL = "";
    private String LECTURA_CUBICULO = "";
    private String LECTURA_MTRABAJO = "";

    public int Cantidad = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        Back = findViewById(R.id.back);
        Prueba = findViewById(R.id.prueba);
        cameraView = findViewById(R.id.camaraview);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityQR.this, MainMenu.class);
                startActivity(intent);
                finish();

            }
        });

        obtenervaloresQR();
        initQR();


    }

    //Cargar los datos de tus entradas de la sala para luego sobre escribirlas de tu contador
    public void obtenervaloresQR(){
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Cubiculo").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String datoCubiculo = dataSnapshot.getValue().toString();
                LECTURA_CUBICULO = datoCubiculo;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("General").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String datoGeneral = dataSnapshot.getValue().toString();
                LECTURA_GENERAL = datoGeneral;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child("Mesa de Trabajo").addValueEventListener(new ValueEventListener() {

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

    //Aqui se valida cual codigo Qr se identifica con cual entrada
    public void SubirDB(String saca){

        if(saca.equals(QR_GENERAL)){
            LECTURA_GENERAL = AumentadordeConteo(LECTURA_GENERAL);
            String aGeneral = "General";
            SubirDBFinal(aGeneral, LECTURA_GENERAL);
          //  Toast.makeText(ActivityQR.this, "SijalaGeneral", Toast.LENGTH_LONG).show();
        } else if(saca.equals(QR_CUBICULO)){
            LECTURA_CUBICULO = AumentadordeConteo(LECTURA_CUBICULO);
            String aCubiculo = "Cubiculo";
            SubirDBFinal(aCubiculo, LECTURA_CUBICULO);
          //  Toast.makeText(ActivityQR.this, "SijalaCubiculo", Toast.LENGTH_LONG).show();
        }else if (saca.equals(QR_MTRABAJO)){
            LECTURA_MTRABAJO = AumentadordeConteo(LECTURA_MTRABAJO);
            String aMTrabajo = "Mesa de Trabajo";
            SubirDBFinal(aMTrabajo, LECTURA_MTRABAJO);
            //Toast.makeText(ActivityQR.this, "SijalaMtrabajo", Toast.LENGTH_LONG).show();
        } else {


            Intent intent = new Intent(ActivityQR.this, MainMenu.class);
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

        FirebaseDatabase.getInstance().getReference("Matricula").child(ActivityIngresar1.uiid).child(Resultado1).setValue(Resultado2).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ActivityQR.this, "Entrada Registrada ", Toast.LENGTH_SHORT).show();
                    Intent finish = new Intent(ActivityQR.this, ActivityFinish.class);
                    startActivity(finish);
                } else {
                    Toast.makeText(ActivityQR.this, "Algo falló al guardar tu entrada", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void initQR() {

        // creo el detector qr

        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();

        // creo la camara
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        // listener de ciclo de vida de la camara
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                // verifico si el usuario dio los permisos para la camara
                if (ActivityCompat.checkSelfPermission(ActivityQR.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // verificamos la version de ANdroid que sea al menos la M para mostrar
                        // el dialog de la solicitud de la camara
                        if (shouldShowRequestPermissionRationale(
                                Manifest.permission.CAMERA)) ;
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                    return;
                } else {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        // preparo el detector de QR
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }


            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() > 0) {

                    texto = barcodes.valueAt(0).displayValue;

                    if(!texto.equals(textoanterior)) {

                        //Prueba.post(new Runnable() {
                         //   @Override
                        //    public void run() {

                        textoanterior = texto;
                        Log.i("texto", texto);
                        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(300);
                                //String texto = barcodes.valueAt(0).displayValue;
                      //  Prueba.setText(texto);
                        SubirDB(texto);


                         //   }
                       // });

                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    synchronized (this) {
                                        wait(5000);
                                        // limpiamos el token
                                        textoanterior = "";
                                    }
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    Log.e("Error", "Waiting didnt work!!");
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }

                    // obtenemos el token
                    /*token = barcodes.valueAt(0).displayValue.toString();

                    // verificamos que el token anterior no se igual al actual
                    // esto es util para evitar multiples llamadas empleando el mismo token
                    if (!token.equals(tokenanterior)) {

                        // guardamos el ultimo token proceado
                        tokenanterior = token;
                        Log.v("token", token);
                        final String prueba1 = token;

                        /*Prueba.setText("Prueba\n"+prueba1);*/

                        /*sacartextodeaqui(prueba1);


                        if (URLUtil.isValidUrl(token)) {
                            // si es una URL valida abre el navegador
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(token));

                            startActivity(browserIntent);


                        } else {
                            // comparte en otras apps
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, token);
                            shareIntent.setType("text/plain");
                            startActivity(shareIntent);
                        }

                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    synchronized (this) {
                                        wait(5000);
                                        // limpiamos el token
                                        tokenanterior = "";
                                    }
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    Log.e("Error", "Waiting didnt work!!");
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                }*/
                }
            }
        });

    }
}
