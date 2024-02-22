package com.example.taskmaster.models

import android.os.Parcel
import android.os.Parcelable

data class TaskModel(
    var title: String = "",
    val createdBy: String = "",
    var cards: ArrayList<CardModel> = ArrayList()
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.createTypedArrayList(CardModel.CREATOR)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(createdBy)
        writeTypedList(cards)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TaskModel> = object : Parcelable.Creator<TaskModel> {
            override fun createFromParcel(source: Parcel): TaskModel = TaskModel(source)
            override fun newArray(size: Int): Array<TaskModel?> = arrayOfNulls(size)
        }
    }
}