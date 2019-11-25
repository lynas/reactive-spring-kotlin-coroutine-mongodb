package com.lynas.reactivespringkotlinmongo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.mapping.Document
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
class CustomerService(val operation: MongoOperations) {

    suspend fun insert(customer: Customer): Customer {
        return operation.save(customer)
    }

    suspend fun findAll(): Flow<Customer> {
        return operation.findAll<Customer>().asFlow()
    }
}

@RestController
@RequestMapping("/customers")
class CustomerRestController(val customerService: CustomerService) {

    @GetMapping
    suspend fun getAll(): Flow<Customer> {
        return customerService.findAll()
    }

    @PostMapping
    suspend fun addNew(@RequestBody customer: Customer): Customer {
        return customerService.insert(customer)
    }


}
