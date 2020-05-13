package mx.lifehealthsolutions.myhealthjournal.models

import android.R
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object User: Comparable<User> {

    val conditions_list =  ArrayList<Condition>()
    val medicine_list = ArrayList<Medicine>()
    lateinit var nombre: String
    lateinit var email: String
    lateinit var password: String
    var db = FirebaseFirestore.getInstance()

    override fun compareTo(other: User): Int {
        return nombre.compareTo(other.nombre)
    }

    fun upload(){
        //upload to the cloud

    }

    fun downloadConditionNames(context: Context): SpinnerAdapter {
        //download from the cloud
        val user = FirebaseAuth.getInstance().currentUser?.email
        var adapter: SpinnerAdapter
        var conditions_string = ArrayList<String>()
        conditions_string.add("Conditions")
        db.collection("Users/$user/Conditions")
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    conditions_string.add(document.id)
                }
            }
            .addOnFailureListener { exception ->

                conditions_string.add("No conditions found")

            }
        adapter = ArrayAdapter(context, R.layout.simple_spinner_item, conditions_string) as SpinnerAdapter
        return adapter
    }

    fun delete(){
        //todo make a function that deletes from firebase
    }

    fun addCondition(condition: Condition){
        conditions_list.add(condition)
    }

    fun removeCondition(condition: Condition){
        conditions_list.remove(condition)
    }

    fun addMedicine(medicine: Medicine){
        medicine_list.add(medicine)
    }

    fun removeMedicine(medicine: Medicine){
        medicine_list.remove(medicine)
    }
}