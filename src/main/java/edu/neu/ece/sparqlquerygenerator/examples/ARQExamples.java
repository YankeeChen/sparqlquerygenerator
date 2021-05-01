package edu.neu.ece.sparqlquerygenerator.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.vocabulary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.utility.FileUtil;

public class ARQExamples {
	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	Random ran = new Random(0);
	double conjunctionGraphPatternProbability = 0.6;
	double unionGraphPatternProbability = 0.15;
	double optionalGraphPatternProbability = 0;
	

	public void buildQueryBySyntax1() {
		/*
		   PREFIX  dc:   <http://purl.org/dc/elements/1.1/>
		   SELECT  ?title
		   WHERE
		      { ?x  dc:title  ?title
		  	    FILTER regex(?title, "sparql", "i")
		  	  }
		 */
		Query query = QueryFactory.make();
		query.setQuerySelectType();

		ElementGroup elg = new ElementGroup();

		Var varTitle = Var.alloc("title");
		Var varX = Var.alloc("x");

		Triple t1 = new Triple(varX, DC.title.asNode(), varTitle);
		elg.addTriplePattern(t1);

		// Adds a filter. Need to wrap variable in a NodeVar.
		Expr expr = new E_Regex(new ExprVar(varTitle), "sparql", "i");
		ElementFilter filter = new ElementFilter(expr);
		elg.addElementFilter(filter);

		// Attach the group to query.
		query.setQueryPattern(elg);

		// Choose what we want - SELECT ?title
		query.addResultVar(varTitle);

		// Print query with line numbers
		// Prefix mapping just helps serialization
		query.getPrefixMapping().setNsPrefix("dc", DC.getURI());
		query.serialize(new IndentedWriter(System.out, false));
		System.out.println();
		
	}
	
	public void buildQueryBySyntax2() {
		/*
		   PREFIX  dc:   <http://purl.org/dc/elements/1.1/>

		   SELECT  ?title
		   WHERE
			 { ?x  dc:title        ?title ;
			       dc:description  ?desc
			 }
		 */
        Query query = QueryFactory.make() ;

        query.setQuerySelectType() ;
        
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;
        
        Var varTitle = Var.alloc("title") ;
        Var varX = Var.alloc("x") ;
        
        Triple t1 = new Triple(varX, DC.title.asNode(),  varTitle) ;
        elg.addTriplePattern(t1) ;
        //elg.addTriplePattern(new Triple(varX, DC.title.asNode(),  varTitle)) ;
        // Don't use bNodes for anon variables.  The conversion is done in parsing.
        // BNodes here are assumed to be values from the target graph.
        Triple t2 = new Triple(varX, DC.description.asNode(), Var.alloc("desc")) ;
        elg.addTriplePattern(t2) ;
        
        // Attach the group to query.  
        query.setQueryPattern(elg) ;

        // Choose what we want - SELECT *
        //query.setQueryResultStar(true) ;
        query.addResultVar(varTitle) ;
       
        // Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        //System.out.println(op) ;
        query = OpAsQuery.asQuery(op);
        // Print query with line numbers
        // Prefix mapping just helps serialization
        query.getPrefixMapping().setNsPrefix("dc", DC.getURI()) ;
        /*
        Map<String, String> prefixMapper = new HashMap<>();
        prefixMapper.put("dc", DC.getURI());
        prefixMapper.put("saref", "https://w3id.org/saref#");
        query.getPrefixMapping().setNsPrefixes(prefixMapper);
        */
       
        query.serialize(new IndentedWriter(System.out,false)) ;
        System.out.println() ;	
	}

