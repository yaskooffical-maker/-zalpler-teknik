package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_requests")
data class ServiceRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val customerName: String,
    val customerPhone: String,
    val deviceType: String,
    val issueDescription: String,
    val address: String,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = STATUS_NEW, // "Yeni Talep", "İşleme Alındı", "Tamamlandı", "İptal"
    val technicianNote: String = ""
) {
    companion object {
        const val STATUS_NEW = "Yeni Talep"
        const val STATUS_IN_PROGRESS = "İşleme Alındı"
        const val STATUS_COMPLETED = "Tamamlandı"
        const val STATUS_CANCELLED = "İptal"

        val ALL_STATUSES = listOf(STATUS_NEW, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_CANCELLED)
    }
}
