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
@Document
data class Order(@Id val id: String? = null, val customerId: String, val orderItemName: String)

@Service
class OrderService(val operation: ReactiveMongoOperations) {
    suspend fun insert(order: Order): Order? {
        return operation.insert(order).awaitSingle()
    }

    suspend fun findByCustomerId(customerId: String): List<Order> {
        return operation.find<Order>(Query(where("customerId").isEqualTo(customerId))).asFlow().toList()
    }
}


@Service
class CustomerService(val operation: ReactiveMongoOperations) {

    suspend fun insert(customer: Customer): Customer? {
        return operation.insert(customer).awaitSingle()
    }

    suspend fun findAll(): List<Customer> {
        return operation.findAll<Customer>().asFlow().toList()
    }

    suspend fun findOneById(customerId: String): Customer {
        return operation.find<Customer>(Query(where("_id").isEqualTo(customerId))).awaitSingle()
    }

    suspend fun findByLastName(lastName: String): Customer? {
        return operation.find<Customer>(Query(where("lastName").isEqualTo(lastName))).awaitSingle()
    }
}

@RestController
@RequestMapping("/customers")
class CustomerRestController(val customerService: CustomerService, val orderService: OrderService) {

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

    @GetMapping("/orders/{customerId}")
    suspend fun getCustomerOrders(@PathVariable customerId: String): List<Order> {
        val customer = customerService.findOneById(customerId)
        return orderService.findByCustomerId(customer.id
                ?: throw RuntimeException("customer not found with customerId: $customerId"))
    }


}
