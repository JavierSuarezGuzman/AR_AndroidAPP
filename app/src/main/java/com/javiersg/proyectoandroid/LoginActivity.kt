/* Javier Suárez Guzmán
        Septiembre 2022 */

package com.javiersg.proyectoandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var loginToRegister: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPass)
        loginButton = findViewById(R.id.loginButton)
        loginToRegister = findViewById(R.id.loginToRegister)

        //Acciones de botónes
        loginToRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        loginButton.setOnClickListener{
            val mail = loginEmail.text.toString()
            val pass = loginPassword.text.toString()

            if (checkEmpty(mail, pass)) {
                //EmailAuthProvider.getCredential(mail,pass) //posiblemente una versión actualizada
                /*auth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this) { task -> //tarea asíncrona
                        if (task.isSuccessful) {*/
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                       /* } else {
                            Toast.makeText(applicationContext, "Ingreso fallido", Toast.LENGTH_LONG).show()
                        }
                    }*/
            } else {
                Toast.makeText(applicationContext, "Rellena ambos campos", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun checkEmpty(mail: String, pass: String): Boolean {
        return mail.isNotEmpty() && pass.isNotEmpty()
    }

}