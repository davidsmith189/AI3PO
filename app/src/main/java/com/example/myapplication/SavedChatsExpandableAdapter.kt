package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.google.gson.Gson

class SavedChatsExpandableAdapter(
    private val ctx: Context,
    private val subjects: List<String>,
    private val data: Map<String, List<SavedChat>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount() = subjects.size
    override fun getChildrenCount(groupPos: Int) =
        data[subjects[groupPos]]?.size ?: 0
    override fun getGroup(groupPos: Int) = subjects[groupPos]
    override fun getChild(groupPos: Int, childPos: Int) =
        data[subjects[groupPos]]!![childPos]
    override fun getGroupId(groupPos: Int) = groupPos.toLong()
    override fun getChildId(groupPos: Int, childPos: Int) = childPos.toLong()
    override fun hasStableIds() = false
    override fun isChildSelectable(groupPos: Int, childPos: Int) = true

    override fun getGroupView(
        groupPos: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View {
        val title = getGroup(groupPos) as String
        val view = convertView ?: LayoutInflater.from(ctx)
            .inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        view.findViewById<TextView>(android.R.id.text1).text = title
        return view
    }

    override fun getChildView(
        groupPos: Int, childPos: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        val chat = getChild(groupPos, childPos) as SavedChat
        val view = convertView ?: LayoutInflater.from(ctx)
            .inflate(R.layout.item_chat, parent, false)
        view.findViewById<TextView>(R.id.chat_title).text = chat.title
        view.findViewById<TextView>(R.id.chat_message).text = chat.lastMessage
        val deleteIcon=view.findViewById<View>(R.id.trash_icon)
        view.setOnClickListener {
            // same launch logic you had in SavedChatsAdapter
            val intent = Intent(ctx, ChatDetailActivity::class.java)
            intent.putExtra("chat_title", chat.title)
            intent.putExtra("chat_messages", Gson().toJson(chat.messages))
            ctx.startActivity(intent)
        }
        deleteIcon.setOnClickListener{
            deleteChat(chat.title){
                val mutableList=data[subjects[groupPos]]?.toMutableList()
                mutableList?.removeAt(childPos)
                (data as MutableMap)[subjects[groupPos]]=mutableList?: emptyList()
                notifyDataSetChanged()
            }
        }

        return view
    }
    private fun deleteChat(title:String,onDeleted:()->Unit){
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("saved_chats")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    db.collection("users")
                        .document(userId)
                        .collection("saved_chats")
                        .document(doc.id)
                        .delete()
                }
                onDeleted()
            }

    }
}
