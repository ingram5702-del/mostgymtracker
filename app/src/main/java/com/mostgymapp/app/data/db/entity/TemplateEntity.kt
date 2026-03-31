package com.mostgymapp.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "template",
    indices = [Index(value = ["name"], unique = true)]
)
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
