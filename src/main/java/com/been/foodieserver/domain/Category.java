package com.been.foodieserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Getter
@ToString(callSuper = true)
@SQLDelete(sql = "UPDATE categories SET deleted_at = NOW() where id = ?")
@SQLRestriction("deleted_at is NULL")
@Table(name = "categories")
@Entity
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    protected Category() {
    }

    private Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Category of(String name, String description) {
        return new Category(name, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Category category)) {
            return false;
        }

        return Objects.equals(getId(), category.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
