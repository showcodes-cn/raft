package cn.showcodes.state;

public interface FiniteStateMachine<T extends Transition> {
    FiniteStateMachine transmit(T transition);
}
