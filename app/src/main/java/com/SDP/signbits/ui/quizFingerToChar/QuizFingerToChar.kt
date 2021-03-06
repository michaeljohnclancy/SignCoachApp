package com.SDP.signbits.ui.quizFingerToChar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.SDP.signbits.MainActivity
import com.SDP.signbits.R
import com.SDP.signbits.RPiHandler
import com.trycatch.mysnackbar.Prompt
import com.trycatch.mysnackbar.TSnackbar


class QuizFingerToChar : Fragment() {

    companion object {
        fun newInstance() =
            QuizFingerToChar()
    }

    private lateinit var viewModel: QuizFingerToCharViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(QuizFingerToCharViewModel::class.java)
        val root = inflater.inflate(R.layout.quiz_finger_to_char_fragment, container, false)

        val pref = requireContext().getSharedPreferences("LearningProgress",0)
        val edit = pref.edit()
        //Back button
        val button : ImageButton = root.findViewById(R.id.imageButton3)
        button.setOnClickListener{
            val fragmentManager : FragmentManager = requireFragmentManager()
            fragmentManager.beginTransaction().apply {
                remove(this@QuizFingerToChar)
            }.commit()
            fragmentManager.popBackStack()
        }


        //start button: start challenge
        val buttonStart : ImageView = root.findViewById(R.id.buttonStart)
        var symbol : CharSequence = ""
        var isconcl = true
        buttonStart.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    buttonStart.setImageResource(R.mipmap.button_foreground_press)
                }
                MotionEvent.ACTION_MOVE -> {
                    buttonStart.setImageResource(R.mipmap.button_foreground_press)
                }
                MotionEvent.ACTION_UP -> {
                    buttonStart.setImageResource(R.mipmap.button_foreground)

                    if (isconcl){
                        symbol = randomString()
                        Log.d("Learning Random String", symbol.toString())
                        val result = RPiHandler.getInstance(requireActivity()).postFingerSpellRequest(symbol)
                        if (result) {
                            isconcl = false
                            snack(Prompt.SUCCESS, "Look at the SignBits!")
                        }
                    } else {
                        snack(Prompt.ERROR, "You have already started!!")
                    }
                }
            }
            return@OnTouchListener true
        })






        //pop up hint: robot perform again
        val hintbutton : Button =root.findViewById(R.id.buttonHint)
        hintbutton.setOnClickListener(){
            if (symbol == "") snack(Prompt.ERROR, "You have not started!")
            else {
                if (RPiHandler.getInstance(requireActivity()).postFingerSpellRequest(symbol))
                    snack(Prompt.WARNING, "Look at the Robot Again!")
            }
        }

        //submit and check
        val submitButton : Button = root.findViewById(R.id.buttonSubmit)
        submitButton.setOnClickListener{
            if (symbol == "") snack(Prompt.ERROR, "You have not started!")
            else {
                edit.putInt("F2CNumber", pref.getInt("F2CNumber",0)+1).apply()
                val text : TextView = root.findViewById(R.id.editTextF2C)
                if (text.editableText.contains(symbol) && text.editableText.length == symbol
                        .length){
                    snack(Prompt.SUCCESS, "Correct!\n" + "Moved to the Next Challenge and click "
                            + "on the START " +"button now")
                    text.editableText.clear()
                    symbol = ""
                    isconcl = true
                    edit.putInt("F2CCorrect", pref.getInt("F2CCorrect", 0)+1).apply()
                    AlertDialog.Builder(requireActivity())
                        .setMessage("")
                        .setTitle("Correct!")
                        .setMessage("Move the next quiz?")
                        .setPositiveButton("Yes") { dialog, which ->
                            symbol = ""
                            snack(Prompt.SUCCESS, "Moved to the Next Challenge!")
                            isconcl = true
                        }
                        .setNeutralButton("No", null)
                        .create()
                        .show()
                } else {
                    snack(Prompt.ERROR, "Wrong! The Correct one is $symbol")
                    text.editableText.clear()
                    AlertDialog.Builder(requireActivity())
                        .setMessage("")
                        .setTitle("Wrong!")
                        .setMessage("Skip this challenge?")
                        .setPositiveButton("Yes") { dialog, which ->
                            symbol = ""
                            snack(Prompt.SUCCESS, "Moved to the Next Challenge!")
                            isconcl = true
                        }
                        .setNeutralButton("No", null)
                        .create()
                        .show()
                }
            }
        }

        val nextButton : Button = root.findViewById(R.id.buttonNext)
        nextButton.setOnClickListener{
            symbol = ""
            snack(Prompt.SUCCESS, "Moved to the Next Challenge!")
            isconcl = true
//            buttonStart.setTextColor(Color.BLACK)
        }

        return root
    }


    private fun randomString() : CharSequence {
        val generator = java.util.Random()
        val stringBuilder : StringBuilder = StringBuilder()
        val rlength = generator.nextInt(5)
        var char : Char
        for (i in 0..rlength){
            char = MainActivity.alphabet.random()
            stringBuilder.append(char)

        }
        return stringBuilder.toString()
    }

    private fun snack(prompt: Prompt, text: CharSequence){
        val duration = TSnackbar.LENGTH_SHORT
        TSnackbar.make(requireView(), text, duration).setPromptThemBackground(prompt).show();
    }

    private fun correct(){
        val pref : SharedPreferences = requireContext().getSharedPreferences("LearningProgress",0)

    }


    ;


}