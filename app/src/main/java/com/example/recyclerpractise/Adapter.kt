package com.example.recyclerpractise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class Adapter(val activity: MainActivity, private val userList: MutableList<User>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(v: View):RecyclerView.ViewHolder(v){
        val time = v.findViewById<TextView>(R.id.timePicker)
        val amp = v.findViewById<TextView>(R.id.ampmPicker)
        val text = v.findViewById<TextView>(R.id.tv_text)
        val img = v.findViewById<ImageView>(R.id.iv_menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_view, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val user: User = userList[p1]
        p0.time.text = user.time
        p0.text.text = user.text
        p0.amp.text = user.amp
        p0.img.setOnClickListener{
            val popup = PopupMenu(activity, p0.img)
            popup.inflate(R.menu.schedulemenu)
            popup.setOnMenuItemClickListener {

                when(it.itemId){
                    R.id.menu_delete->{
                        activity.dbHelper.deleteData(userList[p1].id.toLong())
                        activity.refreshList()
                    }
                    R.id.menu_update->{
                        activity.updateUser(userList[p1])
                    }
                    R.id.notify->{
                        activity.notifyMeUpdate(userList[p1])
                    }

                }
                true
            }
            popup.show()
        }
    }
}