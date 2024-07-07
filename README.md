
# Clean Architecture - Kotlin - Android - Jetpack Compose
[Project Link](https://github.com/farukcuha/CleanArchitectureAndroidSample)

Clean Architecture is a design pattern that separates the concerns of an application into distinct layers, making the code more modular, testable, and maintainable. This article will explore the implementation of Clean Architecture in an Android application using Kotlin, focusing on the flow from service to screen. 
## Package Structure

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
        │   ├── screens
        └   └── theme (typefaces, theme, colors)
```
The package structure is organized according to the Clean Architecture layers. Each package has a specific role in the application:

- `data`: This package contains all the data-related classes. It includes the Room database interfaces (`dao`), the entities for the Room database (`entity`), the request and response objects for data operations (`request` and `response`), and the network data transfer objects for mapping between layers (`network_dto`). The `remote` package contains the Retrofit interfaces (`api`), and the `repository` package contains the repository interfaces.
- `domain`: This package contains the core business logic of the application. It includes interfaces and base classes (`core`), domain models (`model`), repository interfaces (`repository`), and use cases or interactors (`usecase`).
- `ui`: This package contains all the UI-related classes. It includes interfaces, wrappers, and base classes (`core`), the different screens of the application (`screens`), and the theme-related classes (`theme`).

## Service Layer
The service layer is responsible for making network requests. It includes a `NotesService` interface with methods to get notes, insert a note, delete a note, and clear notes.

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

The `NotesService` interface is provided as a singleton in the `RemoteModule` class. This class uses the Retrofit library to create an instance of `NotesService`.

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

The `RemoteModule` is installed in the `SingletonComponent` to ensure that there is only one instance of `NotesService` throughout the application. This is important to prevent multiple instances of the service from being created, which could lead to issues such as inconsistent data or unnecessary network requests. The `provideNotesService` method uses the Retrofit builder to create the `NotesService` instance. The base URL for the API is retrieved from the `BuildConfig`, and the Gson converter factory is added to handle the conversion of JSON data to Kotlin objects. Finally, the `NotesService` class is created from the Retrofit instance. This service will be used to make all the network requests in the application. 

## Repository Layer
The repository layer is responsible for handling data operations in the application. It uses the `Result<T>` class from Kotlin to represent the result of these operations. [Click to see the Result<T> class reference](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/)

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
This function `performHttpRequest` is a generic function that performs a network request and transforms the response into a domain model. It takes two parameters: `process`, a suspend function that performs the network request and returns a `Response<T>`, and `transform`, a function that transforms the response body into a domain model of type `K`. The function returns a `Result<K>`.

The `ApiResponse<T>` interface represents a response from the API. It has a single function `toDomainModel()` that transforms the response into a domain model.

```kotlin
interface ApiResponse<T> {
    fun toDomainModel(): T?
}
```

The `GetNotesResponse` data class represents the response from the `getNotes` API call. It implements the `ApiResponse<List<Note>>` interface and overrides the `toDomainModel()` function to transform the response into a list of `Note` domain models.

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

```kotlin
data class InsertNoteRequest(
    val title: String?
)
```

The `NotesRepository` interface defines the functions for the repository layer. It includes functions to get notes and insert a note.

```kotlin
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>?>

    suspend fun insertNote(
        params: InsertNoteUseCase.Params
    ): Result<Unit>
}
```

The `NotesRepositoryImpl` class implements the `NotesRepository` interface. It uses the `NotesService` to perform the network requests and the `performHttpRequest` function to handle the responses.

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

The `RepositoryModule` class provides the `NotesRepository` as a singleton. It uses the `NotesService` to create an instance of `NotesRepositoryImpl`.

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

This module is installed in the `SingletonComponent` to ensure that there is only one instance of `NotesRepository` throughout the application. This is important to prevent multiple instances of the repository from being created, which could lead to issues such as inconsistent data. The `provideNotesRepository` method uses the `NotesService` to create the `NotesRepositoryImpl` instance. This repository will be used to handle all the data operations in the application. 

## Use Case Layer
The use case layer is responsible for executing business logic in a use case scenario. The `BaseUseCase` interface is a generic interface that represents a use case with input parameters `P` and output results `T`.

```kotlin
interface BaseUseCase<P, T> {
    suspend operator fun invoke(params: P): Result<T>
}
```

The `GetNotesUseCase` class implements the `BaseUseCase` interface and overrides the `invoke` function to get notes from the repository.

```kotlin
class GetNotesUseCase(
    private val notesRepository: NotesRepository
): BaseUseCase<Unit, List<Note>?> {

    override suspend fun invoke(params: Unit): Result<List<Note>?>  {
        return notesRepository.getNotes()
    }
}
```

The `InsertNoteUseCase` class implements the `BaseUseCase` interface and overrides the `invoke` function to insert a note into the repository.

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

The `DeleteNoteUseCase` class implements the `BaseUseCase` interface and overrides the `invoke` function to delete a note from the repository.

```kotlin
class DeleteNoteUseCase(
    private val repository: NotesRepository
) : BaseUseCase<DeleteNoteUseCase.Params, Unit> {

    data class Params(
        val noteId: String?
    )

    override suspend fun invoke(params: Params): Result<Unit> {
        return repository.deleteNote(params)
    }
}
```

The `ClearNotesUseCase` class implements the `BaseUseCase` interface and overrides the `invoke` function to clear all notes from the repository.

```kotlin
class ClearNotesUseCase(
    private val repository: NotesRepository
): BaseUseCase<Unit, Unit> {

    override suspend fun invoke(params: Unit): Result<Unit> {
        return repository.clearNotes()
    }
}
```

The `UseCaseModule` class provides the use cases as singletons. It uses the `NotesRepository` to create instances of the use cases.

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

    @Provides
    @Singleton
    fun provideDeleteNoteUseCase(
        notesRepository: NotesRepository
    ): DeleteNoteUseCase {
        return DeleteNoteUseCase(notesRepository)
    }

    @Provides
    @Singleton
    fun provideClearNotesUseCase(
        notesRepository: NotesRepository
    ): ClearNotesUseCase {
        return ClearNotesUseCase(notesRepository)
    }
}
```

