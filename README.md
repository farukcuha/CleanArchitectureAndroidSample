
# Clean Architecture in Android with Kotlin

Clean Architecture is a design pattern that separates the concerns of an application into distinct layers, making the code more modular, testable, and maintainable. This article will explore the implementation of Clean Architecture in an Android application using Kotlin, focusing on the flow from service to screen.

## Package Structure
// Write here
```
com
└── yourcompany
    └── yourapp
        ├── data
        │   ├── local
        │   │   └── dao (Room database interfaces)
        │   ├── model
        │   │   ├── entity (Entities for Room database)
        │   │   ├── request (Request objects for data operations)
        │   │   ├── response (Response objects from data operations)
        │   │   └── network_dto (Network Data Transfer Objects for mapping between layers)
        │   ├── remote
        │   │   └── api (Retrofit interfaces)
        │   └── repository (Repository interfaces)
        ├── domain
        │   ├── core (interfaces and base classes)
        │   ├── model (Domain models)
        │   ├── repository (Repository interfaces)
        │   └── usecase (Interactors)
        ├── ui
        │   ├── core (interfaces, wrappers and base classes)
        │   ├── screens ()
        └   └── theme (typefaces, theme, colors)
```



## Service Layer
// Write here
```kotlin
interface NotesService {
    @GET("notes")
    suspend fun getNotes(): Response<GetNotesResponse?>

    @POST("notes")
    suspend fun insertNote(
        @Body insertNoteRequest: InsertNoteRequest
    ): Response<Unit>

    @DELETE("notes/{noteId}")
    suspend fun deleteNote(
        @Path("noteId") noteId: String?
    ): Response<String>

    @POST("notes/clear")
    suspend fun clearNotes(): Response<String>
}
```
// Write here
```kotlin
@Module
@InstallIn(SingletonComponent::class)
class RemoteModule {

    @Provides
    @Singleton
    fun provideNotesService(): NotesService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotesService::class.java)
    }
}
```
// Write here
## Repository Layer
// Write here
```kotlin
suspend inline fun <reified K, T> performHttpRequest(
    crossinline process: suspend () -> Response<T>,
    transform: (T?) -> K
): Result<K> {
    return try {
        val response = process()
        when {
            response.isSuccessful -> {
                val body = response.body()
                val domainData = transform(body)
                Result.success(domainData)
            }
            else -> {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("${response.code()} - $errorBody"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```
// mention kotlin Result<T> class

```kotlin
interface ApiResponse<T> {
    fun toDomainModel(): T?
}
```

```kotlin
data class GetNotesResponse(
    val size: Int?,
    val data: List<NoteDto>?
): ApiResponse<List<Note>> {
    override fun toDomainModel(): List<Note>? {
        return data?.map {
            Note(
                id = it._id,
                title = it.title,
                time = it.createdTime
            )
        }
    }
}
```
// Write here

```kotlin
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>?>

    suspend fun insertNote(
        params: InsertNoteUseCase.Params
    ): Result<Unit>
}
```

```kotlin
class NotesRepositoryImpl(
    private val notesService: NotesService
): NotesRepository {

    override suspend fun getNotes(): Result<List<Note>?> {
        return performHttpRequest(
            process = { notesService.getNotes() },
            transform = { it?.toDomainModel() }
        )
    }

    override suspend fun insertNote(params: InsertNoteUseCase.Params): Result<Unit> {
        return performHttpRequest(
            process = { notesService.insertNote(InsertNoteRequest(title = params.title))},
            transform = {}
        )
    }
}
```
// Write here

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideNotesRepository(
        notesService: NotesService
    ): NotesRepository {
        return NotesRepositoryImpl(notesService)
    }
}
```
// Write here

## Use Case Layer
// Write here
```kotlin
interface BaseUseCase<P, T> {
    suspend operator fun invoke(params: P): Result<T>
}
```
// Write here

```kotlin
class GetNotesUseCase(
    private val notesRepository: NotesRepository
): BaseUseCase<Unit, List<Note>?> {

    override suspend fun invoke(params: Unit): Result<List<Note>?>  {
        return notesRepository.getNotes()
    }
}
```
// Write here

```kotlin
class InsertNoteUseCase(
    private val notesRepository: NotesRepository
): BaseUseCase<InsertNoteUseCase.Params, Unit> {

    data class Params(
        val title: String?
    )

    override suspend fun invoke(params: Params): Result<Unit> {
        return notesRepository.insertNote(params)
    }
}
```
// Write here

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideGetNotesUseCase(
        notesRepository: NotesRepository
    ): GetNotesUseCase {
        return GetNotesUseCase(notesRepository)
    }

    @Provides
    @Singleton
    fun provideInsertNoteUseCase(
        notesRepository: NotesRepository
    ): InsertNoteUseCase {
        return InsertNoteUseCase(notesRepository)
    }
}
```
// Write here

