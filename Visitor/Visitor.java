package Visitor;

import SimpleFactor.Fruit;

import java.awt.print.Book;

public interface Visitor {
    void visit(Book book);
    void visit(Fruit fruit);
}
