package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ServiceRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRequestDao {
    @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<ServiceRequest>>

    @Query("SELECT * FROM service_requests WHERE id = :id")
    fun getRequestById(id: Long): Flow<ServiceRequest?>

    @Query("SELECT * FROM service_requests WHERE status = :status ORDER BY createdAt DESC")
    fun getRequestsByStatus(status: String): Flow<List<ServiceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequest): Long

    @Update
    suspend fun updateRequest(request: ServiceRequest)

    @Delete
    suspend fun deleteRequest(request: ServiceRequest)

    @Query("DELETE FROM service_requests WHERE id = :id")
    suspend fun deleteRequestById(id: Long)

    @Query("UPDATE service_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE service_requests SET technicianNote = :note WHERE id = :id")
    suspend fun updateTechnicianNote(id: Long, note: String)

    @Query("SELECT COUNT(*) FROM service_requests")
    fun getTotalCount(): Flow<Int>
}
