package com.jigi.mealjigsaw.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jigi.mealjigsaw.databinding.ActivityDifficultBinding

class DifficultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDifficultBinding
    private val btnEasy by lazy { binding.btnLvlEasy }
    private val btnMiddle by lazy { binding.btnLvlMiddle}
    private val btnDifficult by lazy { binding.btnLvlDifficult }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDifficultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnEasy.setOnClickListener {
            val iPuz = Intent(this, MainActivity::class.java)

            iPuz.putExtra("piecesNumber", 12)
            iPuz.putExtra("rows", 4)
            iPuz.putExtra("cols", 3)

            startActivity(iPuz)
            finish()
        }

        btnMiddle.setOnClickListener {
            val iPuz = Intent(this, MainActivity::class.java)

            iPuz.putExtra("piecesNumber",16)
            iPuz.putExtra("rows", 6)
            iPuz.putExtra("cols", 4)

            startActivity(iPuz)
            finish()
        }

        btnDifficult.setOnClickListener {
            val iPuz = Intent(this, MainActivity::class.java)
            iPuz.putExtra("piecesNumber", 32)
            iPuz.putExtra("rows", 8)
            iPuz.putExtra("cols", 6)

            startActivity(iPuz)
            finish()
        }
    }
}