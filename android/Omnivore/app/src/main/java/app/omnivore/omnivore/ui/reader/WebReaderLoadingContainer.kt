package app.omnivore.omnivore.ui.reader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.omnivore.omnivore.MainActivity
import app.omnivore.omnivore.R
import app.omnivore.omnivore.persistence.entities.SavedItemLabel
import app.omnivore.omnivore.ui.components.LabelsSelectionSheetContent
import app.omnivore.omnivore.ui.notebook.NotebookView
import app.omnivore.omnivore.ui.notebook.NotebookViewModel
import app.omnivore.omnivore.ui.savedItemViews.SavedItemContextMenu
import app.omnivore.omnivore.ui.theme.OmnivoreTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import app.omnivore.omnivore.ui.notebook.EditNoteModal


@AndroidEntryPoint
class WebReaderLoadingContainerActivity: ComponentActivity() {
  val viewModel: WebReaderViewModel by viewModels()
  val notebookViewModel: NotebookViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val requestID = intent.getStringExtra("SAVED_ITEM_REQUEST_ID")
    val slug = intent.getStringExtra("SAVED_ITEM_SLUG")

    setContent {
      val systemUiController = rememberSystemUiController()
      val useDarkIcons = !isSystemInDarkTheme()

      DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setSystemBarsColor(
          color = Color.Black,
          darkIcons = false
        )

        onDispose {}
      }

      OmnivoreTheme {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
        ) {
          if (viewModel.hasFetchError.value == true) {
            Text("We were unable to fetch your content.")
          } else {
            WebReaderLoadingContainer(
              requestID = requestID,
              slug = slug,
              onLibraryIconTap = if (requestID != null) { { startMainActivity() } } else null,
              webReaderViewModel = viewModel,
              notebookViewModel = notebookViewModel,
            )
          }
        }
      }
    }

    // animate the view up when keyboard appears
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val rootView = findViewById<View>(android.R.id.content).rootView
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
      val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
      rootView.setPadding(0, 0, 0, imeHeight)
      insets
    }
  }

  private fun startMainActivity() {
    val intent = Intent(this, MainActivity::class.java)
    this.startActivity(intent)
  }
}

