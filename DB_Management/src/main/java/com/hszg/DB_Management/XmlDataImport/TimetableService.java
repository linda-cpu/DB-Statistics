package com.hszg.DB_Management.XmlDataImport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hszg.DB_Management.DelayReason.API.IDelayReasonRepository;
import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Database.StationEntity;
import com.hszg.DB_Management.TrainLine.Api.ITrainLineRepository;
import com.hszg.DB_Management.TrainLine.Database.LinesPerStationEntity;
import com.hszg.DB_Management.Trip.Annotation.Database.AnnotationEntity;
import com.hszg.DB_Management.Trip.Api.ITripRepository;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;
import com.hszg.DB_Management.XmlDataImport.modelChangeData.ArrivalDeparture;
import com.hszg.DB_Management.XmlDataImport.modelChangeData.Message;
import com.hszg.DB_Management.XmlDataImport.modelChangeData.Stop;
import com.hszg.DB_Management.XmlDataImport.modelChangeData.Timetable;
import com.hszg.DB_Management.XmlDataImport.modelStationInfo.StationInfo;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

/**
 * responsible for transforming/converting data
 */
@Slf4j
@Service
public class TimetableService {
	
	@Value("${xml.import.path}")
	private String xmlPath;

	private final DbTimetableClient apiClient;
	private final ITripRepository tripRepository;
	private final IDelayReasonRepository delayReasonRepository;
	private final IStationRepository stationRepository;
	private final ITrainLineRepository lineRepository;
	
	// DB API Date Format: YYMMDDHHMM
	private static final DateTimeFormatter DB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmm");
	
	
	
	public TimetableService(DbTimetableClient apiClient, ITripRepository tripRepository, 
		IStationRepository stationRepository,
		IDelayReasonRepository delayReasonRepository,
		ITrainLineRepository lineRepository
		) {

		this.apiClient = apiClient;
		this.tripRepository = tripRepository;
		this.delayReasonRepository = delayReasonRepository;
		this.stationRepository = stationRepository;
		this.lineRepository = lineRepository;
	}

	/**
	 * Fetches stations that matches the given pattern
	 * 
	 * @param pattern
	 */
	public StationEntity fetchStationInfo(String pattern) {
		StationEntity dbStation = null;

		StationInfo stationInfo = apiClient.fetchStationInfo(pattern);
		if (stationInfo.getStation().size() == 0) {
			// no station found
			log.warn("No station found using DB-API (deutsche Bahn) with the following pattern: " + pattern);
		} else if (stationInfo.getStation().size() == 1) {
			//found exactly one station matching the pattern
			com.hszg.DB_Management.XmlDataImport.modelStationInfo.Station xmlStation = stationInfo.getStation().get(0);
			try {
				dbStation = new StationEntity(Integer.parseInt(xmlStation.getEva()), xmlStation.getName());
			} catch (NumberFormatException e) {
				log.warn("Invalid station eva format, can not be parsed as Integer: {}", xmlStation.getEva());
			}
		}
		return dbStation;
	}
	
	
	/**
	 * fetches the change data from the Deutsche Bahn API
	 * 
	 * @param evaId
	 * @param date
	 */
	public void downloadAndImportChangeData(String evaId) {

		// Fetch the change data
		log.info("Starting fetching change data from DB-API timetable for EVA ID: {}", evaId);
        Timetable xmlDataChange = apiClient.fetchTimetableChange(evaId);
        log.info("Finished fetching change data from DB-API timetable for EVA ID: {}", evaId);
		convertAndStore(evaId, xmlDataChange);

    }

	/**
	 * fetches the plan data from the Deutsche Bahn API
	 * 
	 * @param evaId
	 * @param date
	 */
	public void downloadAndImportPlanData(String evaId, String date, String hour) {
		// Fetch the plan data
		//TODO: don't just fetch data from one hour
		log.info("Start fetching plan data from DB-API timetable for EVA ID: {}, Date: {}, Hour: {}", evaId, date);

		Timetable xmlDataPlan = apiClient.fetchTimetablePlan(evaId, date, String.valueOf(hour));
		convertAndStore(evaId, xmlDataPlan);

		log.info("Finished fetching and importing plan data timetable for EVA ID: {}, Date: {}, Hour: {}", evaId, date);
	}

