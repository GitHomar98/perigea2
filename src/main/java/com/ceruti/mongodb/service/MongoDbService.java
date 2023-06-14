package com.ceruti.mongodb.service;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MongoDbService {

    @Value("${data.mongodb.host}")
    private String mongoHost;

    @Value("${data.mongodb.port}")
    private int mongoPort;

    @Value("${data.mongodb.database}")
    private String mongoDatabase = "mySecondDatabase";
    
    @Value("${api.url}")
    private String apiUrl;
    
    private final String api = "https://comuni-ita.herokuapp.com/api/province";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private static final String COLLECTION_NAME = "myCollection";
    Map<String, String> provinceMap = new HashMap<>();
    

	public void connect() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://" + mongoHost + ":" + mongoPort))
                .build();
        mongoClient = MongoClients.create(settings);

        database = mongoClient.getDatabase(mongoDatabase);
    }
	
	public Map<String, String> fetchApi() {
		 try {
			URL url = new URL(api);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			 InputStream inputStream = conn.getInputStream();
			 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			   String line;
	        	StringBuilder response = new StringBuilder();
		        while ((line = bufferedReader.readLine()) != null) {
		            response.append(line);
		        }
		        bufferedReader.close();
		        
		        //manipolazione dell'output
		        JSONArray jsonArray = new JSONArray(response.toString());
		        for (int i = 0; i < jsonArray.length(); i++) {
		            JSONObject obj = jsonArray.getJSONObject(i);
		            provinceMap.put(obj.getString("sigla"), obj.getString("nome"));
		        }
		 } catch (Exception e) {
		        e.printStackTrace();
		    }
		 return provinceMap;
	}
	
	public void fetchAndInsertData() {
	    try {
	        // Scarico dati da API
	        URL url = new URL(apiUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        InputStream inputStream = conn.getInputStream();
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	        StringBuilder response = new StringBuilder();
	        String line;
	        while ((line = bufferedReader.readLine()) != null) {
	            response.append(line);
	        }
	        bufferedReader.close();

	        // Conversione dati in documenti MongoDB + inserimento a DB
	        JSONArray data = new JSONArray(response.toString());
	        provinceMap = fetchApi();
	        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
	        long id = 0;
	        for (int i = 0; i < data.length(); i++) {
	            JSONObject obj = data.getJSONObject(i);
	            String provinciaNome = obj.getString("provincia_dom");
	            String provinciaSigla = null;
	            for (Map.Entry<String, String> entry : provinceMap.entrySet()) {
	                if (entry.getValue().equalsIgnoreCase(provinciaNome)) {
	                    provinciaSigla = entry.getKey();
	                    break;
	                }
	            }
	            Document doc = new Document("id", id)
	                    .append("data", LocalDate.now())
	                    .append("codice", obj.getString("codistat_comune_dom"))	                    
	                    .append("comune", obj.getString("comune_dom"))
	                    .append("provincia", provinciaNome)
	                    .append("sigla", provinciaSigla)
	                    .append("solo_dose1", obj.getInt("tot_solo_dose_1"))
	                    .append("dose_unica", obj.getInt("tot_dose_2_unica"))
	                    .append("dose_richiamo", obj.getInt("tot_dose_richimm_rich2_"))
	                    .append("booster", obj.getInt("tot_dose_addizionale_booster"));
	            collection.insertOne(doc);
	            id++;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName, Document.class);
    }

    public long count() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection.countDocuments();
    }

    public int sumDose1() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        List<Document> results = collection.aggregate(Arrays.asList(new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", "$dose1"))))).into(new ArrayList<>());
        return results.isEmpty() ? 0 : results.get(0).getInteger("total");
    }

    public int sumBooster() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        List<Document> results = collection.aggregate(Arrays.asList(new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", "$booster"))))).into(new ArrayList<>());
        return results.isEmpty() ? 0 : results.get(0).getInteger("total");
    }


    public int sumDose2() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        List<Document> results = collection.aggregate(Arrays.asList(new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", "$dose2"))))).into(new ArrayList<>());
        return results.isEmpty() ? 0 : results.get(0).getInteger("total");
    }

    public Optional<Document> findById(Long id) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Optional<Document> doc = Optional.ofNullable(collection.find(new Document("id", id)).first());
        return doc;
    }

    public List<Document> findAll() {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return (collection.find().into(new ArrayList<>()));
    }

    public List<Document> findByData(LocalDate data) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection.find(new Document("data", data)).into(new ArrayList<>());
    }

    public List<Document> findByComune(String comune) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection.find(new Document("comune", comune)).into(new ArrayList<>());
    }

    public List<Document> findByProvincia(String provincia) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection.find(new Document("provincia", provincia)).into(new ArrayList<>());
    }
    
    public List<Document> findByComuneAndData(String comune, LocalDate data) {
        MongoCollection<Document> collection = getCollection(COLLECTION_NAME);
        BasicDBObject query = new BasicDBObject();
        query.put("comune", comune);
        query.put("data_somministrazione", data);

        FindIterable<Document> cursor = collection.find(query);
        List<Document> results = new ArrayList<>();
        for (Document doc : cursor) {
            results.add(doc);
        }
        return results;
    }
     
    public List<Document> findByProvinciaAndComuneAndData(String provincia, String comune, LocalDate data) {
        MongoCollection<Document> collection = getCollection(COLLECTION_NAME);
        Document query = new Document("provincia", provincia)
                .append("comune", comune)
                .append("data_somministrazione", java.sql.Date.valueOf(data));
        return collection.find(query).into(new ArrayList<>());
    }

    public List<Document> findByProvinciaAndComune(String provincia, String comune) {
        MongoCollection<Document> collection = getCollection(COLLECTION_NAME);
        Document query = new Document("provincia", provincia)
                .append("comune", comune);
        return collection.find(query).into(new ArrayList<>());
    }

    public List<Document> findByProvinciaAndData(String provincia, LocalDate data) {
        MongoCollection<Document> collection = getCollection(COLLECTION_NAME);
        Document query = new Document("provincia", provincia)
                .append("data_somministrazione", java.sql.Date.valueOf(data));
        return collection.find(query).into(new ArrayList<>());
    }
    
    public void closeConnection() {
    	mongoClient.close();
    }
    
}