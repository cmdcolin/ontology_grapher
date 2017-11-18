import spark.Spark.*
import com.google.gson.Gson
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;





fun traverse( oc : OntClass, occurs : MutableList<OntClass>, depth : Int, dataset : HashMap<String, OntologyClass> ) {
    if( oc.getLocalName() == null || oc.getLocalName().equals( "Nothing" ) ) return;
    
    for(i in 1..depth) {
        print("\t")
    }
    var parents = mutableListOf<String>();
    var cl = OntologyClass(oc.getLocalName(), parents);
    var list = mutableListOf<String>();

    if ( oc.canAs( OntClass::class.java ) && !occurs.contains( oc ) ) {
        for ( subClass in oc.listSuperClasses( true ) ) {
            occurs.add( oc );
            if(subClass.getLocalName() != null) {
                parents.add(subClass.getLocalName());
                traverse( subClass, occurs, depth + 1, dataset );
            }
            occurs.remove( oc );
        }
        dataset.put(oc.getLocalName(), cl);
    }
    
}
fun main(args: Array<String>) {
    
    get("/ontology") { req, res ->
        val sourceURL = "pizza.owl";
        val namespace = sourceURL + "#";
        val base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        base.read( sourceURL, "RDF/XML" );

        val inf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, base );
        var asp = inf.getOntClass( "http://www.co-ode.org/ontologies/pizza/pizza.owl#Napoletana");
        var dataset = hashMapOf<String, OntologyClass>();
        traverse( asp, mutableListOf<OntClass>(), 0, dataset );
        res.header("Access-Control-Allow-Origin", "*")
        res.type("application/json")
        val gson = Gson()
        gson.toJson(dataset);
    }
}

