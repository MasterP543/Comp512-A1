package Server.TCP;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {
    public String method;
    public List<Object> args;

    public Request(String method, List<Object> args) {
        this.method = method;
        this.args = args;
    }
}

