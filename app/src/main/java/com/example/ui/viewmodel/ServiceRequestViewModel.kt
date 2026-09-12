package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ServiceRequest
import com.example.data.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiceRequestViewModel(
    application: Application,
    private val repository: ServiceRequestRepository
) : AndroidViewModel(application) {

    val allRequests: StateFlow<List<ServiceRequest>> = repository.allRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedStatusFilter = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")

    val filteredAdminRequests: StateFlow<List<ServiceRequest>> = combine(
        allRequests,
        selectedStatusFilter,
        searchQuery
    ) { requests, filter, query ->
        requests.filter { req ->
            val matchesFilter = filter == null || req.status == filter
            val matchesQuery = query.isBlank() ||
                    req.customerName.contains(query, ignoreCase = true) ||
                    req.customerPhone.contains(query, ignoreCase = true) ||
                    req.deviceType.contains(query, ignoreCase = true) ||
                    req.address.contains(query, ignoreCase = true) ||
                    req.issueDescription.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        seedSampleDataIfEmpty()
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            allRequests.collect { list ->
                if (list.isEmpty()) {
                    // Pre-fill a couple realistic service requests so the admin panel showcases functionality right away
                    repository.insertRequest(
                        ServiceRequest(
                            customerName = "Ahmet Yılmaz",
                            customerPhone = "0532 456 78 90",
                            deviceType = "Çamaşır Makinesi",
                            issueDescription = "Sıkma yaparken aşırı ses çıkarıyor ve su tahliyesi yapmıyor.",
                            address = "Bakırköy, Zuhuratbaba Mah. Demet Sok. No:14/2",
                            createdAt = System.currentTimeMillis() - 3600000 * 2,
                            status = ServiceRequest.STATUS_NEW,
                            technicianNote = "Müşteri ile 14:00 için randevulaşıldı."
                        )
                    )
                    repository.insertRequest(
                        ServiceRequest(
                            customerName = "Elif Demir",
                            customerPhone = "0541 234 56 78",
                            deviceType = "Buzdolabı",
                            issueDescription = "Alt bölme soğutmuyor, üst dondurucu aşırı buzlanma yapıyor.",
                            address = "Bahçelievler, Şirinevler Mah. Meriç Cad. No:8",
                            createdAt = System.currentTimeMillis() - 3600000 * 6,
                            status = ServiceRequest.STATUS_IN_PROGRESS,
                            technicianNote = "Sensör ve termostat değişimi planlandı, parça sipariş edildi."
                        )
                    )
                    repository.insertRequest(
                        ServiceRequest(
                            customerName = "Mustafa Kaya",
                            customerPhone = "0555 890 12 34",
                            deviceType = "Kombi",
                            issueDescription = "Sıcak su açıldığında ateşleme yapmıyor ve F28 hatası veriyor.",
                            address = "Beylikdüzü, Barış Mah. Ada Çiftliği Cad. A3 Blok",
                            createdAt = System.currentTimeMillis() - 86400000,
                            status = ServiceRequest.STATUS_COMPLETED,
                            technicianNote = "Ateşleme elektrodu temizlendi ve bar basıncı ayarlandı. Sorun giderildi."
                        )
                    )
                }
            }
        }
    }

    fun submitRequest(
        customerName: String,
        customerPhone: String,
        deviceType: String,
        issueDescription: String,
        address: String,
        photoUri: String?,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newRequest = ServiceRequest(
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                deviceType = deviceType.trim(),
                issueDescription = issueDescription.trim(),
                address = address.trim(),
                photoUri = photoUri,
                createdAt = System.currentTimeMillis(),
                status = ServiceRequest.STATUS_NEW
            )
            val id = repository.insertRequest(newRequest)
            onSuccess(id)
        }
    }

    fun updateStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(id, newStatus)
        }
    }

    fun updateTechnicianNote(id: Long, note: String) {
        viewModelScope.launch {
            repository.updateTechnicianNote(id, note)
        }
    }

    fun deleteRequest(id: Long) {
        viewModelScope.launch {
            repository.deleteRequestById(id)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = AppDatabase.getDatabase(application)
            val repository = ServiceRequestRepository(database.serviceRequestDao())
            return ServiceRequestViewModel(application, repository) as T
        }
    }
}
