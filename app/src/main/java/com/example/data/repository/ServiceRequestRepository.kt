package com.example.data.repository

import com.example.data.local.ServiceRequestDao
import com.example.data.model.ServiceRequest
import kotlinx.coroutines.flow.Flow

class ServiceRequestRepository(private val dao: ServiceRequestDao) {
    val allRequests: Flow<List<ServiceRequest>> = dao.getAllRequests()

    fun getRequestById(id: Long): Flow<ServiceRequest?> = dao.getRequestById(id)

    fun getRequestsByStatus(status: String): Flow<List<ServiceRequest>> = dao.getRequestsByStatus(status)

    suspend fun insertRequest(request: ServiceRequest): Long = dao.insertRequest(request)

    suspend fun updateRequest(request: ServiceRequest) = dao.updateRequest(request)

    suspend fun deleteRequest(request: ServiceRequest) = dao.deleteRequest(request)

    suspend fun deleteRequestById(id: Long) = dao.deleteRequestById(id)

    suspend fun updateStatus(id: Long, status: String) = dao.updateStatus(id, status)

    suspend fun updateTechnicianNote(id: Long, note: String) = dao.updateTechnicianNote(id, note)
}
