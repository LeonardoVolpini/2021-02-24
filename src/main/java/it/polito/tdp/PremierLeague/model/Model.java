package it.polito.tdp.PremierLeague.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private SimpleDirectedWeightedGraph<Player, DefaultWeightedEdge> grafo;
	private PremierLeagueDAO dao;
	private Map<Integer,Player> idMap;
	
	public Model() {
		this.dao= new PremierLeagueDAO();
		this.idMap= new HashMap<>();
		this.dao.listAllPlayers(idMap);
	}
	
	public void creaGrafo(Match m) {
		grafo= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, this.dao.getVertici(m,idMap));
		
		//aggiungo gli archi
		for (Adiacenza a : dao.getAdiacenze(m, idMap)) {
			if (a.getPeso()>=0) //da p1 a p2
				Graphs.addEdgeWithVertices(grafo, a.getP1(), a.getP2(), a.getPeso());
			else //da p2 a p1
				Graphs.addEdgeWithVertices(grafo, a.getP2(), a.getP1(), ((double)-1)*a.getPeso());
		}
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Match> getAllMatches(){
		List<Match> result= dao.listAllMatches();
		Collections.sort(result, new Comparator<Match>() {
			@Override
			public int compare(Match o1, Match o2) {
				return o1.getMatchID().compareTo(o2.matchID);
			}
		});
		return result;
	}
	
	public GiocatoreMigliore getMigliore() {
		if (grafo==null)
			return null;
		Player best=null;
		Double maxDelta=(double)Integer.MIN_VALUE;
		for (Player p :this.grafo.vertexSet()) {
			//calcolo somma pesi archi uscenti:
			double pesoOut=0.0;
			for (DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(p))
				pesoOut += this.grafo.getEdgeWeight(e);
			
			//calcolo somma pesi archi entranti:
			double pesoIn=0.0;
			for (DefaultWeightedEdge e: this.grafo.incomingEdgesOf(p))
				pesoIn += this.grafo.getEdgeWeight(e);
			double delta = pesoOut-pesoIn;
			if (delta>maxDelta) {
				maxDelta=delta;
				best=p;
			}
		}
		return new GiocatoreMigliore(best,maxDelta);
	}

	public Graph<Player,DefaultWeightedEdge> getGrafo() {
		return this.grafo;
	}
	
	
	
}
