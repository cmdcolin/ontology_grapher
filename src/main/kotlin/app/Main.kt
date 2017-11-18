import spark.Spark.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;


fun main(args: Array<String>) {
    fun traverse( oc : OntClass, occurs : MutableList<OntClass>, depth : Int, dataset : MutableList<OntologyClass> ) {
        if( oc.getLocalName() == null || oc.getLocalName().equals( "Nothing" ) ) return;
        
        for(i in 1..depth) {
            print("\t")
        }
        var parents = mutableListOf<String>();
        var cl = OntologyClass(oc.toString(), parents);
        var list = mutableListOf<String>();

        println( oc.toString() );

        
        if ( oc.canAs( OntClass::class.java ) && !occurs.contains( oc ) ) {
            for ( subClass in oc.listSuperClasses( true ) ) {
                occurs.add( oc );
                parents.add(subClass.toString());
                traverse( subClass, occurs, depth + 1, dataset );
                occurs.remove( oc );
            }
            dataset.add(cl);
        }
        
    }
    get("/ontology"), req, res -> {
        val sourceURL = "pizza.owl";
        val namespace = sourceURL + "#";
        val base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        base.read( sourceURL, "RDF/XML" );

        val inf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, base );
        var asp = inf.getOntClass( "http://www.co-ode.org/ontologies/pizza/pizza.owl#Napoletana");
        var dataset = mutableListOf<OntologyClass>();
        traverse( asp, mutableListOf<OntClass>(), 0, dataset );
        res.header("Access-Control-Allow-Origin", "*")
        res.type("application/json")

        return dataset;
    }
}