enum class BottomSheetState(
) {
  NONE(),
  PREFERENCES(),
  NOTEBOOK(),
  ADDNOTE(),
  HIGHLIGHTNOTE(),
  LABELS(),
  LINK()
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WebReaderLoadingContainer(slug: String? = null, requestID: String? = null,
                              onLibraryIconTap: (() -> Unit)? = null,
                              webReaderViewModel: WebReaderViewModel,
                              notebookViewModel: NotebookViewModel) {
  val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
  val bottomSheetState: BottomSheetState? by webReaderViewModel.bottomSheetStateLiveData.observeAsState(BottomSheetState.NONE)

  val webReaderParams: WebReaderParams? by webReaderViewModel.webReaderParamsLiveData.observeAsState(null)
  val shouldPopView: Boolean by webReaderViewModel.shouldPopViewLiveData.observeAsState(false)

  val labels: List<SavedItemLabel> by webReaderViewModel.savedItemLabelsLiveData.observeAsState(listOf())

  val maxToolbarHeight = 48.dp
  webReaderViewModel.maxToolbarHeightPx = with(LocalDensity.current) { maxToolbarHeight.roundToPx().toFloat() }
  webReaderViewModel.loadItem(slug = slug, requestID = requestID)

  val coroutineScope = rememberCoroutineScope()

  val styledContent = webReaderParams?.let {
    val webReaderContent = WebReaderContent(
      preferences = webReaderViewModel.storedWebPreferences(isSystemInDarkTheme()),
      item = it.item,
      articleContent = it.articleContent,
    )
    webReaderContent.styledContent()
  } ?: null


  val modalBottomSheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    skipHalfExpanded = bottomSheetState == BottomSheetState.ADDNOTE,
    confirmValueChange = {
      if (it == ModalBottomSheetValue.Hidden) {
        webReaderViewModel.resetBottomSheet()
      }
      true
    }
  )


    when (bottomSheetState) {
      BottomSheetState.PREFERENCES -> {
        coroutineScope.launch {
          if (!modalBottomSheetState.isVisible) {
            modalBottomSheetState.show()
          }
        }
      }
      BottomSheetState.NOTEBOOK -> {
        coroutineScope.launch {
          modalBottomSheetState.show()
        }
      }
      BottomSheetState.ADDNOTE -> {
        coroutineScope.launch {
          modalBottomSheetState.show()
        }
      }
      BottomSheetState.HIGHLIGHTNOTE -> {
        coroutineScope.launch {
          modalBottomSheetState.show()
        }
      }
      BottomSheetState.LABELS -> {
        coroutineScope.launch {
          modalBottomSheetState.show()
        }
      }
      BottomSheetState.LINK -> {
        coroutineScope.launch {
          modalBottomSheetState.show()
        }
      }
      BottomSheetState.NONE -> {
        coroutineScope.launch {
          modalBottomSheetState.hide()
        }
      }
      else -> {
        coroutineScope.launch {
          modalBottomSheetState.hide()
        }
      }
    }

  ModalBottomSheetLayout(
    modifier = Modifier
      .statusBarsPadding(),
    sheetBackgroundColor = Color.Transparent,
    sheetState = modalBottomSheetState,
    sheetContent = {
      when (bottomSheetState) {
        BottomSheetState.PREFERENCES -> {
          BottomSheetUI("Reader Preferences") {
            ReaderPreferencesView(webReaderViewModel)
          }
        }
        BottomSheetState.NOTEBOOK -> {
          webReaderParams?.let { params ->
            BottomSheetUI(title = "Notebook") {
              NotebookView(savedItemId = params.item.savedItemId, viewModel = notebookViewModel, onEditArticleNotes = {
                webReaderViewModel.setBottomSheet(BottomSheetState.ADDNOTE)
              })
            }
          }
        }
        BottomSheetState.ADDNOTE -> {
          webReaderParams?.let { params ->
            EditNoteModal(onDismiss = {
              coroutineScope.launch {
                notebookViewModel.addArticleNote(savedItemId = params.item.savedItemId, note = it)
                webReaderViewModel.setBottomSheet(BottomSheetState.NOTEBOOK)
              }
            })
          }
        }
        BottomSheetState.HIGHLIGHTNOTE -> {
          webReaderViewModel.annotation?.let { annotation ->
            BottomSheetUI(title = "Edit Note") {
              AnnotationEditView(
                initialAnnotation = annotation,
                onSave = {
                  webReaderViewModel.saveAnnotation(it)
                  coroutineScope.launch {
                    webReaderViewModel.resetBottomSheet()
                  }
                },
                onCancel = {
                  webReaderViewModel.cancelAnnotationEdit()
                  coroutineScope.launch {
                    webReaderViewModel.resetBottomSheet()
                  }
                }
              )
            }
          }
        }
        BottomSheetState.LABELS -> {
          BottomSheetUI(title = "Notebook") {
            LabelsSelectionSheetContent(
              labels = labels,
              initialSelectedLabels = webReaderParams?.labels ?: listOf(),
              onCancel = {
                coroutineScope.launch {
                  webReaderViewModel.resetBottomSheet()
                }
              },
              isLibraryMode = false,
              onSave = {
                if (it != labels) {
                  webReaderViewModel.updateSavedItemLabels(
                    savedItemID = webReaderParams?.item?.savedItemId ?: "", labels = it
                  )
                }
                coroutineScope.launch {
                  webReaderViewModel.resetBottomSheet()
                }
              },
              onCreateLabel = { newLabelName, labelHexValue ->
                webReaderViewModel.createNewSavedItemLabel(newLabelName, labelHexValue)
              }
            )
          }
        }
        BottomSheetState.LINK -> {
          BottomSheetUI(title = "Open Link") {
            OpenLinkView(webReaderViewModel)
          }
        }
        BottomSheetState.NONE -> {

        }
        else -> {

        }
      }
      Spacer(modifier = Modifier.weight(1.0F))
    }
  ) {
    Scaffold(
      topBar = {
        ReaderTopAppBar(webReaderViewModel, onLibraryIconTap)
      }) { paddingValues ->
        if (styledContent != null) {
          WebReader(
            styledContent = styledContent,
            webReaderViewModel = webReaderViewModel
          )
        }

        LaunchedEffect(shouldPopView) {
          if (shouldPopView) {
            onBackPressedDispatcher?.onBackPressed()
          }
        }
      }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopAppBar(webReaderViewModel: WebReaderViewModel, onLibraryIconTap: (() -> Unit)? = null) {
  val context = LocalContext.current
  val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

  val isDarkMode = isSystemInDarkTheme()
  val currentThemeKey = webReaderViewModel.currentThemeKey.observeAsState()
  val currentTheme = Themes.values().find { it.themeKey == currentThemeKey.value }
  val toolbarHeightPx: Float by webReaderViewModel.currentToolbarHeightLiveData.observeAsState(0.0f)
  val webReaderParams: WebReaderParams? by webReaderViewModel.webReaderParamsLiveData.observeAsState(null)
  var isMenuExpanded by remember { mutableStateOf(false) }

  val themeBackgroundColor = currentTheme?.let {
    if (it.themeKey == "System" && isDarkMode) {
      Color(0xFF000000)
    } else if (it.themeKey == "System" ) {
      Color(0xFFFFFFFF)
    } else {
      Color(it.backgroundColor ?: 0xFFFFFFFF)
    }
  } ?: Color(0xFFFFFFFF)

  val themeTintColor = currentTheme?.let {
    if (it.themeKey == "System" && isDarkMode) {
      Color(0xFFFFFFFF)
    } else if (it.themeKey == "System" ) {
      Color(0xFF000000)
    } else {
      Color(it.foregroundColor ?: 0xFF000000)
    }
  } ?: Color(0xFF000000)


  TopAppBar(
    modifier = Modifier
      .height(height = with(LocalDensity.current) {
        toolbarHeightPx.roundToInt().toDp()
      }),
    backgroundColor = themeBackgroundColor,
    elevation = 0.dp,
    title = {},
    navigationIcon = {
      IconButton(onClick = {
        onBackPressedDispatcher?.onBackPressed()
      }) {
        Icon(
          imageVector = Icons.Filled.ArrowBack,
          modifier = Modifier,
          contentDescription = "Back",
          tint = themeTintColor
        )
      }
    },
    actions = {
      if (onLibraryIconTap != null) {
        IconButton(onClick = { onLibraryIconTap() }) {
          Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            tint = themeTintColor,
          )
        }
      }
      webReaderParams?.let {
        IconButton(onClick = {
          webReaderViewModel.setBottomSheet(BottomSheetState.NOTEBOOK)
        }) {
          Icon(
            painter = painterResource(id = R.drawable.notebook),
            contentDescription = null,
            tint = themeTintColor
          )
        }
      }
      IconButton(onClick = {
        webReaderViewModel.setBottomSheet(BottomSheetState.PREFERENCES)
      }) {
        Icon(
          painter = painterResource(id = R.drawable.format_letter_case),
          contentDescription = null,
          tint = themeTintColor
        )
      }
      IconButton(onClick = { isMenuExpanded = true }) {
        Icon(
          painter = painterResource(id = R.drawable.dots_horizontal),
          contentDescription = null,
          tint = themeTintColor
        )
        if (isMenuExpanded) {
          webReaderParams?.let { params ->
            SavedItemContextMenu(
              context = context,
              isExpanded = isMenuExpanded,
              isArchived = params.item.isArchived,
              onDismiss = { isMenuExpanded = false },
              webReaderViewModel = webReaderViewModel,
              actionHandler = {
                webReaderViewModel.handleSavedItemAction(
                  params.item.savedItemId,
                  it
                )
              }
            )
          }
        }
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BottomSheetUI(title: String?, content: @Composable () -> Unit) {
  Box(
    modifier = Modifier
      .wrapContentHeight()
      .fillMaxWidth()
      .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp))
      .background(Color.White)
      .statusBarsPadding()
  ) {
    Scaffold(
    ) { paddingValues ->
      Box(modifier = Modifier.fillMaxSize()) {
        content()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun OpenLinkView(webReaderViewModel: WebReaderViewModel) {
  val context = LocalContext.current

  Column(modifier = Modifier
    .padding(top = 50.dp)
    .padding(horizontal = 50.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
    Row {
      Button(onClick = { webReaderViewModel.openCurrentLink(context) }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Open in Browser")

      }
    }
    Row() {
      Button(onClick = { webReaderViewModel.saveCurrentLink(context) }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Save to Omnivore")

      }
    }
    Row() {
      Button(onClick = {webReaderViewModel.copyCurrentLink(context) }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Copy Link")

      }
    }
    Row {
      Button(onClick = {webReaderViewModel.resetBottomSheet() }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Cancel")

      }
    }
  }
}
