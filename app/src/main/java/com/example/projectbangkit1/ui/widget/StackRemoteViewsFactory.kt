package com.example.projectbangkit1.ui.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.example.projectbangkit1.R
import com.example.projectbangkit1.data.offline.StoryDatabase

/**
 * Created by dicoding on 1/9/2017.
 */

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val mName = ArrayList<String>()
    private val mDesc = ArrayList<String>()
    private val storyDatabase by lazy { StoryDatabase.getDatabase(mContext).storyDao() }


    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        storyDatabase.getStoryWidget().map { list ->
            mName.add(list.name.toString())
            mDesc.add(list.description.toString())
        }
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = mName.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setTextViewText(R.id.name, mName[position])
        rv.setTextViewText(R.id.desc, mDesc[position])

        val extras = bundleOf(
            StoryWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false

}