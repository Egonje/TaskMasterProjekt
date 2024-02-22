package com.example.taskmaster.models

import android.os.Parcel
import android.os.Parcelable

data class SelectedFriendsModel(
    val id: String = "",
    val image: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(image)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SelectedFriendsModel> =
            object : Parcelable.Creator<SelectedFriendsModel> {
                override fun createFromParcel(source: Parcel): SelectedFriendsModel =
                    SelectedFriendsModel(source)

                override fun newArray(size: Int): Array<SelectedFriendsModel?> = arrayOfNulls(size)
            }
    }
}