package com.pandorina.cleanarchitectureandroidsample.domain.use_case.di

import com.pandorina.cleanarchitectureandroidsample.domain.repository.NotesRepository
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.ClearNotesUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.DeleteNoteUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.GetNotesUseCase
import com.pandorina.cleanarchitectureandroidsample.domain.use_case.InsertNoteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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