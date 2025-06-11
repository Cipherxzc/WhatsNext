package com.cipherxzc.whatsnext.ui.core.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.whatsnext.data.repository.CloudRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncViewModel(
    application: Application,
    private val todoDataViewModel: TodoDataViewModel
) : AndroidViewModel(application) {
    companion object {
        private const val PREFS_NAME = "sync_prefs"
        private const val KEY_LAST_SYNC = "last_sync_millis"
    }

    private val prefs: SharedPreferences = application.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val cloudRepo: CloudRepository by lazy {
        CloudRepository(FirebaseFirestore.getInstance())
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private fun loadLastSync(): Timestamp {
        val millis = prefs.getLong(KEY_LAST_SYNC, 0L)
        val seconds = millis / 1000
        val nanos = ((millis % 1000) * 1_000_000).toInt()
        return Timestamp(seconds, nanos)
    }

    private fun saveLastSync(timestamp: Timestamp) {
        val millis = timestamp.toDate().time // 将 Timestamp 转换为毫秒级的时间戳
        prefs.edit() { putLong(KEY_LAST_SYNC, millis) }
    }

    fun sync(
        onComplete: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                val since = loadLastSync()
                val userId = todoDataViewModel.getCurrentUser()
                val now = Timestamp.now()

                // 拉取云端增量 Item
                val updatedItems   = cloudRepo.fetchUpdatedItems(userId, since)
                todoDataViewModel.upsertItems(updatedItems)

                // 推送本地未同步的 Item
                val unSyncedItems   = todoDataViewModel.getUnSyncedItems()
                cloudRepo.pushItems(userId, unSyncedItems)
                todoDataViewModel.upsertItems(unSyncedItems)

                // 更新本地最后同步时间
                saveLastSync(now)

                onComplete?.invoke()
            } catch (e: Throwable) {
                onError?.invoke(e)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

class SyncViewModelFactory(
    private val application: Application,
    private val todoDataViewModel: TodoDataViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == SyncViewModel::class.java)
        return SyncViewModel(application, todoDataViewModel) as T
    }
}