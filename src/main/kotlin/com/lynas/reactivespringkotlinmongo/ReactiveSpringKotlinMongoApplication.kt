package com.lynas.reactivespringkotlinmongo

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@SpringBootApplication
class ReactiveSpringKotlinMongoApplication {

    @Bean
    fun init(operations: ReactiveMongoOperations) = CommandLineRunner{
        runBlocking {
            operations.insert(Book(name = "book1",authorId = "1"))
                    .thenMany(operations.insert(Book(name = "book2",authorId = "1")))
                    .thenMany(operations.insert(Book(name = "book3",authorId = "2")))
                    .thenMany(operations.insert(Author(id = "1",name = "author1")))
                    .thenMany(operations.insert(Author(id = "2",name = "author2")))
                    .subscribe(System.out::println)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ReactiveSpringKotlinMongoApplication>(*args)
}


@Document
data class Author(@Id val id: String? = null, val name: String)

@Document
data class Book(@Id val id: String? = null, val name: String, val authorId: String)

@Service
class BookService(val operations: ReactiveMongoOperations) {
    suspend fun insert(book: Book): Book {
        return operations.insert(book).awaitSingle()
    }

    suspend fun findByAuthorId(authorId: String): List<Book> {
        return operations.find<Book>(Query(where("authorId").isEqualTo(authorId))).asFlow().toList()
    }

    suspend fun findAll(): List<Book> {
        return operations.findAll<Book>().asFlow().toList()
    }
}


@Service
class AuthorService(val operations: ReactiveMongoOperations) {

    suspend fun findByName(name: String): Author? {
        return operations.find<Author>(Query(where("name").isEqualTo(name)))
                .awaitFirstOrElse {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found with name: $name")
                }
    }
}

@RestController
@RequestMapping("/books")
class BookRestController(val bookService: BookService, val authorService: AuthorService) {

    @GetMapping
    suspend fun getAll(): List<Book> {
        return bookService.findAll()
    }

    @PostMapping
    suspend fun addNew(@RequestBody book: Book): Book {
        return bookService.insert(book)
    }

    @GetMapping("/author-name/{authorName}")
    suspend fun getBooksByAuthorName(@PathVariable authorName: String): List<Book> {
        val authorId = authorService.findByName(authorName)?.id
                ?: throw RuntimeException("Author not found with name $authorName")
        return bookService.findByAuthorId(authorId)
    }

}
