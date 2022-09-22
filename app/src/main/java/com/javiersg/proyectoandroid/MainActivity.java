/* Javier Suárez Guzmán
        Septiembre 2022 */

package com.javiersg.proyectoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

//import android.widget.ArrayAdapter;
//import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    //private Spinner sp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sp1 = (Spinner)findViewById(R.id.spinner);
        //btn1 = (Button)findViewById(R.id.id_btn1);
        /*
        String [] valores = {"1. Australia","2. Dos", "3. Tres"};

        ArrayAdapter <String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valores);
        sp1.setAdapter(adaptador);
*/
    }

    public void Mapa(View view){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    /*public void Ar(View view) {
        Intent i = new Intent(this, ArActivity.class);
        startActivity(i);
    }*/


}