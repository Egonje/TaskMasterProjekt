package com.example.taskmaster.models

import android.os.Parcel
import android.os.Parcelable

data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val mobile: Long = 0,
    val fcmToken: String = "",
    var completedTasks: Int = 0,
    var createdTasks: Int = 0,
    var selected: Boolean = false
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readLong(),
        source.readString()!!,
        source.readInt(),
        source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeLong(mobile)
        writeString(fcmToken)
        writeInt(completedTasks)
        writeInt(createdTasks)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<UserModel> = object : Parcelable.Creator<UserModel> {
            override fun createFromParcel(source: Parcel): UserModel = UserModel(source)
            override fun newArray(size: Int): Array<UserModel?> = arrayOfNulls(size)
        }
    }
}