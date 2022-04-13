package com.example.legoev3android.utils

// https://learn.digilentinc.com/Documents/400#:~:text=Piano%20Octaves&text=All%20seven%20octaves%20on%20a,so%20we%20will%20ignore%20them.
sealed class Note(val byte1: Byte, val byte2: Byte) {
    object C : Note(0x06, 0x01)
    object CSharp : Note(0x15, 0x01)
    object D : Note(0x26, 0x01)
    object DSharp : Note(0x37, 0x01)
    object E : Note(0x4A, 0x01)
    object F : Note(0x5D, 0x01)
    object FSharp : Note(0x72, 0x01)
    object G : Note(0x88.toByte(), 0x01)
    object GSharp : Note(0x9F.toByte(), 0x01)
    object A : Note(0xB8.toByte(), 0x01)
    object ASharp : Note(0xD2.toByte(), 0x01)
    object B : Note(0xEE.toByte(), 0x01)
}
