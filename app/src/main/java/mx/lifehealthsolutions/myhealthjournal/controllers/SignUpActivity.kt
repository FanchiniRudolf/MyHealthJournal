package mx.lifehealthsolutions.myhealthjournal.controllers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*
import mx.lifehealthsolutions.myhealthjournal.R


class SignUpActivity : AppCompatActivity() {
    val RC_SIGN_IN = 123
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()




    }
    fun load(){
        progressBar2.visibility = View.VISIBLE
    }

    fun visible(){
        progressBar2.visibility = View.INVISIBLE
    }
    fun addUser(v: View){

        if( email.text.isNullOrEmpty() || password.text.isNullOrEmpty() || cpassword.text.isNullOrEmpty()){
            Toast.makeText(
                baseContext, "¡Hay campos vacíos!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        var email = email.text.toString()
        var password = password.text.toString()
        var cpassword = cpassword.text.toString()


        if(cpassword != password){
            Toast.makeText(
                baseContext, "Las contraseñas no coinciden",
                Toast.LENGTH_SHORT
            ).show()
        }
        else {
            if(password.length < 8){
                Toast.makeText(
                    baseContext, "La contraseña debe de ser de al menos 8 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            load()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(
                            baseContext, "Registro exitoso",
                            Toast.LENGTH_SHORT
                        ).show()
                        val user = auth.currentUser
                        val  setupIntent = Intent(this, StartupSetup::class.java).putExtra("email",email)
                        startActivity(setupIntent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext, "Error al registrar, es posible que exista ya un usuario con el correo",
                            Toast.LENGTH_SHORT
                        ).show()
                        visible()
                    }

                    // ...
                }
        }
    }
}