	/**
	 * converts and stores the given xml data
	 * 
	 * @param evaId 
	 * @param xmlData
	 * @param isPlanData
	 */
	private void convertAndStore(String evaId, Timetable xmlData) {
		if (evaId == null) {
			String stationName = xmlData.getStation();
			evaId = stationRepository.findByName(stationName)
						.map(station -> String.valueOf(station.getEva()))
                		.orElse(null);
			
			if (evaId == null) {
				log.warn("Could not find EVA ID for station {} in the database.", stationName);
			}
		}

        if (xmlData != null && xmlData.getStops() != null) {
        	for (Stop stopXml : xmlData.getStops()) {
        		TripStopEntity trip = tripRepository.findById(stopXml.getId())
                        .orElse(new TripStopEntity());

				log.debug("got following object from database: " + trip.toString());
        		mapXmlToModel(evaId, xmlData, stopXml, trip);
				log.info("Start storing trip stop with Trip ID: {}", trip.getTripId());
        		tripRepository.save(trip);
				log.info("Finished storing trip stop with Trip ID: {}", trip.getTripId());
        	}
        }
	}
	
	
	/**
	 * mappes the xmls from the DB-API to a database model object
	 * 
	 * @param evaId eva numbet of station
	 * @param root
	 * @param stopXml
	 * @param stopDB
	 * @return
	 */
    private TripStopEntity mapXmlToModel(String evaId, Timetable root, Stop stopXml, TripStopEntity stopDB) {
        log.info("Start mapping XML to TripStop model");

		// don't change trip id if it is already set
		if (stopDB.getId() == null && stopXml.getId() != null) {
			String fullXmlId = stopXml.getId();
			stopDB.setId(fullXmlId);

			// extract trip id
			int lastHyphen = fullXmlId.lastIndexOf('-');
			int targetHyphen = fullXmlId.lastIndexOf('-', lastHyphen - 1);
			String tripId = fullXmlId.substring(0, targetHyphen);
			stopDB.setTripId(tripId);
		}
        
		// station information
		if (stopDB.getStationName() == null && root.getStation() != null) {
			stopDB.setStationName(root.getStation());
		}
		if (stopDB.getStationEva() == 0 && (evaId != null && !evaId.isBlank())) {
			try {
				stopDB.setStationEva(Integer.parseInt(evaId));

			} catch (NumberFormatException e) {
				log.warn("Invalid station eva format, can not be parsed as Integer: {}, try to get eva number from the database", evaId);
				// get eva from db using station name
				if (stopDB.getStationName() != null) {
					StationEntity station = stationRepository.findByName(stopDB.getStationName())
							.orElse(null);
					if (station != null) {
						stopDB.setStationEva(station.getEva());
					}
				}
			}
		}

		boolean arrivalPlannedTimeIsSet = false;
		boolean departurePlannedTimeIsSet = false;

        // arrival information
		if (stopXml.getArrival() != null) {
			ArrivalDeparture xmlArrival = stopXml.getArrival();

			// plan arrival
			if (stopDB.getArrivalPlan() == null && xmlArrival.getPlannedTime() != null) {
				stopDB.setArrivalPlan(parseDBTime(xmlArrival.getPlannedTime()));
				arrivalPlannedTimeIsSet = true;
			}
			// real (changed) arrival
			if (stopDB.getArrivalReal() == null && xmlArrival.getChangedTime() != null) {
				stopDB.setArrivalReal(parseDBTime(xmlArrival.getChangedTime()));
			}
			// plan plattform
			if (stopDB.getPlattformPlan() == 0 && xmlArrival.getPlannedPlattform() != null) {
				try {
					stopDB.setPlattformPlan(Integer.parseInt(xmlArrival.getPlannedPlattform()));
				} catch (NumberFormatException e) {
					log.warn("Invalid station plattform format, can not be parsed as Integer: {}", xmlArrival.getPlannedPlattform());
				}
			}
			// real plattform
			if (stopDB.getPlattformReal() == null && xmlArrival.getChangedPlattform() != null) {
				
				try {
					stopDB.setPlattformReal(Integer.parseInt(xmlArrival.getChangedPlattform()));
				} catch (NumberFormatException e) {
					log.warn("Invalid plattform format, can not be parsed as Integer: {}", xmlArrival.getChangedPlattform());
				}
			}
			if (stopDB.getLine() == null && xmlArrival.getLine() != null) {
				stopDB.setLine(xmlArrival.getLine());
				updateTrainLinePerStation(stopDB.getStationEva(), xmlArrival.getLine());
			}
			if (stopDB.getCancellationTime() == null && xmlArrival.getCancellationTime() != null) {
				stopDB.setCancellationTime(parseDBTime(xmlArrival.getCancellationTime()));
			}
		}

        // departure information
        if (stopXml.getDeparture() != null) {
			ArrivalDeparture xmlDeparture = stopXml.getDeparture();
			
			if (stopDB.getDeparturePlan() == null && xmlDeparture.getPlannedTime() != null) {
				stopDB.setDeparturePlan(parseDBTime(xmlDeparture.getPlannedTime()));
				departurePlannedTimeIsSet = true;
			}
			if (stopDB.getDepartureReal() == null && xmlDeparture.getChangedTime() != null) {
				stopDB.setDepartureReal(parseDBTime(xmlDeparture.getChangedTime()));
			}
			if (stopDB.getPlattformPlan() == 0 && xmlDeparture.getPlannedPlattform() != null) {
				
				try {
					stopDB.setPlattformPlan(Integer.parseInt(xmlDeparture.getPlannedPlattform()));
				} catch (NumberFormatException e) {
					log.warn("Invalid plattform format, can not be parsed as Integer: {}", xmlDeparture.getPlannedPlattform());
				}
			}
			if (stopDB.getPlattformReal() == null && xmlDeparture.getChangedPlattform() != null) {
				try {
					stopDB.setPlattformReal(Integer.parseInt(xmlDeparture.getChangedPlattform()));
				} catch (NumberFormatException e) {
					log.warn("Invalid plattform format, can not be parsed as Integer: {}", xmlDeparture.getChangedPlattform());
				}
			}
			if (stopDB.getLine() == null && xmlDeparture.getLine() != null) {
				stopDB.setLine(xmlDeparture.getLine());
				updateTrainLinePerStation(stopDB.getStationEva(), xmlDeparture.getLine());
			}
			if (stopDB.getCancellationTime() == null && xmlDeparture.getCancellationTime() != null) {
				stopDB.setCancellationTime(parseDBTime(xmlDeparture.getCancellationTime()));
			}
		}

		// if only one of the planned times is set, set the other one as well
		if (arrivalPlannedTimeIsSet && !departurePlannedTimeIsSet) {
			stopDB.setDeparturePlan(stopDB.getArrivalPlan());
		} else if (!arrivalPlannedTimeIsSet && departurePlannedTimeIsSet) {
			stopDB.setArrivalPlan(stopDB.getDeparturePlan());
		}

		// handle annotations/messages

		handleAnnotations(stopDB, stopXml);

        log.info("Finished mapping XML to Timetable model.");

		log.debug("stop object to save: " + stopDB.toString());
        
        return stopDB;
    }

