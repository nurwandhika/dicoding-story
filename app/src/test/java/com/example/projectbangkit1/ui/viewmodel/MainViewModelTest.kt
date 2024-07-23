package com.example.projectbangkit1.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.projectbangkit1.DataDummy
import com.example.projectbangkit1.MainDispatcherRule
import com.example.projectbangkit1.data.StoryRepository
import com.example.projectbangkit1.data.response.DetailResponse
import com.example.projectbangkit1.data.response.ListStoryItem
import com.example.projectbangkit1.getOrAwaitValue
import com.example.projectbangkit1.ui.adapter.ListItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import com.example.projectbangkit1.data.Result
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ListStoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mainViewModel: MainViewModel
    private val dummyDetail = DataDummy.generateDummyDetail()

    @Before
    fun setUp() {
        mainViewModel = MainViewModel(storyRepository)
    }

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
    private val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
    private val id = "eyJhbGciOiJIUzI1NiInR5cCI6IkpJ9"

    @Test
    fun dataNotNull() = runTest {
        val dummyStory = DataDummy.generateDummyResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStory)
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        `when`(storyRepository.getStory(token)).thenReturn(expectedStory)
        val viewModel = MainViewModel(storyRepository)
        val actualStory: PagingData<ListStoryItem> = viewModel.getStory(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListItemAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size)
        assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun detailNotNull() = runTest {
        val expectedNews = MutableLiveData<Result<DetailResponse>>()
        expectedNews.value = Result.Success(dummyDetail)
        `when`(storyRepository.getDetail(token, id)).thenReturn(expectedNews)

        val actualNews = mainViewModel.getDetail(token, id).getOrAwaitValue()
        verify(storyRepository).getDetail(token, id)
        assertNotNull(actualNews)
        assertTrue(actualNews is Result.Success)
        assertEquals(dummyDetail.story, (actualNews as Result.Success).data.story)
    }

    @Test
    fun detailNull() {
        val headlineNews = MutableLiveData<Result<DetailResponse>>()
        headlineNews.value = Result.Error("Error")
        `when`(storyRepository.getDetail(token, id)).thenReturn(headlineNews)

        val actualNews = mainViewModel.getDetail(token, id).getOrAwaitValue()
        verify(storyRepository).getDetail(token, id)
        assertNotNull(actualNews)
        assertTrue(actualNews is Result.Error)
    }

    @Test
    fun dataNull() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        `when`(storyRepository.getStory(token)).thenReturn(expectedStory)

        val viewModel = MainViewModel(storyRepository)
        val actualStory: PagingData<ListStoryItem> = viewModel.getStory(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListItemAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertEquals(0, differ.snapshot().size)
    }
}

class StoryPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}