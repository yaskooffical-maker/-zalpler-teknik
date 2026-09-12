package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object ContactUtils {
    const val PHONE_NUMBER = "05343105317"
    const val PHONE_DISPLAY = "0534 310 53 17"
    const val WHATSAPP_NUMBER = "905343105317"
    const val DEFAULT_WHATSAPP_MSG = "Merhaba, servis hizmeti almak istiyorum."

    fun call(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$PHONE_NUMBER")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Arama başlatılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, message: String = DEFAULT_WHATSAPP_MSG) {
        val encoded = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }
        
        // Try direct WhatsApp app intent first
        try {
            val whatsappUri = Uri.parse("whatsapp://send?phone=$WHATSAPP_NUMBER&text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            // Fallback to https://wa.me web link which system resolves to WhatsApp or browser
        }

        try {
            val webUri = Uri.parse("https://wa.me/$WHATSAPP_NUMBER?text=$encoded")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp açılamadı: $PHONE_DISPLAY", Toast.LENGTH_LONG).show()
        }
    }

    fun callCustomer(context: Context, phone: String) {
        val sanitized = phone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$sanitized")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Arama başlatılamadı: $phone", Toast.LENGTH_SHORT).show()
        }
    }

    fun messageCustomer(context: Context, phone: String, message: String) {
        var sanitized = phone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").replace("+", "")
        if (sanitized.startsWith("0")) {
            sanitized = "90" + sanitized.substring(1)
        } else if (!sanitized.startsWith("90") && sanitized.length == 10) {
            sanitized = "90$sanitized"
        }

        val encoded = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        try {
            val whatsappUri = Uri.parse("whatsapp://send?phone=$sanitized&text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {
        }

        try {
            val webUri = Uri.parse("https://wa.me/$sanitized?text=$encoded")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp açılamadı: $phone", Toast.LENGTH_LONG).show()
        }
    }

    fun openMapLocation(context: Context, address: String) {
        try {
            val encoded = URLEncoder.encode(address, "UTF-8")
            val uri = Uri.parse("geo:0,0?q=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Harita açılamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
