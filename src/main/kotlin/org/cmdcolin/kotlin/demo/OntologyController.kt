package org.cmdcolin.kotlin.demo

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
class OntologyController {

    fun traverseStart( model : OntModel ) {
        for (tmp in model.listHierarchyRootClasses()) {
            traverse( tmp, mutableListOf<OntClass>(), 0 );
        }
    }
    
    fun traverse( oc : OntClass, occurs : MutableList<OntClass>, depth : Int ) {
    	if( oc.getLocalName() == null || oc.getLocalName().equals( "Nothing" ) ) return;
		
        for(i in 1..depth) {
            print("\t")
		}
		println( oc.toString() );

		
        if ( oc.canAs( OntClass::class.java ) && !occurs.contains( oc ) ) {
            for ( subClass in oc.listSubClasses( true ) ) {
                occurs.add( oc );
                traverse( subClass, occurs, depth + 1 );
                occurs.remove( oc );
            }
        }
    	
    }
    @GetMapping("/ontology")
    fun ontology(@RequestParam(value = "name", defaultValue = "MeatPizza") name: String) : List<String> {
        val sourceURL = "pizza.owl";
        val namespace = sourceURL + "#";
        val base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        base.read( sourceURL, "RDF/XML" );

        val inf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, base );
		traverseStart( inf );
        var list = mutableListOf<String>();

        return list
    }

}
