package jbf;

public
class YYlocation {

  public int lineno; // absolute linenumber in file, starting at zero
  public int charno; // character number in current line, starting at zero
  public int charno0; // absolute character number in file, starting at zero

  public YYlocation() {this(-1,-1,-1);}

  public YYlocation(int cn0, int ln, int cn) {setlocation(cn0,ln,cn);}

  public void setlocation(int cn0, int ln, int cn)
        {lineno = ln; charno = cn; charno0 = cn0;}

  public void setlocation(YYlocation l)
        {setlocation(l.charno0,l.lineno,l.charno);}

  public int lineno() {return lineno;}
  public int charno() {return charno;}
  public int charno0() {return charno0;}

};
