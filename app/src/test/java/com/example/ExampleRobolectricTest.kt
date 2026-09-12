package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ServiceRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Özalpler", appName)
  }

  @Test
  fun `room database service request persistence and status update`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.serviceRequestDao()

    val newRequest = ServiceRequest(
      customerName = "Mehmet Demir",
      customerPhone = "0532 111 22 33",
      deviceType = "Çamaşır Makinesi",
      issueDescription = "Kazan dönmüyor",
      address = "Kadıköy, İstanbul",
      status = ServiceRequest.STATUS_NEW
    )

    val id = dao.insertRequest(newRequest)
    val saved = dao.getRequestById(id).first()

    assertNotNull(saved)
    assertEquals("Mehmet Demir", saved?.customerName)
    assertEquals("Çamaşır Makinesi", saved?.deviceType)
    assertEquals(ServiceRequest.STATUS_NEW, saved?.status)

    // Update status to in progress
    dao.updateStatus(id, ServiceRequest.STATUS_IN_PROGRESS)
    val updated = dao.getRequestById(id).first()
    assertEquals(ServiceRequest.STATUS_IN_PROGRESS, updated?.status)

    // Update technician note
    dao.updateTechnicianNote(id, "Kayış değiştirildi")
    val withNote = dao.getRequestById(id).first()
    assertEquals("Kayış değiştirildi", withNote?.technicianNote)

    db.close()
  }

  @Test
  fun `app update info json parsing`() {
    val json = """
      {
        "versionCode": 3,
        "versionName": "1.2.0",
        "apkUrl": "https://example.com/ozalpler-v1.2.0.apk",
        "releaseNotes": "Kamera ile arıza fotoğrafı ekleme desteği.",
        "fileSizeMb": 15.4,
        "forceUpdate": true
      }
    """.trimIndent()

    val parsed = com.example.data.model.AppUpdateInfo.fromJson(json)
    assertNotNull(parsed)
    assertEquals(3, parsed?.versionCode)
    assertEquals("1.2.0", parsed?.versionName)
    assertEquals("https://example.com/ozalpler-v1.2.0.apk", parsed?.apkUrl)
    assertEquals("Kamera ile arıza fotoğrafı ekleme desteği.", parsed?.releaseNotes)
    assertEquals(15.4, parsed?.fileSizeMb ?: 0.0, 0.01)
    assertEquals(true, parsed?.forceUpdate)
  }

  @Test
  fun `update manager version comparison`() {
    val manager = com.example.util.UpdateManager

    // Higher versionCode
    val isHigherCode = manager.isVersionHigher(
      remoteCode = 2,
      remoteName = "1.0",
      localCode = 1,
      localName = "1.0"
    )
    assertEquals(true, isHigherCode)

    // Lower versionCode
    val isLowerCode = manager.isVersionHigher(
      remoteCode = 1,
      remoteName = "2.0",
      localCode = 2,
      localName = "1.0"
    )
    assertEquals(false, isLowerCode)

    // Same code, higher semver name
    val isHigherSemver = manager.isVersionHigher(
      remoteCode = 0,
      remoteName = "1.2.1",
      localCode = 0,
      localName = "1.2.0"
    )
    assertEquals(true, isHigherSemver)
  }

  @Test
  fun `update manager custom json url persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.util.UpdateManager

    val customUrl = "https://ozalplerteknik.com/api/latest_version.json"
    manager.setUpdateJsonUrl(context, customUrl)
    assertEquals(customUrl, manager.getUpdateJsonUrl(context))

    manager.resetUpdateJsonUrl(context)
    assertEquals(com.example.util.UpdateManager.DEFAULT_UPDATE_URL, manager.getUpdateJsonUrl(context))
  }
}
