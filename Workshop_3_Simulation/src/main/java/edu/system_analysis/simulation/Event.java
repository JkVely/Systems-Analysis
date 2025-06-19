package edu.system_analysis.simulation;

public abstract class Event {
  public abstract double probabilityFunction(double entry);
  public abstract double result(double entry);
  public abstract void setWeight(double weight);
  public abstract void setNext(Event handler);
}
