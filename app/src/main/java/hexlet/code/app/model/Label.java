package hexlet.code.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "labels")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Label implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank
    @Size(min = 3, max = 1000)
    private String name;

    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "labels")
    private List<Task> tasks = new ArrayList<>();

}
