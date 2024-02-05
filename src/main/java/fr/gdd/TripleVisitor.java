package fr.gdd;


import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;

import java.util.HashSet;
import java.util.Set;

public class TripleVisitor extends OpVisitorBase {
    private Set<Triple> triples;

    public TripleVisitor() {
        this.triples = new HashSet<>();
    }

    public Set<Triple> getTriples() {
        return triples;
    }

    @Override
    public void visit(OpBGP opBGP) {
        BasicPattern pattern = opBGP.getPattern();
        for (Triple triple : pattern) {
            Node s = triple.getSubject();
            Node p = triple.getPredicate();
            Node o = triple.getObject();
            if (s.isVariable()) {
                s = Var.alloc("s");
            }
            if (p.isVariable()) {
                p = Var.alloc("p");
            }
            if (o.isVariable()) {
                o = Var.alloc("o");
            }
            triple = Triple.create(s,p,o);
            triples.add(triple);
        }

    }

    @Override
    public void visit(OpTriple opTriple) {
        triples.add(opTriple.getTriple());
    }

    @Override
    public void visit(OpJoin opJoin) {
        opJoin.getLeft().visit(this);
        opJoin.getRight().visit(this);
    }

    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        opLeftJoin.getLeft().visit(this);
        opLeftJoin.getRight().visit(this);
    }
    @Override
    public void visit(OpFilter opFilter) {
        opFilter.getSubOp().visit(this);

    }

    @Override
    public void visit(OpProject opProject) {
        opProject.getSubOp().visit(this);

    }

    @Override
    public void visit(OpDistinct opDistinct) {
        opDistinct.getSubOp().visit(this);
    }

    @Override
    public void visit(OpOrder opOrder) {
        opOrder.getConditions();

    }

    @Override
    public void visit(OpUnion opUnion) {
        opUnion.getLeft().visit(this);
        opUnion.getRight().visit(this);
    }



    @Override
    public void visit(OpSlice opSlice) {
        long start = opSlice.getStart();
        long length = opSlice.getLength();
        opSlice.getSubOp().visit(this);
    }

    @Override
    public void visit(OpTopN opTop) {
        long limit = opTop.getLimit();
        opTop.getSubOp().visit(this);
    }



}
