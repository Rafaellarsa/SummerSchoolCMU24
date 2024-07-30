package paws;

abstract class Export
{
    public String type;

    public abstract void insertContentConcept(final String title, final String concept, final int sline, int eline);
    
    public abstract void deleteConcept(final String question, final String[] conceptsToBeRemoved, final boolean isExample);

    public String getType(){
      return this.type;
    }
}
