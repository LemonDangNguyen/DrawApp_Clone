@file:Suppress("DEPRECATION")

package com.draw.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.draw.GIFHelper.AnimatedGifEncoder
import com.draw.R
import com.draw.adapter.DrawViewAdapter
import com.draw.database.DataClient.listTypeAnimationGuide
import com.draw.databinding.ActivityDrawBinding
import com.draw.databinding.DialogExitBinding
import com.draw.databinding.DialogProgressBinding
import com.draw.model.AnimationGuide
import com.draw.model.DrawInfo
import com.draw.ultis.Common
import com.draw.ultis.Common.KEY_ANIM_GUIDE
import com.draw.ultis.Common.KEY_POSITION_ANIM_GUIDE
import com.draw.ultis.Common.getBitmapFromPathListHistory
import com.draw.ultis.Common.getBitmapWithoutWhite
import com.draw.ultis.ViewControl.actionAnimation
import com.draw.ultis.ViewControl.gone
import com.draw.ultis.ViewControl.invisible
import com.draw.ultis.ViewControl.visible
import com.draw.viewcustom.DrawView
import com.draw.viewcustom.StickerImportDialog
import com.draw.viewcustom.StickerManager
import com.draw.viewcustom.StickerMemeView
import com.draw.viewcustom.StickerPhotoDialog
import com.draw.viewcustom.StickerPhotoView
import com.draw.viewcustom.StickerTextDialog
import com.draw.viewcustom.StickerTextView
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


@SuppressLint("NotifyDataSetChanged")
class DrawActivity : BaseActivity() {

    private val binding by lazy { ActivityDrawBinding.inflate(layoutInflater) }
    private lateinit var adapter: DrawViewAdapter
    private lateinit var runable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var i = 0
    private var isGuide = false
    private lateinit var animationGuide: AnimationGuide
    private var timeDelay = 100L
    private lateinit var stickerManager: StickerManager
    private lateinit var dialog: Dialog
    private lateinit var bindingDialog: DialogProgressBinding
    private var mDefaultColor = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCreateAnimation.isClickable = false
        binding.btnCreateAnimation.isSelected = true
        binding.btnCreateAnimation.isClickable = true
        binding.btnPrevios.isEnabled = false
        isGuide = intent.getIntExtra(KEY_POSITION_ANIM_GUIDE, -1) != -1
        val stickerContainer = binding.stickerContainer // Lấy tham chiếu đến sticker container
        stickerManager = StickerManager(stickerContainer)

        if (isGuide) {
            val position = intent.getIntExtra(KEY_POSITION_ANIM_GUIDE, -1)
            val typeGuide = intent.getIntExtra(KEY_ANIM_GUIDE, -1)
            animationGuide = listTypeAnimationGuide()[typeGuide].listAnimationGuide[position]

            binding.btnPrevios.gone()
            binding.btnNext.gone()
            binding.btnPreview.setImageResource(R.drawable.ic_play)

            timeDelay = 1000L / animationGuide.animationSpeed
        } else {
            timeDelay = Common.time_frame
        }

        adapter = DrawViewAdapter(this, isGuide) { newPosition, oldPosition ->
            binding.rcvDraw.smoothScrollToPosition(newPosition + 1)
            binding.drawView.resetPos()
            adapter.listDraw[oldPosition].setInfo(
                DrawInfo(
                    getBitmapWithoutWhite(
                        binding.drawView.getHistoryPaint(),
                        binding.drawView.width,
                        binding.drawView.height
                    ),
                    binding.drawView.getHistoryPaint(),
                    binding.drawView.getHistoryUndo()
                )
            )
            if (isGuide) {
                binding.drawView.backgroundBitmap =
                    BitmapFactory.decodeResource(resources, animationGuide.listFrame[newPosition])
            } else {
                if (newPosition != 0) {
                    binding.drawView.backgroundBitmap = adapter.listDraw[newPosition - 1].bitmap
                } else {
                    binding.drawView.backgroundBitmap = null
                }
            }
            binding.drawView.setHistory(adapter.listDraw[newPosition])

            if (newPosition == 0) {
                binding.btnPrevios.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#C5C9CC"))
                binding.btnPrevios.isEnabled = false
            } else {
                binding.btnPrevios.imageTintList = null
                binding.btnPrevios.isEnabled = true
            }

            if (newPosition == adapter.listDraw.size - 1) {
                binding.btnNext.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#C5C9CC"))
                binding.btnNext.isEnabled = false
            } else {
                binding.btnNext.imageTintList = null
                binding.btnNext.isEnabled = true
            }
        }

