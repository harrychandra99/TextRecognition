package org.meicode.textrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.meicode.textrecognition.databinding.ActivityStorageBinding

class StorageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStorageBinding
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReadData.setOnClickListener {
            val title: String = binding.edtTitle.text.toString()
            if(title.isNotEmpty()){
                    readData(title)
                } else{
                    Helper.toastText(this,"Please Enter The Title")
                }
        }
    }

    fun readData(title: String){
        databaseReference = FirebaseDatabase.getInstance().getReference("Data")
        databaseReference.child(title).get().addOnSuccessListener {
            if(it.exists()){
                val dataTitle = it.child("dataTitle").value
                val dataText = it.child("dataText").value
                Helper.toastText(this,"Successfull Read")
                binding.edtTitle.text.clear()
                binding.tvTitleResult.text = dataTitle.toString()
                binding.tvTextDataResult.text = dataText.toString()
            } else{
                Helper.toastText(this,"Title Doesnt Exit")
            }
        }.addOnFailureListener {
            Helper.toastText(this,"Failed")
        }
    }
}