package edu.system_analysis.simulation;

public class EventFactory {
  
  public Event generateEvent(double weight) {
    return new EventConcrete(weight);
  }
}