This module is installed in the `SingletonComponent` to ensure that there is only one instance of each use case throughout the application. This is important to prevent multiple instances of the use cases from being created, which could lead to issues such as inconsistent data. The `provide...UseCase` methods use the `NotesRepository` to create the use case instances. These use cases will be used to handle all the business logic in the application.

Sure, I can help you fill in the sections. Here's a possible way to complete the sections:

## UI Layer
The UI layer is responsible for rendering the user interface and handling user interactions. It communicates with the ViewModel to get the necessary data and updates the UI accordingly.

```kotlin
├── notes
│     ├── NotesScreen // This is the main screen that displays the list of notes.
│     ├── NotesViewModel // This is the ViewModel that manages the state of the NotesScreen.
│     ├── NotesUiState // This represents the different states the NotesScreen can be in.
│     ├── NotesUiEvent // These are the different events that can trigger state changes.
│     └── NotesNavigation // This handles navigation for the NotesScreen.
```

The `NavigationCommand` interface defines the contract for navigation commands. Each navigation command will have a list of arguments and a destination.

```kotlin
interface NavigationCommand {
    val arguments: List<NamedNavArgument>
    val destination: String
}
```

The `NotesNavigation` object is a concrete implementation of the `NavigationCommand` interface for the notes screen.

```kotlin
object NotesNavigation: NavigationCommand {
    override val arguments: List<NamedNavArgument>
        get() = emptyList()
    override val destination: String
        get() = "notes_screen"
}
```

The `AppRoot` function is a composable function that sets up the navigation for the application.

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

The `UiState` and `UiEvent` interfaces define the contract for UI states and events.

```kotlin
interface UiState {  
    val error: Throwable?  
}

interface UiEvent
```

