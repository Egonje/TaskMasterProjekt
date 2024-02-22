package com.example.taskmaster.models

import android.os.Parcel
import android.os.Parcelable


data class CardModel(
    val name: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val labelColor: String = "",
    val dueDate: Long = 0
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.createStringArrayList()!!,
        source.readString()!!,
        source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(labelColor)
        writeLong(dueDate)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CardModel> = object : Parcelable.Creator<CardModel> {
            override fun createFromParcel(source: Parcel): CardModel = CardModel(source)
            override fun newArray(size: Int): Array<CardModel?> = arrayOfNulls(size)
        }
    }
}