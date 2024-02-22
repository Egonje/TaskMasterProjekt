package com.example.taskmaster.models

import android.os.Parcel
import android.os.Parcelable

data class TaskBoardModel (
    val name: String = "",
    val image: String = "",
    val whoCreated: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentId: String = "",
    var taskList: ArrayList<TaskModel> = ArrayList(),
    var backgroundImage: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(TaskModel.CREATOR)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(whoCreated)
        parcel.writeStringList(assignedTo)
        parcel.writeString(documentId)
        parcel.writeTypedList(taskList)
        parcel.writeString(backgroundImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskBoardModel> {
        override fun createFromParcel(parcel: Parcel): TaskBoardModel {
            return TaskBoardModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskBoardModel?> {
            return arrayOfNulls(size)
        }
    }
}