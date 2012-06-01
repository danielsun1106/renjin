package org.renjin.primitives.io;

import java.io.IOException;
import java.io.PrintWriter;

import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.io.connections.Connection;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.Raw;
import org.renjin.sexp.RawVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SexpVisitor;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


public class Cat extends SexpVisitor<String> {

  @Primitive
  public static void cat(ListVector list, Connection connection, String sep,
      SEXP fill, SEXP labels, boolean append) throws IOException {
    
    PrintWriter printWriter = connection.getPrintWriter();
    Cat visitor = new Cat(printWriter, sep, 0);
    for (SEXP element : list) {
      element.accept(visitor);
    }
    printWriter.flush();
  }
  
  
  private final PrintWriter writer;
  private String separator;
  private boolean needsSeparator = false;
  private int fill;

  private Cat(PrintWriter writer, String separator, int fill) {
    this.writer = writer;
    this.separator = separator;
    this.fill = fill;
  }

  @Override
  public void visit(StringVector vector) {
    catVector(vector);
  }

  @Override
  public void visit(IntVector vector) {
    catVector(vector);
  }

  @Override
  public void visit(LogicalVector vector) {
    catVector(vector);
  }

  @Override
  public void visit(Null nullExpression) {
    // do nothing
  }

  @Override
  public void visit(DoubleVector vector) {
    catVector(vector);
  }

  private void catVector(AtomicVector vector) {
    for (int i = 0; i != vector.length(); ++i) {
      catElement(vector.getElementAsString(i));
    }
  }

  @Override
  public void visit(Symbol symbol) {
    catElement(symbol.getPrintName());
  }

  private void catElement(String element) {
    if (needsSeparator) {
      writer.print(separator);
    } else {
      needsSeparator = true;
    }
    writer.print(element);
  }

  @Override
  public void visit(RawVector vector) {
    catVector(vector);
  }

  public void visit(Raw raw) {
    catVector(new RawVector(raw));
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new EvalException(
        "argument of type '%s' cannot be handled by 'cat'", exp.getTypeName());
  }

}