package com.app.testik.util.cursor

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import com.app.testik.R
import com.app.testik.util.loadAvatar
import de.hdodenhof.circleimageview.CircleImageView

class UserCursorAdapter(context: Context) : CursorAdapter(context, cursor, 0) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.item_search, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        if (view == null || context == null || cursor == null) return

        val tvUsername = view.findViewById(R.id.tvUsername) as TextView
        val tvNameSurname = view.findViewById(R.id.tvNameSurname) as TextView
        val ivAvatar = view.findViewById(R.id.ivAvatar) as CircleImageView

        tvUsername.text = cursor.getString(cursor.getColumnIndexOrThrow("username"))
        tvNameSurname.text = cursor.getString(cursor.getColumnIndexOrThrow("nameSurname"))
        loadAvatar(context, ivAvatar, cursor.getString(cursor.getColumnIndexOrThrow("avatar")))
    }

    companion object {
        val cursor = MatrixCursor(
            arrayOf(
                BaseColumns._ID,
                "username",
                "nameSurname",
                "avatar"
            )
        )
    }
}