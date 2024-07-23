package com.example.projectbangkit1

import com.example.projectbangkit1.data.response.DetailResponse
import com.example.projectbangkit1.data.response.ListStoryItem
import com.example.projectbangkit1.data.response.Story


object DataDummy {

    fun generateDummyResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..50) {
            val story = ListStoryItem(
                "https://www.google.com/url?sa=i&url=https%3A%2F%2Fhelp.dicoding.com%2Fakun%2Fcara-update-data-profil-akun-dicoding%2F&psig=AOvVaw0qlVl_6LB1_CHF7l84NGYM&ust=1683402108161000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCKDx-tb33v4CFQAAAAAdAAAAABAj",
                "created at $i",
                "user $i",
                "description $i",
                i.toDouble(),
                "$i",
                i.toDouble()
            )
            items.add(story)
        }
        return items
    }

    fun generateDummyDetail(): DetailResponse {
        return DetailResponse(
            false, "sucesfull", Story(
                "https://www.google.com/url?sa=i&url=https%3A%2F%2Fhelp.dicoding.com%2Fakun%2Fcara-update-data-profil-akun-dicoding%2F&psig=AOvVaw0qlVl_6LB1_CHF7l84NGYM&ust=1683402108161000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCKDx-tb33v4CFQAAAAAdAAAAABAj",
                "created at ",
                "user ",
                "description",
                60.5656,
                "548676",
                -43435
            )
        )
    }
}