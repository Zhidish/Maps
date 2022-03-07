package com.leobit.pizzadelivery.ui.dialog
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.leobit.pizzadelivery.R




class DeliveryDialong : DialogFragment() {
    internal lateinit var listener: NoticeDialogListener


    interface NoticeDialogListener{
        fun onDialogPositiveClick(dialong: DialogFragment)
        fun onNegativeDialogListener(dialong:DialogFragment)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.pizza_house)
                .setPositiveButton(R.string.deliver_pizza,
                    DialogInterface.OnClickListener { dialog, id ->
                      listener.onDialogPositiveClick(this)
                    })
                .setNegativeButton(R.string.deliver_pizza_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                      listener.onNegativeDialogListener(this)
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
}
