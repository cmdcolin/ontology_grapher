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
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String) : Greeting {
        var sourceURL = "https://protege.stanford.edu/ontologies/pizza/pizza.owl";
        var namespace = sourceURL + "#";
        var base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        base.read( sourceURL, "RDF/XML" );
        println("HIHIHIHIHI")


        var inf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, base );

        var classes = inf.listClasses();
        while (classes.hasNext()) {
            var essaClasse = classes.next();

            var vClasse = essaClasse.getLocalName();
            if(vClasse != null)
                println("${vClasse}");
        }
//
//        System.out.println("\n---- Inferred assertions ----");
//        paper = inf.getIndividual( namespace + "paper1" );
//        for (i in paper.listRDFTypes(false)) {
//            print( "${paper.getURI()} is a ${i}" );
//        }

         return Greeting(counter.incrementAndGet(), "Hello, $name")
    }

}
