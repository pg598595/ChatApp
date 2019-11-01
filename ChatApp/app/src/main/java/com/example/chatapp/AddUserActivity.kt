package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddUserActivity : AppCompatActivity() {

    private var setNickName: Button? = null
    private var userNickName: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        userNickName = findViewById(R.id.userNickName)
        setNickName = findViewById(R.id.setNickName)


        userNickName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.length > 0) {
                    setNickName!!.isEnabled = true
                    Log.i(MainActivity.TAG, "onTextChanged: ABLED")
                } else {
                    Log.i(MainActivity.TAG, "onTextChanged: DISABLED")
                    setNickName!!.isEnabled = false
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })


        setNickName!!.setOnClickListener {
            val intent = Intent(this@AddUserActivity, MainActivity::class.java)
            intent.putExtra("username", userNickName!!.text.toString())
            startActivity(intent)
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//
//    }


}
