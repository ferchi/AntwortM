package edu.itq.antwort.Adapters

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import edu.itq.antwort.Activities.QuestionDetails
import edu.itq.antwort.Classes.Notifications
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ItemNotificationBinding

class NotificationAdapter (private val fragment: Fragment, private val notifications: MutableList<Notifications>):
RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private lateinit var popUpMenu :  PowerMenu.Builder
    private val db = FirebaseFirestore.getInstance()
    private var notificationPosition: Int = 0
    private var id: String = ""

    class ViewHolder(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }//onCreateViewHolder

    override fun getItemCount() = notifications.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val notification = notifications[position]
        popUpMenu = PowerMenu.Builder(fragment.requireContext())

        holder.binding.imgNotificationOptions.setOnClickListener {

            popUpMenu.build().clearPreference()
            createPopUpOwner()
            notificationPosition = holder.layoutPosition
            popUpMenu.build().showAsDropDown(it)
            id = notification.id

        }//se dio click en opciones de notificaciones
        
        holder.binding.cvNotification.setOnClickListener {
        
            val homeIntent = Intent(fragment.requireContext(), QuestionDetails::class.java).apply {
            
                putExtra("id", notifications[position].question)
            
            }//homeIntent
            
            fragment.startActivity(homeIntent)
            
        }//setOnClickListener
        
        holder.binding.txtTitleIN.text = notifications[position].title
        holder.binding.txtContentNI.text = notifications[position].content
        
    }//onBindViewHolder
    
    private fun createPopUpOwner(){

        val context = fragment.requireContext()
        popUpMenu = PowerMenu.Builder(fragment.requireContext())
        popUpMenu
            .addItem(PowerMenuItem("Eliminar", false))
            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(context, R.color.black))
            .setTextGravity(Gravity.CENTER)
            //.setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListener)
            .setAutoDismiss(true)
            .build()

    }//createPopUpOwner

    private val onMenuItemClickListener: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            when(item.title)
            {
                "Eliminar" -> {
                    showAlert()
                }
            }
        }//onMenuItemClickListener

    private fun showAlert(){

        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle("Eliminar notificación")
        builder.setMessage("¿Esta seguro que desea elimiar la notificación?")
        builder.setPositiveButton("Sí"
        ) { _, _ ->

            deleteNotification(id)
        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun deleteNotification(id: String) {

        db.collection("Notifications").document(id).delete()

        notifications.removeAt(notificationPosition)
        this.notifyItemRemoved(notificationPosition)
        
    }//deleteNotification


}//borrar notificaciones

