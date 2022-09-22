/* Javier Suárez Guzmán
        Septiembre 2022 */

package com.javiersg.proyectoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.javiersg.proyectoandroid.hellogeospatial.HelloGeoActivity;

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
        String [] valores = {"1. Australia","2. Argentina", "3. Chile", "4. Venezuela"};

        ArrayAdapter <String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valores);
        sp1.setAdapter(adaptador);
*/
    }

    public void Mapa(View view){
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void Ar(View view) {
        Intent i = new Intent(this, HelloGeoActivity.class);
        startActivity(i);
    }


}