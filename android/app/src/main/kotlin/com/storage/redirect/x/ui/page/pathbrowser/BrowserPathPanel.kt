package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.icon.AppIcons
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 路径面板：从顶栏下方滑出，逐级缩进并用连接线串起当前目录的各级祖先
@Immutable
private data class PathLevel(
    val path: String,
    val label: String,
    val depth: Int,
)

private sealed interface PathLevelNode {
    val indentLevel: Int

    data class Entry(val level: PathLevel, override val indentLevel: Int) : PathLevelNode

    data class Collapsed(val hiddenCount: Int, override val indentLevel: Int) : PathLevelNode
}

@Composable
internal fun BrowserPathPanel(state: PathBrowserState) {
    Box(Modifier.fillMaxSize().clipToBounds()) {
        AnimatedVisibility(
            visible = state.isPathPanelExpanded,
            enter = fadeIn(tween(SCRIM_DURATION_MILLIS)),
            exit = fadeOut(tween(SCRIM_DURATION_MILLIS)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.windowDimming)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { state.isPathPanelExpanded = false },
                    ),
            )
        }
        AnimatedVisibility(
            visible = state.isPathPanelExpanded,
            enter = slideInVertically(tween(PANEL_ENTER_MILLIS)) { -it },
            exit = slideOutVertically(tween(PANEL_EXIT_MILLIS)) { -it },
        ) {
            PathPanelContent(state)
        }
    }
}

@Composable
private fun PathPanelContent(state: PathBrowserState) {
    val rootLabel = rootLabel(state)
    val levels = remember(state.currentPath, rootLabel) { buildPathLevels(state.currentPath, rootLabel) }
    var isShowingAllLevels by remember(levels) { mutableStateOf(false) }
    val onLevelClick = { path: String ->
        state.isPathPanelExpanded = false
        if (path != state.currentPath) state.navigateTo(path)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = PANEL_BOTTOM_MARGIN)
            .dropShadow(shape = PANEL_SHAPE, shadow = PANEL_SHADOW)
            .background(MiuixTheme.colorScheme.surface, PANEL_SHAPE)
            .padding(vertical = PANEL_PADDING)
            .testTag("browser_path_panel"),
    ) {
        PathPanelHeader(
            fullPath = state.absolutePath,
            isShowingAllLevels = isShowingAllLevels,
            onCollapseClick = { isShowingAllLevels = false },
        )
        HorizontalDivider(
            modifier = Modifier.padding(PANEL_PADDING),
            color = MiuixTheme.colorScheme.dividerLine,
        )
        if (isShowingAllLevels) {
            FlatLevelList(levels, onLevelClick)
        } else {
            IndentedLevelList(levels, onExpandAllClick = { isShowingAllLevels = true }, onLevelClick)
        }
    }
}

// 完整路径可长按选择复制
@Composable
private fun PathPanelHeader(fullPath: String, isShowingAllLevels: Boolean, onCollapseClick: () -> Unit) {
    Row(Modifier.padding(horizontal = PANEL_PADDING + ROW_PADDING), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.browser_current_location),
                style = MiuixTheme.textStyles.subtitle,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(2.dp))
            SelectionContainer {
                Text(
                    text = fullPath,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("browser_full_path"),
                )
            }
        }
        if (isShowingAllLevels) {
            Spacer(Modifier.width(ICON_SPACING))
            Surface(
                onClick = onCollapseClick,
                shape = RoundedCornerShape(50),
                color = MiuixTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.browser_path_collapse),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun IndentedLevelList(
    levels: List<PathLevel>,
    onExpandAllClick: () -> Unit,
    onLevelClick: (String) -> Unit,
) {
    val nodes = remember(levels) { levels.toLevelNodes() }
    val connectorColor = MiuixTheme.colorScheme.outline
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PANEL_PADDING)
            .drawBehind { drawLevelConnectors(nodes, connectorColor) },
    ) {
        nodes.forEachIndexed { index, node ->
            when (node) {
                is PathLevelNode.Entry -> PathLevelRow(
                    level = node.level,
                    isCurrent = index == nodes.lastIndex,
                    rowHeight = ROW_HEIGHT,
                    onClick = { onLevelClick(node.level.path) },
                    modifier = Modifier.padding(start = INDENT_STEP * node.indentLevel),
                )
                is PathLevelNode.Collapsed -> CollapsedLevelsRow(node, onExpandAllClick)
            }
        }
    }
}

// 展开全部层级时改为带序号的平铺列表，限制高度可滚动
@Composable
private fun FlatLevelList(levels: List<PathLevel>, onLevelClick: (String) -> Unit) {
    val listState = rememberLazyListState()
    val maxHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } * EXPANDED_HEIGHT_RATIO
    LaunchedEffect(Unit) { listState.scrollToItem(COLLAPSED_HEAD_LEVELS) }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .heightIn(max = maxHeight)
            .padding(horizontal = PANEL_PADDING),
    ) {
        itemsIndexed(levels, key = { _, level -> level.depth }) { index, level ->
            val isCurrent = index == levels.lastIndex
            PathLevelRow(
                level = level,
                isCurrent = isCurrent,
                rowHeight = FLAT_ROW_HEIGHT,
                onClick = { onLevelClick(level.path) },
                leadingContent = {
                    Text(
                        text = if (level.depth == 0) "" else level.depth.toString(),
                        modifier = Modifier.width(LEVEL_INDEX_WIDTH),
                        color = if (isCurrent) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.footnote1,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(ICON_SPACING))
                },
            )
        }
    }
}

