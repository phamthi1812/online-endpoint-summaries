package fr.gdd;

import fr.gdd.fedup.summary.Summary;
import fr.gdd.fedup.summary.SummaryFactory;
import fr.gdd.sage.generics.LazyIterator;
import fr.gdd.sage.interfaces.SPOC;
import fr.gdd.sage.jena.JenaBackend;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.trans.bplustree.ProgressJenaIterator;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.store.NodeId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SummaryTest {





    @Disabled
    @Test
    public void sampling_summary_triple_fedshop200() throws IOException {
        ProgressJenaIterator.NB_WALKS = 10_000;

        String pathToDataset= "/GDD/Thi/fedshop200/fedup-id";

        JenaBackend backend = new JenaBackend(pathToDataset);
        LazyIterator<NodeId, ?> gspo = (LazyIterator<NodeId, ?>) backend.search(backend.any(), backend.any(), backend.any(), backend.any());
        ProgressJenaIterator gspoR = (ProgressJenaIterator) gspo.iterator;
        Set<NodeId> graph = new HashSet<>();
        while (gspo.hasNext()) {
            gspo.next();
            graph.add(gspo.getId(SPOC.GRAPH));
        }
        Summary hgs = SummaryFactory.createModuloOnSuffix(1,Location.create("/GDD/Thi/TEST"));
        String pathToQueryFolder= "/GDD/RSFB/engines/FedUP-experiments/queries/new_fedshop_thi";
        List<ImmutablePair<String, String>> queriespair = Files.walk(Paths.get(pathToQueryFolder))
                .filter(p -> p.toString().endsWith(".sparql")&& !p.getFileName().toString().startsWith("."))
                .map(p -> {
                    try {
                        return new ImmutablePair<>(p.getFileName().toString(), new String(Files.readAllBytes(p)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();

        List<Query> queries = new ArrayList<>();
        for (ImmutablePair pair : queriespair) {
            queries.add(QueryFactory.create((String) pair.getRight()));
        }

        TripleVisitor visitor = new TripleVisitor();
        for (Query q : queries) {
            Op op = Algebra.compile(q);
            op.visit(visitor);
        }
        Set<Triple> tripleSet= visitor.getTriples();
        int i = 0;
        int value = 0;
        while (value != 5700) {
            for (NodeId g : graph) {
                for (Triple t : tripleSet) {
                    try {
                        NodeId s = (t.getSubject().isVariable()) ? backend.any() : backend.getId(t.getSubject());
                        NodeId p;
                        try {
                            p = (t.getPredicate().isVariable()) ? backend.any() : backend.getId(t.getPredicate());
                        } catch (Exception e) {
                            continue;
                        }
                        NodeId o = (t.getObject().isVariable()) ? backend.any() : backend.getId(t.getObject());

                        LazyIterator<NodeId, ?> spo = (LazyIterator<NodeId, ?>) backend.search(s, p, o, g);
                        ProgressJenaIterator spoR = (ProgressJenaIterator) spo.iterator;
                        Pair<Tuple<NodeId>, Double> rWt = spoR.getRandomSPOWithProbability();
                        if (rWt.getKey() != null) {
                            Quad q = new Quad(
                                    backend.getNode(rWt.getKey().get(0)),
                                    backend.getNode(rWt.getKey().get(1)),
                                    backend.getNode(rWt.getKey().get(2)),
                                    backend.getNode(rWt.getKey().get(3)));
                            hgs.add(q);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            i++;
            hgs.getSummary().begin(ReadWrite.READ);
            value = (int) hgs.getSummary().getUnionModel().size();
            hgs.getSummary().commit();
            hgs.getSummary().end();

            System.out.println(i * graph.size() + " " + value);
        }

    }
    @Disabled
    @Test
    public void summary_sampling_SPO() {
        String fedShop200 = "/GDD/Thi/fedshop200/fedup-id";
        String fedShop200Sum = "/GDD/Thi/fedshop200/fedup-h0";
        String pathtoSum = "/GDD/Thi/FedShopSum";
        String pathtoSum2 = "/GDD/Thi/LRB_SPO";
        String largeRDFbench = "/GDD/RSFB/engines/FedUP-experiments/backup/summaries/largerdfbench/fedup-id";
        String largeRDFbenchSum = "/GDD/RSFB/engines/FedUP-experiments/backup/summaries/largerdfbench/fedup-h0";
        //summary_sampling_SPO(fedShop200, fedShop200Sum,pathtoSum);

        //summary_sampling_SPO(largeRDFbench,largeRDFbenchSum,pathtoSum2);
    }

    @Disabled
    @Test
    public void distinct_element(){
        JenaBackend backendsum = new JenaBackend("/GDD/RSFB/engines/FedUP-experiments/backup/summaries/largerdfbench/fedup-h0");
        LazyIterator<NodeId, ?> gsposum = (LazyIterator<NodeId, ?>) backendsum.search(backendsum.any(), backendsum.any(), backendsum.any(), backendsum.any());
        ProgressJenaIterator gspoRsum = (ProgressJenaIterator) gsposum.iterator;

        Set<NodeId> distict_predicate = new HashSet<>();
        Set<NodeId> distict_subject = new HashSet<>();
        Set<NodeId> distict_object = new HashSet<>();
        Set<NodeId> graph = new HashSet<>();
        while (gsposum.hasNext()) {
            gsposum.next();
            graph.add(gsposum.getId(SPOC.GRAPH));
            distict_predicate.add(gsposum.getId(SPOC.PREDICATE));
            distict_subject.add(gsposum.getId(SPOC.SUBJECT));
            distict_object.add(gsposum.getId(SPOC.OBJECT));

        }
        System.out.println(distict_predicate.size());
        System.out.println(distict_subject.size());
        System.out.println(distict_object.size());

        for(NodeId n:distict_predicate){
            System.out.println(backendsum.getValue(n));
        }
    }

    @Disabled
    @Test
    public void test_read_incomplete_sum(){
        JenaBackend backendsum = new JenaBackend("/GDD/Thi/fedshop200/fedup-id");

        NodeId homepage = backendsum.getId("<http://xmlns.com/foaf/0.1/homepage>");
        LazyIterator<NodeId, ?> gsposum = (LazyIterator<NodeId, ?>) backendsum.search(backendsum.any(),homepage , backendsum.any(), backendsum.any());
        ProgressJenaIterator gspoRsum = (ProgressJenaIterator) gsposum.iterator;
        System.out.println(((ProgressJenaIterator) gsposum.iterator).count());
        Set<NodeId> distict_predicate = new HashSet<>();
        Set<NodeId> distict_subject = new HashSet<>();
        Set<NodeId> distict_object = new HashSet<>();
        Set<NodeId> graph = new HashSet<>();
        while (gsposum.hasNext()) {
            gsposum.next();
            graph.add(gsposum.getId(SPOC.GRAPH));
            distict_predicate.add(gsposum.getId(SPOC.PREDICATE));
            distict_subject.add(gsposum.getId(SPOC.SUBJECT));
            distict_object.add(gsposum.getId(SPOC.OBJECT));

        }
        System.out.println(distict_predicate.size());
        System.out.println(distict_subject.size());
        System.out.println(distict_object.size());

        for(NodeId n:distict_object){
            System.out.println(backendsum.getValue(n));
        }
        for(NodeId n:distict_subject){
            System.out.println(backendsum.getValue(n));
        }


    }


}