        if (isGuide) {
            adapter.setAnimationGuide(animationGuide)
            binding.drawView.backgroundBitmap =
                BitmapFactory.decodeResource(resources, animationGuide.listFrame[0])
        }

        binding.rcvDraw.adapter = adapter

        runable = Runnable {
            if (i >= adapter.listDraw.size) {
                i = 0
            }

            binding.imgPreview.setImageBitmap(adapter.listDraw[i].bitmap)

            i++
            handler.postDelayed(runable, timeDelay)
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnUndo.setOnClickListener {
            val action = stickerManager.undo()
            action?.let {
                // Cập nhật giao diện hoặc trạng thái nếu cần thiết
                Toast.makeText(this, "Undo: ${it.sticker.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show()
            }
            binding.drawView.setUndo()
        }
        binding.btnRedo.setOnClickListener {
            val action = stickerManager.redo()
            action?.let {
                // Cập nhật giao diện hoặc trạng thái nếu cần thiết
                Toast.makeText(this, "Redo: ${it.sticker.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show()
            }
            binding.drawView.setRedo()
        }

        binding.btnFlip.setOnClickListener {
            binding.drawView.flip()
        }

        binding.drawView.setOnDrawChange(object : DrawView.OnDrawChange {
            override fun onDrawChange() {
                if (binding.drawView.getHistoryPaint().size > 0) {
                    binding.btnUndo.imageTintList = ColorStateList.valueOf(Color.parseColor("#292D32"))
                } else {
                    binding.btnUndo.imageTintList = null
                }

                if (binding.drawView.getHistoryUndo().size > 0) {
                    binding.btnRedo.imageTintList = ColorStateList.valueOf(Color.parseColor("#292D32"))
                } else {
                    binding.btnRedo.imageTintList = null
                }
            }

        })

        binding.btnPen.setOnClickListener {
            if(binding.lnPenwidth.visibility == View.VISIBLE && !binding.drawView.getEraserMode()){
                binding.lnPenwidth.gone()
            }else{
                binding.lnPenwidth.visible()
            }
            binding.drawView.setEraserMode(false)
            binding.btnEraser.backgroundTintList = null
            binding.btnPen.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#01C296"))
            binding.btnPen.imageTintList = null
        }

        binding.btnEraser.setOnClickListener {
            if(binding.lnPenwidth.visibility == View.VISIBLE && binding.drawView.getEraserMode()){
                binding.lnPenwidth.gone()
            }else{
                binding.lnPenwidth.visible()
            }
            binding.drawView.setEraserMode(true)
            binding.btnEraser.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#01C296"))
            binding.btnPen.backgroundTintList = null
            binding.btnPen.imageTintList = ColorStateList.valueOf(Color.parseColor("#01C296"))

        }

        binding.btnReset.setOnClickListener {
            binding.drawView.clearDraw()
            resetStickers()
        }

        binding.btnColor.setOnClickListener {

            binding.lnPenwidth.gone()
        }
        binding.btnCopy.setOnClickListener {
            adapter.listDraw[adapter.getSelectItem()].setInfo(
                DrawInfo(
                    getBitmapWithoutWhite(
                        binding.drawView.getHistoryPaint(),
                        binding.drawView.width,
                        binding.drawView.height
                    ),
                    binding.drawView.getHistoryPaint(),
                    binding.drawView.getHistoryUndo()
                )
            )
            adapter.makeCopy(binding.drawView.width, binding.drawView.height)
            binding.lnPenwidth.gone()
        }



        binding.btnInsertText.setOnClickListener {
            showStickerTextDialog(stickerTextView = StickerTextView(this), defaultX = 0f, defaultY = 0f) // Gọi phương thức hiển thị dialog để thêm sticker text
        }
        binding.btnInsertPicture.setOnClickListener {
            showStickerPhotoDialog(stickerPhotoView = StickerPhotoView(this), defaultX = 0f, defaultY = 0f) // Gọi phương thức hiển thị dialog để thêm sticker photo
        }
        binding.btnInsertSticker.setOnClickListener {
            showStickerImportDialog(stickerMemeView = StickerMemeView(this), defaultX = 0f, defaultY = 0f) // Gọi phương thức hiển thị dialog để thêm sticker meme
        }

        binding.btnOption.setOnClickListener {
            Toast.makeText(this, "Comming soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnColorr.setOnClickListener {

            val dialog = ColorPickerDialog
                .Builder(this)
                .setTitle("Select Color")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                    ColorEnvelopeListener { envelope, _ ->
                        binding.drawView.setPenColor(envelope.color)
                        binding.icColor.backgroundTintList = ColorStateList.valueOf(envelope.color)
                    }
                )
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .create()

            dialog.window?.setBackgroundDrawableResource(R.drawable.bg_radius_14dp)

            dialog.show()
        }

        binding.icColor.width = 40
        binding.icColor.height = 40

        binding.sbPenwith.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.drawView.setPenWidth(progress.toFloat())
                binding.icColor.width = progress + 5
                binding.icColor.height = progress + 5
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        binding.btnNext.setOnClickListener {
            adapter.setNextorPrevios(true)
        }
        binding.btnPrevios.setOnClickListener {
            adapter.setNextorPrevios(false)
        }

        binding.btnCreateAnimation.setOnClickListener {

            adapter.listDraw[adapter.getSelectItem()].setInfo(
                DrawInfo(
                    getBitmapWithoutWhite(
                        binding.drawView.getHistoryPaint(),
                        binding.drawView.width,
                        binding.drawView.height
                    ),
                    binding.drawView.getHistoryPaint(),
                    binding.drawView.getHistoryUndo()
                )
            )

            adapter.notifyDataSetChanged()

            var isDrawed = false

            for (draw in adapter.listDraw) {
                if (draw.listHistory.isNotEmpty()) {
                    isDrawed = true
                }
            }

            if (isDrawed) {
                showDialogProgress()
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        createAnimation(adapter.listDraw, timeDelay.toInt())
                    }
                }
            } else {
                Toast.makeText(this, R.string.not_draw, Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnPreview.setOnClickListener {
            adapter.listDraw[adapter.getSelectItem()].setInfo(
                DrawInfo(
                    getBitmapWithoutWhite(
                        binding.drawView.getHistoryPaint(),
                        binding.drawView.width,
                        binding.drawView.height
                    ),
                    binding.drawView.getHistoryPaint(),
                    binding.drawView.getHistoryUndo()
                )
            )
            adapter.notifyDataSetChanged()

            var isDrawed = false

            for (draw in adapter.listDraw) {
                if (draw.listHistory.isNotEmpty()) {
                    isDrawed = true
                }
            }
            if (isDrawed) {
                showPreview()
            } else {
                Toast.makeText(this, R.string.no_frame, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPause.setOnClickListener {
            stopPreview()
        }
    }
    private fun showStickerTextDialog(stickerTextView: StickerTextView, defaultX: Float, defaultY: Float) {
        // Tạo một dialog để nhập sticker văn bản
        val dialog = StickerTextDialog(stickerTextView, mDefaultColor)
        dialog.setOnStickerTextEntered { text ->
            stickerManager.addStickerText(text, defaultX, defaultY) // Thêm sticker text vào sticker manager
        }
        dialog.show(supportFragmentManager, "StickerTextDialog")
    }

    private fun showStickerPhotoDialog(stickerPhotoView: StickerPhotoView, defaultX: Float, defaultY: Float) {
        // Tạo một dialog để chọn sticker ảnh
        val dialog = StickerPhotoDialog(stickerPhotoView, this@DrawActivity)
        dialog.setOnStickerPhotoSelected { bitmap ->
            stickerManager.addStickerPhoto(bitmap, defaultX, defaultY) // Thêm sticker photo vào sticker manager
        }
        dialog.show(supportFragmentManager, "StickerPhotoDialog")
    }

    private fun showStickerImportDialog(stickerMemeView: StickerMemeView, defaultX: Float, defaultY: Float) {
        // Tạo một dialog để nhập sticker meme
        val dialog = StickerImportDialog(stickerMemeView)
        dialog.setOnMemeSelected { resId ->
            stickerManager.addStickerMeme(resId, defaultX, defaultY) // Thêm sticker meme vào sticker manager
        }
        dialog.show(supportFragmentManager, "StickerImportDialog")
    }



    private fun showDialogProgress() {
        bindingDialog = DialogProgressBinding.inflate(layoutInflater)
        dialog = Dialog(this)
        dialog.setContentView(bindingDialog.root)
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        bindingDialog.ltv.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER
        ) { PorterDuffColorFilter(Color.parseColor("#01C296"), PorterDuff.Mode.SRC_ATOP) }
        dialog.show()
    }


    private fun showPreview() {
        binding.root.actionAnimation()
        binding.lnHeader.invisible()
        binding.lnPenwidth.invisible()
        binding.btnPause.visible()
        binding.rcvDraw.invisible()
        binding.lnCreate.invisible()
        binding.imgPreview.visible()
        binding.drawView.invisible()
        binding.lnPen.invisible()
        binding.lnPenwidth.invisible()
        binding.drawView.createNewDraw()

        i = 0
        binding.imgPreview.setImageBitmap(adapter.listDraw[i].bitmap)

        i++
        handler.postDelayed(runable, 100)
    }

    private fun stopPreview() {
        binding.root.actionAnimation()
        binding.lnHeader.visible()
        binding.rcvDraw.visible()
        binding.lnCreate.visible()
        binding.lnPenwidth.visible()
        binding.imgPreview.invisible()
        binding.drawView.visible()
        binding.lnPen.visible()
        binding.btnPause.invisible()

        binding.drawView.setHistory(adapter.listDraw[adapter.getSelectItem()])
        handler.removeCallbacks(runable)
    }

    private fun createAnimation(listDrawInfo: MutableList<DrawInfo>, timeDelay: Int) {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "DrawAnimation"
        )
        if (!directory.exists()) {
            directory.mkdirs()
            if (!directory.mkdirs()) {
                dialog.dismiss()
                runOnUiThread {
                    Toast.makeText(this, getText(R.string.error), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
        val fileName = "animation_${System.currentTimeMillis()}.gif"
        val file = File(directory, fileName)

        try {
            val outStream = FileOutputStream(file)
            outStream.write(generateGIF(listDrawInfo, timeDelay))
            outStream.close()
            dialog.dismiss()
            var projectName = intent.getStringExtra(Common.KEY_PROJECT_NAME)?:""
            if(isGuide){
                projectName = getString(animationGuide.name)
            }
            startActivity(
                Intent(this, ShareAnimationActivity::class.java).putExtra(
                    "LINK",
                    file.path
                ).putExtra(
                    Common.KEY_PROJECT_NAME,
                    projectName
                ).putExtra(
                    Common.KEY_IS_GUIDE,
                    isGuide
                )
            )
        } catch (e: Exception) {
            dialog.dismiss()
            runOnUiThread {
                Toast.makeText(this, getText(R.string.error), Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }

    private fun generateGIF(listDraw: MutableList<DrawInfo>, timeDelay: Int): ByteArray {
        val bos = ByteArrayOutputStream()
        val encoder = AnimatedGifEncoder()
        encoder.setDelay(timeDelay)
        encoder.start(bos)

        for (draw in listDraw) {
            // Tạo bitmap cho frame
            val frameBitmap = Bitmap.createBitmap(binding.drawView.width, binding.drawView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(frameBitmap)

            // Vẽ nội dung từ DrawView lên canvas
            val drawBitmap = getBitmapFromPathListHistory(draw.listHistory, binding.drawView.width, binding.drawView.height)
            canvas.drawBitmap(drawBitmap, 0f, 0f, null)

            drawStickers(canvas)
            encoder.addFrame(frameBitmap)
        }
        encoder.finish()
        return bos.toByteArray()
    }
    private fun drawStickers(canvas: Canvas) {
        for ((sticker, position) in stickerManager.getStickers()) {
            val stickerBitmap = when (sticker) {
                is StickerTextView -> sticker.getStickerBitmap()
                is StickerPhotoView -> sticker.getStickerBitmap()
                is StickerMemeView -> sticker.getStickerBitmap()
                else -> null
            }

            stickerBitmap?.let {
                // Lấy vị trí từ tuple
                val (x, y) = position
                // Vẽ bitmap tại vị trí của sticker
                canvas.drawBitmap(it, x, y, null)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        val binding1= DialogExitBinding.inflate(layoutInflater)
        val dialog1 = Dialog(this)
        dialog1.setContentView(binding1.root)
        val window = dialog1.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog1.setCanceledOnTouchOutside(false)
        dialog1.setCancelable(false)

        binding1.btnExit.setOnClickListener {
            dialog1.dismiss()
            super.onBackPressed()
        }

        binding1.btnStay.setOnClickListener {
            dialog1.dismiss()
        }

        dialog1.show()
    }

    private fun resetStickers() {
        stickerManager.removeAllStickers() // Gọi hàm xóa tất cả sticker
    }

}