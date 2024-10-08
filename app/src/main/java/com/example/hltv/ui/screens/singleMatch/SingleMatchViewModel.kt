package com.example.hltv.ui.screens.singleMatch

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.hltv.data.convertTimestampToWeekDateClock
import com.example.hltv.data.remote.Event
import com.example.hltv.data.remote.Game
import com.example.hltv.data.remote.Media
import com.example.hltv.data.remote.Prediction
import com.example.hltv.data.remote.getEvent
import com.example.hltv.data.remote.getGamesFromEvent
import com.example.hltv.data.remote.getMapImageFromMapID
import com.example.hltv.data.remote.getPredictionFromFirestore
import com.example.hltv.data.remote.getTeamImage
import com.example.hltv.data.remote.getTeamMedia
import com.example.hltv.data.remote.getTournamentLogo
import com.example.hltv.data.remote.sendPredictionToFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SingleMatchViewModel : ViewModel() {
    var prediction: MutableState<Prediction> = mutableStateOf(Prediction(0, 0))
    val games = mutableListOf<Game>()
    val mapImages = mutableStateListOf<Bitmap?>(null)
    var event = mutableStateOf<Event?>(null)
    var liveEvent = mutableStateOf<Event?>(null)
    var upcomingEvent = mutableStateOf<Event?>(null)
    var finishedEvent = mutableStateOf<Event?>(null)
    var awayTeamIcon = mutableStateOf<Bitmap?>(null)
    var homeTeamIcon = mutableStateOf<Bitmap?>(null)
    var tournamentIcon = mutableStateOf<Bitmap?>(null)
    var description = ""
    val homeTeamColor = mutableStateOf(Color.White)
    val awayTeamColor = mutableStateOf(Color.White)
    private var dataLoaded = false
    private var gamesLoaded = false


    private var _tournamentMedia = MutableStateFlow(ArrayList<Media>())
    var tournamentMedia: MutableStateFlow<ArrayList<Media>> = _tournamentMedia

    private fun getPrediction(matchID: String?) {
        val niceMatchID = matchID!!.toInt()
        CoroutineScope(Dispatchers.IO).launch {
            val tempPrediction =
                getPredictionFromFirestore(niceMatchID)
            if (tempPrediction == null) {
                prediction.value = Prediction(0, 0)
                return@launch
            } else {
                prediction.value = tempPrediction
            }
            calculateVotePercentage(prediction.value)
        }
    }

    fun updatePrediction(vote: Int, matchID: String?) {
        val niceMatchID = matchID!!.toInt()
        CoroutineScope(Dispatchers.IO).launch {
            when (vote) {
                1 -> {
                    prediction.value = Prediction(
                        prediction.value.homeTeamVoteCount + 1,
                        prediction.value.awayTeamVoteCount
                    )
                }
                2 -> {
                    prediction.value = Prediction(
                        prediction.value.homeTeamVoteCount,
                        prediction.value.awayTeamVoteCount + 1
                    )
                }
                else -> {
                    return@launch
                }
            }
            calculateVotePercentage(prediction.value)
            sendPredictionToFirestore(prediction.value, niceMatchID)
        }
    }

    private fun calculateVotePercentage(prediction: Prediction) {
        val totalVotes = prediction.homeTeamVoteCount + prediction.awayTeamVoteCount
        if (totalVotes == 0) {
            Log.d("SingleMatchViewModel", "totalVotes = 0")
            return
        }
        prediction.homeTeamVotePercentage =
            prediction.homeTeamVoteCount * 100 / totalVotes
        prediction.awayTeamVotePercentage =
            prediction.awayTeamVoteCount * 100 / totalVotes
    }

    fun loadData(matchID: String?) {
        if (dataLoaded) return
        dataLoaded = true
        val niceMatchID = matchID!!.toInt()
        viewModelScope.launch(Dispatchers.IO) {
            event.value = getEvent(niceMatchID).event!!
            homeTeamIcon.value = getTeamImage(event.value!!.homeTeam.id)
            awayTeamIcon.value = getTeamImage(event.value!!.awayTeam.id)


            if (homeTeamIcon.value != null){
                val homeTeamPalette = Palette.from(homeTeamIcon.value!!).generate()
                if (homeTeamPalette.vibrantSwatch?.rgb != null) {
                    homeTeamColor.value = Color(homeTeamPalette.vibrantSwatch?.rgb!!)
                } else homeTeamColor.value = Color.Blue
            } else homeTeamColor.value = Color.Blue

            if (awayTeamIcon.value != null){
                val awayTeamPalette = Palette.from(awayTeamIcon.value!!).generate()
                if (awayTeamIcon.value != null && awayTeamPalette.vibrantSwatch?.rgb != null) {
                    awayTeamColor.value = Color(awayTeamPalette.vibrantSwatch?.rgb!!)
                } else awayTeamColor.value = Color.Red
            } else awayTeamColor.value = Color.Red



            tournamentIcon.value = getTournamentLogo(event.value!!.tournament.uniqueTournament?.id)
            Log.i("tournamentIcon", "tournamentIcon added ${tournamentIcon.value}")
            getPrediction(matchID)
            when (event.value!!.status?.type) {
                "finished" -> { // Match with description "ended" has finished
                    finishedEvent.value = event.value
                }
                "inprogress" -> { // Match is not started
                    liveEvent.value = event.value
                }
                else -> { // Match must be upcoming
                    upcomingEvent.value = event.value
                    description =
                        "${event.value!!.homeTeam.name} will be playing against ${event.value!!.awayTeam.name}" +
                                " at ${convertTimestampToWeekDateClock(event.value!!.startTimestamp)} in the ${event.value!!.tournament.name} tournament." +
                                " They will be playing in a best of ${event.value!!.bestOf} map format."
                }
            }
            _tournamentMedia.value =
                getMedia(event.value!!.homeTeam.id, event.value!!.awayTeam.id)
        }
    }

    fun loadGames(matchID: String?) {
        if (gamesLoaded) return
        gamesLoaded = true
        val niceMatchID = matchID!!.toInt()
        viewModelScope.launch(Dispatchers.IO) {
            games.addAll(getGamesFromEvent(niceMatchID).games)
            mapImages.clear()
            games.forEach { game ->
                if (game.map?.id != null) {
                    val mapImage = getMapImageFromMapID(game.map?.id!!)
                    if (mapImage != null) {
                        mapImages.add(mapImage)
                    }
                } else Log.d(
                    "SingleMatchViewModel",
                    "Map ID is null for game with ID ${game.id}"
                )
            }
            getPrediction(niceMatchID.toString())
        }
    }

    private suspend fun getMedia(homeTeamID: Int?, awayTeamID: Int?): ArrayList<Media> {
        val allMedia = getTeamMedia(homeTeamID).media
        allMedia.addAll(getTeamMedia(awayTeamID).media)
        return allMedia
    }
}