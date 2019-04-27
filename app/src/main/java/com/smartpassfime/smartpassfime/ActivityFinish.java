package com.smartpassfime.smartpassfime;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ActivityFinish extends AppCompatActivity {

    private Button Okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Okay = findViewById(R.id.okay);

        Toast.makeText(ActivityFinish.this, "Entrada Registrada ", Toast.LENGTH_SHORT).show();

        Okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityFinish.this, MainMenu.class);
                startActivity(intent);
                finish();

            }
        });

    }
}
