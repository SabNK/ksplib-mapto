package ru.polescanner.ksp.domain

import java.util.*

data class User(val id: UUID, val author: Author)

data class Author(val id: UUID, val name: String, val surname: String)

interface IMapper<I, O> {
    fun mapTo(i: I): O
    fun mapFrom(o: O): I
}