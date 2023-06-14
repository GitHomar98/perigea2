package com.ceruti.mongodb.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ceruti.mongodb.service.MongoDbService;

@RestController
public class DataAggregatorController {

	@Autowired
	private MongoDbService mongoDbService;
	
	//da dove inizia il microservizio, usi questa annotazione
	@PostConstruct
	public void init() {
		mongoDbService.connect();
		System.out.println("Connessione a MongoDB eseguita correttamente.");
		mongoDbService.fetchAndInsertData();
		System.out.println("Dati scaricati e inseriti correttamente.");
	}

	@GetMapping("/dataaggregator")
	public ResponseEntity<List<Document>> getDataAggregated(
			@RequestParam(required = false, value= "provincia") Optional<String> provincia,
			@RequestParam(required = false, value= "comune") Optional<String> comune,
			@RequestParam(required = false, value= "data") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> data) {
		
		List<Document> outputDataList;
		
		
		if(provincia.isPresent() && comune.isPresent() && data.isPresent()) {
			outputDataList = mongoDbService.findByProvinciaAndComuneAndData(provincia.get(), comune.get(), data.get());
		}
		else if(provincia.isPresent() && comune.isPresent()) {
			outputDataList = mongoDbService.findByProvinciaAndComune(provincia.get(), comune.get());
		}
		else if(provincia.isPresent() && data.isPresent()) {
			outputDataList = mongoDbService.findByProvinciaAndData(provincia.get(), data.get());
		}
		else if(comune.isPresent() && data.isPresent()) {
			outputDataList = mongoDbService.findByComuneAndData(comune.get(), data.get());
		}
		else if(provincia.isPresent()) {
			outputDataList = mongoDbService.findByProvincia(provincia.get());
		}
		else if(comune.isPresent()) {
			outputDataList = mongoDbService.findByComune(comune.get());
		}
		else if(data.isPresent()) {
			outputDataList = mongoDbService.findByData(data.get());
		}
		else {
			outputDataList = mongoDbService.findAll();
		}
		
		if(outputDataList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Document>>(outputDataList, HttpStatus.OK);
	}
	
	@GetMapping("/dataaggregator/{id}")
	public ResponseEntity<Document> getDataAggregatedById(@PathVariable("id") Long id) {
		Optional<Document> outputData = mongoDbService.findById(id);
		if(outputData.isPresent()) {
			return new ResponseEntity<>(outputData.get(), HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/dataaggregator/count")
	public ResponseEntity<Long> getCount() {
		Long count = mongoDbService.count();
		return new ResponseEntity<>(count, HttpStatus.OK);
	}
	
	@GetMapping("/dataaggregator/sumdose1")
	public ResponseEntity<Integer> getSumDose1() {
		Integer sum = mongoDbService.sumDose1();
		return new ResponseEntity<>(sum, HttpStatus.OK);
	}
	
	@GetMapping("/dataaggregator/sumdose2")
	public ResponseEntity<Integer> getSumDose2() {
		Integer sum = mongoDbService.sumDose2();
		return new ResponseEntity<>(sum, HttpStatus.OK);
	}
	
	@GetMapping("/dataaggregator/sumbooster")
	public ResponseEntity<Integer> getSumBooster() {
		Integer sum = mongoDbService.sumBooster();
		return new ResponseEntity<>(sum, HttpStatus.OK);
	}
	
	@PreDestroy
	public void disconnect() {
		mongoDbService.closeConnection();
		System.out.println("Connessione a MongoDB chiusa correttamente.");
	}
}