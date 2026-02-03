package io.proleap.cobol.preprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/* this keeps a record of how the preprocessing goes, so later stages can make subtle decisions
   based upon the provenence of a given line of code.
*/

public class PreprocessingInfo {

  ArrayList<SourceRecord> sourceRecords = new ArrayList();

  public String toString(){
    return String.format("<PreprocessingInfo %d>"+System.identityHashCode(this));
  }

  public static class SourceReplacement {
    public String replaceable;
    public String replacement;

    SourceReplacement(String replaceable, String replacement){
      this.replaceable = replaceable;
      this.replacement = replacement;
    }

    String asJSONString(){
      return String.format("{ \"%s\": \"%s\" }",replaceable, replacement);
    }
  }

  public static class SourceRecord {
    boolean isPush;
    String name;
    int position;
    ArrayList<SourceReplacement> sourceReplacements = new ArrayList();

    SourceRecord(boolean isPush,
                 String name,
                 int position){
      this.isPush = isPush;
      this.name = name;
      this.position = position;
    }

    public void addReplacement(String replaceable, String replacement){
      sourceReplacements.add(new SourceReplacement(replaceable,replacement));
    }

    public String toString(){
      return String.format("<SourceRecord %s %s at %d>",
                           (isPush ? "PUSH" : "POP"),
                           name,
                           position);
    }

    public String asJSONString(){
      StringJoiner joiner = new StringJoiner(",\n               ");
      sourceReplacements.forEach( r -> joiner.add(r.asJSONString()));
      return String.format("{ \"isPush\": %s, \"name\": \"%s\", \"position\": %d,\n    \"replacements\": [%s ] }",
                           (isPush? "true" : "false"),
                           this.name,
                           this.position,
                           joiner.toString());
    }
  }

  public List<SourceRecord> getSourceRecords(){
    return sourceRecords;
  }

  public SourceRecord push(String name,
                           int position){
    SourceRecord sourceRecord = new SourceRecord(true,name,position);
    sourceRecords.add(sourceRecord);
    return sourceRecord;
  }

  public void pop(String name,
                  int position){
    sourceRecords.add(new SourceRecord(false,name,position));
  }
    
}
