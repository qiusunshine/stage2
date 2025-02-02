package com.thallo.stage.componets.popup

import android.content.Intent

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thallo.stage.HolderActivity
import com.thallo.stage.MainActivity
import com.thallo.stage.R
import com.thallo.stage.componets.BookmarkDialog
import com.thallo.stage.componets.HomeLivedata
import com.thallo.stage.componets.MenuAddonsAdapater
import com.thallo.stage.componets.TabBottomSheetDialog.Companion.TAG
import com.thallo.stage.database.bookmark.BookmarkViewModel
import com.thallo.stage.databinding.PopupMenuBinding
import com.thallo.stage.session.DelegateLivedata
import com.thallo.stage.session.PrivacyFlow
import com.thallo.stage.session.SessionDelegate
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.StorageController.ClearFlags.ALL

class MenuPopup{
    val context: MainActivity
    private var bottomSheetDialog:BottomSheetDialog
    var binding: PopupMenuBinding
    private var bookmarkViewModel: BookmarkViewModel
    private  var sessionDelegate: SessionDelegate? =null
    var isHome:Boolean = true
    var isPrivacy:Boolean = false

    constructor(
        context: MainActivity,
    ) {
        this.context = context
        bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialog )
        bookmarkViewModel = ViewModelProvider(context).get<BookmarkViewModel>(BookmarkViewModel::class.java)
        binding = PopupMenuBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)
        var privacyRename = ViewModelProvider(context)[PrivacyFlow::class.java]

        DelegateLivedata.getInstance().observe(context){
            sessionDelegate=it
            binding.user = sessionDelegate

        }
        context.lifecycleScope.launch {
            privacyRename.data.collect(){
                    value: Boolean ->isPrivacy = value
                if (value)
                    binding.privacyButton.icon = context.getDrawable(R.drawable.emoji_sunglasses_fill)
                else
                    binding.privacyButton.icon = context.getDrawable(R.drawable.emoji_wink_fill)
            }
        }



        HomeLivedata.getInstance().observe(context){
            isHome=it
            if (it){
                binding.constraintLayout5.visibility=View.GONE
            }
            else {
                binding.constraintLayout5.visibility=View.VISIBLE
            }
        }
        binding.downloadButton.setOnClickListener {
            var intent=Intent(context, HolderActivity::class.java)
            intent.putExtra("Page","DOWNLOAD")
            context.startActivity(intent)
            bottomSheetDialog.dismiss()
        }
        binding.settingButton.setOnClickListener {
            var intent=Intent(context, HolderActivity::class.java)
            intent.putExtra("Page","SETTINGS")
            context.startActivity(intent)
            bottomSheetDialog.dismiss()

        }
        binding.privacyButton.setOnClickListener {
            if (isPrivacy)
                privacyRename.changeMode(false)
            else
                privacyRename.changeMode(true)
            bottomSheetDialog.dismiss()
        }
        binding.bookmarkButton.setOnClickListener {
            BookmarkPopup(context).show()
            bottomSheetDialog.dismiss()

        }
        binding.historyButton.setOnClickListener {
            HistoryPopup(context).show()
            bottomSheetDialog.dismiss()

        }
        binding.starButton.setOnClickListener {
            if (sessionDelegate!=null){
                BookmarkDialog(context, sessionDelegate!!.mTitle, sessionDelegate!!.u).show()
            }
        }

        binding.shareButton.setOnClickListener {
            ComposeMenuPopup().show(context.supportFragmentManager,TAG)
            bottomSheetDialog.dismiss()
        }

        binding.reloadBotton.setOnClickListener {
            if (!isHome)
                sessionDelegate?.session?.reload()
            bottomSheetDialog.dismiss()

        }
        binding.forwardButton.setOnClickListener {
            if (!isHome)
                sessionDelegate?.session?.goForward()
            bottomSheetDialog.dismiss()

        }
        binding.dataClearingButton.setOnClickListener {
            sessionDelegate?.let { it1 ->
                GeckoRuntime.getDefault(context).storageController.clearDataFromHost(
                    it1.secureHost,ALL)
                it1.session.reload()

            }
            bottomSheetDialog.dismiss()
        }
        binding.modeBotton.setOnClickListener {
            if (!isHome) {
                if (sessionDelegate?.session?.settings?.userAgentMode == GeckoSessionSettings.USER_AGENT_MODE_DESKTOP) {
                    sessionDelegate!!.session.settings.userAgentMode =
                        GeckoSessionSettings.USER_AGENT_MODE_MOBILE
                    sessionDelegate!!.session.reload()
                } else {
                    sessionDelegate!!.session.settings.userAgentMode =
                        GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                    sessionDelegate!!.session.reload()
                }
            }
            bottomSheetDialog.dismiss()

        }

        var adapter= MenuAddonsAdapater()
        binding.menuAddonsRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
        binding.menuAddonsRecyclerView.adapter=adapter
        GeckoRuntime.getDefault(context).webExtensionController.list().accept {

            if (it != null) {

                if (it.size!=0&&!isHome)
                    binding.constraintLayout2.visibility=View.VISIBLE
                else
                    binding.constraintLayout2.visibility=View.GONE
                var webExtensions=it
                for (e in it){
                    if (!e.metaData.enabled)
                        if (webExtensions != null) {
                            webExtensions=webExtensions.toMutableList().apply { remove(e) }
                        }
                }
                adapter.submitList(webExtensions)
            }
        }

    }
    fun show(){
        bottomSheetDialog.show()
    }
}