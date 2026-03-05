package claudodg.customerapi.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;



    @Column(nullable = false, length = 60)
    private String password;

    private String role = "USER";
}
