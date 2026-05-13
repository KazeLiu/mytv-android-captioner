package top.yogiczy.mytv.tv.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed as rowItemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import top.yogiczy.mytv.core.util.utils.humanizeMs
import top.yogiczy.mytv.tv.ui.material.Drawer
import top.yogiczy.mytv.tv.ui.material.DrawerPosition
import top.yogiczy.mytv.tv.ui.material.PopupHandleableApplication
import top.yogiczy.mytv.tv.ui.material.SimplePopup
import top.yogiczy.mytv.tv.ui.theme.MyTVTheme
import top.yogiczy.mytv.tv.ui.theme.colors
import top.yogiczy.mytv.tv.ui.tooling.PreviewWithLayoutGrids
import top.yogiczy.mytv.tv.ui.utils.focusOnLaunchedSaveable
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents
import top.yogiczy.mytv.tv.ui.utils.ifElse
import kotlin.math.max

@Composable
fun <T> SelectDialog(
    modifier: Modifier = Modifier,
    title: String,
    currentDataProvider: () -> T,
    dataListProvider: () -> List<T>,
    dataText: (T) -> String,
    columns: Int = 10,
    horizontal: Boolean = false,
    itemTextAlign: TextAlign = TextAlign.Center,
    itemMaxLines: Int = Int.MAX_VALUE,
    itemFillMaxWidth: Boolean = true,
    onDataSelected: (T) -> Unit = {},
    visibleProvider: () -> Boolean = { true },
    onDismissRequest: (() -> Unit)? = null,
) {
    val currentData = currentDataProvider()
    val dataList = dataListProvider()

    SimplePopup(
        visibleProvider = visibleProvider,
        onDismissRequest = onDismissRequest,
    ) {
        Drawer(
            position = DrawerPosition.Bottom,
            onDismissRequest = onDismissRequest,
            header = { Text(title) },
        ) {
            if (horizontal) {
                LazyRow(
                    modifier = modifier,
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItemsIndexed(dataList) { index, data ->
                        SelectDialogButton(
                            modifier = Modifier.ifElse(
                                index == max(0, dataList.indexOf(currentData)),
                                Modifier.focusOnLaunchedSaveable(),
                            ),
                            text = dataText(data),
                            selected = data == currentData,
                            maxLines = itemMaxLines,
                            onSelected = { onDataSelected(data) },
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    modifier = modifier,
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    gridItemsIndexed(dataList) { index, data ->
                        SelectDialogItem(
                            modifier = Modifier.ifElse(
                                index == max(0, dataList.indexOf(currentData)),
                                Modifier.focusOnLaunchedSaveable(),
                            ),
                            text = dataText(data),
                            textAlign = itemTextAlign,
                            maxLines = itemMaxLines,
                            fillMaxWidth = itemFillMaxWidth,
                            onSelected = { onDataSelected(data) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectDialogButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    maxLines: Int = Int.MAX_VALUE,
    onSelected: () -> Unit = {},
) {
    val shape = RoundedCornerShape(8.dp)
    var isFocused by remember { mutableStateOf(false) }
    val highlighted = selected || isFocused

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .handleKeyEvents(onSelect = onSelected)
            .focusable()
            .background(
                color = if (highlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = shape,
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Text(
            text = text,
            color = if (highlighted) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SelectDialogItem(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    fillMaxWidth: Boolean = true,
    onSelected: () -> Unit = {},
) {
    Card(
        onClick = {},
        modifier = modifier
            .handleKeyEvents(onSelect = onSelected),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colors.surfaceContainerHigh,
            focusedContainerColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onSurface),
            ),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .ifElse(fillMaxWidth, Modifier.fillMaxWidth())
                .padding(vertical = 6.dp, horizontal = 12.dp),
        )
    }
}

@Preview(device = "id:tv_720p")
@Composable
private fun SelectDialogPreview() {
    MyTVTheme {
        PreviewWithLayoutGrids {
            PopupHandleableApplication {
                SelectDialog(
                    title = "直播源缓存时间",
                    currentDataProvider = { 0L },
                    dataListProvider = {
                        (0..<24).map { it * 1000L * 60 * 60 }
                            .plus((1..15).map { it * 1000L * 60 * 60 * 24 })
                            .plus(listOf(Long.MAX_VALUE))
                    },
                    dataText = {
                        when (it) {
                            0L -> "不缓存"
                            Long.MAX_VALUE -> "永久"
                            else -> it.humanizeMs()
                        }
                    },
                )
            }
        }
    }
}
