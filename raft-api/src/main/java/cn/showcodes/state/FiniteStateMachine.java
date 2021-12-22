package cn.showcodes.state;

public interface FiniteStateMachine<T extends Transition> {
    void transmit(T transition);
}
