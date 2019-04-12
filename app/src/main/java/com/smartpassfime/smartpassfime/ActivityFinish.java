package com.smartpassfime.smartpassfime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityFinish extends AppCompatActivity {

    private Button Okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        Okay = findViewById(R.id.okay);

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
