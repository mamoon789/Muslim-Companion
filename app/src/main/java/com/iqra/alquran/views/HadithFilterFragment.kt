package com.iqra.alquran.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Book
import com.iqra.alquran.viewmodels.MainViewModel


class HadithFilterFragment : BottomSheetDialogFragment(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener
{
    private lateinit var mainActivity: MainActivity
    private lateinit var viewModel: MainViewModel
    private lateinit var rgStatus: RadioGroup
    private lateinit var rgBooks: RadioGroup
    private lateinit var btDone: Button
    private lateinit var btReset: Button
    private lateinit var statusPair: Pair<Int, String>
    private lateinit var bookPair: Pair<Int, String>
    private var books = listOf<Book.Books>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mainActivity = activity as MainActivity
        viewModel = ViewModelProvider(mainActivity).get(MainViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_hadith_filter, container, false)
        rgStatus = view.findViewById(R.id.rgStatus)
        rgBooks = view.findViewById(R.id.rgBooks)
        btDone = view.findViewById(R.id.btDone)
        btReset = view.findViewById(R.id.btReset)

        rgStatus.setOnCheckedChangeListener(this)
        rgBooks.setOnCheckedChangeListener(this)
        btDone.setOnClickListener(this)
        btReset.setOnClickListener(this)

        viewModel.books.observe(viewLifecycleOwner) {
            if (it.data != null)
            {
                books = it.data.books
                books.forEach { book ->
                    val rbBooks =
                        LayoutInflater.from(context)
                            .inflate(R.layout.row_hadith_book_filter, rgBooks, false) as RadioButton
                    rbBooks.id = book.id
                    rbBooks.text = book.bookName
                    rgBooks.addView(rbBooks)
                }
            }
        }

        viewModel.statusPair.observe(viewLifecycleOwner) {
            statusPair = it
            rgStatus.check(it.first)
        }

        viewModel.bookPair.observe(viewLifecycleOwner) {
            bookPair = it
            rgBooks.check(it.first)
        }

        return view
    }

    override fun onCheckedChanged(v: RadioGroup, id: Int)
    {
        when (id)
        {
            R.id.rbStatusAll ->
            {
                statusPair = statusPair.copy(id, "")
            }

            R.id.rbStatusSahih,
            R.id.rbStatusHasan,
            R.id.rbStatusDaeef ->
            {
                val status = rgStatus.findViewById<RadioButton>(id).text.toString()
                statusPair = statusPair.copy(id, status)
            }

            R.id.rbBooksAll ->
            {
                bookPair = bookPair.copy(id, "")
            }

            else ->
            {
                val bookSlug = books.find { it.id == id }!!.bookSlug
                bookPair = bookPair.copy(id, bookSlug)
            }
        }
    }

    override fun onClick(v: View)
    {
        when (v.id)
        {
            R.id.btDone ->
            {
                viewModel.updateHadithSearchFilter(statusPair, bookPair)
                dismiss()
            }

            R.id.btReset ->
            {
                viewModel.updateHadithSearchFilter(
                    statusPair.copy(R.id.rbStatusAll, ""),
                    bookPair.copy(R.id.rbBooksAll, "")
                )
            }
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = HadithFilterFragment()
    }

}