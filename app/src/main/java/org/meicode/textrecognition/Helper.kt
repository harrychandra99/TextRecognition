package org.meicode.textrecognition

import android.content.Context
import android.widget.Toast

object Helper {
    fun toastText(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}