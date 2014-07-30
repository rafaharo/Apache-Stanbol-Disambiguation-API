package mpi.aidalight.service.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aidalight.Settings;
import mpi.aidalight.entitycoherence.GraphSettings;
import mpi.aidalight.entitycoherence.GraphSettingsExperiment;
import mpi.aidalight.entitycoherence.NED;

@Path("/service")
public class RequestProcessor {

	@Path("/example")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String example() {
		// check whether disambiguation works
		String text = "With United, Beckham won the Premier League title 6 times";

		GraphSettings gSettings = new GraphSettingsExperiment();
		NED disambiguator = new NED(new Settings(), gSettings);

		// define settings for disambiguation
		NED.fullSettings = true;
		NED.updateLocalSimByChosenEntities = true;
		NED.decreaseRelatednessbyMentionDistance = true;
		NED.entityEntityRelatedness = true;
		NED.expandRelatedNessConstantbyGraph = true;

		Map<Mention, Entity> results = new HashMap<Mention, Entity>();

		String toReturn = "Text to Disambiguate: " + text + "\n";

		try {
			results = disambiguator.disambiguate(text, null);
		} catch (Exception e) {
			return "Cannot Disambiguate Text.....";
		}

		toReturn += "Results:\n";

		Set<Entry<Mention, Entity>> it = results.entrySet();
		for (Entry<Mention, Entity> entry : it) {
			toReturn += entry.getKey().toString() + ": "
					+ entry.getValue().toString();
		}

		return toReturn;
	}

	@Path("/disambiguate-default")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response Process(DisambiguationData disambiguationData) {

		// get mentions annotations from the request and create mentions for
		// aida
		List<MentionAnnotation> mentionAnnotations = disambiguationData.mentionAnnotations;
		List<Mention> mentions = new ArrayList<>();

		for (MentionAnnotation mentionAnnotation : mentionAnnotations) {
			Mention mention = new Mention();
			mention.setMention(mentionAnnotation.textAnnotation);
			mention.setStartToken(mentionAnnotation.start);
			mention.setEndToken(mentionAnnotation.end);
			
			mentions.add(mention);
		}
		
		// define settings for disambiguation
		GraphSettings gSettings = new GraphSettingsExperiment();
		NED disambiguator = new NED(new Settings(), gSettings);

		// define settings for disambiguation
		NED.fullSettings = true;
		NED.updateLocalSimByChosenEntities = true;
		NED.decreaseRelatednessbyMentionDistance = true;
		NED.entityEntityRelatedness = true;
		NED.expandRelatedNessConstantbyGraph = true;

		Map<Mention, Entity> results = new HashMap<Mention, Entity>();

		//Disambiguate mentions
		try {
			results = disambiguator.disambiguate(disambiguationData.text, null);
		} catch (Exception e) {
			return Response.serverError().build();
		}
		
		List<DisambiguationResult> disambiguationResults = new ArrayList<>();
		
		Set<Entry<Mention, Entity>> it = results.entrySet();
		for (Entry<Mention, Entity> entry : it) {
			DisambiguationResult disambiguationResult = new DisambiguationResult();
			disambiguationResult.mention = entry.getKey().getMention();
			disambiguationResult.entity = entry.getValue().getName();
			disambiguationResult.disambiguationConfidence = entry.getKey().getDisambiguationConfidence();
			
			disambiguationResults.add(disambiguationResult);
		}
		

		GenericEntity<List<DisambiguationResult>> entity = new GenericEntity<List<DisambiguationResult>>(
				disambiguationResults) {
		};
		return Response.ok(entity).build();
	}

}