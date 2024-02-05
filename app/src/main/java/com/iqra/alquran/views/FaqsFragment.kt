package com.iqra.alquran.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.iqra.alquran.R
import com.iqra.alquran.utils.Constants

class FaqsFragment : Fragment()
{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_faqs, container, false)
        val rvFaqs = view.findViewById<RecyclerView>(R.id.rvFaqs)
        rvFaqs.adapter = Adapter(
            resources.getStringArray(R.array.faqs_keys),
            resources.getStringArray(R.array.faqs_values)
        )

        return view
    }

    class Adapter(val keys: Array<String>, val values: Array<String>) :
        RecyclerView.Adapter<Adapter.ViewHolder>()
    {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener
        {
            val tvQuestion = view.findViewById<TextView>(R.id.tvQuestion)
            val tvAnswer = view.findViewById<TextView>(R.id.tvAnswer)

            init
            {
                tvQuestion.setOnClickListener(this)
            }

            override fun onClick(p0: View?)
            {
                if (tvAnswer.isVisible)
                {
                    tvAnswer.visibility = GONE
                } else
                {
                    tvAnswer.visibility = VISIBLE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.faq_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.tvQuestion.text = keys[position]
            holder.tvAnswer.text = values[position]
        }

        override fun getItemCount(): Int
        {
            return keys.size
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = FaqsFragment()
    }
}