@Composable
private fun PathLevelRow(
    level: PathLevel,
    isCurrent: Boolean,
    rowHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable () -> Unit = {},
) {
    val accentColor = MiuixTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = ROW_SHAPE,
        color = if (isCurrent) accentColor.copy(alpha = CURRENT_ROW_ALPHA) else Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .testTag("browser_path_level_${level.depth}"),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .padding(horizontal = ROW_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()
            Icon(
                imageVector = if (level.depth == 0) AppIcons.Phone else AppIcons.Folder,
                contentDescription = null,
                tint = if (isCurrent) accentColor else MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.size(ICON_SIZE),
            )
            Spacer(Modifier.width(ICON_SPACING))
            Text(
                text = level.label,
                modifier = Modifier.weight(1f),
                color = if (isCurrent) accentColor else MiuixTheme.colorScheme.onSurface,
                style = MiuixTheme.textStyles.body1,
                fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isCurrent) {
                Icon(AppIcons.Check, null, Modifier.size(CHECK_ICON_SIZE), tint = accentColor)
            }
        }
    }
}

@Composable
private fun CollapsedLevelsRow(node: PathLevelNode.Collapsed, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ROW_SHAPE,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = INDENT_STEP * node.indentLevel)
            .testTag("browser_path_expand_all"),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(ROW_HEIGHT)
                .padding(horizontal = ROW_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                AppIcons.More, null, Modifier.size(ICON_SIZE),
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
            Spacer(Modifier.width(ICON_SPACING))
            Text(
                text = pluralStringResource(R.plurals.browser_path_more_levels, node.hiddenCount, node.hiddenCount),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun DrawScope.drawLevelConnectors(nodes: List<PathLevelNode>, color: Color) {
    val rowHeight = ROW_HEIGHT.toPx()
    val indentStep = INDENT_STEP.toPx()
    val iconRadius = ICON_SIZE.toPx() / 2
    val iconCenterOffset = ROW_PADDING.toPx() + iconRadius
    val corner = CONNECTOR_CORNER.toPx()
    val gap = CONNECTOR_GAP.toPx()
    val stroke = Stroke(width = CONNECTOR_WIDTH.toPx(), cap = StrokeCap.Round)

    for (index in 1..nodes.lastIndex) {
        val startX = nodes[index - 1].indentLevel * indentStep + iconCenterOffset
        val startY = (index - 1) * rowHeight + rowHeight / 2 + iconRadius + gap
        val centerY = index * rowHeight + rowHeight / 2
        val endX = nodes[index].indentLevel * indentStep + iconCenterOffset - iconRadius - gap
        val connector = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, centerY - corner)
            quadraticTo(startX, centerY, startX + corner, centerY)
            lineTo(endX, centerY)
        }
        drawPath(path = connector, color = color, style = stroke)
    }
}

private fun buildPathLevels(currentPath: String, rootLabel: String): List<PathLevel> {
    val parts = currentPath.split('/').filter { it.isNotEmpty() }
    return listOf(PathLevel(path = "", label = rootLabel, depth = 0)) +
        parts.mapIndexed { index, name ->
            PathLevel(path = parts.take(index + 1).joinToString("/"), label = name, depth = index + 1)
        }
}

// 层级过多时保留开头两级与末尾几级，中间折叠成一行
private fun List<PathLevel>.toLevelNodes(): List<PathLevelNode> {
    if (size <= MAX_VISIBLE_LEVELS) {
        return map { PathLevelNode.Entry(level = it, indentLevel = it.depth) }
    }
    val tailCount = MAX_VISIBLE_LEVELS - COLLAPSED_HEAD_LEVELS - 1
    val headLevels = take(COLLAPSED_HEAD_LEVELS)
    val tailLevels = takeLast(tailCount)
    val hiddenCount = size - COLLAPSED_HEAD_LEVELS - tailCount
    return buildList {
        headLevels.forEachIndexed { index, level ->
            add(PathLevelNode.Entry(level = level, indentLevel = index))
        }
        add(PathLevelNode.Collapsed(hiddenCount = hiddenCount, indentLevel = COLLAPSED_HEAD_LEVELS))
        tailLevels.forEachIndexed { index, level ->
            add(PathLevelNode.Entry(level = level, indentLevel = COLLAPSED_HEAD_LEVELS + 1 + index))
        }
    }
}

private val PANEL_SHAPE = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
private val ROW_SHAPE = RoundedCornerShape(12.dp)
private val PANEL_SHADOW = Shadow(radius = 10.dp, color = Color.Black, alpha = 0.1f)
private val PANEL_BOTTOM_MARGIN = 8.dp
private val PANEL_PADDING = 14.dp
private val ROW_HEIGHT = 46.dp
private val FLAT_ROW_HEIGHT = 40.dp
private val ROW_PADDING = 10.dp
private val INDENT_STEP = 26.dp
private val ICON_SIZE = 20.dp
private val ICON_SPACING = 10.dp
private val CHECK_ICON_SIZE = 18.dp
private val LEVEL_INDEX_WIDTH = 20.dp
private val CONNECTOR_WIDTH = 1.5.dp
private val CONNECTOR_CORNER = 7.dp
private val CONNECTOR_GAP = 3.dp
private const val CURRENT_ROW_ALPHA = 0.14f
private const val EXPANDED_HEIGHT_RATIO = 0.6f
private const val MAX_VISIBLE_LEVELS = 7
private const val COLLAPSED_HEAD_LEVELS = 2
private const val SCRIM_DURATION_MILLIS = 160
private const val PANEL_ENTER_MILLIS = 220
private const val PANEL_EXIT_MILLIS = 160
