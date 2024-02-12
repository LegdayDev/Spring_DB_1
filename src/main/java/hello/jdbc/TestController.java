package hello.jdbc;

import org.springframework.stereotype.Controller;

@Controller
public class TestController {

    public String index(){
        return "hi";
    }
}