	public void buildQueryBySyntax3() {
		/*
		   PREFIX  SDR:  <http://ece.neu.edu/ontologies/SDR.owl#>
		   PREFIX  RFDevice: <http://ece.neu.edu/ontologies/RFDevice.owl#>

		   SELECT DISTINCT  ?USRPB200-210_0
		   WHERE
			 { ?USRPB200-210_0
			             a                     SDR:USRPB200-210 ;
			             RFDevice:hasWeight    SDR:Weight_350g
			 }
		 */
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;
        
        Var varSub1 = Var.alloc("USRPB200-210_0") ;
            
        Triple t1 = new Triple(varSub1, RDF.Nodes.type, NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#USRPB200-210"));
        ElementGroup elgSubSub1 = new ElementGroup();
        elgSubSub1.addTriplePattern(new Triple(varSub1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWeight"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Weight_350g")));
        
        ElementGroup elgSub1 = new ElementGroup() ;
        elgSub1.addTriplePattern(t1) ;	
        elgSub1.addElement(elgSubSub1) ;
        
        elg.addElement(elgSub1);
        Query query = QueryFactory.make() ;
        query.setQuerySelectType() ;
        query.setQueryPattern(elg) ;
        query.setDistinct(true);
        query.addResultVar(varSub1) ;
        //query.setResultVars();
        
        // Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        //System.out.println(op) ;
        query = OpAsQuery.asQuery(op);
        
        
        Map<String, String> prefixMapper = new HashMap<>();
        prefixMapper.put("SDR", "http://ece.neu.edu/ontologies/SDR.owl#");
        prefixMapper.put("RFDevice", "http://ece.neu.edu/ontologies/RFDevice.owl#");
        //prefixMapper.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        query.getPrefixMapping().setNsPrefixes(prefixMapper);
        query.serialize(new IndentedWriter(System.out,false), Syntax.syntaxSPARQL_11) ;
        System.out.println() ;	
        //dumpIntoFile(query, 1);
	}

