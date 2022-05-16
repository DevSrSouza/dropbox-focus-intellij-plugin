package br.com.devsrsouza.intellij.dropboxfocus.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusModule
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.color.LocalButtonColors
import io.kanro.compose.jetbrains.control.Button
import io.kanro.compose.jetbrains.control.JPanel
import io.kanro.compose.jetbrains.control.Text
import io.kanro.compose.jetbrains.control.TextField
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun FocusSelection(
    currentFocusGradleSettingsState: StateFlow<FocusGradleSettings?>,
    isLoadingState: StateFlow<Boolean>,
    syncGradle: () -> Unit,
    selectModuleToFocus: (FocusGradleSettings, FocusModule) -> Unit,
    clearFocus: () -> Unit,
    withClearFocusButton: Boolean,
) {
    Theme {
        JPanel(
            modifier = Modifier.fillMaxSize()
        ) {
            val currentFocusGradleSettings = currentFocusGradleSettingsState.collectAsState().value
            val isLoading = isLoadingState.collectAsState().value

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        color = LocalButtonColors.current.defaultStart
                    )
                }
            } else {
                if (currentFocusGradleSettings == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Focus plugin not present.",
                            style = JBTheme.typography.h3Bold,
                        )
                        Button(
                            onClick = syncGradle,
                            modifier = Modifier.padding(all = 32.dp)
                        ) {
                            Text(
                                "Sync Gradle",
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                        if (currentFocusGradleSettings.currentFocusModulePath != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Current focus:",
                                    style = JBTheme.typography.h3Bold,
                                )
                                Text(
                                    text = currentFocusGradleSettings.currentFocusModulePath ?: "None",
                                    modifier = Modifier.padding(start = 24.dp),
                                    style = JBTheme.typography.h3,
                                    color = JBTheme.textColors.success,
                                )
                                if (withClearFocusButton) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(onClick = clearFocus) {
                                        Text("Clear Focus")
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Search",
                            style = JBTheme.typography.h3Bold,
                        )

                        val (search, setSearch) = remember {
                            mutableStateOf("")
                        }

                        TextField(
                            value = search,
                            onValueChange = setSearch,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Choose a module to Focus",
                            style = JBTheme.typography.h3Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
                        ) {

                            val state = rememberLazyListState()
                            val modules = remember(search) {
                                currentFocusGradleSettings.allModules.filter {
                                    search.isBlank() ||
                                        search.lowercase() in it.gradleModulePath.lowercase()
                                }
                            }

                            LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp, top = 4.dp), state) {
                                items(modules) { item ->
                                    Button(
                                        onClick = { selectModuleToFocus(currentFocusGradleSettings, item) },
                                        modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                                    ) {
                                        Text(
                                            text = item.gradleModulePath,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(
                                    scrollState = state
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}