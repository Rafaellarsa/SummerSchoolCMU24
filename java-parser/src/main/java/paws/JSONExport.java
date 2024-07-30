package paws;

import org.json.JSONObject;
import org.json.JSONArray;

// 
// Decompiled by Procyon v0.5.30
// 

public class JSONExport extends Export
{
  JSONArray payload;
	
    public JSONExport() {
      this.type = "json";
      this.payload = new JSONArray();
    }
    
    public void insertContentConcept(final String title, final String concept, final int sline, int eline) {
      JSONObject conceptObj = new JSONObject();
      conceptObj.put("id", title);
    	conceptObj.put("concept", concept);
    	conceptObj.put("sline", sline);
      conceptObj.put("eline", eline);
      
      this.payload.put(conceptObj.toString());
    }
    
    public void deleteConcept(final String question, final String[] conceptsToBeRemoved, final boolean isExample) {
    }

    public String exportJSON(){
      return this.payload.toString();
    }
}
