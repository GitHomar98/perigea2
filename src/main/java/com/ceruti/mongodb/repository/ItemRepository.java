package com.ceruti.mongodb.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ceruti.mongodb.model.Item;

public interface ItemRepository extends MongoRepository<Item, Long> {

    @Query("{comune:'?0'}")
    Item findItemByComune(String comune);
    
    @Query(value="{comune:'?0'}", fields="{'provincia' : 1, 'sigla' : 1}")
    List<Item> findAll(String comune);
	
}
