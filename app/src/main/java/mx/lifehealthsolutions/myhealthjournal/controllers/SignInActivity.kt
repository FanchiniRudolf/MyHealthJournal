package mx.lifehealthsolutions.myhealthjournal.controllers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import mx.lifehealthsolutions.myhealthjournal.R
import mx.lifehealthsolutions.myhealthjournal.models.User

class SignInActivity : AppCompatActivity() {
    val RC_SIGN_IN = 123
    private lateinit var auth: FirebaseAuth
    val TAG: String = "SignInActivity";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        val  gso =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        val GoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val regText = findViewById(R.id.registrate) as TextView
        regText.setOnClickListener{
            val regIntent = Intent(this, SignUpActivity::class.java)
            startActivity(regIntent)
        }
        googleButton.setOnClickListener{
            val signInIntent = GoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);

        };
        buttonIniciar.setOnClickListener{
            signIn()

        }
        visible()

    }

    fun load(){
        progressBar.visibility = View.VISIBLE
        buttonIniciar.visibility = View.INVISIBLE
        googleButton.visibility = View.INVISIBLE
        textInputLayoutUser.visibility = View.INVISIBLE
        textInputLayoutPass.visibility = View.INVISIBLE
        reg1.visibility = View.INVISIBLE
        iniciacon.visibility = View.INVISIBLE
        circle.visibility = View.INVISIBLE
        registrate.visibility = View.INVISIBLE
    }
    fun visible(){
        progressBar.visibility = View.INVISIBLE
        buttonIniciar.visibility = View.VISIBLE
        googleButton.visibility = View.VISIBLE
        reg1.visibility = View.VISIBLE
        iniciacon.visibility = View.VISIBLE
        circle.visibility = View.VISIBLE
        registrate.visibility = View.VISIBLE
        textInputLayoutUser.visibility = View.VISIBLE
        textInputLayoutPass.visibility = View.VISIBLE
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // ...
                print("**************************************")
                Log.w(TAG,
                    "***************************\n"+e+"\n***************************")
                print(e)
                print("**************************************")
            }
        }
    }


    fun signIn(){
        if(semail.text?.isNotEmpty()!! && spassword.text?.isNotEmpty()!!){
            load()
            var email = semail.text.toString()
            var password = spassword.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {

                    //Verificamos que la tarea se ejecutó correctamente
                        task ->
                    if (task.isSuccessful) {
                        // Si se inició correctamente la sesión vamos a la vista Home de la aplicación
                        load()
                        val db = FirebaseFirestore.getInstance()

                        val userRef = db.collection("Users/").document("$email")
                        userRef.get()
                            .addOnSuccessListener { document ->
                                if(document.data != null){
                                    User.name = document.data?.get("name").toString()
                                }
                            }

                        val mainIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        visible()
                        // sino le avisamos el usuario que ocurrió un problema
                        Toast.makeText(this, "Usuario o contraseña incorrecto(s)",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
        else{
            Toast.makeText(this, "Email or password are empty",
                Toast.LENGTH_SHORT).show()
        }
    }



    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        load()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    val user = auth.currentUser
                    val email = user?.email
                    val name = user?.displayName
                    val mainActivity = Intent(this, MainActivity::class.java)

                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("Users/").document("$email")

                    userRef.get()
                        .addOnSuccessListener { document ->
                            if(document.data == null){
                                val  setupIntent = Intent(this, StartupSetup::class.java).putExtra("email",email)
                                startActivity(setupIntent)
                            }
                            else{
                                val db = FirebaseFirestore.getInstance()
                                User.name = "undefined"
                                val userRef = db.collection("Users/").document("$email")
                                userRef.get()
                                    .addOnSuccessListener { document ->
                                        if(document.data != null){
                                            User.name = document.data?.get("name").toString()
                                            User.email = email.toString()
                                            startActivity(mainActivity)
                                            finish()
                                        }
                                        else{
                                            startActivity(mainActivity)
                                            finish()
                                        }
                                    }
                            }
                        }

                } else {
                    // If sign in fails, display a message to the user.
                }

                // ...
            }
    }


    override fun onStart() {
        super.onStart()
        val  account = GoogleSignIn.getLastSignedInAccount(this);
    }
}