The `StatefulViewModel` abstract class is a base class for ViewModels that have a state. It provides functionality for updating the state and handling errors.

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

The `ViewState` sealed class represents the different states a view can be in.

```kotlin
sealed class ViewState<out T> {  
    object Idle : ViewState<Nothing>()  
    object Loading : ViewState<Nothing>()  
    data class Success<out T>(val data: T? = null) : ViewState<T>()  
    data class Error(val error: Throwable? = null) : ViewState<Nothing>()  
}
```

The `NotesUiEvent` sealed class represents the different events that can occur in the notes screen. The `NotesUiState` data class represents the different states the notes screen can be in.

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
Sure, here's a possible way to complete the sections:

The `NotesViewModel` class is a ViewModel for the notes screen. It handles the different events that can occur and updates the state accordingly. It uses different use cases to perform actions like getting notes, inserting a note, deleting a note, and clearing notes.

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

The `ViewStateContainer` composable function is a container that displays different views based on the current view state. It can display a loading view, a content view, or a default view.

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
Sure, let's break down the code and provide explanations for each section.

The `NotesScreen` is a composable function that sets up the notes screen. It creates an instance of `NotesViewModel`, collects the UI state, and handles errors. It also triggers the `GetNotes` event when the screen is launched.

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
        onClickDeleteNote = { viewModel.onTriggerEvent(NotesUiEvent.DeleteNote(it)) },
        onClickClearNotes = { viewModel.onTriggerEvent(NotesUiEvent.ClearNotes) },
        title = uiState.inputText,
        onTitleChanged = { viewModel.onTriggerEvent(NotesUiEvent.UpdateInputText(it)) },
        onClickSend = { viewModel.onTriggerEvent(NotesUiEvent.InsertNote) }
    )
}
```

The `NotesScreenContent` is another composable function that displays the content of the notes screen. It shows a list of notes and provides functionality for deleting notes, clearing notes, and inserting new notes.

```kotlin
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
            Surface(shadowElevation = 8.dp) {
                TopAppBar(
                    title = { Text(text = "Notes") },
                    actions = {
                        ViewStateContainer(
                            viewState = clearNotesViewState,
                            loadingView = { CircularProgressIndicator() },
                            contentView = { Button(onClick = onClickClearNotes) { Text(text = "Clear") } }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            ViewStateContainer(
                modifier = Modifier.fillMaxSize().weight(1.0f),
                viewState = notesViewState,
                loadingView = { CircularProgressIndicator() },
                contentView = { notes ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(notes?.size ?: 0) { index ->
                            NoteView(note = notes?.get(index)) { noteId -> onClickDeleteNote(noteId) }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            )
            Row(
                modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp).padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(value = title, onValueChange = onTitleChanged, modifier = Modifier.fillMaxWidth().weight(0.85f))
                ViewStateContainer(
                    modifier = Modifier.fillMaxWidth().weight(0.15f),
                    viewState = insertNoteViewState,
                    loadingView = { CircularProgressIndicator() },
                    contentView = {
                        IconButton(onClick = { onClickSend(); focusManager.clearFocus() }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "send")
                        }
                    }
                )
            }
        }
    }
}
```

Finally, the `NotesScreenContentPreview` function is used to preview the `NotesScreenContent` in both light and dark modes.

```kotlin
@Composable
@Preview(showBackground = true, name = "LightMode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "DarkMode")
fun NotesScreenContentPreview() {
    CleanArchitectureAndroidSampleTheme {
        NotesScreenContent()
    }
}
```
In conclusion, implementing Clean Architecture in an Android application using Kotlin provides a structured and organized approach to development. By separating concerns into distinct layers, such as the service layer for network requests and the repository layer for data operations, we ensure that each component has a clear responsibility. This separation enhances the modularity, testability, and maintainability of the code. Additionally, by using patterns like singletons for service instances and the Result class for handling operation results, we can further improve the reliability and efficiency of the application. Overall, Clean Architecture offers a robust framework for building scalable and maintainable Android applications.






