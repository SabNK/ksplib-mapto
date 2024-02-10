<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Kotlin_Icon.svg/512px-Kotlin_Icon.svg.png" align="right" title="Kotlin Logo" width="120" alt="Kotlin Logo">

# MapTo KSP Library

A library for generating boilerplate code with Mapper to different projection. 
Supports nested Domain primitives with their own Mappers

## Use Cases

- Generate extensions fun compact and lightweignt based on Mapper

## Example

Create sources
```Kotlin
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
```
Generation results succinct (as a human work)
```Kotlin
package ru.polescanner.ksp

import ru.polescanner.ksp.domain.Author
import ru.polescanner.ksp.domain.User
import ru.polescanner.ksp.UserDTO.AuthorDTO

fun User.toDTO(): UserDTO = UserDTO.Mapper.mapTo(this)

fun UserDTO.toDomain(): User = UserDTO.Mapper.mapFrom(this)

fun Author.toDTO(): AuthorDTO = AuthorDTO.Mapper.mapTo(this)

fun AuthorDTO.toDomain(): Author = AuthorDTO.Mapper.mapFrom(this)
```
Generation results verbose (as a result of KSP compile)
```Kotlin
package ru.polescanner.ksp

import ru.polescanner.ksp.domain.Author
import ru.polescanner.ksp.domain.User

public fun User.toDTO(): UserDTO = ru.polescanner.ksp.UserDTO.Mapper.mapTo(this)

public fun UserDTO.toDomain(): User = ru.polescanner.ksp.UserDTO.Mapper.mapFrom(this)

public fun Author.toDTO(): UserDTO.AuthorDTO =
  ru.polescanner.ksp.UserDTO.AuthorDTO.Mapper.mapTo(this)

public fun UserDTO.AuthorDTO.toDomain(): Author =
  ru.polescanner.ksp.UserDTO.AuthorDTO.Mapper.mapFrom(this)
```


## Features
- Supports any extensions that used to distinguish Domain class from Projection class, e.g. DTO, DAO, Db etc. 
Get fun name by comparing two classes say User and UserDTO = toDTO, Author and AuthorDb = toDb. Back is alwais toDomain
- Works with nested classes having Mapper.
- Annotations supported @MapTo. Annotation should be applied to Mapper

## Installation <img src="https://i.imgur.com/iV36acM.png" width="23">

The package is not available on Maven Central, only on Github Packages. So below is not working in the moment!

Add dependency to your module's `build.gradle` file:

```Kotlin
plugins {
  kotlin("jvm") version "1.8.0"
     // ...
  id ("com.google.devtools.ksp") version "1.8.0-1.0.9"    
}

dependencies {
     // ...
  implementation("ru.polescanner.ksp:lib:0.0.3")
  ksp("ru.polescanner.ksp:lib:0.0.3")
}
```
Keep [ksp version](https://github.com/google/ksp/releases) in accordance with your kotlin plugin version

## License

Copyright (C) 2024 Nick Sabinin

Licensed under Apache License 2.0