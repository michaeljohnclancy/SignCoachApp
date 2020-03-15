package com.SDP.signbits.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.SDP.signbits.MainActivity
import com.SDP.signbits.R
import com.SDP.signbits.RPiHandler
import com.SDP.signbits.TextProgressBar
import com.SDP.signbits.ui.learn.LearnFragment
import com.SDP.signbits.ui.quiz.QuizFragment
import com.trycatch.mysnackbar.Prompt
import com.trycatch.mysnackbar.TSnackbar
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)


        //text check button
        val spinner : Spinner = root.findViewById(R.id.spinner)

        val character = arrayOf("Choose One Character","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z")

        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_dropdown_item,
            character
        )

        spinner.adapter = adapter
        spinner.prompt = character[0]

        var choosen_character : String

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                // Display the selected item text on text view
                choosen_character = parent.getItemAtPosition(position).toString()
                if (choosen_character != character[0])
                    AlertDialog.Builder(requireActivity())
                        .setMessage("Do you want the robot to perform the character?")
                        .setTitle("We do support this!")
                        .setPositiveButton("Yes",
                            { dialog, which ->  onClick(dialog, which, choosen_character)})
                        .setNeutralButton("No", null)
                        .create()
                        .show()
            }


            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }


        val pref : SharedPreferences = requireContext().getSharedPreferences("LearningProgress",0)
        val progressBar : TextProgressBar = root.findViewById(R.id.progressBar)
        progressBar.max = MainActivity.alphabet.count()
        progressBar.progress = pref.getInt("Learning", -1)
//        progressBar.progress = 13
        val progressBar2 : TextProgressBar = root.findViewById(R.id.progressBar2)
        progressBar2.max = 100
        val correct = 100 * (pref.getInt("F2CCorrect",0) + pref.getInt("C2FCorrect", 0))
//        val correct = 1
        val total = pref.getInt("F2CNumber", 0) + pref.getInt("C2FNumber", 0)
//        val total = 2
        var acc : Int
        if (total != 0) {
            acc = correct / total
        } else {
            acc = 0
        }
        progressBar2.progress = acc

        return root
    }


    private fun robotFingerspell(charSequence : CharSequence) {
        RPiHandler.getInstance(requireActivity()).postFingerSpellRequest(charSequence)
    }


    private fun snack(prompt: Prompt, text: CharSequence){
        val duration = TSnackbar.LENGTH_SHORT
        TSnackbar.make(requireView(), text, duration).setPromptThemBackground(prompt).show();
    }


    fun onClick(diag: DialogInterface, which: Int, charSequence: CharSequence) {
        when (which){
            -1 -> {
                robotFingerspell(charSequence)
                snack(Prompt.SUCCESS, "Look at the Robot")
            }
        }
    }


}