package net.activitywatch.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.*
import net.activitywatch.android.AWPreferences
import net.activitywatch.android.R
import net.activitywatch.android.databinding.FragmentRemoteSyncSettingsBinding
import net.activitywatch.android.watcher.RemoteSyncWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RemoteSyncSettingsFragment : Fragment() {

    private var _binding: FragmentRemoteSyncSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: AWPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoteSyncSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = AWPreferences(requireContext())

        // Load saved preferences
        binding.etServerUrl.setText(prefs.getRemoteServerUrl())
        binding.switchRemoteSync.isChecked = prefs.isRemoteSyncEnabled()
        updateSyncStatus()

        // Save button
        binding.btnSave.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            val enabled = binding.switchRemoteSync.isChecked

            // 清理 URL：去掉 /#/ 及尾部斜杠
            val cleanUrl = url.trimEnd('/').removeSuffix("/#").removeSuffix("#").trimEnd('/')
            if (cleanUrl != url) {
                binding.etServerUrl.setText(cleanUrl)
            }
            if (enabled && cleanUrl.isEmpty()) {
                binding.tvUrlError.text = getString(R.string.remote_sync_url_empty_error)
                binding.tvUrlError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }
            binding.tvUrlError.visibility = android.view.View.GONE

            prefs.setRemoteServerUrl(cleanUrl)
            prefs.setRemoteSyncEnabled(enabled)

            if (enabled) {
                schedulePeriodicSync()
                Toast.makeText(requireContext(), R.string.remote_sync_saved_toast, Toast.LENGTH_SHORT).show()
            } else {
                cancelPeriodicSync()
                Toast.makeText(requireContext(), R.string.remote_sync_stopped_toast, Toast.LENGTH_SHORT).show()
            }
            updateSyncStatus()
        }

        // Manual sync button (immediate one-time sync for debug)
        binding.btnManualSync.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            if (url.isEmpty()) {
                binding.tvUrlError.text = getString(R.string.remote_sync_url_empty_error)
                binding.tvUrlError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }
            prefs.setRemoteServerUrl(url.trimEnd('/').removeSuffix("/#").removeSuffix("#").trimEnd('/'))
            val request = androidx.work.OneTimeWorkRequestBuilder<net.activitywatch.android.watcher.RemoteSyncWorker>().build()
            androidx.work.WorkManager.getInstance(requireContext()).enqueue(request)
            Toast.makeText(requireContext(), getString(R.string.remote_sync_manual_toast), Toast.LENGTH_SHORT).show()
        }

        // Stop button
        binding.btnStop.setOnClickListener {
            prefs.setRemoteSyncEnabled(false)
            binding.switchRemoteSync.isChecked = false
            cancelPeriodicSync()
            Toast.makeText(requireContext(), R.string.remote_sync_stopped_toast, Toast.LENGTH_SHORT).show()
            updateSyncStatus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateSyncStatus() {
        val enabled = prefs.isRemoteSyncEnabled()
        val url = prefs.getRemoteServerUrl()
        val lastSync = prefs.getLastSyncTimestamp()

        val statusText = when {
            !enabled -> getString(R.string.remote_sync_status_disabled)
            url.isEmpty() -> getString(R.string.remote_sync_url_empty_error)
            lastSync == 0L -> getString(R.string.remote_sync_status_enabled)
            else -> {
                val date = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(lastSync))
                getString(R.string.remote_sync_status_enabled) + "  (" + getString(R.string.remote_sync_last_sync_label) + ": " + date + ")"
            }
        }
        binding.tvSyncStatus.text = statusText
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<RemoteSyncWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(0, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            RemoteSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    private fun cancelPeriodicSync() {
        WorkManager.getInstance(requireContext())
            .cancelUniqueWork(RemoteSyncWorker.WORK_NAME)
    }

    companion object {
        fun newInstance() = RemoteSyncSettingsFragment()
    }
}
