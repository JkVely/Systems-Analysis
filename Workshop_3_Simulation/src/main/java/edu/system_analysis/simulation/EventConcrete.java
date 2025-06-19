package edu.system_analysis.simulation;

public class EventConcrete extends Event {
  
  public double weight;
  public Event handler;

    public EventConcrete(double weight) {
        this.weight = weight;
    }
    
    @Override
    public double probabilityFunction(double entry) {
      if (this.handler != null) {
        entry = this.handler.result(entry);
      }
      return this.weight * entry;
    }
    @Override 
    public void setNext(Event handler) {
      this.handler = handler;
    }

    @Override
    public double result(double entry) {
      return this.probabilityFunction(entry);
    }

    @Override
    public void setWeight(double weight) {
      this.weight = weight;
    }
  
}
