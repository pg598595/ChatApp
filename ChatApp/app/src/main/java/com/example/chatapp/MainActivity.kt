package com.example.chatapp

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.emitter.Emitter.Listener

import org.json.JSONException
import org.json.JSONObject

import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var textField: EditText? = null
    private var sendButton: ImageButton? = null

    private var Username: String? = null

    private var hasConnection: Boolean? = false

    private var messageListView: ListView? = null
    private var messageAdapter: MessageAdapter? = null

    private var thread2: Thread? = null
    private var startTyping = false
    private var time = 2

    private var mSocket: Socket? = null

    @SuppressLint("HandlerLeak")
    internal var handler2: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i(TAG, "handleMessage: typing stopped $startTyping")
            if (time == 0) { title = "ChatApp"
                Log.i(TAG, "handleMessage: typing stopped time is $time")
                startTyping = false
                time = 2
            }

        }
    }

    private var onNewMessage: Listener = Listener { args ->
        runOnUiThread(Runnable {
            Log.i(TAG, "run: ")
            Log.i(TAG, "run: " + args.size)
            val data = args[0] as JSONObject
            val username: String
            val message: String
            val id: String
            val time:String
            try {
                username = data.getString("username")
                message = data.getString("message")
                id = data.getString("uniqueId")
                time = data.getString("time")

                Log.i(TAG, "run: username---$username     message---$message    id---$id     time---$time")

                val format = MessageFormat(id, username, message, time)
                Log.i(TAG, "run:4 ")
                messageAdapter!!.add(format)
                Log.i(TAG, "run:5 ")

            } catch (e: Exception) {
                return@Runnable
            }
        })


    }

    private var onNewUser: Listener = Listener { args ->
        runOnUiThread(Runnable {
            val length = args.size

            if (length == 0) {
                return@Runnable
            }
            Log.i(TAG, "run: ")
            Log.i(TAG, "run: " + args.size)
            var username = args[0].toString()
            try {
                val `object` = JSONObject(username)
                username = `object`.getString("username")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val format = MessageFormat(null, username, null,null)
            messageAdapter!!.add(format)
            messageListView!!.smoothScrollToPosition(0)
            messageListView!!.scrollTo(0, messageAdapter!!.count - 1)
            Log.i(TAG, "run: $username")
        })





    }


    private var onTyping: Listener = Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            Log.i(TAG, "run: " + args[0])
            try {
                var typingOrNot: Boolean = data.getBoolean("typing")
                val userName = data.getString("username") + " is Typing......"
                val id = data.getString("uniqueId")

                if (id == uniqueId) {
                    typingOrNot = false
                } else {
                    title = userName
                }

                if (typingOrNot) {

                    if (!startTyping) {
                        startTyping = true
                        thread2 = Thread(
                                object : Runnable {
                                    override fun run() {
                                        while (time > 0) {
                                            synchronized(this) {
                                                try {
//                                                    wait(1000)
                                                    Log.i(TAG, "run: typing $time")
                                                } catch (e: InterruptedException) {
                                                    e.printStackTrace()
                                                }

                                                time--
                                            }
                                            handler2.sendEmptyMessage(0)
                                        }

                                    }
                                }
                        )
                        thread2!!.start()
                    } else {
                        time = 2
                    }

                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
    }







    init {
        try {
            mSocket = IO.socket("https://stormy-earth-18482.herokuapp.com/")
        } catch (e: URISyntaxException) {
        }

    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Username = intent.getStringExtra("username")

        uniqueId = UUID.randomUUID().toString()
        Log.i(TAG, "onCreate: $uniqueId")

        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection")
        }

        if (hasConnection!!) {

        } else {
            mSocket!!.connect()
            mSocket!!.on("connect user", onNewUser)
            mSocket!!.on("chat message", onNewMessage)
            mSocket!!.on("on typing", onTyping)

            val userId = JSONObject()
            try {
                userId.put("username", Username!! + " Connected")
                mSocket!!.emit("connect user", userId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        Log.i(TAG, "onCreate: " + hasConnection!!)
        hasConnection = true


        Log.i(TAG, "onCreate: $Username Connected")

        textField = findViewById(R.id.textField)
        sendButton = findViewById(R.id.sendButton)
        messageListView = findViewById(R.id.messageListView)

        val messageFormatList = ArrayList<MessageFormat>()
        messageAdapter = MessageAdapter(this, R.layout.item_message, messageFormatList)
        messageListView!!.adapter = messageAdapter

       // onTypeButtonEnable()
    }






    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("hasConnection", hasConnection!!)
    }







    private fun onTypeButtonEnable() {
        textField!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val onTyping = JSONObject()
                try {
                    onTyping.put("typing", false)
                    onTyping.put("username", Username)
                    onTyping.put("uniqueId", uniqueId)
                    mSocket!!.emit("on typing", onTyping)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                val onTyping = JSONObject()
                try {
                    onTyping.put("typing", true)
                    onTyping.put("username", Username)
                    onTyping.put("uniqueId", uniqueId)
                    mSocket!!.emit("on typing", onTyping)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                sendButton!!.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {
                val onTyping = JSONObject()
                try {
                    onTyping.put("typing", false)
                    onTyping.put("username", Username)
                    onTyping.put("uniqueId", uniqueId)
                    mSocket!!.emit("on typing", onTyping)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        })
    }








    fun sendMessage(view: View) {
        Log.i(TAG, "sendMessage: ")
        val message = textField!!.text.toString().trim { it <= ' ' }

        val sdf = SimpleDateFormat("hh:mm a")
        val currentDate = sdf.format(Date())

        if (TextUtils.isEmpty(message)) {
            Log.i(TAG, "sendMessage:2 ")
            return
        }
        textField!!.setText("")
        val jsonObject = JSONObject()
        try {
            jsonObject.put("message", message)
            jsonObject.put("username", Username)
            jsonObject.put("uniqueId", uniqueId)
            jsonObject.put("time",currentDate)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.i(TAG, "sendMessage: 1" + mSocket!!.emit("chat message", jsonObject))
    }











    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            Log.i(TAG, "onDestroy: ")

            val userId = JSONObject()
            try {
                userId.put("username", Username!! + " DisConnected")
                mSocket!!.emit("connect user", userId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mSocket!!.disconnect()
            mSocket!!.off("chat message", onNewMessage)
            mSocket!!.off("connect user", onNewUser)
            mSocket!!.off("on typing", onTyping)
            Username = ""
            messageAdapter!!.clear()
        } else {
            Log.i(TAG, "onDestroy: is rotating.....")
        }

    }

    companion object {

        val TAG = "MainActivity"
        var uniqueId: String = ""
    }


}
