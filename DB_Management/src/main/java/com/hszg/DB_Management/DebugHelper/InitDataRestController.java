package com.hszg.DB_Management.DebugHelper;

import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hszg.DB_Management.TrainLine.Api.ITrainLineRepository;
import com.hszg.DB_Management.Trip.Api.ITripRepository;
import com.hszg.DB_Management.XmlDataImport.TimetableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
public class InitDataRestController {
	
	private TimetableService timetableService;
	private ITrainLineRepository lineRepository;
	private ITripRepository tripRepository;
	

	public InitDataRestController(TimetableService timetableService, ITrainLineRepository lineRepository, ITripRepository tripRepository) {
		this.timetableService = timetableService;
		this.lineRepository = lineRepository;
		this.tripRepository = tripRepository;
	}
	
	@Operation(summary = "Trigger local XML import", description = "Manually starts the process of importing XML files from the configured local directory")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Import process triggered successfully",
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@PostMapping("/import/xmls")
	public void importXmls() throws IOException {
		timetableService.importLocalXmls();
	}



	/*@PostMapping("stations/{stationEva}/init/lines")
	public void fillLinesPerStation(@PathVariable int stationEva) {
		List<String> distinctLines = tripRepository.findDistinctLinesByStationEva(stationEva);

		// 2. Update the station once
		lineRepository.findById(stationEva).ifPresent(station -> {
			station.getLines().addAll(distinctLines);
			// Ensure uniqueness if you use a Set instead of a List in your Entity
			lineRepository.save(station);
		});
	}*/
}
