/* Javier Suárez Guzmán
        Septiembre 2022 */

package com.javiersg.proyectoandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerPassRep: EditText
    private lateinit var registerButton: Button
    private lateinit var registerToLogin: Button

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        //auth = Firebase.auth //Esta declaración no funcionó

        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPass)
        registerPassRep = findViewById(R.id.registerPassRep)
        registerButton = findViewById(R.id.registerButton)
        registerToLogin = findViewById(R.id.registerToLogin)

        //Acciones de botónes
        registerToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        registerButton.setOnClickListener {
            val mail = registerEmail.text.toString()
            val pass = registerPassword.text.toString()
            val passRep = registerPassRep.text.toString()

            if (checkEmpty(mail, pass, passRep)) {
                auth.createUserWithEmailAndPassword(mail,pass)
                    .addOnCompleteListener() { task ->
                        if (task.isSuccessful) {
    //15 clicks/taps 1 registro exitoso, le toma: entre 3 min y 4 registrar en firebase
                            // y el cambio de activity appx 6
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(applicationContext,"Registro fallido",Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkEmpty(mail: String, pass: String, passRep: String): Boolean {
        return mail.isNotEmpty() && pass.isNotEmpty() && passRep.isNotEmpty()
    }
/*
    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {task -> //tarea asíncrona
                if(task.isSuccessful){
                    //var i = Intent(this, DashboardActivity::class.java) //forma larga y nueva activity comentada
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Registro fallido", Toast.LENGTH_LONG).show()
                }
            }
    }*/
}