/* Javier Suárez Guzmán
        Septiembre 2022 */

package com.javiersg.proyectoandroid;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javiersg.proyectoandroid.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtiene el SupportMapFragment del layout.xml
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Cuando el mapa está disponible, lo utiliza
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Marcador en Santiago, Chile
        LatLng santiago = new LatLng(-33.45, -70.666);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //Se cambia a vista híbrida = 4
        //GoogleMap.MAP_TYPE_SATELLITE = 2
        //GoogleMap.MAP_TYPE_TERRAIN = 3
        mMap.addMarker(new MarkerOptions().position(santiago).title("Marca en Santiago"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(santiago));
    }
}