## UI Layer
```
├── notes
│     ├── NotesScreen
│     ├── NotesViewModel
│     ├── NotesUiState
│     ├── NotesUiEvent
│     └── NotesNavigation
```
```kotlin
interface NavigationCommand {

    val arguments: List<NamedNavArgument>

    val destination: String
}
```
```kotlin
object NotesNavigation: NavigationCommand {
    override val arguments: List<NamedNavArgument>
        get() = emptyList()
    override val destination: String
        get() = "notes_screen"
}
```
```kotlin
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = NotesNavigation.destination
    ) {
        composable(NotesNavigation.destination) {
            NotesScreen(navController)
        }
    }
}
```
```kotlin
interface UiState {  
    val error: Throwable?  
}

interface UiEvent
```

```kotlin
abstract class StatefulViewModel<T: UiState, E: UiEvent>(uiState: T): ViewModel() {
    private val _uiState: MutableStateFlow<T> = MutableStateFlow(uiState)
    val uiState = _uiState.asStateFlow()

    init { observeErrors() }

    abstract fun onTriggerEvent(eventType: E)

    protected fun updateState(updateState: (T) -> T) = safeLaunch {
        _uiState.update { updateState(it) }
    }

    private fun logError(exception: Throwable?) {
        Log.e(this::class.java.simpleName, exception?.localizedMessage ?: "Unknown error")
    }

    private fun observeErrors() = safeLaunch {
        _uiState.collect {
            logError(it.error)
        }
    }

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(CoroutineExceptionHandler { _, exception ->
            logError(exception)
        }, block = block)
    }
}
```

```kotlin
sealed class ViewState<out T> {  
    object Idle : ViewState<Nothing>()  
    object Loading : ViewState<Nothing>()  
    data class Success<out T>(val data: T? = null) : ViewState<T>()  
    data class Error(val error: Throwable? = null) : ViewState<Nothing>()  
}
```

```kotlin
sealed class NotesUiEvent: UiEvent {
    object GetNotes: NotesUiEvent()
    object RefreshNotes: NotesUiEvent()
    object InsertNote: NotesUiEvent()
    data class DeleteNote(val noteId: String?): NotesUiEvent()
    object ClearNotes: NotesUiEvent()
    data class UpdateInputText(val text: String): NotesUiEvent()
}

data class NotesUiState(
    override val error: Throwable? = null,
    val notesViewState: ViewState<List<Note>> = ViewState.Idle,
    val insertNoteViewState: ViewState<Unit> = ViewState.Idle,
    val deleteNoteViewState: ViewState<Unit> = ViewState.Idle,
    val clearNotesViewState: ViewState<Unit> = ViewState.Idle,
    val inputText: String = ""
): UiState {

    fun ableToInsertNote(): Boolean {
        return inputText.isNotEmpty()
    }
}
```