	public void buildQueryBySyntax4() {
		/*
			PREFIX  SDR:  <http://ece.neu.edu/ontologies/SDR.owl#>
			PREFIX  RFDevice: <http://ece.neu.edu/ontologies/RFDevice.owl#>
			PREFIX  Nuvio: <http://cogradio.org/ont/Nuvio.owl#>

			SELECT DISTINCT  ?USRPB200210_0
			WHERE
			  { ?USRPB200210_0
			              a                     SDR:USRPB200-210
			    { ?USRPB200210_0
			                RFDevice:hasWeight  SDR:Weight_350g
			      { ?USRPB200210_0
			                  Nuvio:aggregateOf  ?Var0
			          { ?Var0  a                     RFDevice:DAC ;
			                   RFDevice:hasResolution  SDR:Data_12bit
			          }
			        UNION
			          { ?Var0  RFDevice:hasDoubleValue  ?DataValue0
			            FILTER ( ?DataValue0 < 2 )
			          }
			      }
			    }
			  }
		*/
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;       
        Var var1 = Var.alloc("USRPB200210_0") ;       
        Triple t1 = new Triple(var1, RDF.Nodes.type, NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#USRPB200-210"));
        elg.addTriplePattern(t1);
        ElementGroup elgSub1 = new ElementGroup();
        
        elgSub1.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWeight"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Weight_350g")));
        
        ElementGroup elgSub2 = new ElementGroup();         
        ElementGroup elgSubSub1 = new ElementGroup();
        
        Var varSubSub1 = Var.alloc("Var0");        
        ElementGroup elgSubSubSub1 = new ElementGroup();
        
        //Var varSubSubSub1 = Var.alloc("DAC_0");
        Triple t2 = new Triple(varSubSub1, RDF.Nodes.type, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#DAC"));
        elgSubSubSub1.addTriplePattern(t2);
       
        ElementGroup elgSubSubSub2 = new ElementGroup();
               
        elgSubSubSub2.addTriplePattern(new Triple(varSubSub1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasResolution"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Data_12bit")));
        
        ElementGroup elgSubSubSub3 = new ElementGroup();
        Var varSubSubSub3 = Var.alloc("DataValue0");
        elgSubSubSub3.addTriplePattern(new Triple(varSubSub1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasDoubleValue"), varSubSubSub3));
        elgSubSubSub3.addElementFilter(new ElementFilter(new E_LessThan(new ExprVar(varSubSubSub3), NodeValue.makeNodeInteger(2))));
           
        ElementGroup g = new ElementGroup();
        g.addElement(elgSubSubSub1);
        g.addElement(elgSubSubSub2);
        ElementUnion e = new ElementUnion(g);
        e.addElement(elgSubSubSub3);
        elgSubSub1.addElement(e);     
        
        /*
        elgSubSub1.addElement(elgSubSubSub1);    
        elgSubSub1.addElement(new ElementMinus(elgSubSubSub2));
        */
        
        //elgSubSub1.addElement(elgSubSubSub1);    
        //elgSubSub1.addElement(new ElementFilter(elgSubSubSub2));
        
        Triple t3 = new Triple(var1, NodeFactory.createURI("http://cogradio.org/ont/Nuvio.owl#aggregateOf"), varSubSub1);
        elgSub2.addTriplePattern(t3);
        elgSub2.addElement(elgSubSub1);
        
        elgSub1.addElement(elgSub2);
        elg.addElement(elgSub1);
        //elg.addElementFilter(new ElementFilter(new E_NotExists(elgSub1)));
        //elgSub1.addElement(elgSub2);
        //elg.addElementFilter(new ElementFilter(new E_NotExists(elgSub2)));
        //elgSub1.addElement(elgSub2);
        //elg.addElementFilter(new ElementFilter(new E_NotExists(elgSub1)));
        //elg.addElementFilter(new ElementFilter(new E_Exists(elgSub1)));
        //elg.addElement(new ElementFilter(new E_NotExists(elgSub2)));
        
        Query query = QueryFactory.make() ;
        query.setQuerySelectType() ;
        query.setQueryPattern(elg) ;
        query.setDistinct(true);
        query.addResultVar(var1) ;
       
        //Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        //System.out.println(op.getClass().getName());
        query = OpAsQuery.asQuery(op);
        
        
        
        Map<String, String> prefixMapper = new HashMap<>();
        prefixMapper.put("SDR", "http://ece.neu.edu/ontologies/SDR.owl#");
        prefixMapper.put("RFDevice", "http://ece.neu.edu/ontologies/RFDevice.owl#");
        prefixMapper.put("Nuvio", "http://cogradio.org/ont/Nuvio.owl#");
           
        //prefixMapper.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        query.getPrefixMapping().setNsPrefixes(prefixMapper);
        query.serialize(new IndentedWriter(System.out,false), Syntax.syntaxSPARQL_11) ;
        System.out.println() ;	
        //dumpIntoFile(query, 1);
	}
	
	/**
	 * Merge combinations of graph patterns
	 */
	public void buildQueryBySyntax5() {
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;       
        Var var1 = Var.alloc("USRPB200210_0") ;       
        Triple t1 = new Triple(var1, RDF.Nodes.type, NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#USRPB200-210"));
        elg.addTriplePattern(t1);
        ElementGroup elgSub1 = new ElementGroup();
        
        elgSub1.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWeight"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Weight_350g")));
        
        ElementGroup elgSub2 = new ElementGroup();       
        elgSub2.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWidth"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Width_9.7cm")));
        
        ElementGroup elgSub3 = new ElementGroup();
        Var var2 = Var.alloc("DataValue0");
        elgSub3.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWidth"), var2));
  
        ElementGroup elgSub4 = new ElementGroup();
        Var var3 = Var.alloc("DataValue1");
        elgSub4.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWidth"), var3));
        //elgSub1 & elgSub2 !elgSub3 !elgSub4
        /*elgSub2.addElement(elgSub3);
        elg.addElement(new ElementMinus(elgSub2));*/
        /*elg.addElement(new ElementNotExists(elgSub1));
        elg.addElement(elgSub2);
        elg.addElement(new ElementNotExists(elgSub3));
        elg.addElement(new ElementNotExists(elgSub4));*/
  
        //elgSub1 & elgSub2 | elgSub3 & elgSub4
        /*elgSub1.addElement(elgSub2);
        elgSub1.addElement(elgSub3);
        elg.addElement(elgSub1);
        elgSub3.addElement(elgSub4);
        ElementUnion temp = new ElementUnion(elgSub2);
        temp.addElement(elgSub3);
        //(new ElementUnion(elgSub3)).addElement(elgSub2);
        elgSub1.addElement(temp);
        elg.addElement(elgSub1);*/
        LinkedList<ElementGroup> list = new LinkedList<>();
        list.offer(elg);
        list.offer(elgSub1);
        list.offer(elgSub2);
        list.offer(elgSub3);
        list.offer(elgSub4);
        list.offer(elgSub4);
           
        /*elgSub1 | elgSub2 | elgSub3
        ElementUnion sub1 = new ElementUnion(elgSub1);
        sub1.addElement(elgSub2);
        ElementUnion sub2 = new ElementUnion(sub1);
        sub2.addElement(elgSub3);
        elg.addElement(sub2);*/
        
        /*elgSub1 & elgSub2 !elgSub3
        elgSub1.addElement(elgSub2);
        elgSub1.addElement(new ElementMinus(elgSub3));
        elg.addElement(elgSub1);*/
 
        /*elgSub1 & elgSub2 | elgSub3
        elgSub1.addElement(elgSub2);
        ElementUnion sub1 = new ElementUnion(elgSub1);
        sub1.addElement(elgSub3);
        elg.addElement(sub1);*/
  
        /*elgSub1 | elgSub2 & elgSub3
        ElementUnion sub1 = new ElementUnion(elgSub1);
        sub1.addElement(elgSub2);
        ElementGroup sub2 = new ElementGroup();
        sub2.addElement(sub1);
        sub2.addElement(elgSub3);
        elg.addElement(sub2);*/
        
        /*elgSub1 | elgSub2 ! elgSub3
        ElementUnion sub1 = new ElementUnion(elgSub1);
        sub1.addElement(elgSub2);
        ElementGroup sub2 = new ElementGroup();
        sub2.addElement(sub1);
        sub2.addElement(new ElementMinus(elgSub3));
        elg.addElement(sub2);*/
           
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.setQueryPattern(joinGraphPatterns(list));
        query.addGroupBy(var1);
        Aggregator egg = AggregatorFactory.createCountExpr(true, new ExprVar(var2));
        query.addHavingCondition(new E_LessThan(query.allocAggregate(egg), NodeValue.makeNodeInteger(2)));
        query.setDistinct(true);
        query.addResultVar(var1);
       
        //Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        //System.out.println(op.getClass().getName());
        query = OpAsQuery.asQuery(op);
               
        Map<String, String> prefixMapper = new HashMap<>();
        prefixMapper.put("SDR", "http://ece.neu.edu/ontologies/SDR.owl#");
        prefixMapper.put("RFDevice", "http://ece.neu.edu/ontologies/RFDevice.owl#");
        prefixMapper.put("Nuvio", "http://cogradio.org/ont/Nuvio.owl#");
           
        //prefixMapper.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        query.getPrefixMapping().setNsPrefixes(prefixMapper);
        query.serialize(new IndentedWriter(System.out,false), Syntax.syntaxSPARQL_11) ;
        System.out.println() ;	
        //dumpIntoFile(query, 1);
	}

	/**
	 * Filter expressions
	 */
	public void buildQueryBySyntax6() {
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;       
        Var var1 = Var.alloc("USRPB200210_0") ;       
        Triple t1 = new Triple(var1, RDF.Nodes.type, NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#USRPB200-210"));
        elg.addTriplePattern(t1);
        Var dataValue = Var.alloc("DataValue0");
        elg.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasDecimalValue"), dataValue));
        String str1 = "92.99";
        Expr expr1 = new E_LessThanOrEqual(new ExprVar(dataValue), NodeValue.makeNodeDecimal(str1));
        expr1 = new E_LogicalNot(expr1);
        Expr expr2 = new E_GreaterThanOrEqual(new ExprVar(dataValue), NodeValue.makeNodeFloat("-90"));
        Expr expr = new E_LogicalOr(expr1, expr2);
        elg.addElementFilter(new ElementFilter(expr));
        
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.setQueryPattern(elg);
        query.addGroupBy(var1);
        Aggregator egg = AggregatorFactory.createCountExpr(true, new ExprVar(var1));
        query.addHavingCondition(new E_LessThan(query.allocAggregate(egg), NodeValue.makeNodeInteger(2)));
        query.setDistinct(true);
        query.addResultVar(var1);
       
        //Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        //System.out.println(op.getClass().getName());
        query = OpAsQuery.asQuery(op);
               
        Map<String, String> prefixMapper = new HashMap<>();
        prefixMapper.put("SDR", "http://ece.neu.edu/ontologies/SDR.owl#");
        prefixMapper.put("RFDevice", "http://ece.neu.edu/ontologies/RFDevice.owl#");
        prefixMapper.put("Nuvio", "http://cogradio.org/ont/Nuvio.owl#");
        prefixMapper.put("xsd", "http://www.w3.org/2001/XMLSchema#");
           
        //prefixMapper.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        query.getPrefixMapping().setNsPrefixes(prefixMapper);
        query.serialize(new IndentedWriter(System.out,false), Syntax.syntaxSPARQL_11) ;
        System.out.println() ;	
        //dumpIntoFile(query, 1);
	}

	private ElementGroup joinGraphPatterns(LinkedList<ElementGroup> list) {
		if (list == null)
			return null;
		if (list.isEmpty())
			return new ElementGroup();
		if (list.size() == 1)
			return list.poll();
		
		LinkedList<ElementGroup> subList = new LinkedList<>();
		int count = ran.nextInt(list.size() - 1) + 1;
		while(count > 0) {
			subList.offer(list.poll());
			count--;
		}
		ElementGroup left = joinGraphPatterns(subList);
		ElementGroup right = joinGraphPatterns(list);
		double ranDouble = ran.nextDouble();
		if (ranDouble < conjunctionGraphPatternProbability) {
			left.addElement(right);
			return left;
		}
		else if (ranDouble < conjunctionGraphPatternProbability + unionGraphPatternProbability) {
			ElementGroup group = new ElementGroup();
			ElementUnion union = new ElementUnion(left);
			union.addElement(right);
			group.addElement(union);
			return group;
		}
		else if (ranDouble < conjunctionGraphPatternProbability + unionGraphPatternProbability + optionalGraphPatternProbability) {
			left.addElement(new ElementOptional(right));
			return left;
		}
		else {
			count = ran.nextInt(3);
			switch (count) {
			case 0:
				left.addElement(new ElementNotExists(right));
				break;
			case 1:
				left.addElement(new ElementExists(right));
				break;
			case 2:
				left.addElement(new ElementMinus(right));
				break;
			}
			return left;
		}		
	}
	
	public void buildQueryBySyntax7() {
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;       
        Var var1 = Var.alloc("USRPB200210_0") ;       
        Triple t1 = new Triple(var1, RDF.Nodes.type, NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#USRPB200-210"));
        elg.addTriplePattern(t1);
        ElementGroup elgSub1 = new ElementGroup();
        
        elgSub1.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWeight"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Weight_350g")));
        
        ElementGroup elgSub2 = new ElementGroup();       
        elgSub2.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWidth"), NodeFactory.createURI("http://ece.neu.edu/ontologies/SDR.owl#Width_9.7cm")));
        
        ElementGroup elgSub3 = new ElementGroup();
        Var var2 = Var.alloc("DataValue0");
        elgSub3.addTriplePattern(new Triple(var1, NodeFactory.createURI("http://ece.neu.edu/ontologies/RFDevice.owl#hasWidth"), var2));
            
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.setQueryPattern(elg);
        elg.addElement(elgSub1);
        
        query.addGroupBy(var1);
        Aggregator egg = AggregatorFactory.createCountExpr(true, new ExprVar(var2));
        query.addHavingCondition(new E_LessThan(query.allocAggregate(egg), NodeValue.makeNodeInteger(2)));
        query.setDistinct(true);
        query.addResultVar(var1);
       
        //Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        //System.out.println(op.getClass().getName());
        query = OpAsQuery.asQuery(op);
               
        Map<String, String> prefixMapper = new HashMap<>();
        prefixMapper.put("SDR", "http://ece.neu.edu/ontologies/SDR.owl#");
        prefixMapper.put("RFDevice", "http://ece.neu.edu/ontologies/RFDevice.owl#");
        prefixMapper.put("Nuvio", "http://cogradio.org/ont/Nuvio.owl#");
           
        //prefixMapper.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        query.getPrefixMapping().setNsPrefixes(prefixMapper);
        query.serialize(new IndentedWriter(System.out,false), Syntax.syntaxSPARQL_11) ;
        System.out.println() ;	
        //dumpIntoFile(query, 1);
	}
	
	public void dumpIntoFile(Query query, int queryID) {
		File sparqlQueryFile = FileUtil.createFile("SPARQLquery" + File.separator + "query" + queryID + ".rq");
	
		/* 
		try {
			query.serialize(new IndentedWriter(new FileOutputStream(sparqlQueryFile), false), Syntax.syntaxSPARQL_11);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		try {
			FileUtils.writeStringToFile(sparqlQueryFile, query.serialize(Syntax.syntaxSPARQL_11), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("There was an error while dumping into file.", e);
		}	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ARQExamples().buildQueryBySyntax5();

	}

}
