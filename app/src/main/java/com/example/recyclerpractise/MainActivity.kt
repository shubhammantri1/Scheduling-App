package com.example.recyclerpractise

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var dbHelper: SchDbHandler
    lateinit var adapter: Adapter
    lateinit var context: Context
    lateinit var alarmManager: AlarmManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = "Maintainer"
        dbHelper = SchDbHandler(this)
        context = this
        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this, dbHelper.readData())
        recyclerView.adapter = adapter
        fab.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Enter Information")
            val view = layoutInflater.inflate(R.layout.schedule_dialog, null)
            val hourTime = view.findViewById<EditText>(R.id.ev_hour)
            val minTime = view.findViewById<EditText>(R.id.ev_minute)
            val amp = view.findViewById<EditText>(R.id.ev_amp)
            val text = view.findViewById<EditText>(R.id.ev_text)
            dialog.setView(view)
            dialog.setPositiveButton("Add"){_: DialogInterface, _: Int ->
                if(minTime.text.isNotEmpty() && hourTime.text.isNotEmpty() && amp.text.isNotEmpty() && text.text.isNotEmpty()){
                    val user = User()
                    user.time = hourTime.text.toString()+ ":" + minTime.text.toString()
                    user.amp = amp.text.toString()
                    user.text = text.text.toString()
                    user.notifyMe = false
                    dbHelper.insertData(user)
                    adapter.notifyItemChanged(dbHelper.readData().size - 1)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { _: DialogInterface, _:Int ->
            }
            dialog.show()
        }
    }

    fun updateUser(user: User){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update Information")
        val view = layoutInflater.inflate(R.layout.schedule_dialog, null)
        val hourTime = view.findViewById<EditText>(R.id.ev_hour)
        val minTime = view.findViewById<EditText>(R.id.ev_minute)
        val amp = view.findViewById<EditText>(R.id.ev_amp)
        val text = view.findViewById<EditText>(R.id.ev_text)
        dialog.setView(view)
        dialog.setPositiveButton("Update"){_: DialogInterface, _: Int ->
            if(minTime.text.isNotEmpty() && hourTime.text.isNotEmpty() && amp.text.isNotEmpty() && text.text.isNotEmpty()){
                user.time = hourTime.text.toString() + ":" + minTime.text.toString()
                user.amp = amp.text.toString()
                user.text = text.text.toString()
                user.notifyMe = user.notifyMe
                dbHelper.updateData(user)
                adapter.notifyItemChanged(dbHelper.readData().size - 1)
                refreshList()
            }
        }
        dialog.setNegativeButton("Cancel") { _: DialogInterface, _:Int ->
        }
        dialog.show()
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    fun refreshList(){
        recycler.adapter = Adapter(this, dbHelper.readData())
    }

    fun notifyMeUpdate(user : User){
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        dbHelper.updateData(user)
        Toast.makeText(context, "Notification On", Toast.LENGTH_SHORT).show()
        user.notifyMe = true
        var hour = 0
        var min  = 0
        for(i in 0 until user.time.length) {
            if (user.time[i] == ':'){
                var str = user.time.subSequence(0, i).toString()
                var minStr = user.time.subSequence(i+1, user.time.length).toString()
                if(user.amp == "AM"){
                    hour = str.toInt()
                    min = minStr.toInt()
                }
                else{
                    hour = str.toInt() + 12
                    min = minStr.toInt()
                }
            }
        }
        val cur_cal: Calendar = GregorianCalendar()
        cur_cal.timeInMillis = System.currentTimeMillis() //set the current time and date for this calendar

        val cal: Calendar = GregorianCalendar()
        cal.add(Calendar.DAY_OF_YEAR, cur_cal[Calendar.DAY_OF_YEAR])
        cal[Calendar.HOUR_OF_DAY] = hour
        cal[Calendar.MINUTE] = min
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = cur_cal[Calendar.MILLISECOND]
        cal[Calendar.DATE] = cur_cal[Calendar.DATE]
        cal[Calendar.MONTH] = cur_cal[Calendar.MONTH]
        val intent = Intent(context, Receiever::class.java)
        intent.putExtra("Description", user.text)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
    }

    class Receiever: BroadcastReceiver(){
        lateinit var notificationManager: NotificationManager
        lateinit var notificationChannel: NotificationChannel
        lateinit var builder: Notification.Builder
        private var channelId = "package com.example.recyclerpractise"
        private var description:String? = "Text Notification"
        override fun onReceive(context: Context?, intent: Intent?){
            val itnt =  Intent(context, LauncherActivity::class.java)
            val pintent = PendingIntent.getActivity(context, 0 , itnt, PendingIntent.FLAG_UPDATE_CURRENT)
            description = intent?.getStringExtra("Description")
            if (context != null) {
                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel =
                    NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)

                if (context != null) {
                    builder = Notification.Builder(context, channelId)
                        .setContentTitle("Hello Moto")
                        .setContentText(description)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground))
                        .setContentIntent(pintent)
                }
                else{
                    builder = Notification.Builder(context)
                        .setContentTitle("Hello Moto")
                        .setContentText(description)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setLargeIcon(BitmapFactory.decodeResource(context?.resources, R.drawable.ic_launcher_foreground))
                        .setContentIntent(pintent)
                }
                notificationManager.notify(19867, builder.build())
            }
        }
    }
}
