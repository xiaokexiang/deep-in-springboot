package top.leejay.springboot.chapter11;

import org.springframework.context.ApplicationEvent;

/**
 * @author xiaokexiang
 */
public class EatEvent extends ApplicationEvent {
    private String name;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public EatEvent(Object source) {
        super(source);
        this.name = (String) source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
