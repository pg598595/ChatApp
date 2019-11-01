package com.example.chatapp

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MessageAdapter(context: Context, resource: Int, objects: List<MessageFormat>) :
    ArrayAdapter<MessageFormat>(context, resource, objects) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view

        val message = getItem(position)

        when {
            TextUtils.isEmpty(message!!.message) -> {


                convertView =
                    (context as Activity).layoutInflater.inflate(R.layout.user_connected, parent, false)

                val messageText = convertView!!.findViewById(R.id.message_body)  as TextView


                val userConnected = message.username
                messageText.text = userConnected

            }
            message.uniqueId.equals(MainActivity.uniqueId) -> {
                Log.i(
                    MainActivity.TAG,
                    "getView: " + message.uniqueId + " " + MainActivity.uniqueId
                )

                convertView =
                    (context as Activity).layoutInflater.inflate(R.layout.my_message, parent, false)
                val messageText = convertView!!.findViewById(R.id.message_body) as TextView
                messageText.text = message.message

                val messageTime = convertView.findViewById(R.id.message_time) as TextView
                messageTime.text = message.time

            }
            else -> {

                convertView =
                    (context as Activity).layoutInflater.inflate(R.layout.their_message, parent, false)

                val messageText = convertView!!.findViewById(R.id.message_body) as TextView
                val usernameText = convertView.findViewById(R.id.name) as TextView
                messageText.visibility = View.VISIBLE
                usernameText.visibility = View.VISIBLE

                messageText.text = message.message
                usernameText.text = message.username


                val messageTime = convertView.findViewById(R.id.message_time) as TextView
                messageTime.text = message.time

            }
        }

        return convertView
    }
}
