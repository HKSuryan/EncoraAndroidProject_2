package com.example.takeanote1.data.repository

import com.example.takeanote1.data.local.dao.NotesDao
import com.example.takeanote1.data.local.entity.NoteEntity

class NotesRepository(
    private val dao: NotesDao
) {
    fun getNotes(uid: String) = dao.getNotes(uid)

    suspend fun addNote(note: NoteEntity) {
        dao.insertNote(note)
    }
}
