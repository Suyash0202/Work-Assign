package com.example.workassign


import android.content.Context
import android.net.*

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var callback: ((Boolean) -> Unit)? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback?.invoke(true)
        }

        override fun onLost(network: Network) {
            callback?.invoke(false)
        }
    }

    fun startListening(onStatusChange: (Boolean) -> Unit) {
        callback = onStatusChange
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
