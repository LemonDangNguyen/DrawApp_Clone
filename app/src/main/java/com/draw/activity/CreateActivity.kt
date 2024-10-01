package com.draw.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.draw.R
import com.draw.databinding.ActivityCreateBinding
import com.draw.ultis.Common
import com.shawnlin.numberpicker.NumberPicker
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Suppress("DEPRECATION")
class CreateActivity : BaseActivity() {
    private val binding by lazy { ActivityCreateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCreateAnimation.setOnClickListener {
            if (binding.edtName.text.toString().isBlank()) {
                Toast.makeText(this, getText(R.string.null_name_content), Toast.LENGTH_SHORT).show()
            } else {
                startActivity(
                    Intent(
                        this,
                        DrawActivity::class.java
                    ).putExtra(Common.KEY_PROJECT_NAME, binding.edtName.text.toString())
                )
                finish()
            }
        }
        val data = arrayOf("5", "10", "20", "30", "40", "50")

        binding.nbSpeed.minValue = 0
        binding.nbSpeed.maxValue = data.size - 1
        binding.nbSpeed.displayedValues = data
        binding.nbSpeed.value = 1

        binding.nbSpeed.setOnValueChangedListener { _, _, newVal ->
            Common.time_frame = 1000 / data[newVal].toLong()
        }

        binding.btnRight.setOnClickListener {
            changeValueByOne(binding.nbSpeed, true)
        }
        binding.btnLeft.setOnClickListener {
            changeValueByOne(binding.nbSpeed, false)
        }

    }
    private fun changeValueByOne(higherPicker: NumberPicker, increment: Boolean) {
        val method: Method
        try {
            method = higherPicker.javaClass.getDeclaredMethod(
                "changeValueByOne",
                Boolean::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(higherPicker, increment)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}