package ru.polescanner.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import junit.framework.TestCase.assertEquals
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.polescanner.ksp.domain.IMapper
import java.io.File


class BuildProcessorTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `mapper`() {
        //val m = IMapper<String, String>
        val kotlinSource = SourceFile.kotlin(
            "UserDTO.kt", """        
            package ru.polescanner.ksp

            import ru.polescanner.ksp.domain.Author
            import ru.polescanner.ksp.domain.IMapper
            import ru.polescanner.ksp.domain.User            
            import java.util.*

            data class UserDTO(val id: String, val author: AuthorDTO) {
                @MapTo
                object Mapper: IMapper<User, UserDTO> {
                    override fun mapTo(i: User) = UserDTO(i.id.toString(), i.author.toDTO1())

                    override fun mapFrom(o: UserDTO) = User(UUID.fromString(o.id), o.author.toDomain1())
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

        val compilationResult = compile(kotlinSource)

        assertEquals(KotlinCompilation.ExitCode.OK, compilationResult.exitCode)
        assertSourceEquals(
            """                              
                package ru.polescanner.ksp

                import ru.polescanner.ksp.domain.Author
                import ru.polescanner.ksp.domain.User

                public fun User.toDTO(): UserDTO = ru.polescanner.ksp.UserDTO.Mapper.mapTo(this)
                
                public fun UserDTO.toDomain(): User = ru.polescanner.ksp.UserDTO.Mapper.mapFrom(this)
                
                public fun Author.toDTO(): UserDTO.AuthorDTO =
                    ru.polescanner.ksp.UserDTO.AuthorDTO.Mapper.mapTo(this)

                public fun UserDTO.AuthorDTO.toDomain(): Author =
                    ru.polescanner.ksp.UserDTO.AuthorDTO.Mapper.mapFrom(this)
                """,
            compilationResult.sourceFor("UserDTOExt.kt")
        )
    }

    private fun compile(vararg source: SourceFile) = KotlinCompilation().apply {
        sources = source.toList()
        symbolProcessorProviders = listOf(BuildProcessorProvider())
        workingDir = temporaryFolder.root
        inheritClassPath = true
        verbose = false
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