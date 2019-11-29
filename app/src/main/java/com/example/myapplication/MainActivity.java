package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView singleClick = (TextView) findViewById(R.id.single_click);
        TextView doubleClick = (TextView) findViewById(R.id.double_click);

        singleClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("单点", "onClick: singleClick" );
            }
        });

        //可以多次点击的,添加注解 @DoubleCLick
        doubleClick.setOnClickListener(new View.OnClickListener() {

            @DoubleCLick
            @Override
            public void onClick(View v) {
                Log.e("多次点击的", "onClick: doubleClick" );
            }
        });
    }
}
