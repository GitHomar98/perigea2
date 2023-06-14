package com.ceruti.mongodb.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Item")
public
class Item {

	@Id
	private Long id;
	
	private LocalDate data;
	private String codice;
	private String comune;
	private String provincia;
	private String sigla;
	private int dose1;
	private int dose2;
	private int booster;
	private int richiamo;
	
	
	public Item(Long id, LocalDate data, String codice, String comune, String provincia, String sigla, int dose1,
			int dose2, int booster, int richiamo) {
		super();
		this.id = id;
		this.data = data;
		this.codice = codice;
		this.comune = comune;
		this.provincia = provincia;
		this.sigla = sigla;
		this.dose1 = dose1;
		this.dose2 = dose2;
		this.booster = booster;
		this.richiamo = richiamo;
	}


	public Long getId() {
		return id;
	}


	public LocalDate getData() {
		return data;
	}


	public String getCodice() {
		return codice;
	}


	public String getComune() {
		return comune;
	}


	public String getProvincia() {
		return provincia;
	}


	public String getSigla() {
		return sigla;
	}


	public int getDose1() {
		return dose1;
	}


	public int getDose2() {
		return dose2;
	}


	public int getBooster() {
		return booster;
	}


	public int getRichiamo() {
		return richiamo;
	}
	
	
}
