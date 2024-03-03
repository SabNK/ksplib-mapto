package ru.polescanner.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import junit.framework.TestCase.assertEquals
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class BuildProcessorTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `mapper should generate file`() {
        //val m = IMapper<String, String>
        val userDTOSource = SourceFile.kotlin(
            "UserDTO.kt", """        
            package ru.polescanner.ksp

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

        """
        )
        val bookDTOSource = SourceFile.kotlin(
            "BookDTO.kt", """        
            package ru.polescanner.books

            import com.example.app.domain.books.Book
            import com.example.app.domain.IMapper
            import ru.polescanner.ksp.MapTo

            data class BookDTO(val isbn: String, val name: String, val pages: Int) {
                @MapTo object Mapper: IMapper<Book, BookDTO> {
                    override fun mapTo(i: Book) = BookDTO(i.isbn, i.name, i.pages)

                    override fun mapFrom(o: BookDTO) = Book(o.isbn, o.name, o.pages)
                }                
            }

    """
        )
        val compilationResult = compile(userDTOSource, bookDTOSource)

        assertEquals(KotlinCompilation.ExitCode.OK, compilationResult.exitCode)
        assertSourceEquals(
            """                              
                package ru.polescanner.ksp

                import com.example.app.domain.Author
                import com.example.app.domain.User
                import kotlin.Array
                import kotlin.collections.List

                public fun User.toDTO(): UserDTO = ru.polescanner.ksp.UserDTO.Mapper.mapTo(this)
                
                public fun UserDTO.toDomain(): User = ru.polescanner.ksp.UserDTO.Mapper.mapFrom(this)

                public fun List<User>.toDTO(): List<UserDTO> = this.map{ it.toDTO() }

                public fun List<UserDTO>.toDomain(): List<User> = this.map{ it.toDomain() }

                public fun Array<User>.toDTO(): Array<UserDTO> = this.map{ it.toDTO() }.toTypedArray()
                
                public fun Array<UserDTO>.toDomain(): Array<User> = this.map{ it.toDomain() }.toTypedArray()
                
                public fun Author.toDTO(): UserDTO.AuthorDTO =
                    ru.polescanner.ksp.UserDTO.AuthorDTO.Mapper.mapTo(this)

                public fun UserDTO.AuthorDTO.toDomain(): Author =
                    ru.polescanner.ksp.UserDTO.AuthorDTO.Mapper.mapFrom(this)

                public fun List<Author>.toDTO(): List<UserDTO.AuthorDTO> = this.map{ it.toDTO() }
                
                public fun List<UserDTO.AuthorDTO>.toDomain(): List<Author> = this.map{ it.toDomain() }
                
                public fun Array<Author>.toDTO(): Array<UserDTO.AuthorDTO> = this.map{ it.toDTO() }.toTypedArray()
                
                public fun Array<UserDTO.AuthorDTO>.toDomain(): Array<Author> = this.map{ it.toDomain()
                    }.toTypedArray()
                """,
            compilationResult.sourceFor("UserDTOExt.kt")
        )
        assertSourceEquals(
            """                              
                package ru.polescanner.books

                import com.example.app.domain.books.Book
                import kotlin.Array
                import kotlin.collections.List

                public fun Book.toDTO(): BookDTO = ru.polescanner.books.BookDTO.Mapper.mapTo(this)
                
                public fun BookDTO.toDomain(): Book = ru.polescanner.books.BookDTO.Mapper.mapFrom(this)
                
                public fun List<Book>.toDTO(): List<BookDTO> = this.map{ it.toDTO() }

                public fun List<BookDTO>.toDomain(): List<Book> = this.map{ it.toDomain() }
                
                public fun Array<Book>.toDTO(): Array<BookDTO> = this.map{ it.toDTO() }.toTypedArray()
                
                public fun Array<BookDTO>.toDomain(): Array<Book> = this.map{ it.toDomain() }.toTypedArray()
                """,
            compilationResult.sourceFor("BookDTOExt.kt")
        )
    }

    private fun compile(vararg source: SourceFile) = KotlinCompilation().apply {
        sources = source.toList()
        symbolProcessorProviders = listOf(BuildProcessorProvider())
        workingDir = temporaryFolder.root
        inheritClassPath = true
        verbose = true
    }.compile()


    private fun assertSourceEquals(@Language("kotlin") expected: String, actual: String) {
        assertEquals(
            expected.trimIndent(),
            // unfortunate hack needed as we cannot enter expected text with tabs rather than spaces
            actual.trimIndent().replace("\t", "    ")
        )
    }

    private fun KotlinCompilation.Result.sourceFor(fileName: String): String {
        return kspGeneratedSources().find { it.name == fileName }
            ?.readText()
            ?: throw IllegalArgumentException("Could not find file $fileName in ${kspGeneratedSources()}")
    }

    private fun KotlinCompilation.Result.kspGeneratedSources(): List<File> {
        val kspWorkingDir = workingDir.resolve("ksp")
        val kspGeneratedDir = kspWorkingDir.resolve("sources")
        val kotlinGeneratedDir = kspGeneratedDir.resolve("kotlin")
        val javaGeneratedDir = kspGeneratedDir.resolve("java")
        return kotlinGeneratedDir.walk().toList() +
                javaGeneratedDir.walk().toList()
    }

    private val KotlinCompilation.Result.workingDir: File
        get() = checkNotNull(outputDirectory.parentFile)
}