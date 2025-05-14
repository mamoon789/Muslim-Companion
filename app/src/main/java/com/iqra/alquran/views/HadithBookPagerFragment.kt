package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iqra.alquran.R
import com.iqra.alquran.network.models.Book
import com.iqra.alquran.network.models.Chapter
import com.iqra.alquran.network.wrapper.Resource
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HadithBookPagerFragment : Fragment()
{
    private lateinit var mainActivity: MainActivity
    private lateinit var viewModel: MainViewModel
    private lateinit var rvBooks: RecyclerView
    private var books = listOf<Book.Books>()
    private var chapters = listOf<Chapter.Chapters>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        viewModel = ViewModelProvider(mainActivity).get(MainViewModel::class.java)
        viewModel.getBooks()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_hadith_book_pager, container, false)
        rvBooks = view.findViewById(R.id.rvBooks)
        rvBooks.adapter = Adapter()
        rvBooks.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        viewModel.books.observe(viewLifecycleOwner) {
            mainActivity.hideDialog()
            if (it.data != null)
            {
                books = it.data.books
                rvBooks.adapter?.notifyDataSetChanged()
            } else if (it.message != null)
            {
                mainActivity.showSnackBar(it.message, it.message == Constants.MSG_CONNECT_INTERNET)
            } else
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }
        }
        viewModel.chapters.observe(viewLifecycleOwner) {
            mainActivity.hideDialog()
            if (it.data != null)
            {
                chapters = it.data.chapters
                rvBooks.adapter?.notifyDataSetChanged()
            } else if (it.message != null)
            {
                mainActivity.showSnackBar(
                    it.message,
                    it.message == Constants.MSG_CONNECT_INTERNET
                )
            } else
            {
                mainActivity.showCustomDialog(R.layout.dialog_progress)
            }
        }
        return view
    }

    override fun onDestroy()
    {
        super.onDestroy()
        viewModel.books.postValue(Resource.Success(Book()))
        viewModel.chapters.postValue(Resource.Success(Chapter()))
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_hadith_book, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.apply {
                setIsRecyclable(false)

                books[position].let { book ->
                    bookSlug = book.bookSlug
                    tvNo.text = "${book.id}"
                    tvName.text = book.bookName
                    tvCount.text = "${book.chapters_count} Chapters, ${book.hadiths_count} Hadiths"

                    if (chapters.isNotEmpty() && book.bookSlug == chapters[0].bookSlug)
                    {
                        CoroutineScope(Dispatchers.Main).launch {
                            async {
                                printChapter(chapters)
                            }.await()
                            ibChapter.setImageDrawable(
                                ContextCompat.getDrawable(
                                    view.context,
                                    R.drawable.arrow_up
                                )
                            )
                            llChapter.visibility = View.VISIBLE
                            vLine.visibility = View.GONE
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int
        {
            return books.size
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener
        {
            lateinit var bookSlug: String
            val tvNo: TextView = view.findViewById(R.id.tvNo)
            val tvName: TextView = view.findViewById(R.id.tvName)
            val tvCount: TextView = view.findViewById(R.id.tvCount)
            val ibChapter: ImageButton = view.findViewById(R.id.ibChapter)
            val llChapter: LinearLayout = view.findViewById(R.id.llChapter)
            val vLine: View = view.findViewById(R.id.view)

            init
            {
                view.setOnClickListener(this)
                ibChapter.setOnClickListener(this)
            }

            @SuppressLint("SetTextI18n")
            override fun onClick(v: View)
            {
                when (v.id)
                {

                    R.id.ibChapter,
                    R.id.parent ->
                    {
                        if (llChapter.visibility == View.VISIBLE)
                        {
                            llChapter.visibility = View.GONE
                            vLine.visibility = View.VISIBLE
                            ibChapter.setImageDrawable(
                                ContextCompat.getDrawable(
                                    v.context,
                                    R.drawable.arrow_down
                                )
                            )
                        } else
                        {
                            viewModel.getChapters(bookSlug)
                        }
                    }

                    else ->
                    {
                        val bundle = v.tag as Bundle
                        val bookSlug = bundle.getString("bookSlug", "")
                        val chapter = bundle.getString("chapter", "")
                        mainActivity.supportFragmentManager.beginTransaction().apply {
                            replace(
                                R.id.container,
                                HadithFragment.newInstance(bookSlug, chapter)
                            );
                            addToBackStack(null);
                            commit();
                        }
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            fun printChapter(chapters: List<Chapter.Chapters>)
            {
                chapters.forEach { chapter ->
                    val view = LayoutInflater.from(llChapter.context)
                        .inflate(R.layout.row_hadith_chapter, llChapter, false)

                    val tvNo: TextView = view.findViewById(R.id.tvNo)
                    val tvName: TextView = view.findViewById(R.id.tvName)

                    tvNo.text = chapter.chapterNumber
                    tvName.text = chapter.chapterEnglish

                    llChapter.addView(view)

                    view.tag = Bundle().apply {
                        putString("bookSlug", chapter.bookSlug)
                        putString("chapter", chapter.chapterNumber)
                    }
                    view.setOnClickListener(this@ViewHolder)
                }
            }
        }
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = HadithBookPagerFragment()
    }
}