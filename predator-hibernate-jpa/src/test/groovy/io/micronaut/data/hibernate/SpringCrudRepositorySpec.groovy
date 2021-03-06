/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.hibernate

import io.micronaut.context.annotation.Property
import io.micronaut.data.hibernate.spring.SpringCrudRepository
import io.micronaut.data.tck.entities.Person
import io.micronaut.test.annotation.MicronautTest
import org.springframework.data.domain.PageRequest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest(rollback = false, packages = "io.micronaut.data.tck.entities")
@Property(name = "datasources.default.name", value = "mydb")
@Property(name = 'jpa.default.properties.hibernate.hbm2ddl.auto', value = 'create-drop')
@Stepwise
class SpringCrudRepositorySpec extends Specification {
    @Inject
    @Shared
    SpringCrudRepository crudRepository

    def setupSpec() {
        crudRepository.saveAll([
                new Person(name: "Jeff"),
                new Person(name: "James")
        ])
    }

    void "test save one"() {
        when:"one is saved"
        def person = new Person(name: "Fred")
        crudRepository.save(person)

        then:"the instance is persisted"
        person.id != null
        crudRepository.findById(person.id).isPresent()
        crudRepository.get(person.id).name == 'Fred'
        crudRepository.existsById(person.id)
        crudRepository.count() == 3
        crudRepository.count("Fred") == 1
        crudRepository.findAll().size() == 3
        crudRepository.listPeople("Fred").size() == 1
    }

    void "test save many"() {
        when:"many are saved"
        def p1 = new Person(name: "Frank")
        def p2 = new Person(name: "Bob")
        def people = [p1, p2]
        crudRepository.saveAll(people)

        then:"all are saved"
        people.every { it.id != null }
        people.every { crudRepository.findById(it.id).isPresent() }
        crudRepository.findAll().size() == 5
        crudRepository.count() == 5
        crudRepository.count("Fred") == 1
        !crudRepository.queryAll(PageRequest.of(0, 1)).isEmpty()
        crudRepository.list(PageRequest.of(1, 10)).isEmpty()
        crudRepository.list(PageRequest.of(0, 1)).size() == 1
    }

    void "test delete by id"() {
        when:"an entity is retrieved"
        def person = crudRepository.findByName("Frank")

        then:"the person is not null"
        person != null
        person.name == 'Frank'
        crudRepository.queryByName("Frank").name == person.name
        crudRepository.findById(person.id).isPresent()

        when:"the person is deleted"
        crudRepository.deleteById(person.id)

        then:"They are really deleted"
        !crudRepository.findById(person.id).isPresent()
        crudRepository.count() == 4
    }

    void "test delete by multiple ids"() {
        when:"A search for some people"
        def people = crudRepository.findByNameLike("J%")

        then:
        people.size() == 2

        when:"the people are deleted"
        crudRepository.deleteAll(people)

        then:"Only the correct people are deleted"
        people.every { !crudRepository.findById(it.id).isPresent() }
        crudRepository.count() == 2
    }

    void "test delete one"() {
        when:"A specific person is found and deleted"
        def bob = crudRepository.findByName("Bob")

        then:"The person is present"
        bob != null

        when:"The person is deleted"
        crudRepository.delete(bob)

        then:"They are deleted"
        !crudRepository.findById(bob.id).isPresent()
        crudRepository.count() == 1
    }

    void "test update one"() {
        when:"A person is retrieved"
        def fred = crudRepository.findByName("Fred")

        then:"The person is present"
        fred != null

        when:"The person is updated"
        crudRepository.updatePerson(fred.id, "Jack")

        then:"the person is updated"
        crudRepository.findByName("Fred") == null
        crudRepository.findByName("Jack") != null
    }

    void "test delete all"() {
        when:"everything is deleted"
        crudRepository.deleteAll()

        then:"data is gone"
        crudRepository.count() == 0
    }

}