```kotlin
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val clearNotesUseCase: ClearNotesUseCase,
): StatefulViewModel<NotesUiState, NotesUiEvent>(NotesUiState()) {

    override fun onTriggerEvent(eventType: NotesUiEvent) {
        when(eventType) {
            is NotesUiEvent.GetNotes -> getNotes()
            is NotesUiEvent.RefreshNotes -> refreshNotes()
            is NotesUiEvent.InsertNote -> insertNote()
            is NotesUiEvent.DeleteNote -> deleteNote(eventType.noteId)
            is NotesUiEvent.ClearNotes -> clearNotes()
            is NotesUiEvent.UpdateInputText -> updateTitleToInsert(eventType.text)
        }
    }

    private fun updateTitleToInsert(title: String) = updateState { it.copy(inputText = title) }

    private fun getNotes() = safeLaunch {
        updateState { it.copy(notesViewState = ViewState.Loading) }
        onTriggerEvent(NotesUiEvent.RefreshNotes)
    }

    private fun refreshNotes() = safeLaunch {
        getNotesUseCase(Unit).onSuccess { notes ->
            updateState { it.copy(notesViewState = ViewState.Success(notes)) }
        }.onFailure { error ->
            updateState {
                it.copy(notesViewState = ViewState.Error(error), error = error)
            }
        }
    }

    private fun insertNote() = safeLaunch {
        if (uiState.value.ableToInsertNote().not()) return@safeLaunch
        updateState { it.copy(insertNoteViewState = ViewState.Loading) }
        insertNoteUseCase(InsertNoteUseCase.Params(uiState.value.inputText)).onSuccess {
            updateState { it.copy(insertNoteViewState = ViewState.Success(), inputText = "") }
            onTriggerEvent(NotesUiEvent.RefreshNotes)
        }.onFailure { error ->
            updateState {
                it.copy(insertNoteViewState = ViewState.Error(error), error = error)
            }
        }
    }

    private fun deleteNote(noteId: String?) = safeLaunch {
        updateState { it.copy(deleteNoteViewState = ViewState.Loading) }
        deleteNoteUseCase(DeleteNoteUseCase.Params(noteId)).onSuccess {
            updateState { it.copy(deleteNoteViewState = ViewState.Success()) }
            onTriggerEvent(NotesUiEvent.RefreshNotes)
        }.onFailure { error ->
            updateState {
                it.copy(deleteNoteViewState = ViewState.Error(error), error = error)
            }
        }
    }

    private fun clearNotes() = safeLaunch {
        updateState { it.copy(clearNotesViewState = ViewState.Loading) }
        clearNotesUseCase(Unit).onSuccess {
            updateState { it.copy(clearNotesViewState = ViewState.Success()) }
            onTriggerEvent(NotesUiEvent.RefreshNotes)
        }.onFailure { error ->
            updateState { it.copy(clearNotesViewState = ViewState.Error(error), error = error) }
        }
    }
}
```

```kotlin
@Composable
inline fun <reified T> ViewStateContainer(
    modifier: Modifier = Modifier,
    viewState: ViewState<T> = ViewState.Idle,
    loadingView: @Composable () -> Unit = {},
    contentView: @Composable (data: T?) -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when(viewState) {
            is ViewState.Loading -> { loadingView() }
            is ViewState.Success -> { contentView(viewState.data) }
            else -> { contentView(null) }
        }
    }
}
```

```kotlin
@Composable
fun NotesScreen(navController: NavHostController) {
    val viewModel: NotesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            context.showToast(uiState.error?.localizedMessage ?: "An error occurred")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onTriggerEvent(NotesUiEvent.GetNotes)
    }

    NotesScreenContent(
        notesViewState = uiState.notesViewState,
        insertNoteViewState = uiState.insertNoteViewState,
        clearNotesViewState = uiState.clearNotesViewState,
        onClickDeleteNote = {
            viewModel.onTriggerEvent(NotesUiEvent.DeleteNote(it))
        },
        onClickClearNotes = {
            viewModel.onTriggerEvent(NotesUiEvent.ClearNotes)
        },
        title = uiState.inputText,
        onTitleChanged = { viewModel.onTriggerEvent(NotesUiEvent.UpdateInputText(it)) },
        onClickSend = {
            viewModel.onTriggerEvent(NotesUiEvent.InsertNote)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreenContent(
    notesViewState: ViewState<List<Note>> = ViewState.Success(listOf()),
    insertNoteViewState: ViewState<Unit> = ViewState.Idle,
    clearNotesViewState: ViewState<Unit> = ViewState.Idle,
    onClickDeleteNote: (String?) -> Unit = {},
    onClickClearNotes: () -> Unit = {},
    title: String = "",
    onTitleChanged: (String) -> Unit = {},
    onClickSend: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 8.dp
            ) {
                TopAppBar(
                    title = { Text(text = "Notes") },
                    actions = {
                        ViewStateContainer(
                            viewState = clearNotesViewState,
                            loadingView = { CircularProgressIndicator() },
                            contentView = {
                                Button(onClick = onClickClearNotes) {
                                    Text(text = "Clear")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            ViewStateContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f),
                viewState = notesViewState,
                loadingView = { CircularProgressIndicator() },
                contentView = { notes ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(notes?.size ?: 0) { index ->
                            NoteView(note = notes?.get(index)) { noteId ->
                                onClickDeleteNote(noteId)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.85f)
                )
                ViewStateContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f),
                    viewState = insertNoteViewState,
                    loadingView = { CircularProgressIndicator() },
                    contentView = {
                        IconButton(onClick = {
                            onClickSend()
                            focusManager.clearFocus()
                        }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "send")
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
fun NotesScreenContentPreview() {
    CleanArchitectureAndroidSampleTheme {
         NotesScreenContent()
    }
}
```





