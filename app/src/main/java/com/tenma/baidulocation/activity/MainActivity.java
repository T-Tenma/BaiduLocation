package com.tenma.baidulocation.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tenma.baidulocation.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mLocationBtn = (Button) findViewById(R.id.btn_location);
        mLocationBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,MapActivity.class);
        startActivity(intent);
    }
}