	private void updateTrainLinePerStation(int stationEva, String line) {
		if (line == null || line.isBlank()) {
			return;
		}
		
		lineRepository.findById(stationEva).ifPresentOrElse(

			station -> {
				if (!station.getLines().contains(line)) {
					station.getLines().add(line);
					lineRepository.save(station);
				}
			}, 
			() -> {
				LinesPerStationEntity newStationLines = new LinesPerStationEntity();
				newStationLines.setEva(stationEva);
				newStationLines.addLine(line);
				lineRepository.save(newStationLines);
			});
	}

	private void handleAnnotations(TripStopEntity stopDB, Stop stopXml) {

		// collect all messages in a temporary list
		List<Message> allXmlMessages = new ArrayList<>();
		
		if (stopXml.getMessages() != null) {
			allXmlMessages.addAll(stopXml.getMessages());
		}
		if (stopXml.getArrival() != null && stopXml.getArrival().getMessages() != null) {
			allXmlMessages.addAll(stopXml.getArrival().getMessages());
		}
		if (stopXml.getDeparture() != null && stopXml.getDeparture().getMessages() != null) {
			allXmlMessages.addAll(stopXml.getDeparture().getMessages());
		}

		// 2. Falls Nachrichten vorhanden sind, diese gesammelt verarbeiten
		if (!allXmlMessages.isEmpty()) {
			processXmlMessages(stopDB, allXmlMessages);
		}
	}
    

