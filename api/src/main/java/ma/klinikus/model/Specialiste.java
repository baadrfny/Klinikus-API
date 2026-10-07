package ma.klinikus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "specialistes")
public class Specialiste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}