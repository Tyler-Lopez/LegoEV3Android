package com.example.legoev3android.domain.use_case

import com.example.legoev3android.services.MyBluetoothService
import com.example.legoev3android.utils.Note

class PlayNoteUseCase {
    operator fun invoke(
        bluetoothService: MyBluetoothService,
        note: Note
    ) {
        bluetoothService.playSound(note)
    }
}