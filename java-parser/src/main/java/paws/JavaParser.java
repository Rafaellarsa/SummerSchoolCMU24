package paws;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONException;

public class JavaParser {

  public static boolean isJSONValid(String test) {
    try {
        new JSONObject(test);
    } catch (JSONException ex) {
        try {
            new JSONArray(test);
        } catch (JSONException ex1) {
            return false;
        }
    }
    return true;
  }

  public static void main(final String[] args) {
    if (args.length < 2) {
      System.out.println("You need at minimum two arguments, path to input file and export type");
      System.exit(-1);
    }

    final String input = args[0];
    final String exportType = args[1];

    if(!exportType.equals("db") && !exportType.equals("json")){
      System.out.println("Unsupported export type, supported types are 'db' and 'json'");
      System.exit(-1);
    }
    
    Export export = null;
		if (exportType.equals("db")) {
			if (args.length != 5) {
				System.out.println("You need to pass four arguments to the class: path to the input file, db user, db pass, db host /w port (e.g. localhost:3306)");
				System.exit(-1);
			}

			final String user = args[2];
			final String pass = args[3];
			final String port = args[4];
      export = new DB(user, pass, port);
    }else if(exportType.equals("json")){
      export = new JSONExport();
    }else{
      System.out.print("Error, invalid export type, use db or json");
      System.exit(-1);
    }

    try {
      List<String> codeLines;
      if(Files.exists(Paths.get(input))) {
        codeLines = Files.readAllLines(Paths.get(input));
        
      }else if(isJSONValid(input)){
        JSONArray json = new JSONArray(new JSONTokener(input));
        String codeString = json.getString(0);
        codeLines = new ArrayList<String>(Arrays.asList(codeString.split("\\r?\\n")));
      }else{
        System.out.print("Error, invalid filepath or json for input");
        System.exit(-1);
        codeLines = null;
      }

      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter, true);
      String id = "";
      Parser parser = null;
      Boolean isJson = export.getType().equals("json");

      for(int i=0; i<codeLines.size();i++) {
        String line = codeLines.get(i);
        if (line.startsWith("#C") | line.startsWith("EOF")) {
          if (!stringWriter.toString().isEmpty()) {
            parser = new Parser();
            parser.parseExample(export, id, stringWriter.toString(), false);
            parser.clearParserData();
            parser = null;
          }
          stringWriter = new StringWriter();
          writer = new PrintWriter(stringWriter, true);
          id = line.trim().substring(2);
        } else {
          writer.println(line);
        }
      }
      if(isJson){
        JSONExport j = (JSONExport)export;
        String s = j.exportJSON();
        System.out.println(s);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    } finally {
      if(export.getType().equals("db")){
        DB db = (DB)export;
        db.disconnectFromParser();
      }
    }
    if(export.getType().equals("db")){
      DB db = (DB)export;
      db.disconnectFromParser();
      System.out.println("Parsing is done!");
    }
  }
}
