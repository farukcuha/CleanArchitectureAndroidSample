# Clean Architecture in Android with Kotlin

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
        │   ├── screens ()
        └   └── theme (typefaces, theme, colors)
```



## Service Layer
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

## Repository Layer
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
## Use Case Layer

## Use Case Layer

## Screen Layer
