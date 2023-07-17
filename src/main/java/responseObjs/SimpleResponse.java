package responseObjs;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SimpleResponse {
    public boolean status;
    public Map<String, Integer> data;
}
