package edu.system_analysis;
import edu.system_analysis.simulation.Event;
import edu.system_analysis.simulation.EventFactory;

public class App 
{
    public static void main( String[] args )
    {
        EventFactory factory = new EventFactory();
        Event phisicalActivity = factory.generateEvent(1);
        Event stress = factory.generateEvent(1);
        Event noise = factory.generateEvent(1);
        Event light = factory.generateEvent(1);
        phisicalActivity.setNext(stress);
        stress.setNext(noise);
        noise.setNext(light);
        for (int i = 0; i < 10; i++) {
            phisicalActivity.setWeight(Math.random());
            stress.setWeight(Math.random());
            noise.setWeight(Math.random());
            light.setWeight(Math.random());
            double result = phisicalActivity.result(1);
            System.out.println(result);
        }
    }
}
