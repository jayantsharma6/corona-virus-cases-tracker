package com.example.demo.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.models.LocationStats;

@Service
public class CoronaVirusDataService {

	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	
	private List<LocationStats> allStats = new ArrayList<>();
	
	public List<LocationStats> getAllStats() {
		return allStats;
	}

	@PostConstruct
	@Scheduled(cron = "* * * * * *")
	public void fetchVirusData() throws IOException, InterruptedException {
		List<LocationStats> newStats = new ArrayList<>();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(VIRUS_DATA_URL))
			.build();
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		StringReader csvBodyReader = new StringReader(httpResponse.body());
		
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locatoinStat = new LocationStats();
			locatoinStat.setState(record.get("Province/State"));
			locatoinStat.setCountry(record.get("Country/Region"));
			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			locatoinStat.setLatestTotalCases(latestCases);
			locatoinStat.setDiffFromPrevDay(latestCases - prevDayCases);
			newStats.add(locatoinStat);
			
		 }
		this.allStats = newStats;
	}
}
