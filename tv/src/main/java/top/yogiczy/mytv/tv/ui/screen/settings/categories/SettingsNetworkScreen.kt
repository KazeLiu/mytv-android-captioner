package top.yogiczy.mytv.tv.ui.screen.settings.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import top.yogiczy.mytv.core.data.utils.Constants
import top.yogiczy.mytv.core.util.utils.humanizeMs
import top.yogiczy.mytv.tv.ui.screen.settings.SettingsViewModel
import top.yogiczy.mytv.tv.ui.screen.settings.components.SettingsCategoryScreen
import top.yogiczy.mytv.tv.ui.screen.settings.components.SettingsListItem
import top.yogiczy.mytv.tv.ui.screen.settings.settingsVM
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme

@Composable
fun SettingsNetworkScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = settingsVM,
    onBackPressed: () -> Unit = {},
) {
    SettingsCategoryScreen(
        modifier = modifier,
        header = { Text("设置 / 网络") },
        onBackPressed = onBackPressed,
    ) { firstItemFocusRequester ->
        item {
            SettingsListItem(
                modifier = Modifier.focusRequester(firstItemFocusRequester),
                headlineContent = "HTTP请求重试次数",
                supportingContent = "影响直播源、节目单数据获取",
                trailingContent = Constants.NETWORK_RETRY_COUNT.toString(),
                locK = true,
            )
        }

        item {
            SettingsListItem(
                headlineContent = "HTTP请求重试间隔时间",
                supportingContent = "影响直播源、节目单数据获取",
                trailingContent = Constants.NETWORK_RETRY_INTERVAL.humanizeMs(),
                locK = true,
            )
        }

        item {
            SettingsListItem(
                headlineContent = "直播网络代理",
                supportingContent = "仅代理直播源、节目单和播放流，不影响AI字幕、云同步、更新和扫码配置页",
                trailingContent = {
                    Switch(settingsViewModel.liveNetworkProxyEnable, null)
                },
                onSelect = {
                    settingsViewModel.liveNetworkProxyEnable =
                        !settingsViewModel.liveNetworkProxyEnable
                },
            )
        }

        item {
            SettingsListItem(
                headlineContent = "直播代理主机",
                supportingContent = "示例：127.0.0.1，仅在直播网络代理开启且端口有效时生效",
                trailingContent = settingsViewModel.liveNetworkProxyHost,
                remoteConfig = true,
            )
        }

        item {
            val portText = settingsViewModel.liveNetworkProxyPort
                .takeIf { it > 0 }
                ?.toString()
                ?: ""

            SettingsListItem(
                headlineContent = "直播代理端口",
                supportingContent = "示例：7890，仅支持HTTP代理",
                trailingContent = portText,
                remoteConfig = true,
            )
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun SettingsNetworkScreenPreview() {
    MyTvTheme {
        SettingsNetworkScreen()
    }
}
