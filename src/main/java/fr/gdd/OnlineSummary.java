package fr.gdd;

import fr.gdd.fedup.summary.Summary;
import fr.gdd.fedup.summary.SummaryFactory;
import fr.gdd.sage.generics.LazyIterator;
import fr.gdd.sage.interfaces.BackendIterator;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class OnlineSummary {

    @Disabled

    public static void summary_WA(String pathToDataset, String pathToQueryFolder, String pathToSummary) throws IOException {
        //Read TDB2 dataset
        JenaBackend backend = new JenaBackend(pathToDataset);
        LazyIterator<NodeId, ?> gspo = (LazyIterator<NodeId, ?>) backend.search(backend.any(), backend.any(), backend.any(), backend.any());
        ProgressJenaIterator gspoR = (ProgressJenaIterator) gspo.iterator;
        Set<NodeId> graph = new HashSet<>();
        while (gspo.hasNext()) {
            gspo.next();
            graph.add(gspo.getId(SPOC.GRAPH));
        }

        Summary hgs = SummaryFactory.createModuloOnSuffix(1, Location.create(pathToSummary));//
        //Read Query and decompose
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

        for (NodeId g:graph) {
            for (Triple t : tripleSet) {
                NodeId s = (t.getSubject().isVariable()) ? backend.any() : backend.getId(t.getSubject());
                NodeId p = (t.getPredicate().isVariable()) ? backend.any() : backend.getId(t.getPredicate());
                NodeId o = (t.getObject().isVariable()) ? backend.any() : backend.getId(t.getObject());

                BackendIterator<NodeId, ?> spo = backend.search(s, p, o, g);
                // build summary from running query
                while(spo.hasNext()){
                    spo.next();
                    Quad q = new Quad(backend.getNode(spo.getId(SPOC.GRAPH)),
                            backend.getNode(spo.getId(SPOC.SUBJECT)),
                            backend.getNode(spo.getId(SPOC.PREDICATE)),
                            backend.getNode(spo.getId(SPOC.OBJECT)));
                    hgs.add(q);
                }
            }
        }
        hgs.getSummary().begin(ReadWrite.READ);
        Integer value = (int) hgs.getSummary().getUnionModel().size();
        System.out.println(value);
        hgs.getSummary().commit();
        hgs.getSummary().end();

    }

    @Disabled

    public static void summary_sampling_WA(String pathToDataset, String pathToQueryFolder, String pathToSummary, Integer groundtruth) throws IOException {

        JenaBackend backend = new JenaBackend(pathToDataset);
        LazyIterator<NodeId, ?> gspo = (LazyIterator<NodeId, ?>) backend.search(backend.any(), backend.any(), backend.any(), backend.any());
        ProgressJenaIterator gspoR = (ProgressJenaIterator) gspo.iterator;
        Set<NodeId> graph = new HashSet<>();
        while (gspo.hasNext()) {//this part takes time!!!
            gspo.next();
            graph.add(gspo.getId(SPOC.GRAPH));
        }

        Summary hgs = SummaryFactory.createModuloOnSuffix(1,Location.create(pathToSummary));

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
        while (value != groundtruth) {
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
        if (i * graph.size() % (graph.size()*10_000) == 0) {
        System.out.println(i * graph.size() + " " + value);
        }

        }

    }
    @Disabled
    public static void summary_sampling_SPO(String pathToDataset, String pathToSummary, Integer groundtruth) {

        Summary hgs = SummaryFactory.createModuloOnSuffix(1, Location.create(pathToSummary));
        JenaBackend backend = new JenaBackend(pathToDataset);
        LazyIterator<NodeId, ?> gspo = (LazyIterator<NodeId, ?>) backend.search(backend.any(), backend.any(), backend.any(), backend.any());
        Set<NodeId> graph = new HashSet<>();
        while (gspo.hasNext()) {
            gspo.next();
            graph.add(gspo.getId(SPOC.CONTEXT));

        }

        int i = 0;
        int value = 0;
        while (value != groundtruth) {
            for (NodeId g : graph) {
                LazyIterator<NodeId, ?> spo = (LazyIterator<NodeId, ?>) backend.search(backend.any(), backend.any(), backend.any(), g);
                ProgressJenaIterator spoR = (ProgressJenaIterator) spo.iterator;
                Pair<Tuple<NodeId>, Double> rWt = spoR.getRandomSPOWithProbability();
                Quad q = new Quad(backend.getNode(rWt.getKey().get(0)), backend.getNode(rWt.getKey().get(1)), backend.getNode(rWt.getKey().get(2)), backend.getNode(rWt.getKey().get(3)));
                hgs.add(q);

            }
            i++;
            hgs.getSummary().begin(ReadWrite.READ);
            value = (int) hgs.getSummary().getUnionModel().size();
            hgs.getSummary().commit();
            hgs.getSummary().end();
            if (i * graph.size() % (graph.size()*10_000) == 0) {
                System.out.println(i * graph.size() + " " + value);

            }
        }
    }
    public static void main(String[] args) throws IOException {
        String datasetFilePath = null;
        String queryFilePath = null;
        String summaryFilePath = null;
        Boolean spo_flag = false;
        Boolean wa_flag = false;
        Boolean sampling_flag = false;
        Integer gt = null;
        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--dataset") && i + 1 < args.length) {
                datasetFilePath = args[i + 1];
                i++;
            } else if (args[i].equals("--GT") && i + 1 < args.length) {
                try {
                    gt = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid ground truth value provided.");
                }
            }else if (args[i].equals("--query") && i + 1 < args.length) {
                queryFilePath = args[i + 1];
                i++;
            } else if (args[i].equals("--create_summary") && i + 1 < args.length) {
                summaryFilePath = args[i + 1];
            i++;
            } else if (args[i].equals("--spo")) {
                spo_flag = true;
            } else if (args[i].equals("--wa")) {
                wa_flag = true;
            }else if (args[i].equals("--sampling")) {
                sampling_flag = true;
            }
        }
        if (spo_flag == true  && wa_flag == false && sampling_flag == true){
            summary_sampling_SPO(datasetFilePath,summaryFilePath,gt);
        } else if (spo_flag == false  && wa_flag == true && sampling_flag == false) {
            summary_WA(datasetFilePath,queryFilePath,summaryFilePath);
        } else if (spo_flag == false  && wa_flag == true && sampling_flag == true) {
            summary_sampling_WA(datasetFilePath,queryFilePath,summaryFilePath,gt);
        }


    }


}