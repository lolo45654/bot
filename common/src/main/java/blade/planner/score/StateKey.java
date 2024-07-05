package blade.planner.score;

import blade.Bot;

public class StateKey {
    public static StateKey key(String name) {
        return new StateKey(name);
    }

    public static StateKey key(String name, Producer producer) {
        return new StateKey(name, producer);
    }

    private final String name;
    private final Producer producer;

    protected StateKey(String name, Producer producer) {
        this.name = name;
        this.producer = producer;
    }

    protected StateKey(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public Double produce(Bot bot) {
        if (producer == null) return null;
        return producer.produce(bot);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateKey key)) return false;
        return name.equals(key.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "key[" + name + "]";
    }

    public interface Producer {
        double produce(Bot bot);
    }
}
