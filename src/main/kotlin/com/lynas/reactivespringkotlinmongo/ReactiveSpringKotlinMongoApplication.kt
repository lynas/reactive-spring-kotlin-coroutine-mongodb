package com.lynas.reactivespringkotlinmongo

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*


@SpringBootApplication
class ReactiveSpringKotlinMongoApplication

fun main(args: Array<String>) {
    runApplication<ReactiveSpringKotlinMongoApplication>(*args)
}


@Document
data class Customer(@Id val id: String? = null, val firstName: String, val lastName: String)

@Service
class CustomerService(val operation: ReactiveMongoOperations) {

    suspend fun insert(customer: Customer): Customer? {
        return operation.insert(customer).awaitSingle()
    }

    suspend fun findAll(): List<Customer> {
        return operation.findAll<Customer>().asFlow().toList()
    }

    suspend fun findByLastName(lastName: String): Customer? {
        return operation.find<Customer>(Query(where("lastName").isEqualTo(lastName))).awaitSingle()

    }
}

@RestController
@RequestMapping("/customers")
class CustomerRestController(val customerService: CustomerService) {

    @GetMapping
    suspend fun getAll(): List<Customer> {
        return customerService.findAll()
    }

    @GetMapping("/{lastName}")
    suspend fun getByLastName(@PathVariable lastName: String): Customer? {
        return customerService.findByLastName(lastName)
    }

    @PostMapping
    suspend fun addNew(@RequestBody customer: Customer): Customer? {
        return customerService.insert(customer)
    }


}
