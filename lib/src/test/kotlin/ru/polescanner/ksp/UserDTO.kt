package ru.polescanner.ksp

/*
import ru.polescanner.ksp.UserDTO.AuthorDTO
import com.example.app.domain.Author
import com.example.app.domain.IMapper
import com.example.app.domain.User
import java.util.*

data class UserDTO(val id: String, val author: AuthorDTO) {
    @MapTo
    object Mapper: IMapper<User, UserDTO> {
        override fun mapTo(i: User) = UserDTO(i.id.toString(), i.author.toDTO())

        override fun mapFrom(o: UserDTO) = User(UUID.fromString(o.id), o.author.toDomain())
    }
    data class AuthorDTO(val id: String, val name: String, val surname: String) {
        @MapTo
        object Mapper: IMapper<Author, AuthorDTO> {
            override fun mapTo(i: Author) = AuthorDTO(i.id.toString(), i.name, i.surname)

            override fun mapFrom(o: AuthorDTO) = Author(UUID.fromString(o.id), o.name, o.surname)
        }
    }
}

fun User.toDTO(): UserDTO = UserDTO.Mapper.mapTo(this)

fun UserDTO.toDomain(): User = UserDTO.Mapper.mapFrom(this)

fun Author.toDTO(): AuthorDTO = AuthorDTO.Mapper.mapTo(this)

public fun AuthorDTO.toDomain(): Author = AuthorDTO.Mapper.mapFrom(this)*/
