package garcia.personal.MatMagic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@Configuration
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:secrets.properties")
})
public class MatMagic {

    public static void main(String[] args) {
        SpringApplication.run(MatMagic.class, args);
    }

}