	private void processXmlMessages(TripStopEntity stopDB, List<Message> xmlMessages) {
		List<AnnotationEntity> dbAnnotations = stopDB.getAnnotations();
		// to prevent duplicates within the xmMessages
		Set<String> processedIdsInThisRun = new HashSet<>();

		for (Message message : xmlMessages) {
			String messageId = message.getId();
			if (messageId == null) continue;

			if (processedIdsInThisRun.contains(messageId)) {
				continue; 
			}
			processedIdsInThisRun.add(messageId);

			boolean idExistsInDb = dbAnnotations.stream()
					.anyMatch(a -> messageId.equals(a.getId()));

			if (!idExistsInDb) {
				int messageCode = 0;

				try {
					messageCode = Integer.parseInt(message.getCode());
				} catch (NumberFormatException e) {
					log.warn("Invalid code format, can not be parsed as Integer: {}", message.getCode());
				}
				
				AnnotationEntity newAnnotation = new AnnotationEntity();
				
				newAnnotation.setId(messageId);
				newAnnotation.setSource("API");
				newAnnotation.setCode(messageCode);
				newAnnotation.setMessageTime(parseDBTime(message.getTimestampCreation()));

				if (message.getContent() == null || message.getContent().isBlank()) {
					delayReasonRepository.findById(messageCode)
							.ifPresent(reason -> newAnnotation.setText(reason.getReason()));
				} else {
					newAnnotation.setText(message.getContent());
				}

				stopDB.addAnnotation(newAnnotation);
			}
		}
	}

	/**
	 * converts the given datetime string to an instant object
	 * @param dbTime
	 * @return
	 */
    private Instant parseDBTime(String dbTime) {
        if (dbTime == null || dbTime.isEmpty()) return null;
        return LocalDateTime.parse(dbTime, DB_DATE_FORMAT)
                            .toInstant(ZoneOffset.UTC);
    }

    /**
     * 
     * @throws IOException 
     * @throws JAXBException if xml could not be mapped or unmashalled
     */
	public void importLocalXmls() throws IOException {
		File folder = new File(xmlPath);
	    File[] listOfFiles = folder.listFiles();

		if (listOfFiles == null) {
			log.error("CRITICAL: The directory {} does not exist inside the container or is not a directory!", xmlPath);
			return; 
		}

		log.info("Found {} files to process.", listOfFiles.length - 2);
	    
	    // needed to convert
	    JAXBContext xmlContext;
	    Unmarshaller jaxbUnmarshaller = null;
	    
		try {
			xmlContext = JAXBContext.newInstance(Timetable.class);
			jaxbUnmarshaller = xmlContext.createUnmarshaller();
		} catch (JAXBException e) {
			log.error("Could not create JAXB Unmarshaller for Timetable import from local XMLs");
			e.printStackTrace();
		}
	     
	    for (File file : listOfFiles) {
			if (file.isDirectory()) {
				continue;
			}
			Timetable xmlObject;

	    	try {
				log.info("Processing file: {}", file.getName());
				xmlObject = (Timetable) jaxbUnmarshaller.unmarshal(file);
				log.info("Successfully converted XML file to Timetable object: " + file.getName());
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				log.error("Could not convert XML file to Timetable object (see folder failed): " + file.getName());
				e.printStackTrace();
				moveFile(file.toPath(), xmlPath + "/failed");
				continue;
			}
			log.info("Start converting data from file: {}", file.getName());
			convertAndStore(null, xmlObject);
			log.info("Finished converting and storing data from file: {}", file.getName());
			Files.delete(file.toPath());
	    }
	}

	/**
	 * moves a file to the given target folder
	 * @param sourcePath
	 * @param targetDir
	 */
	private void moveFile(Path sourcePath, String targetDir) {
		try {
			Path targetFolder = Paths.get(targetDir);
			// Ensure the subfolder exists (finished/ or failed/)
			if (Files.notExists(targetFolder)) {
				Files.createDirectories(targetFolder);
			}
			
			// Construct the final target path: /app/data/xml/finished/filename.xml
			Path targetPath = targetFolder.resolve(sourcePath.getFileName());
			
			// Move the file (replaces if a file with the same name already exists in target)
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("Could not move file {}: {}", sourcePath.getFileName(), e.getMessage());
		}
	}
}