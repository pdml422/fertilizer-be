package vn.edu.usth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "image")
public class Image {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "path")
    private String path;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "userId")
    private int userId;
}
