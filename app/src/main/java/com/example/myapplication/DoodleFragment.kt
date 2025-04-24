package com.example.myapplication

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.databinding.FragmentDoodleBinding
import com.google.android.material.tabs.TabLayout

class DoodleFragment : Fragment() {

    private lateinit var binding: FragmentDoodleBinding
    private val colors = arrayOf(
        Color.BLACK,
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.MAGENTA
    )
    private var currentColorIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoodleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClear.setOnClickListener {
            binding.drawingView.clear()
        }

        binding.btnUndo.setOnClickListener {
            binding.drawingView.undo()
        }

        binding.btnColor.setOnClickListener {
            currentColorIndex = (currentColorIndex + 1) % colors.size
            binding.drawingView.setColor(colors[currentColorIndex])
        }
        
        binding.btnUpload.setOnClickListener {
            uploadDrawingToChat()
        }

        binding.seekBarWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.drawingView.setStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun uploadDrawingToChat() {
        // Save the current drawing to a file
        val drawingUri = binding.drawingView.saveToFile()
        
        if (drawingUri != null) {
            // Get references to the ViewPager and HomeFragment
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            
            // Set shared data to pass to HomeFragment
            (requireActivity() as MainActivity).setPendingDrawingUri(drawingUri)
            
            // Switch to the first tab (HomeFragment)
            viewPager.setCurrentItem(0, false)
            
            // Clear the drawing
            binding.drawingView.clear()
            
            Toast.makeText(requireContext(), "Drawing uploaded to chat", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Nothing to upload. Draw something first!", Toast.LENGTH_SHORT).show()
        }
    }
} 