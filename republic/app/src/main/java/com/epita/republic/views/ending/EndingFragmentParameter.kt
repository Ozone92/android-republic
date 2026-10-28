package com.epita.republic.views.ending

import android.os.Parcel
import android.os.Parcelable
import com.epita.republic.domain.Character

data class EndingFragmentParameter(
    val playerIndex: Int,
    val popularity: Int,
    val vote: Int,
    val isBot: Boolean,
    val character: Character

): Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(playerIndex)
        dest.writeInt(popularity)
        dest.writeInt(vote)
        dest.writeBoolean(isBot)
        dest.writeInt(character.ordinal)
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readBoolean(),
        Character.entries[parcel.readInt()]
    )

    companion object CREATOR: Parcelable.Creator<EndingFragmentParameter?> {
        override fun createFromParcel(`in`: Parcel): EndingFragmentParameter {
            return EndingFragmentParameter(`in`)
        }

        override fun newArray(size: Int): Array<EndingFragmentParameter?> {
            return arrayOfNulls(size)
        }
    }
}
