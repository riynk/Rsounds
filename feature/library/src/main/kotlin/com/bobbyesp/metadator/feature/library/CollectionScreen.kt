/*
 * Copyright (C) 2026  Gabriel Fontán (BobbyESP)
 */
package com.bobbyesp.metadator.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bobbyesp.metadator.core.designsystem.component.ActionButtonGroup
import com.bobbyesp.metadator.core.designsystem.component.GroupAction
import com.bobbyesp.metadator.core.designsystem.component.LoadingScreen
import com.bobbyesp.metadator.core.designsystem.component.sidesOf
import com.bobbyesp.metadator.core.designsystem.theme.GroupShapes
import com.bobbyesp.metadator.core.designsystem.theme.Spacing
import com.bobbyesp.metadator.core.model.Track
import com.bobbyesp.metadator.core.model.TrackCollection
import com.bobbyesp.metadator.core.model.TrackId
import com.bobbyesp.metadator.core.ui.R as CoreUiR
import com.bobbyesp.metadator.core.ui.component.ArtworkImage
import com.bobbyesp.metadator.core.ui.component.TrackListItem

/** The songs of one album, artist or folder, with what to do with all of them at once. */
@Composable
internal fun CollectionScreen(
    title: String,
    collection: TrackCollection?,
    playingId: TrackId?,
    isPlaybackRunning: Boolean,
    showBack: Boolean,
    onBack: () -> Unit,
    onOpenTrack: (Track) -> Unit,
    onPlay: (tracks: List<Track>, index: Int, shuffle: Boolean) -> Unit,
    onEditAll: (List<Track>) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                subtitle =
                    collection?.let {
                        {
                            Text(
                                pluralStringResource(
                                    R.plurals.song_count,
                                    it.tracks.size,
                                    it.tracks.size,
                                )
                            )
                        }
                    },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                stringResource(CoreUiR.string.back),
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        if (collection == null) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }
        val tracks = collection.tracks
        LazyColumn(
            modifier = Modifier.fillMaxSize().sidesOf(padding),
            contentPadding =
                PaddingValues(
                    top = padding.calculateTopPadding(),
                    start = Spacing.screen,
                    end = Spacing.screen,
                    bottom = Spacing.floatingClearance,
                ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item(key = "header", contentType = "header") {
                CollectionHeader(
                    collection = collection,
                    onPlay = { onPlay(tracks, 0, false) },
                    onShuffle = { onPlay(tracks, 0, true) },
                    onEditAll = { onEditAll(tracks) },
                )
            }
            itemsIndexed(
                tracks,
                key = { _, it -> it.id.value },
                contentType = { _, _ -> "track" },
            ) { index, track ->
                val isPlaying = track.id == playingId
                TrackListItem(
                    track = track,
                    onClick = { onPlay(tracks, index, false) },
                    onLongClick = { onOpenTrack(track) },
                    onPlay = { onPlay(tracks, index, false) },
                    onEdit = { onOpenTrack(track) },
                    isPlaying = isPlaying,
                    isPlaybackRunning = isPlaying && isPlaybackRunning,
                    shapes = GroupShapes.listItemShapes(index, tracks.size),
                )
            }
        }
    }
}

@Composable
private fun CollectionHeader(
    collection: TrackCollection,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onEditAll: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        if (collection !is TrackCollection.Folder) {
            ArtworkImage(
                model = collection.artworkRef?.uri,
                contentDescription = null,
                modifier = Modifier.size(240.dp),
                shape =
                    if (collection is TrackCollection.Artist) CircleShape
                    else MaterialTheme.shapes.medium,
            )
        }
        Row(
            modifier = Modifier.widthIn(max = ActionsMaxWidth).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            ActionButtonGroup(
                primary =
                    GroupAction(
                        label = stringResource(CoreUiR.string.play),
                        icon = Icons.Rounded.PlayArrow,
                        onClick = onPlay,
                    ),
                secondary =
                    listOf(
                        GroupAction(
                            label = stringResource(CoreUiR.string.shuffle),
                            icon = Icons.Rounded.Shuffle,
                            labeled = false,
                            onClick = onShuffle,
                        )
                    ),
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = stringResource(CoreUiR.string.more_options),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.collection_edit_all)) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEditAll()
                        },
                    )
                }
            }
        }
    }
}

private val ActionsMaxWidth = 520.dp
