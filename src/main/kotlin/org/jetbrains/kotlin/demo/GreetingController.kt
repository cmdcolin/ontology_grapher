package org.jetbrains.kotlin.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong
import java.util.Iterator;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;



@RestController
class GreetingController {

    val counter = AtomicLong()

    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String) : List<String> {
        val sourceURL = "pizza.owl";
        val namespace = sourceURL + "#";
        val base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        base.read( sourceURL, "RDF/XML" );

        val inf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, base );

        var asp = inf.getOntClass( "http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatTopping");

        // Print each of its instances.
        for ( i in asp.listInstances() ) {
            println( i );
        }
        println("hi!");

        val classes = inf.listClasses();
        var list = mutableListOf<String>();
        while (classes.hasNext()) {
            val essaClasse = classes.next();
            list.add(essaClasse.toString());
        }

        return list
    }

}
